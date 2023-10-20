package io.github.vinicreis.model.response

import com.google.gson.annotations.SerializedName
import io.github.vinicreis.model.enums.Operation
import io.github.vinicreis.model.enums.Result

sealed class Response(
    @SerializedName("result") val result: Result,
    @SerializedName("message") val message: String?
) {
    abstract val operation: Operation
}