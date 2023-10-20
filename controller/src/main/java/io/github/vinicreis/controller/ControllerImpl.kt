package io.github.vinicreis.controller

import io.github.vinicreis.controller.thread.DispatcherThread
import io.github.vinicreis.controller.thread.ReplicateThread
import io.github.vinicreis.model.enums.Result
import io.github.vinicreis.model.log.ConsoleLog
import io.github.vinicreis.model.log.Log
import io.github.vinicreis.model.repository.KeyValueRepository
import io.github.vinicreis.model.repository.TimestampRepository
import io.github.vinicreis.model.request.ExitRequest
import io.github.vinicreis.model.request.JoinRequest
import io.github.vinicreis.model.request.PutRequest
import io.github.vinicreis.model.request.ReplicationRequest
import io.github.vinicreis.model.response.*
import io.github.vinicreis.model.util.AssertionUtils.handleException
import io.github.vinicreis.model.util.IOUtil.printfLn

class ControllerImpl(override val port: Int, debug: Boolean) : Controller {
    private val timestampRepository: TimestampRepository = TimestampRepository()
    override val keyValueRepository: KeyValueRepository = KeyValueRepository(timestampRepository)
    private val dispatcher: DispatcherThread = DispatcherThread(this)
    private val nodes: MutableList<Controller.Node> = mutableListOf()

    init {
        log.isDebug = debug
    }

    override fun start() {
        try {
            timestampRepository.start()
            dispatcher.start()
        } catch (e: Exception) {
            handleException(TAG, "Failed to start Controller!", e)
        }
    }

    override fun stop() {
        try {
            timestampRepository.stop()
            dispatcher.interrupt()
        } catch (e: Exception) {
            handleException(TAG, "Failed while stopping Controller", e)
        }
    }

    override fun join(request: JoinRequest): JoinResponse {
        return try {
            log.d(String.format("Joining node %s:%d", request.host, request.port))

            if (hasNode(Controller.Node(request))) {
                log.d(String.format("Node %s:%d already joined!", request.host, request.port))

                return JoinResponse.Builder()
                    .result<Response.AbstractBuilder<JoinResponse>>(Result.ERROR)
                    .message<Response.AbstractBuilder<JoinResponse>>(
                        String.format(
                            "Node %s:%d already joined!",
                            request.host,
                            request.port
                        )
                    ).build()
            }

            nodes.add(Controller.Node(request))
            log.d(String.format("Node %s:%d joined!", request.host, request.port))

            JoinResponse.Builder()
                .result<Response.AbstractBuilder<JoinResponse>>(Result.OK)
                .build()
        } catch (e: Exception) {
            handleException(TAG, "Failed to process JOIN operation", e)

            JoinResponse.Builder().exception<Response.AbstractBuilder<JoinResponse>>(e).build()
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

            if (replicationResponse.result === Result.OK) {
                PutResponse.Builder()
                    .timestamp(timestamp)
                    .result<Response.AbstractBuilder<PutResponse>>(Result.OK)
                    .build()
            } else PutResponse.Builder()
                .result<Response.AbstractBuilder<PutResponse>>(replicationResponse.result!!)
                .message<Response.AbstractBuilder<PutResponse>>(
                    String.format(
                        "Falha ao adicionar valor %s a chave %s",
                        request.value,
                        request.key
                    )
                )
                .build()
        } catch (e: Exception) {
            handleException(TAG, "Failed to process PUT operation", e)
            PutResponse.Builder().exception<Response.AbstractBuilder<PutResponse>>(e).build()
        }
    }

    override fun replicate(request: ReplicationRequest): ReplicationResponse {
        return try {
            val nodesWithError: MutableList<Controller.Node?> = ArrayList(nodes.size)
            val threads: MutableList<ReplicateThread> = ArrayList(nodes.size)

            // Start all threads to join them later to process request asynchronously
            for (node in nodes) {
                val replicateThread = ReplicateThread(node, request, log.isDebug)
                threads.add(replicateThread)
                log.d(String.format("Starting replication to node %s...", node.toString()))
                replicateThread.start()
            }

            // Wait for each result at once, but at least they are already being processed
            for (thread in threads) {
                thread.join()
                if (thread.result !== Result.OK) nodesWithError.add(thread.node)
                log.d(
                    String.format(
                        "Replication to node %s got result: %s",
                        thread.node,
                        thread.result.toString()
                    )
                )
            }

            if (nodesWithError.isEmpty()) {
                printfLn(
                    "Enviando PUT_OK ao Cliente %s:%d da key: %s ts: %d",
                    request.host,
                    request.port,
                    request.key,
                    request.timestamp
                )

                return ReplicationResponse.Builder()
                    .result<Response.AbstractBuilder<ReplicationResponse>>(Result.OK)
                    .build()
            }

            ReplicationResponse.Builder()
                .result<Response.AbstractBuilder<ReplicationResponse>>(Result.ERROR)
                .message<Response.AbstractBuilder<ReplicationResponse>>(
                    "Falha ao replicar o dado no peer no(s) servidor(s) ${nodesWithError.joinToString(", ")}"
                ).build()
        } catch (e: Exception) {
            handleException(TAG, "Failed to process REPLICATE operation", e)

            ReplicationResponse.Builder().exception<Response.AbstractBuilder<ReplicationResponse>>(e).build()
        }
    }

    override fun exit(request: ExitRequest): ExitResponse {
        return try {
            val node = Controller.Node(request)
            if (!hasNode(node)) return ExitResponse.Builder()
                .result<Response.AbstractBuilder<ExitResponse>>(Result.ERROR)
                .message<Response.AbstractBuilder<ExitResponse>>("Servidor %s:%d não conectado!")
                .build()

            nodes.remove(node)
            log.d(String.format("Node %s exited!", node))

            ExitResponse.Builder()
                .result<Response.AbstractBuilder<ExitResponse>>(Result.OK)
                .build()
        } catch (e: Exception) {
            handleException(TAG, String.format("Failed to remove node %s:%d", request.host, request.port), e)

            ExitResponse.Builder()
                .exception<Response.AbstractBuilder<ExitResponse>>(e)
                .build()
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
