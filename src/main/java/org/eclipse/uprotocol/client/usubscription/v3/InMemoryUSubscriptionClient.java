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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.eclipse.uprotocol.communication.CallOptions;
import org.eclipse.uprotocol.communication.InMemoryRpcClient;
import org.eclipse.uprotocol.communication.Notifier;
import org.eclipse.uprotocol.communication.RpcClient;
import org.eclipse.uprotocol.communication.RpcMapper;
import org.eclipse.uprotocol.communication.SimpleNotifier;
import org.eclipse.uprotocol.communication.UPayload;
import org.eclipse.uprotocol.communication.UStatusException;
import org.eclipse.uprotocol.core.usubscription.v3.FetchSubscribersRequest;
import org.eclipse.uprotocol.core.usubscription.v3.FetchSubscribersResponse;
import org.eclipse.uprotocol.core.usubscription.v3.FetchSubscriptionsRequest;
import org.eclipse.uprotocol.core.usubscription.v3.FetchSubscriptionsResponse;
import org.eclipse.uprotocol.core.usubscription.v3.NotificationsRequest;
import org.eclipse.uprotocol.core.usubscription.v3.NotificationsResponse;
import org.eclipse.uprotocol.core.usubscription.v3.SubscribeAttributes;
import org.eclipse.uprotocol.core.usubscription.v3.SubscriptionRequest;
import org.eclipse.uprotocol.core.usubscription.v3.SubscriptionResponse;
import org.eclipse.uprotocol.core.usubscription.v3.SubscriptionStatus;
import org.eclipse.uprotocol.core.usubscription.v3.USubscriptionProto;
import org.eclipse.uprotocol.core.usubscription.v3.UnsubscribeRequest;
import org.eclipse.uprotocol.core.usubscription.v3.UnsubscribeResponse;
import org.eclipse.uprotocol.core.usubscription.v3.Update;
import org.eclipse.uprotocol.transport.UListener;
import org.eclipse.uprotocol.transport.UTransport;
import org.eclipse.uprotocol.uri.factory.UriFactory;
import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UMessage;
import org.eclipse.uprotocol.v1.UMessageType;
import org.eclipse.uprotocol.v1.UStatus;
import org.eclipse.uprotocol.v1.UUri;

import com.google.protobuf.Descriptors.ServiceDescriptor;

/**
 * Implementation of USubscriptionClient that caches state information within the object 
 * and used for single tenant applications (ex. in-vehicle). The implementation uses {@link InMemoryRpcClient}
 * that also stores RPC corelation information within the objects
 */
public class InMemoryUSubscriptionClient implements USubscriptionClient {
    private final UTransport transport;
    private final RpcClient rpcClient;
    private final Notifier notifier;

    private static final ServiceDescriptor USUBSCRIPTION = USubscriptionProto.getDescriptor().getServices().get(0);

    // TODO: The following items eventually need to be pulled from generated code
    private static final UUri SUBSCRIBE_METHOD = UriFactory.fromProto(USUBSCRIPTION, 1);
    private static final UUri UNSUBSCRIBE_METHOD = UriFactory.fromProto(USUBSCRIPTION, 2);
    private static final UUri FETCH_SUBSCRIBERS_METHOD = UriFactory.fromProto(USUBSCRIPTION, 8);
    private static final UUri FETCH_SUBSCRIPTIONS_METHOD = UriFactory.fromProto(USUBSCRIPTION, 3);
    private static final UUri REGISTER_NOTIFICATIONS_METHOD = UriFactory.fromProto(USUBSCRIPTION, 6);
    private static final UUri UNREGISTER_NOTIFICATIONS_METHOD = UriFactory.fromProto(USUBSCRIPTION, 7);

    private static final UUri NOTIFICATION_TOPIC = UriFactory.fromProto(USUBSCRIPTION, 0x8000);


    // Map to store subscription change notification handlers
    private final ConcurrentHashMap<UUri, SubscriptionChangeHandler> mHandlers = new ConcurrentHashMap<>();

    // transport Notification listener that will process subscription change notifications
    private final UListener mNotificationListener = this::handleNotifications;


    /**
     * Creates a new USubscription client passing {@link UTransport} and {@link CallOptions}
     * used to provide additional options for the RPC requests to uSubscription service.
     * 
     * @param transport the transport to use for sending the notifications
     */
    public InMemoryUSubscriptionClient (UTransport transport) {
        this(transport, new InMemoryRpcClient(transport), new SimpleNotifier(transport));
    }


