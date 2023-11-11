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
import java.io.IOException
import java.net.ConnectException
import java.net.InetAddress
import kotlin.random.Random

class ClientImpl(
    private val port: Int,
    private val serverHost: String,
    private val serverPorts: List<Int> = Client.DefaultServerPorts,
    debug: Boolean
) : Client {
    private val host: String = InetAddress.getLocalHost().canonicalHostName
    private val keyTimestampMap: HashMap<String, Long> = LinkedHashMap()
    private val worker: Worker = Worker(this, debug)

    init {
        log.isDebug = debug
    }

    override fun start() {
        worker.run()
    }

    override fun stop() {
        println("Encerrando...")
    }

    override fun get(key: String) {
        try {
            val serverPort = serverPorts.random()
            val timestamp = keyTimestampMap.getOrDefault(key, 0L)
            val request = GetRequest(
                host = host,
                port = port,
                key = key,
                timestamp = timestamp
            )

            doRequest(
                host = serverHost,
                port = serverPort,
                request = request,
                responseClass = GetResponse::class.java
            ).run {
                keyTimestampMap[key] = timestamp

                when (result) {
                    OperationResult.OK,
                    OperationResult.TRY_AGAIN_ON_OTHER_SERVER -> println(
                        "GET_$result key: $key value: $value realizada no servidor" +
                                " $serverHost:$serverPort, meu timestamp ${request.timestamp} " +
                                "e do servidor $timestamp",
                    )
                    OperationResult.ERROR,
                    OperationResult.NOT_FOUND -> println("Falha ao obter o valor da key $key: $message")
                }
            }

        } catch (e: IOException) {
            log.e("Failed to process GET request", e)

            println(
                "Falha ao obter o valor da key $key"
            )
        }
    }

    override fun put(key: String, value: String) {
        val serverPort = serverPorts.random()

        try {
            val request = PutRequest(
                host = host,
                port = port,
                key = key,
                value = value
            )

            doRequest(
                host = serverHost,
                port = serverPort,
                request = request,
                responseClass = PutResponse::class.java,
                debug = log.isDebug
            ).run {
                when(result) {
                    OperationResult.NOT_FOUND -> {
                        println("Key $key not found on server")
                    }
                    OperationResult.ERROR -> {
                        println("Failed to get key $key from server")
                    }
                    OperationResult.TRY_AGAIN_ON_OTHER_SERVER -> {
                        println(
                            "Failed to get key $key from server. Please, try again or try other server"
                        )
                    }
                    OperationResult.OK -> {
                        keyTimestampMap[key] = timestamp ?: 0L

                        println(
                            "PUT_OK key: $key value: $value timestamp: $timestamp" +
                                    " realizada no servidor $serverHost:$serverPort",
                        )
                    }
                }
            }
        } catch (e: ConnectException) {
            log.e("Failed connect to socket on ${host}:${serverPort}", e)
        } catch (e: IOException) {
            log.e("Failed to run PUT operation", e)
        }
    }

    private fun <T> List<T>.random(): T = get(Random.nextInt(size))

    companion object {
        private const val TAG = "ClientImpl"
        private val log: Log = ConsoleLog(TAG)
    }
}
