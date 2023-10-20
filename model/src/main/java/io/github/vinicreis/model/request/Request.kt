package io.github.vinicreis.model.request

import com.google.gson.annotations.SerializedName
import io.github.vinicreis.model.enums.Operation

sealed class Request(
    @SerializedName("host") val host: String,
    @SerializedName("port") val port: Int
) {
    abstract val operation: Operation
}
