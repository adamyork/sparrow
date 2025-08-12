package com.github.adamyork.socketgame.socket

import com.github.adamyork.socketgame.engine.Engine
import com.github.adamyork.socketgame.game.AssetService
import com.github.adamyork.socketgame.game.data.ControlAction
import com.github.adamyork.socketgame.game.data.ControlType
import com.github.adamyork.socketgame.game.Game
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers


class GameHandler : WebSocketHandler {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(GameHandler::class.java)
        const val INPUT_TYPE: String = "START"
        const val INPUT_KEY_STATE = "keydown"
        const val INPUT_KEY_RIGHT: String = "ArrowRight"
        const val INPUT_KEY_LEFT: String = "ArrowLeft"
        const val INPUT_KEY_JUMP: String = "Space"
    }

    val assetService: AssetService
    val engine: Engine
    var game: Game? = null

    constructor(
        assetService: AssetService,
        engine: Engine
    ) {
        this.assetService = assetService
        this.engine = engine
    }

    override fun handle(session: WebSocketSession): Mono<Void?> {
        val map = session.receive()
            .doOnNext { message -> message.retain() }
            .publishOn(Schedulers.boundedElastic())
            .map { message ->
                val payloadAsText = message.payloadAsText
                if (payloadAsText == INPUT_TYPE) {
                    game = Game(session, assetService, engine)
                    game?.init()
                    game?.start()
                } else {
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

                session.textMessage("Message Received: $payloadAsText")
            }
        return session.send(map)
    }

    private fun handleNoGameCreated() {
        LOGGER.debug("No Game Created Yet")
    }

}