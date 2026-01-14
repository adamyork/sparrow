package com.github.adamyork.sparrow.socket

import com.github.adamyork.sparrow.common.AudioQueue
import com.github.adamyork.sparrow.common.StatusProvider
import com.github.adamyork.sparrow.game.Game
import com.github.adamyork.sparrow.game.GameAudio
import com.github.adamyork.sparrow.game.engine.Engine
import com.github.adamyork.sparrow.game.service.AssetService
import com.github.adamyork.sparrow.game.service.ScoreService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.server.WebSocketService
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy


@Configuration
class SocketConfig {

    @Bean
    fun webSocketMessageBuilder(): WebSocketMessageBuilder {
        val messageBuilder = object : WebSocketMessageBuilder {}
        return messageBuilder
    }


    @Bean
    fun handlerMapping(
        assetService: AssetService,
        engine: Engine,
        scoreService: ScoreService,
        audioQueue: AudioQueue,
        statusProvider: StatusProvider,
        webSocketMessageBuilder: WebSocketMessageBuilder,
        @Value("\${player.x}") playerInitialX: Int,
        @Value("\${player.y}") playerInitialY: Int,
        @Value("\${viewport.x}") viewPortInitialX: Int,
        @Value("\${viewport.y}") viewPortInitialY: Int,
        @Value("\${viewport.width}") viewPortWidth: Int,
        @Value("\${viewport.height}") viewPortHeight: Int,
        @Value("\${engine.fps.max}") fpsMax: Int,
    ): HandlerMapping {
        val handlers: MutableMap<String?, WebSocketHandler?> = HashMap()
        val game = Game(
            assetService,
            engine,
            scoreService,
            statusProvider,
            playerInitialX,
            playerInitialY,
            viewPortInitialX,
            viewPortInitialY,
            viewPortWidth,
            viewPortHeight,
            fpsMax
        )
        val gameAudio = GameAudio(assetService, audioQueue)
        handlers["/game"] =
            GameHandler(game, assetService, engine, scoreService, statusProvider, webSocketMessageBuilder)
        handlers["/input"] = InputHandler(game)
        handlers["/input-audio"] = InputAudioHandler(assetService, statusProvider, webSocketMessageBuilder)
        handlers["/game-audio"] = EnvironmentAudioHandler(gameAudio, assetService, audioQueue, webSocketMessageBuilder)
        handlers["/background-audio"] = BackgroundAudioHandler(assetService, statusProvider, webSocketMessageBuilder)
        val mapping = SimpleUrlHandlerMapping()
        mapping.urlMap = handlers
        mapping.order = Ordered.HIGHEST_PRECEDENCE
        return mapping
    }

    @Bean
    fun handlerAdapter(): WebSocketHandlerAdapter {
        return WebSocketHandlerAdapter(webSocketService())
    }

    @Bean
    fun webSocketService(): WebSocketService {
        val strategy = ReactorNettyRequestUpgradeStrategy()
        return HandshakeWebSocketService(strategy)
    }

}