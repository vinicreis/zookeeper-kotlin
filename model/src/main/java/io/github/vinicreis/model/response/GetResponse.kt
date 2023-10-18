package io.github.vinicreis.model.response

import com.google.gson.annotations.SerializedName
import io.github.vinicreis.model.enums.Operation
import io.github.vinicreis.model.enums.Result

class GetResponse(
    result: Result,
    message: String?,
    @SerializedName("timestamp") val timestamp: Long?,
    @SerializedName("value") val value: String?
) : Response(result, message) {
    override val operation: Operation = Operation.GET

    class Builder : Response.Builder<GetResponse>() {
        private var value: String? = null
        private var timestamp: Long? = null

        override fun build() = GetResponse(result, message, timestamp, value)

        fun value(value: String): Builder {
            this.value = value

            return this
        }

        fun timestamp(timestamp: Long): Builder {
            this.timestamp = timestamp

            return this
        }
    }
}