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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.eclipse.uprotocol.transport.StaticUriProvider;
import org.eclipse.uprotocol.transport.UListener;
import org.eclipse.uprotocol.transport.UTransport;
import org.eclipse.uprotocol.uri.factory.UriFactory;
import org.eclipse.uprotocol.v1.UUri;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// [utest->dsn~communication-layer-impl-default~1]
class UClientTest {
    private static final UUri TRANSPORT_SOURCE = UUri.newBuilder()
            .setAuthorityName("my-vehicle")
            .setUeId(0xa1)
            .setUeVersionMajor(0x01)
            .setResourceId(0x0000)
            .build();
    private static final UUri TOPIC_URI = UUri.newBuilder(TRANSPORT_SOURCE)
            .setResourceId(0x8000)
            .build();
    private static final UUri DESTINATION_URI = UUri.newBuilder()
            .setAuthorityName("other-vehicle")
            .setUeId(0x2bbbb)
            .setUeVersionMajor(0x02)
            .build();
    private static final UUri METHOD_URI = UUri.newBuilder(DESTINATION_URI)
            .setResourceId(3)
            .build();

    private RpcClient rpcClient;
    private RpcServer rpcServer;
    private Publisher publisher;
    private Notifier notifier;

    @BeforeEach
    void setUp() {
        rpcClient = mock(RpcClient.class);
        rpcServer = mock(RpcServer.class);
        publisher = mock(Publisher.class);
        notifier = mock(Notifier.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testFactoryMethod() {
        var transport = mock(UTransport.class);
        when(transport.registerListener(any(UUri.class), any(Optional.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(null));
        var uriProvider = StaticUriProvider.of(TRANSPORT_SOURCE);
        UClient.create(transport, uriProvider);
        verify(transport).registerListener(any(UUri.class), eq(Optional.of(TRANSPORT_SOURCE)), any(UListener.class));
    }

    @Test
    void testPublisher() {
        when(publisher.publish(anyInt(), any(CallOptions.class), any(UPayload.class)))
            .thenReturn(CompletableFuture.completedFuture(null));
        var client = new UClient(rpcClient, rpcServer, publisher, notifier);
        client.publish(TOPIC_URI.getResourceId()).toCompletableFuture().join();
        verify(publisher).publish(eq(TOPIC_URI.getResourceId()), any(CallOptions.class), any(UPayload.class));
    }

    @Test
    void testNotifier() {
        when(notifier.notify(anyInt(), any(UUri.class), any(CallOptions.class), any(UPayload.class)))
            .thenReturn(CompletableFuture.completedFuture(null));
        var client = new UClient(rpcClient, rpcServer, publisher, notifier);
        client.notify(TOPIC_URI.getResourceId(), DESTINATION_URI).toCompletableFuture().join();
        verify(notifier).notify(
            eq(TOPIC_URI.getResourceId()),
            eq(DESTINATION_URI),
            any(CallOptions.class),
            any(UPayload.class));

        when(notifier.registerNotificationListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(null));
        when(notifier.unregisterNotificationListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(null));
        final var listener = mock(UListener.class);
        client = new UClient(rpcClient, rpcServer, publisher, notifier);
        client.registerNotificationListener(TOPIC_URI, listener).toCompletableFuture().join();
        verify(notifier).registerNotificationListener(TOPIC_URI, listener);
        client.unregisterNotificationListener(TOPIC_URI, listener).toCompletableFuture().join();
        verify(notifier).unregisterNotificationListener(TOPIC_URI, listener);
    }

    @Test
    void testRpcClient() {
        when(rpcClient.invokeMethod(any(UUri.class), any(UPayload.class), any(CallOptions.class)))
            .thenReturn(CompletableFuture.completedFuture(null));
        var client = new UClient(rpcClient, rpcServer, publisher, notifier);
        client.invokeMethod(METHOD_URI, UPayload.EMPTY, CallOptions.DEFAULT).toCompletableFuture().join();
        verify(rpcClient).invokeMethod(eq(METHOD_URI), eq(UPayload.EMPTY), eq(CallOptions.DEFAULT));
    }

    @Test
    void testRpcServer() {
        when(rpcServer.registerRequestHandler(any(UUri.class), anyInt(), any(RequestHandler.class)))
            .thenReturn(CompletableFuture.completedFuture(null));
        when(rpcServer.unregisterRequestHandler(any(UUri.class), anyInt(), any(RequestHandler.class)))
            .thenReturn(CompletableFuture.completedFuture(null));
        final var handler = mock(RequestHandler.class);
        var client = new UClient(rpcClient, rpcServer, publisher, notifier);
        client.registerRequestHandler(
            UriFactory.ANY,
            METHOD_URI.getResourceId(),
            handler).toCompletableFuture().join();
        verify(rpcServer).registerRequestHandler(
            eq(UriFactory.ANY),
            eq(METHOD_URI.getResourceId()),
            eq(handler));

        client.unregisterRequestHandler(
            UriFactory.ANY,
            METHOD_URI.getResourceId(),
            handler).toCompletableFuture().join();
        verify(rpcServer).unregisterRequestHandler(
            eq(UriFactory.ANY),
            eq(METHOD_URI.getResourceId()),
            eq(handler));
    }
}
