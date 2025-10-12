package com.github.adamyork.socketgame.game.service

import com.github.adamyork.socketgame.game.data.MapItem
import com.github.adamyork.socketgame.game.data.MapItemState
import com.github.adamyork.socketgame.game.data.MapItemType
import org.springframework.stereotype.Service

@Service
class ScoreService {

    var gameMapItem: ArrayList<MapItem> = ArrayList()

    fun getTotal(): Int {
        return gameMapItem
            .filter { it.type == MapItemType.COLLECTABLE }
            .size
    }

    fun getRemaining(): Int {
        return gameMapItem
            .filter { it.type == MapItemType.COLLECTABLE }
            .count { it.state == MapItemState.ACTIVE }
    }

    fun allFound(): Boolean {
        return getRemaining() == 0
    }
}