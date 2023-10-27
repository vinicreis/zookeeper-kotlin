package io.github.vinicreis.client

import io.github.vinicreis.client.thread.Worker
import io.github.vinicreis.model.enums.OperationResult
import io.github.vinicreis.model.log.ConsoleLog
import io.github.vinicreis.model.log.Log
import io.github.vinicreis.model.request.GetRequest
import io.github.vinicreis.model.request.PutRequest
import io.github.vinicreis.model.response.GetResponse
import io.github.vinicreis.model.response.PutResponse
import io.github.vinicreis.model.util.NetworkUtil.doRequest
import io.github.vinicreis.model.util.handleException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.ConnectException
import java.net.InetAddress
import java.util.*
import kotlin.collections.LinkedHashMap

class ClientImpl(
    private val port: Int,
    private val serverHost: String,
    private val serverPorts: List<Int>,
    debug: Boolean
) : Client {
    private val host: String = InetAddress.getLocalHost().canonicalHostName
    private val keyTimestampMap: HashMap<String, Long?> = LinkedHashMap()
    private val worker: Worker = Worker(this)
    private var job: Job? = null

    init {
        log.isDebug = debug
    }

    override fun start() {
        job = CoroutineScope(Dispatchers.IO).launch { worker.run() }
    }

    override fun stop() {
        job?.cancel()
        println("Encerrando...")
    }

    override fun get(key: String) {
        try {
            val serverPort = serverPorts.getAnyOrNull()!!
            val timestamp: Long = keyTimestampMap.getOrDefault(key, null) ?: 0L
            val request = GetRequest(host, port, key, timestamp)
            val response = doRequest(serverHost, serverPort, request, GetResponse::class.java)

            keyTimestampMap[key] = response.timestamp

            when (response.result) {
                OperationResult.OK,
                OperationResult.TRY_AGAIN_ON_OTHER_SERVER -> println(
                    "GET_${response.result} key: $key value: ${response.value} realizada no servidor" +
                            " $serverHost:$serverPort, meu timestamp ${request.timestamp} e do servidor ${response.timestamp}",
                )

                OperationResult.ERROR,
                OperationResult.NOT_FOUND -> println(
                    "Falha ao obter o valor da key $key: ${response.message}",
                )
            }
        } catch (e: Throwable) {
            log.e("Failed to process GET request", e)
        }
    }

    override fun put(key: String, value: String) {
        val serverPort = serverPorts.getAnyOrNull()!!

        try {
            val request = PutRequest(host, port, key, value)
            val response = doRequest(serverHost, serverPort, request, PutResponse::class.java)
            if (response.result != OperationResult.OK) {
                throw RuntimeException("PUT operation failed: ${response.message}")
            }

            keyTimestampMap[key] = response.timestamp

            println(
                "PUT_OK key: $key value: $value timestamp: ${response.timestamp} realizada no servidor $serverHost:$serverPort",
            )
        } catch (e: ConnectException) {
            log.e("Failed connect to socket on ${host}:${serverPort}", e)
        } catch (e: IOException) {
            log.e("Failed to run PUT operation", e)
        } catch (e: Throwable) {
            handleException(TAG, "Failed to complete PUT operation", e)
        }
    }

    private fun <T> List<T>.getAnyOrNull(): T? = when {
        isEmpty() -> null
        else -> get(kotlin.random.Random.nextInt(0, size))
    }

    companion object {
        private const val TAG = "ClientImpl"
        private val log: Log = ConsoleLog(TAG)
    }
}
