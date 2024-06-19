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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.CompletionException;

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
public class InMemoryRpcClient implements RpcClient {
    // The transport to use for sending the RPC requests
    private final UTransport transport;

    // Map to store the futures that needs to be completed when the response comes in
    private final ConcurrentHashMap<UUID, CompletableFuture<UMessage>> mRequests = new ConcurrentHashMap<>();

    // Generic listener to handle all RPC response messages
    private final UListener mResponseHandler = this::handleResponses;

    
    /**
     * Constructor for the DefaultRpcClient.
     * 
     * @param transport the transport to use for sending the RPC requests
     */
    public InMemoryRpcClient (UTransport transport) {
        Objects.requireNonNull(transport, UTransport.TRANSPORT_NULL_ERROR);
        this.transport = transport;
   
        transport.registerListener(UriFactory.ANY, 
            transport.getSource(), mResponseHandler).toCompletableFuture().join();
    }


    /**
     * Invoke a method (send an RPC request) and receive the response 
     * (the returned {@link CompletionStage} {@link UPayload}. <br>
     * 
     * @param methodUri The method URI to be invoked.
     * @param requestPayload The request message to be sent to the server.
     * @param options RPC method invocation call options, see {@link CallOptions}
     * @return Returns the CompletionStage with the response payload or exception with the failure
     *         reason as {@link UStatus}.
     */
    @Override
    public CompletionStage<UPayload> invokeMethod(UUri methodUri, UPayload requestPayload, CallOptions options) {
        options = Objects.requireNonNullElse(options, CallOptions.DEFAULT);
        UMessageBuilder builder = UMessageBuilder.request(transport.getSource(), methodUri, options.timeout());
        UMessage request;
        
        try {
            if (!options.token().isBlank()) {
                builder.withToken(options.token());
            }
            // Build a request uMessage
            request = builder.build(requestPayload);
            
            // Create the response future and store it in mRequests
            CompletableFuture<UMessage> responseFuture = new CompletableFuture<UMessage>()
                    .orTimeout(request.getAttributes().getTtl(), TimeUnit.MILLISECONDS)
                    .handle((responseMessage, exception) -> {
                        if (exception != null) {
                            if (exception instanceof CompletionException) exception = exception.getCause();
                            if (exception instanceof TimeoutException) {
                                throw new UStatusException(UCode.DEADLINE_EXCEEDED, "Request timed out");
                            } else if (exception instanceof UStatusException) {
                                throw new UStatusException(((UStatusException) exception).getStatus());
                            } else {
                                throw new UStatusException(UCode.UNKNOWN, exception.getMessage());
                            }
                        }
                        return responseMessage;
                    });

            responseFuture.whenComplete(
                    (responseMessage, exception) -> mRequests.remove(request.getAttributes().getId()));

            mRequests.compute(request.getAttributes().getId(), (requestId, currentRequest) -> {
                if (currentRequest != null)
                    throw new UStatusException(UCode.ALREADY_EXISTS, "Duplicated request found");
                return responseFuture;
            });

            // Send the request
            CompletionStage<UStatus> status = transport.send(request);

            return status.thenApply(s -> {
                if (s.getCode() != UCode.OK) throw new UStatusException(s);
                return s;
            }).thenCompose(s -> responseFuture.thenApply(responseMessage ->
                    UPayload.pack(responseMessage.getPayload(), responseMessage.getAttributes().getPayloadFormat())
            ));

        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    public void close() {
        mRequests.clear();
        transport.unregisterListener(UriFactory.ANY, transport.getSource(), mResponseHandler);
    }

    /**
     * Handle the responses coming back from the server
     * @param response The response message from the server
     */
    private void handleResponses(UMessage response) {
        // Only handle responses messages, ignore all other messages like notifications
        if (response.getAttributes().getType() != UMessageType.UMESSAGE_TYPE_RESPONSE) {
            return;
        }
        
        final UAttributes responseAttributes = response.getAttributes();
        
        // Check if the response is for a request we made, if not then ignore it
        final CompletableFuture<UMessage> responseFuture = mRequests.remove(responseAttributes.getReqid());
        if (responseFuture == null) {
            return;
        }

        // Check if the response has a commstatus and if it is not OK then complete the future with an exception
        if (responseAttributes.hasCommstatus()) {
            final UCode code = responseAttributes.getCommstatus();
            responseFuture.completeExceptionally(
                new UStatusException(code, "Communication error [" + code + "]"));
            return;
        }
        responseFuture.complete(response); 
    }
}
