package io.github.vinicreis.model.response

import io.github.vinicreis.model.enums.Operation
import io.github.vinicreis.model.enums.Result

/**
 * Represents a EXIT response made to when a `Node` leaves the connection with `Controller`
 */
class ExitResponse(result: Result, message: String) : Response(result, message) {
    override val operation: Operation = Operation.EXIT

    class Builder : Response.Builder<ExitResponse>() {
        override fun build(): ExitResponse {
            return ExitResponse(result, message!!)
        }
    }
}
