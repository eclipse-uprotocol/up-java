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

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.eclipse.uprotocol.transport.LocalUriProvider;
import org.eclipse.uprotocol.transport.UListener;
import org.eclipse.uprotocol.transport.UTransport;
import org.eclipse.uprotocol.transport.builder.UMessageBuilder;
import org.eclipse.uprotocol.uri.factory.UriFactory;
import org.eclipse.uprotocol.v1.UAttributes;
import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UMessage;
import org.eclipse.uprotocol.v1.UMessageType;
import org.eclipse.uprotocol.v1.UStatus;
import org.eclipse.uprotocol.v1.UUID;
import org.eclipse.uprotocol.v1.UUri;

/**
 * The following is an example implementation of the {@link RpcClient} interface that
 * wraps the {@link UTransport} for implementing the RPC pattern to send 
 * RPC requests and receive RPC responses. This implementation uses an in-memory
 * map to store the futures that needs to be completed when the response comes in from the server.
 * 
 * *NOTE:* Developers are not required to use these APIs, they can implement their own
 *         or directly use the {@link UTransport} to send RPC requests and register listeners that
 *         handle the RPC responses.
 */
// [impl->dsn~communication-layer-impl-default~1]
public class InMemoryRpcClient extends AbstractCommunicationLayerClient implements RpcClient {
    // Map to store the futures that needs to be completed when the response comes in
    private final Map<UUID, CompletableFuture<UMessage>> mRequests = new ConcurrentHashMap<>();

    // Generic listener to handle all RPC response messages
    private final UListener mResponseHandler = this::handleResponse;

    private Consumer<UMessage> unexpectedMessageHandler;

    /**
     * Creates a client for a transport.
     *
     * @param transport The transport to use for sending the RPC requests.
     * @param uriProvider The helper for creating URIs that represent local resources.
     * @throws NullPointerException if any of the arguments are {@code null}.
     * @throws CompletionException if registration of the response listener fails.
     */
    public InMemoryRpcClient(UTransport transport, LocalUriProvider uriProvider) {
        super(transport, uriProvider);

        getTransport().registerListener(
                UriFactory.ANY,
                Optional.of(getUriProvider().getSource()),
                mResponseHandler)
            .toCompletableFuture().join();
    }

    /**
     * Sets a handler to be invoked for unexpected inbound messages.
     *
     * @param unexpectedResponseHandler A handler to invoke for incoming messages that cannot
     * be processed, either because they are no RPC response messages or because they contain
     * an unknown request ID.
     */
    void setUnexpectedMessageHandler(Consumer<UMessage> handler) {
        this.unexpectedMessageHandler = handler;
    }

    @Override
    public CompletionStage<UPayload> invokeMethod(UUri methodUri, UPayload requestPayload, CallOptions options) {
        Objects.requireNonNull(methodUri, "Method URI cannot be null");
        Objects.requireNonNull(requestPayload, "Request payload cannot be null");
        Objects.requireNonNull(options, "Call options cannot be null");

        UMessageBuilder builder = UMessageBuilder.request(getUriProvider().getSource(), methodUri, options.timeout());
        Optional.ofNullable(options.priority()).ifPresent(builder::withPriority);
        Optional.ofNullable(options.token())
            .filter(s -> !s.isBlank())
            .ifPresent(builder::withToken);

        // Build the request message
        final UMessage request = builder.build(requestPayload);
        
        // Create the response future and store it in mRequests
        CompletableFuture<UMessage> responseFuture = new CompletableFuture<UMessage>()
                .orTimeout(request.getAttributes().getTtl(), TimeUnit.MILLISECONDS)
                .exceptionally(e -> {
                    throw new UStatusException(UCode.DEADLINE_EXCEEDED, "Request timed out");
                })
                .whenComplete((responseMessage, exception) -> mRequests.remove(request.getAttributes().getId()));

        mRequests.compute(request.getAttributes().getId(), (requestId, currentRequest) -> {
            return responseFuture;
        });

        // Send the request
        return getTransport().send(request)
            .thenCompose(s -> responseFuture)
            .thenApply(responseMessage -> UPayload.pack(
                responseMessage.getPayload(),
                responseMessage.getAttributes().getPayloadFormat())
            );
    }

    /**
     * Close the RPC client and clean up any resources.
     */
    public void close() {
        mRequests.clear();
        getTransport().unregisterListener(
            UriFactory.ANY,
            Optional.of(getUriProvider().getSource()),
            mResponseHandler);
    }

    private void handleResponse(UMessage message) {
        final UAttributes responseAttributes = message.getAttributes();
        // Only handle responses messages
        if (responseAttributes.getType() != UMessageType.UMESSAGE_TYPE_RESPONSE) {
            Optional.ofNullable(unexpectedMessageHandler).ifPresent(handler -> handler.accept(message));
            return;
        }

        // Check if the response is for a request we made, if not then ignore it
        final CompletableFuture<UMessage> responseFuture = mRequests.remove(responseAttributes.getReqid());
        if (responseFuture == null) {
            Optional.ofNullable(unexpectedMessageHandler).ifPresent(handler -> handler.accept(message));
            return;
        }

        // Check if the response has a commstatus and if it is not OK then complete the future with an exception
        if (responseAttributes.hasCommstatus() && responseAttributes.getCommstatus() != UCode.OK) {
            // first, try to extract error details from payload
            final var exception = UPayload.unpack(message, UStatus.class)
                .map(UStatusException::new)
                // fall back to a generic error based on commstatus
                .orElseGet(() -> new UStatusException(responseAttributes.getCommstatus(), "Communication error"));
            responseFuture.completeExceptionally(exception);
        } else {
            responseFuture.complete(message); 
        }
    }
}
