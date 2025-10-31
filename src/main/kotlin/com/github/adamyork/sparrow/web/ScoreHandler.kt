package com.github.adamyork.sparrow.web

import com.github.adamyork.sparrow.game.service.ScoreService
import com.github.adamyork.sparrow.web.data.Score
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Mono


class ScoreHandler {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(ScoreHandler::class.java)
    }

    final val scoreService: ScoreService

    constructor(scoreService: ScoreService) {
        this.scoreService = scoreService
    }

    fun getScore(request: ServerRequest): Mono<ServerResponse> {
        LOGGER.info("request for ${request.requestPath()} received")
        return ok().bodyValue(Score(scoreService.getTotal(), scoreService.getRemaining()))
    }

}