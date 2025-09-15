package com.github.adamyork.socketgame.game.data

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MapItem {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(MapItem::class.java)
        const val ANIMATION_DEACTIVATING_FRAMES = 5
    }

    var width: Int = 64//TODO Magic Number
    var height: Int = 64//TODO Magic Number
    var x: Int = 0
    var y: Int = 0
    var state: MapItemState
    var deactivatingFrames: HashMap<Int, FrameMetadata> = HashMap()
    var frameMetadata: FrameMetadata = FrameMetadata(1, Cell(1, 1, 64, 64))

    constructor(width: Int, height: Int, x: Int, y: Int, state: MapItemState) {
        this.width = width
        this.height = height
        this.x = x
        this.y = y
        this.state = state
        generateAnimationFrameIndex()
    }

    constructor(width: Int, height: Int, x: Int, y: Int, state: MapItemState, frameMetadata: FrameMetadata) {
        this.width = width
        this.height = height
        this.x = x
        this.y = y
        this.state = state
        this.frameMetadata = frameMetadata
        generateAnimationFrameIndex()
    }

    fun getNextFrameCell(): FrameMetadata {
        if (state == MapItemState.DEACTIVATING) {
            if (frameMetadata.frame == ANIMATION_DEACTIVATING_FRAMES) {
                return FrameMetadata(0, Cell(0, 0, 64, 64))
            } else {
                val nextFrame = frameMetadata.frame + 1
                return deactivatingFrames.get(nextFrame) ?: FrameMetadata(1, Cell(0, 0, 64, 64))
            }
        }
        return FrameMetadata(1, Cell(1, 1, 64, 64))
    }

    private fun generateAnimationFrameIndex() {
        deactivatingFrames[1] = FrameMetadata(1, Cell(1, 1, 64, 64))
        deactivatingFrames[2] = FrameMetadata(2, Cell(1, 2, 64, 64))
        deactivatingFrames[3] = FrameMetadata(3, Cell(1, 3, 64, 64))
        deactivatingFrames[4] = FrameMetadata(4, Cell(1, 4, 64, 64))
        deactivatingFrames[5] = FrameMetadata(5, Cell(1, 5, 64, 64))
    }
}