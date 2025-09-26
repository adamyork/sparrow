package com.github.adamyork.socketgame.game.engine

import com.github.adamyork.socketgame.common.AudioQueue
import com.github.adamyork.socketgame.game.Game
import com.github.adamyork.socketgame.game.data.*
import com.github.adamyork.socketgame.game.engine.data.Particle
import com.github.adamyork.socketgame.game.service.data.Asset
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.util.function.Tuple2
import reactor.util.function.Tuples
import java.awt.Color
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

class SinglePassEngine : Engine {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(SinglePassEngine::class.java)
    }

    val physics: Physics
    val collision: Collision
    val particles: Particles
    val audioQueue: AudioQueue

    constructor(physics: Physics, collision: Collision, particles: Particles, audioQueue: AudioQueue) {
        this.physics = physics
        this.particles = particles
        this.audioQueue = audioQueue
        this.collision = collision
    }

    override fun managePlayer(
        player: Player,
        map: GameMap,
        collisionAsset: Asset
    ): Player {
        val physicsResult = physics.applyPlayerPhysics(player, map, collisionAsset)
        val frameMetadata = player.getNextFrameCell()
        return Player(
            physicsResult.x,
            physicsResult.y,
            physicsResult.vx,
            physicsResult.vy,
            physicsResult.jumping,
            physicsResult.jumpY,
            physicsResult.jumpReached,
            physicsResult.moving,
            physicsResult.direction,
            frameMetadata,
            physicsResult.colliding,
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
        //LOGGER.info("player.x : ${player.x} nextX is ${nextX}")
        return GameMap(
            gameMap.farGroundAsset,
            gameMap.midGroundAsset,
            gameMap.nearFieldAsset,
            gameMap.collisionAsset,
            nextX,
            nextY,
            gameMap.width,
            gameMap.height,
            managedMapItems,
            managedMapEnemies.t1,
            managedMapEnemies.t2,
        )
    }

    override fun manageCollision(
        player: Player,
        previousX: Int,
        previousY: Int,
        map: GameMap,
        collisionAsset: Asset
    ): Tuple2<Player, GameMap> {
        val currentCollisionArea = collisionAsset.bufferedImage.getSubimage(map.x, map.y, 1024, 768)
        var nextPlayer = collision.checkForPlayerCollision(previousX, previousY, player, currentCollisionArea)
        var nextMap = collision.checkForItemCollision(nextPlayer, map, audioQueue)
        val enemyCollisionResult = collision.checkForEnemyCollision(nextPlayer, nextMap, audioQueue, particles, physics)
        nextPlayer = enemyCollisionResult.player
        nextMap = enemyCollisionResult.map
        return Tuples.of(nextPlayer, nextMap)
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
            var itemState = item.state
            var frameMetadata = item.frameMetadata
            if (itemState == MapItemState.DEACTIVATING) {
                frameMetadata = item.getNextFrameCell()
                if (frameMetadata.frame == 0) {
                    itemState = MapItemState.INACTIVE
                }
            }
            MapItem(item.width, item.height, itemX, itemY, itemState, frameMetadata)
        }.toCollection(ArrayList())
    }

    private fun manageMapEnemies(
        gameMap: GameMap,
        nextX: Int,
        nextY: Int
    ): Tuple2<ArrayList<MapEnemy>, ArrayList<Particle>> {
        val lastX = gameMap.x
        val lastY = gameMap.y
        val yDelta = lastY - nextY
        val xDelta = nextX - lastX
        var mapParticles = gameMap.particles
        return Tuples.of(gameMap.enemies.map { enemy ->
            val nextPosition = enemy.getNextPosition(xDelta, yDelta)
            val itemX = nextPosition.x
            val itemY = nextPosition.y
            mapParticles = physics.applyParticlePhysics(mapParticles)
            mapParticles = mapParticles.filter { particle -> particle.frame <= particle.lifetime }
                .toCollection(ArrayList())
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
        }.toCollection(ArrayList()), mapParticles)
    }

    override fun paint(
        map: GameMap,
        playerAsset: Asset,
        player: Player,
        mapItemAsset: Asset,
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
        val nearFieldSubImag = map.nearFieldAsset.bufferedImage.getSubimage(map.x, map.y, 1024, 768)
        val collisionSubImage = map.collisionAsset.bufferedImage.getSubimage(map.x, map.y, 1024, 768)
        graphics.drawImage(farGroundSubImage, 0, 0, null)
        graphics.drawImage(midGroundSubImage, 0, 0, null)
        graphics.drawImage(nearFieldSubImag, 0, 0, null)
        graphics.drawImage(collisionSubImage, 0, 0, null)
        map.items.forEach { item ->
            if (item.state != MapItemState.INACTIVE) {
                val mapItemSubImage = mapItemAsset.bufferedImage.getSubimage(
                    item.frameMetadata.cell.x,
                    item.frameMetadata.cell.y,
                    item.width,
                    item.height
                )
                graphics.drawImage(mapItemSubImage, item.x, item.y, null)
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
            particleGraphics.color = Color.WHITE
            particleGraphics.fillRect(particle.x, particle.y, particle.width, particle.height)
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