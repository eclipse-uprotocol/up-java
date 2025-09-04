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
package org.eclipse.uprotocol.client.usubscription.v3;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import org.eclipse.uprotocol.Uoptions;
import org.eclipse.uprotocol.communication.CallOptions;
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
import org.eclipse.uprotocol.core.usubscription.v3.USubscriptionProto;
import org.eclipse.uprotocol.core.usubscription.v3.UnsubscribeRequest;
import org.eclipse.uprotocol.core.usubscription.v3.UnsubscribeResponse;
import org.eclipse.uprotocol.v1.UUri;

import com.google.protobuf.Message;
import com.google.protobuf.Descriptors.ServiceDescriptor;

/**
 * A USubscription client implementation for invoking operations of a USubscription service.
 * <p>
 * The client requires an {@link RpcClient} for performing the remote procedure calls.
 */
public class RpcClientBasedUSubscriptionClient implements USubscriptionClient {
    private static final ServiceDescriptor USUBSCRIPTION_DEFAULT_DESCRIPTOR = USubscriptionProto.getDescriptor()
        .getServices().get(0);

    // TODO: The following items eventually need to be pulled from generated code
    private static final int SUBSCRIBE_METHOD_ID = 0x0001;
    private static final int UNSUBSCRIBE_METHOD_ID = 0x0002;
    private static final int FETCH_SUBSCRIPTIONS_METHOD_ID = 0x0003;
    private static final int REGISTER_NOTIFICATIONS_METHOD_ID = 0x0006;
    private static final int UNREGISTER_NOTIFICATIONS_METHOD_ID = 0x0007;
    private static final int FETCH_SUBSCRIBERS_METHOD_ID = 0x0008;
    private static final int NOTIFICATION_TOPIC_ID = 0x8000;

    private final RpcClient rpcClient;
    private final UUri serviceUri;
    private final CallOptions callOptions;

    /**
     * Creates a new client for interacting with the default instance of
     * the local uSubscription service.
     * 
     * @param rpcClient The client to use for sending the RPC requests.
     * @param callOptions The options to use for the RPC calls.
     * @throws NullPointerException if rpcClient or callOptions are {@code null}.
     */
    public RpcClientBasedUSubscriptionClient (RpcClient rpcClient, CallOptions callOptions) {
        this(rpcClient, callOptions, 0x0000, null);
    }

    /**
     * Creates a new client for interacting with a given uSubscription
     * service instance.
     * 
     * @param rpcClient The client to use for sending the RPC requests.
     * @param callOptions The options to use for the RPC calls.
     * @param subscriptionServiceInstanceId The instance of the subscription service to invoke,
     * {@code 0x000} to use the default instance.
     * @param subscriptionServiceAuthority The authority that the subscription service runs on,
     * or {@code null} if the instance runs on the local authority.
     * @throws NullPointerException if rpcClient or callOptions are {@code null}.
     * @throws IllegalArgumentException if the instance ID is invalid.
     */
    public RpcClientBasedUSubscriptionClient (
            RpcClient rpcClient,
            CallOptions callOptions,
            int subscriptionServiceInstanceId,
            String subscriptionServiceAuthority) {
        Objects.requireNonNull(rpcClient, "RpcClient missing");
        Objects.requireNonNull(callOptions, "CallOptions missing");
        if (subscriptionServiceInstanceId < 0 || subscriptionServiceInstanceId >= 0xFFFF) {
            throw new IllegalArgumentException("Invalid subscription service instance ID");
        }
        this.rpcClient = rpcClient;
        this.callOptions = callOptions;
        this.serviceUri = getUSubscriptionServiceUri(subscriptionServiceInstanceId, subscriptionServiceAuthority);
    }

    private static UUri getUSubscriptionServiceUri(int instanceId, String authority) {
        final var options = USUBSCRIPTION_DEFAULT_DESCRIPTOR.getOptions();
        var builder = UUri.newBuilder();
        Optional.ofNullable(authority).ifPresent(builder::setAuthorityName);
        return builder
            .setUeId((instanceId << 16) | options.getExtension(Uoptions.serviceId))
            .setUeVersionMajor(options.getExtension(Uoptions.serviceVersionMajor))
            .build();
    }

    private <T extends Message> CompletionStage<T> invokeMethod(
            int methodId,
            UPayload request,
            CallOptions options,
            Class<T> responseType) {
        Objects.requireNonNull(request, "Request missing");
        Objects.requireNonNull(options, "CallOptions missing");
        Objects.requireNonNull(responseType, "Response type missing");

        final var method = UUri.newBuilder(serviceUri).setResourceId(methodId).build();
        return rpcClient.invokeMethod(method, request, options)
                .thenApply(responsePayload -> UPayload.unpackOrDefaultInstance(responsePayload, responseType));
    }

    @Override
    public UUri getSubscriptionServiceNotificationTopic() {
        return UUri.newBuilder(serviceUri)
            .setResourceId(NOTIFICATION_TOPIC_ID)
            .build();
    }

    @Override
    public CompletionStage<SubscriptionResponse> subscribe(SubscriptionRequest request) {
        Objects.requireNonNull(request, "Subscribe request missing");
        return invokeMethod(
            SUBSCRIBE_METHOD_ID,
            UPayload.pack(request),
            callOptions,
            SubscriptionResponse.class);
    }

    @Override
    public CompletionStage<UnsubscribeResponse> unsubscribe(UnsubscribeRequest request) {
        Objects.requireNonNull(request, "Unsubscribe request missing");
        return invokeMethod(
            UNSUBSCRIBE_METHOD_ID,
            UPayload.pack(request),
            callOptions,
            UnsubscribeResponse.class);
    }

    @Override
    public CompletionStage<FetchSubscribersResponse> fetchSubscribers(FetchSubscribersRequest request) {
        Objects.requireNonNull(request, "Request missing");

        return invokeMethod(
            FETCH_SUBSCRIBERS_METHOD_ID,
            UPayload.pack(request),
            callOptions,
            FetchSubscribersResponse.class);
    }

    @Override
    public CompletionStage<FetchSubscriptionsResponse> fetchSubscriptions(FetchSubscriptionsRequest request) {
        Objects.requireNonNull(request, "Request missing");

        return invokeMethod(
            FETCH_SUBSCRIPTIONS_METHOD_ID,
            UPayload.pack(request),
            callOptions,
            FetchSubscriptionsResponse.class);
    }

    @Override
    public CompletionStage<NotificationsResponse> registerForNotifications(NotificationsRequest request) {
        Objects.requireNonNull(request, "Request missing");
        return invokeMethod(
            REGISTER_NOTIFICATIONS_METHOD_ID,
            UPayload.pack(request),
            callOptions,
            NotificationsResponse.class);
    }

    @Override
    public CompletionStage<NotificationsResponse> unregisterForNotifications(NotificationsRequest request) {
        Objects.requireNonNull(request, "Request missing");
        return invokeMethod(
            UNREGISTER_NOTIFICATIONS_METHOD_ID,
            UPayload.pack(request),
            callOptions,
            NotificationsResponse.class);
    }
}
