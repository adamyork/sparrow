package com.github.adamyork.socketgame.game.data

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MapEnemy {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(MapEnemy::class.java)
        const val ANIMATION_COLLISION_FRAMES = 8
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
    var animatingFrames: HashMap<Int, FrameMetadata> = HashMap()
    var collisionFrames: HashMap<Int, FrameMetadata> = HashMap()
    var frameMetadata: FrameMetadata = FrameMetadata(1, Cell(1, 1, 128, 128))
    var enemyPosition: EnemyPosition
    var colliding: Boolean

    constructor(width: Int, height: Int, x: Int, y: Int, state: MapItemState) {
        this.width = width
        this.height = height
        this.x = x
        this.y = y
        this.originX = x
        this.originY = y
        this.state = state
        this.enemyPosition = EnemyPosition(this.x, this.y, Direction.LEFT)
        this.colliding = false
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
        enemyPosition: EnemyPosition,
        colliding: Boolean
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
        this.colliding = colliding
        generateAnimationFrameIndex()
    }

    fun getNextFrameCell(): FrameMetadata {
        if (colliding) {
            if (frameMetadata.frame == ANIMATION_COLLISION_FRAMES) {
                return collisionFrames[1] ?: FrameMetadata(1, Cell(1, 1, 64, 64))
            } else {
                val nextFrame = frameMetadata.frame + 1
                return collisionFrames.get(nextFrame) ?: FrameMetadata(1, Cell(1, 1, 64, 64))
            }
        }
        return FrameMetadata(1, Cell(1, 1, 128, 128))
    }

    fun getNextPosition(xDelta: Int, yDelta: Int): EnemyPosition {
        if (enemyPosition.direction == Direction.LEFT) {
            return if (enemyPosition.x >= originX - MAX_X_MOVEMENT) {
                EnemyPosition(
                    enemyPosition.x - MOVEMENT_X_DISTANCE - xDelta,
                    enemyPosition.y + yDelta,
                    Direction.LEFT
                )
            } else {
                EnemyPosition(enemyPosition.x, enemyPosition.y + yDelta, Direction.RIGHT)
            }
        } else {
            return if (enemyPosition.x <= originX + MAX_X_MOVEMENT) {
                EnemyPosition(
                    enemyPosition.x + MOVEMENT_X_DISTANCE - xDelta,
                    enemyPosition.y + yDelta,
                    Direction.RIGHT
                )
            } else {
                EnemyPosition(enemyPosition.x, enemyPosition.y + yDelta, Direction.LEFT)
            }
        }
    }

    private fun generateAnimationFrameIndex() {
        animatingFrames[1] = FrameMetadata(1, Cell(1, 1, 128, 128))

        collisionFrames[1] = FrameMetadata(1, Cell(1, 2, 128, 128))
        collisionFrames[2] = FrameMetadata(2, Cell(1, 2, 128, 128))
        collisionFrames[3] = FrameMetadata(3, Cell(1, 1, 128, 128))
        collisionFrames[4] = FrameMetadata(4, Cell(1, 1, 128, 128))
        collisionFrames[5] = FrameMetadata(5, Cell(1, 2, 128, 128))
        collisionFrames[6] = FrameMetadata(6, Cell(1, 2, 128, 128))
        collisionFrames[7] = FrameMetadata(7, Cell(1, 1, 128, 128))
        collisionFrames[8] = FrameMetadata(8, Cell(1, 1, 128, 128))
    }
}