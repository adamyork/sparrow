package com.github.adamyork.sparrow.game.engine

import com.github.adamyork.sparrow.common.GameStatusProvider
import com.github.adamyork.sparrow.game.Game
import com.github.adamyork.sparrow.game.data.Direction
import com.github.adamyork.sparrow.game.data.Player
import com.github.adamyork.sparrow.game.engine.data.Particle
import com.github.adamyork.sparrow.game.engine.data.PhysicsXResult
import com.github.adamyork.sparrow.game.engine.data.PhysicsYResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.util.function.Tuple2
import reactor.util.function.Tuples
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

class DefaultPhysics : Physics {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(DefaultPhysics::class.java)
        const val GRAVITY: Double = 20.0
        const val FRICTION: Double = 0.9
        const val Y_EASING_COEFFICIENT: Double = 0.1
        const val Y_VELOCITY_COEFFICIENT: Double = 0.5
        const val X_MOVEMENT_DISTANCE: Double = 1.0
        const val X_ACCELERATION_RATE: Double = 2.0
        const val X_DEACCELERATION_RATE: Double = 4.0
    }

    val gameStatusProvider: GameStatusProvider

    constructor(gameStatusProvider: GameStatusProvider) {
        this.gameStatusProvider = gameStatusProvider
    }

    override fun applyPlayerPhysics(player: Player): Player {
        val vx = getXVelocity(player.vx, player.moving)
        val vy = getYVelocity(player.y, player.jumpDy, player.vy, player.jumping)
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
            player.jumpDy,
            player.jumpReached
        )
        return Player(
            xResult.x,
            yResult.y,
            player.width,
            player.height,
            xResult.vx,
            yResult.vy,
            yResult.jumping,
            yResult.jumpDy,
            yResult.jumpReached,
            xResult.moving,
            player.direction,
            player.frameMetadata,
            player.colliding,
            yResult.scanVerticalCeiling,
            yResult.scanVerticalFloor
        )
    }

    @OptIn(ExperimentalAtomicApi::class)
    private fun getXVelocity(playerVx: Double, playerMoving: Boolean): Double {
        var nextVx: Double = playerVx
        if (playerMoving) {
            if (nextVx == 0.0) {
                nextVx = X_MOVEMENT_DISTANCE
            }
            nextVx = X_MOVEMENT_DISTANCE * (nextVx * X_ACCELERATION_RATE)
            nextVx *= FRICTION
            if (nextVx > Player.MAX_X_VELOCITY) {
                nextVx = Player.MAX_X_VELOCITY
            }
            //LOGGER.debug("incrementing player $nextVx")
        } else {
            if (nextVx > 0.0) {
                nextVx -= X_DEACCELERATION_RATE
                //LOGGER.debug("decrementing player $nextVx")
                if (nextVx < 0) {
                    nextVx = 0.0
                }
            }
        }
        //LOGGER.debug("player vx $nextVx")
        return nextVx
    }

    private fun getYVelocity(playerY: Int, playerJumpDy: Int, playerVy: Double, playerJumping: Boolean): Double {
        var nextVy: Double = playerVy
        if (playerJumping) {
            if (nextVy == 0.0) {
                nextVy += Player.JUMP_DISTANCE / 2
            } else if (nextVy < Player.MAX_Y_VELOCITY) {
                val distanceToTarget = playerY - playerJumpDy
                nextVy += (Y_VELOCITY_COEFFICIENT * nextVy) + (distanceToTarget * Y_EASING_COEFFICIENT)
                if (nextVy > Player.MAX_Y_VELOCITY) {
                    nextVy = Player.MAX_Y_VELOCITY
                }
            }
        } else {
            nextVy = 0.0
        }
        return nextVy
    }

    @OptIn(ExperimentalAtomicApi::class)
    private fun movePlayerX(
        playerWidth: Int,
        playerX: Int,
        playerVx: Double,
        playerMoving: Boolean,
        playerDirection: Direction
    ): PhysicsXResult {
        var targetX = playerX
        var deltaTime = (System.currentTimeMillis().toInt() - gameStatusProvider.lastPaintTime.load()) / 60
        if (deltaTime <= 0) {
            deltaTime = 1
        }
        //LOGGER.info("deltaTime: $deltaTime")
        if (playerMoving || playerVx != 0.0) {
            if (playerDirection == Direction.LEFT) {
                targetX -= (playerVx * deltaTime).roundToInt()
                if (targetX < 0) {
                    targetX = 0
                }
            } else {
                targetX += (playerVx * deltaTime).roundToInt()
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
        playerJumpDy: Int,
        playerJumpReached: Boolean
    ): PhysicsYResult {
        var destinationY = playerY + GRAVITY.roundToInt()
        var jumpDy = playerJumpDy
        var jumpReached = playerJumpReached
        var scanVerticalCeiling = false
        var scanVerticalFloor = false
        if (playerJumping) {
            if (jumpDy == 0 && !jumpReached) {
                LOGGER.info("starting a jump")
                jumpDy = destinationY - Player.JUMP_DISTANCE
                destinationY -= vy.roundToInt()
                if (destinationY < 0) {
                    scanVerticalCeiling = true
                    LOGGER.info("the initial jump destination is beyond the top of the viewport")
                    destinationY = 0
                    jumpDy = 0
                    jumpReached = true
                }
            } else if (destinationY > jumpDy && !jumpReached) {
                destinationY -= vy.roundToInt()
                if (destinationY <= 0) {
                    scanVerticalCeiling = true
                    LOGGER.info("the current jump destination is beyond the top of the viewport")
                    destinationY = 0
                    jumpDy = 0
                    jumpReached = true
                }
            } else if (destinationY <= jumpDy && !jumpReached) {
                LOGGER.info("the jump height has been reached")
                jumpReached = true
                jumpDy = 0
            }
        }
        if (destinationY > Game.VIEWPORT_HEIGHT - playerHeight) {
            scanVerticalFloor = true
            LOGGER.info("the destinationY is beyond the bottom of the viewport")
            destinationY = Game.VIEWPORT_HEIGHT - playerHeight
        }
        return PhysicsYResult(
            destinationY,
            vy,
            playerJumping,
            jumpDy,
            jumpReached,
            scanVerticalCeiling,
            scanVerticalFloor
        )
    }

    override fun applyCollisionParticlePhysics(mapParticles: ArrayList<Particle>): ArrayList<Particle> {
        return mapParticles.map { particle ->
            val nextFrame = particle.frame + 1
            var nextRadius = particle.radius
            var position = Tuples.of(particle.x.toFloat(), particle.y.toFloat())
            if (particle.radius < Particles.MAX_SQUARE_RADIAL_RADIUS) {
                nextRadius = particle.radius + 10
                position =
                    getCollisionParticlePosition(nextRadius.toFloat(), particle.id.toFloat(), particle.originX, particle.originY)
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

    private fun getCollisionParticlePosition(
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