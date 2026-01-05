package com.github.adamyork.sparrow.game.engine.v1

import com.github.adamyork.sparrow.common.AudioQueue
import com.github.adamyork.sparrow.common.GameStatusProvider
import com.github.adamyork.sparrow.game.data.*
import com.github.adamyork.sparrow.game.data.enemy.*
import com.github.adamyork.sparrow.game.data.item.GameItem
import com.github.adamyork.sparrow.game.data.item.MapCollectibleItem
import com.github.adamyork.sparrow.game.data.item.MapFinishItem
import com.github.adamyork.sparrow.game.data.item.MapItemType
import com.github.adamyork.sparrow.game.data.map.GameMap
import com.github.adamyork.sparrow.game.data.map.GameMapState
import com.github.adamyork.sparrow.game.data.player.Player
import com.github.adamyork.sparrow.game.data.player.PlayerMovingState
import com.github.adamyork.sparrow.game.engine.Collision
import com.github.adamyork.sparrow.game.engine.Engine
import com.github.adamyork.sparrow.game.engine.Particles
import com.github.adamyork.sparrow.game.engine.Physics
import com.github.adamyork.sparrow.game.engine.data.CollisionBoundaries
import com.github.adamyork.sparrow.game.engine.data.ParticleShape
import com.github.adamyork.sparrow.game.engine.data.ParticleType
import com.github.adamyork.sparrow.game.service.AssetService
import com.github.adamyork.sparrow.game.service.ScoreService
import com.github.adamyork.sparrow.game.service.data.ImageAsset
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.AlphaComposite
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.io.BufferedOutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.concurrent.atomics.ExperimentalAtomicApi


