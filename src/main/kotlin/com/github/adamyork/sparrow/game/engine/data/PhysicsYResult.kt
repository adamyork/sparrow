package com.github.adamyork.sparrow.game.engine.data

import com.github.adamyork.sparrow.game.data.player.PlayerJumpingState

data class PhysicsYResult(
    val y: Int,
    val vy: Double,
    val jumping: PlayerJumpingState
)
