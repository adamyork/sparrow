package com.github.adamyork.sparrow.game.data.item

import com.github.adamyork.sparrow.game.data.Cell
import com.github.adamyork.sparrow.game.data.FrameMetadata
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.image.BufferedImage

class MapFinishItem : MapItem {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(MapFinishItem::class.java)
        const val ANIMATION_DEACTIVATING_FRAMES = 1
        const val ANIMATION_ACTIVE_FRAMES = 1
    }

    constructor(
        width: Int,
        height: Int,
        x: Int,
        y: Int,
        type: MapItemType,
        state: MapItemState,
        bufferedImage: BufferedImage
    ) : super(
        width,
        height,
        x,
        y,
        type,
        state,
        bufferedImage
    )

    constructor(
        width: Int,
        height: Int,
        x: Int,
        y: Int,
        type: MapItemType,
        state: MapItemState,
        bufferedImage: BufferedImage,
        frameMetadata: FrameMetadata,
    ) : super(
        width,
        height,
        x,
        y,
        type,
        state,
        bufferedImage,
        frameMetadata
    )

    override fun getNextFrameCell(): FrameMetadata {
        if (state == MapItemState.DEACTIVATING) {
            if (frameMetadata.frame == ANIMATION_DEACTIVATING_FRAMES) {
                LOGGER.info("deactivating complete 0")
                return FrameMetadata(1, Cell(1, 1, width, height))
            } else {
                val nextFrame = frameMetadata.frame + 1
                LOGGER.info("deactivating frame $nextFrame")
                return deactivatingFrames.get(nextFrame) ?: FrameMetadata(1, Cell(1, 1, width, height))
            }
        }
        if (state == MapItemState.ACTIVE) {
            if (frameMetadata.frame == ANIMATION_ACTIVE_FRAMES) {
                return FrameMetadata(1, Cell(1, 1, width, height))
            } else {
                val nextFrame = frameMetadata.frame + 1
                return activeFrames.get(nextFrame) ?: FrameMetadata(1, Cell(1, 1, width, height))
            }
        }
        return FrameMetadata(1, Cell(1, 1, width, height))
    }

    override fun generateAnimationFrameIndex() {
        activeFrames[1] = FrameMetadata(1, Cell(1, 1, width, height))
        deactivatingFrames[1] = FrameMetadata(1, Cell(1, 1, width, height))
    }

    override fun from(state: MapItemState, frameMetadata: FrameMetadata): MapFinishItem {
        return MapFinishItem(
            this.width,
            this.height,
            this.x,
            this.y,
            this.type,
            state,
            this.bufferedImage,
            frameMetadata
        )
    }

    override fun from(x: Int, y: Int, state: MapItemState, frameMetadata: FrameMetadata): MapFinishItem {
        return MapFinishItem(
            this.width,
            this.height,
            x,
            y,
            this.type,
            state,
            this.bufferedImage,
            frameMetadata
        )
    }

}