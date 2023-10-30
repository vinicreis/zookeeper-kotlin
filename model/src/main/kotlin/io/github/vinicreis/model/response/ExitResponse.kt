package io.github.vinicreis.model.response

import io.github.vinicreis.model.enums.OperationResult

class ExitResponse(
    result: OperationResult,
    message: String? = null
) : Response(result, message)