    /**
     * Creates a new USubscription client passing {@link UTransport}, {@link CallOptions},
     * and an implementation of {@link RpcClient} and {@link Notifier}.
     * 
     * @param transport the transport to use for sending the notifications
     * @param rpcClient the rpc client to use for sending the RPC requests
     * @param notifier the notifier to use for registering the notification listener
     */
    public InMemoryUSubscriptionClient (UTransport transport, RpcClient rpcClient, Notifier notifier) {
        Objects.requireNonNull(transport, UTransport.TRANSPORT_NULL_ERROR);
        Objects.requireNonNull(rpcClient, "RpcClient missing");
        Objects.requireNonNull(notifier, "Notifier missing");
        this.transport = transport;
        this.rpcClient = rpcClient;
        this.notifier = notifier;

        // Register the notification listener to receive subscription change notifications
        notifier.registerNotificationListener(NOTIFICATION_TOPIC, mNotificationListener).toCompletableFuture().join();
    }


    /**
     * Subscribes to a given topic.
     * 
     * The API will return a {@link CompletionStage} with the response {@link SubscriptionResponse} or exception
     * with the failure if the subscription was not successful. The optional passed {@link SubscriptionChangeHandler}
     * is used to receive notifications of changes to the subscription status like a transition from
     * {@link SubscriptionStatus.State#SUBSCRIBE_PENDING} to {@link SubscriptionStatus.State#SUBSCRIBED} that
     * occurs when we subscribe to remote topics that the device we are on has not yet a subscriber that has
     * subscribed to said topic. 
     * 
     * @param topic The topic to subscribe to.
     * @param listener The listener to be called when messages are received.
     * @param options The {@link CallOptions} to be used for the subscription.
     * @param handler {@link SubscriptionChangeHandler} to handle changes to subscription states.
     * @return Returns the CompletionStage with {@link SubscriptionResponse} or exception with the failure
     * reason as {@link UStatus}. {@link UCode#ALREADY_EXISTS} will be returned if you call this API multiple
     * times passing a different handler. 
     */
    @Override
    public CompletionStage<SubscriptionResponse> subscribe(
        UUri topic,
        UListener listener,
        CallOptions options,
        SubscriptionChangeHandler handler) {
        Objects.requireNonNull(topic, "Subscribe topic missing");
        Objects.requireNonNull(listener, "Request listener missing");
        Objects.requireNonNull(options, "CallOptions missing");

        final SubscriptionRequest request = SubscriptionRequest.newBuilder()
            .setTopic(topic)
            .build();
        
        // Send the subscription request and handle the response
        return RpcMapper.mapResponse(rpcClient.invokeMethod(
                SUBSCRIBE_METHOD, UPayload.pack(request), options), SubscriptionResponse.class)
            
            // Then register the listener to be called when messages are received
            .thenCompose(response -> {
                if ( response.getStatus().getState() == SubscriptionStatus.State.SUBSCRIBED ||
                    response.getStatus().getState() == SubscriptionStatus.State.SUBSCRIBE_PENDING) {
                    // When registering the listener fails, we have end up in a situation where we
                    // have successfully (logically) subscribed to the topic via the USubscriptio service
                    // but we have not been able to register the listener with the local transport.
                    // This means that events might start getting forwarded to the local authority which
                    // are not being consumed. Apart from this inefficiency, this does not pose a real
                    // problem and since we return a failed future, the client might be inclined to try
                    // again and (eventually) succeed in registering the listener as well.
                    return transport.registerListener(topic, listener).thenApply(status -> response);
                }
                return CompletableFuture.completedFuture(response);
            })

            // Then Add the handler (if the client provided one) so the client can be notified of 
            // changes to the subscription state.
            .whenComplete( (response, exception) -> {
                if (exception == null && handler != null) {
                    mHandlers.compute(topic, (k, existingHandler) -> {
                        if (existingHandler != null && existingHandler != handler) {
                            throw new UStatusException(UCode.ALREADY_EXISTS, "Handler already registered");
                        }
                        return handler;
                    });
                }
            });
    }


