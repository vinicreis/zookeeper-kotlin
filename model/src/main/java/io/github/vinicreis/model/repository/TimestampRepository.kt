package io.github.vinicreis.model.repository

import io.github.vinicreis.model.repository.thread.TimestampIncrementThread

class TimestampRepository {
    private val thread = TimestampIncrementThread(DEFAULT_STEP)

    @get:Throws(IllegalStateException::class)
    val current: Long
        get() = thread.getCurrent()

    fun start() {
        thread.start()
    }

    fun stop() {
        thread.interrupt()
    }

    fun reset() {
        thread.reset()
    }

    companion object {
        private const val DEFAULT_STEP = 100L
    }
}
