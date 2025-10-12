package com.github.adamyork.sparrow.socket

import com.github.adamyork.sparrow.common.AudioQueue
import com.github.adamyork.sparrow.common.GameStatusProvider
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
    fun handlerMapping(
        assetService: AssetService,
        engine: Engine,
        scoreService: ScoreService,
        audioQueue: AudioQueue,
        gameStatusProvider: GameStatusProvider,
        @Value("\${player.x}") playerInitialX: Int,
        @Value("\${player.y}") playerInitialY: Int
    ): HandlerMapping {
        val map: MutableMap<String?, WebSocketHandler?> = HashMap()
        val game = Game(assetService, engine, scoreService, gameStatusProvider, playerInitialX, playerInitialY)
        val gameAudio = GameAudio(assetService, audioQueue)
        map["/game"] = GameHandler(game, assetService, engine, scoreService, gameStatusProvider)
        map["/input"] = InputHandler(game)
        map["/input-audio"] = InputAudioHandler(assetService, gameStatusProvider)
        map["/game-audio"] = GameAudioHandler(gameAudio, assetService, audioQueue)
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