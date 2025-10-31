package com.github.adamyork.sparrow.game.engine

import com.github.adamyork.sparrow.common.AudioQueue
import com.github.adamyork.sparrow.game.data.Player
import com.github.adamyork.sparrow.game.data.ViewPort
import com.github.adamyork.sparrow.game.data.map.GameMap
import com.github.adamyork.sparrow.game.engine.data.CollisionBoundaries
import java.awt.image.BufferedImage

interface Collision {

    var collisionImage: BufferedImage

    fun getCollisionBoundaries(player: Player): CollisionBoundaries

    fun recomputeXBoundaries(
        player: Player,
        previousBoundaries: CollisionBoundaries
    ): CollisionBoundaries

    fun checkForItemCollision(
        player: Player,
        gameMap: GameMap,
        audioQueue: AudioQueue
    ): GameMap

    fun checkForEnemyCollisionAndProximity(
        player: Player,
        gameMap: GameMap,
        viewPort: ViewPort,
        audioQueue: AudioQueue,
        particles: Particles
    ): Pair<Player, GameMap>

    fun checkForProjectileCollision(
        player: Player,
        gameMap: GameMap,
        viewPort: ViewPort,
        audioQueue: AudioQueue,
        particles: Particles
    ): Pair<Player, GameMap>

}