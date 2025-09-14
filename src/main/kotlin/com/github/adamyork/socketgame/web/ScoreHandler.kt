package com.github.adamyork.socketgame.web

import com.github.adamyork.socketgame.game.ScoreService
import com.github.adamyork.socketgame.web.data.Score
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Mono


@Component
class ScoreHandler {

    final val scoreService: ScoreService

    constructor(scoreService: ScoreService) {
        this.scoreService = scoreService
    }

    fun getScore(request: ServerRequest): Mono<ServerResponse> {
        return ok().bodyValue(Score(scoreService.getTotal(), scoreService.getRemaining()))
    }

}