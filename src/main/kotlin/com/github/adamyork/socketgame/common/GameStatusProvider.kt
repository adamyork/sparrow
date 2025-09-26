package com.github.adamyork.socketgame.common;

import org.springframework.stereotype.Component;
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi


@OptIn(ExperimentalAtomicApi::class)
@Component
class GameStatusProvider {

    val running: AtomicBoolean = AtomicBoolean(false)

}
