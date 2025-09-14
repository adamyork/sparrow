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
import org.springframework.stereotype.Component
import reactor.util.function.Tuple2
import reactor.util.function.Tuples
import java.awt.image.BufferedImage
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin


@Component
class Physics {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(Physics::class.java)
        const val GRAVITY: Double = 20.0
        const val FRICTION: Double = 0.1
        const val X_EASING_COEFFICIENT: Double = 1.0
        const val Y_EASING_COEFFICIENT: Double = 0.1
        const val VELOCITY_COEFFICIENT: Double = 0.5
        const val COLLISION_COLOR_VALUE: Int = -16711906
    }

    fun applyPlayerPhysics(player: Player, gameMap: GameMap, collisionAsset: Asset): Player {
        val currentCollisionArea = collisionAsset.bufferedImage.getSubimage(gameMap.x, gameMap.y, 1024, 768)
        var nextPlayer = player
        if (gameMap.moved) {
            nextPlayer = handleCollisionAfterMapMove(player, currentCollisionArea)
        }
        val vx = getXVelocity(nextPlayer.vx, player.moving)
        val vy = getYVelocity(nextPlayer.y, nextPlayer.jumpY, player.vy, player.jumping)
        val xResult = movePlayerX(
            nextPlayer.width,
            nextPlayer.height.coerceAtMost(Game.VIEWPORT_HEIGHT - player.y),
            nextPlayer.x,
            nextPlayer.y,
            vx,
            nextPlayer.moving,
            nextPlayer.direction,
            currentCollisionArea,
            nextPlayer.colliding
        )
        val yResult = movePlayerY(
            nextPlayer.width,
            nextPlayer.height,
            xResult.x,
            nextPlayer.y,
            vy,
            nextPlayer.jumping,
            nextPlayer.jumpY,
            nextPlayer.jumpReached,
            currentCollisionArea
        )
        return Player(
            xResult.x,
            yResult.y,
            xResult.vx,
            nextPlayer.vy,
            yResult.jumping,
            yResult.jumpY,
            yResult.jumpReached,
            xResult.moving,
            nextPlayer.direction,
            nextPlayer.frameMetadata,
            nextPlayer.colliding,
        )
    }

    private fun movePlayerY(
        playerWidth: Int,
        playerHeight: Int,
        playerX: Int,
        playerY: Int,
        vy: Double,
        playerJumping: Boolean,
        playerJumpY: Int,
        playerJumpReached: Boolean,
        collisionImage: BufferedImage
    ): PhysicsYResult {
        var destinationY = playerY + GRAVITY.roundToInt()
        var jumping = playerJumping
        var jumpY = playerJumpY
        var jumpReached = playerJumpReached
        var collisionBitMap: IntArray
        val xBoundary: Int = playerX + 1
        var yBoundary: Int
        val boundaryWidth: Int = playerWidth - 2
        val boundaryHeight = 1
        if (jumping) {
            if (jumpY == 0 && !jumpReached) {
                LOGGER.info("starting a jump")
                jumpY = destinationY - Player.JUMP_DISTANCE
                destinationY -= vy.roundToInt()
                if (destinationY < 0) {
                    LOGGER.info("the jump destination is beyond the top of the viewport")
                    destinationY = 0
                    jumpY = 0
                    jumpReached = true
                } else {
                    LOGGER.info("scanning the entire vertical stride of the initial jump")
                    collisionBitMap =
                        collisionImage.getRGB(
                            xBoundary,
                            destinationY,
                            boundaryWidth,
                            playerY - destinationY,
                            null,
                            0,
                            64
                        )
                    if (collisionBitMap.contains(COLLISION_COLOR_VALUE)) {
                        LOGGER.info("theres a vertical collision in the stride of the initial jump")
                        val ceiling =
                            findCeilingAscending(xBoundary, boundaryWidth, 1, playerHeight, collisionImage, playerY)
                        val dist = ceiling - jumpY
                        if (dist > vy.roundToInt()) {
                            jumpY = ceiling + 1
                            destinationY = ceiling + 1
                            jumpReached = true
                        }
                    }
                }
            } else if (destinationY > jumpY && !jumpReached) {
                destinationY -= vy.roundToInt()
                if (destinationY <= 0) {
                    destinationY = 0
                    jumpY = 0
                    jumpReached = true
                    yBoundary = 0
                } else {
                    yBoundary = destinationY
                }
                collisionBitMap = collisionImage.getRGB(
                    xBoundary,
                    yBoundary,
                    boundaryWidth,
                    boundaryHeight,
                    null,
                    0,
                    64
                )
                LOGGER.info("player is jumping and rising rect is x=$xBoundary, y=$yBoundary boundaryWidth=$boundaryWidth, boundaryHeight=$boundaryHeight")
                if (collisionBitMap.contains(COLLISION_COLOR_VALUE)) {
                    LOGGER.info("collision while jumping")
                    destinationY =
                        findCeilingDescending(
                            xBoundary,
                            boundaryWidth,
                            boundaryHeight,
                            playerHeight,
                            collisionImage,
                            yBoundary
                        )
                    jumpY = destinationY
                    jumpReached = true
                }
                //check for ceiling
            } else if (destinationY <= jumpY && !jumpReached) {
                LOGGER.info("the jump height has been reached")
                jumpReached = true
                jumpY = 0
            }
        }
        yBoundary = (destinationY + playerHeight).coerceAtMost(Game.VIEWPORT_HEIGHT - 1)
        LOGGER.debug("checking for floor Y Boundary: $yBoundary")
        collisionBitMap =
            collisionImage.getRGB(xBoundary, yBoundary, boundaryWidth, boundaryHeight, null, 0, 64)
        if (collisionBitMap.contains(COLLISION_COLOR_VALUE)) {
            LOGGER.debug("Vertical Collision Detected")
            destinationY = findFloor(xBoundary, boundaryWidth, boundaryHeight, playerHeight, collisionImage, yBoundary)
            if (jumping) {
                LOGGER.info("jump completed")
                jumpY = 0
                jumping = false
                jumpReached = false
            }
        }
        if (destinationY > Game.VIEWPORT_HEIGHT - playerHeight) {
            destinationY = Game.VIEWPORT_HEIGHT - playerHeight
        }
        return PhysicsYResult(destinationY, vy, jumping, jumpY, jumpReached)
    }

    private fun findFloor(
        xBoundary: Int,
        boundaryWidth: Int,
        boundaryHeight: Int,
        playerHeight: Int,
        collisionImage: BufferedImage,
        lastY: Int
    ): Int {
        val nextY = lastY - 1
        val collisionBitMap =
            collisionImage.getRGB(xBoundary, nextY, boundaryWidth, boundaryHeight, null, 0, 64)
        if (collisionBitMap.contains(COLLISION_COLOR_VALUE)) {
            return findFloor(xBoundary, boundaryWidth, boundaryHeight, playerHeight, collisionImage, nextY)
        }
        return nextY - playerHeight
    }

    private fun findCeilingDescending(
        xBoundary: Int,
        boundaryWidth: Int,
        boundaryHeight: Int,
        playerHeight: Int,
        collisionImage: BufferedImage,
        lastY: Int
    ): Int {
        val nextY = lastY + 1
        val collisionBitMap =
            collisionImage.getRGB(xBoundary, nextY, boundaryWidth, boundaryHeight, null, 0, 64)
        if (collisionBitMap.contains(COLLISION_COLOR_VALUE)) {
            return findCeilingDescending(xBoundary, boundaryWidth, boundaryHeight, playerHeight, collisionImage, nextY)
        }
        return nextY
    }

    private fun findCeilingAscending(
        xBoundary: Int,
        boundaryWidth: Int,
        boundaryHeight: Int,
        playerHeight: Int,
        collisionImage: BufferedImage,
        lastY: Int
    ): Int {
        val nextY = lastY - 1
        val collisionBitMap =
            collisionImage.getRGB(xBoundary, nextY, boundaryWidth, boundaryHeight, null, 0, 64)
        if (collisionBitMap.contains(COLLISION_COLOR_VALUE)) {
            return nextY
        }
        return findCeilingAscending(xBoundary, boundaryWidth, boundaryHeight, playerHeight, collisionImage, nextY)
    }

    fun movePlayerX(
        playerWidth: Int,
        playerHeight: Int,
        playerX: Int,
        playerY: Int,
        playerVx: Double,
        playerMoving: Boolean,
        playerDirection: Direction,
        collisionImage: BufferedImage,
        colliding: Boolean
    ): PhysicsXResult {
        var targetX = playerX
        var xBoundary = playerX
        var nextVx = playerVx
        var collisionRebound = 0
        if (colliding) {
            collisionRebound = playerWidth
            if (playerDirection == Direction.LEFT) {
                targetX += collisionRebound
                if (targetX >= Game.VIEWPORT_WIDTH - playerWidth) {
                    targetX = Game.VIEWPORT_WIDTH - playerWidth - 1
                }
            } else {
                targetX -= collisionRebound
                if (targetX < 0) {
                    targetX = 0
                }
            }
            nextVx = 0.0
        }
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
                xBoundary = targetX + playerWidth
            }
        }
        //LOGGER.info("xBoundary = $xBoundary playerY = $playerY ")
        val collisionBitMap =
            collisionImage.getRGB(xBoundary, playerY, 1, playerHeight, null, 0, 64)
        if (collisionBitMap.contains(COLLISION_COLOR_VALUE)) {
            targetX = findEdge(playerY, 1, playerHeight, collisionImage, playerDirection, targetX)
            nextVx = 0.0
        }
        return PhysicsXResult(targetX, nextVx, playerMoving)
    }

    private fun findEdge(
        yBoundary: Int,
        boundaryWidth: Int,
        boundaryHeight: Int,
        collisionImage: BufferedImage,
        playerDirection: Direction,
        lastX: Int
    ): Int {
        var nextX = lastX
        var xBoundary = nextX - 1
        if (playerDirection == Direction.LEFT) {
            nextX += 1
        } else {
            nextX -= 1
            xBoundary = nextX + 64 + 1
        }
        if (xBoundary == 0 || xBoundary == Game.VIEWPORT_WIDTH) {
            LOGGER.info("cant find an edge. giving up")
            return -1
        }
        val collisionBitMap =
            collisionImage.getRGB(xBoundary, yBoundary, boundaryWidth, boundaryHeight, null, 0, 64)
        if (collisionBitMap.contains(COLLISION_COLOR_VALUE)) {
            return findEdge(yBoundary, boundaryWidth, boundaryHeight, collisionImage, playerDirection, nextX)
        }
        return nextX
    }

    fun getXVelocity(playerVx: Double, playerMoving: Boolean): Double {
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

    fun getYVelocity(playerY: Int, playerJumpY: Int, playerVy: Double, playerJumping: Boolean): Double {
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

    fun handleCollisionAfterMapMove(player: Player, currentCollisionArea: BufferedImage): Player {
        var playerHeight = player.height.coerceAtMost(Game.VIEWPORT_HEIGHT - player.y)
        if (playerHeight <= 0) {
            playerHeight = 1
        }
        var playerX = player.x
        var playerVx = player.vx
        var playerY = player.y
        var jumpY = player.jumpY
        //LOGGER.info("player.x = ${player.x}, player.y = ${player.y} player.width = ${player.width} playerHeight = $playerHeight")
        val collisionBitMap = currentCollisionArea.getRGB(
            player.x,
            player.y,
            player.width,
            playerHeight,
            null,
            0,
            64
        )
        if (collisionBitMap.contains(COLLISION_COLOR_VALUE)) {
            LOGGER.info("Mapped was moved a player is colliding now")
            if (player.moving) {
                val nextX = findEdge(playerY, 1, playerHeight, currentCollisionArea, player.direction, playerX)
                if (nextX != -1) {
                    playerX = nextX
                }
                playerVx = 0.0
            }
            if (player.jumping) {
                playerY =
                    findCeilingDescending(playerX, player.width, 1, player.height, currentCollisionArea, player.y)
                jumpY = playerY
                return Player(
                    playerX,
                    playerY,
                    playerVx,
                    player.vy,
                    player.jumping,
                    jumpY,
                    true,
                    player.moving,
                    player.direction,
                    player.frameMetadata,
                    player.colliding
                )
            }
            val boundaryY = (player.y + playerHeight).coerceAtMost(Game.VIEWPORT_HEIGHT - 1)
            val playerY =
                findFloor(playerX, player.width, 1, player.height, currentCollisionArea, boundaryY)
            return Player(
                playerX,
                playerY,
                playerVx,
                player.vy,
                player.jumping,
                jumpY,
                player.jumpReached,
                player.moving,
                player.direction,
                player.frameMetadata,
                player.colliding
            )
        }
        return player
    }

    fun applyParticlePhysics(mapParticles: ArrayList<Particle>): ArrayList<Particle> {
        return mapParticles.map { particle ->
            val nextFrame = particle.frame + 1
            var nextRadius = particle.radius
            var position = Tuples.of(particle.x.toFloat(), particle.y.toFloat())
            if (particle.radius < Particles.MAX_SQUARE_RADIAL_RADIUS) {
                nextRadius = particle.radius + 10
                position = getPosition(nextRadius.toFloat(), particle.id.toFloat(), particle.originX, particle.originY)
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

    fun getPosition(radius: Float, angleInDegrees: Float, originX: Int, originY: Int): Tuple2<Float, Float> {
        val x: Float = (radius * cos(angleInDegrees * Math.PI / 180f)).toFloat() + originX
        val y: Float = (radius * sin(angleInDegrees * Math.PI / 180f)).toFloat() + originY
        return Tuples.of(x, y)
    }

}
