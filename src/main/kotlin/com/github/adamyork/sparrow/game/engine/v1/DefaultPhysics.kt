package com.github.adamyork.sparrow.game.engine.v1

import com.github.adamyork.sparrow.common.GameStatusProvider
import com.github.adamyork.sparrow.game.data.Direction
import com.github.adamyork.sparrow.game.data.Player
import com.github.adamyork.sparrow.game.data.Player.Companion.JUMP_DISTANCE
import com.github.adamyork.sparrow.game.engine.Collision
import com.github.adamyork.sparrow.game.engine.Physics
import com.github.adamyork.sparrow.game.engine.data.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

class DefaultPhysics : Physics {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(DefaultPhysics::class.java)
        const val GRAVITY: Double = 20.0
        const val FRICTION: Double = 0.9
        const val Y_VELOCITY_COEFFICIENT: Double = 0.5
        const val X_MOVEMENT_DISTANCE: Double = 1.0
        const val X_ACCELERATION_RATE: Double = 1.5
        const val X_DEACCELERATION_RATE: Double = 4.0
    }

    val gameStatusProvider: GameStatusProvider

    constructor(gameStatusProvider: GameStatusProvider) {
        this.gameStatusProvider = gameStatusProvider
    }

    override fun applyPlayerPhysics(
        player: Player,
        collisionBoundaries: CollisionBoundaries,
        collision: Collision
    ): Player {
        val vx = getXVelocity(player.vx, player.moving)
        val vy = getYVelocity(player.y, player.vy, player.jumping, collisionBoundaries)
        val yResult = movePlayerY(
            player.y,
            vy,
            player.jumping,
            collisionBoundaries
        )
        var adjustedCollisionBoundaries = collisionBoundaries
        if (player.y != yResult.y) {
            val nextPlayer = player.from(yResult)
            adjustedCollisionBoundaries =
                collision.recomputeXBoundaries(nextPlayer, collisionBoundaries)
        }
        val xResult = movePlayerX(
            player.x,
            vx,
            player.moving,
            player.direction,
            adjustedCollisionBoundaries
        )
        return player.from(xResult, yResult)
    }

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
        } else {
            if (nextVx > 0.0) {
                nextVx -= X_DEACCELERATION_RATE
                if (nextVx < 0) {
                    nextVx = 0.0
                }
            }
        }
        return nextVx
    }

    private fun getYVelocity(
        playerY: Int,
        playerVy: Double,
        playerJumping: Boolean,
        collisionBoundaries: CollisionBoundaries
    ): Double {
        val playerIsOnFloor = playerY == collisionBoundaries.bottom
        var nextVy: Double = playerVy
        if (playerJumping) {
            if (nextVy == 0.0 && playerIsOnFloor) {
                LOGGER.info("starting a jump")
                nextVy += JUMP_DISTANCE / 2
            } else if (nextVy < Player.MAX_Y_VELOCITY) {
                nextVy += (Y_VELOCITY_COEFFICIENT * nextVy)
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
        playerX: Int,
        playerVx: Double,
        playerMoving: Boolean,
        playerDirection: Direction,
        collisionBoundaries: CollisionBoundaries
    ): PhysicsXResult {
        var targetX = playerX
        val deltaTime = gameStatusProvider.getDeltaTime()
        if (playerMoving || playerVx != 0.0) {
            if (playerDirection == Direction.LEFT) {
                targetX -= (playerVx * deltaTime).roundToInt()
                if (targetX <= collisionBoundaries.left) {
                    LOGGER.info("targetX $targetX less or equal to the left boundary ${collisionBoundaries.left}")
                    LOGGER.info("(left) playerVx $playerVx and $deltaTime")
                    targetX = collisionBoundaries.left + 1
                }
            } else {
                targetX += (playerVx * deltaTime).roundToInt()
                if (targetX >= collisionBoundaries.right) {
                    LOGGER.info("targetX $targetX greater or equal to the right boundary ${collisionBoundaries.right}")
                    LOGGER.info("(right) playerVx $playerVx and $deltaTime")
                    targetX = (collisionBoundaries.right - 1).coerceAtLeast(0)
                }
            }
        }
        return PhysicsXResult(targetX, playerVx, playerMoving)
    }

    private fun movePlayerY(
        playerY: Int,
        vy: Double,
        playerJumping: Boolean,
        collisionBoundaries: CollisionBoundaries
    ): PhysicsYResult {
        var destinationY = playerY + GRAVITY.roundToInt()
        var nextPlayerJumping = playerJumping
        var nextPlayerVy = vy
        val playerIsOnFloor = playerY == collisionBoundaries.bottom
        if (nextPlayerJumping && !playerIsOnFloor && vy == 0.0) {
            LOGGER.info("double jump detected")
            nextPlayerJumping = false
        } else {
            val deltaTime = gameStatusProvider.getDeltaTime()
            destinationY -= (vy * deltaTime).roundToInt()
        }
        if (playerJumping) {
            val jumpBoundary = collisionBoundaries.bottom - JUMP_DISTANCE
            if (destinationY <= jumpBoundary) {
                LOGGER.info("jump height reached")
                nextPlayerJumping = false
                nextPlayerVy = 0.0
            }
        }
        if (destinationY > collisionBoundaries.bottom) {
            destinationY = collisionBoundaries.bottom
        } else if (destinationY < collisionBoundaries.top) {
            destinationY = collisionBoundaries.top + 1
            if (nextPlayerJumping) {
                LOGGER.info("jump height reached because of top of viewport")
                nextPlayerJumping = false
                nextPlayerVy = 0.0
            }
        }
        return PhysicsYResult(
            destinationY,
            nextPlayerVy,
            nextPlayerJumping
        )
    }

    override fun applyCollisionParticlePhysics(mapParticles: ArrayList<Particle>): ArrayList<Particle> {
        return mapParticles
            .map { particle ->
                if (particle.type == ParticleType.COLLISION) {
                    val nextFrame = particle.frame + 1
                    var nextRadius = particle.radius
                    var position = Pair(particle.x.toFloat(), particle.y.toFloat())
                    if (particle.radius < DefaultParticles.MAX_SQUARE_RADIAL_RADIUS) {
                        nextRadius = particle.radius + 10
                        position =
                            getCollisionParticlePosition(
                                nextRadius.toFloat(),
                                particle.id.toFloat(),
                                particle.originX,
                                particle.originY
                            )
                    } else {
                        if (particle.frame <= particle.lifetime) {
                            position = Pair(particle.x.toFloat(), particle.y.toFloat() + GRAVITY.toFloat())
                        }
                    }
                    particle.from(position, nextFrame, nextRadius)
                } else {
                    particle
                }
            }.filter { particle -> particle.frame <= particle.lifetime }
            .toCollection(ArrayList())
    }

    override fun applyDustParticlePhysics(mapParticles: ArrayList<Particle>): ArrayList<Particle> {
        return mapParticles
            .map { particle ->
                if (particle.type == ParticleType.DUST) {
                    val nextFrame = particle.frame + 1
                    val nextWidth = (particle.width + 1).coerceAtMost(40)
                    val nextHeight = (particle.height + 1).coerceAtMost(40)
                    val nextRadius = (particle.radius - 15).coerceAtLeast(0)
                    particle.from(nextWidth, nextHeight, nextFrame, nextRadius)
                } else {
                    particle
                }
            }.filter { particle -> particle.frame <= particle.lifetime }
            .toCollection(ArrayList())
    }

    override fun applyProjectileParticlePhysics(mapParticles: ArrayList<Particle>): ArrayList<Particle> {
        return mapParticles
            .map { particle ->
                if (particle.type == ParticleType.FURBALL) {
                    val nextFrame = particle.frame + 1
                    val nextX: Int = if (particle.originX == particle.x) {
                        particle.x
                    } else if (particle.originX < particle.x) {
                        particle.x - particle.xJitter
                    } else {
                        particle.x + particle.xJitter
                    }
                    val nextY: Int = if (particle.originY == particle.y) {
                        particle.y
                    } else if (particle.originY < particle.y) {
                        particle.y - particle.yJitter
                    } else {
                        particle.y + particle.yJitter
                    }
                    particle.from(nextX, nextY, nextFrame)
                } else {
                    particle
                }
            }.filter { particle -> particle.frame <= particle.lifetime }
            .toCollection(ArrayList())
    }

    private fun getCollisionParticlePosition(
        radius: Float,
        angleInDegrees: Float,
        originX: Int,
        originY: Int
    ): Pair<Float, Float> {
        val x: Float = (radius * cos(angleInDegrees * Math.PI / 180f)).toFloat() + originX
        val y: Float = (radius * sin(angleInDegrees * Math.PI / 180f)).toFloat() + originY
        return Pair(x, y)
    }
}