package io.github.vinicreis.model;

import io.github.vinicreis.model.enums.Result;
import io.github.vinicreis.model.exception.OutdatedEntryException;
import io.github.vinicreis.model.repository.KeyValueRepository;
import io.github.vinicreis.model.request.GetRequest;
import io.github.vinicreis.model.response.GetResponse;
import io.github.vinicreis.model.response.PutResponse;
import io.github.vinicreis.model.response.ReplicationResponse;
import io.github.vinicreis.model.util.AssertionUtils;
import io.github.vinicreis.model.util.IOUtil;
import io.github.vinicreis.model.request.PutRequest;
import io.github.vinicreis.model.request.ReplicationRequest;

/**
 * Server generic interface used to represent a server instance.
 */
public interface Server {
    /**
     * Gets the server port.
     * @return a {@code int} value representing the server port
     */
    int getPort();

    /**
     * Gets the instance of thw key/value pair used by the server to save the values.
     * @return an {@code KeyValueRepository} instance
     */
    KeyValueRepository getKeyValueRepository();

    /**
     * Starts the server execution to listen to requests.
     */
    void start();

    /**
     * Stops the server instance.
     */
    void stop();

    /**
     * Handles the PUT request.
     * @param request a {@code PutRequest} instance received
     * @return a {@code PutResponse} instance
     */
    PutResponse put(PutRequest request);

    /**
     * Handles the REPLICATE request.
     * @param request a {@code ReplicationRequest} instance received
     * @return a {@code ReplicationResponse} instance
     */
    ReplicationResponse replicate(ReplicationRequest request);

    /**
     * Default implementation that handles a GET request, since both {@code Controller} and
     * {@code Node} instances handles the GET request the same way.
     * @param request a {@code GetRequest} instance
     * @return a {@code GetResponse} instance
     */
    default GetResponse get(GetRequest request) {
        GetResponse response;

        try {
            IOUtil.printf(
                    "Cliente %s:%d GET key: %s ts: %d. ",
                    request.getHost(),
                    request.getPort(),
                    request.getKey(),
                    request.getTimestamp()
            );

            final KeyValueRepository.Entry entry = getKeyValueRepository().find(request.getKey(), request.getTimestamp());

            if (entry == null) {
                response = new GetResponse.Builder()
                        .result(Result.NOT_FOUND)
                        .message(String.format("Valor com a chave %s não encontrado", request.getKey()))
                        .build();
            } else {
                IOUtil.printfLn(
                        "Cliente %s:%d GET key: %s ts: %d. Meu ts é %d, portanto devolvendo %s",
                        request.getHost(),
                        request.getPort(),
                        request.getKey(),
                        request.getTimestamp(),
                        entry.getTimestamp(),
                        entry.getValue()
                );

                response = new GetResponse.Builder()
                        .timestamp(entry.getTimestamp())
                        .value(entry.getValue())
                        .result(Result.OK)
                        .build();
            }
        } catch (OutdatedEntryException e) {
            response = new GetResponse.Builder()
                    .timestamp(e.getCurrentTimestamp())
                    .result(Result.TRY_OTHER_SERVER_OR_LATER)
                    .message("Please, try again later or try other server")
                    .build();
        } catch (Exception e) {
            AssertionUtils.handleException("Server", "Failed to process GET operation", e);

            response = new GetResponse.Builder().exception(e).build();
        }

        IOUtil.printf("Meu ts é %d, portanto devolvendo ", response.getTimestamp());

        if (response.getResult() == Result.OK)
            IOUtil.printLn(response.getValue());
        else
            IOUtil.printLn(response.getResult().toString());

        return response;
    }
}
