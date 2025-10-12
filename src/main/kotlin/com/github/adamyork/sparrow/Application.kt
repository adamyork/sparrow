package com.github.adamyork.sparrow

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SocketGame

fun main(args: Array<String>) {
    runApplication<SocketGame>(*args)
}