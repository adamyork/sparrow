package com.github.adamyork.sparrow.game.engine

import com.github.adamyork.sparrow.game.data.Player
import com.github.adamyork.sparrow.game.data.ViewPort
import com.github.adamyork.sparrow.game.data.map.GameMap
import com.github.adamyork.sparrow.game.engine.data.CollisionBoundaries
import com.github.adamyork.sparrow.game.service.data.ImageAsset

interface Engine {

    fun setCollisionBufferedImage(asset: ImageAsset)

    fun getCollisionBoundaries(
        player: Player,
        collisionAsset: ImageAsset
    ): CollisionBoundaries

    fun managePlayer(player: Player, collisionBoundaries: CollisionBoundaries): Player

    fun manageViewport(player: Player, viewPort: ViewPort): ViewPort

    fun manageMap(player: Player, gameMap: GameMap, viewPort: ViewPort): GameMap

    fun manageEnemyAndItemCollision(
        player: Player,
        map: GameMap,
        viewPort: ViewPort,
        collisionAsset: ImageAsset
    ): Pair<Player, GameMap>

    fun draw(
        map: GameMap,
        viewPort: ViewPort,
        playerAsset: ImageAsset,
        player: Player,
        mapItemAsset: ImageAsset,
        finishItemAsset: ImageAsset,
        mapEnemyVacuumAsset: ImageAsset,
        mapEnemyBotAsset: ImageAsset
    ): ByteArray

}