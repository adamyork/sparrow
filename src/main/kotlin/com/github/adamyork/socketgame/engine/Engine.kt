package com.github.adamyork.socketgame.engine

import com.github.adamyork.socketgame.game.Game
import com.github.adamyork.socketgame.game.data.Asset
import com.github.adamyork.socketgame.game.data.Direction
import com.github.adamyork.socketgame.game.data.GameMap
import com.github.adamyork.socketgame.game.data.Player
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
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

    constructor(physics: Physics) {
        this.physics = physics
    }

    fun managePlayer(player: Player, map: GameMap, collisionAsset: Asset): Player {
        val physicsResult = physics.applyPlayerPhysics(player, map, collisionAsset)
        return Player(
            physicsResult.x,
            physicsResult.y,
            physicsResult.vx,
            physicsResult.vy,
            physicsResult.jumping,
            physicsResult.jumpY,
            physicsResult.jumpReached,
            physicsResult.moving,
            physicsResult.direction
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
        if(player.y <= 0){
            LOGGER.info("move map vertical")
            nextY -= GameMap.VIEWPORT_VERTICAL_ADVANCE_RATE
            if(nextY < 0){
                nextY = 0
            }
            moved = true
        } else if(player.y + player.height >= Game.VIEWPORT_HEIGHT - 1) {
            nextY += GameMap.VIEWPORT_VERTICAL_ADVANCE_RATE
            if(nextY > Game.VIEWPORT_HEIGHT) {
                nextY = Game.VIEWPORT_HEIGHT
            }
            moved = true
        }
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
            moved
        )
    }

    fun paint(map: GameMap, playerAsset: Asset, player: Player): ByteArray {
        val compositeImage = BufferedImage(
            Game.VIEWPORT_WIDTH, Game.VIEWPORT_HEIGHT,
            BufferedImage.TYPE_4BYTE_ABGR
        )
        //LOGGER.info("player.x = ${player.x}, player.y = ${player.y} map.x = ${map.x} map.y = ${map.y}")
        val graphics = compositeImage.graphics
        val farGroundSubImage = map.farGroundAsset.bufferedImage.getSubimage(map.x, map.y, 1024, 768)
        val midGroundSubImage = map.midGroundAsset.bufferedImage.getSubimage(map.x, map.y, 1024, 768)
        val nearFieldSubImag = map.nearFieldAsset.bufferedImage.getSubimage(map.x, map.y, 1024, 768)
        val collisionSubImage = map.collisionAsset.bufferedImage.getSubimage(map.x, map.y, 1024, 768)
        graphics.drawImage(farGroundSubImage, 0, 0, null)
        graphics.drawImage(midGroundSubImage, 0, 0, null)
        graphics.drawImage(nearFieldSubImag, 0, 0, null)
        graphics.drawImage(collisionSubImage, 0, 0, null)
        graphics.drawImage(transformPlayer(playerAsset.bufferedImage, player), player.x, player.y, null)
        val backgroundBuffer = ByteArrayOutputStream()
        ImageIO.write(compositeImage, "png", backgroundBuffer)
        return backgroundBuffer.toByteArray()
    }

    private fun transformPlayer(playerImage: BufferedImage, player: Player): BufferedImage {
        if (player.direction == Direction.LEFT) {
            val tx = AffineTransform.getScaleInstance(-1.0, 1.0)
            tx.translate(-player.width.toDouble(), 0.0)
            val op = AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR)
            return op.filter(playerImage, null)
        }
        return playerImage
    }


}
