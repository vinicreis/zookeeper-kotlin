package io.github.vinicreis.node.thread

import io.github.vinicreis.model.Server
import io.github.vinicreis.model.enums.Operation
import io.github.vinicreis.model.log.ConsoleLog
import io.github.vinicreis.model.log.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.net.ServerSocket
import java.net.SocketException

class Dispatcher(private val server: Server) {
    private val log: Log = ConsoleLog(TAG)
    private val serverSocket: ServerSocket = ServerSocket(server.port)
    private var running = true

    suspend fun run() = withContext(Dispatchers.IO) {
        try {
            while (running) {
                log.d("Listening for operation requests...")
                val socket = serverSocket.accept()
                log.d("Request received!")
                val reader = DataInputStream(socket.getInputStream())

                val operationCode = reader.readUTF()
                val message = reader.readUTF()

                log.d("Starting Worker thread...")
                CoroutineScope(Dispatchers.IO).launch {
                    Worker(server, socket, Operation.valueOf(operationCode), message).run()
                }
            }
        } catch (e: SocketException) {
            log.e("Socket closed!", e)
        } finally {
            serverSocket.close()
        }
    }

    companion object {
        private const val TAG = "DispatcherThread"
    }
}
