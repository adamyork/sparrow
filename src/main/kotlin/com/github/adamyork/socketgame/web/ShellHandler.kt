package com.github.adamyork.socketgame.web

import com.github.adamyork.socketgame.web.data.Greeting
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono


@Component
class ShellHandler {
    fun hello(request: ServerRequest?): Mono<ServerResponse?> {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue<Any?>(Greeting("Hello, Spring!")))
    }
}