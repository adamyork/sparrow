package com.github.adamyork.sparrow.socket

import com.github.adamyork.sparrow.common.GameStatusProvider
import com.github.adamyork.sparrow.game.service.AssetService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import kotlin.concurrent.atomics.ExperimentalAtomicApi

class BackgroundAudioHandler : WebSocketHandler {

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(BackgroundAudioHandler::class.java)
    }

    val assetService: AssetService
    val gameStatusProvider: GameStatusProvider

    constructor(assetService: AssetService, gameStatusProvider: GameStatusProvider) {
        this.assetService = assetService
        this.gameStatusProvider = gameStatusProvider
    }

    @OptIn(ExperimentalAtomicApi::class)
    override fun handle(session: WebSocketSession): Mono<Void?> {
        val map = session.receive()
            .flatMap { _ ->
                val currentChunk = gameStatusProvider.backgroundMusicChunk.load()
                val byteArray = assetService.backgroundMusicBytesMap[currentChunk] ?: ByteArray(0)
                if (currentChunk == assetService.backgroundMusicBytesMap.size - 1) {
                    gameStatusProvider.backgroundMusicChunk.store(0)
                } else {
                    gameStatusProvider.backgroundMusicChunk.store(currentChunk + 1)
                }
                Mono.just(byteArray).map { bytes ->
                    session.binaryMessage { session -> session.wrap(bytes) }
                }
            }
        return session.send(map)
    }
}