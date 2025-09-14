package com.github.adamyork.socketgame.game.engine.data

data class PhysicsYResult(
    val y: Int,
    val vy: Double,
    val jumping: Boolean,
    val jumpY: Int,
    val jumpReached: Boolean
)
