package io.github.vinicreis.model.request

import io.github.vinicreis.model.enums.Operation

class PutRequest(host: String, port: Int) : Request(host, port) {
    override val operation: Operation = Operation.EXIT
}
