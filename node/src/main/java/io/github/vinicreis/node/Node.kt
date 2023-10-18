package io.github.vinicreis.node

import io.github.vinicreis.model.Server
import io.github.vinicreis.model.log.ConsoleLog
import io.github.vinicreis.model.log.Log
import io.github.vinicreis.model.util.IOUtil.pressAnyKeyToFinish
import io.github.vinicreis.model.util.IOUtil.readWithDefault
import java.util.*

/**
 * Generic interface that represents `Node` instance of a `Server`
 */
interface Node : Server {
    /**
     * Trigger the JOIN process from a `Node` to a `Controller`
     */
    fun join()

    /**
     * Trigger the EXIT process from a `Node` to a `Controller`
     */
    fun exit()

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            try {
                val log: Log = ConsoleLog("NodeMain")
                val debug = Arrays.stream(args).anyMatch { arg: String -> arg == "--debug" || arg == "-d" }
                val port = readWithDefault("Digite a porta do servidor", "10098").toInt()
                val controllerHost = readWithDefault("Digite o endereço do Controller", "localhost")
                val controllerPort = readWithDefault("Digite a porta do Controller", "10097").toInt()
                val node: Node = NodeImpl(port, controllerHost, controllerPort, debug)
                log.isDebug = debug
                log.d("Starting node...")
                node.start()
                log.d("Node running...")
                pressAnyKeyToFinish()
                log.d("Finishing node...")
                node.stop()
                log.d("Node finished!")
            } catch (e: Exception) {
                handleException("NodeMain", "Failed to initialize Node", e)
            }
        }
    }
}
