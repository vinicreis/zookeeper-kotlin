package io.github.vinicreis.node

import io.github.vinicreis.model.Server
import io.github.vinicreis.model.log.ConsoleLog
import io.github.vinicreis.model.log.Log
import io.github.vinicreis.model.util.IOUtil.pressAnyKeyToFinish
import io.github.vinicreis.model.util.IOUtil.readIntWithDefault
import io.github.vinicreis.model.util.IOUtil.readWithDefault
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.util.*

interface Node : Server {
    fun join()
    fun exit()

    companion object {
        private const val TAG = "NodeMain"
        private const val DEFAULT_PORT = 10098
        private const val DEFAULT_CONTROLLER_PORT = 10097

        @JvmStatic
        fun main(args: Array<String>) {
            val coroutineContext = Dispatchers.IO + SupervisorJob() + CoroutineName(TAG)
            val coroutineScope = CoroutineScope(coroutineContext)
            val log: Log = ConsoleLog("NodeMain")
            val debug = Arrays.stream(args).anyMatch { arg: String -> arg == "--debug" || arg == "-d" }
            val port = readIntWithDefault("Digite a porta do servidor", DEFAULT_PORT)
            val controllerHost = readWithDefault("Digite o endereço do Controller", "archlaptop")
            val controllerPort = readIntWithDefault("Digite a porta do Controller", DEFAULT_CONTROLLER_PORT)
            val node: Node = NodeImpl(port, controllerHost, controllerPort, debug, coroutineScope)
            log.isDebug = debug
            log.d("Starting node...")
            node.start()
            log.d("Node running...")
            pressAnyKeyToFinish()
            log.d("Finishing node...")
            node.stop()
            log.d("Node finished!")
        }
    }
}
