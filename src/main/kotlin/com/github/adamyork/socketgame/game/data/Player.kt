package com.github.adamyork.socketgame.game.data

class Player {

    companion object {
        const val MAX_X_VELOCITY: Double = 16.0
        const val MAX_Y_VELOCITY: Double = 64.0
        const val JUMP_DISTANCE: Int = 256
        const val ANIMATION_MOVING_FRAMES = 3
        const val ANIMATION_JUMPING_FRAMES = 4
        const val ANIMATION_COLLISION_FRAMES = 8
    }

    var movingFrames: HashMap<Int, FrameMetadata> = HashMap()
    var jumpingFrames: HashMap<Int, FrameMetadata> = HashMap()
    var collisionFrames: HashMap<Int, FrameMetadata> = HashMap()

    val width: Int = 64//TODO Magic Number
    val height: Int = 64//TODO Magic Number
    var x: Int = 0
    var y: Int = 0
    var vx: Double = 0.0
    var vy: Double = 0.0
    var jumping: Boolean = false
    var jumpY: Int = 0
    var jumpReached: Boolean = false
    var moving: Boolean = false
    var direction: Direction = Direction.RIGHT
    var frameMetadata: FrameMetadata = FrameMetadata(1, Cell(1, 1, 64, 64))
    var colliding: Boolean = false

    constructor(xPos: Int, yPos: Int) {
        this.x = xPos
        this.y = yPos
        generateAnimationFrameIndex()
    }

    constructor(
        x: Int,
        y: Int,
        vx: Double,
        vy: Double,
        jumping: Boolean,
        jumpY: Int,
        jumpReached: Boolean,
        moving: Boolean,
        direction: Direction,
        frame: FrameMetadata,
        colliding: Boolean
    ) {
        this.x = x
        this.y = y
        this.vx = vx
        this.vy = vy
        this.jumping = jumping
        this.jumpY = jumpY
        this.jumpReached = jumpReached
        this.moving = moving
        this.direction = direction
        this.frameMetadata = frame
        this.colliding = colliding
        generateAnimationFrameIndex()
    }

    fun setPlayerState(moving: Boolean, jumping: Boolean, direction: Direction) {
        this.moving = moving
        this.jumping = jumping
        this.direction = direction
    }

    fun getNextFrameCell(): FrameMetadata {
        if (colliding) {
            if (frameMetadata.frame == ANIMATION_COLLISION_FRAMES) {
                return collisionFrames.get(1) ?: FrameMetadata(1, Cell(1, 1, 64, 64))
            } else {
                val nextFrame = frameMetadata.frame + 1
                return collisionFrames.get(nextFrame) ?: FrameMetadata(1, Cell(1, 1, 64, 64))
            }
        }
        if (jumping && !jumpReached) {
            if (frameMetadata.frame == ANIMATION_JUMPING_FRAMES) {
                return jumpingFrames.get(1) ?: FrameMetadata(1, Cell(1, 1, 64, 64))
            } else {
                val nextFrame = frameMetadata.frame + 1
                return jumpingFrames.get(nextFrame) ?: FrameMetadata(1, Cell(1, 1, 64, 64))
            }
        } else if (jumping) {
            return jumpingFrames.get(4) ?: FrameMetadata(1, Cell(1, 1, 64, 64))
        }
        if (moving) {
            if (frameMetadata.frame == ANIMATION_MOVING_FRAMES) {
                return movingFrames.get(1) ?: FrameMetadata(1, Cell(1, 1, 64, 64))
            } else {
                val nextFrame = frameMetadata.frame + 1
                return movingFrames.get(nextFrame) ?: FrameMetadata(1, Cell(1, 1, 64, 64))
            }
        }
        return FrameMetadata(1, Cell(1, 1, 64, 64))
    }

    private fun generateAnimationFrameIndex() {
        movingFrames[1] = FrameMetadata(1, Cell(1, 1, 64, 64))
        movingFrames[2] = FrameMetadata(2, Cell(1, 2, 64, 64))
        movingFrames[3] = FrameMetadata(3, Cell(1, 3, 64, 64))

        jumpingFrames[1] = FrameMetadata(1, Cell(1, 4, 64, 64))
        jumpingFrames[2] = FrameMetadata(2, Cell(1, 5, 64, 64))
        jumpingFrames[3] = FrameMetadata(3, Cell(1, 6, 64, 64))
        jumpingFrames[4] = FrameMetadata(4, Cell(2, 1, 64, 64))

        collisionFrames[1] = FrameMetadata(1, Cell(3, 5, 64, 64))
        collisionFrames[2] = FrameMetadata(2, Cell(3, 5, 64, 64))
        collisionFrames[3] = FrameMetadata(3, Cell(1, 3, 64, 64))
        collisionFrames[4] = FrameMetadata(4, Cell(1, 3, 64, 64))
        collisionFrames[5] = FrameMetadata(5, Cell(3, 5, 64, 64))
        collisionFrames[6] = FrameMetadata(6, Cell(3, 5, 64, 64))
        collisionFrames[7] = FrameMetadata(7, Cell(1, 3, 64, 64))
        collisionFrames[8] = FrameMetadata(8, Cell(1, 3, 64, 64))
    }
}
