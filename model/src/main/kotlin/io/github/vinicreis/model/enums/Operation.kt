package io.github.vinicreis.model.enums

import io.github.vinicreis.model.util.IOUtil.read

enum class Operation(val code: Int) {
    JOIN(0),
    GET(1),
    PUT(2),
    REPLICATE(3),
    EXIT(4);

    override fun toString() = "$name [${code}]"

    companion object {
        private fun fromClient(code: String): Operation {
            return when (code) {
                "1" -> GET
                "2" -> PUT
                else -> throw IllegalArgumentException("Unknown operation entered!")
            }
        }

        fun readToClient(): Operation {
            return fromClient(
                read("Digite a operação desejada ou Ctrl+D para encerrar...\n${printToClient()}")
            )
        }

        private fun printToClient(): String = arrayOf(GET, PUT).joinToString(" | ")
    }
}
