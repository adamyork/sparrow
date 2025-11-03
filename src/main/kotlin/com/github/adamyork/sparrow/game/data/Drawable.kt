package com.github.adamyork.sparrow.game.data

import com.github.adamyork.sparrow.game.data.item.MapItemState
import java.awt.image.BufferedImage

abstract class Drawable {

    var x: Int = 0
    var y: Int = 0
    var width: Int = 0
    var height: Int = 0
    var state: MapItemState = MapItemState.INACTIVE
    var frameMetadata: FrameMetadata = FrameMetadata(0, Cell(0, 0, 0, 0))
    var bufferedImage: BufferedImage = BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB)

    abstract fun nestedDirection(): Direction
}