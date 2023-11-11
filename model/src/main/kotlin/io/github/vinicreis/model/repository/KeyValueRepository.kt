package io.github.vinicreis.model.repository

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
        timestampRepository ?: error("Timestamp repository is not initialized!")

        val timestamp = timestampRepository.current
        data[key] = Entry(value, timestamp)

        return timestamp
    }

    fun replicate(key: String, value: String, timestamp: Long) {
        data[key] = Entry(value, timestamp)
    }

    fun find(key: String, timestamp: Long?): Entry? {
        return data.getOrDefault(key, null)?.also { entry ->
            timestamp?.let {
                if(entry.timestamp < it) error("Outdated entry found!")
            }
        }
    }
}
