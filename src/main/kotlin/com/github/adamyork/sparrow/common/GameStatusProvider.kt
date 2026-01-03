package com.github.adamyork.sparrow.common

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.awt.image.BufferedImage
import kotlin.concurrent.atomics.*


@OptIn(ExperimentalAtomicApi::class)
@Component
class GameStatusProvider {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(GameStatusProvider::class.java)
    }

    final val fpsMax: Int
    final val fpsMin: Int

    constructor(
        @Value("\${engine.fps.max}") fpsMax: Int,
        @Value("\${engine.fps.min}") fpsMin: Int
    ) {
        this.fpsMax = fpsMax
        this.fpsMin = fpsMin
    }

    val running: AtomicBoolean = AtomicBoolean(false)
    val lastPaintTime: AtomicLong = AtomicLong(0L)
    val backgroundMusicChunk: AtomicInt = AtomicInt(0)
    val lastBackgroundComposite: AtomicReference<BufferedImage> =
        AtomicReference(BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB))

    fun getDeltaTime(): Double {
        val targetDeltaTimeMs = 1000 / fpsMax
        val deltaTime = System.currentTimeMillis().toInt() - lastPaintTime.load()
        if (deltaTime > targetDeltaTimeMs) {
            val deltaTimePercent: Double = (deltaTime - targetDeltaTimeMs).toDouble() / targetDeltaTimeMs.toDouble()
            val numOfFramesDropped = fpsMax * deltaTimePercent
            return if ((fpsMax - numOfFramesDropped.toInt()) < fpsMin) {
                //LOGGER.info("FPS drop detected; long deltaTime $deltaTimePercent percent; frames: $numOfFramesDropped")
                1.0 + deltaTimePercent
            } else {
                1.0
            }
        }
        return 1.0
    }

    fun reset() {
        lastPaintTime.store(0L)
        backgroundMusicChunk.store(0)
        lastBackgroundComposite.store(BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB))
    }

}
