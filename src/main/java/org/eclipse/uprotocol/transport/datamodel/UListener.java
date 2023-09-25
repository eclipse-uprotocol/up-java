package org.eclipse.uprotocol.transport.datamodel;

import java.util.Objects;

import org.eclipse.uprotocol.uri.datamodel.UUri;

/**
 * Listener to receive sent events from the uTransport. The Listeners are registered and 
 * unregistered through the uTransport API.
 */
public interface UListener {

    /**
     * Method called to handle/process events.
     * @param topic Topic the underlying source of the message.
     * @param payload Payload of the message.
     * @param attributes Transportation attributes
     * @return Returns an Ack every time a message is received and processed.
     */
    UStatus onReceive(UUri topic, UPayload payload, UAttributes attributes);

    /**
     * Method called to handle/process events.
     * @param message Message received.
     * @return Returns an Ack every time a message is received and processed.
     */
    default UStatus OnReceive(UMessage message) {
        Objects.requireNonNull(message);
        return onReceive(message.topic(), message.payload(), message.attributes());
    }

}
