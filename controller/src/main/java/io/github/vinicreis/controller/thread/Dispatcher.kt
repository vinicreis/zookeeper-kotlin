package io.github.vinicreis.controller.thread

import io.github.vinicreis.model.Server
import io.github.vinicreis.model.enums.Operation
import io.github.vinicreis.model.log.ConsoleLog
import io.github.vinicreis.model.log.Log
import io.github.vinicreis.model.util.AssertionUtils.handleException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.io.EOFException
import java.net.ServerSocket
import java.net.SocketException

/**
 * Thread to keep listening and dispatch workers to process Controller's operations.
 * @see WorkerThread
 */
class Dispatcher(private val server: Server) {
    private val log: Log = ConsoleLog(TAG)
    private val serverSocket: ServerSocket = ServerSocket(server.port)
    private var running = true

    private suspend fun run() = withContext(Dispatchers.IO) {
        try {
            while (running) {
                log.d("Listening for operation requests...")
                val socket = serverSocket.accept()
                log.d("Request received!")
                val reader = DataInputStream(socket.getInputStream())
                val operationCode = reader.readUTF()
                val message = reader.readUTF()
                log.d("Starting Worker thread...")
                WorkerThread(server, socket, Operation.fromCode(operationCode), message).start()
            }
        } catch (e: EOFException) {
            handleException(TAG, "Invalid input received from client", e)
        } catch (e: SocketException) {
            log.d("Socket closed!")
        } catch (e: Exception) {
            handleException(TAG, "Failed during dispatch execution", e)
        }
    }

    fun start() {

    }

    fun stop() {
        try {
            super.interrupt()
            serverSocket.close()
            running = false
        } catch (e: Exception) {
            handleException(TAG, "Failed while interrupting dispatcher!", e)
        }
    }

    companion object {
        private const val TAG = "DispatcherThread"
    }
}