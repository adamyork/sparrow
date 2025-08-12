package com.github.adamyork.socketgame.game

import com.github.adamyork.socketgame.engine.Engine
import com.github.adamyork.socketgame.game.data.ControlAction
import com.github.adamyork.socketgame.game.data.ControlType
import com.github.adamyork.socketgame.game.data.Direction
import com.github.adamyork.socketgame.game.data.Player
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.Disposable
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.Duration
import java.util.function.Function
import java.util.function.Supplier


class Game {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(Game::class.java)
    }

    val webSocketSession: WebSocketSession
    val assetService: AssetService
    val engine: Engine

    lateinit var player: Player

    constructor(
        webSocketSession: WebSocketSession,
        assetService: AssetService,
        engine: Engine
    ) {
        this.webSocketSession = webSocketSession
        this.assetService = assetService
        this.engine = engine
    }

    fun init() {
        val floor = assetService.backgroundAsset.height - assetService.playerAsset.height
        player = Player(0, floor, floor)
    }

    fun start(): Disposable {
        return Flux.interval(Duration.ofMillis(80))
            .publishOn(Schedulers.boundedElastic())
            .onBackpressureDrop()
            .concatMap(Function { foo: Long? ->
                Mono.defer(Supplier {
                    player = engine.tick(
                        player, assetService.backgroundAsset,
                        assetService.collisionAsset
                    )
                    val bytes: ByteArray = engine.paint(
                        assetService.backgroundAsset.bufferedImage,
                        assetService.playerAsset.bufferedImage,
                        assetService.collisionAsset.bufferedImage,
                        player
                    )
                    val binaryMessage = webSocketSession.binaryMessage { session -> session.wrap(bytes) }
                    val messages: List<WebSocketMessage> = listOf(binaryMessage)
                    val messageFlux: Flux<WebSocketMessage> = Flux.fromIterable(messages)
                    webSocketSession.send(messageFlux)
                    //Mono.just("completed")
                })
            }, 0)
            .subscribe()
    }

    fun applyInput(controlType: ControlType, controlAction: ControlAction) {
        when (controlType) {
            ControlType.START -> startInput(controlAction)
            ControlType.STOP -> stopInput(controlAction)
        }
    }

    private fun startInput(controlAction: ControlAction) {
        LOGGER.debug("start {}", controlAction)
        when (controlAction) {
            ControlAction.LEFT -> player.setPlayerState(
                true,
                jumping = player.jumping,
                direction = Direction.LEFT
            )

            ControlAction.RIGHT -> player.setPlayerState(
                true,
                jumping = player.jumping,
                direction = Direction.RIGHT
            )

            ControlAction.JUMP -> {
                if (!player.jumping) {
                    LOGGER.debug("player is not jumping start a jump player.vy: ${player.vy}")
                    player.setPlayerState(
                        player.moving,
                        jumping = true,
                        direction = player.direction,
                    )
                }
            }
        }
    }

    private fun stopInput(controlAction: ControlAction) {
        LOGGER.debug("stop {}", controlAction)
        when (controlAction) {
            ControlAction.LEFT -> player.setPlayerState(
                false,
                jumping = player.jumping,
                direction = Direction.LEFT
            )

            ControlAction.RIGHT -> player.setPlayerState(
                false,
                jumping = player.jumping,
                direction = Direction.RIGHT
            )

            ControlAction.JUMP -> player.setPlayerState(
                player.moving,
                jumping = player.jumping,
                direction = player.direction
            )
        }
    }

}