package com.github.adamyork.sparrow.game

import com.github.adamyork.sparrow.common.ControlAction
import com.github.adamyork.sparrow.common.ControlType
import com.github.adamyork.sparrow.common.GameStatusProvider
import com.github.adamyork.sparrow.game.data.*
import com.github.adamyork.sparrow.game.data.map.GameMap
import com.github.adamyork.sparrow.game.data.player.Player
import com.github.adamyork.sparrow.game.data.player.PlayerJumpingState
import com.github.adamyork.sparrow.game.data.player.PlayerMovingState
import com.github.adamyork.sparrow.game.engine.Engine
import com.github.adamyork.sparrow.game.service.AssetService
import com.github.adamyork.sparrow.game.service.ScoreService
import com.github.adamyork.sparrow.game.service.data.ImageAsset
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
    val fpsMax: Int


    lateinit var viewPort: ViewPort
    lateinit var player: Player
    lateinit var gameMap: GameMap
    lateinit var playerAsset: ImageAsset
    lateinit var mapItemCollectibleAsset: ImageAsset
    lateinit var mapItemFinishAsset: ImageAsset
    lateinit var mapEnemyBlockerAsset: ImageAsset
    lateinit var mapEnemyShooterAsset: ImageAsset

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
        fpsMax: Int,
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
        this.fpsMax = fpsMax
    }

    fun init(): Mono<Boolean> {
        return Mono.zip(
            assetService.loadMap(0),
            assetService.loadPlayer(),
            assetService.loadItem(0),
            assetService.loadItem(1),
            assetService.loadEnemy(0),
            assetService.loadEnemy(1)
        ).map { objects ->
            LOGGER.info("assets Loaded")
            viewPort = ViewPort(viewPortInitialX, viewPortInitialY, 0, 0, viewPortWidth, viewPortHeight)
            gameMap = objects.t1
            playerAsset = objects.t2
            mapItemCollectibleAsset = objects.t3
            mapItemFinishAsset = objects.t4
            mapEnemyBlockerAsset = objects.t5
            mapEnemyShooterAsset = objects.t6
            player = Player(
                playerInitialX,
                playerInitialY,
                playerAsset.width,
                playerAsset.height,
                GameElementState.ACTIVE,
                FrameMetadata(1, Cell(1, 1, playerAsset.width, playerAsset.height)),
                playerAsset.bufferedImage,
                0.0,
                0.0,
                PlayerJumpingState.GROUNDED,
                PlayerMovingState.STATIONARY,
                Direction.RIGHT,
                GameElementCollisionState.FREE
            )
            gameMap.generateMapItems(mapItemCollectibleAsset, mapItemFinishAsset, assetService)
            LOGGER.info("map items generated")
            gameMap.generateMapEnemies(mapEnemyBlockerAsset, mapEnemyShooterAsset, assetService)
            LOGGER.info("enemy items generated")
            engine.setCollisionBufferedImage(gameMap.collisionAsset)
            scoreService.gameMapItem = gameMap.items
            isInitialized = true
            true
        }.onErrorResume { throwable ->
            LOGGER.error("Error initializing game ${throwable.message}")
            LOGGER.error(throwable.stackTraceToString())
            Mono.just(false)
        }
    }

    @OptIn(ExperimentalAtomicApi::class)
    fun next(): Mono<ByteArray> {
        return Flux.fromIterable(listOf(1))
            .collectList()
            .map(Function { _ ->
                if (gameStatusProvider.running.load()) {
                    val lastPaintMs = gameStatusProvider.lastPaintTime.load()
                    val nextPaintTimeMs = System.currentTimeMillis()
                    val deltaTime = nextPaintTimeMs - lastPaintMs
                    val fpsMaxDeltaTimeMs = 1000 / fpsMax
                    if (deltaTime < fpsMaxDeltaTimeMs) {
                        ByteArray(0)
                    } else {
                        val collisionBoundaries = engine.getCollisionBoundaries(player)
                        player = engine.managePlayer(player, collisionBoundaries)
                        viewPort = engine.manageViewport(player, viewPort)
                        gameMap = engine.manageMap(player, gameMap)
                        val nextPlayerAndMap = engine.manageEnemyAndItemCollision(player, gameMap, viewPort)
                        player = nextPlayerAndMap.first
                        gameMap = nextPlayerAndMap.second
                        scoreService.gameMapItem = gameMap.items
                        gameStatusProvider.lastPaintTime.store(nextPaintTimeMs)
                        engine.draw(gameMap, viewPort, player)
                    }
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
        player = Player(
            playerInitialX,
            playerInitialY,
            playerAsset.width,
            playerAsset.height,
            GameElementState.ACTIVE,
            FrameMetadata(1, Cell(1, 1, playerAsset.width, playerAsset.height)),
            playerAsset.bufferedImage,
            0.0,
            0.0,
            PlayerJumpingState.GROUNDED,
            PlayerMovingState.STATIONARY,
            Direction.RIGHT,
            GameElementCollisionState.FREE
        )
        gameMap.reset(
            mapItemCollectibleAsset,
            mapItemFinishAsset,
            mapEnemyBlockerAsset,
            mapEnemyShooterAsset,
            assetService
        )
        viewPort = ViewPort(viewPortInitialX, viewPortInitialY, 0, 0, viewPortWidth, viewPortHeight)
        scoreService.gameMapItem = gameMap.items
        gameStatusProvider.reset()
    }

    private fun startInput(controlAction: ControlAction) {
        when (controlAction) {
            ControlAction.LEFT -> {
                val nextVx = adjustXVelocity(controlAction)
                player = player.copy(moving = PlayerMovingState.MOVING, direction = Direction.LEFT, vx = nextVx)
            }

            ControlAction.RIGHT -> {
                val nextVx = adjustXVelocity(controlAction)
                player = player.copy(moving = PlayerMovingState.MOVING, direction = Direction.RIGHT, vx = nextVx)
            }

            ControlAction.JUMP -> {
                if (player.jumping == PlayerJumpingState.GROUNDED) {
                    LOGGER.info("starting player jump")
                    player = player.copy(jumping = PlayerJumpingState.INITIAL)
                }
            }
        }
    }

    private fun adjustXVelocity(controlAction: ControlAction): Double {
        if (controlAction == ControlAction.LEFT) {
            if (player.direction == Direction.RIGHT) {
                LOGGER.info(getDirectionChangedLogMessage())
                return 0.0
            } else {
                return player.vx
            }
        } else {
            if (player.direction == Direction.LEFT) {
                LOGGER.info(getDirectionChangedLogMessage())
                return 0.0
            } else {
                return player.vx
            }
        }
    }

    private fun getDirectionChangedLogMessage(): String {
        return "direction changed player vx was: ${player.vx} and is now 0"
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
                player = player.copy(moving = PlayerMovingState.STATIONARY, direction = Direction.RIGHT)
            }
        } else if (controlAction == ControlAction.LEFT) {
            if (player.direction == Direction.LEFT) {
                player = player.copy(moving = PlayerMovingState.STATIONARY, direction = Direction.LEFT)
            }
        }
    }

}
