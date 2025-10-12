package com.github.adamyork.socketgame.game.engine

import com.github.adamyork.socketgame.game.data.Player
import com.github.adamyork.socketgame.game.engine.data.Particle

interface Physics {

    fun applyPlayerPhysics(player: Player): Player

    fun applyCollisionParticlePhysics(mapParticles: ArrayList<Particle>): ArrayList<Particle>

}