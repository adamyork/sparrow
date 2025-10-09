package com.github.adamyork.socketgame.game

import com.github.adamyork.socketgame.common.AudioQueue
import com.github.adamyork.socketgame.common.Sounds
import com.github.adamyork.socketgame.game.service.AssetService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.function.Function

class GameAudio {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(GameAudio::class.java)
    }

    val assetService: AssetService
    val audioQueue: AudioQueue

    constructor(assetService: AssetService, audioQueue: AudioQueue) {
        this.assetService = assetService
        this.audioQueue = audioQueue
    }

    fun next(): Mono<ByteArray> {
        return Flux.fromIterable(listOf(1))
            .collectList()
            .map(Function { _ ->
                if (audioQueue.queue.isNotEmpty()) {
                    val sound = audioQueue.queue.element()
                    if (sound == Sounds.PLAYER_COLLISION) {
                        LOGGER.info("playing player collision audio")
                    } else if (sound == Sounds.ITEM_COLLECT) {
                        LOGGER.info("playing item collect audio")
                    }
                    val bytes = assetService.getSoundStream(sound)
                    audioQueue.queue.remove()
                    bytes
                } else {
                    ByteArray(0)
                }
            })
    }
}