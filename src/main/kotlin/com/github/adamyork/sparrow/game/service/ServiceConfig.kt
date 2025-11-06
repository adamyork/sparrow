package com.github.adamyork.sparrow.game.service

import com.github.adamyork.sparrow.game.service.v1.DefaultAssetService
import com.github.adamyork.sparrow.game.service.v1.DefaultScoreService
import com.github.adamyork.sparrow.game.service.v1.DefaultWavService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ServiceConfig {

    @Bean
    fun wavService(): WavService = DefaultWavService()

    @Bean
    fun scoreService(): ScoreService = DefaultScoreService()

    @Bean
    fun assetService(
        wavService: WavService,
        @Value("\${viewport.width}") viewPortWidth: Int,
        @Value("\${viewport.height}") viewPortHeight: Int,
        @Value("\${map.width}") mapWidth: Int,
        @Value("\${map.height}") mapHeight: Int,
        @Value("\${map.bg}") mapBackgroundPath: String,
        @Value("\${map.mg}") mapMiddleGroundPath: String,
        @Value("\${map.fg}") mapForGroundPath: String,
        @Value("\${map.col}") mapCollisionPath: String,
        @Value("\${player.width}") playerWidth: Int,
        @Value("\${player.height}") playerHeight: Int,
        @Value("\${player.asset.path}") playerAssetPath: String,
        @Value("\${map.item.width}") mapItemWidth: Int,
        @Value("\${map.item.height}") mapItemHeight: Int,
        @Value("\${map.item.asset.one.path}") mapItemAssetOnePath: String,
        @Value("\${map.item.asset.two.path}") mapItemAssetTwoPath: String,
        @Value("\${map.enemy.asset.one.width}") mapEnemyOneWidth: Int,
        @Value("\${map.enemy.asset.one.height}") mapEnemyOneHeight: Int,
        @Value("\${map.enemy.asset.one.path}") mapEnemyAssetOnePath: String,
        @Value("\${map.enemy.asset.two.path}") mapEnemyAssetTwoPath: String,
        @Value("\${map.enemy.asset.two.width}") mapEnemyTwoWidth: Int,
        @Value("\${map.enemy.asset.two.height}") mapEnemyTwoHeight: Int,
        @Value("\${audio.player.jump}") audioPlayerJumpPath: String,
        @Value("\${audio.player.collision}") audioPlayerCollisionPath: String,
        @Value("\${audio.item.collect}") audioItemCollectPath: String,
        @Value("\${audio.background}") audioBackgroundMusicPath: String,
        @Value("\${audio.enemy.shoot}") audioEnemyShootPath: String,
    ): AssetService {
        return DefaultAssetService(
            wavService,
            viewPortWidth,
            viewPortHeight,
            mapWidth,
            mapHeight,
            mapBackgroundPath,
            mapMiddleGroundPath,
            mapForGroundPath,
            mapCollisionPath,
            playerWidth,
            playerHeight,
            playerAssetPath,
            mapItemWidth,
            mapItemHeight,
            mapItemAssetOnePath,
            mapItemAssetTwoPath,
            mapEnemyOneWidth,
            mapEnemyOneHeight,
            mapEnemyAssetOnePath,
            mapEnemyAssetTwoPath,
            mapEnemyTwoWidth,
            mapEnemyTwoHeight,
            audioPlayerJumpPath,
            audioPlayerCollisionPath,
            audioItemCollectPath,
            audioBackgroundMusicPath,
            audioEnemyShootPath
        )
    }

}