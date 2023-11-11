package io.github.vinicreis.node.thread

import io.github.vinicreis.model.Server
import io.github.vinicreis.model.enums.Operation
import io.github.vinicreis.model.log.ConsoleLog
import io.github.vinicreis.model.log.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.net.ServerSocket
import java.net.SocketException
import kotlin.coroutines.CoroutineContext

class Dispatcher(private val server: Server) {
    private val log: Log = ConsoleLog(TAG)
    private val serverSocket: ServerSocket = ServerSocket(server.port)

    suspend fun run(
        coroutineContext: CoroutineContext = Dispatchers.IO
    ): Unit = withContext(coroutineContext) {
        try {
            while (true) {
                log.d("Listening for operation requests...")
                val socket = serverSocket.accept()
                log.d("Request received!")
                val reader = DataInputStream(socket.getInputStream())

                val operationCode = reader.readUTF()
                val message = reader.readUTF()

                log.d("Starting Worker thread...")

                launch(coroutineContext) {
                    Worker(
                        server = server,
                        socket = socket,
                        operation = Operation.valueOf(operationCode),
                        request = message
                    ).run(coroutineContext)
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
