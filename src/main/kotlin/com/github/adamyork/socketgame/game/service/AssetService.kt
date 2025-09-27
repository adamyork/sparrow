package com.github.adamyork.socketgame.game.service

import com.github.adamyork.socketgame.common.Sounds
import com.github.adamyork.socketgame.game.Game
import com.github.adamyork.socketgame.game.data.GameMap
import com.github.adamyork.socketgame.game.service.data.Asset
import kotlinx.coroutines.reactor.mono
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.awt.image.BufferedImage
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
        val playerCollisionSoundBytes =
            urlToBytes(this::class.java.classLoader.getResource("static/player-collision.wav"))

        soundBytesMap[Sounds.JUMP] = jumpSoundBytes
        soundBytesMap[Sounds.ITEM_COLLECT] = itemCollectSoundBytes
        soundBytesMap[Sounds.PLAYER_COLLISION] = playerCollisionSoundBytes

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

    suspend fun loadBufferedImageAsync(file: File): BufferedImage {
        return ImageIO.read(file)
    }

    fun loadMap(id: Int): Mono<GameMap> {
        val farGroundFile = urlToFile(mapUrlMap[id])
        val midGroundFile = urlToFile(mapUrlMap[id + 1])
        val nearFieldFile = urlToFile(mapUrlMap[id + 2])
        val collisionFile = urlToFile(mapUrlMap[id + 3])

        val farGroundMono = mono {
            loadBufferedImageAsync(farGroundFile)
        }
        val midGroundMono = mono {
            loadBufferedImageAsync(midGroundFile)
        }
        val nearFieldMono = mono {
            loadBufferedImageAsync(nearFieldFile)
        }
        val collisionMono = mono {
            loadBufferedImageAsync(collisionFile)
        }

        return Mono.zip(farGroundMono, midGroundMono, nearFieldMono, collisionMono)
            .map { objects ->
                mapAssetMap[id]?.bufferedImage = objects.t1
                mapAssetMap[id + 1]?.bufferedImage = objects.t2
                mapAssetMap[id + 2]?.bufferedImage = objects.t3
                mapAssetMap[id + 3]?.bufferedImage = objects.t4
                GameMap(
                    mapAssetMap[id]!!,
                    mapAssetMap[id + 1]!!,
                    mapAssetMap[id + 2]!!,
                    mapAssetMap[id + 3]!!,
                    0,
                    Game.Companion.VIEWPORT_HEIGHT,
                    mapAssetMap[id + 3]!!.width,
                    mapAssetMap[id + 3]!!.height,
                    ArrayList(),
                    ArrayList(),
                    ArrayList()
                )
            }

    }

    fun loadItem(id: Int): Mono<Asset> {
        val itemUrl = itemUrlMap[id]
        val itemFile = urlToFile(itemUrl)
        val itemMono = mono {
            loadBufferedImageAsync(itemFile)
        }
        return itemMono.map { image ->
            mapItem1Asset.bufferedImage = image
            mapItem1Asset
        }
    }

    fun loadEnemy(id: Int): Mono<Asset> {
        val enemyUrl = enemyUrlMap[id]
        val enemyFile = urlToFile(enemyUrl)
        val enemyMono = mono {
            loadBufferedImageAsync(enemyFile)
        }
        return enemyMono.map { image ->
            mapEnemy1Asset.bufferedImage = image
            mapEnemy1Asset
        }
    }

    fun loadPlayer(): Mono<Asset> {
        val playerFile = urlToFile(this::class.java.classLoader.getResource(playerAsset.path))
        val playerMono = mono {
            loadBufferedImageAsync(playerFile)
        }
        return playerMono.map { image ->
            playerAsset.bufferedImage = image
            playerAsset
        }
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
                throw IOException("File only partially read: `${file.path}`")
            }
        }
        return byteArray
    }


}