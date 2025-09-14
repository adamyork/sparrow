package com.github.adamyork.socketgame.socket

import com.github.adamyork.socketgame.game.service.AssetService
import com.github.adamyork.socketgame.common.Sounds
import com.github.adamyork.socketgame.socket.GameHandler.Companion.INPUT_KEY_JUMP
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

class InputAudioHandler : WebSocketHandler {
    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(InputAudioHandler::class.java)
    }

    val assetService: AssetService

    constructor(assetService: AssetService) {
        this.assetService = assetService
    }

    override fun handle(session: WebSocketSession): Mono<Void?> {
        val map = session.receive()
            .doOnNext { message -> message.retain() }
            .publishOn(Schedulers.boundedElastic())
            .flatMap { message ->
                val payloadAsText = message.payloadAsText
                val controlCodes = payloadAsText.split(":")
                val input = controlCodes[1]
                var messageFlux = Flux.fromIterable<WebSocketMessage>(listOf())
                if (input == INPUT_KEY_JUMP) {
                    val bytes = assetService.getSoundStream(Sounds.JUMP)
                    val binaryMessage = session.binaryMessage { session -> session.wrap(bytes) }
                    val messages: List<WebSocketMessage> = listOf(binaryMessage)
                    messageFlux = Flux.fromIterable(messages)
                }
                messageFlux
            }
        return session.send(map)
    }
}