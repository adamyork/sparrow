package com.github.adamyork.socketgame.game.engine

import com.github.adamyork.socketgame.game.Game
import com.github.adamyork.socketgame.game.data.Direction
import com.github.adamyork.socketgame.game.data.GameMap
import com.github.adamyork.socketgame.game.data.Player
import com.github.adamyork.socketgame.game.engine.data.Particle
import com.github.adamyork.socketgame.game.engine.data.PhysicsXResult
import com.github.adamyork.socketgame.game.engine.data.PhysicsYResult
import com.github.adamyork.socketgame.game.service.data.Asset
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.util.function.Tuple2
import reactor.util.function.Tuples
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

class CollisionAgnosticPhysics : Physics {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(CollisionAgnosticPhysics::class.java)
        const val GRAVITY: Double = 20.0
        const val FRICTION: Double = 0.1
        const val X_EASING_COEFFICIENT: Double = 1.0
        const val Y_EASING_COEFFICIENT: Double = 0.1
        const val VELOCITY_COEFFICIENT: Double = 0.5
    }

    @Suppress("DuplicatedCode")
    override fun applyPlayerPhysics(
        player: Player,
        gameMap: GameMap,
        collisionAsset: Asset
    ): Player {
        val vx = getXVelocity(player.vx, player.moving)
        val vy = getYVelocity(player.y, player.jumpY, player.vy, player.jumping)
        val xResult = movePlayerX(
            player.width,
            player.x,
            vx,
            player.moving,
            player.direction
        )
        val yResult = movePlayerY(
            player.height,
            player.y,
            vy,
            player.jumping,
            player.jumpY,
            player.jumpReached
        )
        return Player(
            xResult.x,
            yResult.y,
            xResult.vx,
            yResult.vy,
            yResult.jumping,
            yResult.jumpY,
            yResult.jumpReached,
            xResult.moving,
            player.direction,
            player.frameMetadata,
            player.colliding,
        )
    }

    @Suppress("DuplicatedCode")
    private fun getXVelocity(playerVx: Double, playerMoving: Boolean): Double {
        var nextVx: Double = playerVx
        if (playerMoving) {
            //LOGGER.debug("incrementing player velocity $vx")
            if (nextVx == 0.0) {
                nextVx += VELOCITY_COEFFICIENT
            } else if (nextVx < Player.MAX_X_VELOCITY) {
                nextVx += (VELOCITY_COEFFICIENT * playerVx)
                if (nextVx > Player.MAX_X_VELOCITY) {
                    nextVx = Player.MAX_X_VELOCITY
                }
            }
        } else {
            if (nextVx > 0.0) {
                //LOGGER.debug("decrementing player velocity $vx")
                nextVx -= (VELOCITY_COEFFICIENT * playerVx)
                if (nextVx < VELOCITY_COEFFICIENT) {
                    nextVx = 0.0
                }
            }
        }
        return nextVx
    }

    @Suppress("DuplicatedCode")
    private fun getYVelocity(playerY: Int, playerJumpY: Int, playerVy: Double, playerJumping: Boolean): Double {
        var nextVy: Double = playerVy
        if (playerJumping) {
            if (nextVy == 0.0) {
                nextVy += Player.JUMP_DISTANCE / 2
            } else if (nextVy < Player.MAX_Y_VELOCITY) {
                val distanceToTarget = playerY - playerJumpY
                nextVy += (VELOCITY_COEFFICIENT * nextVy) + (distanceToTarget * Y_EASING_COEFFICIENT)
                if (nextVy > Player.MAX_Y_VELOCITY) {
                    nextVy = Player.MAX_Y_VELOCITY
                }
            }
        } else {
            nextVy = 0.0
        }
        return nextVy
    }

    private fun movePlayerX(
        playerWidth: Int,
        playerX: Int,
        playerVx: Double,
        playerMoving: Boolean,
        playerDirection: Direction
    ): PhysicsXResult {
        var targetX = playerX
        if (playerMoving || playerVx != 0.0) {
            if (playerDirection == Direction.LEFT) {
                targetX -= ((X_EASING_COEFFICIENT * playerVx) + FRICTION).roundToInt()
                if (targetX < 0) {
                    targetX = 0
                }
            } else {
                targetX += ((X_EASING_COEFFICIENT * playerVx) - FRICTION).roundToInt()
                if (targetX >= Game.VIEWPORT_WIDTH - playerWidth) {
                    targetX = Game.VIEWPORT_WIDTH - playerWidth - 1
                }
            }
        }
        return PhysicsXResult(targetX, playerVx, playerMoving)
    }

    private fun movePlayerY(
        playerHeight: Int,
        playerY: Int,
        vy: Double,
        playerJumping: Boolean,
        playerJumpY: Int,
        playerJumpReached: Boolean
    ): PhysicsYResult {
        var destinationY = playerY + GRAVITY.roundToInt()
        var jumpY = playerJumpY
        var jumpReached = playerJumpReached
        if (playerJumping) {
            if (jumpY == 0 && !jumpReached) {
                LOGGER.info("starting a jump")
                jumpY = destinationY - Player.JUMP_DISTANCE
                destinationY -= vy.roundToInt()
                if (destinationY < 0) {
                    LOGGER.info("the jump destination is beyond the top of the viewport")
                    destinationY = 0
                    jumpY = 0
                    jumpReached = true
                }
            } else if (destinationY > jumpY && !jumpReached) {
                destinationY -= vy.roundToInt()
                if (destinationY <= 0) {
                    destinationY = 0
                    jumpY = 0
                    jumpReached = true
                }
            } else if (destinationY <= jumpY && !jumpReached) {
                LOGGER.info("the jump height has been reached")
                jumpReached = true
                jumpY = 0
            }
        }
        if (destinationY > Game.VIEWPORT_HEIGHT - playerHeight) {
            destinationY = Game.VIEWPORT_HEIGHT - playerHeight
        }
        return PhysicsYResult(destinationY, vy, playerJumping, jumpY, jumpReached)
    }

    @Suppress("DuplicatedCode")
    override fun applyParticlePhysics(mapParticles: ArrayList<Particle>): ArrayList<Particle> {
        return mapParticles.map { particle ->
            val nextFrame = particle.frame + 1
            var nextRadius = particle.radius
            var position = Tuples.of(particle.x.toFloat(), particle.y.toFloat())
            if (particle.radius < Particles.MAX_SQUARE_RADIAL_RADIUS) {
                nextRadius = particle.radius + 10
                position =
                    getParticlePosition(nextRadius.toFloat(), particle.id.toFloat(), particle.originX, particle.originY)
            } else {
                if (particle.frame <= particle.lifetime) {
                    position = Tuples.of(particle.x.toFloat(), particle.y.toFloat() + GRAVITY.toFloat())
                }
            }
            Particle(
                particle.id,
                position.t1.toInt() + particle.xJitter,
                position.t2.toInt() + particle.yJitter,
                particle.originX,
                particle.originY,
                particle.width,
                particle.height,
                particle.type,
                nextFrame,
                particle.lifetime,
                particle.xJitter,
                particle.yJitter,
                nextRadius
            )
        }.toCollection(ArrayList())
    }

    private fun getParticlePosition(
        radius: Float,
        angleInDegrees: Float,
        originX: Int,
        originY: Int
    ): Tuple2<Float, Float> {
        val x: Float = (radius * cos(angleInDegrees * Math.PI / 180f)).toFloat() + originX
        val y: Float = (radius * sin(angleInDegrees * Math.PI / 180f)).toFloat() + originY
        return Tuples.of(x, y)
    }
}