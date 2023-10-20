package io.github.vinicreis.model.request

import io.github.vinicreis.model.enums.Operation

class PutRequest(
    host: String,
    port: Int,
    val key: String,
    val value: String
) : Request(host, port) {
    override val operation: Operation = Operation.EXIT
}
