package com.github.adamyork.sparrow.common

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.awt.image.BufferedImage
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi


@OptIn(ExperimentalAtomicApi::class)
@Component
class GameStatusProvider {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(GameStatusProvider::class.java)
    }

    val running: AtomicBoolean = AtomicBoolean(false)
    val lastPaintTime: AtomicInt = AtomicInt(0)
    val backgroundMusicChunk: AtomicInt = AtomicInt(0)
    val lastBackgroundComposite: AtomicReference<BufferedImage> =
        AtomicReference(BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB))

    fun getDeltaTime(): Int {
        var deltaTime = (System.currentTimeMillis().toInt() - lastPaintTime.load()) / 60
        if (deltaTime <= 0) {
            deltaTime = 1
        }
        if (deltaTime >= 3) {
            LOGGER.warn("long deltaTime detected: $deltaTime")
        }
        return deltaTime
    }

}
