package com.github.adamyork.sparrow.web

import com.github.adamyork.sparrow.game.service.PhysicsSettingsService
import com.github.adamyork.sparrow.web.data.PhysicsSettings
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Mono


class PhysicsSettingsHandler {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(PhysicsSettingsHandler::class.java)
    }

    val physicsSettingsService: PhysicsSettingsService

    constructor(physicsSettingsService: PhysicsSettingsService) {
        this.physicsSettingsService = physicsSettingsService
    }

    fun updatePhysics(request: ServerRequest): Mono<ServerResponse> {
        LOGGER.info("request for ${request.requestPath()} received")
        return request.bodyToMono<PhysicsSettings>()
            .flatMap { settings ->
                physicsSettingsService.maxXVelocity = settings.maxXVelocityRange
                physicsSettingsService.maxYVelocity = settings.maxYVelocityRange
                physicsSettingsService.jumpDistance = settings.jumpDistanceRange
                physicsSettingsService.gravity = settings.gravityRange
                physicsSettingsService.friction = settings.frictionRange
                physicsSettingsService.yVelocityCoefficient = settings.velocityYCoefficientRange
                physicsSettingsService.xMovementDistance = settings.movementXDistanceRange
                physicsSettingsService.xAccelerationRate = settings.accelerationXRateRange
                physicsSettingsService.xDeaccelerationRate = settings.deaccelerationXRateRange
                LOGGER.info("settings updated to $settings")
                ok().build()
            }
    }
}