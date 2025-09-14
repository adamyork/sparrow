package com.github.adamyork.socketgame.game

import com.github.adamyork.socketgame.engine.Engine
import com.github.adamyork.socketgame.game.data.*
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
        const val VIEWPORT_WIDTH: Int = 1024
        const val VIEWPORT_HEIGHT: Int = 768
    }

    val gameWebSocketSession: WebSocketSession
    val assetService: AssetService
    val engine: Engine
    var isInitialized: Boolean = false


    lateinit var player: Player
    lateinit var gameMap: GameMap
    lateinit var playerAsset: Asset
    lateinit var mapItemAsset: Asset
    lateinit var mapEnemyAsset: Asset

    constructor(
        webSocketSession: WebSocketSession,
        assetService: AssetService,
        engine: Engine
    ) {
        this.gameWebSocketSession = webSocketSession
        this.assetService = assetService
        this.engine = engine
    }

    fun init() {
        player = Player(400, 100)
        gameMap = assetService.loadMap(0)
        playerAsset = assetService.loadPlayer()
        mapItemAsset = assetService.loadItem(0)
        mapEnemyAsset = assetService.loadEnemy(0)
        gameMap.generateMapItems()
        gameMap.generateMapEnemies()
        isInitialized = true
    }

    fun start(): Disposable {
        return Flux.interval(Duration.ofMillis(80))
            .publishOn(Schedulers.boundedElastic())
            .onBackpressureDrop()
            .concatMap(Function { _: Long? ->
                Mono.defer(Supplier {
                    gameMap = engine.manageMap(player, gameMap)
                    player = engine.managePlayer(player, gameMap, gameMap.collisionAsset)
                    val bytes: ByteArray = engine.paint(gameMap, playerAsset, player, mapItemAsset, mapEnemyAsset)
                    val binaryMessage = gameWebSocketSession.binaryMessage { session -> session.wrap(bytes) }
                    val messages: List<WebSocketMessage> = listOf(binaryMessage)
                    val messageFlux: Flux<WebSocketMessage> = Flux.fromIterable(messages)
                    gameWebSocketSession.send(messageFlux)
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
        //LOGGER.debug("start {}", controlAction)
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
        //LOGGER.debug("stop {}", controlAction)
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