    /**
     * Unsubscribes from a given topic.
     * 
     * The subscriber no longer wishes to be subscribed to said topic so we issue a unsubscribe
     * request to the USubscription service. The API will return a {@link CompletionStage} with the
     * {@link UStatus} of the result. If we are unable to unsubscribe to the topic with USubscription
     * service, the listener and handler (if any) will remain registered.
     * 
     * @param topic The topic to unsubscribe to.
     * @param listener The listener to be called when messages are received.
     * @param options The {@link CallOptions} to be used for the unsubscribe request.
     * @return Returns {@link UStatus} with the result from the unsubscribe request.
     */
    @Override
    public CompletionStage<UStatus> unsubscribe(UUri topic, UListener listener, CallOptions options) {
        Objects.requireNonNull(topic, "Unsubscribe topic missing");
        Objects.requireNonNull(listener, "listener missing");
        Objects.requireNonNull(options, "CallOptions missing");

        final UnsubscribeRequest unsubscribeRequest = UnsubscribeRequest.newBuilder()
            .setTopic(topic)
            .build();

        return RpcMapper.mapResponseToResult(
            // Send the unsubscribe request
            rpcClient.invokeMethod(UNSUBSCRIBE_METHOD, UPayload.pack(unsubscribeRequest), options),     
                UnsubscribeResponse.class)
            // Then unregister the listener
            .thenCompose( response ->  {
                if (response.isSuccess()) {
                    // Remove the handler only if unsubscribe was successful
                    mHandlers.remove(topic);

                    return transport.unregisterListener(topic, listener);
                }
                return CompletableFuture.completedFuture(response.failureValue());
            });
    }


    /**
     * Unregister the listener and removes any registered {@link SubscriptionChangeHandler} for the topic.
     * 
     * This method is used to remove handlers/listeners without notifying the uSubscription service 
     * so that we can be persistently subscribed even when the uE is not running.
     * 
     * @param topic The topic to subscribe to.
     * @param listener The listener to be called when messages are received.
     * @return Returns {@link UStatus} with the status of the listener unregister request.
     */
    @Override
    public CompletionStage<UStatus> unregisterListener(UUri topic, UListener listener) {
        Objects.requireNonNull(topic, "Unsubscribe topic missing");
        Objects.requireNonNull(listener, "Request listener missing");
        return transport.unregisterListener(topic, listener)
            .whenComplete((status, exception) ->  mHandlers.remove(topic));
    }

    /**
     * Close the subscription client and clean up resources.
     */
    public void close() {
        mHandlers.clear();
        notifier.unregisterNotificationListener(NOTIFICATION_TOPIC, mNotificationListener)
            .toCompletableFuture().join();
    }


    /**
     * Register for Subscription Change Notifications.
     * 
     * This API allows producers to register to receive subscription change notifications for
     * topics that they produce only.
     * 
     * @param topic The topic to register for notifications.
     * @param handler The {@link SubscriptionChangeHandler} to handle the subscription changes.
     * @param options The {@link CallOptions} to be used for the register request.
     * @return {@link CompletionStage} completed successfully if uSubscription service accepts the
     *         request to register the caller to be notified of subscription changes, or 
     *         the CompletionStage completes exceptionally with {@link UStatus} that indicates
     *         the failure reason.
     */
    @Override
    public CompletionStage<NotificationsResponse> registerForNotifications(UUri topic,
        SubscriptionChangeHandler handler, CallOptions options) {
        Objects.requireNonNull(topic, "Topic missing");
        Objects.requireNonNull(handler, "Handler missing");
        Objects.requireNonNull(options, "CallOptions missing");

        NotificationsRequest request = NotificationsRequest.newBuilder()
            .setTopic(topic)
            .build();

        return RpcMapper.mapResponse(rpcClient.invokeMethod(REGISTER_NOTIFICATIONS_METHOD,
            UPayload.pack(request), options), NotificationsResponse.class)
            // Then Add the handler (if the client provided one) so the client can be notified of 
            // changes to the subscription state.
            .whenComplete( (response, exception) -> {
                if (exception == null) {
                    mHandlers.compute(topic, (k, existingHandler) -> {
                        if (existingHandler != null && existingHandler != handler) {
                            throw new UStatusException(UCode.ALREADY_EXISTS, "Handler already registered");
                        }
                        return handler;
                    });
                }
            });
    }


