package com.github.adamyork.socketgame.socket

import com.github.adamyork.socketgame.common.GameStatusProvider
import com.github.adamyork.socketgame.game.Game
import com.github.adamyork.socketgame.game.engine.Engine
import com.github.adamyork.socketgame.game.service.AssetService
import com.github.adamyork.socketgame.game.service.ScoreService
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
    val gameStatusProvider: GameStatusProvider

    val game: Game

    constructor(
        game: Game,
        assetService: AssetService,
        engine: Engine,
        scoreService: ScoreService,
        gameStatusProvider: GameStatusProvider
    ) {
        this.game = game
        this.assetService = assetService
        this.engine = engine
        this.scoreService = scoreService
        this.gameStatusProvider = gameStatusProvider
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
                            gameStatusProvider.lastPaintTime.store(System.currentTimeMillis().toInt())
                        }
                        LOGGER.info("game started")
                    }

                    INPUT_PAUSE -> {
                        LOGGER.info("game paused")
                        gameStatusProvider.running.store(false)
                    }

                    INPUT_RESUME -> {
                        LOGGER.info("game resumed")
                        gameStatusProvider.running.store(true)
                    }
                }
                if (!game.isInitialized) {
                    game.init().flatMap { initialized ->
                        if (initialized) {
                            gameStatusProvider.running.store(true)
                            game.next()
                                .map { bytes ->
                                    session.binaryMessage { session -> session.wrap(bytes) }
                                }
                        } else {
                            LOGGER.error("Game not initialized")
                            Mono.error(RuntimeException("Game cannot be initialized"))
                        }
                    }
                } else {
                    game.next()
                        .map { bytes ->
                            session.binaryMessage { session -> session.wrap(bytes) }
                        }
                }
            }
            .doOnError { error ->
                LOGGER.error("Game error", error)
                exitProcess(0)
            }
        return session.send(map)
    }

}