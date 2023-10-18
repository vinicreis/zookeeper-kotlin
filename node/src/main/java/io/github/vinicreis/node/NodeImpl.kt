package io.github.vinicreis.node

import io.github.vinicreis.model.enums.Result
import io.github.vinicreis.model.log.ConsoleLog
import io.github.vinicreis.model.log.Log
import io.github.vinicreis.model.repository.KeyValueRepository
import io.github.vinicreis.model.request.ExitRequest
import io.github.vinicreis.model.request.JoinRequest
import io.github.vinicreis.model.request.PutRequest
import io.github.vinicreis.model.request.ReplicationRequest
import io.github.vinicreis.model.response.*
import io.github.vinicreis.model.util.IOUtil.printfLn
import io.github.vinicreis.model.util.NetworkUtil.doRequest
import io.github.vinicreis.node.thread.DispatcherThread
import java.net.InetAddress

class NodeImpl(
    override val port: Int,
    private val controllerHost: String,
    private val controllerPort: Int,
    debug: Boolean
) : Node {
    override val keyValueRepository: KeyValueRepository
    private val dispatcher: DispatcherThread

    init {
        dispatcher = DispatcherThread(this)
        keyValueRepository = KeyValueRepository()
        log.isDebug = debug
    }

    override fun start() {
        dispatcher.start()
        join()
    }

    override fun stop() {
        dispatcher.interrupt()
        exit()
    }

    override fun join() {
        try {
            val request = JoinRequest(InetAddress.getLocalHost().hostName, port)
            val response = doRequest(controllerHost, controllerPort, request, JoinResponse::class.java)
            if (response.result !== Result.OK) {
                throw RuntimeException(String.format("Failed to join on controller server: %s", response.message))
            }

            // TODO: Save controller info to validate REPLICATE requests
            log.d("Node successfully joined!")
        } catch (e: Exception) {
            handleException(TAG, "Failed to process JOIN operation", e)
        }
    }

    override fun put(request: PutRequest?): PutResponse? {
        return try {
            printfLn(
                "Encaminhando %s:%d PUT key: %s value: %s",
                request!!.getHost(),
                request.getPort(),
                request.key,
                request.value
            )
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
            if (controllerResponse.result !== Result.OK) {
                PutResponse.Builder()
                    .timestamp(controllerResponse.timestamp)
                    .result<Response.AbstractBuilder<PutResponse>>(Result.ERROR)
                    .message<Response.AbstractBuilder<PutResponse>>(controllerResponse.message)
                    .build()
            } else PutResponse.Builder()
                .timestamp(controllerResponse.timestamp)
                .result<Response.AbstractBuilder<PutResponse>>(Result.OK)
                .build()
        } catch (e: Exception) {
            handleException(TAG, "Failed to process PUT operation", e)
            PutResponse.Builder().exception<Response.AbstractBuilder<PutResponse>>(e).build()
        }
    }

    override fun replicate(request: ReplicationRequest?): ReplicationResponse? {
        return try {
            printfLn(
                "REPLICATION key: %s value: %s ts: %d",
                request!!.key,
                request.value,
                request.timestamp
            )
            log.d("Saving replicated data locally...")
            keyValueRepository.replicate(request.key, request.value, request.timestamp)
            ReplicationResponse.Builder().result<Response.AbstractBuilder<ReplicationResponse>>(Result.OK).build()
        } catch (e: Exception) {
            handleException(TAG, "Failed to process REPLICATE operation", e)
            ReplicationResponse.Builder().exception<Response.AbstractBuilder<ReplicationResponse>>(e).build()
        }
    }

    override fun exit() {
        try {
            val request = ExitRequest(InetAddress.getLocalHost().hostName, port)
            log.d("Leaving controller...")
            val response = doRequest(controllerHost, controllerPort, request, ExitResponse::class.java)
            if (response.result !== Result.OK) {
                throw RuntimeException(String.format("Failed to send EXIT request: %s", response.message))
            }
            log.d("Successfully left on controller!")
        } catch (e: Exception) {
            handleException(TAG, "Failed to process EXIT request", e)
        }
    }

    companion object {
        private const val TAG = "NodeImpl"
        private val log: Log = ConsoleLog(TAG)
    }
}
