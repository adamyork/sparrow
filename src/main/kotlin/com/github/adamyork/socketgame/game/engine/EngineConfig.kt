package com.github.adamyork.socketgame.game.engine

import com.github.adamyork.socketgame.common.AudioQueue
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EngineConfig {

    @Bean
    fun physics(): DefaultPhysics = DefaultPhysics()

    @Bean
    fun engine(
        physics: Physics,
        collision: Collision,
        particles: Particles,
        audioQueue: AudioQueue
    ): Engine {
        return DefaultEngine(physics, collision, particles, audioQueue)
    }

}