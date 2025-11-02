package com.github.adamyork.sparrow.game.data

import com.github.adamyork.sparrow.game.engine.data.PhysicsXResult
import com.github.adamyork.sparrow.game.engine.data.PhysicsYResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.image.BufferedImage

class Player {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(Player::class.java)
        const val MAX_X_VELOCITY: Double = 16.0
        const val MAX_Y_VELOCITY: Double = 32.0
        const val JUMP_DISTANCE: Int = 256
        const val ANIMATION_MOVING_FRAMES = 4
        const val ANIMATION_JUMPING_FRAMES = 8
        const val ANIMATION_COLLISION_FRAMES = 8
    }

    var movingFrames: HashMap<Int, FrameMetadata> = HashMap()
    var jumpingFrames: HashMap<Int, FrameMetadata> = HashMap()
    var collisionFrames: HashMap<Int, FrameMetadata> = HashMap()

    var width: Int
    var height: Int
    var bufferedImage: BufferedImage
    var x: Int
    var y: Int
    var vx: Double
    var vy: Double
    var jumping: Boolean
    var moving: Boolean
    var direction: Direction
    var frameMetadata: FrameMetadata
    var colliding: Boolean

    constructor(xPos: Int, yPos: Int, width: Int, height: Int, bufferedImage: BufferedImage) {
        this.x = xPos
        this.y = yPos
        this.width = width
        this.height = height
        this.bufferedImage = bufferedImage
        this.vx = 0.0
        this.vy = 0.0
        this.jumping = false
        this.moving = false
        this.direction = Direction.RIGHT
        this.frameMetadata = FrameMetadata(1, Cell(1, 1, width, height))
        this.colliding = false
        generateAnimationFrameIndex()
    }

    private constructor(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        bufferedImage: BufferedImage,
        vx: Double,
        vy: Double,
        jumping: Boolean,
        moving: Boolean,
        direction: Direction,
        frame: FrameMetadata,
        colliding: Boolean,
    ) {
        this.x = x
        this.y = y
        this.width = width
        this.height = height
        this.bufferedImage = bufferedImage
        this.vx = vx
        this.vy = vy
        this.jumping = jumping
        this.moving = moving
        this.direction = direction
        this.frameMetadata = frame
        this.colliding = colliding
        generateAnimationFrameIndex()
    }

    fun setPlayerState(moving: Boolean, jumping: Boolean, direction: Direction) {
        this.moving = moving
        this.jumping = jumping
        if (this.direction != direction) {
            LOGGER.info("direction changed player vx was: ${this.vx} and is now 0")
            this.vx = 0.0
        }
        this.direction = direction
    }

    fun getNextFrameCell(): FrameMetadata {
        if (colliding) {
            if (frameMetadata.frame == ANIMATION_COLLISION_FRAMES) {
                return collisionFrames.get(1) ?: FrameMetadata(1, Cell(1, 1, width, height))
            } else {
                val nextFrame = frameMetadata.frame + 1
                return collisionFrames.get(nextFrame) ?: FrameMetadata(1, Cell(1, 1, width, height))
            }
        }
        if (jumping) {
            LOGGER.info("here")
            if (frameMetadata.frame == ANIMATION_JUMPING_FRAMES) {
                LOGGER.info("here1")
                return jumpingFrames.get(1) ?: FrameMetadata(1, Cell(1, 1, width, height))
            } else {
                LOGGER.info("here2")
                val nextFrame = frameMetadata.frame + 1
                return jumpingFrames.get(nextFrame) ?: FrameMetadata(1, Cell(1, 1, width, height))
            }
        }
        if (moving) {
            if (frameMetadata.frame == ANIMATION_MOVING_FRAMES) {
                return movingFrames.get(1) ?: FrameMetadata(1, Cell(1, 1, width, height))
            } else {
                val nextFrame = frameMetadata.frame + 1
                return movingFrames.get(nextFrame) ?: FrameMetadata(1, Cell(1, 1, width, height))
            }
        }
        return FrameMetadata(1, Cell(1, 1, width, height))
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

    fun reset(xPos: Int, yPos: Int) {
        this.x = xPos
        this.y = yPos
        this.vx = 0.0
        this.vy = 0.0
        this.jumping = false
        this.moving = false
        this.direction = Direction.RIGHT
        this.frameMetadata = FrameMetadata(1, Cell(1, 1, width, height))
        this.colliding = false
    }

    fun from(physicsYResult: PhysicsYResult): Player {
        return Player(
            this.x,
            physicsYResult.y,
            this.width,
            this.height,
            this.bufferedImage,
            this.vx,
            physicsYResult.vy,
            physicsYResult.jumping,
            this.moving,
            this.direction,
            this.frameMetadata,
            this.colliding
        )
    }

    fun from(physicsXResult: PhysicsXResult, physicsYResult: PhysicsYResult): Player {
        return Player(
            physicsXResult.x,
            physicsYResult.y,
            this.width,
            this.height,
            this.bufferedImage,
            physicsXResult.vx,
            physicsYResult.vy,
            physicsYResult.jumping,
            physicsXResult.moving,
            this.direction,
            this.frameMetadata,
            this.colliding
        )
    }

    fun from(x: Int, vx: Double, isColliding: Boolean): Player {
        return Player(
            x,
            this.y,
            this.width,
            this.height,
            this.bufferedImage,
            vx,
            this.vy,
            this.jumping,
            this.moving,
            this.direction,
            this.frameMetadata,
            isColliding,
        )
    }

    fun from(player: Player, frameMetadata: FrameMetadata): Player {
        return Player(
            player.x,
            player.y,
            player.width,
            player.height,
            this.bufferedImage,
            player.vx,
            player.vy,
            player.jumping,
            player.moving,
            player.direction,
            frameMetadata,
            player.colliding
        )
    }

    fun from(colliding: Boolean): Player {
        return Player(
            this.x,
            this.y,
            this.width,
            this.height,
            this.bufferedImage,
            this.vx,
            this.vy,
            this.jumping,
            this.moving,
            this.direction,
            this.frameMetadata,
            colliding
        )
    }
}
