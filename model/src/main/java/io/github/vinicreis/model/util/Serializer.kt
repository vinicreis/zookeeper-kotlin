package io.github.vinicreis.model.util

import com.google.gson.Gson

object Serializer {
    private val gson = Gson()

    @JvmStatic
    fun toJson(obj: Any?): String {
        return gson.toJson(obj)
    }

    @JvmStatic
    fun <T> fromJson(json: String?, clazz: Class<T>?): T {
        return gson.fromJson(json, clazz)
    }
}
