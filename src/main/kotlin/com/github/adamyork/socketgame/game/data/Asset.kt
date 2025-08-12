package com.github.adamyork.socketgame.game.data

import java.awt.image.BufferedImage

class Asset {
    val path: String
    val width: Int
    val height: Int

    lateinit var bufferedImage: BufferedImage

    constructor(path: String, width: Int, height: Int) {
        this.path = path
        this.width = width
        this.height = height
    }
}