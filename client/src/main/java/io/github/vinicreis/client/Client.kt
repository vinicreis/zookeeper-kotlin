package io.github.vinicreis.client

import io.github.vinicreis.model.util.IOUtil.readWithDefault
import io.github.vinicreis.model.util.handleException
import java.util.*
import java.util.stream.Collectors

interface Client {
    fun start()
    fun stop()
    fun put(key: String, value: String)
    fun get(key: String)

    companion object {
        private const val TAG = "ClientMain"

        @JvmStatic
        fun main(args: Array<String>) {
            try {
                val debug = args.any { it in listOf("--debug", "-d") }
                val port = readWithDefault("Digite a sua porta", "10090").toInt()
                val serverHost = readWithDefault("Digite o host do servidor", "localhost")
                val serverPortsList = readWithDefault("Dígite as portas do servidor", "10097,10098,10099")
                val serverPorts = serverPortsList.replace(" ", "").split(",").map(String::toInt)
                val client: Client = ClientImpl(port, serverHost, serverPorts, debug)
                client.start()
            } catch (e: Throwable) {
                handleException(TAG, "Failed to start client!", e)
            }
        }
    }
}
