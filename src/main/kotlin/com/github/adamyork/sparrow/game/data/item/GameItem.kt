package com.github.adamyork.sparrow.game.data.item

import com.github.adamyork.sparrow.game.data.FrameMetadata
import java.awt.image.BufferedImage

interface GameItem {
    val type: MapItemType
    val frameMetadata: FrameMetadata
    val state: MapItemState
    val height: Int
    val width: Int
    val y: Int
    val x: Int
    val bufferedImage: BufferedImage

    fun getFirstDeactivatingFrame(): FrameMetadata

    fun from(state: MapItemState, nextFrameMetaData: FrameMetadata): GameItem

    fun from(x: Int, y: Int, state: MapItemState, frameMetadata: FrameMetadata): GameItem {
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