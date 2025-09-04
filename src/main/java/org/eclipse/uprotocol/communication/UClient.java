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

import java.util.Objects;
import java.util.concurrent.CompletionStage;

import org.eclipse.uprotocol.transport.LocalUriProvider;
import org.eclipse.uprotocol.transport.UListener;
import org.eclipse.uprotocol.transport.UTransport;
import org.eclipse.uprotocol.v1.UUri;

/**
 * A client for Communication Layer APIs.
 */
public final class UClient implements RpcServer, Notifier, Publisher, RpcClient {

    private final RpcServer rpcServer;
    private final Publisher publisher;
    private final Notifier notifier;
    private final RpcClient rpcClient;

    /**
     * Creates a new client.
     *
     * @param rpcClient The RPC client to use.
     * @param rpcServer The RPC server to use.
     * @param publisher The publisher to use.
     * @param notifier The notifier to use.
     * @throws NullPointerException if any of the arguments are {@code null}.
     */
    public UClient(RpcClient rpcClient, RpcServer rpcServer, Publisher publisher, Notifier notifier) {
        this.rpcClient = Objects.requireNonNull(rpcClient);
        this.rpcServer = Objects.requireNonNull(rpcServer);
        this.publisher = Objects.requireNonNull(publisher);
        this.notifier = Objects.requireNonNull(notifier);
    }

    @Override
    public CompletionStage<Void> notify(int resourceId, UUri destination, CallOptions options, UPayload payload) {
        return notifier.notify(resourceId, destination, options, payload);
    }

    @Override
    public CompletionStage<Void> registerNotificationListener(UUri topic, UListener listener) {
        return notifier.registerNotificationListener(topic, listener);
    }

    @Override
    public CompletionStage<Void> unregisterNotificationListener(UUri topic, UListener listener) {
        return notifier.unregisterNotificationListener(topic, listener);
    }


    @Override
    public CompletionStage<Void> publish(int resourceId, CallOptions options, UPayload payload) {
        return publisher.publish(resourceId, options, payload);
    }


    @Override
    public CompletionStage<Void> registerRequestHandler(UUri originFilter, int resourceId, RequestHandler handler) {
        return rpcServer.registerRequestHandler(originFilter, resourceId, handler);
    }

    @Override
    public CompletionStage<Void> unregisterRequestHandler(UUri originFilter, int resourceId,
            RequestHandler handler) {
        return rpcServer.unregisterRequestHandler(originFilter, resourceId, handler);
    }


    @Override
    public CompletionStage<UPayload> invokeMethod(UUri methodUri, UPayload requestPayload, CallOptions options) {
        return rpcClient.invokeMethod(methodUri, requestPayload, options);
    }

    /**
     * Creates a new client for a transport implementation.
     *
     * @param transport The transport to use for sending and receiving messages.
     * @param uriProvider The helper to use for creating local resource URIs.
     * @return Returns a new instance of the RPC client
     * @throws NullPointerException if any of the arguments are {@code null}.
     */
    public static UClient create(UTransport transport, LocalUriProvider uriProvider) {
        Objects.requireNonNull(transport);
        Objects.requireNonNull(uriProvider);
        return new UClient(
            new InMemoryRpcClient(transport, uriProvider),
            new InMemoryRpcServer(transport, uriProvider),
            new SimplePublisher(transport, uriProvider),
            new SimpleNotifier(transport, uriProvider)
        );
    }
}
