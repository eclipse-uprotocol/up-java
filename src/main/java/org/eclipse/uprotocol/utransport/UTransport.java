package org.eclipse.uprotocol.utransport;

import org.eclipse.uprotocol.Ack;
import org.eclipse.uprotocol.umessage.UMessage;

/**
 * This is the interface for uProtocol transports such as Binder, EventHub, HTTP or Zenoh.
 */
public interface UTransport<T> {

    /**
     * Translates a specific Transport protocol into a UMessage.
     * @param transportProtocol Specific T transport protocol
     * @return Returns a UMessage from the T transport protocol.
     */
    UMessage unwrap(T transportProtocol);

    /**
     * Translates a UMessage into a specific T Transport protocol.
     * @param uMessage The UMessage to be wrapped and sent on the T Transport.
     * @return Returns the T transport protocol.
     */
    T wrap(UMessage uMessage);

    /**
     * Runs on the client, sends the message using the underlying transport and returns, in a fire and forget fashion.
     * @param uMessage The UMessage to be sent on the underlying transport.
     * @return Returns immediately with an Ack if it could be sent.
     */
    Ack send(UMessage uMessage);

    /**
     * Support for retrieving the configured uMessage listener that will be called when a message is received by the underlying transport layer.
     * @return Returns the UTransportMessageProcessor that will be called when a message is received by the underlying transport layer.
     */
    UTransportMessageProcessor uMessageProcessor();

}
