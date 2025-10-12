package com.github.adamyork.sparrow.game.engine.data

data class PhysicsYResult(
    val y: Int,
    val vy: Double,
    val jumping: Boolean,
    val jumpDy: Int,
    val jumpReached: Boolean,
    val scanVerticalCeiling: Boolean,
    val scanVerticalFloor: Boolean,
)
