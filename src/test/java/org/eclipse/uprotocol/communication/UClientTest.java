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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CompletionStage;

import org.eclipse.uprotocol.transport.UListener;
import org.eclipse.uprotocol.v1.UMessage;
import org.eclipse.uprotocol.v1.UUri;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class UClientTest {
    
    // Main functionality is tested in the various individual implementations
    @Test
    @DisplayName("Test happy path for all APIs")
    public void test() {
        UClient client = UClient.create(new TestUTransport());
        UListener listener = new UListener() {
            @Override
            public void onReceive(UMessage message) {
                assertNotNull(message);
            }
        };

        assertFalse(client.notify(createTopic(), createDestinationUri(), null)
            .toCompletableFuture().isCompletedExceptionally());
        
        assertFalse(client.publish(createTopic(), null)
            .toCompletableFuture().isCompletedExceptionally());

        assertFalse(client.invokeMethod(createMethodUri(), null, null)
            .toCompletableFuture().isCompletedExceptionally());

        assertFalse(client.subscribe(createTopic(), listener, null)
            .toCompletableFuture().isCompletedExceptionally());

        assertFalse(client.unsubscribe(createTopic(), listener, null)
            .toCompletableFuture().isCompletedExceptionally());

        // The listener is not registered anymore so it should fail
        CompletionStage<Void> future = client.unregisterListener(createTopic(), listener);
        Exception exception = assertThrows(java.util.concurrent.ExecutionException.class, 
            future.toCompletableFuture()::get);
        assertTrue(future.toCompletableFuture().isCompletedExceptionally());
        
        
        assertFalse(client.registerNotificationListener(createTopic(), listener)
            .toCompletableFuture().isCompletedExceptionally());

        assertFalse(client.unregisterNotificationListener(createTopic(), listener)
            .toCompletableFuture().isCompletedExceptionally());

        RequestHandler handler = new RequestHandler() {
            @Override
            public UPayload handleRequest(UMessage message) throws UStatusException {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'handleRequest'");
            }
        };

        assertFalse(client.registerRequestHandler(createMethodUri(), handler)
            .toCompletableFuture().isCompletedExceptionally());
        
        assertFalse(client.unregisterRequestHandler(createMethodUri(), handler)
            .toCompletableFuture().isCompletedExceptionally());
        
    }


   
    private UUri createTopic() {
        return UUri.newBuilder()
            .setAuthorityName("Hartley")
            .setUeId(4)
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


    private UUri createMethodUri() {
        return UUri.newBuilder()
            .setAuthorityName("Hartley")
            .setUeId(4)
            .setUeVersionMajor(1)
            .setResourceId(3).build();
    }
}
