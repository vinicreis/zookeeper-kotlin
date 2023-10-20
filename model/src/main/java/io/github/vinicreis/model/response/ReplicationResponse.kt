package io.github.vinicreis.model.response

import io.github.vinicreis.model.enums.Operation
import io.github.vinicreis.model.enums.Result

class ReplicationResponse(
    result: Result,
    message: String?,
) : Response(result, message) {
    override val operation: Operation = Operation.REPLICATE
}