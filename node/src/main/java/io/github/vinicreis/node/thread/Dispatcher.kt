package io.github.vinicreis.node.thread

import io.github.vinicreis.model.Server
import io.github.vinicreis.model.enums.Operation
import io.github.vinicreis.model.log.ConsoleLog
import io.github.vinicreis.model.log.Log
import io.github.vinicreis.model.util.handleException
import kotlinx.coroutines.*
import java.io.DataInputStream
import java.io.EOFException
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
                val operationCode = reader.readUTF().toInt()
                val message = reader.readUTF()
                log.d("Starting Worker thread...")
                WorkerThread(server, socket, Operation.fromClient(operationCode), message).start()
            }
        } catch (e: EOFException) {
            handleException(TAG, "Invalid input received from client", e)
        } catch (e: SocketException) {
            log.d("Socket closed!")
        } catch (e: Throwable) {
            handleException(TAG, "Failed during dispatch execution", e)
        } finally {
            serverSocket.close()
        }
    }

    companion object {
        private const val TAG = "DispatcherThread"
    }
}