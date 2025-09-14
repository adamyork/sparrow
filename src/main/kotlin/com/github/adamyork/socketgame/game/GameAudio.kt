package com.github.adamyork.socketgame.game

import com.github.adamyork.socketgame.game.service.AssetService
import com.github.adamyork.socketgame.socket.GameHandler
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
    val gameHandler: GameHandler

    constructor(gameHandler: GameHandler, webSocketSession: WebSocketSession, assetService: AssetService) {
        this.gameHandler = gameHandler
        this.assetService = assetService
        this.webSocketSession = webSocketSession
    }

    fun start(): Disposable {
        return Flux.interval(Duration.ofMillis(80))
            .publishOn(Schedulers.boundedElastic())
            .onBackpressureDrop()
            .concatMap(Function { foo: Long? ->
                Mono.defer(Supplier {
                    val messages: ArrayList<WebSocketMessage> = ArrayList()
                    if (this.gameHandler.game?.isInitialized == true) {
                        val audio = this.gameHandler.game?.gameMap?.pendingAudio
                        while (audio?.isNotEmpty() ?: false) {
                            LOGGER.info("playing item collect")
                            val sound = audio[0]
                            audio.removeAt(0)
                            val bytes = assetService.getSoundStream(sound)
                            val binaryMessage = webSocketSession.binaryMessage { session -> session.wrap(bytes) }
                            messages.add(binaryMessage)
                        }
                    }
                    val messageFlux = Flux.fromIterable(messages)
                    webSocketSession.send(messageFlux)
                })
            }, 0)
            .subscribe()
    }
}