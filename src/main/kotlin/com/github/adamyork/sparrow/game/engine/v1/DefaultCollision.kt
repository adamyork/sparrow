package com.github.adamyork.sparrow.game.engine.v1

import com.github.adamyork.sparrow.common.AudioQueue
import com.github.adamyork.sparrow.common.Sounds
import com.github.adamyork.sparrow.game.data.Direction
import com.github.adamyork.sparrow.game.data.Player
import com.github.adamyork.sparrow.game.data.ViewPort
import com.github.adamyork.sparrow.game.data.enemy.MapEnemyType
import com.github.adamyork.sparrow.game.data.item.MapItemState
import com.github.adamyork.sparrow.game.data.item.MapItemType
import com.github.adamyork.sparrow.game.data.map.GameMap
import com.github.adamyork.sparrow.game.data.map.GameMapState
import com.github.adamyork.sparrow.game.engine.Collision
import com.github.adamyork.sparrow.game.engine.Particles
import com.github.adamyork.sparrow.game.engine.Physics
import com.github.adamyork.sparrow.game.engine.data.CollisionBoundaries
import com.github.adamyork.sparrow.game.engine.data.ParticleType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Rectangle
import java.awt.geom.Point2D
import java.awt.image.BufferedImage

class DefaultCollision : Collision {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(DefaultCollision::class.java)
        const val COLLISION_COLOR_VALUE: Int = -16711906
    }

    val physics: Physics

    constructor(physics: Physics) {
        this.physics = physics
    }

    override lateinit var collisionImage: BufferedImage

    override fun getCollisionBoundaries(player: Player): CollisionBoundaries {
        val left = findEdge(player.x, player, collisionImage, Direction.LEFT)
        val right = findEdge(player.x, player, collisionImage, Direction.RIGHT)
        val floor = findFloor(player.y, player, collisionImage)
        val ceiling = findCeiling(player.y, player, collisionImage)
        return CollisionBoundaries(left, right, ceiling, floor)
    }

    override fun recomputeXBoundaries(
        player: Player,
        previousBoundaries: CollisionBoundaries
    ): CollisionBoundaries {
        val left = findEdge(player.x, player, collisionImage, Direction.LEFT)
        val right = findEdge(player.x, player, collisionImage, Direction.RIGHT)
        return CollisionBoundaries(left, right, previousBoundaries.top, previousBoundaries.bottom)
    }

    override fun checkForItemCollision(
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
            item.from(nextItemState, nextFrameMetaData)
        }.toCollection(ArrayList())
        return gameMap.from(gameState, managedMapItems)
    }

    override fun checkForEnemyCollisionAndProximity(
        player: Player,
        gameMap: GameMap,
        viewPort: ViewPort,
        audioQueue: AudioQueue,
        particles: Particles
    ): Pair<Player, GameMap> {
        var managedMapParticles = gameMap.particles
        var playerIsColliding = false
        val managedMapEnemies = gameMap.enemies.map { enemy ->
            var isColliding = false
            if (enemy.state != MapItemState.INACTIVE) {
                val enemyRect = Rectangle(enemy.x, enemy.y, enemy.width, enemy.height)
                val playerRect = Rectangle(player.x, player.y, player.width, player.height)
                if (playerRect.intersects(enemyRect)) {
                    LOGGER.info("enemy collision !")
                    audioQueue.queue.add(Sounds.PLAYER_COLLISION)
                    managedMapParticles = particles.createCollisionParticles(enemy.x, enemy.y)
                    isColliding = true
                    playerIsColliding = true
                }
                if (enemy.type == MapEnemyType.BOT) {
                    val dist =
                        Point2D.distance(
                            player.x.toDouble(),
                            player.y.toDouble(),
                            enemy.x.toDouble(),
                            enemy.y.toDouble()
                        )
                            .toInt()
                    if (dist <= 200) {
                        LOGGER.info("enemy shoots")
                        val managedProjectileParticles =
                            particles.createProjectileParticle(player, enemy, gameMap.particles)
                        managedMapParticles.addAll(managedProjectileParticles)
                    }
                }
                val frameMetadata = enemy.getNextFrameCell()
                enemy.from(frameMetadata, isColliding)
            } else {
                enemy
            }
        }.toCollection(ArrayList())
        val nextPlayer: Player = if (playerIsColliding) {
            physics.applyPlayerCollisionPhysics(player, viewPort)
        } else {
            player.from(false)
        }
        return Pair(
            nextPlayer,
            gameMap.from(managedMapEnemies, managedMapParticles)
        )
    }

    override fun checkForProjectileCollision(
        player: Player,
        gameMap: GameMap,
        viewPort: ViewPort,
        audioQueue: AudioQueue,
        particles: Particles
    ): Pair<Player, GameMap> {
        var playerIsColliding = false
        val managedMapParticles = gameMap.particles.map { particle ->
            if (particle.type == ParticleType.FURBALL) {
                val particleRect = Rectangle(particle.x, particle.y, particle.width, particle.height)
                val playerRect = Rectangle(player.x, player.y, player.width, player.height)
                var nextFrame = particle.frame
                if (playerRect.intersects(particleRect)) {
                    LOGGER.info("particle collision !")
                    audioQueue.queue.add(Sounds.PLAYER_COLLISION)
                    playerIsColliding = true
                    nextFrame = particle.lifetime
                }
                particle.from(nextFrame)
            } else {
                particle
            }
        }.toCollection(ArrayList())
        if (playerIsColliding) {
            val collisionParticles = particles.createCollisionParticles(player.x, player.y)
            managedMapParticles.addAll(collisionParticles)
        }
        val nextPlayer: Player = if (playerIsColliding) {
            physics.applyPlayerCollisionPhysics(player, viewPort)
        } else {
            player.from(false)
        }
        return Pair(
            nextPlayer,
            gameMap.from(gameMap.enemies, managedMapParticles)
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

    private fun testForColorCollision(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        collisionImage: BufferedImage
    ): Boolean {
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