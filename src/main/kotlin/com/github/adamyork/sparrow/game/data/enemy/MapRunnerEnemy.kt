package com.github.adamyork.sparrow.game.data.enemy

import com.github.adamyork.sparrow.game.data.*
import com.github.adamyork.sparrow.game.data.player.Player
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.image.BufferedImage

data class MapRunnerEnemy(
    override val x: Int,
    override val y: Int,
    override val width: Int,
    override val height: Int,
    override val state: GameElementState,
    override val frameMetadata: FrameMetadata,
    override val bufferedImage: BufferedImage,
    override val type: MapEnemyType,
    override val originX: Int,
    override val originY: Int,
    override val enemyPosition: EnemyPosition,
    override val colliding: GameElementCollisionState,
    override val interacting: GameEnemyInteractionState
) : GameEnemy {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(MapShooterEnemy::class.java)
        const val MOVEMENT_X_DISTANCE = 10
        const val PLAYER_PROXIMITY_THRESHOLD = 200
        const val ANIMATION_INTERACTING_FRAMES = 8
    }

    var animatingFrames: HashMap<Int, FrameMetadata> = HashMap()
    var collisionFrames: HashMap<Int, FrameMetadata> = HashMap()
    var interactingFrames: HashMap<Int, FrameMetadata> = HashMap()

    init {
        generateAnimationFrameIndex()
    }

    override fun getNextEnemyState(player: Player): GameElementState {
        val withinXRange = player.x >= this.originX - PLAYER_PROXIMITY_THRESHOLD
        val withinYRange = player.y == this.originY - this.height
        val notAtEndOfPath = this.x > 0
        return if (withinXRange && withinYRange && notAtEndOfPath) {
            GameElementState.ACTIVE
        } else if (this.x == 0) {
            GameElementState.INACTIVE
        } else {
            this.state
        }
    }

    @Suppress("DuplicatedCode")
    private fun generateAnimationFrameIndex() {
        animatingFrames[1] = FrameMetadata(1, Cell(1, 1, width, height))

        interactingFrames[1] = FrameMetadata(1, Cell(1, 2, width, height))
        interactingFrames[2] = FrameMetadata(2, Cell(1, 2, width, height))
        interactingFrames[3] = FrameMetadata(3, Cell(1, 2, width, height))
        interactingFrames[4] = FrameMetadata(4, Cell(1, 2, width, height))
        interactingFrames[5] = FrameMetadata(5, Cell(1, 2, width, height))
        interactingFrames[6] = FrameMetadata(6, Cell(1, 2, width, height))
        interactingFrames[7] = FrameMetadata(7, Cell(1, 2, width, height))
        interactingFrames[8] = FrameMetadata(8, Cell(1, 2, width, height))

        collisionFrames[1] = FrameMetadata(1, Cell(1, 2, width, height))
        collisionFrames[2] = FrameMetadata(2, Cell(1, 2, width, height))
        collisionFrames[3] = FrameMetadata(3, Cell(1, 1, width, height))
        collisionFrames[4] = FrameMetadata(4, Cell(1, 1, width, height))
        collisionFrames[5] = FrameMetadata(5, Cell(1, 2, width, height))
        collisionFrames[6] = FrameMetadata(6, Cell(1, 2, width, height))
        collisionFrames[7] = FrameMetadata(7, Cell(1, 1, width, height))
        collisionFrames[8] = FrameMetadata(8, Cell(1, 1, width, height))
    }

    override fun getNextFrameMetadataWithState(): Pair<FrameMetadata, FrameMetadataState> {
        var metadata = animatingFrames[1] ?: throw RuntimeException("missing animation frame")
        var metadataState = FrameMetadataState(this.colliding, this.interacting, state)
        if (this.interacting == GameEnemyInteractionState.INTERACTING) {
            if (frameMetadata.frame == ANIMATION_INTERACTING_FRAMES) {
                metadataState = metadataState.copy(interacting = GameEnemyInteractionState.ISOLATED)
                return Pair(metadata, metadataState)
            } else {
                val nextFrame = frameMetadata.frame + 1
                metadata = interactingFrames[nextFrame] ?: throw RuntimeException("missing animation frame")
                return Pair(metadata, metadataState)
            }
        }
        return this.getNextCollisionMetadataWithState(animatingFrames, collisionFrames)
    }

    override fun nestedDirection(): Direction {
        return this.enemyPosition.direction
    }

    override fun getNextPosition(viewPort: ViewPort): EnemyPosition {
        return if (this.x >= 0) {
            EnemyPosition(
                enemyPosition.x - MOVEMENT_X_DISTANCE,
                enemyPosition.y,
                Direction.LEFT
            )
        } else {
            EnemyPosition(
                0,
                enemyPosition.y,
                Direction.LEFT
            )
        }
    }
}