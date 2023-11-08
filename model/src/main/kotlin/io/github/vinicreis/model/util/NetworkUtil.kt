package io.github.vinicreis.model.util

import io.github.vinicreis.model.log.ConsoleLog
import io.github.vinicreis.model.log.Log
import io.github.vinicreis.model.request.Request
import io.github.vinicreis.model.response.Response
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.Socket

object NetworkUtil {
    const val TAG = "NetworkUtil"
    val log: Log = ConsoleLog(TAG)

    @JvmStatic
    fun <Req : Request, Res : Response> doRequest(
        host: String?,
        port: Int,
        request: Req,
        responseClass: Class<Res>?
    ): Res {
        return doRequest(host, port, request, responseClass, false)
    }

    @JvmStatic
    fun <Req : Request?, Res : Response?> doRequest(
        host: String?,
        port: Int,
        request: Req,
        responseClass: Class<Res>?,
        debug: Boolean
    ): Res {
        try {
            Socket(host, port).use { socket ->
                val `in` = DataInputStream(socket.getInputStream())
                val out = DataOutputStream(socket.getOutputStream())
                val json = Serializer.toJson(request)
                log.isDebug = debug
                log.d("Sending operation ${request!!.operation}")
                out.writeUTF(request.operation.name)
                out.flush()
                log.d("Sending JSON: $json")
                out.writeUTF(json)
                out.flush()
                return Serializer.fromJson(`in`.readUTF(), responseClass)
            }
        } catch (e: IOException) {
            throw IOException("Failed to make request", e)
        }
    }
}
