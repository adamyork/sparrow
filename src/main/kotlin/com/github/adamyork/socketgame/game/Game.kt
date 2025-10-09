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
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.function.Function
import kotlin.concurrent.atomics.ExperimentalAtomicApi


class Game {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(Game::class.java)
        const val VIEWPORT_WIDTH: Int = 1024
        const val VIEWPORT_HEIGHT: Int = 768
    }

    val assetService: AssetService
    val engine: Engine
    val scoreService: ScoreService
    val gameStatusProvider: GameStatusProvider
    var isInitialized: Boolean = false
    val playerInitialX: Int
    val playerInitialY: Int


    lateinit var player: Player
    lateinit var gameMap: GameMap
    lateinit var playerAsset: Asset
    lateinit var mapItemAsset: Asset
    lateinit var finishItemAsset: Asset
    lateinit var mapEnemyAsset: Asset

    constructor(
        assetService: AssetService,
        engine: Engine,
        scoreService: ScoreService,
        gameStatusProvider: GameStatusProvider,
        playerInitialX: Int,
        playerInitialY: Int
    ) {
        this.assetService = assetService
        this.engine = engine
        this.scoreService = scoreService
        this.gameStatusProvider = gameStatusProvider
        this.playerInitialX = playerInitialX
        this.playerInitialY = playerInitialY
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
            player = Player(playerInitialX, playerInitialY, playerAsset.width, playerAsset.height)
            gameMap.generateMapItems(mapItemAsset, finishItemAsset, assetService)
            gameMap.generateMapEnemies(mapEnemyAsset, assetService)
            isInitialized = true
            LOGGER.info("assets Loaded")
            true
        }.onErrorReturn(false)
    }

    @OptIn(ExperimentalAtomicApi::class)
    fun next(): Mono<ByteArray> {
        return Flux.fromIterable(listOf(1))
            .collectList()
            .map(Function { _ ->
                if (gameStatusProvider.running.load()) {
                    val previousX = player.x
                    val previousY = player.y
                    player = engine.managePlayer(player)
                    gameMap = engine.manageMap(player, gameMap)
                    scoreService.gameMapItem = gameMap.items
                    val collisionResult =
                        engine.manageCollision(player, previousX, previousY, gameMap, gameMap.collisionAsset)
                    player = collisionResult.player
                    gameMap = collisionResult.map
                    engine.paint(gameMap, playerAsset, player, mapItemAsset, finishItemAsset, mapEnemyAsset)
                } else {
                    ByteArray(0)
                }
            })
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
                    LOGGER.debug("player is not jumping start a jump")
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
