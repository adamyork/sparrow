package com.github.adamyork.sparrow.game.data.enemy

import com.github.adamyork.sparrow.game.data.FrameMetadata
import com.github.adamyork.sparrow.game.data.Player
import com.github.adamyork.sparrow.game.data.ViewPort
import com.github.adamyork.sparrow.game.data.item.MapItemState
import java.awt.image.BufferedImage

interface GameEnemy {

    val width: Int
    val height: Int
    val x: Int
    val y: Int
    val type: MapEnemyType
    val state: MapItemState
    val bufferedImage: BufferedImage
    val frameMetadata: FrameMetadata
    val originX: Int
    val originY: Int
    val enemyPosition: EnemyPosition
    val colliding: Boolean
    val interacting: Boolean

    fun getNextPosition(player: Player, viewPort: ViewPort): EnemyPosition

    fun getNextEnemyState(player: Player): MapItemState

    fun from(frameMetadata: FrameMetadata, isColliding: Boolean, isInteracting: Boolean): GameEnemy

    fun from(
        x: Int,
        y: Int,
        state: MapItemState,
        frameMetadata: FrameMetadata,
        nextPosition: EnemyPosition,
        isColliding: Boolean,
        isInteracting: Boolean
    ): GameEnemy

}