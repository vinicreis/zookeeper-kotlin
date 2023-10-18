package io.github.vinicreis.model.util

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

object IOUtil {
    private val reader = BufferedReader(InputStreamReader(System.`in`))

    /**
     * Prints a message and jump a new line at the end.
     * @param message message to be printed on the console.
     */
    @JvmStatic
    fun printLn(message: String?) {
        if (!message.isNullOrEmpty()) println(message)
    }

    /**
     * Formats and prints a message in the console.
     * @param message message to be printed
     * @param args format args to format received message.
     */
    fun printf(message: String?, vararg args: Any?) {
        if (!message.isNullOrEmpty()) System.out.printf(message, *args)
    }

    /**
     * Formats and prints a messa in the console with a new line at the end.
     * @param message message to be printed
     * @param args format args to format received message
     */
    @JvmStatic
    fun printfLn(message: String?, vararg args: Any?) {
        if (!message.isNullOrEmpty()) System.out.printf(message + "\n", *args)
    }

    /**
     * Reads a new line from console.
     * @return a `string` object with the read line on the console.
     * @throws IOException if an I/O error occurs while reading a line from console
     */
    @JvmStatic
    @Throws(IOException::class)
    fun read(): String {
        return reader.readLine()
    }

    /**
     * Reads a line from console with an introduction message presented first.
     * @param message message to be presented to the user.
     * @return a `string` object with the read line on the console.
     * @throws IOException if an I/O error occurs while reading a line from console
     */
    @JvmStatic
    @Throws(IOException::class)
    fun read(message: String): String {
        print("$message: ")
        return reader.readLine()
    }

    /**
     * Reads a line from console with a formatted introduction message presented first.
     * @param message message to be presented to the user.
     * @param args format args to format the `message`
     * @return a `string` object with the read line on the console.
     * @throws IOException if an I/O error occurs while reading a line from console
     */
    @JvmStatic
    @Throws(IOException::class)
    fun read(message: String?, vararg args: Any?): String {
        message?.let { System.out.printf(it, *args) }

        return reader.readLine()
    }

    /**
     * Reads a line from console with an introduction message presented first.
     * Returns the `defaultValue` if no input is read from the user.
     * @param message message to be presented to the user.
     * @return a `string` object with the read line on the console or the default value.
     * @throws IOException if an I/O error occurs while reading a line from console
     */
    @JvmStatic
    @Throws(IOException::class)
    fun readWithDefault(message: String, defaultValue: String): String {
        System.out.printf("$message [%s] : ", defaultValue)
        val read = reader.readLine()
        return if (read == null || read.isEmpty() || read == "\n") defaultValue else read
    }

    /**
     * Blocks the execution until the user press any key while displaying a message
     * to the user.
     * @throws IOException if an I/O error occurs.
     */
    @JvmStatic
    @Throws(IOException::class)
    fun pressAnyKeyToFinish() {
        printLn("Pressione qualquer tecla para finalizar...")
        System.`in`.read()
    }
}
