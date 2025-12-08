package com.github.adamyork.sparrow.game.data

import com.github.adamyork.sparrow.game.data.item.MapItemState
import java.awt.image.BufferedImage

interface GameElement {

    val frameMetadata: FrameMetadata
    val state: MapItemState
    val height: Int
    val width: Int
    val bufferedImage: BufferedImage
    val x: Int
    val y: Int

    fun getNextFrameCell(): FrameMetadata

    fun nestedDirection(): Direction

}