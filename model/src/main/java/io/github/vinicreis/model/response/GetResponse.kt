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
}