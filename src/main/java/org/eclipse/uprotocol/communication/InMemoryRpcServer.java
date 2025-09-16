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
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CompletableFuture;

import org.eclipse.uprotocol.transport.LocalUriProvider;
import org.eclipse.uprotocol.transport.UListener;
import org.eclipse.uprotocol.transport.UTransport;
import org.eclipse.uprotocol.transport.builder.UMessageBuilder;
import org.eclipse.uprotocol.uri.factory.UriFactory;
import org.eclipse.uprotocol.uri.serializer.UriSerializer;
import org.eclipse.uprotocol.uri.validator.UriValidator;
import org.eclipse.uprotocol.v1.UAttributes;
import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UMessage;
import org.eclipse.uprotocol.v1.UMessageType;
import org.eclipse.uprotocol.v1.UStatus;
import org.eclipse.uprotocol.v1.UUri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The following is an example implementation of the {@link RpcServer} interface that
 * wraps the {@link UTransport} for implementing the server-side of the RPC pattern
 * to register handlers for processing RPC requests from clients. This implementation
 * uses an in-memory map to store the request handlers that needs to be invoked when the
 * request comes in from the client.
 * <p>
 * <em>NOTE:</em> Developers are not required to use these APIs, they can implement their own
 *                or directly use a {@link UTransport} to register listeners that handle 
 *                RPC requests and send RPC responses.
 */
// [impl->dsn~communication-layer-impl-default~1]
public class InMemoryRpcServer extends AbstractCommunicationLayerClient implements RpcServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryRpcServer.class);

    protected static final String REQUEST_HANDLER_ERROR_MESSAGE = "Failed to handle RPC request";

    // Map to store the request handlers so we can handle the right request on the server side
    private final Map<UUri, RequestHandler> mRequestsHandlers = new ConcurrentHashMap<>();

    // Generic listener to handle all RPC request messages
    private final UListener mRequestHandler = this::handleRequest;

    private Consumer<UMessage> unexpectedMessageHandler;
    private Consumer<Throwable> sendResponseErrorHandler;

    /**
     * Creates a new server for a transport.
     *
     * @param transport The transport to use for receiving RPC requests and
     *                  sending RPC responses.
     * @param uriProvider The URI provider to use for generating local resource URIs.
     * @throws NullPointerException if transport is {@code null}.
     */
    public InMemoryRpcServer (UTransport transport, LocalUriProvider uriProvider) {
        super(transport, uriProvider);
    }

    /**
     * Sets the handler to invoke when an unexpected message is received.
     *
     * @param handler The handler to invoke when an unexpected message is received.
     */
    void setUnexpectedMessageHandler(Consumer<UMessage> handler) {
        this.unexpectedMessageHandler = handler;
    }

    /**
     * Sets the handler to invoke when sending an RPC response fails.
     *
     * @param handler The handler to invoke when sending an RPC response fails.
     */
    void setSendErrorHandler(Consumer<Throwable> handler) {
        this.sendResponseErrorHandler = handler;
    }

    @Override
    public CompletionStage<Void> registerRequestHandler(UUri originFilter, int resourceId, RequestHandler handler) {
        Objects.requireNonNull(originFilter, "Origin filter must not be null");
        Objects.requireNonNull(handler, "Request handler must not be null");

        // create the method URI for where we want to register the listener
        final var method = UUri.newBuilder(getUriProvider().getSource())
            .setResourceId(resourceId)
            .build();
        if (!UriValidator.isRpcMethod(method)) {
            return CompletableFuture.failedFuture(new UStatusException(
                UCode.INVALID_ARGUMENT, "Resource ID must be an RPC method ID"));
        }

        synchronized (mRequestsHandlers) {
            if (mRequestsHandlers.containsKey(method)) {
                return CompletableFuture.failedFuture(new UStatusException(
                    UCode.ALREADY_EXISTS, "Handler already registered"));
            }
            return getTransport().registerListener(UriFactory.ANY, method, mRequestHandler)
                .whenComplete((ok, throwable) -> {
                    if (throwable != null) {
                        mRequestsHandlers.remove(method);
                    } else {
                        mRequestsHandlers.put(method, handler);
                    }
                });
        }
    }

    @Override
    public CompletionStage<Void> unregisterRequestHandler(
            UUri originFilter,
            int resourceId,
            RequestHandler handler) {
        Objects.requireNonNull(originFilter, "Origin filter must not be null");
        Objects.requireNonNull(handler, "Request handler must not be null");

        final var method = UUri.newBuilder(getUriProvider().getSource())
            .setResourceId(resourceId)
            .build();
        if (!UriValidator.isRpcMethod(method)) {
            return CompletableFuture.failedFuture(new UStatusException(
                UCode.INVALID_ARGUMENT, "Resource ID must be an RPC method ID"));
        }

        if (mRequestsHandlers.remove(method, handler)) {
            return getTransport().unregisterListener(UriFactory.ANY, method, mRequestHandler);
        } else {
            return CompletableFuture.failedFuture(new UStatusException(
                UCode.NOT_FOUND, "Handler not found"));
        }
    }

    /**
     * Generic incoming handler to process RPC requests from clients
     * @param request The request message from clients
     */
    private void handleRequest(UMessage request) {
        final UAttributes requestAttributes = request.getAttributes();

        // Only handle request messages, ignore all other messages like notifications
        if (requestAttributes.getType() != UMessageType.UMESSAGE_TYPE_REQUEST) {
            Optional.ofNullable(unexpectedMessageHandler).ifPresent(handler -> handler.accept(request));
            return;
        }

        // Check if the request is for one that we have registered a handler for, if not ignore it
        final var requestHandler = mRequestsHandlers.get(requestAttributes.getSink());
        if (requestHandler == null) {
            Optional.ofNullable(unexpectedMessageHandler).ifPresent(handler -> handler.accept(request));
            return;
        }

        UPayload responsePayload;
        final UMessageBuilder responseBuilder = UMessageBuilder.response(request.getAttributes());

        try {
            responsePayload = requestHandler.handleRequest(request);
        } catch (UStatusException e) {
            responseBuilder.withCommStatus(e.getStatus().getCode());
            responsePayload = UPayload.pack(e.getStatus());
        } catch (Exception e) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("""
                    RPC RequestHandler threw unexpected exception while processing RPC request \
                    [source: {}, sink: {}]: {}""",
                    UriSerializer.serialize(request.getAttributes().getSource()),
                    UriSerializer.serialize(request.getAttributes().getSink()),
                    e.getMessage());
            }

            final var status = UStatus.newBuilder()
                .setCode(UCode.INTERNAL)
                .setMessage(REQUEST_HANDLER_ERROR_MESSAGE)
                .build();
            responseBuilder.withCommStatus(status.getCode());
            responsePayload = UPayload.pack(status);
        }
        
        final var responseMessage = responseBuilder.build(responsePayload);
        getTransport().send(responseMessage)
            .whenComplete((ok, t) -> {
                if (t != null) {
                    Optional.ofNullable(sendResponseErrorHandler).ifPresent(handler -> handler.accept(t));
                }
            });
    }
}
