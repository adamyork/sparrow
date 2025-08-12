package com.github.adamyork.socketgame.game.data

class Player {

    companion object {
        const val MAX_X_VELOCITY: Double = 8.0
        const val MAX_Y_VELOCITY: Double = 32.0
        const val JUMP_DISTANCE: Int = 350
    }

    val width: Int = 64//TODO Magic Number
    val height: Int = 64//TODO Magic Number
    var x: Int = 0
    var y: Int = 0
    var dy: Int = 0
    var vx: Double = 0.0
    var vy: Double = 0.0
    var jumping: Boolean = false
    var moving: Boolean = false
    var direction: Direction = Direction.RIGHT
    var floor: Int = 0

    constructor(xPos: Int, yPos: Int, floor: Int) {
        this.x = xPos
        this.y = yPos
        this.floor = floor
    }

    constructor(
        x: Int,
        y: Int,
        dy: Int,
        vx: Double,
        vy: Double,
        jumping: Boolean,
        moving: Boolean,
        direction: Direction,
        floor: Int
    ) {
        this.x = x
        this.y = y
        this.dy = dy
        this.vx = vx
        this.vy = vy
        this.jumping = jumping
        this.moving = moving
        this.direction = direction
        this.floor = floor
    }

    fun setPlayerState(moving: Boolean, jumping: Boolean, direction: Direction) {
        this.moving = moving
        this.jumping = jumping
        this.direction = direction
    }
}