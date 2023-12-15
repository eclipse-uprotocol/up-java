package org.eclipse.uprotocol.rpc;

import java.util.concurrent.CompletableFuture;
import org.eclipse.uprotocol.v1.*;

/**
 * uService (servers) implement this to receive requests messages from clients. <br>
 * The service must implement the {@link #onReceive(UUri, UPayload, UAttributes, CompletableFuture)} method to handle
 * the request and then complete the future passed to the method that triggers the uLink library to
 * send (over the transport) the response.
 */
public interface URpcListener {

    /**
     * The new request is received.
     *
     * server must call {@link CompletableFuture#complete(Object)} to send response upon
     * completion of the request handling, for example: {@code responseFuture.complete(response)}.
     *
     * @param method Method UUri of the request.
     * @param payload Request method payload
     * @param attributes Request method attributes
     * @param responseFuture A {@code CompletableFuture} used by a server to send a response upon completion.
     */
    void onReceive(UUri method, UPayload payload, UAttributes attributes, CompletableFuture<UPayload> responseFuture);

    
    /**
     * Method called to handle/process events.
     * @param message Message received.
     */
    default void onReceive(UMessage message, CompletableFuture<UPayload> responseFuture) {
        onReceive(message.getSource(), message.getPayload(), message.getAttributes(), responseFuture);
    }

}
