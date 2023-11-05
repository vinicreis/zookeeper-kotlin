package io.github.vinicreis.node

import io.github.vinicreis.model.enums.OperationResult
import io.github.vinicreis.model.log.ConsoleLog
import io.github.vinicreis.model.log.Log
import io.github.vinicreis.model.repository.KeyValueRepository
import io.github.vinicreis.model.request.ExitRequest
import io.github.vinicreis.model.request.JoinRequest
import io.github.vinicreis.model.request.PutRequest
import io.github.vinicreis.model.request.ReplicationRequest
import io.github.vinicreis.model.response.ExitResponse
import io.github.vinicreis.model.response.JoinResponse
import io.github.vinicreis.model.response.PutResponse
import io.github.vinicreis.model.response.ReplicationResponse
import io.github.vinicreis.model.util.NetworkUtil.doRequest
import io.github.vinicreis.model.util.handleException
import io.github.vinicreis.node.thread.Dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.net.InetAddress

class NodeImpl(
    override val port: Int,
    private val controllerHost: String,
    private val controllerPort: Int,
    debug: Boolean,
    private val coroutineScope: CoroutineScope
) : Node {
    override val keyValueRepository: KeyValueRepository = KeyValueRepository()
    private val dispatcher: Dispatcher = Dispatcher(this)
    private var job: Job? = null

    init {
        log.isDebug = debug
    }

    override fun start() {
        job = coroutineScope.launch { dispatcher.run() }
        join()
    }

    override fun stop() {
        exit()
        job?.cancel()
    }

    override fun join() {
        try {
            val request = JoinRequest(InetAddress.getLocalHost().hostName, port)
            val response = doRequest(controllerHost, controllerPort, request, JoinResponse::class.java)
            if (response.result !== OperationResult.OK) {
                throw RuntimeException("Failed to join on controller server: ${response.message}")
            }

            log.d("Node successfully joined!")
        } catch (e: Throwable) {
            handleException(TAG, "Failed to process JOIN operation", e)
        }
    }

    override fun put(request: PutRequest): PutResponse {
        return try {
            with(request) {
                println("Encaminhando $host:$port PUT key: $key value: $value",)
            }
            val controllerRequest = PutRequest(
                InetAddress.getLocalHost().hostName,
                port,
                request.key,
                request.value
            )
            val controllerResponse = doRequest(
                controllerHost,
                controllerPort,
                controllerRequest,
                PutResponse::class.java
            )

            PutResponse(
                result = OperationResult.OK,
                timestamp = controllerResponse.timestamp,
                message = controllerResponse.message
            )
        } catch (e: Throwable) {
            handleException(TAG, "Failed to process PUT operation", e)

            PutResponse(
                result = OperationResult.ERROR,
                message = "Failed to process operation"
            )
        }
    }

    override fun replicate(request: ReplicationRequest): ReplicationResponse {
        return try {
            with(request) {
                println("REPLICATION key: $key value: $value ts: $timestamp")
            }
            log.d("Saving replicated data locally...")
            keyValueRepository.replicate(request.key, request.value, request.timestamp)
            ReplicationResponse(result = OperationResult.OK)
        } catch (e: Throwable) {
            handleException(TAG, "Failed to process REPLICATE operation", e)

            ReplicationResponse(
                result = OperationResult.ERROR,
                message = "Failed to process operation"
            )
        }
    }

    override fun exit() {
        try {
            val request = ExitRequest(InetAddress.getLocalHost().hostName, port)
            log.d("Leaving controller...")
            val response = doRequest(controllerHost, controllerPort, request, ExitResponse::class.java)
            if (response.result !== OperationResult.OK) {
                throw RuntimeException("Failed to send EXIT request: ${response.message}")
            }
            log.d("Successfully left on controller!")
        } catch (e: Throwable) {
            handleException(TAG, "Failed to process EXIT request", e)
        }
    }

    companion object {
        private const val TAG = "NodeImpl"
        private val log: Log = ConsoleLog(TAG)
    }
}
