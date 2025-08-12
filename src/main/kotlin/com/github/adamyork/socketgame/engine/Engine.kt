package com.github.adamyork.socketgame.engine

import com.github.adamyork.socketgame.game.data.Asset
import com.github.adamyork.socketgame.game.data.Direction
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

    fun tick(player: Player, backgroundAsset: Asset, collisionAsset: Asset): Player {
        val physicsResult = physics.applyPhysics(player, backgroundAsset, collisionAsset)
        return Player(
            physicsResult.x,
            physicsResult.y,
            physicsResult.dy,
            physicsResult.vx,
            physicsResult.vy,
            physicsResult.jumping,
            physicsResult.moving,
            physicsResult.direction,
            physicsResult.floor
        )
    }

    fun paint(
        backgroundImage: BufferedImage,
        playerImage: BufferedImage,
        collisionImage: BufferedImage,
        player: Player
    ): ByteArray {
        val compositeImage = BufferedImage(
            backgroundImage.width, backgroundImage.height,
            BufferedImage.TYPE_3BYTE_BGR
        )
        val graphics = compositeImage.graphics
        graphics.drawImage(backgroundImage, 0, 0, null)
        graphics.drawImage(collisionImage, 0, 0, null)
        graphics.drawImage(transformPlayer(playerImage, player), player.x, player.y, null)
        val backgroundBuffer = ByteArrayOutputStream()
        ImageIO.write(compositeImage, "bmp", backgroundBuffer)
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