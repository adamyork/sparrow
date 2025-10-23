package com.github.adamyork.sparrow.game.engine

import com.github.adamyork.sparrow.common.AudioQueue
import com.github.adamyork.sparrow.common.GameStatusProvider
import com.github.adamyork.sparrow.game.service.AssetService
import com.github.adamyork.sparrow.game.service.ScoreService
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
        scoreService: ScoreService,
        assetService: AssetService
    ): Engine {
        return DefaultEngine(physics, collision, particles, audioQueue, scoreService, assetService)
    }

}