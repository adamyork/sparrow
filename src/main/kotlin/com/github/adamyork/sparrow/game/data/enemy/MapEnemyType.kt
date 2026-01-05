package com.github.adamyork.sparrow.game.data.enemy

enum class MapEnemyType {
    BLOCKER,
    SHOOTER,
    RUNNER;

    companion object {
        fun from(literalValue: String): MapEnemyType {
            return when (literalValue) {
                "blocker" -> {
                    BLOCKER
                }

                "shooter" -> {
                    SHOOTER
                }

                "runner" -> {
                    RUNNER
                }

                else -> {
                    throw IllegalArgumentException("Unknown map item type $literalValue")
                }
            }
        }
    }
}