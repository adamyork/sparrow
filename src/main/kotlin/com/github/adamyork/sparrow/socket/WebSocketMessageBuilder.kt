package com.github.adamyork.sparrow.socket

import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession

interface WebSocketMessageBuilder {

    fun build(session: WebSocketSession, bytes: ByteArray): WebSocketMessage {
        return session.binaryMessage { session -> session.wrap(bytes) }
    }

}