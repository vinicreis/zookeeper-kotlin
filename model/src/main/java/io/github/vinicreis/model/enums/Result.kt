package io.github.vinicreis.model.enums

sealed interface Result<T> {
    data class OkResult<T>(val data: T) : Result<T>
    data class ErrorResult<T>(val message: String) : Result<T>
    data class TryOtherServer<T>(val message: String, val timestamp: Long) : Result<T>
    data class NotFound<T>(val message: String) : Result<T>
    data class ExceptionResult<T>(val e: Exception) : Result<T>
}
