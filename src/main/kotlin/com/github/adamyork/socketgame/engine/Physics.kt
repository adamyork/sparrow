package com.github.adamyork.socketgame.engine

import com.github.adamyork.socketgame.engine.data.PhysicsResult
import com.github.adamyork.socketgame.engine.data.PhysicsXResult
import com.github.adamyork.socketgame.engine.data.PhysicsYResult
import com.github.adamyork.socketgame.game.data.Asset
import com.github.adamyork.socketgame.game.data.Direction
import com.github.adamyork.socketgame.game.data.Player
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.awt.image.BufferedImage
import kotlin.math.roundToInt


@Service
class Physics {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(Physics::class.java)
        const val GRAVITY: Double = 10.0
        const val FRICTION: Double = 0.1
        const val X_EASING_COEFFICIENT: Double = 1.0
        const val Y_EASING_COEFFICIENT: Double = 0.1
        const val VELOCITY_COEFFICIENT: Double = 0.5
        const val COLLISION_COLOR_VALUE: Int = -16711906
    }

    fun applyPhysics(player: Player, backgroundAsset: Asset, collisionAsset: Asset): PhysicsResult {
        val vx = applyXVelocity(player)
        val vy = applyYVelocity(player)
        val xPos = calculatePlayerX(player, vx)
        val yState = calculatePlayerY(player, backgroundAsset, vy)
        //val boundedPlayerX = adjustedPlayerX(player, vx, xPos, collisionAsset.bufferedImage)
        val boundedPlayerY =
            adjustedPlayerY(player, xPos, yState.y, yState.dy, yState.vy, yState.jumping, collisionAsset)
//        return PhysicsResult(
//            boundedPlayerX.x,
//            yState.y,
//            yState.dy,
//            boundedPlayerX.vx,
//            yState.vy,
//            boundedPlayerX.moving,
//            yState.jumping,
//            player.direction,
//            player.floor
//        )
        return PhysicsResult(
            xPos,
            boundedPlayerY.y,
            boundedPlayerY.dy,
            vx,
            boundedPlayerY.vy,
            player.moving,
            boundedPlayerY.jumping,
            player.direction,
            boundedPlayerY.floor
        )
    }

    private fun onFloor(player: Player): Boolean {
        return player.y == player.floor
    }

    fun calculatePlayerX(player: Player, vx: Double): Int {
        var x = player.x
        if (player.moving || player.vx != 0.0) {
            x = when (player.direction) {
                Direction.LEFT -> player.x - ((X_EASING_COEFFICIENT * vx) + FRICTION).roundToInt()
                Direction.RIGHT -> player.x + ((X_EASING_COEFFICIENT * vx) - FRICTION).roundToInt()
            }
        }
        return x
    }

    fun calculatePlayerY(player: Player, backgroundAsset: Asset, vy: Double): PhysicsYResult {
        var y = player.y
        var vy = vy
        var dy = player.dy
        var jumping = player.jumping
        val onFloor = onFloor(player)
         if (jumping && onFloor) {
            LOGGER.info("starting jump setting the dy y: $y player.jumping: $jumping vy: $vy dy: $dy onFloor: $onFloor floor: ${player.floor}")
            dy = y - Player.JUMP_DISTANCE
            y -= 20
        } else if (jumping && y > dy) {
            LOGGER.info("In the process of jumping y: $y player.jumping: $jumping vy: $vy dy: $dy onFloor: $onFloor floor: ${player.floor}")
             y -= ((X_EASING_COEFFICIENT * vy) - GRAVITY).roundToInt()
            if (y <= dy) {
                LOGGER.info("Jump heigh reached y: $y player.jumping: $jumping vy: $vy dy: $dy onFloor: $onFloor floor: ${player.floor}")
                y = dy
                dy = player.floor
            }
        } else if (jumping && y < dy) {
            LOGGER.info("In the process of falling y: $y player.jumping: $jumping vy: $vy dy: $dy onFloor: $onFloor floor: ${player.floor}")
            y += ((X_EASING_COEFFICIENT * vy) - GRAVITY).roundToInt()
            if (y >= player.floor) {
                LOGGER.info("jump complete y: $y player.jumping: $jumping vy: $vy dy: $dy onFloor: $onFloor floor: ${player.floor}")
                y = player.floor
                jumping = false
                vy = 0.0
                dy = y
            }
        } else {
            y = (y + GRAVITY.roundToInt()).coerceAtMost(backgroundAsset.height - player.height)
            if(y == backgroundAsset.height - player.height){
                PhysicsYResult(y, vy, dy, jumping, y)
            }
        }
        return PhysicsYResult(y, vy, dy, jumping, player.floor)
    }

    fun applyXVelocity(player: Player): Double {
        var vx: Double = player.vx
        if (player.moving) {
            //LOGGER.debug("incrementing player velocity $vx")
            if (vx == 0.0) {
                vx += VELOCITY_COEFFICIENT
            } else if (vx < Player.Companion.MAX_X_VELOCITY) {
                vx = vx + (VELOCITY_COEFFICIENT * vx)
                if (vx > Player.Companion.MAX_X_VELOCITY) {
                    vx = Player.Companion.MAX_X_VELOCITY
                }
            }
        } else {
            if (vx > 0.0) {
                //LOGGER.debug("decrementing player velocity $vx")
                vx = vx - (VELOCITY_COEFFICIENT * vx)
                if (vx < VELOCITY_COEFFICIENT) {
                    vx = 0.0
                }
            }
        }
        return vx
    }

    fun applyYVelocity(player: Player): Double {
        val y = player.y
        val dy = player.dy
        var vy: Double = player.vy
        if (player.jumping) {
            //LOGGER.debug("incrementing player velocity $velocityY")
            if (vy == 0.0) {
                vy = vy + VELOCITY_COEFFICIENT
            } else if (vy < Player.MAX_Y_VELOCITY) {
                val distanceToTarget = y - dy
                LOGGER.debug("incrementing player velocity $vy dist: $distanceToTarget")
                vy = vy + (VELOCITY_COEFFICIENT * vy) + (distanceToTarget * Y_EASING_COEFFICIENT)
                if (vy > Player.MAX_Y_VELOCITY) {
                    vy = Player.MAX_Y_VELOCITY
                }
            }
        } else {
            vy = 0.0
        }
        return vy
    }

    fun adjustedPlayerX(player: Player, vx: Double, dx: Int, collisionImage: BufferedImage): PhysicsXResult {
        var xBoundary = dx
        if (player.direction == Direction.RIGHT) {
            xBoundary = dx + player.width;
        }
        val rgb1 = collisionImage.getRGB(xBoundary, player.y, 1, player.height, null, 0, player.width + 1)
        if (rgb1.contains(COLLISION_COLOR_VALUE)) {
            LOGGER.info("Collision")
            val safeX = findSafeX(player, dx, collisionImage)
            return PhysicsXResult(
                safeX,
                0.0,
                false
            )
        }
        return PhysicsXResult(
            dx,
            vx,
            player.moving
        )
    }

    fun adjustedPlayerY(
        player: Player,
        dx: Int,
        y: Int,
        dy: Int,
        vy: Double,
        jumping: Boolean,
        collisionAsset: Asset
    ): PhysicsYResult {
        var startX: Int
        var startY: Int
        var width: Int
        var height: Int
        var isFalling = false
//        if (y == player.floor) {
//            LOGGER.info("player is on the floor, bypass collision")
//            return PhysicsYResult(
//                y,
//                vy,
//                dy,
//                jumping,
//                player.floor
//            )
//        }
        if (y > dy && player.y != player.floor) {
            LOGGER.info("player is rising")
            startX = dx + 1
            startY = y - 1
            width = player.width - 1
            height = 1
        } else {
            LOGGER.info("player is falling")
            startX = dx + 1
            startY = (y + player.height + 1).coerceAtMost(collisionAsset.height - 1)
            width = player.width - 1
            height = 1
            isFalling = true
        }
        val rgb1 = collisionAsset.bufferedImage.getRGB(startX, startY, width, height, null, 0, 64)
        if (rgb1.contains(COLLISION_COLOR_VALUE)) {
            LOGGER.info("Vertical Collision")
            val safeY = findSafeY(startX, startY, width, height, isFalling, collisionAsset.bufferedImage)
            return PhysicsYResult(
                safeY,
                0.0,
                safeY,
                false,
                safeY
            )
        }
        return PhysicsYResult(
            y,
            vy,
            dy,
            jumping,
            player.floor
        )
    }

    private fun findSafeX(player: Player, dx: Int, collisionImage: BufferedImage): Int {
        var nextDx = dx
        if (player.direction == Direction.LEFT) {
            nextDx += 1
        } else {
            nextDx -= 1
        }
        val rgb1 = collisionImage.getRGB(nextDx, player.y, player.width, player.height, null, 0, 64)
        if (rgb1.contains(COLLISION_COLOR_VALUE)) {
            return findSafeX(player, nextDx, collisionImage)
        }
        return nextDx
    }

    private fun findSafeY(
        startX: Int,
        startY: Int,
        width: Int,
        height: Int,
        isFalling: Boolean,
        collisionImage: BufferedImage
    ): Int {
        //LOGGER.info("findSafeY $startY")
        var nextY: Int = startY
        nextY = if (isFalling) {
            startY - 1
        } else {
            startY + 1
        }
        val rgb1 = collisionImage.getRGB(startX, nextY, width, height, null, 0, 64)
        if (rgb1.contains(COLLISION_COLOR_VALUE)) {
            return findSafeY(startX, nextY, width, height, isFalling, collisionImage)
        }
        if (isFalling) {
            return nextY - 64
        }
        return nextY
    }

}