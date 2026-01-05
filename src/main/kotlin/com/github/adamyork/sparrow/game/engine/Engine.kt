package com.github.adamyork.sparrow.game.engine

import com.github.adamyork.sparrow.game.data.ViewPort
import com.github.adamyork.sparrow.game.data.map.GameMap
import com.github.adamyork.sparrow.game.data.player.Player
import com.github.adamyork.sparrow.game.engine.data.CollisionBoundaries
import com.github.adamyork.sparrow.game.service.data.ImageAsset

interface Engine {

    fun setCollisionBufferedImage(asset: ImageAsset)

    fun getCollisionBoundaries(player: Player): CollisionBoundaries

    fun managePlayer(player: Player, collisionBoundaries: CollisionBoundaries): Player

    fun manageViewport(player: Player, viewPort: ViewPort): ViewPort

    fun manageMap(player: Player, gameMap: GameMap): GameMap

    fun manageEnemyAndItemCollision(
        player: Player,
        map: GameMap,
        viewPort: ViewPort
    ): Pair<Player, GameMap>

    fun draw(
        map: GameMap,
        viewPort: ViewPort,
        player: Player
    ): ByteArray

}