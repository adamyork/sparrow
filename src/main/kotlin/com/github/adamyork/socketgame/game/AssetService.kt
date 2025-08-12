package com.github.adamyork.socketgame.game

import com.github.adamyork.socketgame.game.data.Asset
import org.springframework.stereotype.Service
import java.io.File
import java.net.URI
import javax.imageio.ImageIO

@Service
class AssetService {

    final val backgroundAsset: Asset = Asset("static/forest.bmp", 1024, 768)
    final val playerAsset: Asset = Asset("static/rabbit.bmp", 64, 64)
    final val collisionAsset: Asset = Asset("static/collision-map.bmp", 1024, 768)

    constructor() {
        val background = this::class.java.classLoader.getResource(backgroundAsset.path)
        val player = this::class.java.classLoader.getResource(playerAsset.path)
        val collision = this::class.java.classLoader.getResource(collisionAsset.path)
        val backgroundUri: URI = background?.toURI() ?: URI("")
        val playerUri: URI = player?.toURI() ?: URI("")
        val collisionUri: URI = collision?.toURI() ?: URI("")
        val backgroundFile = File(backgroundUri.path)
        val playerFile = File(playerUri.path)
        val collisionFile = File(collisionUri.path)
        backgroundAsset.bufferedImage = ImageIO.read(backgroundFile)
        playerAsset.bufferedImage = ImageIO.read(playerFile)
        collisionAsset.bufferedImage = ImageIO.read(collisionFile)
    }
}