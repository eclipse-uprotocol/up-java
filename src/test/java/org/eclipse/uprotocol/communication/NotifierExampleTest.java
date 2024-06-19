package org.eclipse.uprotocol.communication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;

import org.eclipse.uprotocol.transport.UListener;
import org.eclipse.uprotocol.transport.UTransport;
import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UMessage;
import org.eclipse.uprotocol.v1.UUri;

public class NotifierExampleTest {
    
    // Notification topic
    private UUri topic = UUri.newBuilder()
        .setUeId(4).setUeVersionMajor(1).setResourceId(0x8000).build();

    @Test
    public void testSendNotification() throws InterruptedException, ExecutionException {
        UTransport transport = new TestUTransport();
        
        
        // Who to send the notification to
        final UUri destination = UUri.newBuilder()
            .setUeId(3).setUeVersionMajor(1).build();

        Notifier notifier = UClient.create(transport);
        // Send the notification (without payload)
        assertEquals(notifier.notify(topic, destination, null)
            .toCompletableFuture().get().getCode(), UCode.OK);
    }

    @Test
    public void testReceivingNotifications() throws InterruptedException, ExecutionException {
        // Test transport
        final TestUTransport transport = new TestUTransport();
        final UListener listener = new UListener() {
            @Override
            public void onReceive(UMessage message) {
                // Handle receiving notifications here
                assertNotNull(message);
            }
        };
        Notifier notifier = UClient.create(transport);

        // Register listener to receive notifications
        assertEquals(notifier.registerNotificationListener(topic, listener)
            .toCompletableFuture().get().getCode(), UCode.OK);

        // Unregister the listener so we no longer receive notifications
        assertEquals(notifier.unregisterNotificationListener(topic, listener)
            .toCompletableFuture().get().getCode(), UCode.OK);
    }
}
