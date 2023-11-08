package io.github.vinicreis.controller

import io.github.vinicreis.model.Server
import io.github.vinicreis.model.log.ConsoleLog
import io.github.vinicreis.model.log.Log
import io.github.vinicreis.model.request.ExitRequest
import io.github.vinicreis.model.request.JoinRequest
import io.github.vinicreis.model.request.Request
import io.github.vinicreis.model.response.ExitResponse
import io.github.vinicreis.model.response.JoinResponse
import io.github.vinicreis.model.util.IOUtil.pressAnyKeyToFinish
import io.github.vinicreis.model.util.IOUtil.readIntWithDefault
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

interface Controller : Server {
    fun join(request: JoinRequest): JoinResponse
    fun exit(request: ExitRequest): ExitResponse

    data class Node(private val request: Request) {
        val host: String = request.host
        val port: Int = request.port

        override fun toString() = "$host:$port"
    }

    companion object {
        private const val TAG = "ControllerMain"
        private const val DEFAULT_PORT = 10097

        @JvmStatic
        fun main(args: Array<String>) {
            val coroutineContext = Dispatchers.IO + SupervisorJob() + CoroutineName(TAG)
            val coroutineScope = CoroutineScope(coroutineContext)
            val log: Log = ConsoleLog("ControllerMain")
            val debug = args.any { it in listOf("--debug", "-d") }
            val port = readIntWithDefault("Digite o valor da porta do servidor", DEFAULT_PORT)
            val controller: Controller = ControllerImpl(port, debug, coroutineScope)

            log.isDebug = debug

            log.d("Starting controller...")
            controller.start()
            log.d("Controller started!")

            pressAnyKeyToFinish()

            log.d("Finishing controller...")
            controller.stop()
            log.d("Controller finished!")
        }
    }
}
