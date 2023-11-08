package io.github.vinicreis.node

import io.github.vinicreis.model.enums.OperationResult
import io.github.vinicreis.model.log.ConsoleLog
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
import io.github.vinicreis.node.thread.Dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.IOException
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
    override val log = ConsoleLog(TAG)

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

            doRequest(controllerHost, controllerPort, request, JoinResponse::class.java).run {
                if (result !== OperationResult.OK) {
                    error("Failed to join on controller server: $message")
                }
            }

            log.d("Node successfully joined!")
        } catch (e: IOException) {
            log.e("Failed to process JOIN operation", e)
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

            doRequest(
                controllerHost,
                controllerPort,
                controllerRequest,
                PutResponse::class.java
            ).run {
                PutResponse(
                    result = result,
                    timestamp = timestamp,
                    message = message
                )
            }
        } catch (e: IOException) {
            log.e("Failed to process PUT operation", e)

            PutResponse(
                result = OperationResult.ERROR,
                message = "Failed to process operation"
            )
        }
    }

    override fun replicate(request: ReplicationRequest): ReplicationResponse {
        with(request) {
            println("REPLICATION key: $key value: $value ts: $timestamp")
        }

        log.d("Saving replicated data locally...")
        keyValueRepository.replicate(request.key, request.value, request.timestamp)

        return ReplicationResponse(result = OperationResult.OK)
    }

    override fun exit() {
        try {
            val request = ExitRequest(InetAddress.getLocalHost().hostName, port)

            doRequest(controllerHost, controllerPort, request, ExitResponse::class.java).run {
                if (result !== OperationResult.OK) {
                    error("Failed to send EXIT request: $message")
                }
            }
        } catch (e: IOException) {
            log.e("Failed to process EXIT request", e)
        }
    }

    companion object {
        private const val TAG = "NodeImpl"
    }
}
