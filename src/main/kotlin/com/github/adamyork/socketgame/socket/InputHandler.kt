package com.github.adamyork.socketgame.socket

import com.github.adamyork.socketgame.common.ControlAction
import com.github.adamyork.socketgame.common.ControlType
import com.github.adamyork.socketgame.game.Game
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import kotlin.system.exitProcess

class InputHandler : WebSocketHandler {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(InputHandler::class.java)
        const val INPUT_KEY_STATE = "keydown"
        const val INPUT_KEY_RIGHT: String = "ArrowRight"
        const val INPUT_KEY_LEFT: String = "ArrowLeft"
        const val INPUT_KEY_JUMP: String = "Space"
    }

    val game: Game

    constructor(game: Game) {
        this.game = game
    }

    override fun handle(session: WebSocketSession): Mono<Void?> {
        val map = session.receive()
            .map { message ->
                val payloadAsText = message.payloadAsText
                val controlCodes = payloadAsText.split(":")
                val type = controlCodes[0]
                val input = controlCodes[1]
                val action = if (type == INPUT_KEY_STATE) ControlType.START else ControlType.STOP
                when (input) {
                    INPUT_KEY_RIGHT -> {
                        game.applyInput(action, ControlAction.RIGHT)
                    }

                    INPUT_KEY_LEFT -> {
                        game.applyInput(action, ControlAction.LEFT)
                    }

                    INPUT_KEY_JUMP -> {
                        LOGGER.debug("jump key received")
                        game.applyInput(action, ControlAction.JUMP)
                    }
                }
                session.textMessage("Message Received: $payloadAsText")
            }
            .doOnError { _ ->
                exitProcess(0)
            }
        return session.send(map)
    }
}