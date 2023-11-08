package io.github.vinicreis.controller

import io.github.vinicreis.controller.thread.Dispatcher
import io.github.vinicreis.model.enums.OperationResult
import io.github.vinicreis.model.log.ConsoleLog
import io.github.vinicreis.model.repository.KeyValueRepository
import io.github.vinicreis.model.repository.TimestampRepository
import io.github.vinicreis.model.request.ExitRequest
import io.github.vinicreis.model.request.JoinRequest
import io.github.vinicreis.model.request.PutRequest
import io.github.vinicreis.model.request.ReplicationRequest
import io.github.vinicreis.model.response.ExitResponse
import io.github.vinicreis.model.response.JoinResponse
import io.github.vinicreis.model.response.PutResponse
import io.github.vinicreis.model.response.ReplicationResponse
import io.github.vinicreis.model.util.NetworkUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.IOException

class ControllerImpl(
    override val port: Int,
    debug: Boolean,
    private val coroutineScope: CoroutineScope
) : Controller {
    private val timestampRepository: TimestampRepository = TimestampRepository()
    override val keyValueRepository: KeyValueRepository = KeyValueRepository(timestampRepository)
    private val dispatcher: Dispatcher = Dispatcher(this)
    private val nodes: MutableList<Controller.Node> = mutableListOf()
    private var timestampJob: Job? = null
    private var dispatcherJob: Job? = null
    override val log = ConsoleLog(TAG)

    init {
        log.isDebug = debug
    }

    override fun start() {
        timestampJob = coroutineScope.launch { timestampRepository.run() }
        dispatcherJob = coroutineScope.launch { dispatcher.run() }
    }

    override fun stop() {
        timestampJob?.cancel()
        dispatcherJob?.cancel()
    }

    override fun join(request: JoinRequest): JoinResponse {
        log.d("Joining node ${request.host}:${request.port}")

        if (hasNode(Controller.Node(request))) {
            log.d("Node ${request.host}:${request.port} already joined!")

            return JoinResponse(
                result = OperationResult.ERROR,
                message = "Node ${request.host}:${request.port} is already joined!"
            )
        }

        nodes.add(Controller.Node(request))
        log.d("Node ${request.host}:${request.port} joined!")

        return JoinResponse(
            result = OperationResult.OK
        )
    }

    override fun put(request: PutRequest): PutResponse {
        return try {
            with(request) {
                print("Client $host:$port PUT key $key with value = $value")
            }

            val timestamp = keyValueRepository.insert(request.key, request.value)
            val replicationResponse = replicate(
                ReplicationRequest(
                    request.host,
                    request.port,
                    request.key,
                    request.value,
                    timestamp
                )
            )

            PutResponse(
                result = replicationResponse.result,
                message = replicationResponse.message,
                timestamp = timestamp
            )
        } catch (e: IllegalStateException) {
            log.e("Failed to process PUT operation", e)

            PutResponse(
                result = OperationResult.ERROR,
                message = "Failed to process PUT operation"
            )
        }
    }

    override fun replicate(request: ReplicationRequest): ReplicationResponse {
        val nodesWithError: MutableList<Controller.Node?> = ArrayList(nodes.size)

        // Start all threads to join them later to process request asynchronously
        runBlocking {
            nodes.map { node ->
                launch {
                    try {
                        NetworkUtil.doRequest(
                            node.host,
                            node.port,
                            request,
                            ReplicationResponse::class.java,
                            log.isDebug
                        ).run {
                            if (result != OperationResult.OK) {
                                nodesWithError.add(node)
                            }

                            log.d("Replication to node $node got result: $result")
                        }
                    } catch (e: IOException) {
                        log.e("Failed to process request to node $node", e)
                        nodesWithError.add(node)
                    }
                }
            }.joinAll()
        }

        if (nodesWithError.isEmpty()) {
            with(request) {
                println(
                    "Enviando PUT_OK ao Cliente $host:$port da key: $key ts: $timestamp",
                )
            }

            return ReplicationResponse(
                result = OperationResult.OK
            )
        }

        return ReplicationResponse(
            result = OperationResult.ERROR,
            message = "Failed to replicate data on servers ${nodesWithError.joinToString(", ")}"
        )
    }

    override fun exit(request: ExitRequest): ExitResponse {
        val node = Controller.Node(request)

        if (!hasNode(node)) return ExitResponse(
            result = OperationResult.ERROR,
            message = "Server ${request.host}:${request.port} not connected!"
        )

        nodes.remove(node)
        log.d("Node $node exited!")

        return ExitResponse(
            result = OperationResult.OK
        )
    }

    private fun hasNode(node: Controller.Node): Boolean {
        return nodes.contains(node)
    }

    companion object {
        private const val TAG = "ControllerImpl"
    }
}
