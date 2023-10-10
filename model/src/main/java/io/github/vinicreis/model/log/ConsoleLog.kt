package io.github.vinicreis.model.log

import java.util.logging.Level
import java.util.logging.Logger

class ConsoleLog(tag: String) : Log {
    private val logger: Logger
    override var isDebug: Boolean = false

    init {
        logger = Logger.getLogger(tag)
    }

    override fun e(msg: String) {
        logger.log(Level.SEVERE, msg)
    }

    override fun e(msg: String, e: Throwable) {
        logger.log(Level.SEVERE, msg, e)
    }

    override fun d(msg: String) {
        if (isDebug) logger.log(Level.INFO, msg)
    }

    override fun w(msg: String) {
        logger.log(Level.WARNING, msg)
    }

    override fun v(msg: String) {
        logger.log(Level.ALL, msg)
    }
}
