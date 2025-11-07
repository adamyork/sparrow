package com.github.adamyork.sparrow.game.data.enemy

import com.github.adamyork.sparrow.game.data.*
import com.github.adamyork.sparrow.game.data.item.MapItemState
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.image.BufferedImage

class MapShooterEnemy : MapEnemy {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(MapShooterEnemy::class.java)
        const val MOVEMENT_X_DISTANCE = 10
        const val PLAYER_PROXIMITY_THRESHOLD = 200
        const val ANIMATION_INTERACTING_FRAMES = 8
    }

    constructor(
        width: Int,
        height: Int,
        x: Int,
        y: Int,
        type: MapEnemyType,
        state: MapItemState,
        bufferedImage: BufferedImage
    ) : super(
        width,
        height,
        x,
        y,
        type,
        state,
        bufferedImage
    )

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
    ) : super(
        width,
        height,
        x,
        y,
        originX,
        originY,
        type,
        state,
        bufferedImage,
        frameMetadata,
        enemyPosition,
        colliding,
        interacting
    )

    override fun getNextEnemyState(player: Player): MapItemState {
        return if (player.x >= this.originX - PLAYER_PROXIMITY_THRESHOLD) {
            MapItemState.ACTIVE
        } else {
            this.state
        }
    }

    @Suppress("DuplicatedCode")
    override fun generateAnimationFrameIndex() {
        animatingFrames[1] = FrameMetadata(1, Cell(1, 1, width, height))

        interactingFrames[1] = FrameMetadata(1, Cell(1, 2, width, height))
        interactingFrames[2] = FrameMetadata(2, Cell(1, 2, width, height))
        interactingFrames[3] = FrameMetadata(3, Cell(1, 2, width, height))
        interactingFrames[4] = FrameMetadata(4, Cell(1, 2, width, height))
        interactingFrames[5] = FrameMetadata(5, Cell(1, 2, width, height))
        interactingFrames[6] = FrameMetadata(6, Cell(1, 2, width, height))
        interactingFrames[7] = FrameMetadata(7, Cell(1, 2, width, height))
        interactingFrames[8] = FrameMetadata(8, Cell(1, 2, width, height))

        collisionFrames[1] = FrameMetadata(1, Cell(1, 2, width, height))
        collisionFrames[2] = FrameMetadata(2, Cell(1, 2, width, height))
        collisionFrames[3] = FrameMetadata(3, Cell(1, 1, width, height))
        collisionFrames[4] = FrameMetadata(4, Cell(1, 1, width, height))
        collisionFrames[5] = FrameMetadata(5, Cell(1, 2, width, height))
        collisionFrames[6] = FrameMetadata(6, Cell(1, 2, width, height))
        collisionFrames[7] = FrameMetadata(7, Cell(1, 1, width, height))
        collisionFrames[8] = FrameMetadata(8, Cell(1, 1, width, height))
    }

    override fun getNextFrameCell(): FrameMetadata {
        if (this.interacting) {
            if (frameMetadata.frame == ANIMATION_INTERACTING_FRAMES) {
                this.interacting = false
                return animatingFrames[1] ?: throw RuntimeException("missing animation frame")
            } else {
                val nextFrame = frameMetadata.frame + 1
                return interactingFrames[nextFrame] ?: throw RuntimeException("missing animation frame")
            }
        }
        return super.getNextFrameCell()
    }

    override fun getNextPosition(player: Player, viewPort: ViewPort): EnemyPosition {
//        return if (this.x >= 0) {
//            EnemyPosition(
//                enemyPosition.x - MOVEMENT_X_DISTANCE,
//                enemyPosition.y,
//                Direction.LEFT
//            )
//        } else {
//            EnemyPosition(
//                1024 - this.width,
//                enemyPosition.y,
//                Direction.LEFT
//            )
//        }//TODO put this back in
        return EnemyPosition(
            viewPort.width - this.width,
            enemyPosition.y,
            Direction.LEFT
        )
    }

    override fun from(
        frameMetadata: FrameMetadata,
        isColliding: Boolean,
        isInteracting: Boolean
    ): MapEnemy {
        return MapShooterEnemy(
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
    ): MapEnemy {
        return MapShooterEnemy(
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