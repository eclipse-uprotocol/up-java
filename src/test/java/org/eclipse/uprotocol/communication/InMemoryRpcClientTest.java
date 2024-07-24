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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.CompletableFuture;
import org.eclipse.uprotocol.transport.UTransport;
import org.eclipse.uprotocol.transport.builder.UMessageBuilder;
import org.eclipse.uprotocol.uuid.factory.UuidFactory;
import org.eclipse.uprotocol.v1.UAttributes;
import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UMessage;
import org.eclipse.uprotocol.v1.UPriority;
import org.eclipse.uprotocol.v1.UStatus;
import org.eclipse.uprotocol.v1.UUri;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class InMemoryRpcClientTest {
    @Test
    @DisplayName("Test calling invokeMethod passing UPayload")
    public void testInvokeMethodWithPayload() {
        UPayload payload = UPayload.packToAny(UUri.newBuilder().build());
        RpcClient rpcClient = new InMemoryRpcClient(new TestUTransport());
        final CompletionStage<UPayload> response = rpcClient.invokeMethod(createMethodUri(), payload, null);
        assertNotNull(response);
        assertDoesNotThrow(() -> {
            UPayload payload1 = response.toCompletableFuture().get();
            assertTrue(payload.equals(payload1));
        });
    }

    @Test
    @DisplayName("Test calling invokeMethod passing a UPaylod and calloptions")
    public void testInvokeMethodWithPayloadAndCallOptions() {
        UPayload payload = UPayload.packToAny(UUri.newBuilder().build());
        CallOptions options = new CallOptions(1000, UPriority.UPRIORITY_CS5);
        RpcClient rpcClient = new InMemoryRpcClient(new TestUTransport());
        CompletionStage<UPayload> response = rpcClient.invokeMethod(createMethodUri(), payload, options);
        assertNotNull(response);
        assertDoesNotThrow(() -> {
            UPayload result = response.toCompletableFuture().get();
            assertTrue(result.equals(payload));
        });
        assertFalse(response.toCompletableFuture().isCompletedExceptionally());
    }

    @Test
    @DisplayName("Test calling invokeMethod passing a Null UPayload")
    public void testInvokeMethodWithNullPayload() {
        RpcClient rpcClient = new InMemoryRpcClient(new TestUTransport());
        CompletionStage<UPayload> response = rpcClient.invokeMethod(createMethodUri(), null, CallOptions.DEFAULT);
        assertNotNull(response);
        assertDoesNotThrow(() -> {
            UPayload payload = response.toCompletableFuture().get();
            assertEquals(payload, UPayload.EMPTY);
        });
        assertFalse(response.toCompletableFuture().isCompletedExceptionally());
    }
 
    @Test
    @DisplayName("Test calling invokeMethod with TimeoutUTransport that will timeout the request")
    public void testInvokeMethodWithTimeoutTransport() {
        final UPayload payload = UPayload.packToAny(UUri.newBuilder().build());
        final CallOptions options = new CallOptions(100, UPriority.UPRIORITY_CS5, "token");
        RpcClient rpcClient = new InMemoryRpcClient(new TimeoutUTransport());
        final CompletionStage<UPayload> response = rpcClient.invokeMethod(createMethodUri(), payload, options);
        
        Exception exception = assertThrows(java.util.concurrent.ExecutionException.class, 
            response.toCompletableFuture()::get);
        assertEquals(exception.getMessage(),
                "org.eclipse.uprotocol.communication.UStatusException: Request timed out");
        assertEquals(((UStatus) (((UStatusException) exception.getCause())).getStatus()).getCode(),
            UCode.DEADLINE_EXCEEDED);
    }


    @Test
    @DisplayName("Test calling close for DefaultRpcClient when there are multiple response listeners registered")
    public void testCloseWithMultipleListeners() {
        InMemoryRpcClient rpcClient = new InMemoryRpcClient(new TestUTransport());
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
        RpcClient rpcClient = new InMemoryRpcClient(new CommStatusTransport());
        UPayload payload = UPayload.packToAny(UUri.newBuilder().build());
        CompletionStage<UPayload> response = rpcClient.invokeMethod(createMethodUri(), payload, null);

        Exception exception = assertThrows(java.util.concurrent.ExecutionException.class, 
            response.toCompletableFuture()::get);
        assertTrue(response.toCompletableFuture().isCompletedExceptionally());
        assertEquals(exception.getMessage(), 
            "org.eclipse.uprotocol.communication.UStatusException: Communication error [FAILED_PRECONDITION]");

        assertEquals(((UStatus) (((UStatusException) exception.getCause())).getStatus()).getCode(),
            UCode.FAILED_PRECONDITION);
    }

    @Test
    @DisplayName("Test calling invokeMethod when we use the ErrorUTransport that fails the transport send()")
    public void testInvokeMethodWithErrorTransport() {
        UTransport transport = new TestUTransport() {
            @Override
            public CompletionStage<UStatus> send(UMessage message) {
                return CompletableFuture.completedFuture(
                    UStatus.newBuilder().setCode(UCode.FAILED_PRECONDITION).build());
            }
        };
        
        RpcClient rpcClient = new InMemoryRpcClient(transport);
        UPayload payload = UPayload.packToAny(UUri.newBuilder().build());
        CompletionStage<UPayload> response = rpcClient.invokeMethod(createMethodUri(), payload, null);

        Exception exception = assertThrows(java.util.concurrent.ExecutionException.class, 
            response.toCompletableFuture()::get);
        assertTrue(response.toCompletableFuture().isCompletedExceptionally());
        assertEquals(exception.getMessage(), 
            "org.eclipse.uprotocol.communication.UStatusException: ");
        assertEquals(((UStatus) (((UStatusException) exception.getCause())).getStatus()).getCode(),
            UCode.FAILED_PRECONDITION);
    }


    @Test
    @DisplayName("Test calling handleResponse when it gets a response for an unknown request")
    public void testHandleResponseForUnknownRequest() {
        UTransport transport = new TestUTransport() {
            @Override
            public UMessage buildResponse(UMessage request) {
                UAttributes attributes = UAttributes.newBuilder(request.getAttributes())
                    .setId(UuidFactory.Factories.UPROTOCOL.factory().create()).build();
                return UMessageBuilder.response(attributes).build();
            }
        };
        
        InMemoryRpcClient rpcClient = new InMemoryRpcClient(transport);

        CallOptions options = new CallOptions(10, UPriority.UPRIORITY_CS5);
        CompletionStage<UPayload> response = rpcClient.invokeMethod(createMethodUri(), null, options);
        assertNotNull(response);
        assertThrows(ExecutionException.class, () -> {
            UPayload payload = response.toCompletableFuture().get();
            assertEquals(payload, UPayload.EMPTY);
        });
        assertTrue(response.toCompletableFuture().isCompletedExceptionally());
    }


    @Test
    @DisplayName("Test calling handleResponse when it gets a message that is not a response")
    public void testHandleResponseForNonResponseMessage() {
        UTransport transport = new TestUTransport() {
            @Override
            public UMessage buildResponse(UMessage request) {
                return UMessageBuilder.publish(createMethodUri()).build();
            }
        };
        
        InMemoryRpcClient rpcClient = new InMemoryRpcClient(transport);

        CallOptions options = new CallOptions(10, UPriority.UPRIORITY_CS5);
        CompletionStage<UPayload> response = rpcClient.invokeMethod(createMethodUri(), null, options);
        assertNotNull(response);
        assertThrows(ExecutionException.class, () -> {
            UPayload payload = response.toCompletableFuture().get();
            assertEquals(payload, UPayload.EMPTY);
        });
        assertTrue(response.toCompletableFuture().isCompletedExceptionally());
    }

    @Test
    @DisplayName("Test calling invokeMethod when we set comm status to UCode.OK")
    public void testInvokeMethodWithCommStatusUCodeOKTransport() {
        RpcClient rpcClient = new InMemoryRpcClient(new CommStatusOkTransport());
        UPayload payload = UPayload.packToAny(UUri.newBuilder().build());
        CompletionStage<UPayload> response = rpcClient.invokeMethod(createMethodUri(), payload, null);
        assertFalse(response.toCompletableFuture().isCompletedExceptionally());
        assertDoesNotThrow(() -> {
            Optional<UStatus> unpackedStatus = UPayload.unpack(response.toCompletableFuture().get(), UStatus.class);
            assertTrue(unpackedStatus.isPresent());
            assertEquals(UCode.OK, unpackedStatus.get().getCode());
            assertEquals("No Communication Error", unpackedStatus.get().getMessage());
        });
    }



    private UUri createMethodUri() {
        return UUri.newBuilder()
            .setAuthorityName("hartley")
            .setUeId(10)
            .setUeVersionMajor(1)
            .setResourceId(3).build();
    }
}
