package com.github.adamyork.sparrow.game.engine

import com.github.adamyork.sparrow.game.data.Player
import com.github.adamyork.sparrow.game.data.enemy.MapEnemy
import com.github.adamyork.sparrow.game.engine.data.Particle

interface Particles {

    fun createCollisionParticles(originX: Int, originY: Int): ArrayList<Particle>

    fun createDustParticles(player: Player): ArrayList<Particle>

    fun createProjectileParticle(
        player: Player,
        enemy: MapEnemy,
        particles: ArrayList<Particle>
    ): ArrayList<Particle>

}