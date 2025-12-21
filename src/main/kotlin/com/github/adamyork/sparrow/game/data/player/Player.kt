package com.github.adamyork.sparrow.game.data.player

import com.github.adamyork.sparrow.game.data.*
import com.github.adamyork.sparrow.game.data.enemy.GameEnemyInteractionState
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.image.BufferedImage

data class Player(
    override val x: Int,
    override val y: Int,
    override val width: Int,
    override val height: Int,
    override val state: GameElementState,
    override val frameMetadata: FrameMetadata,
    override val bufferedImage: BufferedImage,
    var vx: Double,
    val vy: Double,
    val jumping: Boolean,
    val moving: PlayerMovingState,
    val direction: Direction,
    val colliding: GameElementCollisionState,
) : GameElement {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(Player::class.java)
        const val ANIMATION_MOVING_FRAMES = 4
        const val ANIMATION_JUMPING_FRAMES = 8
        const val ANIMATION_COLLISION_FRAMES = 8
    }

    var movingFrames: HashMap<Int, FrameMetadata> = HashMap()
    var jumpingFrames: HashMap<Int, FrameMetadata> = HashMap()
    var collisionFrames: HashMap<Int, FrameMetadata> = HashMap()

    init {
        generateAnimationFrameIndex()
    }

    override fun getNextFrameMetadataWithState(): Pair<FrameMetadata, FrameMetadataState> {
        var metadata = movingFrames[1] ?: throw RuntimeException("missing animation frame")
        var metadataState = FrameMetadataState(
            GameElementCollisionState.FREE,
            GameEnemyInteractionState.ISOLATED,
            state
        )
        if (colliding == GameElementCollisionState.COLLIDING) {
            if (frameMetadata.frame >= ANIMATION_COLLISION_FRAMES) {
                metadataState = metadataState.copy(colliding = GameElementCollisionState.FREE)
                metadata = movingFrames[1] ?: throw RuntimeException("missing animation frame")
                return Pair(metadata, metadataState)
            } else {
                val nextFrame = frameMetadata.frame + 1
                metadata = collisionFrames[nextFrame] ?: throw RuntimeException("missing animation frame")
                return Pair(metadata, metadataState)
            }
        }
        if (jumping) {
            if (frameMetadata.frame >= ANIMATION_JUMPING_FRAMES) {
                metadata = jumpingFrames[1] ?: throw RuntimeException("missing animation frame")
                LOGGER.info("here 2")
                return Pair(metadata, metadataState)
            } else {
                val nextFrame = frameMetadata.frame + 1
                metadata = jumpingFrames[nextFrame] ?: throw RuntimeException("missing animation frame")
                return Pair(metadata, metadataState)
            }
        }
        if (moving == PlayerMovingState.MOVING) {
            if (frameMetadata.frame >= ANIMATION_MOVING_FRAMES) {
                metadata = movingFrames[1] ?: throw RuntimeException("missing animation frame")
                return Pair(metadata, metadataState)
            } else {
                val nextFrame = frameMetadata.frame + 1
                metadata = movingFrames[nextFrame] ?: throw RuntimeException("missing animation frame")
                return Pair(metadata, metadataState)
            }
        }
        return Pair(metadata, metadataState)
    }

    private fun generateAnimationFrameIndex() {
        movingFrames[1] = FrameMetadata(1, Cell(1, 1, width, height))
        movingFrames[2] = FrameMetadata(2, Cell(1, 1, width, height))
        movingFrames[3] = FrameMetadata(3, Cell(1, 2, width, height))
        movingFrames[4] = FrameMetadata(4, Cell(1, 2, width, height))

        jumpingFrames[1] = FrameMetadata(1, Cell(1, 1, width, height))
        jumpingFrames[2] = FrameMetadata(2, Cell(1, 2, width, height))
        jumpingFrames[3] = FrameMetadata(3, Cell(1, 3, width, height))
        jumpingFrames[4] = FrameMetadata(3, Cell(1, 3, width, height))
        jumpingFrames[5] = FrameMetadata(3, Cell(1, 3, width, height))
        jumpingFrames[6] = FrameMetadata(3, Cell(1, 3, width, height))
        jumpingFrames[7] = FrameMetadata(3, Cell(1, 3, width, height))
        jumpingFrames[8] = FrameMetadata(3, Cell(1, 3, width, height))

        collisionFrames[1] = FrameMetadata(1, Cell(1, 4, width, height))
        collisionFrames[2] = FrameMetadata(2, Cell(1, 4, width, height))
        collisionFrames[3] = FrameMetadata(3, Cell(1, 3, width, height))
        collisionFrames[4] = FrameMetadata(4, Cell(1, 3, width, height))
        collisionFrames[5] = FrameMetadata(5, Cell(1, 4, width, height))
        collisionFrames[6] = FrameMetadata(6, Cell(1, 4, width, height))
        collisionFrames[7] = FrameMetadata(7, Cell(1, 3, width, height))
        collisionFrames[8] = FrameMetadata(8, Cell(1, 3, width, height))
    }

    override fun nestedDirection(): Direction {
        return this.direction
    }
}