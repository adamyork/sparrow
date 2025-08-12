package com.github.adamyork.socketgame.socket

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono

class InputHandler : WebSocketHandler {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(InputHandler::class.java)
    }

    override fun handle(session: WebSocketSession): Mono<Void?> {
        val output = session.receive()
            .doOnNext { message -> LOGGER.debug(message.payloadAsText) }
            .map { session.textMessage("Echo: $it") }
        return session.send(output)
    }
}
