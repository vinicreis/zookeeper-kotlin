package io.github.vinicreis.controller.thread

import io.github.vinicreis.controller.Controller
import io.github.vinicreis.model.Server
import io.github.vinicreis.model.enums.Operation
import io.github.vinicreis.model.log.ConsoleLog
import io.github.vinicreis.model.log.Log
import io.github.vinicreis.model.request.ExitRequest
import io.github.vinicreis.model.request.GetRequest
import io.github.vinicreis.model.request.JoinRequest
import io.github.vinicreis.model.request.PutRequest
import io.github.vinicreis.model.request.ReplicationRequest
import io.github.vinicreis.model.util.Serializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException
import java.net.ServerSocket
import java.net.Socket
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

                send(message, socket, Operation.valueOf(operationCode))
            }
        } catch (e: EOFException) {
            log.e("Invalid input received from client", e)
        } catch (e: SocketException) {
            log.e("Socket closed!", e)
        } finally {
            serverSocket.close()
        }
    }

    private suspend fun send(
        request: String,
        socket: Socket,
        operation: Operation,
        coroutineContext: CoroutineContext = Dispatchers.IO
    ): Unit = withContext(coroutineContext) {
        try {
            val writer = DataOutputStream(socket.getOutputStream())
            val response = when (operation) {
                Operation.JOIN -> (server as? Controller)?.join(Serializer.fromJson(request, JoinRequest::class.java))
                    ?: error("Nodes can not handle JOIN requests")

                Operation.PUT -> server.put(Serializer.fromJson(request, PutRequest::class.java))
                Operation.REPLICATE -> server.replicate(Serializer.fromJson(request, ReplicationRequest::class.java))
                Operation.GET -> server.get(Serializer.fromJson(request, GetRequest::class.java))
                Operation.EXIT -> (server as? Controller)?.exit(Serializer.fromJson(request, ExitRequest::class.java))
                    ?: error("Nodes can not handle EXIT requests")
            }

            writer.writeUTF(Serializer.toJson(response))
            writer.flush()

            socket.close()
        } catch (e: IllegalStateException) {
            log.e("Something went wrong", e)
        }
    }

    companion object {
        private const val TAG = "DispatcherThread"
    }
}
