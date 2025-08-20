package com.github.adamyork.socketgame.game

import com.github.adamyork.socketgame.game.data.Asset
import com.github.adamyork.socketgame.game.data.GameMap
import org.springframework.stereotype.Service
import java.io.File
import java.net.URI
import javax.imageio.ImageIO

@Service
class AssetService {

    final val map1FarGroundAsset: Asset = Asset("static/map-1-far-ground.png", 2048, 1536)
    final val map1MidGroundAsset: Asset = Asset("static/map-1-mid-ground.png", 2048, 1536)
    final val map1NearFieldAsset: Asset = Asset("static/map-1-near-field.png", 2048, 1536)
    final val map1CollisionAsset: Asset = Asset("static/map-1-collision.png", 2048, 1536)
    final val playerAsset: Asset = Asset("static/rabbit.bmp", 64, 64)


    constructor() {

    }

    fun loadMap(id: Int): GameMap {
        val farGround = this::class.java.classLoader.getResource(map1FarGroundAsset.path)
        val midGround = this::class.java.classLoader.getResource(map1MidGroundAsset.path)
        val nearField = this::class.java.classLoader.getResource(map1NearFieldAsset.path)
        val collision = this::class.java.classLoader.getResource(map1CollisionAsset.path)


        val farGroundUri: URI = farGround?.toURI() ?: URI("")
        val midGroundUri: URI = midGround?.toURI() ?: URI("")
        val nearFieldUri: URI = nearField?.toURI() ?: URI("")
        val collisionUri: URI = collision?.toURI() ?: URI("")


        val farGroundFile = File(farGroundUri.path)
        val midGroundFile = File(midGroundUri.path)
        val nearFieldFile = File(nearFieldUri.path)
        val collisionFile = File(collisionUri.path)

        map1FarGroundAsset.bufferedImage = ImageIO.read(farGroundFile)
        map1MidGroundAsset.bufferedImage = ImageIO.read(midGroundFile)
        map1NearFieldAsset.bufferedImage = ImageIO.read(nearFieldFile)
        map1CollisionAsset.bufferedImage = ImageIO.read(collisionFile)

        return GameMap(
            map1FarGroundAsset,
            map1MidGroundAsset,
            map1NearFieldAsset,
            map1CollisionAsset,
            0,
            Game.VIEWPORT_HEIGHT,
            map1CollisionAsset.width,
            map1CollisionAsset.height,
            false
        )
    }

    fun loadPlayer(): Asset {
        val player = this::class.java.classLoader.getResource(playerAsset.path)
        val playerUri: URI = player?.toURI() ?: URI("")
        val playerFile = File(playerUri.path)
        playerAsset.bufferedImage = ImageIO.read(playerFile)
        return playerAsset
    }
}