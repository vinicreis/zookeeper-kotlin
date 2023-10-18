package io.github.vinicreis.model.repository

import io.github.vinicreis.model.exception.OutdatedEntryException
import io.github.vinicreis.model.util.AssertionUtils
import java.util.concurrent.ConcurrentHashMap

class KeyValueRepository {
    private val data: MutableMap<String, Entry?> = ConcurrentHashMap()
    private val timestampRepository: TimestampRepository?

    constructor(timestampRepository: TimestampRepository?) {
        this.timestampRepository = timestampRepository
    }

    constructor() {
        timestampRepository = null
    }

    data class Entry(val value: String, val timestamp: Long)

    fun insert(key: String, value: String): Long {
        AssertionUtils.check(timestampRepository != null, "Timestamp repository is not initialized!")

        val timestamp = timestampRepository!!.current
        data[key] = Entry(value, timestamp)

        return timestamp
    }

    fun replicate(key: String, value: String, timestamp: Long) {
        data[key] = Entry(value, timestamp)
    }

    @Throws(OutdatedEntryException::class)
    fun find(key: String, timestamp: Long?): Entry? {
        val result = data.getOrDefault(key, null)

        if (timestamp != null && timestamp > 0 && result != null && result.timestamp < timestamp)
            throw OutdatedEntryException(key, result.timestamp)

        return result
    }
}
