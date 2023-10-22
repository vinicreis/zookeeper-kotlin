package io.github.vinicreis.client

import io.github.vinicreis.client.thread.Worker
import io.github.vinicreis.model.enums.OperationResult
import io.github.vinicreis.model.log.ConsoleLog
import io.github.vinicreis.model.log.Log
import io.github.vinicreis.model.request.GetRequest
import io.github.vinicreis.model.request.PutRequest
import io.github.vinicreis.model.response.GetResponse
import io.github.vinicreis.model.response.PutResponse
import io.github.vinicreis.model.util.IOUtil.printLn
import io.github.vinicreis.model.util.IOUtil.printfLn
import io.github.vinicreis.model.util.NetworkUtil.doRequest
import io.github.vinicreis.model.util.handleException
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

    init {
        log.isDebug = debug
    }

    override fun start() {
        worker.start()
    }

    override fun stop() {
        printLn("Encerrando...")
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
                OperationResult.TRY_AGAIN_ON_OTHER_SERVER -> printfLn(
                    "GET_%s key: %s value: %s realizada no servidor %s:%d, meu timestamp %d e do servidor %d",
                    response.result,
                    key,
                    response.value,
                    serverHost,
                    serverPort,
                    request.timestamp,
                    response.timestamp
                )

                OperationResult.ERROR,
                OperationResult.NOT_FOUND -> printfLn(
                    "Falha ao obter o valor da key %s: %s",
                    key,
                    response.message
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
                throw RuntimeException(String.format("PUT operation failed: %s", response.message))
            }

            keyTimestampMap[key] = response.timestamp

            printfLn(
                "PUT_OK key: %s value: %s timestamp: %d realizada no servidor %s:%d",
                key,
                value,
                response.timestamp,
                serverHost,
                serverPort
            )
        } catch (e: ConnectException) {
            log.e(String.format("Failed connect to socket on ${host}:${serverPort}", host, port), e)
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
