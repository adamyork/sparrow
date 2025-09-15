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
    var gameAudio: GameAudio? = null

    constructor(assetService: AssetService, audioQueue: AudioQueue) {
        this.assetService = assetService
        this.audioQueue = audioQueue
    }

    override fun handle(session: WebSocketSession): Mono<Void?> {
        val map = session.receive()
            .doOnNext { message -> message.retain() }
            .publishOn(Schedulers.boundedElastic())
            .map { message ->
                LOGGER.info("Fx Started")
                val payloadAsText = message.payloadAsText
                gameAudio = GameAudio(session, assetService, audioQueue)
                gameAudio?.start()
                session.textMessage("Message Received: $payloadAsText")
            }
        return session.send(map)
    }
}