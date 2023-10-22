package io.github.vinicreis.client

import io.github.vinicreis.client.thread.WorkerThread
import io.github.vinicreis.model.enums.Result
import io.github.vinicreis.model.log.ConsoleLog
import io.github.vinicreis.model.log.Log
import io.github.vinicreis.model.request.GetRequest
import io.github.vinicreis.model.request.PutRequest
import io.github.vinicreis.model.response.GetResponse
import io.github.vinicreis.model.response.PutResponse
import io.github.vinicreis.model.util.Utils.isNullOrEmpty
import io.github.vinicreis.model.util.IOUtil.printLn
import io.github.vinicreis.model.util.IOUtil.printfLn
import io.github.vinicreis.model.util.NetworkUtil.doRequest
import java.io.IOException
import java.net.ConnectException
import java.net.InetAddress
import java.util.*

class ClientImpl(
    private val port: Int,
    private val serverHost: String,
    private val serverPorts: List<Int>,
    debug: Boolean
) : Client {
    private val host: String
    private val keyTimestampMap: HashMap<String, Long?>
    private val workerThread: WorkerThread

    /**
     * Default constructor.
     * @param port Port to start the client
     * @param serverHost Server host address to connect to.
     * @param serverPorts Server port to connect to.
     * @param debug Debug flag to enable debug messages.
     * @throws UnknownHostException in case the hostname could not be resolved into an address.
     */
    init {
        host = InetAddress.getLocalHost().canonicalHostName
        keyTimestampMap = LinkedHashMap()
        workerThread = WorkerThread(this)
        log.isDebug = debug
    }

    override fun start() {
        workerThread.start()
    }

    override fun stop() {
        printLn("Encerrando...")
    }

    override fun get(key: String) {
        try {
            val serverPort = serverPort
            val timestamp: Long?
            timestamp = keyTimestampMap.getOrDefault(key, null)
            val request = GetRequest(host, port, key, timestamp!!)
            val response = doRequest(
                serverHost,
                serverPort,
                request,
                GetResponse::class.java
            )
            keyTimestampMap[key] = response.timestamp
            when (response.result) {
                Result.OK, Result.TRY_OTHER_SERVER_OR_LATER -> printfLn(
                    "GET_%s key: %s value: %s realizada no servidor %s:%d, meu timestamp %d e do servidor %d",
                    response.result,
                    key,
                    response.value,
                    serverHost,
                    serverPort,
                    request.timestamp,
                    response.timestamp
                )

                Result.ERROR, Result.EXCEPTION -> printfLn(
                    "Falha ao obter o valor da key %s: %s",
                    key,
                    response.message
                )

                else -> printfLn("Falha ao obter o valor da key %s: %s", key, response.message)
            }
        } catch (e: Exception) {
            log.e("Failed to process GET request", e)
        }
    }

    override fun put(key: String, value: String?) {
        try {
            val serverPort = serverPort
            check(!isNullOrEmpty(key), "A chave não pode ser nula ou vazia")
            check(!isNullOrEmpty(value), "O valor não pode ser nulo ou vazio")
            val request = PutRequest(host, port, key, value!!)
            val response = doRequest(serverHost, serverPort, request, PutResponse::class.java)
            if (response.result !== Result.OK) {
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
            log.e(String.format("Failed connect to socket on %s:%d", host, port), e)
        } catch (e: IOException) {
            log.e("Failed to run PUT operation", e)
        } catch (e: Exception) {
            handleException(TAG, "Failed to complete PUT operation!", e)
        }
    }

    @get:Throws(IllegalArgumentException::class)
    private val serverPort: Int
        /**
         * Get a random port among the input server ports read when the client was initialized.
         * @return an integer value representing a server port.
         * @throws IllegalArgumentException in case no server ports were provided when the client was initialized.
         */
        private get() = if (serverPorts.size == 1) serverPorts[0] else if (serverPorts.size > 1) serverPorts[Random().nextInt(
            serverPorts.size - 1
        )] else throw IllegalArgumentException("No server ports were found!")

    companion object {
        private const val TAG = "ClientImpl"
        private val log: Log = ConsoleLog(TAG)
    }
}
