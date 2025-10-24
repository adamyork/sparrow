package com.github.adamyork.sparrow.game.engine

import com.github.adamyork.sparrow.common.AudioQueue
import com.github.adamyork.sparrow.common.Sounds
import com.github.adamyork.sparrow.game.data.*
import com.github.adamyork.sparrow.game.engine.data.CollisionBoundaries
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.awt.Rectangle
import java.awt.image.BufferedImage

@Component
class Collision {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(Collision::class.java)
        const val COLLISION_COLOR_VALUE: Int = -16711906
    }

    lateinit var collisionImage: BufferedImage

    fun getCollisionBoundaries(player: Player): CollisionBoundaries {
        val left = findEdge(player.x, player, collisionImage, Direction.LEFT)
        val right = findEdge(player.x, player, collisionImage, Direction.RIGHT)
        val floor = findFloor(player.y, player, collisionImage)
        val ceiling = findCeiling(player.y, player, collisionImage)
        return CollisionBoundaries(left, right, ceiling, floor)
    }

    fun recomputeXBoundaries(
        player: Player,
        previousBoundaries: CollisionBoundaries
    ): CollisionBoundaries {
        val left = findEdge(player.x, player, collisionImage, Direction.LEFT)
        val right = findEdge(player.x, player, collisionImage, Direction.RIGHT)
        return CollisionBoundaries(left, right, previousBoundaries.top, previousBoundaries.bottom)
    }

    fun checkForItemCollision(
        player: Player,
        gameMap: GameMap,
        audioQueue: AudioQueue
    ): GameMap {
        var gameState = gameMap.state
        val managedMapItems = gameMap.items.map { item ->
            val itemRect = Rectangle(item.x, item.y, item.width, item.height)
            val playerRect = Rectangle(player.x, player.y, player.width, player.height)
            var nextItemState = item.state
            var nextFrameMetaData = item.frameMetadata
            if (playerRect.intersects(itemRect) && nextItemState == MapItemState.ACTIVE) {
                if (item.type == MapItemType.FINISH) {
                    LOGGER.info("finish reached")
                    gameState = GameMapState.COMPLETED
                    nextItemState = MapItemState.INACTIVE
                } else {
                    LOGGER.info("item collision")
                    nextItemState = MapItemState.DEACTIVATING
                    audioQueue.queue.add(Sounds.ITEM_COLLECT)
                    nextFrameMetaData = item.getFirstDeactivatingFrame()
                }
            }
            if (item.type == MapItemType.FINISH) {
                MapFinishItem(item.width, item.height, item.x, item.y, item.type, nextItemState, nextFrameMetaData)
            } else {
                MapItem(item.width, item.height, item.x, item.y, item.type, nextItemState, nextFrameMetaData)
            }
        }.toCollection(ArrayList())
        return gameMap.from(gameState, managedMapItems)
    }

    fun checkForEnemyCollision(
        player: Player,
        gameMap: GameMap,
        viewPort: ViewPort,
        audioQueue: AudioQueue,
        particles: Particles
    ): Pair<Player, GameMap> {
        var managedMapParticles = gameMap.particles
        var playerIsColliding = false
        val managedMapEnemies = gameMap.enemies.map { enemy ->
            val enemyRect = Rectangle(enemy.x, enemy.y, enemy.width, enemy.height)
            val playerRect = Rectangle(player.x, player.y, player.width, player.height)
            var isColliding = false
            if (playerRect.intersects(enemyRect)) {
                LOGGER.info("enemy collision !")
                audioQueue.queue.add(Sounds.PLAYER_COLLISION)
                managedMapParticles = particles.createCollisionParticles(enemy.x, enemy.y)
                isColliding = true
                playerIsColliding = true
            }
            val frameMetadata = enemy.getNextFrameCell()
            enemy.from(frameMetadata, isColliding)

        }.toCollection(ArrayList())
        var nextX = player.x
        var nextVx = player.vx
        if (playerIsColliding) {
            val collisionRebound: Int = player.width
            if (player.direction == Direction.LEFT) {
                nextX += collisionRebound
                if (nextX >= viewPort.width - player.width) {
                    nextX = viewPort.width - player.width - 1
                }
            } else {
                nextX -= collisionRebound
                if (nextX < 0) {
                    nextX = 0
                }
            }
            nextVx = 0.0
        }
        return Pair(
            player.from(nextX, nextVx, playerIsColliding),
            gameMap.from(managedMapEnemies, managedMapParticles)
        )
    }

    private fun findFloor(
        startY: Int,
        player: Player,
        collisionImage: BufferedImage
    ): Int {
        if (startY >= collisionImage.height) {
            return collisionImage.height - player.height
        }
        val normalizedX = player.x.coerceAtMost(collisionImage.width - player.width)
        return if (testForColorCollision(normalizedX, startY, player.width, 1, collisionImage)) {
            startY - player.height
        } else {
            findFloor(startY + 1, player, collisionImage)
        }
    }

    private fun findCeiling(startY: Int, player: Player, collisionImage: BufferedImage): Int {
        if (startY <= 0) {
            return 0
        }
        val normalizedX = player.x.coerceAtMost(collisionImage.width - player.width)
        return if (testForColorCollision(normalizedX, startY, player.width, 1, collisionImage)) {
            startY + 1
        } else {
            findCeiling(startY - 1, player, collisionImage)
        }
    }

    private fun findEdge(
        startX: Int,
        player: Player,
        collisionImage: BufferedImage,
        direction: Direction
    ): Int {
        if (startX < 0) {
            return 0
        }
        if (startX > collisionImage.width - player.width) {
            return collisionImage.width - player.width
        }
        return if (testForColorCollision(startX, player.y, 1, player.height, collisionImage)) {
            if (direction == Direction.RIGHT) {
                startX - player.width
            } else {
                startX
            }
        } else {
            if (direction == Direction.RIGHT) {
                findEdge(startX + 1, player, collisionImage, direction)
            } else {
                findEdge(startX - 1, player, collisionImage, direction)
            }
        }
    }

    fun testForColorCollision(x: Int, y: Int, width: Int, height: Int, collisionImage: BufferedImage): Boolean {
        try {
            val collisionBitMap =
                collisionImage.getRGB(x, y, width, height, null, 0, width)
            return collisionBitMap.contains(COLLISION_COLOR_VALUE)
        } catch (exception: Exception) {
            LOGGER.info("ArrayIndexOutOfBoundsException x $x y:$y width:$width height:$height $exception")
            return true
        }
    }
}