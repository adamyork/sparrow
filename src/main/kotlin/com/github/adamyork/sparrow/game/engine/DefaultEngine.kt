package com.github.adamyork.sparrow.game.engine

import com.github.adamyork.sparrow.common.AudioQueue
import com.github.adamyork.sparrow.game.Game
import com.github.adamyork.sparrow.game.data.*
import com.github.adamyork.sparrow.game.engine.data.Particle
import com.github.adamyork.sparrow.game.engine.data.ParticleType
import com.github.adamyork.sparrow.game.engine.data.PlayerMapPair
import com.github.adamyork.sparrow.game.service.ScoreService
import com.github.adamyork.sparrow.game.service.data.Asset
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Color
import java.awt.Font
import java.awt.FontMetrics
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

    constructor(
        physics: Physics,
        collision: Collision,
        particles: Particles,
        audioQueue: AudioQueue,
        scoreService: ScoreService
    ) {
        this.physics = physics
        this.particles = particles
        this.audioQueue = audioQueue
        this.collision = collision
        this.scoreService = scoreService
    }

    override fun managePlayer(player: Player): Player {
        val physicsAppliedPlayer = physics.applyPlayerPhysics(player)
        val nextFrameMetadata = player.getNextFrameCell()
        return Player(
            physicsAppliedPlayer.x,
            physicsAppliedPlayer.y,
            physicsAppliedPlayer.width,
            physicsAppliedPlayer.height,
            physicsAppliedPlayer.vx,
            physicsAppliedPlayer.vy,
            physicsAppliedPlayer.jumping,
            physicsAppliedPlayer.jumpDy,
            physicsAppliedPlayer.jumpReached,
            physicsAppliedPlayer.moving,
            physicsAppliedPlayer.direction,
            nextFrameMetadata,
            physicsAppliedPlayer.colliding,
            physicsAppliedPlayer.scanVerticalCeiling,
            physicsAppliedPlayer.scanVerticalFloor
        )
    }

    override fun manageMap(
        player: Player,
        gameMap: GameMap
    ): GameMap {
        var nextX = gameMap.x
        var nextY = gameMap.y
        if (player.moving) {
            if (player.direction == Direction.RIGHT) {
                if (player.x + player.width == Game.VIEWPORT_WIDTH - 1) {
                    nextX += GameMap.VIEWPORT_HORIZONTAL_ADVANCE_RATE
                }
                if (nextX > Game.VIEWPORT_WIDTH) {
                    nextX = Game.VIEWPORT_WIDTH
                }
            } else {
                if (player.x == 0) {
                    nextX -= GameMap.VIEWPORT_HORIZONTAL_ADVANCE_RATE
                }
                if (nextX < 0) {
                    nextX = 0
                }
            }
        }
        if (player.y <= 0) {
            LOGGER.info("move map vertical up")
            nextY -= GameMap.VIEWPORT_VERTICAL_ADVANCE_RATE
            if (nextY < 0) {
                nextY = 0
            }
        } else if (player.y + player.height >= Game.VIEWPORT_HEIGHT - 1) {
            LOGGER.info("move map vertical down")
            nextY += GameMap.VIEWPORT_VERTICAL_ADVANCE_RATE
            if (nextY > Game.VIEWPORT_HEIGHT) {
                nextY = Game.VIEWPORT_HEIGHT
            }
        }
        val managedMapItems = manageMapItems(gameMap, nextX, nextY)
        val managedMapEnemies = manageMapEnemies(gameMap, nextX, nextY)
        val managedCollisionParticles = physics.applyCollisionParticlePhysics(gameMap.particles)
        val dustParticles: ArrayList<Particle> = gameMap.particles
            .filter { it.type == ParticleType.DUST }.toCollection(ArrayList())
        if (player.moving && !player.jumping) {
            val nextDustParticles =
                particles.createDustParticles(player.x, player.y, player.width, player.height, player.direction)
            dustParticles.addAll(nextDustParticles)
        }
        val managedDustParticles = physics.applyDustParticlePhysics(dustParticles)
        managedCollisionParticles.addAll(managedDustParticles)
        var mapState = gameMap.state
        if (mapState == GameMapState.COLLECTING && scoreService.allFound()) {
            LOGGER.info("all items found map is in completing mode")
            mapState = GameMapState.COMPLETING
        }
        return GameMap(
            mapState,
            gameMap.farGroundAsset,
            gameMap.midGroundAsset,
            gameMap.nearFieldAsset,
            gameMap.collisionAsset,
            nextX,
            nextY,
            gameMap.width,
            gameMap.height,
            managedMapItems,
            managedMapEnemies,
            managedCollisionParticles,
        )
    }

    override fun manageCollision(
        player: Player,
        previousX: Int,
        previousY: Int,
        map: GameMap,
        collisionAsset: Asset
    ): PlayerMapPair {
        val currentCollisionArea = collisionAsset.bufferedImage.getSubimage(map.x, map.y, 1024, 768)
        var nextPlayer = collision.checkForPlayerCollision(previousX, previousY, player, currentCollisionArea)
        var nextMap = collision.checkForItemCollision(nextPlayer, map, audioQueue)
        val enemyCollisionResult = collision.checkForEnemyCollision(nextPlayer, nextMap, audioQueue, particles)
        nextPlayer = enemyCollisionResult.player
        nextMap = enemyCollisionResult.map
        return PlayerMapPair(nextPlayer, nextMap)
    }

    private fun manageMapItems(
        gameMap: GameMap,
        nextX: Int,
        nextY: Int
    ): ArrayList<MapItem> {
        val lastX = gameMap.x
        val lastY = gameMap.y
        val yDelta = lastY - nextY
        val xDelta = nextX - lastX
        return gameMap.items.map { item ->
            val itemX = item.x - xDelta
            val itemY = item.y + yDelta
            val frameMetadata = item.getNextFrameCell()
            MapItem(item.width, item.height, itemX, itemY, item.type, item.state, frameMetadata)
        }.toCollection(ArrayList())
    }

    private fun manageMapEnemies(
        gameMap: GameMap,
        nextX: Int,
        nextY: Int
    ): ArrayList<MapEnemy> {
        val lastX = gameMap.x
        val lastY = gameMap.y
        val yDelta = lastY - nextY
        val xDelta = nextX - lastX
        return gameMap.enemies.map { enemy ->
            val nextPosition = enemy.getNextPosition(xDelta, yDelta)
            val itemX = nextPosition.x
            val itemY = nextPosition.y
            val frameMetadata = enemy.getNextFrameCell()
            MapEnemy(
                enemy.width,
                enemy.height,
                itemX,
                itemY,
                enemy.originX - xDelta,
                enemy.originY + yDelta,
                enemy.state,
                frameMetadata,
                nextPosition,
                false
            )
        }.toCollection(ArrayList())
    }

    override fun paint(
        map: GameMap,
        playerAsset: Asset,
        player: Player,
        mapItemAsset: Asset,
        finishItemAsset: Asset,
        mapEnemyAsset: Asset
    ): ByteArray {
        val compositeImage = BufferedImage(
            Game.VIEWPORT_WIDTH, Game.VIEWPORT_HEIGHT,
            BufferedImage.TYPE_4BYTE_ABGR
        )
        val graphics = compositeImage.graphics
        var farGroundX = map.x / GameMap.VIEWPORT_HORIZONTAL_FAR_PARALLAX_OFFSET
        var midGroundX = map.x / GameMap.VIEWPORT_HORIZONTAL_MID_PARALLAX_OFFSET
        if (farGroundX < 0 || farGroundX > Game.VIEWPORT_WIDTH) {
            farGroundX = map.x
        }
        if (midGroundX < 0 || midGroundX > Game.VIEWPORT_WIDTH) {
            midGroundX = map.x
        }
        val farGroundSubImage = map.farGroundAsset.bufferedImage.getSubimage(farGroundX, map.y, 1024, 768)
        val midGroundSubImage = map.midGroundAsset.bufferedImage.getSubimage(midGroundX, map.y, 1024, 768)
        val nearFieldSubImage = map.nearFieldAsset.bufferedImage.getSubimage(map.x, map.y, 1024, 768)
        val collisionSubImage = map.collisionAsset.bufferedImage.getSubimage(map.x, map.y, 1024, 768)
        graphics.drawImage(farGroundSubImage, 0, 0, null)
        graphics.drawImage(midGroundSubImage, 0, 0, null)
        graphics.drawImage(nearFieldSubImage, 0, 0, null)
        graphics.drawImage(collisionSubImage, 0, 0, null)
        if (map.state == GameMapState.COMPLETING) {
            val gameScreenMessage = "FIND THE FINISH FLAG"
            graphics.color = Color.BLACK
            graphics.font = Font("Arial", Font.BOLD, 32)
            val metrics: FontMetrics? = graphics.getFontMetrics(graphics.font)
            val txtX = (Game.VIEWPORT_WIDTH - (metrics?.stringWidth(gameScreenMessage) ?: 0)) / 2
            val txtY = ((Game.VIEWPORT_HEIGHT - (metrics?.height ?: 0)) / 2) + (metrics?.ascent ?: 0)
            graphics.drawString(gameScreenMessage, txtX, txtY)
            map.activateFinish()
        }

        map.items.forEach { item ->
            if (item.state != MapItemState.INACTIVE) {
                if (item.type == MapItemType.FINISH) {
                    val finishItemSubImage = finishItemAsset.bufferedImage.getSubimage(
                        0,
                        0,
                        item.width,
                        item.height
                    )
                    graphics.drawImage(finishItemSubImage, item.x, item.y, null)
                } else {

                    val mapItemSubImage = mapItemAsset.bufferedImage.getSubimage(
                        item.frameMetadata.cell.x,
                        item.frameMetadata.cell.y,
                        item.width,
                        item.height
                    )
                    graphics.drawImage(mapItemSubImage, item.x, item.y, null)
                }
            }
        }
        map.enemies.forEach { enemy ->
            if (enemy.state != MapItemState.INACTIVE) {
                val mapEnemySubImage = mapEnemyAsset.bufferedImage.getSubimage(
                    enemy.frameMetadata.cell.x,
                    enemy.frameMetadata.cell.y,
                    enemy.width,
                    enemy.height
                )
                graphics.drawImage(
                    transformDirection(mapEnemySubImage, enemy.enemyPosition.direction, enemy.width),
                    enemy.x,
                    enemy.y,
                    null
                )
            }
        }
        val particleImage = BufferedImage(
            Game.VIEWPORT_WIDTH, Game.VIEWPORT_HEIGHT,
            BufferedImage.TYPE_4BYTE_ABGR
        )
        map.particles.forEach { particle ->
            val particleGraphics = particleImage.graphics
            if (particle.type == ParticleType.COLLISION) {
                particleGraphics.color = Color.WHITE
                particleGraphics.fillRect(particle.x, particle.y, particle.width, particle.height)
            } else {
                particleGraphics.color = Color(230, 234, 218, particle.radius)
                particleGraphics.fillOval(particle.x, particle.y, particle.width, particle.height)
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
        graphics.drawImage(transformDirection(playerSubImage, player.direction, player.width), player.x, player.y, null)
        val backgroundBuffer = ByteArrayOutputStream()
        ImageIO.write(compositeImage, "png", backgroundBuffer)
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