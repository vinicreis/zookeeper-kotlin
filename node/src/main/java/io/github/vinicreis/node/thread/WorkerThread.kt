package io.github.vinicreis.node.thread

import io.github.vinicreis.model.Server
import io.github.vinicreis.model.enums.Operation
import io.github.vinicreis.model.request.GetRequest
import io.github.vinicreis.model.request.PutRequest
import io.github.vinicreis.model.request.ReplicationRequest
import io.github.vinicreis.model.response.*
import io.github.vinicreis.model.util.Serializer.fromJson
import io.github.vinicreis.model.util.Serializer.toJson
import io.github.vinicreis.model.util.handleException
import kotlinx.coroutines.*
import java.io.DataOutputStream
import java.net.Socket

class WorkerThread(
    private val server: Server,
    private val socket: Socket,
    private val operation: Operation,
    private val request: String
){
    private var job: Job? = null

    fun start() {
        runBlocking {
            job = launch { run() }
        }
    }

    private suspend fun run() = withContext(Dispatchers.IO) {
        try {
            val writer = DataOutputStream(socket.getOutputStream())
            val response: Response = when (operation) {
                Operation.JOIN -> throw IllegalStateException("Nodes can not handle JOIN requests")
                Operation.PUT -> server.put(fromJson(request, PutRequest::class.java))
                Operation.REPLICATE -> server.replicate(fromJson(request, ReplicationRequest::class.java))
                Operation.GET -> server.get(fromJson(request, GetRequest::class.java))
                Operation.EXIT -> throw IllegalStateException("Nodes can not handle EXIT requests")
                else -> throw IllegalStateException("Operation unknown!")
            }
            writer.writeUTF(toJson(response))
            writer.flush()
            socket.close()
        } catch (e: Throwable) {
            handleException(TAG, "Failed during worker execution", e)
        }
    }

    companion object {
        private const val TAG = "WorkerThread"
    }
}
