package io.github.vinicreis.model.exception

class OutdatedEntryException(
    val key: String,
    val currentTimestamp: Long
) : Exception()
