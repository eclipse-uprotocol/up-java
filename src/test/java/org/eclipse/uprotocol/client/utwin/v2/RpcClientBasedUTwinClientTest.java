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

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.eclipse.uprotocol.communication.CallOptions;
import org.eclipse.uprotocol.communication.RpcClient;
import org.eclipse.uprotocol.communication.UPayload;
import org.eclipse.uprotocol.communication.UStatusException;
import org.eclipse.uprotocol.core.utwin.v2.GetLastMessagesResponse;
import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UUri;
import org.eclipse.uprotocol.v1.UUriBatch;

/**
 * The uTwin client implementation using RpcClient uP-L2 communication layer interface.
 * This is the test code for said implementation.
 */
@ExtendWith(MockitoExtension.class)
class RpcClientBasedUTwinClientTest {

    private static final UUri TOPIC = UUri.newBuilder()
        .setAuthorityName("hartley")
        .setUeId(0x0003)
        .setUeVersionMajor(0x01)
        .setResourceId(0x8000)
        .build();

    @Mock
    private RpcClient rpcClient;
    private UTwinClient twinClient;

    @BeforeEach
    void setUp() {
        twinClient = new RpcClientBasedUTwinClient(rpcClient);
    }

    @Test
    @DisplayName("Test calling getLastMessages() with valid topics")
    void testGetLastMessages() {
        when(rpcClient.invokeMethod(
                eq(RpcClientBasedUTwinClient.GETLASTMESSAGE_METHOD),
                any(UPayload.class),
                any(CallOptions.class)))
            .thenReturn(CompletableFuture.completedFuture(UPayload.pack(GetLastMessagesResponse.getDefaultInstance())));

        UUriBatch topics = UUriBatch.newBuilder().addUris(TOPIC).build();
        var response = twinClient.getLastMessages(topics).toCompletableFuture().join();
        assertEquals(0, response.getResponsesCount());
    }


    @Test
    @DisplayName("Test calling getLastMessages() with empty topics")
    void testGetLastMessagesEmptyTopics() {
        UUriBatch topics = UUriBatch.getDefaultInstance();
        var exception = assertThrows(CompletionException.class, () -> {
            twinClient.getLastMessages(topics).toCompletableFuture().join();
        });
        assertInstanceOf(UStatusException.class, exception.getCause());
        assertEquals(UCode.INVALID_ARGUMENT, ((UStatusException) exception.getCause()).getCode());
    }


    @Test
    @DisplayName("Test calling getLastMessages() when the RpcClient completes exceptionally")
    void testGetLastMessagesException() {
        when(rpcClient.invokeMethod(
                eq(RpcClientBasedUTwinClient.GETLASTMESSAGE_METHOD),
                any(UPayload.class),
                any(CallOptions.class)))
            .thenReturn(CompletableFuture.failedFuture(new UStatusException(UCode.NOT_FOUND, "Not found")));

        UUriBatch topics = UUriBatch.newBuilder().addUris(TOPIC).build();
        var exception = assertThrows(CompletionException.class, () -> {
            twinClient.getLastMessages(topics).toCompletableFuture().join();
        });
        assertInstanceOf(UStatusException.class, exception.getCause());
        assertEquals(UCode.NOT_FOUND, ((UStatusException) exception.getCause()).getCode());
    }
}
