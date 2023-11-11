package io.github.vinicreis.model.response

import io.github.vinicreis.model.enums.OperationResult

class JoinResponse(
    override val result: OperationResult,
    override val message: String? = null
) : Response
