package com.github.adamyork.sparrow.game.engine

import com.github.adamyork.sparrow.game.data.Player
import com.github.adamyork.sparrow.game.engine.data.Particle

interface Physics {

    fun applyPlayerPhysics(player: Player): Player

    fun applyCollisionParticlePhysics(mapParticles: ArrayList<Particle>): ArrayList<Particle>

    fun applyDustParticlePhysics(mapParticles: ArrayList<Particle>): ArrayList<Particle>
}