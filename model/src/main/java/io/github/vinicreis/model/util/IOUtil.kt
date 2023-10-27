package io.github.vinicreis.model.util

import java.io.IOException
import java.util.*

object IOUtil {
    @JvmStatic
    @Throws(IOException::class)
    fun read(message: String): String {
        print("$message: ")
        return readln()
    }

    @JvmStatic
    @Throws(IOException::class)
    fun readInt(message: String): Int {
        print("$message: ")

        return Scanner(System.`in`).nextInt()
    }

    @JvmStatic
    @Throws(IOException::class)
    fun readWithDefault(message: String, defaultValue: String): String {
        print("$message [$defaultValue] : ")
        val read = readlnOrNull()

        return if (read.isNullOrEmpty() || read == "\n") defaultValue else read
    }

    @JvmStatic
    @Throws(IOException::class)
    fun readIntWithDefault(message: String, defaultValue: Int): Int {
        print("$message [$defaultValue] : ")

        return try { Scanner(System.`in`).nextInt() } catch (e: Exception) { defaultValue }
    }

    /**
     * Blocks the execution until the user press any key while displaying a message
     * to the user.
     * @throws IOException if an I/O error occurs.
     */
    @JvmStatic
    @Throws(IOException::class)
    fun pressAnyKeyToFinish() {
        println("Pressione qualquer tecla para finalizar...")
        System.`in`.read()
    }
}
