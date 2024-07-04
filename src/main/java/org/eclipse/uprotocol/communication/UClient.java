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
import org.eclipse.uprotocol.core.usubscription.v3.SubscriptionResponse;
import org.eclipse.uprotocol.transport.UListener;
import org.eclipse.uprotocol.transport.UTransport;
import org.eclipse.uprotocol.v1.UStatus;
import org.eclipse.uprotocol.v1.UUri;

/**
 * Default implementation of the communication layer that uses the {@link UTransport}.
 */
public class UClient implements RpcServer, Subscriber, Notifier, Publisher, RpcClient {
    
    // The transport to use for sending the RPC requests
    private final UTransport transport;

    private final InMemoryRpcServer rpcServer;
    private final SimplePublisher publisher;
    private final SimpleNotifier notifier;
    private final InMemoryRpcClient rpcClient;
    private final InMemorySubscriber subscriber;

    private UClient (UTransport transport) {
        this.transport = transport;

        rpcServer = new InMemoryRpcServer(transport);
        publisher = new SimplePublisher(transport);
        notifier = new SimpleNotifier(transport);
        rpcClient = new InMemoryRpcClient(transport);
        subscriber = new InMemorySubscriber(transport, rpcClient, notifier);
    }


    @Override
    public CompletionStage<SubscriptionResponse> subscribe(UUri topic, UListener listener, 
        CallOptions options, SubscriptionChangeHandler handler) {
        return subscriber.subscribe(topic, listener, options);
    }


    @Override
    public CompletionStage<UStatus> unsubscribe(UUri topic, UListener listener, CallOptions options) {
        return subscriber.unsubscribe(topic, listener, options);
    }
    
    @Override
    public CompletionStage<UStatus> unregisterListener(UUri topic, UListener listener) {
        return subscriber.unregisterListener(topic, listener);
    }

    @Override
    public CompletionStage<UStatus> notify(UUri topic, UUri destination, CallOptions options, UPayload payload) {
        return notifier.notify(topic, destination, options, payload);
    }

    @Override
    public CompletionStage<UStatus> registerNotificationListener(UUri topic, UListener listener) {
        Objects.requireNonNull(transport, UTransport.TRANSPORT_NULL_ERROR);
        return notifier.registerNotificationListener(topic, listener);
    }

    @Override
    public CompletionStage<UStatus> unregisterNotificationListener(UUri topic, UListener listener) {
        return notifier.unregisterNotificationListener(topic, listener);
    }


    @Override
    public CompletionStage<UStatus> publish(UUri topic, CallOptions options, UPayload payload) {
        return publisher.publish(topic, options, payload);
    }


    @Override
    public CompletionStage<UStatus> registerRequestHandler(UUri method, RequestHandler handler) {
        return rpcServer.registerRequestHandler(method, handler);
    }


    @Override
    public CompletionStage<UStatus> unregisterRequestHandler(UUri method, RequestHandler handler) {
        return rpcServer.unregisterRequestHandler(method, handler);
    }

 

    @Override
    public CompletionStage<UPayload> invokeMethod(UUri methodUri, UPayload requestPayload, CallOptions options) {
        return rpcClient.invokeMethod(methodUri, requestPayload, options);
    }
    

    /**
     * Create a new instance of UPClient
     * @param transport The transport to use for sending the RPC requests
     * @return Returns a new instance of the RPC client
     */
    public static UClient create(UTransport transport) {
        Objects.requireNonNull(transport, UTransport.TRANSPORT_NULL_ERROR);
        return new UClient(transport);
    }


    public void close() {
        rpcClient.close();
        subscriber.close();
        transport.close();
    }
}
