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

import org.eclipse.uprotocol.transport.UTransport;
import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UMessage;
import org.eclipse.uprotocol.v1.UPriority;
import org.eclipse.uprotocol.v1.UStatus;
import org.eclipse.uprotocol.v1.UUri;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class DefaultRpcClientTest {
    @Test
    @DisplayName("Test calling invokeMethod passing UPayload")
    public void testInvokeMethodWithPayload() {
        UPayload payload = UPayload.packToAny(UUri.newBuilder().build());
        RpcClient rpcClient = new DefaultRpcClient(new TestUTransport());
        CompletionStage<UPayload> response = rpcClient.invokeMethod(createMethodUri(), payload, null);
        assertNotNull(response);
        response.toCompletableFuture().join();
        assertFalse(response.toCompletableFuture().isCompletedExceptionally());
    }

    @Test
    @DisplayName("Test calling invokeMethod passing a UPaylod and calloptions")
    public void testInvokeMethodWithPayloadAndCallOptions() {
        UPayload payload = UPayload.packToAny(UUri.newBuilder().build());
        CallOptions options = new CallOptions(1000, UPriority.UPRIORITY_CS5);
        RpcClient rpcClient = new DefaultRpcClient(new TestUTransport());
        CompletionStage<UPayload> response = rpcClient.invokeMethod(createMethodUri(), payload, options);
        assertNotNull(response);
        response.toCompletableFuture().join();
        assertFalse(response.toCompletableFuture().isCompletedExceptionally());
    }

    @Test
    @DisplayName("Test calling invokeMethod passing a Null UPayload")
    public void testInvokeMethodWithNullPayload() {
        RpcClient rpcClient = new DefaultRpcClient(new TestUTransport());
        CompletionStage<UPayload> response = rpcClient.invokeMethod(createMethodUri(), null, CallOptions.DEFAULT);
        assertNotNull(response);
        response.toCompletableFuture().join();
        assertFalse(response.toCompletableFuture().isCompletedExceptionally());
    }
 
    @Test
    @DisplayName("Test calling invokeMethod with TimeoutUTransport that will timeout the request")
    public void testInvokeMethodWithTimeoutTransport() {
        final UPayload payload = UPayload.packToAny(UUri.newBuilder().build());
        final CallOptions options = new CallOptions(100, UPriority.UPRIORITY_CS5, "token");
        RpcClient rpcClient = new DefaultRpcClient(new TimeoutUTransport());
        final CompletionStage<UPayload> response = rpcClient.invokeMethod(createMethodUri(), payload, options);
        Exception exception = assertThrows(java.util.concurrent.ExecutionException.class, 
            response.toCompletableFuture()::get);
        assertEquals(exception.getMessage(),
                "java.util.concurrent.TimeoutException");
    }


    @Test
    @DisplayName("Test calling invokeMethod with MultiInvokeUTransport that will invoke multiple listeners")
    public void testInvokeMethodWithMultiInvokeTransport() {
        RpcClient rpcClient = new DefaultRpcClient(new TestUTransport());
        UPayload payload = UPayload.packToAny(UUri.newBuilder().build());

        CompletionStage<UPayload> response = rpcClient.invokeMethod(createMethodUri(), payload, null);
        assertNotNull(response);
        CompletionStage<UPayload> response2 = rpcClient.invokeMethod(createMethodUri(), payload, null);
        assertNotNull(response2);
        response.toCompletableFuture().join();
        response2.toCompletableFuture().join();
        assertFalse(response.toCompletableFuture().isCompletedExceptionally());
    }

    @Test
    @DisplayName("Test calling close for DefaultRpcClient when there are multiple response listeners registered")
    public void testCloseWithMultipleListeners() {
        DefaultRpcClient rpcClient = new DefaultRpcClient(new TestUTransport());
        UPayload payload = UPayload.packToAny(UUri.newBuilder().build());
        CompletionStage<UPayload> response = rpcClient.invokeMethod(createMethodUri(), payload, null);
        assertNotNull(response);
        CompletionStage<UPayload> response2 = rpcClient.invokeMethod(createMethodUri(), payload, null);
        assertNotNull(response2);
        rpcClient.close();
    }

    @Test
    @DisplayName("Test calling invokeMethod when we use the CommStatusTransport")
    public void testInvokeMethodWithCommStatusTransport() {
        RpcClient rpcClient = new DefaultRpcClient(new CommStatusTransport());
        UPayload payload = UPayload.packToAny(UUri.newBuilder().build());
        CompletionStage<UPayload> response = rpcClient.invokeMethod(createMethodUri(), payload, null);

        Exception exception = assertThrows(java.util.concurrent.ExecutionException.class, 
            response.toCompletableFuture()::get);
        assertTrue(response.toCompletableFuture().isCompletedExceptionally());
        assertEquals(exception.getMessage(), 
            "org.eclipse.uprotocol.communication.UStatusException: Communication error [FAILED_PRECONDITION]");
    }

    @Test
    @DisplayName("Test calling invokeMethod when we use the ErrorUTransport that fails the transport send()")
    public void testInvokeMethodWithErrorTransport() {
        UTransport transport = new TestUTransport() {
            @Override
            public UStatus send(UMessage message) {
                return UStatus.newBuilder().setCode(UCode.FAILED_PRECONDITION).build();
            }
        };
        
        RpcClient rpcClient = new DefaultRpcClient(transport);
        UPayload payload = UPayload.packToAny(UUri.newBuilder().build());
        CompletionStage<UPayload> response = rpcClient.invokeMethod(createMethodUri(), payload, null);

        Exception exception = assertThrows(java.util.concurrent.ExecutionException.class, 
            response.toCompletableFuture()::get);
        assertTrue(response.toCompletableFuture().isCompletedExceptionally());
        assertEquals(exception.getMessage(), 
            "org.eclipse.uprotocol.communication.UStatusException: ");
    }

   
    private UUri createMethodUri() {
        return UUri.newBuilder()
            .setAuthorityName("hartley")
            .setUeId(10)
            .setUeVersionMajor(1)
            .setResourceId(3).build();
    }
}
