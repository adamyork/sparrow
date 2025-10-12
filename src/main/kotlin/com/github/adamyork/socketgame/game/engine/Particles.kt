package com.github.adamyork.socketgame.game.engine

import com.github.adamyork.socketgame.game.engine.data.Particle
import com.github.adamyork.socketgame.game.engine.data.ParticleType
import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class Particles {

    companion object {
        const val MAX_SQUARE_RADIAL_RADIUS: Int = 45
    }

    fun createCollisionParticles(originX: Int, originY: Int): ArrayList<Particle> {
        val particles: ArrayList<Particle> = ArrayList()
        for (i in 0..360) {
            val particle = Particle(
                i,
                originX,
                originY,
                originX,
                originY,
                2,
                2,
                ParticleType.SQUARE_RADIAL,
                0,
                20,
                Random.nextInt(50),
                Random.nextInt(50),
                1
            )
            particles.add(particle)
        }
        return particles
    }

    fun createFireworksParticles(originX: Int, originY: Int): ArrayList<Particle> {
        val particles: ArrayList<Particle> = ArrayList()
        for (i in 0..360) {
            val particle = Particle(
                i,
                originX,
                originY,
                originX,
                originY,
                2,
                2,
                ParticleType.CIRCLE,
                0,
                20,
                Random.nextInt(50),
                Random.nextInt(50),
                1
            )
            particles.add(particle)
        }
        return particles
    }

}