package com.github.adamyork.sparrow.socket

import com.github.adamyork.sparrow.common.StatusProvider
import com.github.adamyork.sparrow.game.Game
import com.github.adamyork.sparrow.game.engine.Engine
import com.github.adamyork.sparrow.game.service.AssetService
import com.github.adamyork.sparrow.game.service.ScoreService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.system.exitProcess


class GameHandler : WebSocketHandler {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(GameHandler::class.java)
        const val INPUT_START: String = "START"
        const val INPUT_PAUSE: String = "PAUSE"
        const val INPUT_RESUME: String = "RESUME"
    }

    val assetService: AssetService
    val engine: Engine
    val scoreService: ScoreService
    val statusProvider: StatusProvider
    val webSocketMessageBuilder: WebSocketMessageBuilder

    val game: Game

    constructor(
        game: Game,
        assetService: AssetService,
        engine: Engine,
        scoreService: ScoreService,
        statusProvider: StatusProvider,
        webSocketMessageBuilder: WebSocketMessageBuilder
    ) {
        this.game = game
        this.assetService = assetService
        this.engine = engine
        this.scoreService = scoreService
        this.statusProvider = statusProvider
        this.webSocketMessageBuilder = webSocketMessageBuilder
    }

    @OptIn(ExperimentalAtomicApi::class)
    override fun handle(session: WebSocketSession): Mono<Void> {
        val map = session.receive()
            .flatMap { message ->
                val payloadAsText = message.payloadAsText
                when (payloadAsText) {
                    INPUT_START -> {
                        if (game.isInitialized) {
                            game.reset()
                            LOGGER.info("game reset")
                        } else {
                            LOGGER.info("game started")
                        }
                    }

                    INPUT_PAUSE -> {
                        LOGGER.info("game paused")
                        statusProvider.running.store(false)
                    }

                    INPUT_RESUME -> {
                        LOGGER.info("game resumed")
                        statusProvider.running.store(true)
                    }
                }
                if (!game.isInitialized) {
                    game.init().flatMap { initialized ->
                        if (initialized) {
                            statusProvider.running.store(true)
                            game.next().map { bytes -> webSocketMessageBuilder.build(session, bytes) }
                        } else {
                            LOGGER.error("Game not initialized")
                            Mono.error(RuntimeException("Game cannot be initialized"))
                        }
                    }
                } else {
                    game.next().map { bytes -> webSocketMessageBuilder.build(session, bytes) }
                }
            }
            .doOnError { error ->
                LOGGER.error("Game error", error)
                exitProcess(0)
            }
        return session.send(map)
    }

}