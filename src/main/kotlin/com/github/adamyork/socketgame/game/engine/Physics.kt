package com.github.adamyork.socketgame.game.engine

import com.github.adamyork.socketgame.game.data.GameMap
import com.github.adamyork.socketgame.game.data.Player
import com.github.adamyork.socketgame.game.engine.data.Particle
import com.github.adamyork.socketgame.game.service.data.Asset

interface Physics {

    fun applyPlayerPhysics(player: Player, gameMap: GameMap, collisionAsset: Asset): Player

    fun applyParticlePhysics(mapParticles: ArrayList<Particle>): ArrayList<Particle>

}