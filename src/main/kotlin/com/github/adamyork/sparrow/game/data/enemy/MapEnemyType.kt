package com.github.adamyork.sparrow.game.data.enemy

enum class MapEnemyType {
    VACUUM,
    BOT;

    companion object {
        fun from(literalValue: String): MapEnemyType {
            return when (literalValue) {
                "vacuum" -> {
                    VACUUM
                }

                "deebot" -> {
                    BOT
                }

                else -> {
                    throw IllegalArgumentException("Unknown map item type $literalValue")
                }
            }
        }
    }
}