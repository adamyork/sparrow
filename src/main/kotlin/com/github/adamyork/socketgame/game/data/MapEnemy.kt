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

    var width: Int
    var height: Int
    var x: Int
    var y: Int
    var originX: Int
    var originY: Int
    var state: MapItemState
    var animatingFrames: HashMap<Int, FrameMetadata> = HashMap()
    var collisionFrames: HashMap<Int, FrameMetadata> = HashMap()
    var frameMetadata: FrameMetadata
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
        this.frameMetadata = FrameMetadata(1, Cell(1, 1, width, height))
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
                return collisionFrames[1] ?: FrameMetadata(1, Cell(1, 1, width, height))
            } else {
                val nextFrame = frameMetadata.frame + 1
                return collisionFrames.get(nextFrame) ?: FrameMetadata(1, Cell(1, 1, width, height))
            }
        }
        return FrameMetadata(1, Cell(1, 1, width, height))
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

    @Suppress("DuplicatedCode")
    private fun generateAnimationFrameIndex() {
        animatingFrames[1] = FrameMetadata(1, Cell(1, 1, width, height))

        collisionFrames[1] = FrameMetadata(1, Cell(1, 2, width, height))
        collisionFrames[2] = FrameMetadata(2, Cell(1, 2, width, height))
        collisionFrames[3] = FrameMetadata(3, Cell(1, 1, width, height))
        collisionFrames[4] = FrameMetadata(4, Cell(1, 1, width, height))
        collisionFrames[5] = FrameMetadata(5, Cell(1, 2, width, height))
        collisionFrames[6] = FrameMetadata(6, Cell(1, 2, width, height))
        collisionFrames[7] = FrameMetadata(7, Cell(1, 1, width, height))
        collisionFrames[8] = FrameMetadata(8, Cell(1, 1, width, height))
    }
}