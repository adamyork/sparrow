package com.github.adamyork.sparrow.game.engine

import com.github.adamyork.sparrow.common.AudioQueue
import com.github.adamyork.sparrow.game.data.*
import com.github.adamyork.sparrow.game.engine.data.CollisionBoundaries
import com.github.adamyork.sparrow.game.engine.data.ParticleType
import com.github.adamyork.sparrow.game.service.AssetService
import com.github.adamyork.sparrow.game.service.ScoreService
import com.github.adamyork.sparrow.game.service.data.ImageAsset
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Color
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO


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

    constructor(
        physics: Physics,
        collision: Collision,
        particles: Particles,
        audioQueue: AudioQueue,
        scoreService: ScoreService,
        assetService: AssetService
    ) {
        this.physics = physics
        this.particles = particles
        this.audioQueue = audioQueue
        this.collision = collision
        this.scoreService = scoreService
        this.assetService = assetService
    }

    override fun setCollisionBufferedImage(asset: ImageAsset) {
        this.collision.collisionImage = asset.bufferedImage
    }

    override fun getCollisionBoundaries(player: Player, collisionAsset: ImageAsset): CollisionBoundaries {
        return collision.getCollisionBoundaries(player)
    }

    override fun managePlayer(player: Player, collisionBoundaries: CollisionBoundaries): Player {
        val physicsAppliedPlayer = physics.applyPlayerPhysics(player, collisionBoundaries, collision)
        val nextFrameMetadata = player.getNextFrameCell()
        return player.from(physicsAppliedPlayer, nextFrameMetadata)
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
        val managedMapEnemies = manageMapEnemies(gameMap)
        val managedCollisionParticles = physics.applyCollisionParticlePhysics(gameMap.particles)
        if (player.moving && !player.jumping) {
            val nextDustParticles = particles.createDustParticles(player)
            managedCollisionParticles.addAll(nextDustParticles)
        }
        val managedAllParticles = physics.applyDustParticlePhysics(managedCollisionParticles)
        var mapState = gameMap.state
        if (mapState == GameMapState.COLLECTING && scoreService.allFound()) {
            LOGGER.info("all items found map is in completing mode")
            mapState = GameMapState.COMPLETING
        }
        return gameMap.from(mapState, managedMapItems, managedMapEnemies, managedAllParticles)
    }

    override fun manageEnemyAndItemCollision(
        player: Player,
        map: GameMap,
        viewPort: ViewPort,
        collisionAsset: ImageAsset
    ): Pair<Player, GameMap> {
        val nextMap = collision.checkForItemCollision(player, map, audioQueue)
        val enemyCollisionResult =
            collision.checkForEnemyCollision(player, nextMap, viewPort, audioQueue, particles)
        return Pair(enemyCollisionResult.first, enemyCollisionResult.second)
    }

    private fun manageMapItems(gameMap: GameMap): ArrayList<MapItem> {
        return gameMap.items.map { item ->
            val itemX = item.x
            val itemY = item.y
            val frameMetadata = item.getNextFrameCell()
            if (item.type == MapItemType.FINISH) {
                var nextState = item.state
                if (gameMap.state == GameMapState.COMPLETING && item.state == MapItemState.INACTIVE) {
                    nextState = MapItemState.ACTIVE
                }
                MapFinishItem(item.width, item.height, itemX, itemY, item.type, nextState, frameMetadata)
            } else {
                MapItem(item.width, item.height, itemX, itemY, item.type, item.state, frameMetadata)
            }
        }.toCollection(ArrayList())
    }

    private fun manageMapEnemies(gameMap: GameMap): ArrayList<MapEnemy> {
        return gameMap.enemies.map { enemy ->
            val nextPosition = enemy.getNextPosition()
            val itemX = nextPosition.x
            val itemY = nextPosition.y
            val frameMetadata = enemy.getNextFrameCell()
            enemy.from(itemX, itemY, frameMetadata, nextPosition, false)
        }.toCollection(ArrayList())
    }

    override fun paint(
        map: GameMap,
        viewPort: ViewPort,
        playerAsset: ImageAsset,
        player: Player,
        mapItemAsset: ImageAsset,
        finishItemAsset: ImageAsset,
        mapEnemyAsset: ImageAsset
    ): ByteArray {//TODO major clean up
        val compositeImage = BufferedImage(
            viewPort.width, viewPort.height,
            BufferedImage.TYPE_BYTE_INDEXED
        )
        val graphics = compositeImage.graphics
        var farGroundX = viewPort.x / GameMap.VIEWPORT_HORIZONTAL_FAR_PARALLAX_OFFSET
        var midGroundX = viewPort.x / GameMap.VIEWPORT_HORIZONTAL_MID_PARALLAX_OFFSET
        if (farGroundX < 0 || farGroundX > viewPort.width) {
            farGroundX = viewPort.x
        }
        if (midGroundX < 0 || midGroundX > viewPort.width) {
            midGroundX = viewPort.x
        }
        val farGroundSubImage =
            map.farGroundAsset.bufferedImage.getSubimage(farGroundX, viewPort.y, viewPort.width, viewPort.height)
        val midGroundSubImage =
            map.midGroundAsset.bufferedImage.getSubimage(midGroundX, viewPort.y, viewPort.width, viewPort.height)
        val nearFieldSubImage =
            map.nearFieldAsset.bufferedImage.getSubimage(viewPort.x, viewPort.y, viewPort.width, viewPort.height)
        val collisionSubImage =
            map.collisionAsset.bufferedImage.getSubimage(viewPort.x, viewPort.y, viewPort.width, viewPort.height)
        graphics.drawImage(farGroundSubImage, 0, 0, null)
        graphics.drawImage(midGroundSubImage, 0, 0, null)
        graphics.drawImage(nearFieldSubImage, 0, 0, null)
        graphics.drawImage(collisionSubImage, 0, 0, null)

        val gameStatusTextImage = assetService.getTextAsset(map.state)
        graphics.drawImage(gameStatusTextImage.image, 0, 0, null)

        map.items.forEach { item ->
            if (item.state != MapItemState.INACTIVE) {
                val localCord = viewPort.globalToLocal(item.x, item.y)
                if (item.type == MapItemType.FINISH) {
                    val finishItemSubImage = finishItemAsset.bufferedImage.getSubimage(
                        0,
                        0,
                        item.width,
                        item.height
                    )
                    graphics.drawImage(finishItemSubImage, localCord.first, localCord.second, null)
                } else {
                    val mapItemSubImage = mapItemAsset.bufferedImage.getSubimage(
                        item.frameMetadata.cell.x,
                        item.frameMetadata.cell.y,
                        item.width,
                        item.height
                    )
                    graphics.drawImage(mapItemSubImage, localCord.first, localCord.second, null)
                }
            }
        }

        map.enemies.forEach { enemy ->
            val localCord = viewPort.globalToLocal(enemy.x, enemy.y)
            if (enemy.state != MapItemState.INACTIVE) {
                val mapEnemySubImage = mapEnemyAsset.bufferedImage.getSubimage(
                    enemy.frameMetadata.cell.x,
                    enemy.frameMetadata.cell.y,
                    enemy.width,
                    enemy.height
                )
                graphics.drawImage(
                    transformDirection(mapEnemySubImage, enemy.enemyPosition.direction, enemy.width),
                    localCord.first,
                    localCord.second,
                    null
                )
            }
        }
        val particleImage = BufferedImage(viewPort.width, viewPort.height, BufferedImage.TYPE_4BYTE_ABGR)
        map.particles.forEach { particle ->
            val particleGraphics = particleImage.graphics
            val localCord = viewPort.globalToLocal(particle.x, particle.y)
            if (particle.type == ParticleType.COLLISION) {
                particleGraphics.color = Color.WHITE//TODO Hardcoded
                particleGraphics.fillRect(localCord.first, localCord.second, particle.width, particle.height)
            } else {
                particleGraphics.color = Color(230, 234, 218, particle.radius)//TODO Hardcoded
                particleGraphics.fillRect(localCord.first, localCord.second, particle.width, particle.height)
            }
        }
        graphics.drawImage(particleImage, 0, 0, null)
        particleImage.graphics.dispose()
        val playerSubImage =
            playerAsset.bufferedImage.getSubimage(
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
        val backgroundBuffer = ByteArrayOutputStream()
        ImageIO.setUseCache(false)
        ImageIO.write(compositeImage, "bmp", backgroundBuffer)
        compositeImage.graphics.dispose()
        val gameBytes = backgroundBuffer.toByteArray()
        backgroundBuffer.reset()
        return gameBytes
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