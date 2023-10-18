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
import io.github.vinicreis.model.util.AssertionUtils
import io.github.vinicreis.model.util.IOUtil

interface Server {
    val port: Int
    val keyValueRepository: KeyValueRepository

    fun start()
    fun stop()
    fun put(request: PutRequest): PutResponse
    fun replicate(request: ReplicationRequest): ReplicationResponse

    fun get(request: GetRequest): GetResponse {
        val response: GetResponse = try {
            IOUtil.printf(
                "Cliente %s:%d GET key: %s ts: %d. ",
                request.host,
                request.port,
                request.key,
                request.timestamp
            )
            val entry = keyValueRepository.find(request.key, request.timestamp)
            if (entry == null) {
                GetResponse.Builder()
                    .result(Result.NOT_FOUND)
                    .message(
                        String.format(
                            "Valor com a chave %s não encontrado",
                            request.key
                        )
                    )
                    .build()
            } else {
                IOUtil.printfLn(
                    "Cliente %s:%d GET key: %s ts: %d. Meu ts é %d, portanto devolvendo %s",
                    request.host,
                    request.port,
                    request.key,
                    request.timestamp,
                    entry.timestamp,
                    entry.value
                )
                GetResponse.Builder()
                    .timestamp(entry.timestamp)
                    .value(entry.value)
                    .result(Result.OK)
                    .build()
            }
        } catch (e: OutdatedEntryException) {
            GetResponse.Builder()
                .timestamp(e.currentTimestamp)
                .result(Result.TRY_OTHER_SERVER_OR_LATER)
                .message("Please, try again later or try other server")
                .build()
        } catch (e: Exception) {
            AssertionUtils.handleException("Server", "Failed to process GET operation", e)
            GetResponse.Builder().exception(e).build()
        }

        IOUtil.printf("Meu ts é %d, portanto devolvendo ", response.timestamp)
        IOUtil.printLn(if(response.result === Result.OK) response.value else response.result.toString())

        return response
    }
}
