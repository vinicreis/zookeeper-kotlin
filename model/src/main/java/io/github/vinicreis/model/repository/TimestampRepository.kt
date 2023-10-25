package io.github.vinicreis.model.repository

import io.github.vinicreis.model.log.ConsoleLog
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicLong

class TimestampRepository(private val step: Long = DEFAULT_STEP) {
    private var running = false
    private val counter: AtomicLong = AtomicLong(0L)
    val current: Long get() = counter.get()

    suspend fun run() = withContext(Dispatchers.IO) {
        try {
            running = true
            while (running) {
                if (counter.get() == Long.MAX_VALUE) counter.set(0L) else counter.incrementAndGet()
                delay(step)
            }
        } catch (e: InterruptedException) {
            log.d("Timestamp clock interrupted!")
        }
    }

    companion object {
        private const val DEFAULT_STEP = 100L
        private const val TAG = "TimestampRepository"
        private val log = ConsoleLog(TAG)
    }
}
