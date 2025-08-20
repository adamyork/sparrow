package com.github.adamyork.socketgame.game.data

class Player {

    companion object {
        const val MAX_X_VELOCITY: Double = 8.0
        const val MAX_Y_VELOCITY: Double = 64.0
        const val JUMP_DISTANCE: Int = 256
    }

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

    constructor(xPos: Int, yPos: Int) {
        this.x = xPos
        this.y = yPos
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
    }

    fun setPlayerState(moving: Boolean, jumping: Boolean, direction: Direction) {
        this.moving = moving
        this.jumping = jumping
        this.direction = direction
    }
}
