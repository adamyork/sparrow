package com.github.adamyork.sparrow.game.data.item

enum class ItemType {
    COLLECTABLE,
    FINISH;

    companion object {
        fun from(literalValue: String): ItemType {
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