package com.github.adamyork.sparrow.game.data.map

import com.github.adamyork.sparrow.game.data.*
import com.github.adamyork.sparrow.game.data.enemy.*
import com.github.adamyork.sparrow.game.data.item.GameItem
import com.github.adamyork.sparrow.game.data.item.MapCollectibleItem
import com.github.adamyork.sparrow.game.data.item.MapFinishItem
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
    var items: ArrayList<GameItem>,
    var enemies: ArrayList<GameEnemy>,
    var particles: ArrayList<Particle>
) {

    companion object {
        const val VIEWPORT_HORIZONTAL_FAR_PARALLAX_OFFSET: Int = 4
        const val VIEWPORT_HORIZONTAL_MID_PARALLAX_OFFSET: Int = 2
    }

    fun getFarGroundX(viewPort: ViewPort): Int {
        var x = viewPort.x / VIEWPORT_HORIZONTAL_FAR_PARALLAX_OFFSET
        if (x < 0 || x > viewPort.width) {
            x = viewPort.x
        }
        return x
    }

    fun getMidGroundX(viewPort: ViewPort): Int {
        var x = viewPort.x / VIEWPORT_HORIZONTAL_MID_PARALLAX_OFFSET
        if (x < 0 || x > viewPort.width) {
            x = viewPort.x
        }
        return x
    }

    fun generateMapItems(collectibleItemAsset: ImageAsset, finishItemAsset: ImageAsset, assetService: AssetService) {
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
                        GameElementState.INACTIVE,
                        finishItemAsset.bufferedImage,
                        FrameMetadata(1, Cell(1, 1, width, height)),
                        i
                    )
                )
            } else {
                items.add(
                    MapCollectibleItem(
                        collectibleItemAsset.width,
                        collectibleItemAsset.height,
                        assetService.getItemPosition(i).x,
                        assetService.getItemPosition(i).y,
                        MapItemType.COLLECTABLE,
                        GameElementState.ACTIVE,
                        collectibleItemAsset.bufferedImage,
                        FrameMetadata(1, Cell(1, 1, width, height)),
                        i
                    )
                )
            }
        }
    }

    fun generateMapEnemies(blockerEnemyAsset: ImageAsset, shooterEnemyAsset: ImageAsset, assetService: AssetService) {
        for (i in 0..<assetService.getTotalEnemies()) {
            val itemType = MapEnemyType.from(assetService.getEnemyPosition(i).type)
            when (itemType) {
                MapEnemyType.BLOCKER -> {
                    enemies.add(
                        MapBlockerEnemy(
                            assetService.getEnemyPosition(i).x,
                            assetService.getEnemyPosition(i).y,
                            blockerEnemyAsset.width,
                            blockerEnemyAsset.height,
                            GameElementState.ACTIVE,
                            FrameMetadata(1, Cell(1, 1, width, height)),
                            blockerEnemyAsset.bufferedImage,
                            MapEnemyType.BLOCKER,
                            assetService.getEnemyPosition(i).x,
                            assetService.getEnemyPosition(i).y,
                            EnemyPosition(
                                assetService.getEnemyPosition(i).x,
                                assetService.getEnemyPosition(i).y,
                                Direction.LEFT
                            ),
                            GameElementCollisionState.FREE,
                            GameEnemyInteractionState.ISOLATED
                        )
                    )
                }
                MapEnemyType.SHOOTER -> {
                    enemies.add(
                        MapShooterEnemy(
                            assetService.getEnemyPosition(i).x,
                            assetService.getEnemyPosition(i).y,
                            shooterEnemyAsset.width,
                            shooterEnemyAsset.height,
                            GameElementState.ACTIVE,
                            FrameMetadata(1, Cell(1, 1, width, height)),
                            shooterEnemyAsset.bufferedImage,
                            MapEnemyType.SHOOTER,
                            assetService.getEnemyPosition(i).x,
                            assetService.getEnemyPosition(i).y,
                            EnemyPosition(
                                assetService.getEnemyPosition(i).x,
                                assetService.getEnemyPosition(i).y,
                                Direction.LEFT
                            ),
                            GameElementCollisionState.FREE,
                            GameEnemyInteractionState.ISOLATED
                        )
                    )
                }
                else -> {
                    enemies.add(
                        MapRunnerEnemy(
                            assetService.getEnemyPosition(i).x,
                            assetService.getEnemyPosition(i).y,
                            shooterEnemyAsset.width,
                            shooterEnemyAsset.height,
                            GameElementState.INACTIVE,
                            FrameMetadata(1, Cell(1, 1, width, height)),
                            shooterEnemyAsset.bufferedImage,
                            MapEnemyType.RUNNER,
                            assetService.getEnemyPosition(i).x,
                            assetService.getEnemyPosition(i).y,
                            EnemyPosition(
                                assetService.getEnemyPosition(i).x,
                                assetService.getEnemyPosition(i).y,
                                Direction.LEFT
                            ),
                            GameElementCollisionState.FREE,
                            GameEnemyInteractionState.ISOLATED
                        )
                    )
                }
            }
        }
    }

    fun reset(
        collectibleItemAsset: ImageAsset,
        finishItemAsset: ImageAsset,
        blockerEnemyAsset: ImageAsset,
        shooterEnemyAsset: ImageAsset,
        assetService: AssetService
    ) {
        this.state = GameMapState.COLLECTING
        this.items = ArrayList()
        this.enemies = ArrayList()
        this.particles = ArrayList()
        generateMapItems(collectibleItemAsset, finishItemAsset, assetService)
        generateMapEnemies(blockerEnemyAsset, shooterEnemyAsset, assetService)
    }

}