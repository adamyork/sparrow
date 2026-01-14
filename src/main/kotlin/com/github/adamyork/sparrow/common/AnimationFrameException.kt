package com.github.adamyork.sparrow.common

class AnimationFrameException(name: String, index: Int) :
    RuntimeException("referenced animation frame $index is missing from $name")