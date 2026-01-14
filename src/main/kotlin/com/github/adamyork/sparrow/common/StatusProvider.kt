package com.github.adamyork.sparrow.common

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.image.BufferedImage
import kotlin.concurrent.atomics.*


@OptIn(ExperimentalAtomicApi::class)
class StatusProvider {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(StatusProvider::class.java)
    }

    val fpsMax: Int
    val fpsMin: Int

    constructor(fpsMax: Int, fpsMin: Int) {
        this.fpsMax = fpsMax
        this.fpsMin = fpsMin
    }

    val running: AtomicBoolean = AtomicBoolean(false)
    val lastPaintTime: AtomicLong = AtomicLong(0L)
    val backgroundMusicChunkIndex: AtomicInt = AtomicInt(0)
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
        backgroundMusicChunkIndex.store(0)
        lastBackgroundComposite.store(BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB))
    }

}
