package io.github.vinicreis.model

import io.github.vinicreis.model.enums.Result
import io.github.vinicreis.model.exception.OutdatedEntryException
import io.github.vinicreis.model.repository.KeyValueRepository
import io.github.vinicreis.model.request.GetRequest
import io.github.vinicreis.model.request.PutRequest
import io.github.vinicreis.model.request.ReplicationRequest
import io.github.vinicreis.model.response.GetResponse
import io.github.vinicreis.model.response.PutResponse
import io.github.vinicreis.model.response.ReplicationResponse
import io.github.vinicreis.model.util.Utils

interface Server {
    val port: Int
    val keyValueRepository: KeyValueRepository

    fun start()
    fun stop()
    fun put(request: PutRequest): PutResponse
    fun replicate(request: ReplicationRequest): ReplicationResponse

    fun get(request: GetRequest): GetResponse {
        return try {
            val entry = keyValueRepository.find(request.key, request.timestamp)

            entry?.let {
                GetResponse(
                    key = request.key,
                    value = it.value,
                    timestamp = it.timestamp
                )
            } ?: Result.NotFound(
                message = "Value with key ${request.key} was not found"
            )
        } catch (e: OutdatedEntryException) {
            Result.TryOtherServer(
                message = "Please, try again later or try other server",
                timestamp = e.currentTimestamp
            )
        } catch (e: Exception) {
            Utils.handleException("Server", "Failed to process GET operation", e)

            Result.ExceptionResult(
                e = e
            )
        }
    }
}
