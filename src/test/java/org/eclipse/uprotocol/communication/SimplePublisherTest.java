   
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
import static org.mockito.Mockito.verify;

import java.util.concurrent.CompletionException;

import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UMessage;
import org.eclipse.uprotocol.v1.UUri;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// [utest->dsn~communication-layer-impl-default~1]
class SimplePublisherTest extends CommunicationLayerClientTestBase {
    private Publisher publisher;
    private CallOptions options;

    @BeforeEach
    void createPublisher() {
        publisher = new SimplePublisher(transport, uriProvider);
        options = CallOptions.DEFAULT;
    }

    private void assertEventAttributes(UMessage message) {
        assertEquals(TOPIC_URI, message.getAttributes().getSource());
        assertFalse(message.getAttributes().hasSink());
        assertEquals(options.priority(), message.getAttributes().getPriority());
        assertEquals(options.timeout(), message.getAttributes().getTtl());
        assertFalse(message.getAttributes().hasToken());
    }

    @Test
    @DisplayName("Test sending a simple publish message without a payload")
    void testSendPublish() {
        publisher.publish(TOPIC_URI.getResourceId()).toCompletableFuture().join();
        verify(transport).send(requestMessage.capture());
        assertEventAttributes(requestMessage.getValue());
    }

    @Test
    @DisplayName("Test sending a simple publish message with CallOptions and no payload")
    void testSendPublishWithOptions() {
        publisher.publish(TOPIC_URI.getResourceId(), options).toCompletableFuture().join();
        verify(transport).send(requestMessage.capture());
        assertEventAttributes(requestMessage.getValue());
    }
    
    @Test
    @DisplayName("Test sending a simple publish message with a stuffed UPayload that was build with packToAny()")
    void testSendPublishWithStuffedPayload() {
        final var payload = UPayload.pack(UUri.newBuilder().setAuthorityName("Hartley").build());
        publisher.publish(TOPIC_URI.getResourceId(), payload).toCompletableFuture().join();
        verify(transport).send(requestMessage.capture());
        assertEventAttributes(requestMessage.getValue());
        assertEquals(payload.data(), requestMessage.getValue().getPayload());
    }

    @Test
    @DisplayName("Test sending a simple publish message with CallOptions and a stuffed UPayload")
    void testSendPublishWithPayloadAndOptions() {
        final var payload = UPayload.pack(UUri.newBuilder().setAuthorityName("Hartley").build());
        publisher.publish(TOPIC_URI.getResourceId(), options, payload).toCompletableFuture().join();
        verify(transport).send(requestMessage.capture());
        assertEventAttributes(requestMessage.getValue());
        assertEquals(payload.data(), requestMessage.getValue().getPayload());
    }

    @Test
    void testSendingPublishWithInvalidTopic() {
        var exception = assertThrows(
            CompletionException.class,
            () ->
            publisher.publish(0x5000).toCompletableFuture().join()
        );
        assertEquals(UCode.INVALID_ARGUMENT, ((UStatusException) exception.getCause()).getCode());
    }
}
