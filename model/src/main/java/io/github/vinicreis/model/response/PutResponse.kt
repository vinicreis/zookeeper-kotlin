package io.github.vinicreis.model.response

import com.google.gson.annotations.SerializedName

data class PutResponse(
    @SerializedName("timestamp") val timestamp: Long?
)
