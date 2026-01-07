package com.github.adamyork.sparrow.game.service.v1

import com.github.adamyork.sparrow.common.Sounds
import com.github.adamyork.sparrow.game.data.map.GameMap
import com.github.adamyork.sparrow.game.data.map.GameMapState
import com.github.adamyork.sparrow.game.service.AssetService
import com.github.adamyork.sparrow.game.service.WavService
import com.github.adamyork.sparrow.game.service.data.ImageAsset
import com.github.adamyork.sparrow.game.service.data.ItemPositionAndType
import com.github.adamyork.sparrow.game.service.data.TextAsset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.withContext
import net.mamoe.yamlkt.Yaml
import net.mamoe.yamlkt.YamlMap
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import java.awt.Color
import java.awt.Font
import java.awt.FontMetrics
import java.awt.image.BufferedImage
import java.io.File
import java.lang.reflect.Field
import java.net.URI
import java.net.URL
import java.util.*
import javax.imageio.ImageIO


class DefaultAssetService : AssetService {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(DefaultAssetService::class.java)
    }

    override var applicationYamlFile: File

    val wavService: WavService

    //sounds
    val soundBytesMap: EnumMap<Sounds, ByteArray> = EnumMap(Sounds::class.java)
    override var backgroundMusicBytesMap: HashMap<Int, ByteArray> = HashMap()

    //viewport
    private val viewPortWidth: Int
    private val viewPortHeight: Int

    //player
    private val playerWidth: Int
    private val playerHeight: Int
    private val playerAssetPath: String

    //maps
    val mapUrlMap: HashMap<Int, URL?> = HashMap()
    val mapAssetMap: HashMap<Int, ImageAsset> = HashMap()
    private val mapWidth: Int
    private val mapHeight: Int
    private val mapBackgroundPath: String
    private val mapMiddleGroundPath: String
    private val mapForGroundPath: String
    private val mapCollisionPath: String

    //items
    val itemUrlMap: HashMap<Int, URL?> = HashMap()
    val itemPositions: YamlMap
    private val mapItemWidth: Int
    private val mapItemHeight: Int
    private val mapItemAssetOnePath: String
    private val mapItemAssetTwoPath: String

    //enemies
    private val mapEnemyOneWidth: Int
    private val mapEnemyOneHeight: Int
    private val mapEnemyTwoWidth: Int
    private val mapEnemyTwoHeight: Int
    private val mapEnemyAssetOnePath: String
    private val mapEnemyAssetTwoPath: String
    val enemyUrlMap: HashMap<Int, URL?> = HashMap()
    val enemyPositions: YamlMap

    //text
    val textAssetMap: EnumMap<GameMapState, TextAsset> = EnumMap(GameMapState::class.java)

    constructor(
        wavService: WavService,
        viewPortWidth: Int,
        viewPortHeight: Int,
        mapWidth: Int,
        mapHeight: Int,
        mapBackgroundPath: String,
        mapMiddleGroundPath: String,
        mapForGroundPath: String,
        mapCollisionPath: String,
        playerWidth: Int,
        playerHeight: Int,
        playerAssetPath: String,
        mapItemWidth: Int,
        mapItemHeight: Int,
        mapItemAssetOnePath: String,
        mapItemAssetTwoPath: String,
        mapEnemyOneWidth: Int,
        mapEnemyOneHeight: Int,
        mapEnemyAssetOnePath: String,
        mapEnemyAssetTwoPath: String,
        mapEnemyTwoWidth: Int,
        mapEnemyTwoHeight: Int,
        audioPlayerJumpPath: String,
        audioPlayerCollisionPath: String,
        audioItemCollectPath: String,
        audioBackgroundMusicPath: String,
        audioEnemyShootPath: String,
        mapDirectiveInitialText: String,
        mapDirectiveInitialTextColor: String,
        mapDirectiveFinishText: String,
        mapDirectiveFinishTextColor: String,
        mapDirectiveCompleteText: String,
        mapDirectiveCompleteTextColor: String
    ) {
        this.wavService = wavService
        this.viewPortWidth = viewPortWidth
        this.viewPortHeight = viewPortHeight
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
        this.mapEnemyOneWidth = mapEnemyOneWidth
        this.mapEnemyOneHeight = mapEnemyOneHeight
        this.mapEnemyTwoWidth = mapEnemyTwoWidth
        this.mapEnemyTwoHeight = mapEnemyTwoHeight
        this.mapEnemyAssetOnePath = mapEnemyAssetOnePath
        this.mapEnemyAssetTwoPath = mapEnemyAssetTwoPath

        applicationYamlFile = urlToFile(this::class.java.classLoader.getResource("application.yml"))
        enemyPositions = parseEnemyPositions(applicationYamlFile)
        itemPositions = parseItemPositions(applicationYamlFile)

        val jumpSoundBytes = urlToBytes(this::class.java.classLoader.getResource(audioPlayerJumpPath))
        val itemCollectSoundBytes = urlToBytes(this::class.java.classLoader.getResource(audioItemCollectPath))
        val playerCollisionSoundBytes = urlToBytes(this::class.java.classLoader.getResource(audioPlayerCollisionPath))
        val enemyShootSoundBytes = urlToBytes(this::class.java.classLoader.getResource(audioEnemyShootPath))

        soundBytesMap[Sounds.JUMP] = jumpSoundBytes
        soundBytesMap[Sounds.ITEM_COLLECT] = itemCollectSoundBytes
        soundBytesMap[Sounds.PLAYER_COLLISION] = playerCollisionSoundBytes
        soundBytesMap[Sounds.ENEMY_SHOOT] = enemyShootSoundBytes

        itemUrlMap[0] = this::class.java.classLoader.getResource(mapItemAssetOnePath)
        itemUrlMap[1] = this::class.java.classLoader.getResource(mapItemAssetTwoPath)

        enemyUrlMap[0] = this::class.java.classLoader.getResource(mapEnemyAssetOnePath)
        enemyUrlMap[1] = this::class.java.classLoader.getResource(mapEnemyAssetTwoPath)

        mapUrlMap[0] = this::class.java.classLoader.getResource(mapBackgroundPath)
        mapUrlMap[1] = this::class.java.classLoader.getResource(mapMiddleGroundPath)
        mapUrlMap[2] = this::class.java.classLoader.getResource(mapForGroundPath)
        mapUrlMap[3] = this::class.java.classLoader.getResource(mapCollisionPath)

        val backgroundMusicFile = urlToFile(this::class.java.classLoader.getResource(audioBackgroundMusicPath))
        backgroundMusicBytesMap = wavService.chunk(backgroundMusicFile, 25000)

        val collectItemsAssetText = buildTextAsset(
            viewPortWidth,
            viewPortHeight,
            Font("Arial", Font.BOLD, 32),
            findColorByName(mapDirectiveInitialTextColor),
            mapDirectiveInitialText,
            centerX = true,
            centerY = false,
        )
        val finishGameTextAsset = buildTextAsset(
            viewPortWidth,
            viewPortHeight,
            Font("Arial", Font.BOLD, 32),
            findColorByName(mapDirectiveFinishTextColor),
            mapDirectiveFinishText,
            centerX = true,
            centerY = false,
        )
        val gameCompleteTextAsset = buildTextAsset(
            viewPortWidth,
            viewPortHeight,
            Font("Arial", Font.BOLD, 32),
            findColorByName(mapDirectiveCompleteTextColor),
            mapDirectiveCompleteText,
            centerX = true,
            centerY = false,
        )
        textAssetMap[GameMapState.COLLECTING] = collectItemsAssetText
        textAssetMap[GameMapState.COMPLETING] = finishGameTextAsset
        textAssetMap[GameMapState.COMPLETED] = gameCompleteTextAsset
    }

    private fun parseEnemyPositions(file: File): YamlMap {
        val yamlDefault = Yaml.Default
        val properties = yamlDefault.decodeFromString(YamlMap.serializer(), file.readText())
        val map = properties["map"] as YamlMap
        val enemy = map["enemy"] as YamlMap
        return enemy["position"] as YamlMap
    }

    private fun parseItemPositions(file: File): YamlMap {
        val yamlDefault = Yaml.Default
        val properties = yamlDefault.decodeFromString(YamlMap.serializer(), file.readText())
        val map = properties["map"] as YamlMap
        val item = map["item"] as YamlMap
        return item["position"] as YamlMap
    }

    override suspend fun loadBufferedImageAsync(file: File): BufferedImage {
        return withContext(Dispatchers.IO) {
            ImageIO.read(file)
        }
    }

    override fun loadMap(id: Int): Mono<GameMap> {
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
                mapAssetMap[id] = ImageAsset(mapWidth, mapHeight, objects.t1)
                mapAssetMap[id + 1] = ImageAsset(mapWidth, mapHeight, objects.t2)
                mapAssetMap[id + 2] = ImageAsset(mapWidth, mapHeight, objects.t3)
                mapAssetMap[id + 3] = ImageAsset(mapWidth, mapHeight, objects.t4)
                GameMap(
                    GameMapState.COLLECTING,
                    mapAssetMap[id]!!,
                    mapAssetMap[id + 1]!!,
                    mapAssetMap[id + 2]!!,
                    mapAssetMap[id + 3]!!,
                    mapAssetMap[id + 3]!!.width,
                    mapAssetMap[id + 3]!!.height,
                    ArrayList(),
                    ArrayList(),
                    ArrayList()
                )
            }
    }

    override fun loadItem(id: Int): Mono<ImageAsset> {
        val itemMono = mono {
            val itemUrl = itemUrlMap[id]
            val itemFile = urlToFile(itemUrl)
            loadBufferedImageAsync(itemFile)
        }
        return itemMono.map { image ->
            ImageAsset(mapItemWidth, mapItemHeight, image)
        }
    }

    override fun loadEnemy(id: Int): Mono<ImageAsset> {
        val enemyMono = mono {
            val enemyUrl = enemyUrlMap[id]
            val enemyFile = urlToFile(enemyUrl)
            loadBufferedImageAsync(enemyFile)
        }
        return enemyMono.map { image ->
            if (id == 0) {
                ImageAsset(mapEnemyOneWidth, mapEnemyOneHeight, image)
            } else {
                ImageAsset(mapEnemyTwoWidth, mapEnemyTwoHeight, image)
            }
        }
    }

    override fun getTotalEnemies(): Int {
        return enemyPositions.keys.size
    }

    override fun getEnemyPosition(id: Int): ItemPositionAndType {
        val item: YamlMap = enemyPositions[id.toString()] as YamlMap
        return ItemPositionAndType(item.getInt("x"), item.getInt("y"), item.getString("type"))
    }

    override fun getTotalItems(): Int {
        return itemPositions.keys.size
    }

    override fun getItemPosition(id: Int): ItemPositionAndType {
        val item: YamlMap = itemPositions[id.toString()] as YamlMap
        return ItemPositionAndType(item.getInt("x"), item.getInt("y"), item.getString("type"))
    }

    override fun loadPlayer(): Mono<ImageAsset> {
        val playerFile = urlToFile(this::class.java.classLoader.getResource(playerAssetPath))
        val playerMono = mono {
            loadBufferedImageAsync(playerFile)
        }
        return playerMono.map { image ->
            ImageAsset(playerWidth, playerHeight, image)
        }
    }

    override fun getSoundStream(sound: Sounds): ByteArray {
        return soundBytesMap.getOrElse(sound) { byteArrayOf() }
    }

    @Suppress("SameParameterValue")
    private fun buildTextAsset(
        viewPortWidth: Int,
        viewPortHeight: Int,
        font: Font,
        color: Color,
        message: String,
        centerX: Boolean,
        centerY: Boolean
    ): TextAsset {
        val textImage = BufferedImage(
            viewPortWidth, viewPortHeight,
            BufferedImage.TYPE_4BYTE_ABGR
        )
        val graphics = textImage.graphics
        graphics.font = font
        val metrics: FontMetrics? = graphics.getFontMetrics(graphics.font)
        val textWidth = metrics?.stringWidth(message) ?: 0
        val textHeight = metrics?.height ?: 0
        val textAscent: Int = metrics?.ascent ?: 0
        val x: Int = if (centerX) {
            (viewPortWidth - textWidth) / 2
        } else {
            0
        }
        val y: Int = if (centerY) {
            ((viewPortHeight - textHeight) / 2) + textAscent
        } else {
            0 + textAscent
        }
        graphics.color = Color.WHITE
        graphics.fillRect(
            (x - 5).coerceAtLeast(0),
            (y - textAscent).coerceAtLeast(0),
            textWidth + 10,
            textHeight
        )
        graphics.color = color
        graphics.drawString(message, x, y)
        return TextAsset(textImage)
    }

    override fun getTextAsset(gameMapState: GameMapState): TextAsset {
        return textAssetMap[gameMapState] ?: TextAsset(BufferedImage(0, 0, 0))
    }

    private fun urlToFile(url: URL?): File {
        val uri: URI = url?.toURI() ?: URI("")
        return File(uri.path)
    }

    private fun urlToBytes(url: URL?): ByteArray {
        val file = urlToFile(url)
        return AssetService.getBytes(file)
    }

    private fun findColorByName(name: String): Color {
        var color: Color
        try {
            val field: Field = Color::class.java.getField(name)
            color = field.get(null) as Color
        } catch (exception: Exception) {
            LOGGER.info("color not found $exception")
            color = Color.CYAN
        }
        return color
    }

}