/**
 * SPDX-FileCopyrightText: 2025 Contributors to the Eclipse Foundation
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
package org.eclipse.uprotocol.client.usubscription.v3;

import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import org.eclipse.uprotocol.communication.CallOptions;
import org.eclipse.uprotocol.communication.InMemoryRpcClient;
import org.eclipse.uprotocol.communication.RpcClient;
import org.eclipse.uprotocol.communication.UPayload;
import org.eclipse.uprotocol.core.usubscription.v3.FetchSubscribersRequest;
import org.eclipse.uprotocol.core.usubscription.v3.FetchSubscribersResponse;
import org.eclipse.uprotocol.core.usubscription.v3.FetchSubscriptionsRequest;
import org.eclipse.uprotocol.core.usubscription.v3.FetchSubscriptionsResponse;
import org.eclipse.uprotocol.core.usubscription.v3.NotificationsRequest;
import org.eclipse.uprotocol.core.usubscription.v3.NotificationsResponse;
import org.eclipse.uprotocol.core.usubscription.v3.SubscriptionRequest;
import org.eclipse.uprotocol.core.usubscription.v3.SubscriptionResponse;
import org.eclipse.uprotocol.core.usubscription.v3.UnsubscribeRequest;
import org.eclipse.uprotocol.core.usubscription.v3.UnsubscribeResponse;
import org.eclipse.uprotocol.v1.UUri;

class RpcClientBasedUSubscriptionClientTest {

    private static final UUri TOPIC = UUri.newBuilder()
        .setAuthorityName("hartley")
        .setUeId(3)
        .setUeVersionMajor(1)
        .setResourceId(0x8000)
        .build();

    private RpcClient rpcClient;
    private CallOptions callOptions;
    private USubscriptionClient subscriptionClient;

    @BeforeEach
    void setup() {
        rpcClient = mock(InMemoryRpcClient.class);
        callOptions = CallOptions.DEFAULT;
        subscriptionClient = new RpcClientBasedUSubscriptionClient(rpcClient, callOptions);
    }

    @Test
    @DisplayName("Test constructors require RpcClient")
    void testConstructorsRequireRpcClient() {
        assertThrows(
            NullPointerException.class,
            () -> new RpcClientBasedUSubscriptionClient(null, CallOptions.DEFAULT));
        assertThrows(
            NullPointerException.class,
            () -> new RpcClientBasedUSubscriptionClient(null, CallOptions.DEFAULT, 0x000, "my-vehicle"));
    }

    @Test
    @DisplayName("Test constructor rejects invalid instance ID")
    void testConstructorRejectsInvalidInstanceId() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new RpcClientBasedUSubscriptionClient(rpcClient, CallOptions.DEFAULT, -1, "local"));
        assertThrows(
            IllegalArgumentException.class,
            () -> new RpcClientBasedUSubscriptionClient(rpcClient, CallOptions.DEFAULT, 0xFFFF, "local"));
    }

    @Test
    void testSubscribeInvokesRpcClient() {
        var request = SubscriptionRequest.newBuilder().setTopic(TOPIC).build();
        var response = SubscriptionResponse.getDefaultInstance();
        when(rpcClient.invokeMethod(any(UUri.class), any(UPayload.class), any(CallOptions.class)))
            .thenReturn(CompletableFuture.completedFuture(UPayload.pack(response)));
        var actualResponse = subscriptionClient.subscribe(request).toCompletableFuture().join();
        verify(rpcClient).invokeMethod(any(UUri.class), eq(UPayload.pack(request)), eq(callOptions));
        assertEquals(response, actualResponse);
    }

    @Test
    void testUnsubscribeInvokesRpcClient() {
        var request = UnsubscribeRequest.newBuilder().setTopic(TOPIC).build();
        var response = UnsubscribeResponse.getDefaultInstance();
        when(rpcClient.invokeMethod(any(UUri.class), any(UPayload.class), any(CallOptions.class)))
            .thenReturn(CompletableFuture.completedFuture(UPayload.pack(response)));
        var actualResponse = subscriptionClient.unsubscribe(request).toCompletableFuture().join();
        verify(rpcClient).invokeMethod(any(UUri.class), eq(UPayload.pack(request)), eq(callOptions));
        assertEquals(response, actualResponse);
    }

    @Test
    void testFetchSubscribersInvokesRpcClient() {
        var request = FetchSubscribersRequest.newBuilder().setTopic(TOPIC).build();
        var response = FetchSubscribersResponse.getDefaultInstance();
        when(rpcClient.invokeMethod(any(UUri.class), any(UPayload.class), any(CallOptions.class)))
            .thenReturn(CompletableFuture.completedFuture(UPayload.pack(response)));
        var actualResponse = subscriptionClient.fetchSubscribers(request).toCompletableFuture().join();
        verify(rpcClient).invokeMethod(any(UUri.class), eq(UPayload.pack(request)), eq(callOptions));
        assertEquals(response, actualResponse);
    }

    @Test
    void testFetchSubscriptionsInvokesRpcClient() {
        var request = FetchSubscriptionsRequest.newBuilder().setTopic(TOPIC).build();
        var response = FetchSubscriptionsResponse.getDefaultInstance();
        when(rpcClient.invokeMethod(any(UUri.class), any(UPayload.class), any(CallOptions.class)))
            .thenReturn(CompletableFuture.completedFuture(UPayload.pack(response)));
        var actualResponse = subscriptionClient.fetchSubscriptions(request).toCompletableFuture().join();
        verify(rpcClient).invokeMethod(any(UUri.class), eq(UPayload.pack(request)), eq(callOptions));
        assertEquals(response, actualResponse);
    }

    @Test
    void testRegisterForNotificationsInvokesRpcClient() {
        var request = NotificationsRequest.newBuilder().setTopic(TOPIC).build();
        var response = NotificationsResponse.getDefaultInstance();
        when(rpcClient.invokeMethod(any(UUri.class), any(UPayload.class), any(CallOptions.class)))
            .thenReturn(CompletableFuture.completedFuture(UPayload.pack(response)));
        var actualResponse = subscriptionClient.registerForNotifications(request).toCompletableFuture().join();
        verify(rpcClient).invokeMethod(any(UUri.class), eq(UPayload.pack(request)), eq(callOptions));
        assertEquals(response, actualResponse);
    }

    @Test
    void testUnregisterForNotificationsInvokesRpcClient() {
        var request = NotificationsRequest.newBuilder().setTopic(TOPIC).build();
        var response = NotificationsResponse.getDefaultInstance();
        when(rpcClient.invokeMethod(any(UUri.class), any(UPayload.class), any(CallOptions.class)))
            .thenReturn(CompletableFuture.completedFuture(UPayload.pack(response)));
        var actualResponse = subscriptionClient.unregisterForNotifications(request).toCompletableFuture().join();
        verify(rpcClient).invokeMethod(any(UUri.class), eq(UPayload.pack(request)), eq(callOptions));
        assertEquals(response, actualResponse);
    }
}
