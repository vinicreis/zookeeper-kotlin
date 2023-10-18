package io.github.vinicreis.model.enums;

/**
 * Enum class that maps all possible results to be returned to clients.
 */
public enum Result {
    /**
     * Indicates that the request finished with no errors.
     */
    OK,
    /**
     * Indicates that some business or validation error occurred on the target.
     */
    ERROR,
    /**
     * Indicates that the client should make the request to other server to fetch an updated value from a key.
     */
    TRY_OTHER_SERVER_OR_LATER,
    /**
     * Indicates that the request key was not found on the server.
     */
    NOT_FOUND,
    /**
     * Indicates that an unexpected exception/error happened during the execution.
     */
    EXCEPTION
}
