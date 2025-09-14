package com.github.adamyork.socketgame.game.engine.data

class Particle {
    val id: Int
    val x: Int
    val y: Int
    val originX: Int
    val originY: Int
    val width: Int
    val height: Int
    val type: ParticleType
    val frame: Int
    val lifetime: Int
    val xJitter: Int
    val yJitter: Int
    val radius: Int

    constructor(
        id: Int,
        x: Int,
        y: Int,
        originX: Int,
        originY: Int,
        width: Int,
        height: Int,
        type: ParticleType,
        frame: Int,
        lifetime: Int,
        xJitter: Int,
        yJitter: Int,
        radius: Int
    ) {
        this.id = id
        this.x = x
        this.y = y
        this.originX = originX
        this.originY = originY
        this.width = width
        this.height = height
        this.type = type
        this.frame = frame
        this.lifetime = lifetime
        this.xJitter = xJitter
        this.yJitter = yJitter
        this.radius = radius
    }

}