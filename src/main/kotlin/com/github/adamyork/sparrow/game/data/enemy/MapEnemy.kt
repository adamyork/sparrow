package com.github.adamyork.sparrow.game.data.enemy

import com.github.adamyork.sparrow.game.data.*
import com.github.adamyork.sparrow.game.data.item.MapItemState
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.image.BufferedImage

open class MapEnemy : Drawable {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(MapEnemy::class.java)
        const val ANIMATION_COLLISION_FRAMES = 8
        const val MAX_X_MOVEMENT = 50
        const val MOVEMENT_X_DISTANCE = 10
    }

    var originX: Int
    var originY: Int
    var type: MapEnemyType
    var animatingFrames: HashMap<Int, FrameMetadata> = HashMap()
    var collisionFrames: HashMap<Int, FrameMetadata> = HashMap()
    var enemyPosition: EnemyPosition
    var colliding: Boolean

    constructor(
        width: Int,
        height: Int,
        x: Int,
        y: Int,
        type: MapEnemyType,
        state: MapItemState,
        bufferedImage: BufferedImage
    ) {
        this.width = width
        this.height = height
        this.x = x
        this.y = y
        this.originX = x
        this.originY = y
        this.type = type
        this.state = state
        this.bufferedImage = bufferedImage
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
        type: MapEnemyType,
        state: MapItemState,
        bufferedImage: BufferedImage,
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
        this.type = type
        this.state = state
        this.bufferedImage = bufferedImage
        this.frameMetadata = frameMetadata
        this.enemyPosition = enemyPosition
        this.colliding = colliding
        generateAnimationFrameIndex()
    }

    override fun nestedDirection(): Direction {
        return this.enemyPosition.direction
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

    open fun getNextPosition(player: Player, viewPort: ViewPort): EnemyPosition {
        if (enemyPosition.direction == Direction.LEFT) {
            return if (enemyPosition.x >= originX - MAX_X_MOVEMENT) {
                EnemyPosition(
                    enemyPosition.x - MOVEMENT_X_DISTANCE,
                    enemyPosition.y,
                    Direction.LEFT
                )
            } else {
                EnemyPosition(enemyPosition.x, enemyPosition.y, Direction.RIGHT)
            }
        } else {
            return if (enemyPosition.x <= originX + MAX_X_MOVEMENT) {
                EnemyPosition(
                    enemyPosition.x + MOVEMENT_X_DISTANCE,
                    enemyPosition.y,
                    Direction.RIGHT
                )
            } else {
                EnemyPosition(enemyPosition.x, enemyPosition.y, Direction.LEFT)
            }
        }
    }

    open fun getNextEnemyState(player: Player): MapItemState {
        return this.state
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

    open fun from(
        frameMetadata: FrameMetadata,
        isColliding: Boolean
    ): MapEnemy {
        return MapEnemy(
            this.width,
            this.height,
            this.x,
            this.y,
            this.originX,
            this.originY,
            this.type,
            this.state,
            this.bufferedImage,
            frameMetadata,
            this.enemyPosition,
            isColliding
        )
    }


    open fun from(
        x: Int,
        y: Int,
        state: MapItemState,
        frameMetadata: FrameMetadata,
        nextPosition: EnemyPosition,
        isColliding: Boolean
    ): MapEnemy {
        return MapEnemy(
            this.width,
            this.height,
            x,
            y,
            this.originX,
            this.originY,
            this.type,
            state,
            this.bufferedImage,
            frameMetadata,
            nextPosition,
            isColliding
        )
    }
}