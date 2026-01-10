package com.github.adamyork.sparrow.web

import com.github.adamyork.sparrow.game.service.PhysicsSettingsService
import com.github.adamyork.sparrow.game.service.ScoreService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router

@Configuration
@EnableWebFlux
class WebConfig : WebFluxConfigurer {

    @Bean
    fun scoreHandler(scoreService: ScoreService): ScoreHandler = ScoreHandler(scoreService)

    @Bean
    fun physicsHandler(physicsSettingsService: PhysicsSettingsService): PhysicsSettingsHandler =
        PhysicsSettingsHandler(physicsSettingsService)

    @Bean
    fun staticResources(): RouterFunction<ServerResponse> =
        RouterFunctions.resources("/**", ClassPathResource("static/"))

    @Bean
    fun spaFallback(): RouterFunction<ServerResponse> =
        router {
            GET("/") {
                ServerResponse
                    .ok()
                    .bodyValue(ClassPathResource("static/index.html"))
            }
        }

    @Bean
    fun scoreRouter(scoreHandler: ScoreHandler) =
        router {
            GET("/score", scoreHandler::getScore)
        }

    @Bean
    fun physicsRouter(physicsSettingsHandler: PhysicsSettingsHandler) =
        router {
            POST("/physics", accept(APPLICATION_JSON), physicsSettingsHandler::setPhysics)
        }

}