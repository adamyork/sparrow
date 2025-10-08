package com.github.adamyork.socketgame.game

import com.github.adamyork.socketgame.common.ControlAction
import com.github.adamyork.socketgame.common.ControlType
import com.github.adamyork.socketgame.common.GameStatusProvider
import com.github.adamyork.socketgame.game.data.Direction
import com.github.adamyork.socketgame.game.data.GameMap
import com.github.adamyork.socketgame.game.data.Player
import com.github.adamyork.socketgame.game.engine.Engine
import com.github.adamyork.socketgame.game.service.AssetService
import com.github.adamyork.socketgame.game.service.ScoreService
import com.github.adamyork.socketgame.game.service.data.Asset
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.Duration
import java.util.function.Function
import java.util.function.Supplier
import kotlin.concurrent.atomics.ExperimentalAtomicApi


class Game {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(Game::class.java)
        const val VIEWPORT_WIDTH: Int = 1024
        const val VIEWPORT_HEIGHT: Int = 768
    }

    val gameWebSocketSession: WebSocketSession
    val assetService: AssetService
    val engine: Engine
    val scoreService: ScoreService
    val gameStatusProvider: GameStatusProvider
    var isInitialized: Boolean = false


    lateinit var player: Player
    lateinit var gameMap: GameMap
    lateinit var playerAsset: Asset
    lateinit var mapItemAsset: Asset
    lateinit var finishItemAsset: Asset
    lateinit var mapEnemyAsset: Asset

    constructor(
        webSocketSession: WebSocketSession,
        assetService: AssetService,
        engine: Engine,
        scoreService: ScoreService,
        gameStatusProvider: GameStatusProvider
    ) {
        this.gameWebSocketSession = webSocketSession
        this.assetService = assetService
        this.engine = engine
        this.scoreService = scoreService
        this.gameStatusProvider = gameStatusProvider
    }

    fun init(): Mono<Boolean> {
        return Mono.zip(
            assetService.loadMap(0),
            assetService.loadPlayer(),
            assetService.loadItem(0),
            assetService.loadItem(1),
            assetService.loadEnemy(0)
        ).map { objects ->
            gameMap = objects.t1
            playerAsset = objects.t2
            mapItemAsset = objects.t3
            finishItemAsset = objects.t4
            mapEnemyAsset = objects.t5
            player = Player(400, 100, playerAsset.width, playerAsset.height)
            gameMap.generateMapItems(mapItemAsset, finishItemAsset, assetService)
            gameMap.generateMapEnemies(mapEnemyAsset, assetService)
            isInitialized = true
            LOGGER.info("assets Loaded")
            true
        }.onErrorReturn(false)
    }

    @OptIn(ExperimentalAtomicApi::class)
    fun start(): Mono<Boolean> {
        return Flux.interval(Duration.ofMillis(80))
            .publishOn(Schedulers.boundedElastic())
            .onBackpressureDrop()
            .concatMap(Function { _: Long? ->
                Mono.defer(Supplier {
                    if (gameStatusProvider.running.load()) {
                        val previousX = player.x
                        val previousY = player.y
                        player = engine.managePlayer(player, gameMap, gameMap.collisionAsset)
                        gameMap = engine.manageMap(player, gameMap)
                        scoreService.gameMapItem = gameMap.items
                        val collisionResult =
                            engine.manageCollision(player, previousX, previousY, gameMap, gameMap.collisionAsset)
                        player = collisionResult.t1
                        gameMap = collisionResult.t2
                        val bytes: ByteArray =
                            engine.paint(gameMap, playerAsset, player, mapItemAsset, finishItemAsset, mapEnemyAsset)
                        val binaryMessage = gameWebSocketSession.binaryMessage { session -> session.wrap(bytes) }
                        val messages: List<WebSocketMessage> = listOf(binaryMessage)
                        val messageFlux: Flux<WebSocketMessage> = Flux.fromIterable(messages)
                        gameWebSocketSession.send(messageFlux)
                    } else {
                        Mono.just(false).then()
                    }
                })
            }, 0)
            .collectList()
            .map { _ -> true }
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
