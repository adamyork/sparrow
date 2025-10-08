package com.github.adamyork.socketgame.game.service

import com.github.adamyork.socketgame.common.Sounds
import com.github.adamyork.socketgame.game.Game
import com.github.adamyork.socketgame.game.data.GameMap
import com.github.adamyork.socketgame.game.service.data.Asset
import kotlinx.coroutines.reactor.mono
import net.mamoe.yamlkt.YamlMap
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.util.function.Tuple3
import reactor.util.function.Tuples
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.net.URI
import java.net.URL
import javax.imageio.ImageIO


@Service
class AssetService {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(AssetService::class.java)
    }

    //sprites
    final val playerAsset: Asset
    final val mapItem1Asset: Asset
    final val mapItem2Asset: Asset
    final val mapEnemy1Asset: Asset

    //sounds
    final val soundBytesMap: HashMap<Sounds, ByteArray> = HashMap()

    //maps
    final val mapUrlMap: HashMap<Int, URL?> = HashMap()
    final val mapAssetMap: HashMap<Int, Asset> = HashMap()

    //items
    final val itemUrlMap: HashMap<Int, URL?> = HashMap()
    final val itemPositions: YamlMap

    //items
    final val enemyUrlMap: HashMap<Int, URL?> = HashMap()
    final val enemyPositions: YamlMap

    constructor(
        @Value("\${map.width}") mapWidth: Int,
        @Value("\${map.height}") mapHeight: Int,
        @Value("\${player.width}") playerWidth: Int,
        @Value("\${player.height}") playerHeight: Int,
        @Value("\${player.asset.path}") playerAssetPath: String,
        @Value("\${map.item.width}") mapItemWidth: Int,
        @Value("\${map.item.height}") mapItemHeight: Int,
        @Value("\${map.item.asset.one.path}") mapItemAssetOnePath: String,
        @Value("\${map.item.asset.two.path}") mapItemAssetTwoPath: String,
        @Value("\${map.enemy.width}") mapEnemyWidth: Int,
        @Value("\${map.enemy.height}") mapEnemyHeight: Int,
        @Value("\${map.enemy.asset.path}") mapEnemyAssetPath: String
    ) {
        val applicationYamlFile = urlToFile(this::class.java.classLoader.getResource("application.yml"))
        enemyPositions = parseEnemyPositions(applicationYamlFile)
        itemPositions = parseItemPositions(applicationYamlFile)

        playerAsset = Asset(playerAssetPath, playerWidth, playerHeight)
        mapItem1Asset = Asset(mapItemAssetOnePath, mapItemWidth, mapItemHeight)
        mapItem2Asset = Asset(mapItemAssetTwoPath, mapItemWidth, mapItemHeight)
        mapEnemy1Asset = Asset(mapEnemyAssetPath, mapEnemyWidth, mapEnemyHeight)

        val jumpSoundBytes = urlToBytes(this::class.java.classLoader.getResource("static/jump-sound.wav"))
        val itemCollectSoundBytes = urlToBytes(this::class.java.classLoader.getResource("static/item-collect.wav"))
        val playerCollisionSoundBytes =
            urlToBytes(this::class.java.classLoader.getResource("static/player-collision.wav"))

        soundBytesMap[Sounds.JUMP] = jumpSoundBytes
        soundBytesMap[Sounds.ITEM_COLLECT] = itemCollectSoundBytes
        soundBytesMap[Sounds.PLAYER_COLLISION] = playerCollisionSoundBytes

        itemUrlMap[0] = this::class.java.classLoader.getResource(mapItem1Asset.path)
        itemUrlMap[1] = this::class.java.classLoader.getResource(mapItem2Asset.path)

        enemyUrlMap[0] = this::class.java.classLoader.getResource(mapEnemy1Asset.path)

        mapAssetMap[0] = Asset("static/map1-bg-full-comp.png", mapWidth, mapHeight)
        mapAssetMap[1] = Asset("static/map1-mg-full-comp.png", mapWidth, mapHeight)
        mapAssetMap[2] = Asset("static/map-1-near-field.png", mapWidth, mapHeight)
        mapAssetMap[3] = Asset("static/map1-collision.png", mapWidth, mapHeight)

        mapUrlMap[0] = this::class.java.classLoader.getResource(mapAssetMap[0]?.path)
        mapUrlMap[1] = this::class.java.classLoader.getResource(mapAssetMap[1]?.path)
        mapUrlMap[2] = this::class.java.classLoader.getResource(mapAssetMap[2]?.path)
        mapUrlMap[3] = this::class.java.classLoader.getResource(mapAssetMap[3]?.path)
    }

    private fun parseEnemyPositions(file: File): YamlMap {
        val yamlDefault = net.mamoe.yamlkt.Yaml.Default
        val properties = yamlDefault.decodeFromString(YamlMap.serializer(), file.readText())
        val map = properties["map"] as YamlMap
        val enemy = map["enemy"] as YamlMap
        return enemy["position"] as YamlMap
    }

    private fun parseItemPositions(file: File): YamlMap {
        val yamlDefault = net.mamoe.yamlkt.Yaml.Default
        val properties = yamlDefault.decodeFromString(YamlMap.serializer(), file.readText())
        val map = properties["map"] as YamlMap
        val item = map["item"] as YamlMap
        return item["position"] as YamlMap
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
        }.onErrorMap {
            LOGGER.error("cant load the far ground image")
            RuntimeException("cant load an image")
        }
        val midGroundMono = mono {
            loadBufferedImageAsync(midGroundFile)
        }.onErrorMap {
            LOGGER.error("cant load the mid ground image")
            RuntimeException("cant load an image")
        }
        val nearFieldMono = mono {
            loadBufferedImageAsync(nearFieldFile)
        }.onErrorMap {
            LOGGER.error("cant load the near field image")
            RuntimeException("cant load an image")
        }
        val collisionMono = mono {
            loadBufferedImageAsync(collisionFile)
        }.onErrorMap {
            LOGGER.error("cant load the collision image")
            RuntimeException("cant load an image")
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
                    Game.VIEWPORT_HEIGHT,
                    mapAssetMap[id + 3]!!.width,
                    mapAssetMap[id + 3]!!.height,
                    ArrayList(),
                    ArrayList(),
                    ArrayList()
                )
            }
    }

    fun loadItem(id: Int): Mono<Asset> {
        val itemMono = mono {
            val itemUrl = itemUrlMap[id]
            val itemFile = urlToFile(itemUrl)
            loadBufferedImageAsync(itemFile)
        }
        return itemMono.map { image ->
            if (id == 0) {
                mapItem1Asset.bufferedImage = image
                mapItem1Asset
            } else {
                mapItem2Asset.bufferedImage = image
                mapItem2Asset
            }
        }
    }

    fun loadEnemy(id: Int): Mono<Asset> {
        val enemyMono = mono {
            val enemyUrl = enemyUrlMap[id]
            val enemyFile = urlToFile(enemyUrl)
            loadBufferedImageAsync(enemyFile)
        }
        return enemyMono.map { image ->
            mapEnemy1Asset.bufferedImage = image
            mapEnemy1Asset
        }
    }

    fun getTotalEnemies(): Int {
        return enemyPositions.keys.size
    }

    fun getEnemyPosition(id: Int): Pair<Int, Int> {
        val item: YamlMap = enemyPositions[id.toString()] as YamlMap
        return Pair(item.getInt("x"), item.getInt("y"))
    }

    fun getTotalItems(): Int {
        return itemPositions.keys.size
    }

    fun getItemPosition(id: Int): Tuple3<Int, Int, String> {
        val item: YamlMap = itemPositions[id.toString()] as YamlMap
        return Tuples.of(item.getInt("x"), item.getInt("y"), item.getString("type"))
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