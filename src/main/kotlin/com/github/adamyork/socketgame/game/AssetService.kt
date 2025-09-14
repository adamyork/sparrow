package com.github.adamyork.socketgame.game

import com.github.adamyork.socketgame.game.data.Asset
import com.github.adamyork.socketgame.game.data.GameMap
import com.github.adamyork.socketgame.game.data.Sounds
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.net.URI
import java.net.URL
import javax.imageio.ImageIO


@Service
class AssetService {

    //sprites
    final val playerAsset: Asset = Asset("static/rabbit.png", 64, 64)
    final val mapItem1Asset: Asset = Asset("static/item.png", 64, 64)
    final val mapEnemy1Asset: Asset = Asset("static/vacuum.png", 128, 128)

    //sounds
    final val soundBytesMap: HashMap<Sounds, ByteArray> = HashMap()

    //maps
    final val mapUrlMap: HashMap<Int, URL?> = HashMap()
    final val mapAssetMap: HashMap<Int, Asset> = HashMap()

    //items
    final val itemUrlMap: HashMap<Int, URL?> = HashMap()

    //items
    final val enemyUrlMap: HashMap<Int, URL?> = HashMap()

    constructor() {
        val jumpSoundBytes = urlToBytes(this::class.java.classLoader.getResource("static/jump-sound.wav"))
        val itemCollectSoundBytes = urlToBytes(this::class.java.classLoader.getResource("static/item-collect.wav"))
        soundBytesMap[Sounds.JUMP] = jumpSoundBytes
        soundBytesMap[Sounds.ITEM_COLLECT] = itemCollectSoundBytes
        itemUrlMap[0] = this::class.java.classLoader.getResource(mapItem1Asset.path)
        enemyUrlMap[0] = this::class.java.classLoader.getResource(mapEnemy1Asset.path)

        mapAssetMap[0] = Asset("static/map-1-far-ground.png", 2048, 1536)
        mapAssetMap[1] = Asset("static/map-1-mid-ground.png", 2048, 1536)
        mapAssetMap[2] = Asset("static/map-1-near-field.png", 2048, 1536)
        mapAssetMap[3] = Asset("static/map-1-collision.png", 2048, 1536)

        mapUrlMap[0] = this::class.java.classLoader.getResource(mapAssetMap[0]?.path)
        mapUrlMap[1] = this::class.java.classLoader.getResource(mapAssetMap[1]?.path)
        mapUrlMap[2] = this::class.java.classLoader.getResource(mapAssetMap[2]?.path)
        mapUrlMap[3] = this::class.java.classLoader.getResource(mapAssetMap[3]?.path)
    }

    fun loadMap(id: Int): GameMap {
        val farGroundFile = urlToFile(mapUrlMap[id])
        val midGroundFile = urlToFile(mapUrlMap[id + 1])
        val nearFieldFile = urlToFile(mapUrlMap[id + 2])
        val collisionFile = urlToFile(mapUrlMap[id + 3])

        mapAssetMap[id]?.bufferedImage = ImageIO.read(farGroundFile)
        mapAssetMap[id + 1]?.bufferedImage = ImageIO.read(midGroundFile)
        mapAssetMap[id + 2]?.bufferedImage = ImageIO.read(nearFieldFile)
        mapAssetMap[id + 3]?.bufferedImage = ImageIO.read(collisionFile)

        return GameMap(
            mapAssetMap[id]!!,
            mapAssetMap[id + 1]!!,
            mapAssetMap[id + 2]!!,
            mapAssetMap[id + 3]!!,
            0,
            Game.VIEWPORT_HEIGHT,
            mapAssetMap[id + 3]!!.width,
            mapAssetMap[id + 3]!!.height,
            false,
            ArrayList(),
            ArrayList(),
            ArrayList(),
            ArrayList(),
            false
        )
    }

    fun loadItem(id: Int): Asset {
        val itemUrl = itemUrlMap[id]
        val itemFile = urlToFile(itemUrl)
        mapItem1Asset.bufferedImage = ImageIO.read(itemFile)
        return mapItem1Asset
    }

    fun loadEnemy(id: Int): Asset {
        val enemyUrl = enemyUrlMap[id]
        val enemyFile = urlToFile(enemyUrl)
        mapEnemy1Asset.bufferedImage = ImageIO.read(enemyFile)
        return mapEnemy1Asset
    }

    fun loadPlayer(): Asset {
        val playerFile = urlToFile(this::class.java.classLoader.getResource(playerAsset.path))
        playerAsset.bufferedImage = ImageIO.read(playerFile)
        return playerAsset
    }

    fun getSoundStream(sound: Sounds): ByteArray {
        return soundBytesMap.getOrElse(sound) { byteArrayOf() }
    }

    private fun urlToFile(url: URL?): File {
        val uri: URI = url?.toURI() ?: URI("")
        return File(uri.path)
    }

    private fun urlToBytes(url: URL?): ByteArray {
        val file = urlToFile(url)
        return getBytes(file)
    }

    private fun getBytes(file: File): ByteArray {
        val byteArray = ByteArray(file.length().toInt())
        FileInputStream(file).use { fis ->
            val bytesRead = fis.read(byteArray)
            if (bytesRead != byteArray.size) {
                throw IOException("Could not read the entire file.")
            }
        }
        return byteArray
    }


}