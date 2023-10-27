package io.github.vinicreis.client.thread

import io.github.vinicreis.client.Client
import io.github.vinicreis.model.enums.Operation
import io.github.vinicreis.model.log.ConsoleLog
import io.github.vinicreis.model.log.Log
import io.github.vinicreis.model.util.IOUtil.read
import io.github.vinicreis.model.util.handleException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Worker(private val client: Client) {
    private var running = true

    suspend fun run() = withContext(Dispatchers.IO) {
        log.d("Starting worker thread...")
        while (running) {
            try {
                // FIXME: Value is not being reads
                val operation = Operation.readToClient()

                when (operation) {
                    Operation.GET -> client.get(read("Digite a chave a ser lida"))
                    Operation.PUT -> client.put(
                        read("Digite a chave utilizada"),
                        read("Digite o valor a ser armazenado")
                    )

                    else -> throw IllegalArgumentException("Client should not call any option other than GET or PUT")
                }
            } catch (e: InterruptedException) {
                log.e("Fail", e)
                running = false
                client.stop()
            } catch (e: NumberFormatException) {
                log.e("Fail", e)
                running = false
                client.stop()
            } catch (e: Throwable) {
                handleException(TAG, "Failed during thread execution!", e)
            }
        }
    }

    companion object {
        private const val TAG = "DispatcherThread"
        private val log: Log = ConsoleLog(TAG)
    }
}
