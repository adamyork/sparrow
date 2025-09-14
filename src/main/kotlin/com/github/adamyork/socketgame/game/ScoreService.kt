package com.github.adamyork.socketgame.game

import com.github.adamyork.socketgame.game.data.MapItemState
import com.github.adamyork.socketgame.socket.GameHandler
import org.springframework.stereotype.Service

@Service
class ScoreService {

    lateinit var gameHandler: GameHandler

    fun getTotal(): Int {
        return gameHandler.game?.gameMap?.items?.size ?: 0
    }

    fun getRemaining(): Int {
        return gameHandler.game?.gameMap?.items?.count { v ->
            v.state == MapItemState.ACTIVE
        } ?: 0
    }
}