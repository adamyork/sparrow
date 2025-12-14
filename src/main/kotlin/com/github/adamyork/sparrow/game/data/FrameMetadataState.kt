package com.github.adamyork.sparrow.game.data

import com.github.adamyork.sparrow.game.data.enemy.GameEnemyInteractionState

data class FrameMetadataState(
    val colliding: GameElementCollisionState,
    val interacting: GameEnemyInteractionState,
    val state: GameElementState
)