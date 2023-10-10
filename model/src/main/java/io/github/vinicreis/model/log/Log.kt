package io.github.vinicreis.model.log

interface Log {
    /**
     * Indicates if debug is enabled on this logger instance.
     * @return true if debug is enabled, false otherwise.
     */
    /**
     * Set the debug flag to indicate if the debug should be enabled.
     * @param enable flag to indicate if `d` messages will be enabled.
     */
    var isDebug: Boolean

    /**
     * Insert a log with an error message.
     * @param msg message to inserted
     */
    fun e(msg: String)

    /**
     * Insert a log with a message and an exception.
     * @param msg message to be inserted
     * @param e thrown/caught exception to be logged.
     */
    fun e(msg: String, e: Throwable)

    /**
     * Insert a debug message.
     * @param msg message to be inserted.
     */
    fun d(msg: String)

    /**
     * Insert a log warning message.
     * @param msg warning message to be logged.
     */
    fun w(msg: String)

    /**
     * Insert a verbose message.
     * @param msg verbose message to be inserted.
     */
    fun v(msg: String)
}
