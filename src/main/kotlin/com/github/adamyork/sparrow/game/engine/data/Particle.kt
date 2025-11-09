package com.github.adamyork.sparrow.game.engine.data

import java.awt.Color

data class Particle(
    val id: Int,
    val x: Int,
    val y: Int,
    val originX: Int,
    val originY: Int,
    val width: Int,
    val height: Int,
    val type: ParticleType,
    val frame: Int,
    val lifetime: Int,
    val xJitter: Int,
    val yJitter: Int,
    val radius: Int,
    val color: Color,
    val shape: ParticleShape
) {

    fun from(nextX: Int, nextY: Int, nextFrame: Int): Particle {
        return Particle(
            this.id,
            nextX,
            nextY,
            this.originX,
            this.originY,
            this.width,
            this.height,
            this.type,
            nextFrame,
            this.lifetime,
            this.xJitter,
            this.yJitter,
            this.radius,
            this.color,
            this.shape
        )
    }

    fun from(coords: Pair<Float, Float>, nextFrame: Int, nextRadius: Int): Particle {
        return Particle(
            this.id,
            coords.first.toInt() + this.xJitter,
            coords.second.toInt() + this.yJitter,
            this.originX,
            this.originY,
            this.width,
            this.height,
            this.type,
            nextFrame,
            this.lifetime,
            this.xJitter,
            this.yJitter,
            nextRadius,
            this.color,
            this.shape
        )
    }

    fun from(nextWidth: Int, nextHeight: Int, nextFrame: Int, nextRadius: Int): Particle {
        return Particle(
            this.id,
            this.x,
            this.y,
            this.originX,
            this.originY,
            nextWidth,
            nextHeight,
            this.type,
            nextFrame,
            this.lifetime,
            this.xJitter,
            this.yJitter,
            nextRadius,
            this.color,
            this.shape
        )
    }

    fun from(nextFrame: Int): Particle {
        return Particle(
            this.id,
            this.x,
            this.y,
            this.originX,
            this.originY,
            this.width,
            this.height,
            this.type,
            nextFrame,
            this.lifetime,
            this.xJitter,
            this.yJitter,
            this.radius,
            this.color,
            this.shape
        )
    }

}


