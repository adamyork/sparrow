package com.github.adamyork.socketgame.game.data

import com.github.adamyork.socketgame.game.engine.data.Particle
import com.github.adamyork.socketgame.game.service.AssetService
import com.github.adamyork.socketgame.game.service.data.Asset

data class GameMap(
    val farGroundAsset: Asset,
    val midGroundAsset: Asset,
    val nearFieldAsset: Asset,
    val collisionAsset: Asset,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
    val items: ArrayList<MapItem>,
    val enemies: ArrayList<MapEnemy>,
    val particles: ArrayList<Particle>
) {

    companion object {
        const val VIEWPORT_HORIZONTAL_ADVANCE_RATE: Int = 10
        const val VIEWPORT_VERTICAL_ADVANCE_RATE: Int = 128
        const val VIEWPORT_HORIZONTAL_FAR_PARALLAX_OFFSET: Int = 4
        const val VIEWPORT_HORIZONTAL_MID_PARALLAX_OFFSET: Int = 2
    }

    fun generateMapItems(collectibleItemAsset: Asset, finishItemAsset: Asset, assetService: AssetService) {
        for (i in 0..<assetService.getTotalItems()) {
            val itemType = MapItemType.from(assetService.getItemPosition(i).type)
            if (itemType == MapItemType.FINISH) {
                items.add(
                    MapItem(
                        finishItemAsset.width,
                        finishItemAsset.height,
                        assetService.getItemPosition(i).x,
                        assetService.getItemPosition(i).y,
                        MapItemType.FINISH,
                        MapItemState.ACTIVE
                    )
                )
            } else {
                items.add(
                    MapItem(
                        collectibleItemAsset.width,
                        collectibleItemAsset.height,
                        assetService.getItemPosition(i).x,
                        assetService.getItemPosition(i).y,
                        MapItemType.COLLECTABLE,
                        MapItemState.ACTIVE
                    )
                )
            }
        }
    }

    fun generateMapEnemies(asset: Asset, assetService: AssetService) {
        for (i in 0..<assetService.getTotalEnemies()) {
            enemies.add(
                MapEnemy(
                    asset.width,
                    asset.height,
                    assetService.getEnemyPosition(i).first,
                    assetService.getEnemyPosition(i).second,
                    MapItemState.ACTIVE
                )
            )
        }
    }

}