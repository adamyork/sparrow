package com.github.adamyork.socketgame.game.engine

import com.github.adamyork.socketgame.common.AudioQueue
import com.github.adamyork.socketgame.common.GameStatusProvider
import com.github.adamyork.socketgame.game.service.ScoreService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EngineConfig {

    @Bean
    fun physics(gameStatusProvider: GameStatusProvider): DefaultPhysics = DefaultPhysics(gameStatusProvider)

    @Bean
    fun engine(
        physics: Physics,
        collision: Collision,
        particles: Particles,
        audioQueue: AudioQueue,
        scoreService: ScoreService
    ): Engine {
        return DefaultEngine(physics, collision, particles, audioQueue, scoreService)
    }

}