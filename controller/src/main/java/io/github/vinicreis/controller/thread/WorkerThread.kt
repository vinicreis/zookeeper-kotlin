package io.github.vinicreis.controller.thread;

import io.github.vinicreis.controller.Controller;
import io.github.vinicreis.model.Server;
import io.github.vinicreis.model.enums.Operation;
import io.github.vinicreis.model.request.*;
import io.github.vinicreis.model.response.Response;
import io.github.vinicreis.model.util.Serializer;

import java.io.DataOutputStream;
import java.net.Socket;

import static io.github.vinicreis.model.util.AssertionUtils.handleException;

/**
 * Worker thread to execute any dispatched operation by {@code DispatcherThread}.
 * @see DispatcherThread
 */
public class WorkerThread extends Thread {
    private static final String TAG = "WorkerThread";
    private final Server server;
    private final Socket socket;
    private final Operation operation;
    private final String request;

    public WorkerThread(Server server, Socket socket, Operation operation, String request) {
        this.server = server;
        this.socket = socket;
        this.operation = operation;
        this.request = request;
    }

    @Override
    public void run() {
        try {
            final Response response;
            final DataOutputStream writer = new DataOutputStream(socket.getOutputStream());

            switch (operation) {
                case JOIN:
                    if (server instanceof Controller) {
                        response = ((Controller)server).join(Serializer.fromJson(request, JoinRequest.class));
                        break;
                    }

                    throw new IllegalStateException("Nodes can not handle JOIN requests");
                case PUT:
                    response = server.put(Serializer.fromJson(request, PutRequest.class));
                    break;
                case REPLICATE:
                    response = server.replicate(Serializer.fromJson(request, ReplicationRequest.class));
                    break;
                case GET:
                    response = server.get(Serializer.fromJson(request, GetRequest.class));
                    break;
                case EXIT:
                    if (server instanceof Controller) {
                        response = ((Controller)server).exit(Serializer.fromJson(request, ExitRequest.class));
                        break;
                    }

                    throw new IllegalStateException("Nodes can not handle EXIT requests");
                default:
                    throw new IllegalStateException("Operation unknown!");
            }

            writer.writeUTF(Serializer.toJson(response));
            writer.flush();

            socket.close();
        } catch (Exception e) {
            handleException(TAG, "Failed during worker execution", e);
        }
    }
}
