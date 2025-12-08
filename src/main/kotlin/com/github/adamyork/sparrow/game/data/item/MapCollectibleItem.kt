package com.github.adamyork.sparrow.game.data.item

import com.github.adamyork.sparrow.game.data.Cell
import com.github.adamyork.sparrow.game.data.Direction
import com.github.adamyork.sparrow.game.data.FrameMetadata
import com.github.adamyork.sparrow.game.data.GameElement
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.image.BufferedImage

class MapCollectibleItem : GameElement, GameItem {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(MapCollectibleItem::class.java)
        const val ANIMATION_DEACTIVATING_FRAMES = 4
        const val ANIMATION_ACTIVE_FRAMES = 16
    }

    override var width: Int
    override var height: Int
    override var x: Int
    override var y: Int
    override var type: MapItemType
    override var state: MapItemState
    override var bufferedImage: BufferedImage
    override var frameMetadata: FrameMetadata

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

    override fun getNextFrameCell(): FrameMetadata {
        if (state == MapItemState.DEACTIVATING) {
            if (frameMetadata.frame == ANIMATION_DEACTIVATING_FRAMES) {
                LOGGER.info("deactivating complete 0")
                this.state = MapItemState.INACTIVE
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
        return Direction.LEFT
    }

    override fun getFirstDeactivatingFrame(): FrameMetadata {
        return deactivatingFrames[1] ?: FrameMetadata(1, Cell(1, 1, width, height))
    }

    @Suppress("DuplicatedCode")
    private fun generateAnimationFrameIndex() {
        activeFrames[1] = FrameMetadata(1, Cell(1, 1, width, height))
        activeFrames[2] = FrameMetadata(2, Cell(1, 1, width, height))
        activeFrames[3] = FrameMetadata(3, Cell(1, 1, width, height))
        activeFrames[4] = FrameMetadata(4, Cell(1, 1, width, height))
        activeFrames[5] = FrameMetadata(5, Cell(1, 2, width, height))
        activeFrames[6] = FrameMetadata(6, Cell(1, 2, width, height))
        activeFrames[7] = FrameMetadata(7, Cell(1, 2, width, height))
        activeFrames[8] = FrameMetadata(8, Cell(1, 2, width, height))
        activeFrames[9] = FrameMetadata(9, Cell(1, 3, width, height))
        activeFrames[10] = FrameMetadata(10, Cell(1, 3, width, height))
        activeFrames[11] = FrameMetadata(11, Cell(1, 3, width, height))
        activeFrames[12] = FrameMetadata(12, Cell(1, 3, width, height))
        activeFrames[13] = FrameMetadata(13, Cell(1, 4, width, height))
        activeFrames[14] = FrameMetadata(14, Cell(1, 4, width, height))
        activeFrames[15] = FrameMetadata(15, Cell(1, 4, width, height))
        activeFrames[16] = FrameMetadata(16, Cell(1, 4, width, height))

        deactivatingFrames[1] = FrameMetadata(1, Cell(1, 5, width, height))
        deactivatingFrames[2] = FrameMetadata(2, Cell(1, 6, width, height))
        deactivatingFrames[3] = FrameMetadata(3, Cell(1, 7, width, height))
        deactivatingFrames[4] = FrameMetadata(4, Cell(1, 8, width, height))
    }

    override fun from(state: MapItemState, nextFrameMetaData: FrameMetadata): GameItem {
        return MapCollectibleItem(
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

    override fun from(x: Int, y: Int, state: MapItemState, frameMetadata: FrameMetadata): GameItem {
        return MapCollectibleItem(
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