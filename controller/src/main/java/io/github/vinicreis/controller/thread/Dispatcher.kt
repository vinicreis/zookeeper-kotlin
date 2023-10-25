package io.github.vinicreis.controller.thread

import io.github.vinicreis.controller.Controller
import io.github.vinicreis.model.Server
import io.github.vinicreis.model.enums.Operation
import io.github.vinicreis.model.log.ConsoleLog
import io.github.vinicreis.model.log.Log
import io.github.vinicreis.model.request.*
import io.github.vinicreis.model.util.Serializer
import io.github.vinicreis.model.util.handleException
import kotlinx.coroutines.*
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException
import java.net.ServerSocket
import java.net.Socket
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

                send(message, socket, Operation.fromClient(operationCode.toInt()))
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

    private suspend fun send(
        request: String,
        socket: Socket,
        operation: Operation
    ) = withContext(Dispatchers.IO) {
        handleException(TAG, "Failed during worker execution") {
            val writer = DataOutputStream(socket.getOutputStream())
            val response = when (operation) {
                Operation.JOIN -> (server as? Controller)?.join(Serializer.fromJson(request, JoinRequest::class.java))
                    ?: throw IllegalStateException("Nodes can not handle JOIN requests")
                Operation.PUT -> server.put(Serializer.fromJson(request, PutRequest::class.java))
                Operation.REPLICATE -> server.replicate(Serializer.fromJson(request, ReplicationRequest::class.java))
                Operation.GET -> server.get(Serializer.fromJson(request, GetRequest::class.java))
                Operation.EXIT -> (server as? Controller)?.exit(Serializer.fromJson(request, ExitRequest::class.java))
                    ?: throw IllegalStateException("Nodes can not handle EXIT requests")
                else -> throw IllegalStateException("Operation unknown!")
            }

            writer.writeUTF(Serializer.toJson(response))
            writer.flush()

            socket.close()
        }
    }

    companion object {
        private const val TAG = "DispatcherThread"
    }
}