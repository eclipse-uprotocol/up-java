package org.eclipse.uprotocol.communication;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.uprotocol.transport.UTransport;
import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UStatus;
import org.eclipse.uprotocol.v1.UUri;
import org.junit.jupiter.api.Test;

public class PublisherExample {
    @Test
    public void testSendPublish() {
        // Topic to publish
        final UUri topic = UUri.newBuilder()
            .setUeId(4).setUeVersionMajor(1).setResourceId(0x8000)
            .build();
        
        // Fake transport to use
        UTransport transport = new TestUTransport();

        Publisher publisher = UPClient.create(transport);
        // Send the publish message
        UStatus status = publisher.publish(topic, null);
        assertEquals(status.getCode(), UCode.OK);
    }
}
