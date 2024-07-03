package org.eclipse.uprotocol.communication;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.eclipse.uprotocol.transport.UTransport;
import org.eclipse.uprotocol.v1.UUri;
import org.junit.jupiter.api.Test;

public class PublisherExampleTest {
    @Test
    public void testSendPublish() {
        // Topic to publish
        final UUri topic = UUri.newBuilder()
            .setUeId(4).setUeVersionMajor(1).setResourceId(0x8000)
            .build();
        
        // Fake transport to use
        UTransport transport = new TestUTransport();

        Publisher publisher = UClient.create(transport);
        // Send the publish message
        assertFalse(publisher.publish(topic).toCompletableFuture().isCompletedExceptionally());
    }
}
