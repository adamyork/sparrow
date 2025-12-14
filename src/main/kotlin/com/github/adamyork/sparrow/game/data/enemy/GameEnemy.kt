package com.github.adamyork.sparrow.game.data.enemy

import com.github.adamyork.sparrow.game.data.GameElement
import com.github.adamyork.sparrow.game.data.GameElementCollisionState
import com.github.adamyork.sparrow.game.data.GameElementState
import com.github.adamyork.sparrow.game.data.ViewPort
import com.github.adamyork.sparrow.game.data.player.Player

interface GameEnemy : GameElement {

    val type: MapEnemyType
    val originX: Int
    val originY: Int
    val enemyPosition: EnemyPosition
    val colliding: GameElementCollisionState
    val interacting: GameEnemyInteractionState

    fun getNextPosition(player: Player, viewPort: ViewPort): EnemyPosition

    fun getNextEnemyState(player: Player): GameElementState

}