package com.github.adamyork.sparrow.game.service.v1

import com.github.adamyork.sparrow.game.service.AssetService
import com.github.adamyork.sparrow.game.service.WavService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem

class DefaultWavService : WavService {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(DefaultWavService::class.java)
    }

    override fun chunk(file: File, chunkMs: Int): HashMap<Int, ByteArray> {
        val output = HashMap<Int, ByteArray>()
        val audioInputStream = AudioSystem.getAudioInputStream(file)
        val audioFormat = audioInputStream?.format ?: AudioFormat(0F, 0, 0, false, false)
        val sampleBits: Int = audioFormat.getSampleSizeInBits()
        val sampleRate: Float = audioFormat.getSampleRate()
        val bytesPerMilliSecond: Float = ((sampleBits * sampleRate * audioFormat.getChannels()) / 8) / 1000
        val chunkBytes = bytesPerMilliSecond.toInt() * chunkMs
        var bytesRead: Int
        val readBuffer = ByteArray(chunkBytes)
        var chunkIndex = 0
        while ((audioInputStream.read(readBuffer).also { bytesRead = it }) != -1) {
            ByteArrayOutputStream().use { outputStream ->
                outputStream.write(readBuffer, 0, bytesRead)
                val bytesIn = ByteArrayInputStream(outputStream.toByteArray())
                val chunkAudioInputStream =
                    AudioInputStream(bytesIn, audioFormat, (readBuffer.size / audioFormat.getFrameSize()).toLong())
                val tempFile = File.createTempFile("bg_music_tmp_$chunkIndex", ".wav")
                AudioSystem.write(chunkAudioInputStream, AudioSystem.getAudioFileFormat(file).type, tempFile)
                val chunkBytes = AssetService.Companion.getBytes(tempFile)
                output[chunkIndex] = chunkBytes
                chunkIndex++
            }
        }
        return output
    }

}