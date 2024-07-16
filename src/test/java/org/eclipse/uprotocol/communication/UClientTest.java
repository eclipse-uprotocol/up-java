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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

        assertDoesNotThrow(() -> 
            client.notify(createTopic(), createDestinationUri()).toCompletableFuture().get());
        
        assertDoesNotThrow(() -> 
            client.publish(createTopic()).toCompletableFuture().get());

        assertDoesNotThrow(() ->
            client.invokeMethod(createMethodUri(), null, null).toCompletableFuture().get());

        assertDoesNotThrow(() ->
            client.registerNotificationListener(createTopic(), listener).toCompletableFuture().get());

        assertDoesNotThrow(() ->
            client.unregisterNotificationListener(createTopic(), listener).toCompletableFuture().get());

        RequestHandler handler = new RequestHandler() {
            @Override
            public UPayload handleRequest(UMessage message) throws UStatusException {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'handleRequest'");
            }
        };

        assertDoesNotThrow(() ->
            client.registerRequestHandler(createMethodUri(), handler).toCompletableFuture().get());
        
        assertDoesNotThrow(() ->
            client.unregisterRequestHandler(createMethodUri(), handler).toCompletableFuture().get());

        client.close();
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
