package com.github.adamyork.sparrow.game.engine.v1

import com.github.adamyork.sparrow.common.AudioQueue
import com.github.adamyork.sparrow.game.data.Direction
import com.github.adamyork.sparrow.game.data.Player
import com.github.adamyork.sparrow.game.data.ViewPort
import com.github.adamyork.sparrow.game.data.enemy.MapEnemy
import com.github.adamyork.sparrow.game.data.item.MapItem
import com.github.adamyork.sparrow.game.data.item.MapItemState
import com.github.adamyork.sparrow.game.data.item.MapItemType
import com.github.adamyork.sparrow.game.data.map.GameMap
import com.github.adamyork.sparrow.game.data.map.GameMapState
import com.github.adamyork.sparrow.game.engine.Collision
import com.github.adamyork.sparrow.game.engine.Engine
import com.github.adamyork.sparrow.game.engine.Particles
import com.github.adamyork.sparrow.game.engine.Physics
import com.github.adamyork.sparrow.game.engine.data.CollisionBoundaries
import com.github.adamyork.sparrow.game.service.AssetService
import com.github.adamyork.sparrow.game.service.ScoreService
import com.github.adamyork.sparrow.game.service.data.ImageAsset
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Graphics
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

    override fun manageMap(player: Player, gameMap: GameMap, viewPort: ViewPort): GameMap {
        val managedMapItems = manageMapItems(gameMap)
        val managedMapEnemies = manageMapEnemies(gameMap, player, viewPort)
        val managedCollisionParticles = physics.applyCollisionParticlePhysics(gameMap.particles)
        if (player.moving && !player.jumping) {
            val nextDustParticles = particles.createDustParticles(player)
            managedCollisionParticles.addAll(nextDustParticles)
        }
        val managedDustParticles = physics.applyDustParticlePhysics(managedCollisionParticles)
        val managedAllParticles = physics.applyProjectileParticlePhysics(managedDustParticles)
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
            collision.checkForEnemyCollisionAndProximity(player, nextMap, viewPort, audioQueue, particles)
        val projectTileCollisionResult =
            collision.checkForProjectileCollision(
                enemyCollisionResult.first,
                enemyCollisionResult.second,
                viewPort,
                audioQueue,
                particles
            )
        return Pair(projectTileCollisionResult.first, projectTileCollisionResult.second)
    }

    private fun manageMapItems(gameMap: GameMap): ArrayList<MapItem> {
        return gameMap.items.map { item ->
            val itemX = item.x
            val itemY = item.y
            val frameMetadata = item.getNextFrameCell()
            if (item.type == MapItemType.FINISH) {//TODO Simplify if
                var nextState = item.state
                if (gameMap.state == GameMapState.COMPLETING && item.state == MapItemState.INACTIVE) {
                    nextState = MapItemState.ACTIVE
                }
                item.from(itemX, itemY, nextState, frameMetadata)
            } else {
                item.from(itemX, itemY, item.state, frameMetadata)
            }
        }.toCollection(ArrayList())
    }

    private fun manageMapEnemies(gameMap: GameMap, player: Player, viewPort: ViewPort): ArrayList<MapEnemy> {
        return gameMap.enemies.map { enemy ->
            val nextState = enemy.getNextEnemyState(player)
            if (nextState != MapItemState.INACTIVE) {
                val nextPosition = enemy.getNextPosition(player, viewPort)
                val itemX = nextPosition.x
                val itemY = nextPosition.y
                val frameMetadata = enemy.getNextFrameCell()
                enemy.from(itemX, itemY, nextState, frameMetadata, nextPosition, false)
            } else {
                enemy
            }
        }.toCollection(ArrayList())
    }

    override fun draw(
        map: GameMap,
        viewPort: ViewPort,
        player: Player
    ): ByteArray {
        val compositeImage = BufferedImage(viewPort.width, viewPort.height, BufferedImage.TYPE_BYTE_INDEXED)
        val graphics = compositeImage.graphics

        drawBackground(map, viewPort, graphics)
        drawStatusText(map, graphics)
        drawMapItems(map, viewPort, graphics)
        drawMapEnemies(map, viewPort, graphics)
        drawParticles(map, viewPort, graphics)
        drawPlayer(player, viewPort, graphics)

        val backgroundBuffer = ByteArrayOutputStream()
        ImageIO.setUseCache(false)
        ImageIO.write(compositeImage, "bmp", backgroundBuffer)
        compositeImage.graphics.dispose()
        val gameBytes = backgroundBuffer.toByteArray()
        backgroundBuffer.reset()
        return gameBytes
    }

    private fun drawBackground(map: GameMap, viewPort: ViewPort, graphics: Graphics) {
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
        graphics.drawImage(farGroundSubImage, 0, 0, null)
        graphics.drawImage(midGroundSubImage, 0, 0, null)
        graphics.drawImage(nearFieldSubImage, 0, 0, null)
        graphics.drawImage(collisionSubImage, 0, 0, null)
    }

    private fun drawPlayer(player: Player, viewPort: ViewPort, graphics: Graphics) {
        val playerSubImage = player.bufferedImage.getSubimage(
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
            particleGraphics.fillRect(
                localCord.first,
                localCord.second,
                particle.width,
                particle.height
            )//TODO make shape variable
        }
        graphics.drawImage(particleImage, 0, 0, null)
        particleImage.graphics.dispose()
    }

    private fun drawStatusText(map: GameMap, graphics: Graphics) {
        val gameStatusTextImage = assetService.getTextAsset(map.state)
        graphics.drawImage(gameStatusTextImage.image, 0, 0, null)
    }

    private fun drawMapItems(
        map: GameMap,
        viewPort: ViewPort,
        graphics: Graphics
    ) {
        map.items.forEach { item ->
            if (item.state != MapItemState.INACTIVE) {
                val localCord = viewPort.globalToLocal(item.x, item.y)
                val itemSubImage = item.bufferedImage.getSubimage(
                    item.frameMetadata.cell.x,
                    item.frameMetadata.cell.y,
                    item.width,
                    item.height
                )
                graphics.drawImage(itemSubImage, localCord.first, localCord.second, null)
            }
        }
    }

    private fun drawMapEnemies(
        map: GameMap,
        viewPort: ViewPort,
        graphics: Graphics
    ) {
        map.enemies.forEach { enemy ->
            val localCord = viewPort.globalToLocal(enemy.x, enemy.y)
            if (enemy.state != MapItemState.INACTIVE) {
                val itemSubImage = enemy.bufferedImage.getSubimage(
                    enemy.frameMetadata.cell.x,
                    enemy.frameMetadata.cell.y,
                    enemy.width,
                    enemy.height
                )
                graphics.drawImage(
                    transformDirection(itemSubImage, enemy.enemyPosition.direction, enemy.width),
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