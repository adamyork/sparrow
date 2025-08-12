package com.github.adamyork.socketgame.engine.data

import com.github.adamyork.socketgame.game.data.Direction

data class PhysicsResult(
    val x: Int,
    val y: Int,
    val dy: Int,
    val vx: Double,
    val vy: Double,
    val moving: Boolean,
    val jumping: Boolean,
    val direction: Direction,
    val floor: Int
)