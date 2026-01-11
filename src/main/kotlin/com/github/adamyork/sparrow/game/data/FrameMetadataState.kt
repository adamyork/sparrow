package com.github.adamyork.sparrow.game.data

import com.github.adamyork.sparrow.game.data.enemy.EnemyInteractionState

data class FrameMetadataState(
    val colliding: GameElementCollisionState,
    val interacting: EnemyInteractionState,
    val state: GameElementState
)