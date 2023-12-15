package org.eclipse.uprotocol.transport;

import org.eclipse.uprotocol.v1.*;

/**
 * For any implementation that defines some kind of callback or function that will be called to handle incoming messages.
 */
public interface UListener {

    /**
     * Method called to handle/process events.
     * @param topic Topic the underlying source of the message.
     * @param payload Payload of the message.
     * @param attributes Transportation attributes
     * @return Returns an Ack every time a message is received and processed.
     */
    void onReceive(UUri topic, UPayload payload, UAttributes attributes);

    
    /**
     * Method called to handle/process events.
     * @param message Message received.
     * @return Returns an Ack every time a message is received and processed.
     */
    default void onReceive(UMessage message) {
        onReceive(message.getSource(), message.getPayload(), message.getAttributes());
    }

}
