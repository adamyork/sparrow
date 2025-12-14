package com.github.adamyork.sparrow.game.data.item

import com.github.adamyork.sparrow.game.data.FrameMetadata
import com.github.adamyork.sparrow.game.data.GameElement

interface GameItem : GameElement {

    val type: MapItemType

    fun getFirstDeactivatingFrame(): FrameMetadata

}