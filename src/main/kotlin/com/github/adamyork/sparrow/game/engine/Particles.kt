package com.github.adamyork.sparrow.game.engine

import com.github.adamyork.sparrow.game.data.Direction
import com.github.adamyork.sparrow.game.data.Player
import com.github.adamyork.sparrow.game.engine.data.Particle
import com.github.adamyork.sparrow.game.engine.data.ParticleType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class Particles {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(Particles::class.java)
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
                ParticleType.COLLISION,
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

    fun createDustParticles(player: Player): ArrayList<Particle> {
        val offsets: HashMap<Int, Pair<Int, Int>> = HashMap()
        offsets[0] = Pair(8, 0)
        offsets[1] = Pair(1, 2)
        offsets[2] = Pair(10, 4)
        offsets[3] = Pair(20, 6)
        offsets[4] = Pair(36, 8)
        offsets[5] = Pair(44, 8)
        offsets[7] = Pair(40, 6)
        offsets[8] = Pair(35, 4)
        offsets[9] = Pair(21, 4)
        offsets[10] = Pair(19, 2)
        offsets[11] = Pair(29, 0)
        val particles: ArrayList<Particle> = ArrayList()
        var startX: Int
        var startY = player.y + player.height - (player.height / 5)
        for (i in 0..11) {
            if (player.direction == Direction.LEFT) {
                startX = player.x + player.width - (player.width / 3)
                startX += offsets[i]?.first ?: 0
            } else {
                startX = player.x + (player.width / 5)
                startX -= offsets[i]?.first ?: 0
            }
            startY -= offsets[i]?.second ?: 0
            val particle = Particle(
                i,
                startX,
                startY,
                player.x,
                player.y,
                (i * 3).coerceAtMost(30),
                (i * 3).coerceAtMost(30),
                ParticleType.DUST,
                0,
                5,
                0,
                0,
                255 - (i * 15)
            )
            particles.add(particle)
        }
        return particles
    }

}