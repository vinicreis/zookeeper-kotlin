package io.github.vinicreis.model.util

import io.github.vinicreis.model.log.ConsoleLog

object AssertionUtils {
    /**
     * Very similar to `assert` call. It checks if any given `rule` is true.
     * @param rule rule to be asserted
     * @param message `RuntimeException` thrown message
     * @throws RuntimeException if the rule assertion is false
     */
    @JvmStatic
    @Throws(RuntimeException::class)
    fun check(rule: Boolean, message: String?) {
        if (!rule) throw RuntimeException(message)
    }

    /**
     * Handles exception passed to it by logging it.
     * @param tag tag to be used in the log
     * @param message message of the exception
     * @param e `Exception` thrown
     */
    fun handleException(tag: String?, message: String?, e: Exception?) {
        ConsoleLog(tag!!).e(message!!, e!!)
    }
}
