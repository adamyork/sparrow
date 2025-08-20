package com.github.adamyork.socketgame.game.data

class GameMap {

    companion object {
        const val VIEWPORT_HORIZONTAL_ADVANCE_RATE: Int = 10
        const val VIEWPORT_VERTICAL_ADVANCE_RATE: Int = 128
    }

    val farGroundAsset: Asset
    val midGroundAsset: Asset
    val nearFieldAsset: Asset
    val collisionAsset: Asset
    val x: Int
    val y: Int
    val width: Int
    val height: Int
    val moved: Boolean

    constructor(
        farGroundAsset: Asset,
        midGroundAsset: Asset,
        nearFieldAsset: Asset,
        collisionAsset: Asset,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        moved: Boolean
    ) {
        this.farGroundAsset = farGroundAsset
        this.midGroundAsset = midGroundAsset
        this.nearFieldAsset = nearFieldAsset
        this.collisionAsset = collisionAsset
        this.x = x
        this.y = y
        this.width = width
        this.height = height
        this.moved = moved
    }

}