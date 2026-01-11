package com.github.adamyork.sparrow.common

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CommonConfig {

    @Bean
    fun audioQueue(): AudioQueue {
        return AudioQueue()
    }

    @Bean
    fun statusProvider(
        @Value("\${engine.fps.max}") fpsMax: Int,
        @Value("\${engine.fps.min}") fpsMin: Int
    ): StatusProvider {
        return StatusProvider(fpsMax, fpsMin)
    }
}