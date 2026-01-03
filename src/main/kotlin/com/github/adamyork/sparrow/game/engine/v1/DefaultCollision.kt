package com.github.adamyork.sparrow.game.engine.v1

import com.github.adamyork.sparrow.common.AudioQueue
import com.github.adamyork.sparrow.common.Sounds
import com.github.adamyork.sparrow.game.data.*
import com.github.adamyork.sparrow.game.data.enemy.*
import com.github.adamyork.sparrow.game.data.item.MapCollectibleItem
import com.github.adamyork.sparrow.game.data.item.MapFinishItem
import com.github.adamyork.sparrow.game.data.item.MapItemType
import com.github.adamyork.sparrow.game.data.map.GameMap
import com.github.adamyork.sparrow.game.data.map.GameMapState
import com.github.adamyork.sparrow.game.data.player.Player
import com.github.adamyork.sparrow.game.engine.Collision
import com.github.adamyork.sparrow.game.engine.Particles
import com.github.adamyork.sparrow.game.engine.Physics
import com.github.adamyork.sparrow.game.engine.data.CollisionBoundaries
import com.github.adamyork.sparrow.game.engine.data.ParticleType
import com.github.adamyork.sparrow.game.service.ScoreService
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
    val scoreService: ScoreService

    constructor(
        physics: Physics,
        scoreService: ScoreService
    ) {
        this.physics = physics
        this.scoreService = scoreService
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
            if (playerRect.intersects(itemRect) && nextItemState == GameElementState.ACTIVE) {
                if (item.type == MapItemType.FINISH) {
                    LOGGER.info("finish reached")
                    gameState = GameMapState.COMPLETED
                    nextItemState = GameElementState.INACTIVE
                } else {
                    LOGGER.info("item collision")
                    nextItemState = GameElementState.DEACTIVATING
                    audioQueue.queue.add(Sounds.ITEM_COLLECT)
                    nextFrameMetaData = item.getFirstDeactivatingFrame()
                }
            }
            if (item.type == MapItemType.FINISH) {
                (item as MapFinishItem).copy(state = nextItemState, frameMetadata = nextFrameMetaData)
            } else {
                (item as MapCollectibleItem).copy(state = nextItemState, frameMetadata = nextFrameMetaData)
            }
        }.toCollection(ArrayList())
        return gameMap.copy(state = gameState, items = managedMapItems)
    }

    override fun checkForEnemyCollisionAndProximity(
        player: Player,
        gameMap: GameMap,
        viewPort: ViewPort,
        audioQueue: AudioQueue,
        particles: Particles
    ): Pair<Player, GameMap> {
        val managedMapParticles = gameMap.particles
        var playerIsColliding = false
        var targetRect: Rectangle? = null
        val managedMapEnemies = gameMap.enemies.map { enemy ->
            var isColliding = false
            var isInteracting = false
            if ((enemy as GameElement).state != GameElementState.INACTIVE) {
                val enemyRect = Rectangle(enemy.x, enemy.y, enemy.width, enemy.height)
                val playerRect = Rectangle(player.x, player.y, player.width, player.height)
                if (playerRect.intersects(enemyRect)) {
                    LOGGER.info("enemy collision !")
                    targetRect = enemyRect
                    audioQueue.queue.add(Sounds.PLAYER_COLLISION)
                    val collisionParticles = particles.createCollisionParticles(enemy.x, enemy.y)
                    managedMapParticles.addAll(collisionParticles)
                    if (scoreService.getTotal() != scoreService.getRemaining()) {
                        val mapItemReturnParticle = particles.createMapItemReturnParticle(player)
                        managedMapParticles.add(mapItemReturnParticle)
                    }
                    isColliding = true
                    playerIsColliding = true
                }
                if (enemy.type == MapEnemyType.SHOOTER) {
                    val dist =
                        Point2D.distance(
                            player.x.toDouble(),
                            player.y.toDouble(),
                            enemy.x.toDouble(),
                            enemy.y.toDouble()
                        )
                            .toInt()
                    if (dist <= MapShooterEnemy.PLAYER_PROXIMITY_THRESHOLD) {
                        val managedProjectileParticlesResult =
                            particles.createProjectileParticle(player, enemy, gameMap.particles)
                        if (managedProjectileParticlesResult.second) {
                            LOGGER.info("enemy shoots")
                            isInteracting = true
                            audioQueue.queue.add(Sounds.ENEMY_SHOOT)
                        }
                        managedMapParticles.addAll(managedProjectileParticlesResult.first)
                    }
                }
                val frameMetadataWithState = (enemy as GameElement).getNextFrameMetadataWithState()
                val metadata = frameMetadataWithState.first
                val metadataState = frameMetadataWithState.second
                if (isColliding) {
                    if (enemy.type == MapEnemyType.SHOOTER) {
                        (enemy as MapShooterEnemy).copy(
                            frameMetadata = metadata,
                            colliding = metadataState.colliding,
                            interacting = enemy.interacting
                        ) as GameEnemy
                    } else if (enemy.type == MapEnemyType.RUNNER) {
                        (enemy as MapRunnerEnemy).copy(
                            frameMetadata = metadata,
                            colliding = GameElementCollisionState.COLLIDING,
                            interacting = enemy.interacting
                        ) as GameEnemy
                    } else {
                        (enemy as MapBlockerEnemy).copy(
                            frameMetadata = metadata,
                            colliding = GameElementCollisionState.COLLIDING,
                            interacting = enemy.interacting
                        ) as GameEnemy
                    }
                } else if (isInteracting) {
                    if (enemy.type == MapEnemyType.SHOOTER) {
                        (enemy as MapShooterEnemy).copy(
                            frameMetadata = metadata,
                            colliding = enemy.colliding,
                            interacting = GameEnemyInteractionState.INTERACTING
                        ) as GameEnemy
                    } else if (enemy.type == MapEnemyType.RUNNER) {
                        (enemy as MapRunnerEnemy).copy(
                            frameMetadata = metadata,
                            colliding = enemy.colliding,
                            interacting = GameEnemyInteractionState.INTERACTING
                        ) as GameEnemy
                    } else {
                        (enemy as MapBlockerEnemy).copy(
                            frameMetadata = metadata,
                            colliding = enemy.colliding,
                            interacting = GameEnemyInteractionState.INTERACTING
                        ) as GameEnemy
                    }
                } else {
                    enemy
                }
            } else {
                enemy
            }
        }.toCollection(ArrayList())
        val nextPlayer: Player = if (playerIsColliding) {
            LOGGER.info("player is colliding apply physics")
            physics.applyPlayerCollisionPhysics(player, targetRect, viewPort)
        } else {
            player
        }
        return Pair(
            nextPlayer,
            gameMap.copy(enemies = managedMapEnemies, particles = managedMapParticles)
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
        var targetRect: Rectangle? = null
        val managedMapParticles = gameMap.particles.map { particle ->
            if (particle.type == ParticleType.PROJECTILE) {
                val particleRect = Rectangle(particle.x, particle.y, particle.width, particle.height)
                val playerRect = Rectangle(player.x, player.y, player.width, player.height)
                var nextFrame = particle.frame
                if (playerRect.intersects(particleRect)) {
                    LOGGER.info("particle collision !")
                    targetRect = particleRect
                    audioQueue.queue.add(Sounds.PLAYER_COLLISION)
                    playerIsColliding = true
                    nextFrame = particle.lifetime
                }
                particle.copy(frame = nextFrame)
            } else {
                particle
            }
        }.toCollection(ArrayList())
        if (playerIsColliding) {
            val collisionParticles = particles.createCollisionParticles(player.x, player.y)
            managedMapParticles.addAll(collisionParticles)
            if (scoreService.getTotal() != scoreService.getRemaining()) {
                val mapItemReturnParticle = particles.createMapItemReturnParticle(player)
                managedMapParticles.add(mapItemReturnParticle)
            }
        }
        val nextPlayer: Player = if (playerIsColliding) {
            physics.applyPlayerCollisionPhysics(player, targetRect, viewPort)
        } else {
            player
        }
        return Pair(
            nextPlayer,
            gameMap.copy(enemies = gameMap.enemies, particles = managedMapParticles)
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