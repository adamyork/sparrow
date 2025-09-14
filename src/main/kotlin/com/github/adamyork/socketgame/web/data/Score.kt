package com.github.adamyork.socketgame.web.data

class Score {

    val total: Int
    val remaining: Int

    constructor(total: Int, remaining: Int) {
        this.total = total
        this.remaining = remaining
    }

}