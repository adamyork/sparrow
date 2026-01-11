package com.github.adamyork.sparrow.common

import com.github.adamyork.sparrow.common.data.Sounds
import java.util.concurrent.ConcurrentLinkedQueue

class AudioQueue {

    val queue: ConcurrentLinkedQueue<Sounds> = ConcurrentLinkedQueue()

}