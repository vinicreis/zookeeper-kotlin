package io.github.vinicreis.model.enums

import io.github.vinicreis.model.util.IOUtil
import java.io.IOException
import java.util.*

/**
 * Enum class that represents an operation from any instance, server or client.
 */
enum class Operation(
    /**
     * Get the operation code which the user selects on console.
     * @return code to be selected.
     */
    val code: Int
) {
    /**
     * Join a node to the controller.
     */
    JOIN(0),

    /**
     * Get a value from server using a key.
     */
    GET(1),

    /**
     * Put a value into some key.
     */
    PUT(2),

    /**
     * Replicate the key/value pair among a controller's nodes.
     */
    REPLICATE(3),

    /**
     * Exit request to signal node is leaving controller.
     */
    EXIT(4);

    companion object {
        /**
         * Parses an operation bases on it's input code to read it from user on console.
         * @param code user input integer code.
         * @return an operation instance to indicate which operation the user is referring to.
         * @throws InterruptedException in case the user wants to shut down the server.
         */
        @Throws(InterruptedException::class)
        fun fromClient(code: Int): Operation {
            return when (code) {
                1 -> GET
                2 -> PUT
                else -> throw InterruptedException("Interrupt command from input!")
            }
        }

        /**
         * Parses any valid input from our users.
         * @return an instance operation
         * @throws IOException if an I/O error occurs
         * @throws InterruptedException in case the user choose to interrupt the execution
         */
        @Throws(IOException::class, InterruptedException::class)
        fun readToClient(): Operation {
            return fromClient(
                IOUtil.read(
                    "Digite a operação desejada ou outra tecla para encerrar...\n%s: ",
                    printToClient()
                ).toInt()
            )
        }

        /**
         * Build a string with all options to be selected by any client instance.
         * @return the string to be shown at the console containing all client options available.
         */
        private fun printToClient(): String = arrayOf(GET, PUT).joinToString(" | ")
    }
}
