package com.github.adamyork.sparrow.game.service.v1

import com.github.adamyork.sparrow.game.service.PhysicsSettingsService

class DefaultPhysicsSettingsService : PhysicsSettingsService {

    override var maxXVelocity: Double = 16.0
    override var maxYVelocity: Double = 32.0
    override var jumpDistance: Double = 256.0
    override var gravity: Double = 20.0
    override var friction: Double = 0.9
    override var yVelocityCoefficient: Double = 0.5
    override var xMovementDistance: Double = 1.0
    override var xAccelerationRate: Double = 1.5
    override var xDeaccelerationRate: Double = 4.0

}