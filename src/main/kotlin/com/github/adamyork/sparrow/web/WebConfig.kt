package com.github.adamyork.sparrow.web

import com.github.adamyork.sparrow.game.service.PhysicsSettingsService
import com.github.adamyork.sparrow.game.service.ScoreService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.WebFluxConfigurer
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
    fun rootRouter() =
        router {
            val index = ClassPathResource("static/index.html")
            val extensions = listOf("js", "css", "wav", "png")
            val spaPredicate = !(path("/score") or
                    path("/physics") or
                    pathExtension(extensions::contains) or
                    path("/form.html"))
            resource(spaPredicate, index)
        }

    @Bean
    fun formRouter() =
        router {
            val index = ClassPathResource("static/form.html")
            val extensions = listOf("js", "css", "wav", "png")
            val spaPredicate = !(path("/score") or
                    path("/physics") or
                    pathExtension(extensions::contains)) and
                    path("/form.html")
            resource(spaPredicate, index)
        }

    @Bean
    fun jsRouter() =
        router {
            val index = ClassPathResource("static/main.js")
            val extensions = listOf("css", "wav", "png")
            val spaPredicate = !(path("/score") or
                    path("/physics") or
                    pathExtension(extensions::contains) or
                    path("/form.js"))
            resource(spaPredicate, index)
        }

    @Bean
    fun formJsRouter() =
        router {
            val index = ClassPathResource("static/form.js")
            val extensions = listOf("css", "wav", "png")
            val spaPredicate = !(path("/score") or
                    path("/physics") or
                    pathExtension(extensions::contains)) and
                    path("/form.js")
            resource(spaPredicate, index)
        }

    @Bean
    fun cssRouter() =
        router {
            val index = ClassPathResource("static/main.css")
            val extensions = listOf("js", "wav", "png")
            val spaPredicate = !(path("/score") or
                    path("/physics") or
                    pathExtension(extensions::contains) or
                    path("/form.css"))
            resource(spaPredicate, index)
        }

    @Bean
    fun formCssRouter() =
        router {
            val index = ClassPathResource("static/form.css")
            val extensions = listOf("js", "wav", "png")
            val spaPredicate = !(path("/score") or
                    path("/physics") or
                    pathExtension(extensions::contains)) and
                    path("/form.css")
            resource(spaPredicate, index)
        }

    @Bean
    fun wavRouter() =
        router {
            val index = ClassPathResource("game/level-1-music.wav")
            val extensions = listOf("js", "css", "html", "png")
            val spaPredicate = !(path("/score") or
                    path("/physics") or
                    pathExtension(extensions::contains))
            resource(spaPredicate, index)
        }

    @Bean
    fun pngRouter() =
        router {
            val index = ClassPathResource("static/splash.png")
            val extensions = listOf("js", "css", "html", "wav")
            val spaPredicate = !(path("/score") or
                    path("/physics") or
                    pathExtension(extensions::contains))
            resource(spaPredicate, index)
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