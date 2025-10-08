package com.github.adamyork.socketgame.game.data

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MapItem {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(MapItem::class.java)
        const val ANIMATION_DEACTIVATING_FRAMES = 5
    }

    var width: Int
    var height: Int
    var x: Int
    var y: Int
    var type: MapItemType
    var state: MapItemState
    var deactivatingFrames: HashMap<Int, FrameMetadata> = HashMap()
    var frameMetadata: FrameMetadata

    constructor(width: Int, height: Int, x: Int, y: Int, type: MapItemType, state: MapItemState) {
        this.width = width
        this.height = height
        this.x = x
        this.y = y
        this.type = type
        this.state = state
        this.frameMetadata = FrameMetadata(1, Cell(1, 1, width, height))
        generateAnimationFrameIndex()
    }

    constructor(
        width: Int,
        height: Int,
        x: Int,
        y: Int,
        type: MapItemType,
        state: MapItemState,
        frameMetadata: FrameMetadata
    ) {
        this.width = width
        this.height = height
        this.x = x
        this.y = y
        this.type = type
        this.state = state
        this.frameMetadata = frameMetadata
        generateAnimationFrameIndex()
    }

    fun getNextFrameCell(): FrameMetadata {
        if (state == MapItemState.DEACTIVATING) {
            if (frameMetadata.frame == ANIMATION_DEACTIVATING_FRAMES) {
                return FrameMetadata(0, Cell(0, 0, width, height))
            } else {
                val nextFrame = frameMetadata.frame + 1
                return deactivatingFrames.get(nextFrame) ?: FrameMetadata(1, Cell(0, 0, width, height))
            }
        }
        return FrameMetadata(1, Cell(1, 1, width, height))
    }

    private fun generateAnimationFrameIndex() {
        deactivatingFrames[1] = FrameMetadata(1, Cell(1, 1, width, height))
        deactivatingFrames[2] = FrameMetadata(2, Cell(1, 2, width, height))
        deactivatingFrames[3] = FrameMetadata(3, Cell(1, 3, width, height))
        deactivatingFrames[4] = FrameMetadata(4, Cell(1, 4, width, height))
        deactivatingFrames[5] = FrameMetadata(5, Cell(1, 5, width, height))
    }
}