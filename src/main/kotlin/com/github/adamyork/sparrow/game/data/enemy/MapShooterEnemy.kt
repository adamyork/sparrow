package com.github.adamyork.sparrow.game.data.enemy

import com.github.adamyork.sparrow.game.data.Direction
import com.github.adamyork.sparrow.game.data.FrameMetadata
import com.github.adamyork.sparrow.game.data.Player
import com.github.adamyork.sparrow.game.data.ViewPort
import com.github.adamyork.sparrow.game.data.item.MapItemState
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MapShooterEnemy : MapEnemy {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(MapShooterEnemy::class.java)
        const val MOVEMENT_X_DISTANCE = 10
        const val PLAYER_PROXIMITY_THRESHOLD = 200
    }

    constructor(width: Int, height: Int, x: Int, y: Int, type: MapEnemyType, state: MapItemState) : super(
        width,
        height,
        x,
        y,
        type,
        state
    )

    constructor(
        width: Int,
        height: Int,
        x: Int,
        y: Int,
        originX: Int,
        originY: Int,
        type: MapEnemyType,
        state: MapItemState,
        frameMetadata: FrameMetadata,
        enemyPosition: EnemyPosition,
        colliding: Boolean
    ) : super(
        width,
        height,
        x,
        y,
        originX,
        originY,
        type,
        state,
        frameMetadata,
        enemyPosition,
        colliding
    )

    override fun getNextEnemyState(player: Player): MapItemState {
        return if (player.x >= this.originX - PLAYER_PROXIMITY_THRESHOLD) {
            MapItemState.ACTIVE
        } else {
            this.state
        }
    }

    override fun getNextPosition(player: Player, viewPort: ViewPort): EnemyPosition {
//        return if (this.x >= 0) {
//            EnemyPosition(
//                enemyPosition.x - MOVEMENT_X_DISTANCE,
//                enemyPosition.y,
//                Direction.LEFT
//            )
//        } else {
//            EnemyPosition(
//                1024 - this.width,
//                enemyPosition.y,
//                Direction.LEFT
//            )
//        }
        return EnemyPosition(
            1024 - this.width,
            enemyPosition.y,
            Direction.LEFT
        )
    }

    override fun from(
        frameMetadata: FrameMetadata,
        isColliding: Boolean
    ): MapEnemy {
        return MapShooterEnemy(
            this.width,
            this.height,
            this.x,
            this.y,
            this.originX,
            this.originY,
            this.type,
            this.state,
            frameMetadata,
            this.enemyPosition,
            isColliding
        )
    }


    override fun from(
        x: Int,
        y: Int,
        state: MapItemState,
        frameMetadata: FrameMetadata,
        nextPosition: EnemyPosition,
        isColliding: Boolean
    ): MapEnemy {
        return MapShooterEnemy(
            this.width,
            this.height,
            x,
            y,
            this.originX,
            this.originY,
            this.type,
            state,
            frameMetadata,
            nextPosition,
            isColliding
        )
    }
}