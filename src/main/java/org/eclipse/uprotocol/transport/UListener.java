package org.eclipse.uprotocol.transport;

import org.eclipse.uprotocol.v1.UStatus;
import org.eclipse.uprotocol.v1.UAttributes;
import org.eclipse.uprotocol.v1.UUri;
import org.eclipse.uprotocol.v1.UPayload;

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
    UStatus onReceive(UUri topic, UPayload payload, UAttributes attributes);

}
