package io.github.vinicreis.model.repository.thread

import io.github.vinicreis.model.log.ConsoleLog
import io.github.vinicreis.model.log.Log
import java.util.concurrent.atomic.AtomicLong

class TimestampIncrementThread(private val step: Long) : Thread() {
    private var running = false
    private val current = AtomicLong(0L)

    override fun run() {
        try {
            running = true
            while (running) {
                if (current.get() == Long.MAX_VALUE) current.set(0L) else current.incrementAndGet()
                sleep(step)
            }
        } catch (e: InterruptedException) {
            log.d("Timestamp clock interrupted!")
            reset()
        }
    }

    override fun interrupt() {
        running = false

        super.interrupt()
    }

    @Throws(IllegalStateException::class)
    fun getCurrent(): Long {
        if (running) return current.get()

        throw IllegalStateException("Timestamp clock not running")
    }

    fun reset() {
        running = false
        current.set(0L)
    }

    companion object {
        private const val TAG = "TimestampIncrementThread"
        private val log: Log = ConsoleLog(TAG)
    }
}
