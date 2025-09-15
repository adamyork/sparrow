package com.github.adamyork.socketgame.game.service

import com.github.adamyork.socketgame.game.data.MapItem
import com.github.adamyork.socketgame.game.data.MapItemState
import org.springframework.stereotype.Service

@Service
class ScoreService {

    var gameMapItem: ArrayList<MapItem> = ArrayList()

    fun getTotal(): Int {
        return gameMapItem.size
    }

    fun getRemaining(): Int {
        return gameMapItem.count { v ->
            v.state == MapItemState.ACTIVE
        }
    }
}