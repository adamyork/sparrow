package com.github.adamyork.sparrow.common

import org.springframework.stereotype.Component
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi


@OptIn(ExperimentalAtomicApi::class)
@Component
class GameStatusProvider {

    val running: AtomicBoolean = AtomicBoolean(false)
    val lastPaintTime: AtomicInt = AtomicInt(0)

}
