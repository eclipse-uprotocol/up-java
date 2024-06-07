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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.eclipse.uprotocol.core.usubscription.v3.SubscriptionResponse;
import org.eclipse.uprotocol.core.usubscription.v3.UnsubscribeResponse;
import org.eclipse.uprotocol.transport.UListener;
import org.eclipse.uprotocol.transport.UTransport;
import org.eclipse.uprotocol.transport.builder.UMessageBuilder;
import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UMessage;
import org.eclipse.uprotocol.v1.UStatus;
import org.eclipse.uprotocol.v1.UUri;


public class DefaultSubscriberTest {
    @Test
    @DisplayName("Test subscribe happy path")
    public void test_subscribe_happy_path() {
        UUri topic = createTopic();
        UTransport transport = new HappySubscribeUTransport();
        Subscriber subscriber = new DefaultSubscriber(transport, new DefaultRpcClient(transport));
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
    @DisplayName("Test unsubscribe happy path")
    public void test_unsubscribe_happy_path() {
        UUri topic = createTopic();
        UTransport transport = new HappyUnSubscribeUTransport();
        Subscriber subscriber = new DefaultSubscriber(transport, new DefaultRpcClient(transport));
        UStatus response = subscriber.unsubscribe(topic, new UListener() {
            @Override
            public void onReceive(UMessage message) {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'onReceive'");
            }
        }, null);
        assertEquals(response.getMessage(), "");
        assertEquals(response.getCode(), UCode.OK );
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
        UTransport transport = new HappySubscribeUTransport();
        Subscriber subscriber = new DefaultSubscriber(transport, new DefaultRpcClient(transport));

        CompletionStage<SubscriptionResponse> response = subscriber.subscribe(topic, myListener, new CallOptions(100));
        response.toCompletableFuture().join();
        assertFalse(response.toCompletableFuture().isCompletedExceptionally());
        UStatus status = subscriber.unregisterListener(topic, myListener);
        assertEquals(status.getCode(), UCode.OK);
    }


    @Test
    @DisplayName("Test unsubscribe with commstatus error using the UnHappyUnSubscribeUTransport")
    public void testUnsubscribeWithCommStatusError() {
        UUri topic = createTopic();
        UTransport transport = new CommStatusTransport();
        Subscriber subscriber = new DefaultSubscriber(transport, new DefaultRpcClient(transport));
        
        UStatus response = subscriber.unsubscribe(topic, new UListener() {
            @Override
            public void onReceive(UMessage message) {
                return;
            }
        }, null);
        assertEquals(response.getMessage(), "Communication error [FAILED_PRECONDITION]");
        assertEquals(response.getCode(), UCode.FAILED_PRECONDITION);
    
    }

    @Test
    @DisplayName("Test unsubscribe where the invokemethod throws an exception")
    public void testUnsubscribeWithException() {
        UUri topic = createTopic();
        UTransport transport = new TimeoutUTransport();
        Subscriber subscriber = new DefaultSubscriber(transport, new DefaultRpcClient(transport));

        UStatus response = subscriber.unsubscribe(topic, new UListener() {
            @Override
            public void onReceive(UMessage message) {
                return;
            }
        }, new CallOptions(1));
        assertEquals(response.getMessage(), "Request timed out");
        assertEquals(response.getCode(), UCode.DEADLINE_EXCEEDED);
    }

    @Test
    @DisplayName("Test unsubscribe where the invokemethod throws an exception")
    public void testUnsubscribeWithException2() {
        UUri topic = createTopic();
        UTransport transport = new TimeoutUTransport();
        Subscriber subscriber = new DefaultSubscriber(transport, new DefaultRpcClient(transport));

        UStatus response = subscriber.unsubscribe(topic, new UListener() {
            @Override
            public void onReceive(UMessage message) {
                return;
            }
        }, new CallOptions(1));
        assertEquals(response.getMessage(), "Request timed out");
        assertEquals(response.getCode(), UCode.DEADLINE_EXCEEDED);
    }


    private UUri createTopic() {
        return UUri.newBuilder()
            .setAuthorityName("hartley")
            .setUeId(3)
            .setUeVersionMajor(1)
            .setResourceId(0x8000)
            .build();
    }


    /**
     * Test UTransport that will return SubscribeResponse
     */
    private class HappySubscribeUTransport extends TestUTransport {
        @Override
        public UMessage buildResponse(UMessage request) {
            return UMessageBuilder.response(request.getAttributes()).build(UPayload.pack(
                SubscriptionResponse.newBuilder().setTopic(createTopic()).build()));
        }
    };


    /**
     * Test UTransport that will return SubscribeResponse
     */
    private class HappyUnSubscribeUTransport extends TestUTransport {
        @Override
        public UMessage buildResponse(UMessage request) {
            return UMessageBuilder.response(request.getAttributes()).build(
                UPayload.pack(UnsubscribeResponse.newBuilder().build()));
        }
    };
}
