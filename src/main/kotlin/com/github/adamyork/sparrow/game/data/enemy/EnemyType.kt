package com.github.adamyork.sparrow.game.data.enemy

enum class EnemyType {
    BLOCKER,
    SHOOTER,
    RUNNER;

    companion object {
        fun from(literalValue: String): EnemyType {
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