package com.github.adamyork.sparrow.game.service.v1

import com.github.adamyork.sparrow.game.data.item.Item
import com.github.adamyork.sparrow.game.data.GameElementState
import com.github.adamyork.sparrow.game.data.item.ItemType
import com.github.adamyork.sparrow.game.service.ScoreService

class DefaultScoreService : ScoreService {

    override var gameMapItem: ArrayList<Item> = ArrayList()

    override fun getTotal(): Int {
        return gameMapItem
            .filter { it.type == ItemType.COLLECTABLE }
            .size
    }

    override fun getRemaining(): Int {
        return gameMapItem
            .filter { it.type == ItemType.COLLECTABLE }
            .count { it.state == GameElementState.ACTIVE }
    }

    override fun allFound(): Boolean {
        return getRemaining() == 0
    }
}