package com.github.adamyork.sparrow.game.service

import com.github.adamyork.sparrow.game.data.item.GameItem

interface ScoreService {

    var gameMapItem: ArrayList<GameItem>

    fun getTotal(): Int

    fun getRemaining(): Int

    fun allFound(): Boolean
}