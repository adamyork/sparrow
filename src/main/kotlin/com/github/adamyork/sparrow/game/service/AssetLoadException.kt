package com.github.adamyork.sparrow.game.service

class AssetLoadException(fileName: String) : RuntimeException("cant load $fileName") {
}