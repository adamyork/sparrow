package com.github.adamyork.socketgame.socket

import com.github.adamyork.socketgame.common.ControlAction
import com.github.adamyork.socketgame.common.ControlType
import com.github.adamyork.socketgame.common.GameStatusProvider
import com.github.adamyork.socketgame.game.Game
import com.github.adamyork.socketgame.game.engine.Engine
import com.github.adamyork.socketgame.game.service.AssetService
import com.github.adamyork.socketgame.game.service.ScoreService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.Disposable
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import kotlin.concurrent.atomics.ExperimentalAtomicApi


class GameHandler : WebSocketHandler {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(GameHandler::class.java)
        const val INPUT_START: String = "START"
        const val INPUT_PAUSE: String = "PAUSE"
        const val INPUT_RESUME: String = "RESUME"
        const val INPUT_KEY_STATE = "keydown"
        const val INPUT_KEY_RIGHT: String = "ArrowRight"
        const val INPUT_KEY_LEFT: String = "ArrowLeft"
        const val INPUT_KEY_JUMP: String = "Space"
    }

    val assetService: AssetService
    val engine: Engine
    val scoreService: ScoreService
    val gameStatusProvider: GameStatusProvider
    var game: Game? = null
    var gameRunner: Disposable? = null

    constructor(
        assetService: AssetService,
        engine: Engine,
        scoreService: ScoreService,
        gameStatusProvider: GameStatusProvider
    ) {
        this.assetService = assetService
        this.engine = engine
        this.scoreService = scoreService
        this.gameStatusProvider = gameStatusProvider
    }

    @OptIn(ExperimentalAtomicApi::class)
    override fun handle(session: WebSocketSession): Mono<Void?> {
        val map = session.receive()
            .doOnNext { message -> message.retain() }
            .publishOn(Schedulers.boundedElastic())
            .map { message ->
                val payloadAsText = message.payloadAsText
                when (payloadAsText) {
                    INPUT_START -> {
                        LOGGER.info("Game started")
                        game = Game(session, assetService, engine, scoreService)
                        game?.init()
                        gameRunner = game?.start()
                        gameStatusProvider.running.store(true)
                    }

                    INPUT_PAUSE -> {
                        LOGGER.info("Game paused")
                        gameRunner?.dispose()
                        gameStatusProvider.running.store(false)
                    }

                    INPUT_RESUME -> {
                        LOGGER.info("Game resumed")
                        gameRunner = game?.start()
                        gameStatusProvider.running.store(true)
                    }

                    else -> {
                        val controlCodes = payloadAsText.split(":")
                        val type = controlCodes[0]
                        val input = controlCodes[1]
                        val action = if (type == INPUT_KEY_STATE) ControlType.START else ControlType.STOP
                        when (input) {
                            INPUT_KEY_RIGHT -> {
                                game?.applyInput(action, ControlAction.RIGHT) ?: handleNoGameCreated()
                            }

                            INPUT_KEY_LEFT -> {
                                game?.applyInput(action, ControlAction.LEFT) ?: handleNoGameCreated()
                            }

                            INPUT_KEY_JUMP -> {
                                LOGGER.debug("jump key received")
                                game?.applyInput(action, ControlAction.JUMP) ?: handleNoGameCreated()
                            }
                        }
                    }
                }
                session.textMessage("Message Received: $payloadAsText")
            }
        return session.send(map)
    }

    private fun handleNoGameCreated() {
        LOGGER.debug("No Game Created Yet")
    }

}