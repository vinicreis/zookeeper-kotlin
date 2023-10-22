package io.github.vinicreis.controller

import io.github.vinicreis.controller.thread.Dispatcher
import io.github.vinicreis.model.enums.OperationResult
import io.github.vinicreis.model.log.ConsoleLog
import io.github.vinicreis.model.log.Log
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
import io.github.vinicreis.model.util.IOUtil.printfLn
import io.github.vinicreis.model.util.NetworkUtil
import io.github.vinicreis.model.util.handleException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ControllerImpl(override val port: Int, debug: Boolean) : Controller {
    private val timestampRepository: TimestampRepository = TimestampRepository()
    override val keyValueRepository: KeyValueRepository = KeyValueRepository(timestampRepository)
    private val dispatcher: Dispatcher = Dispatcher(this)
    private val nodes: MutableList<Controller.Node> = mutableListOf()

    init {
        log.isDebug = debug
    }

    override fun start() {
        try {
            timestampRepository.start()
            dispatcher.start()
        } catch (e: Throwable) {
            handleException(TAG, "Failed to start Controller!", e)
        }
    }

    override fun stop() {
        try {
            timestampRepository.stop()
            dispatcher.stop()
        } catch (e: Throwable) {
            handleException(TAG, "Failed while stopping Controller", e)
        }
    }

    override fun join(request: JoinRequest): JoinResponse {
        return try {
            log.d(String.format("Joining node %s:%d", request.host, request.port))

            if (hasNode(Controller.Node(request))) {
                log.d(String.format("Node %s:%d already joined!", request.host, request.port))

                return JoinResponse(
                    result = OperationResult.ERROR,
                    message = "Node ${request.host}:${request.port} is already joined!"
                )
            }

            nodes.add(Controller.Node(request))
            log.d("Node ${request.host}:${request.port} joined!")

            JoinResponse(
                result = OperationResult.OK
            )
        } catch (e: Throwable) {
            handleException(TAG, "Failed to process JOIN operation", e)

            JoinResponse(
                result = OperationResult.ERROR,
                message = "Failed to process operation"
            )
        }
    }

    override fun put(request: PutRequest): PutResponse {
        return try {
            printfLn(
                "Cliente %s:%d PUT key: %s value: %s",
                request.host,
                request.port,
                request.key,
                request.value
            )

            var timestamp = keyValueRepository.insert(request.key, request.value)
            val replicationResponse = replicate(
                ReplicationRequest(
                    request.host,
                    request.port,
                    request.key,
                    request.value,
                    timestamp
                )
            )

            /*
             * The code below is used to simulate the TRY_OTHER_SERVER_OR_LATER scenario.
             * If the client host is localhost ("host.docker.internal:10092") and the port is 10092,
             * along with the key "testing" and the value "tosol", the timestamp returned should be greater
             * than the saved one. So, when the same client tries to get the value with
             * the key "testing", the timestamp sent would be greater and the server
             * should return the TRY_OTHER_SERVER result.
             */
            if (request.host == "host.docker.internal" && request.port == 10092 && request.key == "testing" && request.value == "tosol") {
                timestamp += 1000L
            }

            PutResponse(
                result = replicationResponse.result,
                message = replicationResponse.message,
                timestamp = timestamp
            )
        } catch (e: Throwable) {
            handleException(TAG, "Failed to process PUT operation", e)

            PutResponse(
                result = OperationResult.ERROR,
                message = "Failed to process PUT operation"
            )
        }
    }

    override fun replicate(request: ReplicationRequest): ReplicationResponse {
        return try {
            val nodesWithError: MutableList<Controller.Node?> = ArrayList(nodes.size)

            // Start all threads to join them later to process request asynchronously
            runBlocking {
                val jobs = mutableListOf<Job>()

                for (node in nodes) {
                    val job = launch {
                        val result = NetworkUtil.doRequest(
                            node.host,
                            node.port,
                            request,
                            ReplicationResponse::class.java,
                            log.isDebug
                        )

                        if (result.result != OperationResult.OK) nodesWithError.add(node)

                        log.d(
                            String.format(
                                "Replication to node %s got result: %s",
                                node,
                                result.toString()
                            )
                        )
                    }

                    jobs.add(job)
                }

                jobs.forEach { it.join() }
            }

            if (nodesWithError.isEmpty()) {
                printfLn(
                    "Enviando PUT_OK ao Cliente %s:%d da key: %s ts: %d",
                    request.host,
                    request.port,
                    request.key,
                    request.timestamp
                )

                return ReplicationResponse(
                    result = OperationResult.OK
                )
            }

            ReplicationResponse(
                result = OperationResult.ERROR,
                message = "Failed to replicate data on servers ${nodesWithError.joinToString(", ")}"
            )
        } catch (e: Throwable) {
            handleException(TAG, "Failed to process REPLICATE operation", e)

            ReplicationResponse(
                result = OperationResult.ERROR,
                message = "Failed to process REPLICATE operation"
            )
        }
    }

    override fun exit(request: ExitRequest): ExitResponse {
        return try {
            val node = Controller.Node(request)
            if (!hasNode(node)) return ExitResponse(
                result = OperationResult.ERROR,
                message = "Server ${request.host}:${request.port} not connected!"
            )

            nodes.remove(node)
            log.d(String.format("Node %s exited!", node))

            ExitResponse(
                result = OperationResult.OK
            )
        } catch (e: Throwable) {
            handleException(TAG, "Failed to remove node ${request.host}:${request.port}", e)

            ExitResponse(
                result = OperationResult.ERROR,
                message = "Failed to remove node ${request.host}:${request.port}"
            )
        }
    }

    private fun hasNode(node: Controller.Node): Boolean {
        return nodes.contains(node)
    }

    companion object {
        private const val TAG = "ControllerImpl"
        private val log: Log = ConsoleLog(TAG)
    }
}
