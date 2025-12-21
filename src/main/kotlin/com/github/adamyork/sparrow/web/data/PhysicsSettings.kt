package com.github.adamyork.sparrow.web.data

data class PhysicsSettings(
    val maxXVelocityRange: Double = 0.0,
    val maxYVelocityRange: Double = 0.0,
    val jumpDistanceRange: Double = 0.0,
    val gravityRange: Double = 0.0,
    val frictionRange: Double = 0.0,
    val velocityYCoefficientRange: Double = 0.0,
    val movementXDistanceRange: Double = 0.0,
    val accelerationXRateRange: Double = 0.0,
    val deaccelerationXRateRange: Double = 0.0
)