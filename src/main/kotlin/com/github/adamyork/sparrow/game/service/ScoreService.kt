package com.github.adamyork.sparrow.game.service

import com.github.adamyork.sparrow.game.data.item.Item

interface ScoreService {

    var gameMapItem: ArrayList<Item>

    fun getTotal(): Int

    fun getRemaining(): Int

    fun allFound(): Boolean
}