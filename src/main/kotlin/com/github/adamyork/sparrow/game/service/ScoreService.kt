package com.github.adamyork.sparrow.game.service

import com.github.adamyork.sparrow.game.data.item.MapItem

interface ScoreService {

    var gameMapItem: ArrayList<MapItem>

    fun getTotal(): Int

    fun getRemaining(): Int

    fun allFound(): Boolean
}