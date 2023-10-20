package io.github.vinicreis.model.enums

sealed interface Result {
    data object OkResult : Result
    data object ErrorResult : Result
    data object ConnectionError : Result
    data object NotFound : Result
    data class Exception(val e: kotlin.Exception) : Result
}
