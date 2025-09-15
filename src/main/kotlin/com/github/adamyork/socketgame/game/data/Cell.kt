package com.github.adamyork.socketgame.game.data

class Cell {

    val xOffset: Int
    val yOffset: Int

    constructor(row: Int, column: Int, width: Int, height: Int) {
        xOffset = (column - 1) * width
        yOffset = (row - 1) * height
    }
}