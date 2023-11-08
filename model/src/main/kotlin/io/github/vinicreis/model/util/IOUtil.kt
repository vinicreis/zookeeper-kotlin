package io.github.vinicreis.model.util

object IOUtil {
    @JvmStatic
    fun read(message: String): String {
        print("$message: ")
        return readln()
    }

    @JvmStatic
    fun readWithDefault(message: String, defaultValue: String): String {
        print("$message [$defaultValue] : ")
        val read = readlnOrNull()

        return if (read.isNullOrEmpty() || read == "\n") defaultValue else read
    }

    @JvmStatic
    fun readIntWithDefault(message: String, defaultValue: Int): Int {
        print("$message [$defaultValue] : ")

        return readln().toIntOrNull() ?: defaultValue
    }

    @JvmStatic
    fun pressAnyKeyToFinish() {
        println("Pressione qualquer tecla para finalizar...")
        System.`in`.read()
    }
}
