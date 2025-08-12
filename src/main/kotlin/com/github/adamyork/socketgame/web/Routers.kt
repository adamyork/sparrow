package com.github.adamyork.socketgame.web

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.RequestPredicates.GET
import org.springframework.web.reactive.function.server.RequestPredicates.accept
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerResponse


@Configuration(proxyBeanMethods = false)
class Routers {

    @Bean
    fun route(shellHandler: ShellHandler): RouterFunction<ServerResponse?> {
        return RouterFunctions
            .route(GET("/game")
                .and(accept(MediaType.APPLICATION_JSON)), shellHandler::hello)
    }

}