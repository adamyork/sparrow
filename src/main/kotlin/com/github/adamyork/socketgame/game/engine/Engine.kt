package com.github.adamyork.socketgame.game.engine

import com.github.adamyork.socketgame.game.data.GameMap
import com.github.adamyork.socketgame.game.data.Player
import com.github.adamyork.socketgame.game.engine.data.PlayerMapPair
import com.github.adamyork.socketgame.game.service.data.Asset

interface Engine {

    fun managePlayer(player: Player): Player

    fun manageMap(player: Player, gameMap: GameMap): GameMap

    fun manageCollision(
        player: Player,
        previousX: Int,
        previousY: Int,
        map: GameMap,
        collisionAsset: Asset
    ): PlayerMapPair

    fun paint(
        map: GameMap,
        playerAsset: Asset,
        player: Player,
        mapItemAsset: Asset,
        finishItemAsset: Asset,
        mapEnemyAsset: Asset
    ): ByteArray

}