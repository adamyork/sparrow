package com.github.adamyork.sparrow.game.data.enemy

import com.github.adamyork.sparrow.game.data.Player
import com.github.adamyork.sparrow.game.data.ViewPort
import com.github.adamyork.sparrow.game.data.item.MapItemState

interface GameEnemy {

    val type: MapEnemyType
    val originX: Int
    val originY: Int
    val enemyPosition: EnemyPosition
    val colliding: Boolean
    val interacting: Boolean

    fun getNextPosition(player: Player, viewPort: ViewPort): EnemyPosition

    fun getNextEnemyState(player: Player): MapItemState

}