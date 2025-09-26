package com.github.adamyork.socketgame.game.engine

import com.github.adamyork.socketgame.common.AudioQueue
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EngineConfig {

    @Bean
    @Qualifier("collisionAgnosticPhysics")
    fun collisionAgnosticPhysics(): CollisionAgnosticPhysics = CollisionAgnosticPhysics()

    @Bean
    @Qualifier("singlePassEngine")
    fun singlePassEngine(
        @Qualifier("collisionAgnosticPhysics")
        physics: Physics,
        collision: Collision,
        particles: Particles,
        audioQueue: AudioQueue
    ): Engine {
        return SinglePassEngine(physics, collision, particles, audioQueue)
    }

}