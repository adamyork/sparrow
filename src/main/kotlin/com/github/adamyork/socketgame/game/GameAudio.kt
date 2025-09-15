package com.github.adamyork.socketgame.game

import com.github.adamyork.socketgame.common.AudioQueue
import com.github.adamyork.socketgame.game.service.AssetService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.Disposable
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.Duration
import java.util.function.Function
import java.util.function.Supplier

class GameAudio {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(GameAudio::class.java)
    }

    val webSocketSession: WebSocketSession
    val assetService: AssetService
    val audioQueue: AudioQueue

    constructor(
        webSocketSession: WebSocketSession,
        assetService: AssetService,
        audioQueue: AudioQueue
    ) {
        this.assetService = assetService
        this.audioQueue = audioQueue
        this.webSocketSession = webSocketSession
    }

    fun start(): Disposable {
        return Flux.interval(Duration.ofMillis(80))
            .publishOn(Schedulers.boundedElastic())
            .onBackpressureDrop()
            .concatMap(Function { _: Long? ->
                Mono.defer(Supplier {
                    val messages: ArrayList<WebSocketMessage> = ArrayList()
                    if (audioQueue.queue.isNotEmpty()) {
                        LOGGER.info("playing item collect")
                        val sound = audioQueue.queue.element()
                        val bytes = assetService.getSoundStream(sound)
                        audioQueue.queue.remove()
                        val binaryMessage = webSocketSession.binaryMessage { session -> session.wrap(bytes) }
                        messages.add(binaryMessage)
                    }
                    val messageFlux = Flux.fromIterable(messages)
                    webSocketSession.send(messageFlux)
                })
            }, 0)
            .subscribe()
    }
}