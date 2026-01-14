package com.github.adamyork.sparrow.socket

import com.github.adamyork.sparrow.common.StatusProvider
import com.github.adamyork.sparrow.common.data.Sounds
import com.github.adamyork.sparrow.game.service.AssetService
import com.github.adamyork.sparrow.socket.InputHandler.Companion.INPUT_KEY_JUMP
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
    val statusProvider: StatusProvider
    val webSocketMessageBuilder: WebSocketMessageBuilder

    constructor(
        assetService: AssetService,
        statusProvider: StatusProvider,
        webSocketMessageBuilder: WebSocketMessageBuilder
    ) {
        this.assetService = assetService
        this.statusProvider = statusProvider
        this.webSocketMessageBuilder = webSocketMessageBuilder
    }

    @OptIn(ExperimentalAtomicApi::class)
    override fun handle(session: WebSocketSession): Mono<Void> {
        val map = session.receive()
            .flatMap { message ->
                var messageFlux = Flux.fromIterable<WebSocketMessage>(listOf())
                if (!statusProvider.running.load()) {
                    messageFlux
                } else {
                    val payloadAsText = message.payloadAsText
                    val controlCodes = payloadAsText.split(":")
                    if (controlCodes.size > 1) {
                        val input = controlCodes[1]
                        if (input == INPUT_KEY_JUMP) {
                            val bytes = assetService.getSoundStream(Sounds.JUMP)
                            val binaryMessage = webSocketMessageBuilder.build(session, bytes)
                            val messages: List<WebSocketMessage> = listOf(binaryMessage)
                            messageFlux = Flux.fromIterable(messages)
                        }
                    } else {
                        LOGGER.error("no control codes found")
                    }
                    messageFlux
                }

            }
        return session.send(map)
    }
}