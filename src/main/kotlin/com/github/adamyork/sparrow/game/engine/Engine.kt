package com.github.adamyork.sparrow.game.engine

import com.github.adamyork.sparrow.game.data.GameMap
import com.github.adamyork.sparrow.game.data.Player
import com.github.adamyork.sparrow.game.data.ViewPort
import com.github.adamyork.sparrow.game.engine.data.CollisionBoundaries
import com.github.adamyork.sparrow.game.engine.data.PlayerMapPair
import com.github.adamyork.sparrow.game.service.data.Asset

interface Engine {

    fun setCollisionBufferedImage(asset: Asset)

    fun getCollisionBoundaries(
        player: Player,
        collisionAsset: Asset
    ): CollisionBoundaries

    fun managePlayer(player: Player, collisionBoundaries: CollisionBoundaries): Player

    fun manageViewport(player: Player, viewPort: ViewPort): ViewPort

    fun manageMap(player: Player, gameMap: GameMap): GameMap

    fun manageEnemyAndItemCollision(
        player: Player,
        map: GameMap,
        viewPort: ViewPort,
        collisionAsset: Asset
    ): PlayerMapPair

    fun paint(
        map: GameMap,
        viewPort: ViewPort,
        playerAsset: Asset,
        player: Player,
        mapItemAsset: Asset,
        finishItemAsset: Asset,
        mapEnemyAsset: Asset
    ): ByteArray

}