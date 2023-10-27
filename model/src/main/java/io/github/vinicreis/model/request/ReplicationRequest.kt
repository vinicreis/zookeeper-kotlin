package io.github.vinicreis.model.request

import com.google.gson.annotations.SerializedName
import io.github.vinicreis.model.enums.Operation

class ReplicationRequest(
    host: String,
    port: Int,
    @SerializedName("key") val key: String,
    @SerializedName("value") val value: String,
    @SerializedName("timestamp") val timestamp: Long,
) : Request(host, port) {
    override val operation: Operation = Operation.REPLICATE
}
