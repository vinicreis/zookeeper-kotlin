package io.github.vinicreis.model.request;

import io.github.vinicreis.model.enums.Operation;

/**
 * Represents a EXIT request made when a {@code Node} leaves the connection with {@code Controller}
 */
public class ExitRequest extends Request {

    public ExitRequest(String host, int port) {
        super(host, port);
    }

    @Override
    public Operation getOperation() {
        return Operation.EXIT;
    }

    @Override
    public String getHost() {
        return this.host;
    }

    @Override
    public int getPort() {
        return this.port;
    }
}
