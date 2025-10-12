package com.github.adamyork.socketgame.game.engine

import com.github.adamyork.socketgame.common.AudioQueue
import com.github.adamyork.socketgame.common.Sounds
import com.github.adamyork.socketgame.game.Game
import com.github.adamyork.socketgame.game.data.*
import com.github.adamyork.socketgame.game.engine.data.PhysicsXResult
import com.github.adamyork.socketgame.game.engine.data.PhysicsYResult
import com.github.adamyork.socketgame.game.engine.data.PlayerMapPair
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.awt.Rectangle
import java.awt.image.BufferedImage
import kotlin.math.abs

@Component
class Collision {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(Collision::class.java)
        const val COLLISION_COLOR_VALUE: Int = -16711906
    }

    fun checkForPlayerCollision(previousX: Int, previousY: Int, player: Player, collisionImage: BufferedImage): Player {
        val adjustedPlayerYResult = adjustPlayerVertically(previousX, previousY, player, collisionImage)
        val adjustedPlayerXResult = adjustPlayerHorizontally(adjustedPlayerYResult.y, player, collisionImage)
        return Player(
            adjustedPlayerXResult.x,
            adjustedPlayerYResult.y,
            player.width,
            player.height,
            adjustedPlayerXResult.vx,
            adjustedPlayerYResult.vy,
            adjustedPlayerYResult.jumping,
            adjustedPlayerYResult.jumpDy,
            adjustedPlayerYResult.jumpReached,
            adjustedPlayerXResult.moving,
            player.direction,
            player.frameMetadata,
            false,
            adjustedPlayerYResult.scanVerticalCeiling,
            false
        )
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
            var itemState = item.state
            var frameMetadata = item.frameMetadata
            if (playerRect.intersects(itemRect) && itemState == MapItemState.ACTIVE) {
                if (item.type == MapItemType.FINISH) {
                    LOGGER.info("finish reached")
                    gameState = GameMapState.COMPLETED
                    itemState = MapItemState.INACTIVE
                } else {
                    LOGGER.info("item collision")
                    itemState = MapItemState.DEACTIVATING
                    audioQueue.queue.add(Sounds.ITEM_COLLECT)
                    frameMetadata = item.getFirstDeactivatingFrame()
                }
            }
            MapItem(item.width, item.height, item.x, item.y, item.type, itemState, frameMetadata)
        }.toCollection(ArrayList())
        return GameMap(
            gameState,
            gameMap.farGroundAsset,
            gameMap.midGroundAsset,
            gameMap.nearFieldAsset,
            gameMap.collisionAsset,
            gameMap.x,
            gameMap.y,
            gameMap.width,
            gameMap.height,
            managedMapItems,
            gameMap.enemies,
            gameMap.particles
        )
    }

    fun checkForEnemyCollision(
        player: Player,
        gameMap: GameMap,
        audioQueue: AudioQueue,
        particles: Particles,
        physics: Physics
    ): PlayerMapPair {
        var managedMapParticles = gameMap.particles
        var playerIsColliding = false
        val managedMapEnemies = gameMap.enemies.map { enemy ->
            val enemyRect = Rectangle(enemy.x, enemy.y, enemy.width, enemy.height)
            val playerRect = Rectangle(player.x, player.y, player.width, player.height)
            var isColliding = false
            if (playerRect.intersects(enemyRect)) {
                LOGGER.info("enemy collision !")
                audioQueue.queue.add(Sounds.PLAYER_COLLISION)
                if (gameMap.particles.isEmpty()) {
                    managedMapParticles = particles.createCollisionParticles(enemy.x, enemy.y)
                }
                isColliding = true
                playerIsColliding = true
            }
            managedMapParticles = physics.applyCollisionParticlePhysics(managedMapParticles)
            managedMapParticles = managedMapParticles.filter { particle -> particle.frame <= particle.lifetime }
                .toCollection(ArrayList())
            val frameMetadata = enemy.getNextFrameCell()
            MapEnemy(
                enemy.width,
                enemy.height,
                enemy.x,
                enemy.y,
                enemy.originX,
                enemy.originY,
                enemy.state,
                frameMetadata,
                enemy.enemyPosition,
                isColliding
            )

        }.toCollection(ArrayList())
        var nextX = player.x
        var nextVx = player.vx
        if (playerIsColliding) {
            val collisionRebound: Int = player.width
            if (player.direction == Direction.LEFT) {
                nextX += collisionRebound
                if (nextX >= Game.VIEWPORT_WIDTH - player.width) {
                    nextX = Game.VIEWPORT_WIDTH - player.width - 1
                }
            } else {
                nextX -= collisionRebound
                if (nextX < 0) {
                    nextX = 0
                }
            }
            nextVx = 0.0
        }
        return PlayerMapPair(
            Player(
                nextX,
                player.y,
                player.width,
                player.height,
                nextVx,
                player.vy,
                player.jumping,
                player.jumpDy,
                player.jumpReached,
                player.moving,
                player.direction,
                player.frameMetadata,
                playerIsColliding,
                player.scanVerticalCeiling,
                player.scanVerticalFloor,
            ), GameMap(
                gameMap.state,
                gameMap.farGroundAsset,
                gameMap.midGroundAsset,
                gameMap.nearFieldAsset,
                gameMap.collisionAsset,
                gameMap.x,
                gameMap.y,
                gameMap.width,
                gameMap.height,
                gameMap.items,
                managedMapEnemies,
                managedMapParticles
            )
        )
    }

    private fun adjustPlayerVertically(
        previousX: Int,
        previousY: Int,
        player: Player,
        collisionImage: BufferedImage
    ): PhysicsYResult {
        val boundedX = player.x.coerceAtMost(collisionImage.width - player.width)
        val scanHeight = abs(player.jumpDy - previousY)
        if (player.jumpDy < 0) {
            return PhysicsYResult(
                player.y, player.vy, player.jumping, player.jumpDy, player.jumpReached,
                scanVerticalCeiling = false,
                scanVerticalFloor = false
            )
        }
        if (player.scanVerticalCeiling) {
            LOGGER.info("potentially moved through a ceiling")
            val ceiling = findCeiling(boundedX, previousY + player.height, player.width, collisionImage)
            return PhysicsYResult(
                ceiling, 0.0, false, 0,
                jumpReached = false,
                scanVerticalCeiling = false,
                scanVerticalFloor = false
            )
        }
        if (player.jumping && !player.jumpReached && player.y > player.jumpDy) {
            LOGGER.info("player is jumping and rising")
            val playerWillCollide = testForColorCollision(
                boundedX,
                player.jumpDy,
                player.width,
                scanHeight,
                collisionImage
            )
            if (playerWillCollide) {
                LOGGER.info("player is jumping a needs to check for ceiling")
                val ceiling = findCeiling(boundedX, previousY, player.width, collisionImage)
                return PhysicsYResult(
                    ceiling, 0.0, false, 0,
                    jumpReached = false,
                    scanVerticalCeiling = false,
                    scanVerticalFloor = false
                )
            } else {
                LOGGER.info("player is jumping and will not collide")
                return PhysicsYResult(
                    player.y,
                    player.vy,
                    player.jumping,
                    player.jumpDy,
                    player.jumpReached,
                    scanVerticalCeiling = false,
                    scanVerticalFloor = false
                )
            }
        }
        if (player.scanVerticalFloor) {
            LOGGER.info("potentially moved through a floor")
            val floor = findFloor(boundedX, previousY - player.height, player.height, player.width, collisionImage)
            return PhysicsYResult(
                floor, 0.0, false, 0,
                jumpReached = false,
                scanVerticalCeiling = false,
                scanVerticalFloor = false
            )
        }
        val floor = findFloor(player.x, previousY + player.height, player.height, player.width, collisionImage)
        if (player.y < floor) {
            LOGGER.info("Player is falling")
            val playerYIsClipping = testForColorCollision(
                player.x,
                player.y,
                1,
                1,
                collisionImage
            )
            if (playerYIsClipping) {
                LOGGER.info("Player is clipping above")
                val newCeiling = findCeilingDescending(player.x, player.y, player.width, collisionImage)
                return PhysicsYResult(
                    newCeiling, 0.0, false, 0,
                    jumpReached = false,
                    scanVerticalCeiling = false,
                    scanVerticalFloor = false
                )
            }
            return PhysicsYResult(
                player.y, player.vy, player.jumping, player.jumpDy, player.jumpReached,
                scanVerticalCeiling = false,
                scanVerticalFloor = false
            )
        }
        val playerYIsClipping = testForColorCollision(
            previousX,
            previousY + player.height - 1,
            1,
            1,
            collisionImage
        )
        if (playerYIsClipping) {
            LOGGER.info("Player is clipping below")
            val newFloor = findFloorAscending(boundedX, previousY, player.width, player.height, collisionImage)
            return PhysicsYResult(
                newFloor, 0.0, false, 0,
                jumpReached = false,
                scanVerticalCeiling = false,
                scanVerticalFloor = false
            )
        }
        return PhysicsYResult(
            floor, 0.0, false, 0,
            jumpReached = false,
            scanVerticalCeiling = false,
            scanVerticalFloor = false
        )
    }


    private fun findFloor(
        x: Int,
        startY: Int,
        playerHeight: Int,
        playerWidth: Int,
        collisionImage: BufferedImage
    ): Int {
        if (startY >= collisionImage.height) {
            return collisionImage.height - playerHeight
        }
        return if (testForColorCollision(x, startY, playerWidth, 1, collisionImage)) {
            //LOGGER.debug("Floor found descending${startY - playerHeight}")
            startY - playerHeight
        } else {
            findFloor(x, startY + 1, playerHeight, playerWidth, collisionImage)
        }
    }

    private fun findCeiling(x: Int, startY: Int, playerWidth: Int, collisionImage: BufferedImage): Int {
        if (startY <= 0) {
            LOGGER.info("Ceiling found ascending 0")
            return 0
        }
        return if (testForColorCollision(x, startY, playerWidth, 1, collisionImage)) {
            LOGGER.info("Ceiling found ascending ${startY + 1}")
            startY + 1
        } else {
            findCeiling(x, startY - 1, playerWidth, collisionImage)
        }
    }

    private fun findCeilingDescending(x: Int, startY: Int, playerWidth: Int, collisionImage: BufferedImage): Int {
        return if (!testForColorCollision(x, startY, playerWidth, 1, collisionImage)) {
            LOGGER.info("Ceiling found descending ${startY - 1}")
            startY - 1
        } else {
            findCeilingDescending(x, startY + 1, playerWidth, collisionImage)
        }
    }

    private fun findFloorAscending(
        x: Int,
        startY: Int,
        playerWidth: Int,
        playerHeight: Int,
        collisionImage: BufferedImage
    ): Int {
        return if (!testForColorCollision(x, startY, playerWidth, 1, collisionImage)) {
            LOGGER.info("Floor found ascending ${startY - playerWidth}")
            startY - playerHeight
        } else {
            findFloorAscending(x, startY - 1, playerWidth, playerHeight, collisionImage)
        }
    }

    private fun adjustPlayerHorizontally(
        playerY: Int,
        player: Player,
        collisionImage: BufferedImage
    ): PhysicsXResult {
        var startX: Int
        startX = if (player.direction == Direction.RIGHT) {
            player.x + player.width
        } else {
            player.x + 1
        }
        if (startX >= collisionImage.width) {
            startX = collisionImage.width - 1
        } else if (startX <= 0) {
            startX = 1
        }
        val playerWillCollide = testForColorCollision(
            startX,
            playerY,
            1,
            player.height,
            collisionImage
        )
        if (playerWillCollide) {
            LOGGER.info("Player will horizontally collide ")
            val edge =
                findEdge(startX, playerY, player.width, player.height, player.vx, player.direction, collisionImage)
            return PhysicsXResult(edge, 0.0, player.moving)
        }
        return PhysicsXResult(player.x, player.vx, player.moving)
    }

    private fun findEdge(
        startX: Int,
        y: Int,
        playerWidth: Int,
        playerHeight: Int,
        playerVx: Double,
        playerDirection: Direction,
        collisionImage: BufferedImage
    ): Int {
        return if (testForColorCollision(startX, y, 1, playerHeight, collisionImage)) {
            if (playerDirection == Direction.RIGHT) {
                LOGGER.info("endFound player.x : startX - playerWidth - 1 ${startX - playerWidth - 1}")
                startX - playerWidth - 1 - playerVx.toInt()
            } else {
                LOGGER.info("endFound player.x : startX + 1 ${startX + 1}")
                startX + 1 + playerVx.toInt()
            }
        } else {
            if (playerDirection == Direction.RIGHT) {
                findEdge(startX + 1, y, playerWidth, playerHeight, playerVx, playerDirection, collisionImage)
            } else {
                findEdge(startX - 1, y, playerWidth, playerHeight, playerVx, playerDirection, collisionImage)
            }
        }
    }

    private fun testForColorCollision(x: Int, y: Int, width: Int, height: Int, collisionImage: BufferedImage): Boolean {
        try {
            val collisionBitMap =
                collisionImage.getRGB(x, y, width, height, null, 0, width)
            return collisionBitMap.contains(COLLISION_COLOR_VALUE)
        } catch (ex: Exception) {
            LOGGER.info("ArrayIndexOutOfBoundsException x $x y:$y width:$width height:$height $ex")
            return true
        }
    }
}