package io.github.vinicreis.model.response

import com.google.gson.annotations.SerializedName

data class GetResponse(
    @SerializedName("key") val key: String,
    @SerializedName("value") val value: String,
    @SerializedName("timestamp") val timestamp: Long,
)