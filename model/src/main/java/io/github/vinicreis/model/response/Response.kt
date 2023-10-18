package io.github.vinicreis.model.response

import com.google.gson.annotations.SerializedName
import io.github.vinicreis.model.enums.Operation
import io.github.vinicreis.model.enums.Result

abstract class Response(
    @SerializedName("result") val result: Result,
    @SerializedName("message") val message: String?
) {
    abstract val operation: Operation

    abstract class Builder<T> {
        protected var result: Result = Result.OK
        protected var message: String? = null

        abstract fun build(): T

        fun result(result: Result): Builder<T> {
            this.result = result

            return this
        }

        fun message(message: String): Builder<T> {
            this.message = message

            return this
        }

        fun exception(e: Exception): Builder<T> {
            result = Result.EXCEPTION
            message = "Falha ao processar operação"

            return this
        }
    }
}