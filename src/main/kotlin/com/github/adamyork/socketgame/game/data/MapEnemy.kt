package com.github.adamyork.socketgame.game.data

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.util.function.Tuples

class MapEnemy {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(MapItem::class.java)
        const val ANIMATION_DEACTIVATING_FRAMES = 5
        const val MAX_X_MOVEMENT = 10
        const val MOVEMENT_X_DISTANCE = 10
    }

    var width: Int = 64//TODO Magic Number
    var height: Int = 64//TODO Magic Number
    var x: Int = 0
    var y: Int = 0
    var originX: Int = 0
    var originY: Int = 0
    var state: MapItemState
    var animatingsFrames: HashMap<Int, FrameMetadata> = HashMap()
    var frameMetadata: FrameMetadata = FrameMetadata(1, Tuples.of(0, 0))

    lateinit var enemyPosition: EnemyPosition

    constructor(width: Int, height: Int, x: Int, y: Int, state: MapItemState) {
        this.width = width
        this.height = height
        this.x = x
        this.y = y
        this.originX = x
        this.originY = y
        this.state = state
        this.enemyPosition = EnemyPosition(this.x, this.y, Direction.LEFT)
        generateAnimationFrameIndex()
    }

    constructor(
        width: Int,
        height: Int,
        x: Int,
        y: Int,
        originX: Int,
        originY: Int,
        state: MapItemState,
        frameMetadata: FrameMetadata,
        enemyPosition: EnemyPosition
    ) {
        this.width = width
        this.height = height
        this.x = x
        this.y = y
        this.originX = originX
        this.originY = originY
        this.state = state
        this.frameMetadata = frameMetadata
        this.enemyPosition = enemyPosition
        generateAnimationFrameIndex()
    }

    fun getNextFrameCell(): FrameMetadata {
//        if (state == MapItemState.DEACTIVATING) {
//            if (frameMetadata.frame == ANIMATION_DEACTIVATING_FRAMES) {
//                return FrameMetadata(0, Tuples.of(0, 0))
//            } else {
//                val nextFrame = frameMetadata.frame + 1
//                return animatingsFrames.get(nextFrame) ?: FrameMetadata(1, Tuples.of(0, 0))
//            }
//        }
        return FrameMetadata(1, Tuples.of(0, 0))
    }

    fun getNextPosition(xDelta: Int, yDelta: Int): EnemyPosition {
        if (enemyPosition.direction == Direction.LEFT) {
            if (enemyPosition.x >= originX - MAX_X_MOVEMENT) {
                return EnemyPosition(
                    enemyPosition.x - MOVEMENT_X_DISTANCE - xDelta,
                    enemyPosition.y + yDelta,
                    Direction.LEFT
                )
            } else {
                return EnemyPosition(enemyPosition.x, enemyPosition.y + yDelta, Direction.RIGHT)
            }
        } else {
            if (enemyPosition.x <= originX + MAX_X_MOVEMENT) {
                return EnemyPosition(
                    enemyPosition.x + MOVEMENT_X_DISTANCE - xDelta,
                    enemyPosition.y + yDelta,
                    Direction.RIGHT
                )
            } else {
                return EnemyPosition(enemyPosition.x, enemyPosition.y + yDelta, Direction.LEFT)
            }
        }
    }

    private fun generateAnimationFrameIndex() {
        animatingsFrames[1] = FrameMetadata(1, Tuples.of(0, 0))
    }
}