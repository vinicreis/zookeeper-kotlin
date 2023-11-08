package io.github.vinicreis.model.log

interface Log {
    var isDebug: Boolean

    fun e(msg: String)
    fun e(msg: String, e: Exception)
    fun d(msg: String)
    fun w(msg: String)
    fun v(msg: String)
}
