package io.github.vinicreis.model.request;

import com.google.gson.annotations.SerializedName;
import io.github.vinicreis.model.enums.Operation;

/**
 * Represents a GET request made when a {@code Client} instance wants to get a value by key.
 */
public class GetRequest extends Request {
    @SerializedName("key") private final String key;
    @SerializedName("timestamp") private final Long timestamp;

    public GetRequest(String host, int port, String key, Long timestamp) {
        super(host, port);

        this.key = key;
        this.timestamp = timestamp;
    }

    @Override
    public Operation getOperation() {
        return Operation.GET;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port;
    }

    /**
     * Get the key which the client wants to retrieve the value.
     * @return a {@code String} with the key
     */
    public String getKey() {
        return key;
    }

    /**
     * Get the timestamp which the client has associated with the key.
     * @return a {@code Long} value representing the timestamp
     */
    public Long getTimestamp() {
        return timestamp;
    }
}
