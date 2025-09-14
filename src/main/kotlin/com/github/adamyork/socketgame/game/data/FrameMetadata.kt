package com.github.adamyork.socketgame.game.data

import reactor.util.function.Tuple2

data class FrameMetadata(val frame: Int, val cell: Tuple2<Int, Int>)