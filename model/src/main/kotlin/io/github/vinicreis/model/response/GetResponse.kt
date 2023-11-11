package io.github.vinicreis.model.response

import com.google.gson.annotations.SerializedName
import io.github.vinicreis.model.enums.OperationResult

class GetResponse(
    override val result: OperationResult,
    override val message: String? = null,
    @SerializedName("key") val key: String? = null,
    @SerializedName("value") val value: String? = null,
    @SerializedName("timestamp") val timestamp: Long? = null,
) : Response
