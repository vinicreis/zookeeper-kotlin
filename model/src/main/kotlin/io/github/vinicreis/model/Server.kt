package io.github.vinicreis.model

import io.github.vinicreis.model.enums.OperationResult
import io.github.vinicreis.model.log.Log
import io.github.vinicreis.model.repository.KeyValueRepository
import io.github.vinicreis.model.request.GetRequest
import io.github.vinicreis.model.request.PutRequest
import io.github.vinicreis.model.request.ReplicationRequest
import io.github.vinicreis.model.response.GetResponse
import io.github.vinicreis.model.response.PutResponse
import io.github.vinicreis.model.response.ReplicationResponse

interface Server {
    val port: Int
    val keyValueRepository: KeyValueRepository
    val log: Log

    fun start()
    fun stop()
    fun put(request: PutRequest): PutResponse
    fun replicate(request: ReplicationRequest): ReplicationResponse

    fun get(request: GetRequest): GetResponse {
        return try {
            keyValueRepository.find(request.key, request.timestamp)?.let { entry ->
                GetResponse(
                    result = OperationResult.OK,
                    key = request.key,
                    value = entry.value,
                    timestamp = entry.timestamp
                )
            } ?: GetResponse(
                result = OperationResult.NOT_FOUND,
                message = "Value with key ${request.key} was not found"
            )
        } catch (e: IllegalStateException) {
            log.e("Something went wrong while processing GET request", e)

            GetResponse(
                result = OperationResult.TRY_AGAIN_ON_OTHER_SERVER,
                message = "Please, try again later or try other server",
            )
        }
    }
}
