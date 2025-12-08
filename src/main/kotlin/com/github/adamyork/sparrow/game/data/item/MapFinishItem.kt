package com.github.adamyork.sparrow.game.data.item

import com.github.adamyork.sparrow.game.data.Cell
import com.github.adamyork.sparrow.game.data.Direction
import com.github.adamyork.sparrow.game.data.FrameMetadata
import com.github.adamyork.sparrow.game.data.GameElement
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.image.BufferedImage

class MapFinishItem : GameElement, GameItem {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(MapFinishItem::class.java)
        const val ANIMATION_DEACTIVATING_FRAMES = 1
        const val ANIMATION_ACTIVE_FRAMES = 1
    }

    override var width: Int
    override var height: Int
    override var x: Int
    override var y: Int
    override var type: MapItemType
    override var state: MapItemState
    override var frameMetadata: FrameMetadata

    override var bufferedImage: BufferedImage

    var activeFrames: HashMap<Int, FrameMetadata> = HashMap()
    var deactivatingFrames: HashMap<Int, FrameMetadata> = HashMap()

    constructor(
        width: Int,
        height: Int,
        x: Int,
        y: Int,
        type: MapItemType,
        state: MapItemState,
        bufferedImage: BufferedImage
    ) {
        this.width = width
        this.height = height
        this.x = x
        this.y = y
        this.type = type
        this.state = state
        this.bufferedImage = bufferedImage
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
        bufferedImage: BufferedImage,
        frameMetadata: FrameMetadata
    ) {
        this.width = width
        this.height = height
        this.x = x
        this.y = y
        this.type = type
        this.state = state
        this.bufferedImage = bufferedImage
        this.frameMetadata = frameMetadata
        generateAnimationFrameIndex()
    }

    override fun getFirstDeactivatingFrame(): FrameMetadata {
        TODO("Not yet implemented")
    }

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

    override fun nestedDirection(): Direction {
        TODO("Not yet implemented")
    }

    private fun generateAnimationFrameIndex() {
        activeFrames[1] = FrameMetadata(1, Cell(1, 1, width, height))
        deactivatingFrames[1] = FrameMetadata(1, Cell(1, 1, width, height))
    }

    override fun from(state: MapItemState, nextFrameMetaData: FrameMetadata): MapFinishItem {
        return MapFinishItem(
            this.width,
            this.height,
            this.x,
            this.y,
            this.type,
            state,
            this.bufferedImage,
            nextFrameMetaData
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