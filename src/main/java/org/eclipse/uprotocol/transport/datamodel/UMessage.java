package org.eclipse.uprotocol.transport.datamodel;

import org.eclipse.uprotocol.uri.datamodel.UUri;

/**
 * UMessage is the envelop that contains a topic, payload, and attributes.
 */
public interface UMessage {

    /**
     * Returns the topic of the message.
     * @return Returns the topic of the message.
     */
    UUri topic();

    /**
     * The UPayload contains the clean Payload information along with its raw serialized structure of a byte[].
     * @return Returns the UPayload.
     */
    UPayload payload();

    /**
     * The UAttributes contains the attributes of the uMessage like ttl, priority, etc...
     * @return Returns the UAttributes.
     */
    UAttributes attributes();
}
