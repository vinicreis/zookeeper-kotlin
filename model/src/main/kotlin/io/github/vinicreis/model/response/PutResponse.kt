package io.github.vinicreis.model.response

import com.google.gson.annotations.SerializedName
import io.github.vinicreis.model.enums.OperationResult

class PutResponse(
    result: OperationResult,
    message: String? = null,
    @SerializedName("timestamp") val timestamp: Long? = null
) : Response(result, message)
