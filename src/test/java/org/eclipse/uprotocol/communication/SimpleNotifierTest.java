/**
 * SPDX-FileCopyrightText: 2024 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.eclipse.uprotocol.communication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.util.concurrent.CompletionStage;

import org.eclipse.uprotocol.transport.UListener;
import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UMessage;
import org.eclipse.uprotocol.v1.UStatus;
import org.eclipse.uprotocol.v1.UUri;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SimpleNotifierTest {
    @Test
    @DisplayName("Test sending a simple notification")
    public void testSendNotification() {
        Notifier notifier = new SimpleNotifier(new TestUTransport());
        CompletionStage<UStatus> result = notifier.notify(createTopic(), createDestinationUri(), null);
        assertEquals(result.toCompletableFuture().join().getCode(), UCode.OK);
    }


    @Test
    @DisplayName("Test sending a simple notification passing a google.protobuf.Message payload")
    public void testSendNotificationWithPayload() {
        UUri uri = UUri.newBuilder().setAuthorityName("Hartley").build();
        Notifier notifier = new SimpleNotifier(new TestUTransport());
        CompletionStage<UStatus> result = notifier.notify(createTopic(), createDestinationUri(), 
            UPayload.pack(uri));
        assertEquals(result.toCompletableFuture().join().getCode(), UCode.OK);
    }


    @Test
    @DisplayName("Test registering and unregistering a listener for a notification topic")
    public void testRegisterListener() {
        UListener listener = new UListener() {
            @Override
            public void onReceive(UMessage message) {
                assertNotNull(message);
            }
        };

        Notifier notifier = new SimpleNotifier(new TestUTransport());
        CompletionStage<UStatus> result = notifier.registerNotificationListener(createTopic(), listener);
        assertEquals(result.toCompletableFuture().join().getCode(), UCode.OK);

        result = notifier.unregisterNotificationListener(createTopic(), listener);
        assertEquals(result.toCompletableFuture().join().getCode(), UCode.OK);
    }


    @Test
    @DisplayName("Test unregistering a listener that was not registered")
    public void testUnregisterListenerNotRegistered() {
        UListener listener = new UListener() {
            @Override
            public void onReceive(UMessage message) {
                assertNotNull(message);
            }
        };
        Notifier notifier = new SimpleNotifier(new TestUTransport());
        CompletionStage<UStatus> result = notifier.unregisterNotificationListener(createTopic(), listener);
        assertFalse(result.toCompletableFuture().isCompletedExceptionally());
        assertEquals(result.toCompletableFuture().join().getCode(), UCode.NOT_FOUND);
    }


    private UUri createTopic() {
        return UUri.newBuilder()
            .setAuthorityName("hartley")
            .setUeId(3)
            .setUeVersionMajor(1)
            .setResourceId(0x8000)
            .build();
    }


    private UUri createDestinationUri() {
        return UUri.newBuilder()
            .setUeId(4)
            .setUeVersionMajor(1)
            .build();
    }
}
