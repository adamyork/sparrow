package com.github.adamyork.sparrow.game.service.v1

import com.github.adamyork.sparrow.game.data.item.GameItem
import com.github.adamyork.sparrow.game.data.GameElementState
import com.github.adamyork.sparrow.game.data.item.MapItemType
import com.github.adamyork.sparrow.game.service.ScoreService

class DefaultScoreService : ScoreService {

    override var gameMapItem: ArrayList<GameItem> = ArrayList()

    override fun getTotal(): Int {
        return gameMapItem
            .filter { it.type == MapItemType.COLLECTABLE }
            .size
    }

    override fun getRemaining(): Int {
        return gameMapItem
            .filter { it.type == MapItemType.COLLECTABLE }
            .count { it.state == GameElementState.ACTIVE }
    }

    override fun allFound(): Boolean {
        return getRemaining() == 0
    }
}