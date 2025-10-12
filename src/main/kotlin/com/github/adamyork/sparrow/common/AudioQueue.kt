package com.github.adamyork.sparrow.common

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentLinkedQueue

@Component
class AudioQueue {

    val queue: ConcurrentLinkedQueue<Sounds> = ConcurrentLinkedQueue()

}