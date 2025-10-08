package com.github.adamyork.socketgame.game.engine.data

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
    val radius: Int
)