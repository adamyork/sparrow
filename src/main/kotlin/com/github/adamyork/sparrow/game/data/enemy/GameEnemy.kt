package com.github.adamyork.sparrow.game.data.enemy

import com.github.adamyork.sparrow.game.data.*
import com.github.adamyork.sparrow.game.data.enemy.MapBlockerEnemy.Companion.ANIMATION_COLLISION_FRAMES
import com.github.adamyork.sparrow.game.data.player.Player

interface GameEnemy : GameElement {

    val type: MapEnemyType
    val originX: Int
    val originY: Int
    val enemyPosition: EnemyPosition
    val colliding: GameElementCollisionState
    val interacting: GameEnemyInteractionState

    fun getNextPosition(): EnemyPosition

    fun getNextEnemyState(player: Player): GameElementState

    fun getNextCollisionMetadataWithState(
        animatingFrames: HashMap<Int, FrameMetadata>,
        collisionFrames: HashMap<Int, FrameMetadata>,
    ): Pair<FrameMetadata, FrameMetadataState> {
        var metadata = animatingFrames[1] ?: throw RuntimeException("missing animation frame")
        var metadataState = FrameMetadataState(this.colliding, this.interacting, state)
        if (colliding == GameElementCollisionState.COLLIDING) {
            if (frameMetadata.frame == ANIMATION_COLLISION_FRAMES) {
                metadataState = metadataState.copy(colliding = GameElementCollisionState.FREE)
                return Pair(metadata, metadataState)
            } else {
                val nextFrame = frameMetadata.frame + 1
                metadata = collisionFrames[nextFrame] ?: throw RuntimeException("missing animation frame")
                return Pair(metadata, metadataState)
            }
        }
        return Pair(metadata, metadataState)
    }

}