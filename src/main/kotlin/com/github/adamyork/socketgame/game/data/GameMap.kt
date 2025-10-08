package com.github.adamyork.socketgame.game.data

import com.github.adamyork.socketgame.game.engine.data.Particle
import com.github.adamyork.socketgame.game.service.AssetService
import com.github.adamyork.socketgame.game.service.data.Asset

class GameMap {

    companion object {
        const val VIEWPORT_HORIZONTAL_ADVANCE_RATE: Int = 10
        const val VIEWPORT_VERTICAL_ADVANCE_RATE: Int = 128
        const val VIEWPORT_HORIZONTAL_FAR_PARALLAX_OFFSET: Int = 4
        const val VIEWPORT_HORIZONTAL_MID_PARALLAX_OFFSET: Int = 2
    }

    val farGroundAsset: Asset
    val midGroundAsset: Asset
    val nearFieldAsset: Asset
    val collisionAsset: Asset
    val x: Int
    val y: Int
    val width: Int
    val height: Int
    val items: ArrayList<MapItem>
    val enemies: ArrayList<MapEnemy>
    val particles: ArrayList<Particle>

    constructor(
        farGroundAsset: Asset,
        midGroundAsset: Asset,
        nearFieldAsset: Asset,
        collisionAsset: Asset,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        items: ArrayList<MapItem>,
        enemies: ArrayList<MapEnemy>,
        particles: ArrayList<Particle>
    ) {
        this.farGroundAsset = farGroundAsset
        this.midGroundAsset = midGroundAsset
        this.nearFieldAsset = nearFieldAsset
        this.collisionAsset = collisionAsset
        this.x = x
        this.y = y
        this.width = width
        this.height = height
        this.items = items
        this.enemies = enemies
        this.particles = particles
    }

    fun generateMapItems(collectibleItemAsset: Asset, finishItemAsset: Asset, assetService: AssetService) {
        for (i in 0..<assetService.getTotalItems()) {
            val itemType = MapItemType.from(assetService.getItemPosition(i).t3)
            if (itemType == MapItemType.FINISH) {
                items.add(
                    MapItem(
                        finishItemAsset.width,
                        finishItemAsset.height,
                        assetService.getItemPosition(i).t1,
                        assetService.getItemPosition(i).t2,
                        MapItemType.FINISH,
                        MapItemState.ACTIVE
                    )
                )
            } else {
                items.add(
                    MapItem(
                        collectibleItemAsset.width,
                        collectibleItemAsset.height,
                        assetService.getItemPosition(i).t1,
                        assetService.getItemPosition(i).t2,
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