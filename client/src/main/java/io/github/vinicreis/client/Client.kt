package io.github.vinicreis.client

import io.github.vinicreis.model.util.IOUtil.readWithDefault
import java.util.*
import java.util.stream.Collectors

/**
 * Client interface that will read and fetch data from servers.
 */
interface Client {
    /**
     * Starts the client execution.
     */
    fun start()

    /**
     * Stops the client execution.
     */
    fun stop()

    /**
     * Sends a request to input a value with the key.
     * @param key key to input the value in
     * @param value value to be inserted
     */
    fun put(key: String, value: String?)

    /**
     * Send a request to server to read a value by key.
     * @param key key to try to read a value from.
     */
    operator fun get(key: String)

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            try {
                val debug = Arrays.stream(args).anyMatch { arg: String -> arg == "--debug" || arg == "-d" }
                val port = readWithDefault("Digite a sua porta", "10090").toInt()
                val serverHost = readWithDefault("Digite o host do servidor", "localhost")
                val serverPortsList = readWithDefault("Dígite as portas do servidor", "10097,10098,10099")
                val serverPorts = Arrays.stream(
                    serverPortsList.replace(" ", "").split(",".toRegex())
                        .dropLastWhile { it.isEmpty() }.toTypedArray()
                ).map { s: String -> s.toInt() }.collect(Collectors.toList())
                val client: Client = ClientImpl(port, serverHost, serverPorts, debug)
                client.start()
            } catch (e: Exception) {
                handleException("ClientMain", "Failed to start client!", e)
            }
        }
    }
}
