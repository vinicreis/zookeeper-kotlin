package io.github.vinicreis.model.response

import com.google.gson.annotations.SerializedName
import io.github.vinicreis.model.enums.Operation
import io.github.vinicreis.model.enums.Result

class PutResponse(
    result: Result,
    message: String?,
    @SerializedName("timestamp") val timestamp: Long?
) : Response(result, message) {
    override val operation: Operation = Operation.PUT
}
