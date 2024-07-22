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

package org.eclipse.uprotocol.client.utwin.v2;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import org.eclipse.uprotocol.communication.InMemoryRpcClient;
import org.eclipse.uprotocol.communication.RpcClient;
import org.eclipse.uprotocol.communication.SimpleNotifier;
import org.eclipse.uprotocol.communication.UPayload;
import org.eclipse.uprotocol.communication.UStatusException;
import org.eclipse.uprotocol.core.utwin.v2.GetLastMessagesResponse;
import org.eclipse.uprotocol.transport.UTransport;
import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UStatus;
import org.eclipse.uprotocol.v1.UUri;
import org.eclipse.uprotocol.v1.UUriBatch;

/**
 * The uTwin client implementation using InMemory up-L2 communication layer implementation.
 * This is the test code for said implementation.
 */
@ExtendWith(MockitoExtension.class)
public class InMemoryUTwinClientTest {
    @Mock
    private UTransport transport;

    
    private final UUri topic = UUri.newBuilder().setAuthorityName("hartley").setUeId(3)
        .setUeVersionMajor(1).setResourceId(0x8000).build();

    private final UUri source = UUri.newBuilder().setAuthorityName("Hartley").setUeId(4)
        .setUeVersionMajor(1).build();

    
    @BeforeEach
    public void setup() {
        transport = mock(UTransport.class);
    }

    
    @Test
    @DisplayName("Test calling getLastMessages() passing null transport")
    void testGetLastMessagesNull() {
        assertThrows(NullPointerException.class, () -> {
            new InMemoryUTwinClient((UTransport)null);
        });
        
    }
    

    @Test
    @DisplayName("Test calling getLastMessages() passing null topics")
    void testGetLastMessagesNullTopics() {

        when(transport.registerListener(any(), any(), any())).thenReturn(
            CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));

        InMemoryUTwinClient client = new InMemoryUTwinClient(transport);
        assertThrows(NullPointerException.class, () -> {
            client.getLastMessages(null);
        });
    }


    @Test
    @DisplayName("Test calling getLastMessages() passing empty topics")
    void testGetLastMessagesEmptyTopics() {
        UUriBatch topics = UUriBatch.getDefaultInstance();

        when(transport.registerListener(any(), any(), any())).thenReturn(
            CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));

        InMemoryUTwinClient client = new InMemoryUTwinClient(transport);
        assertThrows(ExecutionException.class, () -> {
            client.getLastMessages(topics).handle((r, e) -> {
                assertNotNull(e);
                UStatusException t = (UStatusException) e.getCause();
                assertNotNull(t);
                assertEquals(t.getCode(), UCode.INVALID_ARGUMENT);
                assertEquals(t.getMessage(), "topics must not be empty");
                return r;
            }).toCompletableFuture().get();
        });
    }


    @Test
    @DisplayName("Test calling getLastMessages() with valid topics")
    void testGetLastMessages() {
        
        RpcClient rpcClient = Mockito.mock(RpcClient.class);

        UUriBatch topics = UUriBatch.newBuilder().addUris(topic).build();

        when(rpcClient.invokeMethod(any(), any(), any())).thenReturn(
            CompletableFuture.completedFuture(UPayload.pack(GetLastMessagesResponse.getDefaultInstance())));

        InMemoryUTwinClient client = new InMemoryUTwinClient(rpcClient);
        CompletionStage<GetLastMessagesResponse> response = client.getLastMessages(topics);
        assertNotNull(response);
        assertFalse(response.toCompletableFuture().isCompletedExceptionally());
        assertDoesNotThrow(() -> response.toCompletableFuture().get());
    }


    @Test
    @DisplayName("Test calling getLastMessages() when the RpcClient completes exceptionally")
    void testGetLastMessagesException() {
        RpcClient rpcClient = Mockito.mock(RpcClient.class);

        UUriBatch topics = UUriBatch.newBuilder().addUris(topic).build();

        when(rpcClient.invokeMethod(any(), any(), any())).thenReturn(
            CompletableFuture.failedFuture(new UStatusException(UCode.NOT_FOUND, "Not found")));

        InMemoryUTwinClient client = new InMemoryUTwinClient(rpcClient);
        CompletionStage<GetLastMessagesResponse> response = client.getLastMessages(topics);
        assertNotNull(response);
        assertTrue(response.toCompletableFuture().isCompletedExceptionally());
        assertDoesNotThrow(() -> {
            response
                .handle((r, e) -> {
                    assertNotNull(e);
                    UStatusException t = (UStatusException) e.getCause();
                    assertNotNull(t);
                    assertEquals(t.getCode(), UCode.NOT_FOUND);
                    assertEquals(t.getMessage(), "Not found");
                    return r;
                })
                .toCompletableFuture().get();
        });
    }

}
