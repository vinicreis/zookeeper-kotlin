package io.github.vinicreis.model.exception

/**
 * Exception to be thrown in case a key with an outdated timestamp tries to be fetch.
 */
class OutdatedEntryException(
    /**
     * Get the requested key with error.
     * @return the key fetched with an outdated timestamp.
     */
    val key: String,
    /**
     * Gets the timestamp that the user has associated with this key.
     * @return the integer timestamp value
     */
    val currentTimestamp: Long
) : Exception()
