package io.github.vinicreis.node

import io.github.vinicreis.model.Server
import io.github.vinicreis.model.log.ConsoleLog
import io.github.vinicreis.model.log.Log
import io.github.vinicreis.model.util.IOUtil.pressAnyKeyToFinish
import io.github.vinicreis.model.util.IOUtil.readIntWithDefault
import io.github.vinicreis.model.util.IOUtil.readWithDefault
import io.github.vinicreis.model.util.handleException
import java.util.*

interface Node : Server {
    fun join()
    fun exit()

    companion object {
        private const val TAG = "NodeMain"

        @JvmStatic
        fun main(args: Array<String>) {
            try {
                val log: Log = ConsoleLog("NodeMain")
                val debug = Arrays.stream(args).anyMatch { arg: String -> arg == "--debug" || arg == "-d" }
                val port = readIntWithDefault("Digite a porta do servidor", 10098)
                val controllerHost = readWithDefault("Digite o endereço do Controller", "archlaptop")
                val controllerPort = readIntWithDefault("Digite a porta do Controller", 10097)
                val node: Node = NodeImpl(port, controllerHost, controllerPort, debug)
                log.isDebug = debug
                log.d("Starting node...")
                node.start()
                log.d("Node running...")
                pressAnyKeyToFinish()
                log.d("Finishing node...")
                node.stop()
                log.d("Node finished!")
            } catch (e: Throwable) {
                handleException(TAG, "Failed to initialize Node", e)
            }
        }
    }
}
