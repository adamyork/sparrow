package com.github.adamyork.sparrow.game.data.enemy

enum class MapEnemyType {
    BLOCKER,
    SHOOTER;

    companion object {
        fun from(literalValue: String): MapEnemyType {
            return when (literalValue) {
                "vacuum" -> {
                    BLOCKER
                }

                "deebot" -> {
                    SHOOTER
                }

                else -> {
                    throw IllegalArgumentException("Unknown map item type $literalValue")
                }
            }
        }
    }
}