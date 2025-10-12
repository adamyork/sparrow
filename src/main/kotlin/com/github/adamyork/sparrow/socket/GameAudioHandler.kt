package com.github.adamyork.sparrow.socket

import com.github.adamyork.sparrow.common.AudioQueue
import com.github.adamyork.sparrow.game.GameAudio
import com.github.adamyork.sparrow.game.service.AssetService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono


class GameAudioHandler : WebSocketHandler {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(GameAudioHandler::class.java)
    }

    val assetService: AssetService
    val audioQueue: AudioQueue
    val gameAudio: GameAudio

    constructor(gameAudio: GameAudio, assetService: AssetService, audioQueue: AudioQueue) {
        this.gameAudio = gameAudio
        this.assetService = assetService
        this.audioQueue = audioQueue
    }

    override fun handle(session: WebSocketSession): Mono<Void> {
        val map = session.receive()
            .flatMap { _ ->
                gameAudio.next().map { bytes ->
                    session.binaryMessage { session -> session.wrap(bytes) }
                }
            }
        return session.send(map)
    }
}