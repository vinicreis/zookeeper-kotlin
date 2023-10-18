package io.github.vinicreis.controller.thread

import io.github.vinicreis.controller.Controller
import io.github.vinicreis.model.Server
import io.github.vinicreis.model.enums.Operation
import io.github.vinicreis.model.request.*
import io.github.vinicreis.model.response.*
import io.github.vinicreis.model.util.AssertionUtils.handleException
import io.github.vinicreis.model.util.Serializer.fromJson
import io.github.vinicreis.model.util.Serializer.toJson
import java.io.DataOutputStream
import java.net.Socket

class WorkerThread(
    private val server: Server,
    private val socket: Socket,
    private val operation: Operation,
    private val request: String
) : Thread() {
    override fun run() {
        try {
            val writer = DataOutputStream(socket.getOutputStream())
            val response: Response = when (operation) {
                Operation.JOIN -> (server as? Controller)?.join(fromJson(request, JoinRequest::class.java))
                    ?: throw IllegalStateException("Nodes can not handle JOIN requests")
                Operation.PUT -> server.put(fromJson(request, PutRequest::class.java))
                Operation.REPLICATE -> server.replicate(fromJson(request, ReplicationRequest::class.java))
                Operation.GET -> server.get(fromJson(request, GetRequest::class.java))
                Operation.EXIT -> (server as? Controller)?.exit(fromJson(request, ExitRequest::class.java))
                    ?: throw IllegalStateException("Nodes can not handle EXIT requests")
                else -> throw IllegalStateException("Operation unknown!")
            }

            writer.writeUTF(toJson(response))
            writer.flush()

            socket.close()
        } catch (e: Exception) {
            handleException(TAG, "Failed during worker execution", e)
        }
    }

    companion object {
        private const val TAG = "WorkerThread"
    }
}
