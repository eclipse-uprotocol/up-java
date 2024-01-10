package org.eclipse.uprotocol.rpc;

import java.util.concurrent.CompletableFuture;
import org.eclipse.uprotocol.v1.*;

/**
 * uService (servers) implement this to receive requests messages from clients. <br>
 * The service must implement the {@link #onReceive(UMessage, CompletableFuture)} method to handle
 * the request and then complete the future passed to the method that triggers the uLink library to
 * send (over the transport) the response.
 */
public interface URpcListener {
  
    /**
     * Method called to handle/process events.
     * @param message Message received.
     */
    void onReceive(UMessage message, CompletableFuture<UPayload> responseFuture);

}
