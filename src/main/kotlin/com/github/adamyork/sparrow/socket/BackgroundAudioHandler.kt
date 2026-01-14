package com.github.adamyork.sparrow.socket

import com.github.adamyork.sparrow.common.StatusProvider
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
    val statusProvider: StatusProvider
    val webSocketMessageBuilder: WebSocketMessageBuilder

    constructor(
        assetService: AssetService,
        statusProvider: StatusProvider,
        webSocketMessageBuilder: WebSocketMessageBuilder
    ) {
        this.assetService = assetService
        this.statusProvider = statusProvider
        this.webSocketMessageBuilder = webSocketMessageBuilder
    }

    @OptIn(ExperimentalAtomicApi::class)
    override fun handle(session: WebSocketSession): Mono<Void?> {
        val map = session.receive()
            .flatMap { _ ->
                val currentBgMusicChunkIndex = statusProvider.backgroundMusicChunkIndex.load()
                val bgMusicChunkByteArrayMono: Mono<ByteArray> =
                    assetService.backgroundMusicBytesMap[currentBgMusicChunkIndex]
                        ?.let { Mono.just(it) }
                        ?: Mono.error(RuntimeException("bg music chunk not found"))
                if (currentBgMusicChunkIndex == assetService.backgroundMusicBytesMap.size - 1) {
                    statusProvider.backgroundMusicChunkIndex.store(0)
                } else {
                    statusProvider.backgroundMusicChunkIndex.store(currentBgMusicChunkIndex + 1)
                }
                bgMusicChunkByteArrayMono.map { bytes -> webSocketMessageBuilder.build(session, bytes) }
            }
        return session.send(map)
    }
}