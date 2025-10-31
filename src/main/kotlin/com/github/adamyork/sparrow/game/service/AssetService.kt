package com.github.adamyork.sparrow.game.service

import com.github.adamyork.sparrow.common.Sounds
import com.github.adamyork.sparrow.game.data.map.GameMap
import com.github.adamyork.sparrow.game.data.map.GameMapState
import com.github.adamyork.sparrow.game.service.data.ImageAsset
import com.github.adamyork.sparrow.game.service.data.ItemPositionAndType
import com.github.adamyork.sparrow.game.service.data.TextAsset
import reactor.core.publisher.Mono
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream
import java.io.IOException

interface AssetService {

    companion object {
        fun getBytes(file: File): ByteArray {
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

    var backgroundMusicBytesMap: HashMap<Int, ByteArray>
    var applicationYamlFile: File

    suspend fun loadBufferedImageAsync(file: File): BufferedImage

    fun loadMap(id: Int): Mono<GameMap>

    fun loadItem(id: Int): Mono<ImageAsset>

    fun loadEnemy(id: Int): Mono<ImageAsset>

    fun getTotalEnemies(): Int

    fun getEnemyPosition(id: Int): ItemPositionAndType

    fun getTotalItems(): Int

    fun getItemPosition(id: Int): ItemPositionAndType

    fun loadPlayer(): Mono<ImageAsset>

    fun getSoundStream(sound: Sounds): ByteArray

    fun getTextAsset(gameMapState: GameMapState): TextAsset

}