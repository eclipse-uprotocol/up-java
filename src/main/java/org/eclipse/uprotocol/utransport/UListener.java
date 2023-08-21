package org.eclipse.uprotocol.utransport;


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
    Status onReceive(UTopic topic, UPayload payload, UAttributes attributes);

}
