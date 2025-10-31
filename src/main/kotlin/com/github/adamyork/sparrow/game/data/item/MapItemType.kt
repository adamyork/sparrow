package com.github.adamyork.sparrow.game.data.item

enum class MapItemType {
    COLLECTABLE,
    FINISH;

    companion object {
        fun from(literalValue: String): MapItemType {
            return when (literalValue) {
                "collectable" -> {
                    COLLECTABLE
                }

                "finish" -> {
                    FINISH
                }

                else -> {
                    throw IllegalArgumentException("Unknown map item type $literalValue")
                }
            }
        }
    }


}