package com.github.adamyork.sparrow.game.service.v1

import com.github.adamyork.sparrow.game.data.item.MapItem
import com.github.adamyork.sparrow.game.data.item.MapItemState
import com.github.adamyork.sparrow.game.data.item.MapItemType
import com.github.adamyork.sparrow.game.service.ScoreService

class DefaultScoreService : ScoreService {

    override var gameMapItem: ArrayList<MapItem> = ArrayList()

    override fun getTotal(): Int {
        return gameMapItem
            .filter { it.type == MapItemType.COLLECTABLE }
            .size
    }

    override fun getRemaining(): Int {
        return gameMapItem
            .filter { it.type == MapItemType.COLLECTABLE }
            .count { it.state == MapItemState.ACTIVE }
    }

    override fun allFound(): Boolean {
        return getRemaining() == 0
    }
}