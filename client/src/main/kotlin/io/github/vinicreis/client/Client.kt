package io.github.vinicreis.client

import io.github.vinicreis.model.util.IOUtil.readIntWithDefault
import io.github.vinicreis.model.util.IOUtil.readWithDefault

interface Client {
    fun start()
    fun stop()
    fun put(key: String, value: String)
    fun get(key: String)

    companion object {
        private const val DEFAULT_PORT = 10090
        private const val DEFAULT_SERVER_PORTS = "10097,10098,10099"
        val DefaultServerPorts = listOf(10097, 10098, 10099)

        @JvmStatic
        fun main(args: Array<String>) {
            val debug = args.any { it in listOf("--debug", "-d") }
            val port = readIntWithDefault("Digite a sua porta", DEFAULT_PORT)
            val serverHost = readWithDefault("Digite o host do servidor", "archlaptop")
            val serverPortsList = readWithDefault("Dígite as portas do servidor", DEFAULT_SERVER_PORTS)
            val serverPorts = serverPortsList.replace(" ", "").split(",").map(String::toInt)
            val client: Client = ClientImpl(port, serverHost, serverPorts, debug)
            client.start()
        }
    }
}
