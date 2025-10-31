package com.github.adamyork.sparrow.game.service

import java.io.File

interface WavService {

    fun chunk(file: File, chunkMs: Int): HashMap<Int, ByteArray>

}