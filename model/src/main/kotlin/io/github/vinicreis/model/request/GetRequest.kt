package io.github.vinicreis.model.request

import com.google.gson.annotations.SerializedName
import io.github.vinicreis.model.enums.Operation

class GetRequest(
    host: String,
    port: Int,
    @SerializedName("key") val key: String,
    @SerializedName("timestamp") val timestamp: Long
) : Request(host, port) {
    override val operation: Operation = Operation.GET
}
