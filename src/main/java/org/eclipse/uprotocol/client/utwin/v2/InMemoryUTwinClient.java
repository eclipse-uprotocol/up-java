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

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.eclipse.uprotocol.communication.CallOptions;
import org.eclipse.uprotocol.communication.InMemoryRpcClient;
import org.eclipse.uprotocol.communication.RpcClient;
import org.eclipse.uprotocol.communication.RpcMapper;
import org.eclipse.uprotocol.communication.UPayload;
import org.eclipse.uprotocol.communication.UStatusException;
import org.eclipse.uprotocol.core.utwin.v2.GetLastMessagesRequest;
import org.eclipse.uprotocol.core.utwin.v2.GetLastMessagesResponse;
import org.eclipse.uprotocol.core.utwin.v2.UTwinProto;
import org.eclipse.uprotocol.transport.UTransport;
import org.eclipse.uprotocol.uri.factory.UriFactory;
import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UStatus;
import org.eclipse.uprotocol.v1.UUri;
import org.eclipse.uprotocol.v1.UUriBatch;

import com.google.protobuf.Descriptors.ServiceDescriptor;

/**
 * The uTwin client implementation using InMemory up-L2 communication layer implementation.
 */
public class InMemoryUTwinClient implements UTwinClient {
    private final RpcClient rpcClient;

    private static final ServiceDescriptor UTWIN = UTwinProto.getDescriptor().getServices().get(0);

    // TODO: The following items eventually need to be pulled from generated code
    private static final UUri GETLASTMESSAGE_METHOD = UriFactory.fromProto(UTWIN, 1);


    /**
     * Create a new instance of the uTwin client using the provided transport.
     * 
     * @param transport The transport to use for communication.
     */
    public InMemoryUTwinClient(UTransport transport) {
        this(new InMemoryRpcClient(transport));
    }


    /**
     * Create a new instance of the uTwin client passing in the RPCClient to use for communication.
     * 
     * @param rpcClient The RPC client to use for communication.
     */
    public InMemoryUTwinClient(RpcClient rpcClient) {
        this.rpcClient = rpcClient;
    }


    /**
     * Fetch the last messages for a batch of topics.
     * 
     * @param topics  {@link UUriBatch} batch of 1 or more topics to fetch the last messages for.
     * @param options The call options.
     * @return CompletionStage completes successfully with {@link GetLastMessagesResponse} if uTwin was able
     *         to fetch the topics or completes exceptionally with {@link UStatus} with the failure reason.
     *         such as {@code UCode.NOT_FOUND}, {@code UCode.PERMISSION_DENIED} etc...
     */
    @Override
    public CompletionStage<GetLastMessagesResponse> getLastMessages(UUriBatch topics, CallOptions options) {
        Objects.requireNonNull(topics, "topics must not be null");
        Objects.requireNonNull(options, "options must not be null");
        
        // Check if topics is empty
        if (topics.equals(UUriBatch.getDefaultInstance())) {
            return CompletableFuture.failedFuture(
                new UStatusException(UCode.INVALID_ARGUMENT, "topics must not be empty"));
        }

        GetLastMessagesRequest request = GetLastMessagesRequest.newBuilder().setTopics(topics).build();
        return RpcMapper.mapResponse(rpcClient.invokeMethod(
            GETLASTMESSAGE_METHOD, UPayload.pack(request), options), GetLastMessagesResponse.class);
    }   
}
