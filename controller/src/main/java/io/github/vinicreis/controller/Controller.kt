package io.github.vinicreis.controller

import io.github.vinicreis.model.Server
import io.github.vinicreis.model.log.ConsoleLog
import io.github.vinicreis.model.log.Log
import io.github.vinicreis.model.request.*
import io.github.vinicreis.model.response.ExitResponse
import io.github.vinicreis.model.response.JoinResponse
import io.github.vinicreis.model.util.AssertionUtils.handleException
import io.github.vinicreis.model.util.IOUtil.pressAnyKeyToFinish
import io.github.vinicreis.model.util.IOUtil.readWithDefault
import java.util.*

interface Controller : Server {
    fun join(request: JoinRequest): JoinResponse
    fun exit(request: ExitRequest): ExitResponse

    data class Node(private val request: Request) {
        val host: String = request.host
        val port: Int = request.port

        override fun equals(other: Any?): Boolean {
            if (other == null) return false
            if (other !is Node) return false

            return other.host == host && other.port == port
        }

        override fun toString(): String {
            return String.format("%s:%d", host, port)
        }
    }

    companion object {
        fun main(args: Array<String>) {
            try {
                val log: Log = ConsoleLog("ControllerMain")
                val debug = args.any { it in listOf("--debug", "-d") }
                val port = readWithDefault("Digite o valor da porta do servidor", "10097").toInt()
                val controller: Controller = ControllerImpl(port, debug)

                log.isDebug = debug

                log.d("Starting controller...")
                controller.start()
                log.d("Controller started!")

                pressAnyKeyToFinish()

                log.d("Finishing controller...")
                controller.stop()
                log.d("Controller finished!")
            } catch (e: Exception) {
                handleException("ControllerMain", "Failed start Controller...", e)
            }
        }
    }
}
