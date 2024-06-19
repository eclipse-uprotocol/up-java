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

import java.util.concurrent.CompletionStage;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.uprotocol.core.usubscription.v3.SubscriptionResponse;
import org.eclipse.uprotocol.core.usubscription.v3.UnsubscribeResponse;
import org.eclipse.uprotocol.transport.UListener;
import org.eclipse.uprotocol.transport.UTransport;
import org.eclipse.uprotocol.transport.builder.UMessageBuilder;
import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UMessage;
import org.eclipse.uprotocol.v1.UStatus;
import org.eclipse.uprotocol.v1.UUri;


public class InMemorySubscriberTest {
    @Test
    @DisplayName("Test subscribe happy path")
    public void test_subscribe_happy_path() {
        UUri topic = createTopic();
        UTransport transport = new TestUTransport();
        Subscriber subscriber = new InMemorySubscriber(transport, new InMemoryRpcClient(transport));
        CompletionStage<SubscriptionResponse> response = subscriber.subscribe(topic, new UListener() {
            @Override
            public void onReceive(UMessage message) {
                // Do nothing
            }
        }, null);
        response.toCompletableFuture().join();
        assertFalse(response.toCompletableFuture().isCompletedExceptionally());
    }

    @Test
    @DisplayName("Test subscribe and then unsubscribe happy path")
    public void test_subscribe_and_then_unsubscribe_happy_path() {
        UUri topic = createTopic();
        UTransport transport = new TestUTransport();
        UListener listener = new UListener() {
            @Override
            public void onReceive(UMessage message) {
                // Do nothing
            }
        };
        Subscriber subscriber = new InMemorySubscriber(transport, new InMemoryRpcClient(transport));
        subscriber.subscribe(topic, listener, null).thenAcceptAsync(response -> {
            assertFalse(subscriber.unsubscribe(topic, listener, null)
                .toCompletableFuture().isCompletedExceptionally());
        });
    }

    @Test
    @DisplayName("Test unsubscribe happy path")
    public void test_unsubscribe_happy_path() {
        UUri topic = createTopic();
        UTransport transport = new TestUTransport();
        Subscriber subscriber = new InMemorySubscriber(transport, new InMemoryRpcClient(transport));
        assertFalse(subscriber.unsubscribe(topic, new UListener() {
            @Override
            public void onReceive(UMessage message) {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'onReceive'");
            }
        }, null).toCompletableFuture().isCompletedExceptionally());
    }

    @Test
    @DisplayName("Test unregisterListener after we successfully subscribed to a topic")
    public void testUnregisterListener() {
        UUri topic = createTopic();
        UListener myListener = new UListener() {
            @Override
            public void onReceive(UMessage message) {
                // Do nothing
            }
        };
        UTransport transport = new TestUTransport();
        Subscriber subscriber = new InMemorySubscriber(transport, new InMemoryRpcClient(transport));

        subscriber.subscribe(topic, myListener, new CallOptions(100))
            .thenAcceptAsync(response -> {
                assertFalse(subscriber.unregisterListener(topic, myListener).toCompletableFuture().isCompletedExceptionally());
            });
    }


    @Test
    @DisplayName("Test unsubscribe with commstatus error using the UnHappyUnSubscribeUTransport")
    public void testUnsubscribeWithCommStatusError() {
        UUri topic = createTopic();
        UTransport transport = new CommStatusTransport();
        Subscriber subscriber = new InMemorySubscriber(transport, new InMemoryRpcClient(transport));
        
        subscriber.unsubscribe(topic, new UListener() {
            @Override
            public void onReceive(UMessage message) {
                return;
            }
        }, null).exceptionally(e -> {
            assertTrue(e instanceof UStatusException);
            assertEquals(((UStatusException) e).getCode(), UCode.FAILED_PRECONDITION);
            assertEquals(((UStatusException) e).getMessage(), "Communication error [FAILED_PRECONDITION]");
            return null;
        });
    }

    @Test
    @DisplayName("Test unsubscribe where the invokemethod throws an exception")
    public void testUnsubscribeWithException() {
        UUri topic = createTopic();
        UTransport transport = new TimeoutUTransport();
        Subscriber subscriber = new InMemorySubscriber(transport, new InMemoryRpcClient(transport));

        subscriber.unsubscribe(topic, new UListener() {
            @Override
            public void onReceive(UMessage message) {
                return;
            }
        }, new CallOptions(1)).exceptionally(e -> {
            assertTrue(e instanceof UStatusException);
            assertEquals(((UStatusException) e).getCode(), UCode.DEADLINE_EXCEEDED);
            assertEquals(((UStatusException) e).getMessage(), "Request timed out");
            return null;
        });
    }

    @Test
    @DisplayName("Test unsubscribe where the invokemethod throws an exception")
    public void testUnsubscribeWithException2() {
        UUri topic = createTopic();
        UTransport transport = new TimeoutUTransport();
        Subscriber subscriber = new InMemorySubscriber(transport, new InMemoryRpcClient(transport));

        subscriber.unsubscribe(topic, new UListener() {
            @Override
            public void onReceive(UMessage message) {
                return;
            }
        }, new CallOptions(1)).exceptionally(e -> {
            assertTrue(e instanceof UStatusException);
            assertEquals(((UStatusException) e).getCode(), UCode.DEADLINE_EXCEEDED);
            assertEquals(((UStatusException) e).getMessage(), "Request timed out");
            return null;
        });
    }


    private UUri createTopic() {
        return UUri.newBuilder()
            .setAuthorityName("hartley")
            .setUeId(3)
            .setUeVersionMajor(1)
            .setResourceId(0x8000)
            .build();
    }
}
