package com.github.adamyork.sparrow.game.data

import com.github.adamyork.sparrow.game.data.item.MapItemState
import java.awt.image.BufferedImage

interface GameElement {

    val x: Int
    val y: Int
    val height: Int
    val width: Int
    val state: MapItemState
    val frameMetadata: FrameMetadata
    val bufferedImage: BufferedImage

    fun getNextFrameCell(): FrameMetadata

    fun nestedDirection(): Direction

}