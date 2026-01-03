package com.github.adamyork.sparrow.game.data.enemy

enum class MapEnemyType {
    BLOCKER,
    SHOOTER,
    RUNNER;

    companion object {
        fun from(literalValue: String): MapEnemyType {
            return when (literalValue) {
                "blocker" -> {//TODO generify
                    BLOCKER
                }

                "shooter" -> {//TODO generify
                    SHOOTER
                }

                "runner" -> {//TODO generify
                    RUNNER
                }

                else -> {
                    throw IllegalArgumentException("Unknown map item type $literalValue")
                }
            }
        }
    }
}