package com.github.adamyork.socketgame.socket

import com.github.adamyork.socketgame.common.AudioQueue
import com.github.adamyork.socketgame.game.GameAudio
import com.github.adamyork.socketgame.game.service.AssetService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers


class GameAudioHandler : WebSocketHandler {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(InputAudioHandler::class.java)
    }

    val assetService: AssetService
    val audioQueue: AudioQueue

    lateinit var gameAudio: GameAudio

    constructor(assetService: AssetService, audioQueue: AudioQueue) {
        this.assetService = assetService
        this.audioQueue = audioQueue
    }

    override fun handle(session: WebSocketSession): Mono<Void> {
        val map = session.receive()
            .doOnNext { message -> message.retain() }
            .publishOn(Schedulers.boundedElastic())
            .map { message ->
                LOGGER.info("Game audio started")
                val payloadAsText = message.payloadAsText
                session.textMessage("Message Received: $payloadAsText")
            }
            .flatMap { message ->
                if (!gameAudio.isInitialized) {
                    gameAudio = GameAudio(session, assetService, audioQueue)
                    gameAudio.start().map {
                        message
                    }
                } else {
                    Mono.just(message)
                }
            }
        return session.send(map)
    }
}