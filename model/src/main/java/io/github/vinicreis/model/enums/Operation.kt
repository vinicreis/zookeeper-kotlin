package io.github.vinicreis.model.enums

import io.github.vinicreis.model.util.IOUtil
import io.github.vinicreis.model.util.IOUtil.read
import io.github.vinicreis.model.util.IOUtil.readInt
import java.io.IOException
import java.util.*

enum class Operation(val code: Int) {
    JOIN(0),
    GET(1),
    PUT(2),
    REPLICATE(3),
    EXIT(4);

    override fun toString() = "$name [${code}]"

    companion object {
        @Throws(InterruptedException::class)
        fun fromClient(code: Int): Operation {
            return when (code) {
                1 -> GET
                2 -> PUT
                else -> throw InterruptedException("Interrupt command from input!")
            }
        }

        @Throws(IOException::class, InterruptedException::class)
        fun readToClient(): Operation {
            return fromClient(
                readInt("Digite a operação desejada ou outra tecla para encerrar...\n${printToClient()}")
            )
        }

        private fun printToClient(): String = arrayOf(GET, PUT).joinToString(" | ")
    }
}
