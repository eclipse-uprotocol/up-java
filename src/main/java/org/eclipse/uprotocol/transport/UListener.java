package org.eclipse.uprotocol.transport;

import org.eclipse.uprotocol.v1.*;

/**
 * For any implementation that defines some kind of callback or function that will be called to handle incoming messages.
 */
public interface UListener {

    /**
     * Method called to handle/process messages.
     * @param message Message received.
     * @return Returns an Ack every time a message is received and processed.
     */
    void onReceive(UMessage message);
}
