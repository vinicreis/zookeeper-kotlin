package io.github.vinicreis.controller.thread

import io.github.vinicreis.controller.Controller
import io.github.vinicreis.model.enums.Result
import io.github.vinicreis.model.log.ConsoleLog
import io.github.vinicreis.model.log.Log
import io.github.vinicreis.model.request.ReplicationRequest
import io.github.vinicreis.model.response.ReplicationResponse
import io.github.vinicreis.model.util.NetworkUtil.doRequest
import java.io.IOException
import kotlin.concurrent.Volatile

/**
 * Thread to send a REPLICATE request to nodes asynchronously.
 */
class ReplicateThread(val node: Controller.Node, private val request: ReplicationRequest?, private val debug: Boolean) :
    Thread() {
    @Volatile
    var result: Result = null
        private set

    override fun run() {
        result = try {
            log.d(
                String.format(
                    "Sending replication request of key %s with value %s to %s:%d",
                    request!!.key,
                    request.value,
                    node.host,
                    node.port
                )
            )
            val response = doRequest(
                node.host,
                node.port,
                request,
                ReplicationResponse::class.java,
                debug
            )

            response
        } catch (e: IOException) {
            handleException(TAG, String.format("Failed during REPLICATE to node %s:%d", node.host, node.port), e)
            Result.EXCEPTION
        }
    }

    companion object {
        private const val TAG = "ReplicateThread"
        private val log: Log = ConsoleLog(TAG)
    }
}
