package com.github.adamyork.sparrow.game.engine

import com.github.adamyork.sparrow.game.data.enemy.GameEnemy
import com.github.adamyork.sparrow.game.data.player.Player
import com.github.adamyork.sparrow.game.engine.data.Particle

interface Particles {

    fun createCollisionParticles(originX: Int, originY: Int): ArrayList<Particle>

    fun createDustParticles(player: Player): ArrayList<Particle>

    fun createProjectileParticle(
        player: Player,
        enemy: GameEnemy,
        particles: ArrayList<Particle>
    ): Pair<ArrayList<Particle>, Boolean>

    fun createMapItemReturnParticle(player: Player): Particle

}