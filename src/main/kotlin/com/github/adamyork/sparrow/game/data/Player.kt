package com.github.adamyork.sparrow.game.data

import org.slf4j.LoggerFactory

class Player {

    companion object {
        val LOGGER: org.slf4j.Logger = LoggerFactory.getLogger(Player::class.java)
        const val MAX_X_VELOCITY: Double = 24.0
        const val MAX_Y_VELOCITY: Double = 64.0
        const val JUMP_DISTANCE: Int = 192
        const val ANIMATION_MOVING_FRAMES = 2
        const val ANIMATION_JUMPING_FRAMES = 8
        const val ANIMATION_COLLISION_FRAMES = 8
    }

    var movingFrames: HashMap<Int, FrameMetadata> = HashMap()
    var jumpingFrames: HashMap<Int, FrameMetadata> = HashMap()
    var collisionFrames: HashMap<Int, FrameMetadata> = HashMap()

    var width: Int
    var height: Int
    var x: Int
    var y: Int
    var vx: Double
    var vy: Double
    var jumping: Boolean
    var jumpDy: Int
    var jumpReached: Boolean
    var moving: Boolean
    var direction: Direction
    var frameMetadata: FrameMetadata
    var colliding: Boolean
    var scanVerticalCeiling: Boolean
    var scanVerticalFloor: Boolean

    constructor(xPos: Int, yPos: Int, width: Int, height: Int) {
        this.x = xPos
        this.y = yPos
        this.width = width
        this.height = height
        this.vx = 0.0
        this.vy = 0.0
        this.jumping = false
        this.jumpDy = 0
        this.jumpReached = false
        this.moving = false
        this.direction = Direction.RIGHT
        this.frameMetadata = FrameMetadata(1, Cell(1, 1, width, height))
        this.colliding = false
        this.scanVerticalCeiling = false
        this.scanVerticalFloor = false
        generateAnimationFrameIndex()
    }

    constructor(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        vx: Double,
        vy: Double,
        jumping: Boolean,
        jumpDy: Int,
        jumpReached: Boolean,
        moving: Boolean,
        direction: Direction,
        frame: FrameMetadata,
        colliding: Boolean,
        scanVerticalCeiling: Boolean,
        scanVerticalFloor: Boolean,
    ) {
        this.x = x
        this.y = y
        this.width = width
        this.height = height
        this.vx = vx
        this.vy = vy
        this.jumping = jumping
        this.jumpDy = jumpDy
        this.jumpReached = jumpReached
        this.moving = moving
        this.direction = direction
        this.frameMetadata = frame
        this.colliding = colliding
        this.scanVerticalCeiling = scanVerticalCeiling
        this.scanVerticalFloor = scanVerticalFloor
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
        if (jumping && !jumpReached) {
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
        movingFrames[2] = FrameMetadata(2, Cell(1, 2, width, height))

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
        this.jumpDy = 0
        this.jumpReached = false
        this.moving = false
        this.direction = Direction.RIGHT
        this.frameMetadata = FrameMetadata(1, Cell(1, 1, width, height))
        this.colliding = false
        this.scanVerticalCeiling = false
        this.scanVerticalFloor = false
    }
}
