package io.github.vinicreis.controller.thread

import io.github.vinicreis.model.Server
import io.github.vinicreis.model.enums.Operation
import io.github.vinicreis.model.log.ConsoleLog
import io.github.vinicreis.model.log.Log
import java.io.DataInputStream
import java.io.EOFException
import java.net.ServerSocket
import java.net.SocketException

/**
 * Thread to keep listening and dispatch workers to process Controller's operations.
 * @see WorkerThread
 */
class DispatcherThread(private val server: Server) : Thread() {
    private val log: Log = ConsoleLog(TAG)
    private val serverSocket: ServerSocket
    private var running = true

    init {
        serverSocket = ServerSocket(server.port)
    }

    override fun run() {
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

    override fun interrupt() {
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