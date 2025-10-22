package com.github.adamyork.sparrow.game

import com.github.adamyork.sparrow.common.ControlAction
import com.github.adamyork.sparrow.common.ControlType
import com.github.adamyork.sparrow.common.GameStatusProvider
import com.github.adamyork.sparrow.game.data.Direction
import com.github.adamyork.sparrow.game.data.GameMap
import com.github.adamyork.sparrow.game.data.Player
import com.github.adamyork.sparrow.game.data.ViewPort
import com.github.adamyork.sparrow.game.engine.Engine
import com.github.adamyork.sparrow.game.service.AssetService
import com.github.adamyork.sparrow.game.service.ScoreService
import com.github.adamyork.sparrow.game.service.data.Asset
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.function.Function
import kotlin.concurrent.atomics.ExperimentalAtomicApi


class Game {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(Game::class.java)
    }

    val assetService: AssetService
    val engine: Engine
    val scoreService: ScoreService
    val gameStatusProvider: GameStatusProvider
    var isInitialized: Boolean = false
    val playerInitialX: Int
    val playerInitialY: Int
    val viewPortInitialX: Int
    val viewPortInitialY: Int
    val viewPortWidth: Int
    val viewPortHeight: Int


    lateinit var viewPort: ViewPort
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
        playerInitialY: Int,
        viewPortInitialX: Int,
        viewPortInitialY: Int,
        viewPortWidth: Int,
        viewPortHeight: Int,
    ) {
        this.assetService = assetService
        this.engine = engine
        this.scoreService = scoreService
        this.gameStatusProvider = gameStatusProvider
        this.playerInitialX = playerInitialX
        this.playerInitialY = playerInitialY
        this.viewPortInitialX = viewPortInitialX
        this.viewPortInitialY = viewPortInitialY
        this.viewPortWidth = viewPortWidth
        this.viewPortHeight = viewPortHeight
    }

    fun init(): Mono<Boolean> {
        return Mono.zip(
            assetService.loadMap(0),
            assetService.loadPlayer(),
            assetService.loadItem(0),
            assetService.loadItem(1),
            assetService.loadEnemy(0)
        ).map { objects ->
            viewPort = ViewPort(viewPortInitialX, viewPortInitialY, 0, 0, viewPortWidth, viewPortHeight)
            gameMap = objects.t1
            playerAsset = objects.t2
            mapItemAsset = objects.t3
            finishItemAsset = objects.t4
            mapEnemyAsset = objects.t5
            player = Player(playerInitialX, playerInitialY, playerAsset.width, playerAsset.height)
            gameMap.generateMapItems(mapItemAsset, finishItemAsset, assetService)
            gameMap.generateMapEnemies(mapEnemyAsset, assetService)
            engine.setCollisionBufferedImage(gameMap.collisionAsset)
            scoreService.gameMapItem = gameMap.items
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
                    val collisionBoundaries = engine.getCollisionBoundaries(player, gameMap.collisionAsset)
                    player = engine.managePlayer(player, collisionBoundaries)
                    viewPort = engine.manageViewport(player, viewPort)
                    gameMap = engine.manageMap(player, gameMap)
                    val nextPlayerAndMap =
                        engine.manageEnemyAndItemCollision(player, gameMap, viewPort, gameMap.collisionAsset)
                    player = nextPlayerAndMap.player
                    gameMap = nextPlayerAndMap.map
                    scoreService.gameMapItem = gameMap.items
                    gameStatusProvider.lastPaintTime.store(System.currentTimeMillis().toInt())
                    engine.paint(gameMap, viewPort, playerAsset, player, mapItemAsset, finishItemAsset, mapEnemyAsset)
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

    fun reset() {
        LOGGER.info("reset game")
        player.reset(playerInitialX, playerInitialY)
        gameMap.reset(mapItemAsset, finishItemAsset, mapEnemyAsset, assetService)
        viewPort = ViewPort(viewPortInitialX, viewPortInitialY, 0, 0, viewPortWidth, viewPortHeight)
        scoreService.gameMapItem = gameMap.items
    }

    private fun startInput(controlAction: ControlAction) {
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
                    LOGGER.info("starting player jump")
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
        if (controlAction == ControlAction.LEFT && player.direction == Direction.RIGHT) {
            LOGGER.warn("stop player left called before right started")
        }
        if (controlAction == ControlAction.RIGHT && player.direction == Direction.LEFT) {
            LOGGER.warn("stop player right called before left started")
        }
        if (controlAction == ControlAction.RIGHT) {
            if (player.direction == Direction.RIGHT) {
                player.setPlayerState(
                    false,
                    jumping = player.jumping,
                    direction = Direction.RIGHT
                )
            }
        } else if (controlAction == ControlAction.LEFT) {
            if (player.direction == Direction.LEFT) {
                player.setPlayerState(
                    false,
                    jumping = player.jumping,
                    direction = Direction.LEFT
                )
            }
        } else {
            player.setPlayerState(
                player.moving,
                jumping = player.jumping,
                direction = player.direction
            )
        }
    }

}
