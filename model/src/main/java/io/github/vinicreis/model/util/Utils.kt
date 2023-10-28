package io.github.vinicreis.model.util

import io.github.vinicreis.model.log.ConsoleLog

fun handleException(tag: String, message: String, block: () -> Unit) {
    try {
        block()
    } catch (e: Throwable) {
        ConsoleLog(tag).e(message, e)
    }
}

fun handleException(tag: String, message: String, e: Throwable) {
    ConsoleLog(tag).e(message, e)
}