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

import org.eclipse.uprotocol.core.usubscription.v3.SubscriberInfo;
import org.eclipse.uprotocol.core.usubscription.v3.SubscriptionRequest;
import org.eclipse.uprotocol.core.usubscription.v3.SubscriptionResponse;
import org.eclipse.uprotocol.core.usubscription.v3.USubscriptionProto;
import org.eclipse.uprotocol.core.usubscription.v3.UnsubscribeRequest;
import org.eclipse.uprotocol.core.usubscription.v3.UnsubscribeResponse;
import org.eclipse.uprotocol.transport.UListener;
import org.eclipse.uprotocol.transport.UTransport;
import org.eclipse.uprotocol.uri.factory.UriFactory;
import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UStatus;
import org.eclipse.uprotocol.v1.UUri;

public class DefaultSubscriber implements Subscriber {

    private final UTransport transport;
    private final RpcClient rpcClient;
    private static final int METHOD_SUBSCRIBE = 1;  // TODO: Fetch this from proto generated code
    private static final int METHOD_UNSUBSCRIBE = 2; // TODO: Fetch this from proto generated code


    /**
     * Constructor for the DefaultSubscriber.
     * 
     * @param transport the transport to use for sending the notifications
     * @param rpcClient the rpc client to use for sending the RPC requests
     */
    public DefaultSubscriber (UTransport transport, RpcClient rpcClient) {
        Objects.requireNonNull(transport, UTransport.TRANSPORT_NULL_ERROR);
        Objects.requireNonNull(rpcClient, "RpcClient missing");
        this.transport = transport;
        this.rpcClient = rpcClient;
    }


    /**
     * Subscribe to a given topic. <br>
     * 
     * The API will return a {@link CompletionStage} with the response {@link SubscriptionResponse} or exception
     * with the failure if the subscription was not successful. The API will also register the listener to be
     * called when messages are received.
     * 
     * @param topic The topic to subscribe to.
     * @param listener The listener to be called when a message is received on the topic.
     * @param options The call options for the subscription.
     * @return Returns the CompletionStage with the response UMessage or exception with the failure
     * reason as {@link UStatus}.
     */
    @Override
    public CompletionStage<SubscriptionResponse> subscribe(UUri topic, UListener listener, CallOptions options) {
        Objects.requireNonNull(topic, "Subscribe topic missing");
        Objects.requireNonNull(listener, "Request listener missing");

        final UUri subscribe = UriFactory.fromProto(
            USubscriptionProto.getDescriptor().getServices().get(0), METHOD_SUBSCRIBE); 
        
        final SubscriptionRequest request = SubscriptionRequest.newBuilder()
            .setTopic(topic)
            .setSubscriber(SubscriberInfo.newBuilder().setUri(transport.getSource()).build())
            .build();
        
        return RpcMapper.mapResponse(rpcClient.invokeMethod(
                subscribe, UPayload.pack(request), options), SubscriptionResponse.class)
            .toCompletableFuture().whenComplete((response, exception) -> {
                transport.registerListener(topic, listener);
            });
    }


    /**
     * Unsubscribe to a given topic. <br>
     * 
     * The subscriber no longer wishes to be subscribed to said topic so we issue a unsubscribe
     * request to the USubscription service.
     * 
     * @param topic The topic to unsubscribe to.
     * @param listener The listener to be called when a message is received on the topic.
     * @param options The call options for the subscription.
     * @return Returns {@link UStatus} with the result from the unsubscribe request.
     */
    @Override
    public UStatus unsubscribe(UUri topic, UListener listener, CallOptions options) {
        Objects.requireNonNull(topic, "Unsubscribe topic missing");
        Objects.requireNonNull(listener, "listener missing");

        final UUri unsubscribe = UriFactory.fromProto(
            USubscriptionProto.getDescriptor().getServices().get(0), METHOD_UNSUBSCRIBE);
        final UnsubscribeRequest unsubscribeRequest = UnsubscribeRequest.newBuilder().setTopic(topic).build();
        
        RpcResult<UnsubscribeResponse> response = RpcMapper.mapResponseToResult(rpcClient.invokeMethod(
            unsubscribe, UPayload.pack(unsubscribeRequest), options), UnsubscribeResponse.class)
            .toCompletableFuture().join();
        return response.isSuccess() ? UStatus.newBuilder().setCode(UCode.OK).build() : response.failureValue();
    }


    /**
     * Unregister a listener from a topic. <br>
     * 
     * This method will only unregister the listener for a given subscription thus allowing a uE to stay
     * subscribed even if the listener is removed.
     * 
     * @param topic The topic to subscribe to.
     * @param listener The listener to be called when a message is received on the topic.
     * @return Returns {@link UStatus} with the status of the listener unregister request.
     */
    @Override
    public UStatus unregisterListener(UUri topic, UListener listener) {
        Objects.requireNonNull(topic, "Unsubscribe topic missing");
        Objects.requireNonNull(listener, "Request listener missing");
        return transport.unregisterListener(topic, listener);
    }
}
