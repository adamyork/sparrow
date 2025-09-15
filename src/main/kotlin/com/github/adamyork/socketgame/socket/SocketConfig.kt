package com.github.adamyork.socketgame.socket

import com.github.adamyork.socketgame.common.AudioQueue
import com.github.adamyork.socketgame.game.engine.Engine
import com.github.adamyork.socketgame.game.service.AssetService
import com.github.adamyork.socketgame.game.service.ScoreService
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
    fun handlerMapping(
        assetService: AssetService,
        engine: Engine,
        scoreService: ScoreService,
        audioQueue: AudioQueue
    ): HandlerMapping {
        val map: MutableMap<String?, WebSocketHandler?> = HashMap()
        map["/game"] = GameHandler(assetService, engine, scoreService)
        map["/input-audio"] = InputAudioHandler(assetService)
        map["/game-audio"] = GameAudioHandler(assetService, audioQueue)
        val mapping = SimpleUrlHandlerMapping()
        mapping.urlMap = map
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