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

    final val dustParticleOffsets: HashMap<Int, Pair<Int, Int>> = HashMap()

    constructor() {
        dustParticleOffsets[0] = Pair(8, 0)
        dustParticleOffsets[1] = Pair(1, 2)
        dustParticleOffsets[2] = Pair(10, 4)
        dustParticleOffsets[3] = Pair(20, 6)
        dustParticleOffsets[4] = Pair(36, 8)
        dustParticleOffsets[5] = Pair(44, 8)
        dustParticleOffsets[7] = Pair(40, 6)
        dustParticleOffsets[8] = Pair(35, 4)
        dustParticleOffsets[9] = Pair(21, 4)
        dustParticleOffsets[10] = Pair(19, 2)
        dustParticleOffsets[11] = Pair(29, 0)
    }

    fun createCollisionParticles(originX: Int, originY: Int): ArrayList<Particle> {
        val intRange = 0..360
        return intRange.toList().toIntArray().map {
            Particle(
                it,
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
        }.toCollection(ArrayList())
    }

    fun createDustParticles(player: Player): ArrayList<Particle> {
        var startX: Int
        var startY = player.y + player.height - (player.height / 16)
        val intRange = 0..11
        return intRange.toList().toIntArray().map {
            if (player.direction == Direction.LEFT) {
                startX = player.x + player.width - (player.width / 3)
                startX += dustParticleOffsets[it]?.first ?: 0
            } else {
                startX = player.x + (player.width / 5)
                startX -= dustParticleOffsets[it]?.first ?: 0
            }
            startY -= dustParticleOffsets[it]?.second ?: 0
            Particle(
                it,
                startX,
                startY,
                player.x,
                player.y,
                (it * 3).coerceAtMost(30),
                (it * 3).coerceAtMost(30),
                ParticleType.DUST,
                0,
                5,
                0,
                0,
                255 - (it * 15)
            )
        }.toCollection(ArrayList())
    }

}