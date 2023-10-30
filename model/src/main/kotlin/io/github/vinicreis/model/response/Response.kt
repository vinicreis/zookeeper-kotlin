package io.github.vinicreis.model.response

import com.google.gson.annotations.SerializedName
import io.github.vinicreis.model.enums.OperationResult

abstract class Response(
    @SerializedName("result") val result: OperationResult,
    @SerializedName("message") val message: String?
)