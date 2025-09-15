package com.github.adamyork.socketgame.common

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentLinkedQueue

@Component
class AudioQueue {

    val queue: ConcurrentLinkedQueue<Sounds> = ConcurrentLinkedQueue()

}