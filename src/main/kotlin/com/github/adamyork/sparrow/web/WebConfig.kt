package com.github.adamyork.sparrow.web

import com.github.adamyork.sparrow.game.service.ScoreService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.function.server.router

@Configuration
@EnableWebFlux
class WebConfig : WebFluxConfigurer {

    @Bean
    fun scoreHandler(scoreService: ScoreService): ScoreHandler = ScoreHandler(scoreService)

    @Bean
    fun rootRouter() =
        router {
            val index = ClassPathResource("static/index.html")
            val extensions = listOf("js", "css", "wav", "png")
            val spaPredicate = !(path("/score") or
                    pathExtension(extensions::contains))
            resource(spaPredicate, index)
        }

    @Bean
    fun jsRouter() =
        router {
            val index = ClassPathResource("static/main.js")
            val extensions = listOf("css", "wav", "png")
            val spaPredicate = !(path("/score") or
                    pathExtension(extensions::contains))
            resource(spaPredicate, index)
        }

    @Bean
    fun cssRouter() =
        router {
            val index = ClassPathResource("static/main.css")
            val extensions = listOf("js", "wav", "png")
            val spaPredicate = !(path("/score") or
                    pathExtension(extensions::contains))
            resource(spaPredicate, index)
        }

    @Bean
    fun wavRouter() =
        router {
            val index = ClassPathResource("static/level-1-music.wav")
            val extensions = listOf("js", "css", "html", "png")
            val spaPredicate = !(path("/score") or
                    pathExtension(extensions::contains))
            resource(spaPredicate, index)
        }

    @Bean
    fun pngRouter() =
        router {
            val index = ClassPathResource("static/splash.png")
            val extensions = listOf("js", "css", "html", "wav")
            val spaPredicate = !(path("/score") or
                    pathExtension(extensions::contains))
            resource(spaPredicate, index)
        }

    @Bean
    fun scoreRouter(scoreHandler: ScoreHandler) =
        router {
            GET("/score", scoreHandler::getScore)
        }

}