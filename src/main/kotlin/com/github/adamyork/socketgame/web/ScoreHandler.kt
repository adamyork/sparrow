package com.github.adamyork.socketgame.web

import com.github.adamyork.socketgame.game.service.ScoreService
import com.github.adamyork.socketgame.web.data.Score
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Mono


@Service
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