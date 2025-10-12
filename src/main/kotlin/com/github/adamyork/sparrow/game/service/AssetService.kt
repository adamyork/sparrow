package com.github.adamyork.sparrow.game.service

import com.github.adamyork.sparrow.common.Sounds
import com.github.adamyork.sparrow.game.Game
import com.github.adamyork.sparrow.game.data.GameMap
import com.github.adamyork.sparrow.game.data.GameMapState
import com.github.adamyork.sparrow.game.service.data.Asset
import com.github.adamyork.sparrow.game.service.data.ItemPositionAndType
import kotlinx.coroutines.reactor.mono
import net.mamoe.yamlkt.YamlMap
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
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

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(AssetService::class.java)
    }

    //sounds
    final val soundBytesMap: HashMap<Sounds, ByteArray> = HashMap()

    //player
    private final val playerWidth: Int
    private final val playerHeight: Int
    private final val playerAssetPath: String

    //maps
    final val mapUrlMap: HashMap<Int, URL?> = HashMap()
    final val mapAssetMap: HashMap<Int, Asset> = HashMap()
    private final val mapWidth: Int
    private final val mapHeight: Int
    private final val mapBackgroundPath: String
    private final val mapMiddleGroundPath: String
    private final val mapForGroundPath: String
    private final val mapCollisionPath: String

    //items
    final val itemUrlMap: HashMap<Int, URL?> = HashMap()
    final val itemPositions: YamlMap
    private final val mapItemWidth: Int
    private final val mapItemHeight: Int
    private final val mapItemAssetOnePath: String
    private final val mapItemAssetTwoPath: String

    //enemies
    private final val mapEnemyWidth: Int
    private final val mapEnemyHeight: Int
    private final val mapEnemyAssetPath: String
    final val enemyUrlMap: HashMap<Int, URL?> = HashMap()
    final val enemyPositions: YamlMap

    constructor(
        @Value("\${map.width}") mapWidth: Int,
        @Value("\${map.height}") mapHeight: Int,
        @Value("\${map.bg}") mapBackgroundPath: String,
        @Value("\${map.mg}") mapMiddleGroundPath: String,
        @Value("\${map.fg}") mapForGroundPath: String,
        @Value("\${map.col}") mapCollisionPath: String,
        @Value("\${player.width}") playerWidth: Int,
        @Value("\${player.height}") playerHeight: Int,
        @Value("\${player.asset.path}") playerAssetPath: String,
        @Value("\${map.item.width}") mapItemWidth: Int,
        @Value("\${map.item.height}") mapItemHeight: Int,
        @Value("\${map.item.asset.one.path}") mapItemAssetOnePath: String,
        @Value("\${map.item.asset.two.path}") mapItemAssetTwoPath: String,
        @Value("\${map.enemy.width}") mapEnemyWidth: Int,
        @Value("\${map.enemy.height}") mapEnemyHeight: Int,
        @Value("\${map.enemy.asset.path}") mapEnemyAssetPath: String,
        @Value("\${audio.player.jump}") audioPlayerJumpPath: String,
        @Value("\${audio.player.collision}") audioPlayerCollisionPath: String,
        @Value("\${audio.item.collect}") audioItemCollectPath: String
    ) {
        this.mapWidth = mapWidth
        this.mapHeight = mapHeight
        this.mapBackgroundPath = mapBackgroundPath
        this.mapMiddleGroundPath = mapMiddleGroundPath
        this.mapForGroundPath = mapForGroundPath
        this.mapCollisionPath = mapCollisionPath
        this.playerWidth = playerWidth
        this.playerHeight = playerHeight
        this.playerAssetPath = playerAssetPath
        this.mapItemWidth = mapItemWidth
        this.mapItemHeight = mapItemHeight
        this.mapItemAssetOnePath = mapItemAssetOnePath
        this.mapItemAssetTwoPath = mapItemAssetTwoPath
        this.mapEnemyWidth = mapEnemyWidth
        this.mapEnemyHeight = mapEnemyHeight
        this.mapEnemyAssetPath = mapEnemyAssetPath

        val applicationYamlFile = urlToFile(this::class.java.classLoader.getResource("application.yml"))
        enemyPositions = parseEnemyPositions(applicationYamlFile)
        itemPositions = parseItemPositions(applicationYamlFile)

        val jumpSoundBytes = urlToBytes(this::class.java.classLoader.getResource(audioPlayerJumpPath))
        val itemCollectSoundBytes = urlToBytes(this::class.java.classLoader.getResource(audioItemCollectPath))
        val playerCollisionSoundBytes = urlToBytes(this::class.java.classLoader.getResource(audioPlayerCollisionPath))

        soundBytesMap[Sounds.JUMP] = jumpSoundBytes
        soundBytesMap[Sounds.ITEM_COLLECT] = itemCollectSoundBytes
        soundBytesMap[Sounds.PLAYER_COLLISION] = playerCollisionSoundBytes

        itemUrlMap[0] = this::class.java.classLoader.getResource(mapItemAssetOnePath)
        itemUrlMap[1] = this::class.java.classLoader.getResource(mapItemAssetTwoPath)

        enemyUrlMap[0] = this::class.java.classLoader.getResource(mapEnemyAssetPath)

        mapUrlMap[0] = this::class.java.classLoader.getResource(mapBackgroundPath)
        mapUrlMap[1] = this::class.java.classLoader.getResource(mapMiddleGroundPath)
        mapUrlMap[2] = this::class.java.classLoader.getResource(mapForGroundPath)
        mapUrlMap[3] = this::class.java.classLoader.getResource(mapCollisionPath)
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
                mapAssetMap[id] = Asset(mapWidth, mapHeight, objects.t1)
                mapAssetMap[id + 1] = Asset(mapWidth, mapHeight, objects.t2)
                mapAssetMap[id + 2] = Asset(mapWidth, mapHeight, objects.t3)
                mapAssetMap[id + 3] = Asset(mapWidth, mapHeight, objects.t4)
                GameMap(
                    GameMapState.COLLECTING,
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
            Asset(mapItemWidth, mapItemHeight, image)
        }
    }

    fun loadEnemy(id: Int): Mono<Asset> {
        val enemyMono = mono {
            val enemyUrl = enemyUrlMap[id]
            val enemyFile = urlToFile(enemyUrl)
            loadBufferedImageAsync(enemyFile)
        }
        return enemyMono.map { image ->
            Asset(mapEnemyWidth, mapEnemyHeight, image)
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

    fun getItemPosition(id: Int): ItemPositionAndType {
        val item: YamlMap = itemPositions[id.toString()] as YamlMap
        return ItemPositionAndType(item.getInt("x"), item.getInt("y"), item.getString("type"))
    }

    fun loadPlayer(): Mono<Asset> {
        val playerFile = urlToFile(this::class.java.classLoader.getResource(playerAssetPath))
        val playerMono = mono {
            loadBufferedImageAsync(playerFile)
        }
        return playerMono.map { image ->
            Asset(playerWidth, playerHeight, image)
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
                throw IOException("file only partially read: `${file.path}`")
            }
        }
        return byteArray
    }


}