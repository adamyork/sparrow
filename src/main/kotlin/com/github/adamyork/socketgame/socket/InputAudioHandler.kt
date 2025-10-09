package com.github.adamyork.socketgame.socket

import com.github.adamyork.socketgame.common.GameStatusProvider
import com.github.adamyork.socketgame.common.Sounds
import com.github.adamyork.socketgame.game.service.AssetService
import com.github.adamyork.socketgame.socket.GameHandler.Companion.INPUT_KEY_JUMP
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import kotlin.concurrent.atomics.ExperimentalAtomicApi

class InputAudioHandler : WebSocketHandler {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(InputAudioHandler::class.java)
    }

    val assetService: AssetService
    val gameStatusProvider: GameStatusProvider

    constructor(assetService: AssetService, gameStatusProvider: GameStatusProvider) {
        this.assetService = assetService
        this.gameStatusProvider = gameStatusProvider
    }

    @OptIn(ExperimentalAtomicApi::class)
    override fun handle(session: WebSocketSession): Mono<Void> {
        val map = session.receive()
            .flatMap { message ->
                var messageFlux = Flux.fromIterable<WebSocketMessage>(listOf())
                if (!gameStatusProvider.running.load()) {
                    messageFlux
                } else {
                    val payloadAsText = message.payloadAsText
                    val controlCodes = payloadAsText.split(":")
                    val input = controlCodes[1]
                    if (input == INPUT_KEY_JUMP) {
                        val bytes = assetService.getSoundStream(Sounds.JUMP)
                        val binaryMessage = session.binaryMessage { session -> session.wrap(bytes) }
                        val messages: List<WebSocketMessage> = listOf(binaryMessage)
                        messageFlux = Flux.fromIterable(messages)
                    }
                    messageFlux
                }

            }
        return session.send(map)
    }
}