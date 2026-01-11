package com.github.adamyork.sparrow.game.data.item

import com.github.adamyork.sparrow.game.data.*
import com.github.adamyork.sparrow.game.data.enemy.EnemyInteractionState

interface Item : GameElement {

    val type: ItemType
    val id: Int

    fun getFirstDeactivatingFrame(): FrameMetadata

    fun getNextActiveMetadataWithState(
        activeFrames: HashMap<Int, FrameMetadata>,
        numActiveFrames: Int
    ): Pair<FrameMetadata, FrameMetadataState> {
        var metadata = activeFrames[1] ?: throw RuntimeException("missing animation frame")
        val metadataState =
            FrameMetadataState(
                GameElementCollisionState.FREE,
                EnemyInteractionState.ISOLATED,
                state
            )
        if (state == GameElementState.ACTIVE) {
            if (frameMetadata.frame == numActiveFrames) {
                return Pair(metadata, metadataState)
            } else {
                val nextFrame = frameMetadata.frame + 1
                metadata = activeFrames[nextFrame] ?: FrameMetadata(1, Cell(1, 1, width, height))
                return Pair(metadata, metadataState)
            }
        }
        return Pair(metadata, metadataState)
    }

}