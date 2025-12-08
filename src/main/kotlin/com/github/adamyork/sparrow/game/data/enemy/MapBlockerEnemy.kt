package com.github.adamyork.sparrow.game.data.enemy

import com.github.adamyork.sparrow.game.data.*
import com.github.adamyork.sparrow.game.data.item.MapItemState
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.image.BufferedImage

class MapBlockerEnemy : GameElement, GameEnemy {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(MapBlockerEnemy::class.java)
        const val ANIMATION_COLLISION_FRAMES = 8
        const val MAX_X_MOVEMENT = 50
        const val MOVEMENT_X_DISTANCE = 10
    }

    override var width: Int
    override var height: Int
    override var x: Int
    override var y: Int
    override var type: MapEnemyType
    override var state: MapItemState
    override var bufferedImage: BufferedImage
    override var frameMetadata: FrameMetadata
    override var originX: Int
    override var originY: Int
    override var enemyPosition: EnemyPosition
    override var colliding: Boolean
    override var interacting: Boolean

    var animatingFrames: HashMap<Int, FrameMetadata> = HashMap()
    var collisionFrames: HashMap<Int, FrameMetadata> = HashMap()
    var interactingFrames: HashMap<Int, FrameMetadata> = HashMap()

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
        this.interacting = false
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
        colliding: Boolean,
        interacting: Boolean
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
        this.interacting = interacting
        generateAnimationFrameIndex()
    }

    override fun nestedDirection(): Direction {
        return this.enemyPosition.direction
    }

    override fun getNextFrameCell(): FrameMetadata {
        if (colliding) {
            if (frameMetadata.frame == ANIMATION_COLLISION_FRAMES) {
                this.colliding = false
                return animatingFrames[1] ?: throw RuntimeException("missing animation frame")
            } else {
                val nextFrame = frameMetadata.frame + 1
                return collisionFrames[nextFrame] ?: throw RuntimeException("missing animation frame")
            }
        }
        return animatingFrames[1] ?: throw RuntimeException("missing animation frame")
    }

    override fun getNextPosition(player: Player, viewPort: ViewPort): EnemyPosition {
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

    override fun getNextEnemyState(player: Player): MapItemState {
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

    override fun from(
        frameMetadata: FrameMetadata,
        isColliding: Boolean,
        isInteracting: Boolean
    ): MapBlockerEnemy {
        return MapBlockerEnemy(
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
            isColliding,
            isInteracting
        )
    }


    override fun from(
        x: Int,
        y: Int,
        state: MapItemState,
        frameMetadata: FrameMetadata,
        nextPosition: EnemyPosition,
        isColliding: Boolean,
        isInteracting: Boolean
    ): MapBlockerEnemy {
        return MapBlockerEnemy(
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
            isColliding,
            isInteracting
        )
    }
}