class DefaultEngine : Engine {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(DefaultEngine::class.java)
    }

    val physics: Physics
    val collision: Collision
    val particles: Particles
    val audioQueue: AudioQueue
    val scoreService: ScoreService
    val assetService: AssetService
    val gameStatusProvider: GameStatusProvider

    constructor(
        physics: Physics,
        collision: Collision,
        particles: Particles,
        audioQueue: AudioQueue,
        scoreService: ScoreService,
        assetService: AssetService,
        gameStatusProvider: GameStatusProvider
    ) {
        this.physics = physics
        this.particles = particles
        this.audioQueue = audioQueue
        this.collision = collision
        this.scoreService = scoreService
        this.assetService = assetService
        this.gameStatusProvider = gameStatusProvider
    }

    override fun setCollisionBufferedImage(asset: ImageAsset) {
        this.collision.collisionImage = asset.bufferedImage
    }

    override fun getCollisionBoundaries(player: Player): CollisionBoundaries {
        return collision.getCollisionBoundaries(player)
    }

    override fun managePlayer(player: Player, collisionBoundaries: CollisionBoundaries): Player {
        val physicsAppliedPlayer = physics.applyPlayerPhysics(player, collisionBoundaries, collision)
        val nextFrameMetadataWithState = physicsAppliedPlayer.getNextFrameMetadataWithState()
        val metadata = nextFrameMetadataWithState.first
        val metadataState = nextFrameMetadataWithState.second
        return physicsAppliedPlayer.copy(frameMetadata = metadata, colliding = metadataState.colliding)
    }

    override fun manageViewport(player: Player, viewPort: ViewPort): ViewPort {
        var nextX = viewPort.x
        var nextY = viewPort.y
        if (player.direction == Direction.RIGHT) {
            val adjustedX = player.x + player.width
            val viewPortRightBoundary = viewPort.x + viewPort.width
            if (adjustedX > viewPortRightBoundary) {
                LOGGER.info("move map horizontal right")
                val diff = adjustedX - viewPortRightBoundary
                nextX = (nextX + diff).coerceAtMost(collision.collisionImage.width - viewPort.width)
            }
        } else {
            val viewPortLeftBoundary = viewPort.x
            if (player.x < viewPortLeftBoundary) {
                LOGGER.info("move map horizontal left")
                val diff = viewPortLeftBoundary - player.x
                nextX = (nextX - diff).coerceAtLeast(0)
            }
        }
        val playerBottom = player.y + player.height
        val viewPortBottomBoundary = viewPort.y + viewPort.height
        if (player.y < viewPort.y) {
            LOGGER.info("move map vertical up")
            val diff = viewPort.y - player.y
            nextY = (nextY - diff).coerceAtLeast(0)
        } else if (playerBottom > viewPortBottomBoundary) {
            LOGGER.info("move map vertical down")
            val diff = player.y - viewPort.y
            nextY = (nextY + diff).coerceAtMost(collision.collisionImage.height - viewPort.height)
        }
        val nextViewPort = ViewPort(nextX, nextY, viewPort.x, viewPort.y, viewPort.width, viewPort.height)
        if (nextX != viewPort.x || nextY != viewPort.y) {
            LOGGER.info("viewport has changed $nextViewPort")
        }
        return nextViewPort
    }

    override fun manageMap(player: Player, gameMap: GameMap): GameMap {
        val managedMapItems = manageMapItems(gameMap)
        val managedMapEnemies = manageMapEnemies(gameMap, player)
        val managedCollisionParticles = physics.applyCollisionParticlePhysics(gameMap.particles)
        val managedMapItemReturnParticles = physics.applyMapItemReturnParticlePhysics(managedCollisionParticles)
        if (player.moving == PlayerMovingState.MOVING && !player.jumping) {
            val nextDustParticles = particles.createDustParticles(player)
            managedMapItemReturnParticles.addAll(nextDustParticles)
        }
        val managedDustParticles = physics.applyDustParticlePhysics(managedMapItemReturnParticles)
        val managedAllParticles = physics.applyProjectileParticlePhysics(managedDustParticles)
        var mapState = gameMap.state
        if (mapState == GameMapState.COLLECTING && scoreService.allFound()) {
            LOGGER.info("all items found map is in completing mode")
            mapState = GameMapState.COMPLETING
        }
        return gameMap.copy(
            state = mapState,
            items = managedMapItems,
            enemies = managedMapEnemies,
            particles = managedAllParticles
        )
    }

    override fun manageEnemyAndItemCollision(
        player: Player,
        map: GameMap,
        viewPort: ViewPort
    ): Pair<Player, GameMap> {
        val nextMap = collision.checkForItemCollision(player, map, audioQueue)
        val enemyCollisionResult =
            collision.checkForEnemyCollisionAndProximity(player, nextMap, viewPort, audioQueue, particles)
        val projectTileCollisionResult =
            collision.checkForProjectileCollision(
                enemyCollisionResult.first,
                enemyCollisionResult.second,
                viewPort,
                audioQueue,
                particles
            )
        var nextGameMap = projectTileCollisionResult.second
        if (projectTileCollisionResult.first.colliding == GameElementCollisionState.COLLIDING) {
            val nextItems = returnMapItemAfterCollision(nextGameMap)
            nextGameMap = nextGameMap.copy(items = nextItems)
        }
        return Pair(projectTileCollisionResult.first, nextGameMap)
    }

    private fun manageMapItems(gameMap: GameMap): ArrayList<GameItem> {
        return gameMap.items.map { item ->
            val itemX = item.x
            val itemY = item.y
            val frameMetadataWithState = (item as GameElement).getNextFrameMetadataWithState()
            val metadata = frameMetadataWithState.first
            var nextState = frameMetadataWithState.second.state
            if (item.type == MapItemType.FINISH) {
                if (gameMap.state == GameMapState.COMPLETING && item.state == GameElementState.INACTIVE) {
                    nextState = GameElementState.ACTIVE
                }
            }
            if (item.type == MapItemType.FINISH) {
                (item as MapFinishItem).copy(x = itemX, y = itemY, state = nextState, frameMetadata = metadata)
            } else {
                (item as MapCollectibleItem).copy(
                    x = itemX,
                    y = itemY,
                    state = nextState,
                    frameMetadata = metadata
                )
            }
        }.toCollection(ArrayList())
    }

    private fun returnMapItemAfterCollision(gameMap: GameMap): ArrayList<GameItem> {
        val firstInactive: GameItem? =
            gameMap.items.firstOrNull { item -> item.type == MapItemType.COLLECTABLE && item.state == GameElementState.INACTIVE }
        if (firstInactive != null) {
            val remainingItems: ArrayList<GameItem> =
                gameMap.items.filter { item -> item.type == MapItemType.COLLECTABLE && item.id != firstInactive.id }
                    .toCollection(ArrayList())
            val reactivatedItem = (firstInactive as MapCollectibleItem).copy(
                state = GameElementState.ACTIVE
            )
            remainingItems.add(reactivatedItem)
            return remainingItems
        } else {
            return gameMap.items
        }
    }

    private fun manageMapEnemies(gameMap: GameMap, player: Player): ArrayList<GameEnemy> {
        return gameMap.enemies.map { enemy ->
            val nextState = enemy.getNextEnemyState(player)
            if (nextState != GameElementState.INACTIVE) {
                val nextPosition = enemy.getNextPosition()
                val itemX = nextPosition.x
                val itemY = nextPosition.y
                val frameMetadataWithState = (enemy as GameElement).getNextFrameMetadataWithState()
                val metadata = frameMetadataWithState.first
                val metadataState = frameMetadataWithState.second
                when (enemy.type) {
                    MapEnemyType.SHOOTER -> {
                        (enemy as MapShooterEnemy).copy(
                            x = itemX,
                            y = itemY,
                            state = nextState,
                            frameMetadata = metadata,
                            enemyPosition = nextPosition,
                            colliding = metadataState.colliding,
                            interacting = metadataState.interacting
                        )
                    }

                    MapEnemyType.RUNNER -> {
                        (enemy as MapRunnerEnemy).copy(
                            x = itemX,
                            y = itemY,
                            state = nextState,
                            frameMetadata = metadata,
                            enemyPosition = nextPosition,
                            colliding = metadataState.colliding,
                            interacting = metadataState.interacting
                        )
                    }

                    else -> {
                        (enemy as MapBlockerEnemy).copy(
                            x = itemX,
                            y = itemY,
                            state = nextState,
                            frameMetadata = metadata,
                            enemyPosition = nextPosition,
                            colliding = metadataState.colliding,
                            interacting = metadataState.interacting
                        )
                    }
                }
            } else if (enemy.type == MapEnemyType.RUNNER) {
                (enemy as MapRunnerEnemy).copy(state = nextState)
            } else {
                enemy
            }
        }.toCollection(ArrayList())
    }

    @OptIn(ExperimentalAtomicApi::class)
    override fun draw(
        map: GameMap,
        viewPort: ViewPort,
        player: Player
    ): ByteArray {
        val compositeImage = BufferedImage(viewPort.width, viewPort.height, BufferedImage.TYPE_BYTE_INDEXED)
        val graphics: Graphics2D = compositeImage.graphics as Graphics2D
        graphics.composite = AlphaComposite.SrcOver
        graphics.setRenderingHint(
            RenderingHints.KEY_ALPHA_INTERPOLATION,
            RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED
        )
        graphics.setRenderingHint(
            RenderingHints.KEY_RENDERING,
            RenderingHints.VALUE_RENDER_SPEED
        )
        graphics.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
        )

        if (gameStatusProvider.lastBackgroundComposite.load().width == 1) {
            val compositeBackgroundImage = compositeBackground(map, viewPort)
            gameStatusProvider.lastBackgroundComposite.store(compositeBackgroundImage)
        }

        if (viewPort.x != viewPort.lastX || viewPort.y != viewPort.lastY) {
            LOGGER.info("view port has moved need to redraw background")
            val compositeBackgroundImage = compositeBackground(map, viewPort)
            gameStatusProvider.lastBackgroundComposite.store(compositeBackgroundImage)
            graphics.drawImage(compositeBackgroundImage, 0, 0, null)
        } else {
            graphics.drawImage(gameStatusProvider.lastBackgroundComposite.load(), 0, 0, null)
        }

        drawStatusText(map, graphics)
        drawMapElements(
            map.items.map { item -> item as GameElement }.toCollection(ArrayList()),
            viewPort,
            graphics,
            false
        )
        drawMapElements(
            map.enemies.map { item -> item as GameElement }.toCollection(ArrayList()),
            viewPort,
            graphics,
            true
        )
        drawParticles(map, viewPort, graphics)
        drawPlayer(player, viewPort, graphics)

        return createByteArrayFromCompositeImage(compositeImage)
    }

    private fun compositeBackground(map: GameMap, viewPort: ViewPort): BufferedImage {
        val bgCompositeImage = BufferedImage(viewPort.width, viewPort.height, BufferedImage.TYPE_BYTE_INDEXED)
        val farGroundSubImage = getSubImage(
            map.farGroundAsset.bufferedImage,
            map.getFarGroundX(viewPort),
            viewPort.y,
            viewPort.width,
            viewPort.height
        )
        val midGroundSubImage = getSubImage(
            map.midGroundAsset.bufferedImage,
            map.getMidGroundX(viewPort),
            viewPort.y,
            viewPort.width,
            viewPort.height
        )
        val nearFieldSubImage =
            getSubImage(map.nearFieldAsset.bufferedImage, viewPort.x, viewPort.y, viewPort.width, viewPort.height)
        val collisionSubImage =
            getSubImage(map.collisionAsset.bufferedImage, viewPort.x, viewPort.y, viewPort.width, viewPort.height)
        bgCompositeImage.graphics.drawImage(farGroundSubImage, 0, 0, null)
        bgCompositeImage.graphics.drawImage(midGroundSubImage, 0, 0, null)
        bgCompositeImage.graphics.drawImage(nearFieldSubImage, 0, 0, null)
        bgCompositeImage.graphics.drawImage(collisionSubImage, 0, 0, null)
        val bytes = createByteArrayFromCompositeImage(bgCompositeImage)
        val bais = ByteArrayInputStream(bytes)
        return ImageIO.read(bais)
    }

    private fun createByteArrayFromCompositeImage(compositeImage: BufferedImage): ByteArray {
        val backgroundBuffer = ByteArrayOutputStream()
        val bufferedOutputStream = BufferedOutputStream(backgroundBuffer)
        ImageIO.setUseCache(false)
        ImageIO.write(compositeImage, "bmp", bufferedOutputStream)
        compositeImage.graphics.dispose()
        val bytes = backgroundBuffer.toByteArray()
        backgroundBuffer.reset()
        bufferedOutputStream.close()
        return bytes
    }

    private fun drawPlayer(player: Player, viewPort: ViewPort, graphics: Graphics) {
        val playerSubImage = getSubImage(
            player.bufferedImage,
            player.frameMetadata.cell.x,
            player.frameMetadata.cell.y,
            player.width,
            player.height
        )
        val localCord = viewPort.globalToLocal(player.x, player.y)
        graphics.drawImage(
            transformDirection(playerSubImage, player.direction, player.width),
            localCord.first,
            localCord.second,
            null
        )
    }

    private fun drawParticles(map: GameMap, viewPort: ViewPort, graphics: Graphics) {
        val particleImage = BufferedImage(viewPort.width, viewPort.height, BufferedImage.TYPE_4BYTE_ABGR)
        val particleGraphics = particleImage.graphics
        map.particles.forEach { particle ->
            val localCord = viewPort.globalToLocal(particle.x, particle.y)
            particleGraphics.color = particle.color
            if (particle.type == ParticleType.MAP_ITEM_RETURN) {
                val mapItemReference = map.items.first { _ -> true }
                val mapItemReferenceSubImage = getSubImage(
                    mapItemReference.bufferedImage,
                    0,
                    0,
                    mapItemReference.width,
                    mapItemReference.height
                )
                particleGraphics.drawImage(
                    mapItemReferenceSubImage,
                    localCord.first,
                    localCord.second,
                    particle.width,
                    particle.height,
                    null
                )
            } else {
                if (particle.shape == ParticleShape.CIRCLE) {
                    particleGraphics.fillOval(
                        localCord.first,
                        localCord.second,
                        particle.width,
                        particle.height
                    )
                } else {
                    particleGraphics.fillRect(
                        localCord.first,
                        localCord.second,
                        particle.width,
                        particle.height
                    )
                }
            }

        }
        graphics.drawImage(particleImage, 0, 0, null)
        particleImage.graphics.dispose()
    }

    private fun drawStatusText(map: GameMap, graphics: Graphics) {
        val gameStatusTextImage = assetService.getTextAsset(map.state)
        graphics.drawImage(gameStatusTextImage.image, 0, 0, null)
    }

    private fun drawMapElements(
        elements: ArrayList<GameElement>,
        viewPort: ViewPort,
        graphics: Graphics,
        transformDirection: Boolean
    ) {
        elements.forEach { element ->
            val localCord = viewPort.globalToLocal(element.x, element.y)
            if (element.state != GameElementState.INACTIVE) {
                var itemSubImage = getSubImage(
                    element.bufferedImage,
                    element.frameMetadata.cell.x,
                    element.frameMetadata.cell.y,
                    element.width,
                    element.height
                )
                if (transformDirection) {
                    itemSubImage = transformDirection(itemSubImage, element.nestedDirection(), element.width)
                }
                graphics.drawImage(
                    itemSubImage,
                    localCord.first,
                    localCord.second,
                    null
                )
            }
        }
    }

    private fun getSubImage(bufferedImage: BufferedImage, x: Int, y: Int, width: Int, height: Int): BufferedImage {
        return bufferedImage.getSubimage(x, y, width, height)
    }

    private fun transformDirection(playerImage: BufferedImage, direction: Direction, width: Int): BufferedImage {
        if (direction == Direction.LEFT) {
            val tx = AffineTransform.getScaleInstance(-1.0, 1.0)
            tx.translate(-width.toDouble(), 0.0)
            val op = AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR)
            return op.filter(playerImage, null)
        }
        return playerImage
    }
}