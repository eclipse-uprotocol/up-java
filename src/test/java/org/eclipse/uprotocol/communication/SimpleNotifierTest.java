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

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.eclipse.uprotocol.transport.UListener;
import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UMessage;
import org.eclipse.uprotocol.v1.UUri;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// [utest->dsn~communication-layer-impl-default~1]
class SimpleNotifierTest extends CommunicationLayerClientTestBase {

    private Notifier notifier;
    private CallOptions options;

    @BeforeEach
    void createNotifier() {
        notifier = new SimpleNotifier(transport, uriProvider);
        options = CallOptions.DEFAULT;
    }

    private void assertNotificationAttributes(UMessage message) {
        assertEquals(TOPIC_URI, message.getAttributes().getSource());
        assertEquals(DESTINATION_URI, message.getAttributes().getSink());
        assertEquals(options.priority(), message.getAttributes().getPriority());
        assertEquals(options.timeout(), message.getAttributes().getTtl());
        assertFalse(message.getAttributes().hasToken());
    }

    @Test
    @DisplayName("Test sending a simple notification")
    void testSendNotification() {
        notifier.notify(TOPIC_URI.getResourceId(), DESTINATION_URI).toCompletableFuture().join();
        verify(transport).send(requestMessage.capture());
        assertNotificationAttributes(requestMessage.getValue());
    }

    @Test
    @DisplayName("Test sending a simple notification passing CallOptions")
    void testSendNotificationWithOptions() {
        notifier.notify(TOPIC_URI.getResourceId(), DESTINATION_URI, options).toCompletableFuture().join();
        verify(transport).send(requestMessage.capture());
        assertNotificationAttributes(requestMessage.getValue());
    }

    @Test
    @DisplayName("Test sending a simple notification passing a google.protobuf.Message payload")
    void testSendNotificationWithPayload() {
        final var payload = UPayload.pack(UUri.newBuilder().setAuthorityName("Hartley").build());
        notifier.notify(TOPIC_URI.getResourceId(), DESTINATION_URI, payload).toCompletableFuture().join();
        verify(transport).send(requestMessage.capture());
        assertNotificationAttributes(requestMessage.getValue());
        assertEquals(payload.data(), requestMessage.getValue().getPayload());
    }

    @Test
    @DisplayName("Test sending a simple notification passing a google.protobuf.Any payload and CallOptions")
    void testSendNotificationWithAnyPayloadAndOptions() {
        final var payload = UPayload.pack(UUri.newBuilder().setAuthorityName("Hartley").build());
        notifier.notify(TOPIC_URI.getResourceId(), DESTINATION_URI, options, payload).toCompletableFuture().join();
        verify(transport).send(requestMessage.capture());
        assertNotificationAttributes(requestMessage.getValue());
        assertEquals(payload.data(), requestMessage.getValue().getPayload());
    }

    @Test
    void testSendNotificationWithInvalidTopic() {
        var exception = assertThrows(
            CompletionException.class,
            () ->
            notifier.notify(0x5000, TOPIC_URI).toCompletableFuture().join()
        );
        assertEquals(UCode.INVALID_ARGUMENT, ((UStatusException) exception.getCause()).getCode());
    }

    @Test
    @DisplayName("Test registering and unregistering a listener for a notification topic")
    void testRegisterListener() {
        final var listener = mock(UListener.class);
        notifier.registerNotificationListener(TOPIC_URI, listener).toCompletableFuture().join();
        verify(transport).registerListener(
            TOPIC_URI,
            TRANSPORT_SOURCE,
            listener);
        notifier.unregisterNotificationListener(TOPIC_URI, listener).toCompletableFuture().join();
        verify(transport).unregisterListener(
            TOPIC_URI,
            TRANSPORT_SOURCE,
            listener);
    }

    @Test
    @DisplayName("Test unregistering a listener that was not registered")
    void testUnregisterListenerNotRegistered() {
        final var listener = mock(UListener.class);
        when(transport.unregisterListener(TOPIC_URI, TRANSPORT_SOURCE, listener))
            .thenReturn(CompletableFuture.failedFuture(
                new UStatusException(UCode.NOT_FOUND, "no such listener")));
        final var exception = assertThrows(CompletionException.class, () -> {
            notifier.unregisterNotificationListener(TOPIC_URI, listener).toCompletableFuture().join();
        });
        verify(transport).unregisterListener(
            TOPIC_URI,
            TRANSPORT_SOURCE,
            listener);
        assertEquals(UCode.NOT_FOUND, ((UStatusException) exception.getCause()).getCode());
    }
}
