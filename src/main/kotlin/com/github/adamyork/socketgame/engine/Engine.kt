package com.github.adamyork.socketgame.engine

import com.github.adamyork.socketgame.engine.data.Particle
import com.github.adamyork.socketgame.game.Game
import com.github.adamyork.socketgame.game.data.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.util.function.Tuple2
import reactor.util.function.Tuple3
import reactor.util.function.Tuples
import java.awt.Color
import java.awt.Rectangle
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO


@Component
class Engine {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(Engine::class.java)
    }

    final val physics: Physics
    final val particles: Particles

    constructor(physics: Physics, particles: Particles) {
        this.physics = physics
        this.particles = particles
    }

    fun managePlayer(player: Player, map: GameMap, collisionAsset: Asset): Player {
        val nextPlayer = Player(
            player.x,
            player.y,
            player.vx,
            player.vy,
            player.jumping,
            player.jumpY,
            player.jumpReached,
            player.moving,
            player.direction,
            player.frameMetadata,
            map.pendingPlayerCollision,
        )
        val physicsResult = physics.applyPlayerPhysics(nextPlayer, map, collisionAsset)
        val frameMetadata = nextPlayer.getNextFrameCell()
        //LOGGER.info("player frameMetadata is ${frameMetadata}")
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

    fun manageMap(player: Player, gameMap: GameMap): GameMap {
        var nextX = gameMap.x
        var nextY = gameMap.y
        var moved = false
        if (player.moving) {
            if (player.direction == Direction.RIGHT) {
                if (player.x + player.width == Game.VIEWPORT_WIDTH - 1) {
                    nextX += GameMap.VIEWPORT_HORIZONTAL_ADVANCE_RATE
                    moved = true
                }
                if (nextX > Game.VIEWPORT_WIDTH) {
                    nextX = Game.VIEWPORT_WIDTH
                    moved = true
                }
            } else {
                if (player.x == 0) {
                    nextX -= GameMap.VIEWPORT_HORIZONTAL_ADVANCE_RATE
                    moved = true
                }
                if (nextX < 0) {
                    nextX = 0
                    moved = true
                }
            }
        }
        if (player.y <= 0) {
            LOGGER.info("move map vertical")
            nextY -= GameMap.VIEWPORT_VERTICAL_ADVANCE_RATE
            if (nextY < 0) {
                nextY = 0
            }
            moved = true
        } else if (player.y + player.height >= Game.VIEWPORT_HEIGHT - 1) {
            nextY += GameMap.VIEWPORT_VERTICAL_ADVANCE_RATE
            if (nextY > Game.VIEWPORT_HEIGHT) {
                nextY = Game.VIEWPORT_HEIGHT
            }
            moved = true
        }
        val managedMapResult = manageMapItems(gameMap, nextX, nextY, player)
        val pendingAudio = ArrayList<Sounds>()
        if (managedMapResult.t2) {
            pendingAudio.add(Sounds.ITEM_COLLECT)
        }
        val managedMapEnemies = manageMapEnemies(gameMap, nextX, nextY, player)
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
            moved,
            managedMapResult.t1,
            managedMapEnemies.t1,
            managedMapEnemies.t3,
            pendingAudio,
            managedMapEnemies.t2
        )
    }

    fun paint(map: GameMap, playerAsset: Asset, player: Player, mapItemAsset: Asset, mapEnemyAsset: Asset): ByteArray {
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
                    item.frameMetadata.cell.t1,
                    item.frameMetadata.cell.t2,
                    item.width,
                    item.height
                )
                graphics.drawImage(mapItemSubImage, item.x, item.y, null)
            }
        }
        map.enemies.forEach { enemy ->
            if (enemy.state != MapItemState.INACTIVE) {
                val mapEnemySubImage = mapEnemyAsset.bufferedImage.getSubimage(
                    enemy.frameMetadata.cell.t1,
                    enemy.frameMetadata.cell.t2,
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
            particleGraphics.color = Color.RED
            particleGraphics.fillRect(particle.x, particle.y, particle.width, particle.height)
        }
        graphics.drawImage(particleImage, 0, 0, null)
        val playerSubImage =
            playerAsset.bufferedImage.getSubimage(player.frameMetadata.cell.t1, player.frameMetadata.cell.t2, 64, 64)
        graphics.drawImage(transformDirection(playerSubImage, player.direction, player.width), player.x, player.y, null)
        val backgroundBuffer = ByteArrayOutputStream()
        ImageIO.write(compositeImage, "png", backgroundBuffer)
        return backgroundBuffer.toByteArray()
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

    private fun manageMapItems(
        gameMap: GameMap,
        nextX: Int,
        nextY: Int,
        player: Player
    ): Tuple2<ArrayList<MapItem>, Boolean> {
        val lastX = gameMap.x
        val lastY = gameMap.y
        val yDelta = lastY - nextY
        val xDelta = nextX - lastX
        var collision = false
        return Tuples.of(gameMap.items.map { item ->
            val itemX = item.x - xDelta
            val itemY = item.y + yDelta
            val itemRect = Rectangle(itemX, itemY, item.width, item.height)
            val playerRect = Rectangle(player.x, player.y, player.width, player.height)
            var itemState = item.state
            if (playerRect.intersects(itemRect) && itemState == MapItemState.ACTIVE) {
                LOGGER.info("item collision !")
                collision = true
                itemState = MapItemState.DEACTIVATING
            }
            var frameMetadata = item.frameMetadata
            if (itemState == MapItemState.DEACTIVATING) {
                frameMetadata = item.getNextFrameCell()
                if (frameMetadata.frame == 0) {
                    itemState = MapItemState.INACTIVE
                }
            }
            MapItem(item.width, item.height, itemX, itemY, itemState, frameMetadata)
        }.toCollection(ArrayList()), collision)
    }

    private fun manageMapEnemies(
        gameMap: GameMap,
        nextX: Int,
        nextY: Int,
        player: Player
    ): Tuple3<ArrayList<MapEnemy>, Boolean, ArrayList<Particle>> {
        val lastX = gameMap.x
        val lastY = gameMap.y
        val yDelta = lastY - nextY
        val xDelta = nextX - lastX
        var isColliding = false
        var mapParticles = gameMap.particles
        return Tuples.of(gameMap.enemies.map { enemy ->
            val nextPosition = enemy.getNextPosition(xDelta, yDelta)
            val itemX = nextPosition.x
            val itemY = nextPosition.y
            val enemyRect = Rectangle(itemX, itemY, enemy.width, enemy.height)
            val playerRect = Rectangle(player.x, player.y, player.width, player.height)
            if (playerRect.intersects(enemyRect)) {
                //LOGGER.info("enemy collision !")
                if (mapParticles.isEmpty()) {
                    mapParticles = particles.createCollisionParticles(itemX, itemY)
                }
                isColliding = true
            }
            mapParticles = physics.applyParticlePhysics(mapParticles)
            mapParticles = mapParticles.filter { particle -> particle.frame <= particle.lifetime }
                .toCollection(ArrayList())
            LOGGER.info("map size = ${mapParticles.size}")
            MapEnemy(
                enemy.width,
                enemy.height,
                itemX,
                itemY,
                enemy.originX - xDelta,
                enemy.originY + yDelta,
                enemy.state,
                enemy.frameMetadata,
                nextPosition
            )
        }.toCollection(ArrayList()), isColliding, mapParticles)
    }


}
