package org.eclipse.uprotocol.communication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.Test;

import org.eclipse.uprotocol.transport.UListener;
import org.eclipse.uprotocol.transport.UTransport;
import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UMessage;
import org.eclipse.uprotocol.v1.UStatus;
import org.eclipse.uprotocol.v1.UUri;

public class SubscriberExample {
    @Test
    public void testSubscribe() {
        // Topic to subscribe to
        UUri topic = UUri.newBuilder()
            .setUeId(4).setUeVersionMajor(1).setResourceId(0x8000)
            .build();

        // Listener to receive published messages on
        final UListener myListener = new UListener() {
            @Override
            public void onReceive(UMessage message) {
                // Handle receiving subscriptions here
            }
        };

        // The target transport
        UTransport transport = new TestUTransport();
        
        // Subscribe
        Subscriber subscriber = UPClient.create(transport);
        assertFalse(subscriber.subscribe(topic, myListener, new CallOptions(100))
            .toCompletableFuture().isCompletedExceptionally());
        
        // Unsubscribe
        assertEquals(subscriber.unsubscribe(topic, myListener, null), 
            UStatus.newBuilder().setCode(UCode.OK).build());
    }
}
