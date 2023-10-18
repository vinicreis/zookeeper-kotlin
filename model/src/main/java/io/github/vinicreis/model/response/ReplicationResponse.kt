package io.github.vinicreis.model.response;

import io.github.vinicreis.model.enums.Operation;
import io.github.vinicreis.model.enums.Result;

/**
 * Represents a REPLICATE request made when a {@code Client} instance wants to insert
 * a value in a determined key.
 */
public class ReplicationResponse extends Response {

    private ReplicationResponse(Result result, String message) {
        super(result, message);
    }

    /**
     * REPLICATE response builder class
     */
    public static class Builder extends AbstractBuilder<ReplicationResponse> {

        @Override
        public ReplicationResponse build() {
            return new ReplicationResponse(result, message);
        }
    }

    @Override
    public Operation getOperation() {
        return Operation.REPLICATE;
    }
}
