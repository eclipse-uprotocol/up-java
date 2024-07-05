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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CompletableFuture;

import org.eclipse.uprotocol.transport.UListener;
import org.eclipse.uprotocol.transport.UTransport;
import org.eclipse.uprotocol.transport.builder.UMessageBuilder;
import org.eclipse.uprotocol.uri.factory.UriFactory;
import org.eclipse.uprotocol.v1.UAttributes;
import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UMessage;
import org.eclipse.uprotocol.v1.UMessageType;
import org.eclipse.uprotocol.v1.UStatus;
import org.eclipse.uprotocol.v1.UUri;


/**
 * The following is an example implementation of the {@link RpcServer} interface that
 * wraps the {@link UTransport} for implementing the server-side of the RPC pattern
 * to register handlers for processing RPC requests from clients. This implementation
 * uses an in-memory map to store the request handlers that needs to be invoked when the
 * request comes in from the client.
 * 
 * *NOTE:* Developers are not required to use these APIs, they can implement their own
 *         or directly use the {@link UTransport} to register listeners that handle 
 *         RPC requests and send RPC responses.
 */
public class InMemoryRpcServer implements RpcServer {
    // The transport to use for sending the RPC requests
    private final UTransport transport;

    // Map to store the request handlers so we can handle the right request on the server side
    private final ConcurrentHashMap<UUri, RequestHandler> mRequestsHandlers = new ConcurrentHashMap<>();

    // Generic listener to handle all RPC request messages
    private final UListener mRequestHandler = this::handleRequests;


    /**
     * Constructor for the DefaultRpcServer.
     * 
     * @param transport the transport to use for sending the RPC requests
     */
    public InMemoryRpcServer (UTransport transport) {
        Objects.requireNonNull(transport, UTransport.TRANSPORT_NULL_ERROR);
        this.transport = transport;
    }


    /**
     * Register a handler that will be invoked when when requests come in from clients for the given method.
     *
     * <p>Note: Only one handler is allowed to be registered per method URI.
     *
     * @param method Uri for the method to register the listener for.
     * @param handler The handler that will process the request for the client.
     * @return Returns the status of registering the RpcListener.
     */
    @Override
    public CompletionStage<UStatus> registerRequestHandler(UUri method, RequestHandler handler) {
        if (method == null || handler == null) {
            return CompletableFuture.completedFuture(
                UStatus.newBuilder()
                    .setCode(UCode.INVALID_ARGUMENT)
                    .setMessage("Method URI or handler missing")
                    .build());
        }
        
        // Ensure the method URI matches the transport source URI 
        if (!method.getAuthorityName().equals(transport.getSource().getAuthorityName()) ||
            method.getUeId() != transport.getSource().getUeId() ||
            method.getUeVersionMajor() != transport.getSource().getUeVersionMajor()) {
            return CompletableFuture.completedFuture(
                UStatus.newBuilder()
                    .setCode(UCode.INVALID_ARGUMENT)
                    .setMessage("Method URI does not match the transport source URI")
                    .build());
        }
        try {
            mRequestsHandlers.compute(method, (key, currentHandler) -> {
                if (currentHandler != null) {
                    throw new UStatusException(UCode.ALREADY_EXISTS, "Handler already registered");
                }
                
                UStatus status = transport.registerListener(UriFactory.ANY, method, mRequestHandler)
                    .toCompletableFuture().join();
                if (status.getCode() != UCode.OK) {
                    throw new UStatusException(status);
                }
                return handler;
            });
            return CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build());
        } catch (UStatusException e) {
            return CompletableFuture.completedFuture(e.getStatus());
        }
    }


    /**
     * Unregister a handler that will be invoked when when requests come in from clients for the given method.
     * 
     * @param method Resolved UUri for where the listener was registered to receive messages from.
     * @param handler The handler for processing requests
     * @return Returns status of registering the RpcListener.
     */
    @Override
    public CompletionStage<UStatus> unregisterRequestHandler(UUri method, RequestHandler handler) {
        if (method == null || handler == null) {
            return CompletableFuture.completedFuture(
                UStatus.newBuilder()
                    .setCode(UCode.INVALID_ARGUMENT)
                    .setMessage("Method URI or handler missing")
                    .build());
        }
    
        // Ensure the method URI matches the transport source URI 
        if (!method.getAuthorityName().equals(transport.getSource().getAuthorityName()) ||
            method.getUeId() != transport.getSource().getUeId() ||
            method.getUeVersionMajor() != transport.getSource().getUeVersionMajor()) {
            return CompletableFuture.completedFuture(
                UStatus.newBuilder()
                    .setCode(UCode.INVALID_ARGUMENT)
                    .setMessage("Method URI does not match the transport source URI")
                    .build());
        }

        if (mRequestsHandlers.remove(method, handler)) {
            return transport.unregisterListener(UriFactory.ANY, method, mRequestHandler);
        }

        return CompletableFuture.completedFuture(
            UStatus.newBuilder().setCode(UCode.NOT_FOUND).setMessage("Handler not found").build());
    }


    /**
     * Generic incoming handler to process RPC requests from clients
     * @param request The request message from clients
     */
    private void handleRequests(UMessage request) {
        // Only handle request messages, ignore all other messages like notifications
        if (request.getAttributes().getType() != UMessageType.UMESSAGE_TYPE_REQUEST) {
            return;
        }
    
        final UAttributes requestAttributes = request.getAttributes();
        
        // Check if the request is for one that we have registered a handler for, if not ignore it
        final RequestHandler handler = mRequestsHandlers.get(requestAttributes.getSink());
        if (handler == null) {
            return;
        }

        UPayload responsePayload;
        UMessageBuilder responseBuilder = UMessageBuilder.response(request.getAttributes());

        try {
            responsePayload = handler.handleRequest(request);
        } catch (Exception e) {
            UCode code = UCode.INTERNAL;
            responsePayload = null;
            if (e instanceof UStatusException statusException) {
                code = statusException.getStatus().getCode();
            } else {
                code = UCode.INTERNAL;
            }
            responseBuilder.withCommStatus(code);
        }
        
        // TODO: Handle error sending the response
        transport.send(responseBuilder.build(responsePayload));
    }
}
