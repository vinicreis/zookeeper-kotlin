package io.github.vinicreis.node.thread

import io.github.vinicreis.model.Server
import io.github.vinicreis.model.enums.Operation
import io.github.vinicreis.model.log.ConsoleLog
import io.github.vinicreis.model.request.GetRequest
import io.github.vinicreis.model.request.PutRequest
import io.github.vinicreis.model.request.ReplicationRequest
import io.github.vinicreis.model.response.Response
import io.github.vinicreis.model.util.Serializer.fromJson
import io.github.vinicreis.model.util.Serializer.toJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.DataOutputStream
import java.net.Socket
import java.net.SocketException
import kotlin.coroutines.CoroutineContext

class Worker(
    private val server: Server,
    private val socket: Socket,
    private val operation: Operation,
    private val request: String
){
    suspend fun run(
        coroutineContext: CoroutineContext = Dispatchers.IO
    ): Unit = withContext(coroutineContext) {
        try {
            val writer = DataOutputStream(socket.getOutputStream())
            val response: Response = when (operation) {
                Operation.JOIN -> error("Nodes can not handle JOIN requests")
                Operation.PUT -> server.put(fromJson(request, PutRequest::class.java))
                Operation.REPLICATE -> server.replicate(fromJson(request, ReplicationRequest::class.java))
                Operation.GET -> server.get(fromJson(request, GetRequest::class.java))
                Operation.EXIT -> error("Nodes can not handle EXIT requests")
            }

            writer.writeUTF(toJson(response))
            writer.flush()
            socket.close()
        } catch (e: SocketException) {
            log.e("Failed during worker execution", e)
        }
    }

    companion object {
        private const val TAG = "WorkerThread"
        private val log = ConsoleLog(TAG)
    }
}
