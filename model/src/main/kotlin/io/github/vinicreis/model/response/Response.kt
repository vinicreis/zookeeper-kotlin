package io.github.vinicreis.model.response

import com.google.gson.annotations.SerializedName
import io.github.vinicreis.model.enums.OperationResult

interface Response {
    @get:SerializedName("result")
    val result: OperationResult
    @get:SerializedName("message")
    val message: String?
}