    /**
     * Unregister for subscription change notifications.
     * 
     * @param topic The topic to unregister for notifications.
     * @param options The {@link CallOptions} to be used for the unregister request.
     * @return {@link CompletionStage} completed successfully with {@link NotificationsResponse} with
     *         the status of the API call to uSubscription service, or completed unsuccessfully with
     *         {@link UStatus} with the reason for the failure. {@link UCode#PERMISSION_DENIED} is
     *         returned if the topic ue_id does not equal the callers ue_id. 
     */
    @Override
    public CompletionStage<NotificationsResponse> unregisterForNotifications(UUri topic, CallOptions options) {
        Objects.requireNonNull(topic, "Topic missing");
        Objects.requireNonNull(options, "CallOptions missing");
        
        NotificationsRequest request = NotificationsRequest.newBuilder()
            .setTopic(topic)
            .build();

        return RpcMapper.mapResponse(rpcClient.invokeMethod(UNREGISTER_NOTIFICATIONS_METHOD, 
            UPayload.pack(request), options), NotificationsResponse.class)
            .whenComplete((response, exception) -> mHandlers.remove(topic));
    }


    /**
     * Fetch the list of subscribers for a given produced topic.
     * 
     * @param topic The topic to fetch the subscribers for.
     * @param options The {@link CallOptions} to be used for the fetch request.
     * @return {@link CompletionStage} completed successfully with {@link FetchSubscribersResponse} with
     *         the list of subscribers, or completed unsuccessfully with {@link UStatus} with the reason
     *         for the failure. 
     */
    @Override
    public CompletionStage<FetchSubscribersResponse> fetchSubscribers(UUri topic, CallOptions options) {
        Objects.requireNonNull(topic, "Topic missing");
        Objects.requireNonNull(options, "CallOptions missing");
        
        FetchSubscribersRequest request = FetchSubscribersRequest.newBuilder().setTopic(topic).build();
        return RpcMapper.mapResponse(rpcClient.invokeMethod(FETCH_SUBSCRIBERS_METHOD, 
            UPayload.pack(request), options), FetchSubscribersResponse.class);
    }


    /**
     * Fetch list of Subscriptions for a given topic. 
     * 
     * API provides more information than {@code fetchSubscribers()} in that it also returns  
     * {@link SubscribeAttributes} per subscriber that might be useful to the producer to know.
     *
     * @param request The request containing the topic to fetch subscriptions for.
     * @param options The {@link CallOptions} to be used for the request.
     * @return {@link CompletionStage} completed successfully with {@link FetchSubscriptionsResponse} that
     *      contains the subscription information per subscriber to the topic or completed unsuccessfully with
     *      {@link UStatus} with the reason for the failure. {@link UCode#PERMISSION_DENIED} is returned if the
     *      topic ue_id does not equal the callers ue_id. 
     */
    @Override
    public CompletionStage<FetchSubscriptionsResponse> fetchSubscriptions(FetchSubscriptionsRequest request,
        CallOptions options) {
        Objects.requireNonNull(request, "Request missing");
        Objects.requireNonNull(options, "CallOptions missing");
        
        return RpcMapper.mapResponse(rpcClient.invokeMethod(FETCH_SUBSCRIPTIONS_METHOD, 
            UPayload.pack(request), options), FetchSubscriptionsResponse.class);
    }


    /**
     * Handles incoming notifications from the USubscription service.
     * 
     * @param message The notification message from the USubscription service
     */
    private void handleNotifications(UMessage message) {
        // Ignore messages that are not notifications
        if (message.getAttributes().getType() != UMessageType.UMESSAGE_TYPE_NOTIFICATION) {
            return;
        }

        // Unpack the notification message from uSubscription called Update
        Optional<Update> subscriptionUpdate = UPayload.unpack(
            message.getPayload(), message.getAttributes().getPayloadFormat(), Update.class);

        // Check if we got the right subscription change notification
        if (subscriptionUpdate.isPresent()) {
            // Check if we have a handler registered for the subscription change notification for the specific 
            // topic that triggered the subscription change notification. It is very possible that the client
            // did not register one to begin with (i.e they don't care to receive it)
            mHandlers.computeIfPresent(subscriptionUpdate.get().getTopic(), (topic, handler) -> {
                try {
                    handler.handleSubscriptionChange(subscriptionUpdate.get().getTopic(), 
                        subscriptionUpdate.get().getStatus());
                } catch (Exception e) {
                    Logger.getGlobal().info(e.getMessage());
                }
                return handler;
            });
        }
    }
}
