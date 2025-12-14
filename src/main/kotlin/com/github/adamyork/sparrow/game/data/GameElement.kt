package com.github.adamyork.sparrow.game.data

import java.awt.image.BufferedImage

interface GameElement {

    val x: Int
    val y: Int
    val height: Int
    val width: Int
    val state: GameElementState
    val frameMetadata: FrameMetadata
    val bufferedImage: BufferedImage

    fun getNextFrameMetadataWithState(): Pair<FrameMetadata, FrameMetadataState>

    fun nestedDirection(): Direction

}