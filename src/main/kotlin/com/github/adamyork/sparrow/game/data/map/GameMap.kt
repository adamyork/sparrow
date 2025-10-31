package com.github.adamyork.sparrow.game.data.map

import com.github.adamyork.sparrow.game.data.enemy.MapEnemy
import com.github.adamyork.sparrow.game.data.enemy.MapEnemyType
import com.github.adamyork.sparrow.game.data.enemy.MapShooterEnemy
import com.github.adamyork.sparrow.game.data.item.MapFinishItem
import com.github.adamyork.sparrow.game.data.item.MapItem
import com.github.adamyork.sparrow.game.data.item.MapItemState
import com.github.adamyork.sparrow.game.data.item.MapItemType
import com.github.adamyork.sparrow.game.engine.data.Particle
import com.github.adamyork.sparrow.game.service.AssetService
import com.github.adamyork.sparrow.game.service.data.ImageAsset

data class GameMap(
    var state: GameMapState,
    val farGroundAsset: ImageAsset,
    val midGroundAsset: ImageAsset,
    val nearFieldAsset: ImageAsset,
    val collisionAsset: ImageAsset,
    val width: Int,
    val height: Int,
    var items: ArrayList<MapItem>,
    var enemies: ArrayList<MapEnemy>,
    var particles: ArrayList<Particle>
) {

    companion object {
        const val VIEWPORT_HORIZONTAL_FAR_PARALLAX_OFFSET: Int = 4
        const val VIEWPORT_HORIZONTAL_MID_PARALLAX_OFFSET: Int = 2
    }

    fun generateMapItems(greenieItemAsset: ImageAsset, finishItemAsset: ImageAsset, assetService: AssetService) {
        for (i in 0..<assetService.getTotalItems()) {
            val itemType = MapItemType.from(assetService.getItemPosition(i).type)
            if (itemType == MapItemType.FINISH) {
                items.add(
                    MapFinishItem(
                        finishItemAsset.width,
                        finishItemAsset.height,
                        assetService.getItemPosition(i).x,
                        assetService.getItemPosition(i).y,
                        MapItemType.FINISH,
                        MapItemState.INACTIVE
                    )
                )
            } else {
                items.add(
                    MapItem(
                        greenieItemAsset.width,
                        greenieItemAsset.height,
                        assetService.getItemPosition(i).x,
                        assetService.getItemPosition(i).y,
                        MapItemType.COLLECTABLE,
                        MapItemState.ACTIVE
                    )
                )
            }
        }
    }

    fun generateMapEnemies(vacuumEnemyAsset: ImageAsset, botEnemyAsset: ImageAsset, assetService: AssetService) {
        for (i in 0..<assetService.getTotalEnemies()) {
            val itemType = MapEnemyType.from(assetService.getEnemyPosition(i).type)
            if (itemType == MapEnemyType.VACUUM) {
                enemies.add(
                    MapEnemy(
                        vacuumEnemyAsset.width,
                        vacuumEnemyAsset.height,
                        assetService.getEnemyPosition(i).x,
                        assetService.getEnemyPosition(i).y,
                        MapEnemyType.VACUUM,
                        MapItemState.ACTIVE
                    )
                )
            } else {
                enemies.add(
                    MapShooterEnemy(
                        botEnemyAsset.width,
                        botEnemyAsset.height,
                        assetService.getEnemyPosition(i).x,
                        assetService.getEnemyPosition(i).y,
                        MapEnemyType.BOT,
                        MapItemState.INACTIVE
                    )
                )
            }
        }
    }

    fun reset(
        greenieItemAsset: ImageAsset,
        finishItemAsset: ImageAsset,
        vacuumEnemyAsset: ImageAsset,
        botEnemyAsset: ImageAsset,
        assetService: AssetService
    ) {
        this.state = GameMapState.COLLECTING
        this.items = ArrayList()
        this.enemies = ArrayList()
        this.particles = ArrayList()
        generateMapItems(greenieItemAsset, finishItemAsset, assetService)
        generateMapEnemies(vacuumEnemyAsset, botEnemyAsset, assetService)
    }

    fun from(managedMapEnemies: ArrayList<MapEnemy>, managedMapParticles: ArrayList<Particle>): GameMap {
        return GameMap(
            this.state,
            this.farGroundAsset,
            this.midGroundAsset,
            this.nearFieldAsset,
            this.collisionAsset,
            this.width,
            this.height,
            this.items,
            managedMapEnemies,
            managedMapParticles
        )
    }

    fun from(gameMapState: GameMapState, managedMapItems: ArrayList<MapItem>): GameMap {
        return GameMap(
            gameMapState,
            this.farGroundAsset,
            this.midGroundAsset,
            this.nearFieldAsset,
            this.collisionAsset,
            this.width,
            this.height,
            managedMapItems,
            this.enemies,
            this.particles
        )
    }

    fun from(
        gameMapState: GameMapState,
        managedMapItems: ArrayList<MapItem>,
        managedMapEnemies: ArrayList<MapEnemy>,
        managedMapParticles: ArrayList<Particle>
    ): GameMap {
        return GameMap(
            gameMapState,
            this.farGroundAsset,
            this.midGroundAsset,
            this.nearFieldAsset,
            this.collisionAsset,
            this.width,
            this.height,
            managedMapItems,
            managedMapEnemies,
            managedMapParticles
        )
    }

}