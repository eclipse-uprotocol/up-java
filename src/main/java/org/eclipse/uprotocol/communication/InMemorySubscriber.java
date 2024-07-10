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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.eclipse.uprotocol.core.usubscription.v3.SubscriberInfo;
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
 * The following is an in-memory implementation of the {@link Subscriber} interface that
 * wraps the {@link UTransport} for implementing the Subscriber-side of the pub/sub 
 * messaging pattern to allow developers to subscribe and unsubscribe to topics. This 
 * implementation uses the {@link InMemoryRpcClient} and {@link SimpleNotifier} interfaces
 * to invoke the subscription request message to the usubscription service, and register
 * to receive notifications for changes from the uSubscription service.
 * 
 */
public class InMemorySubscriber implements Subscriber {

    private final UTransport transport;
    private final RpcClient rpcClient;
    private final Notifier notifier;

    private static final ServiceDescriptor USUBSCRIPTION = USubscriptionProto.getDescriptor().getServices().get(0);

    // TODO: The following items need to be pulled from generated code
    private static final UUri SUBSCRIBE_METHOD = UriFactory.fromProto(USUBSCRIPTION, 1);
    private static final UUri UNSUBSCRIBE_METHOD = UriFactory.fromProto(USUBSCRIPTION, 2);
    private static final UUri NOTIFICATION_TOPIC = UriFactory.fromProto(USUBSCRIPTION, 0x8000); 

    // Map to store subscription change notification handlers
    private final ConcurrentHashMap<UUri, SubscriptionChangeHandler> mHandlers = new ConcurrentHashMap<>();

    // transport Notification listener that will process subscription change notifications
    private final UListener mNotificationListener = this::handleNotifications;
    

    /**
     * Constructor for the DefaultSubscriber.
     * 
     * @param transport the transport to use for sending the notifications
     * @param rpcClient the rpc client to use for sending the RPC requests
     * @param notifier the notifier to use for registering the notification listener
     */
    public InMemorySubscriber (UTransport transport, RpcClient rpcClient, Notifier notifier) {
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
     * Subscribe to a given topic. <br>
     * 
     * The API will return a {@link CompletionStage} with the response {@link SubscriptionResponse} or exception
     * with the failure if the subscription was not successful. The optional passed {@link SubscriptionChangeHandler}
     * is used to receive notifications of changes to {@link SubscriptionStatus.State} such as SUBSCRIBE_PENDING 
     * to SUBSCRIBED that occurs when we subscribe to remote topics that the device we are on has not yet
     * a subscriber who has subscribed to said topic. 
     * 
     * NOTE: If you call this API multiple times passing a different handler, {@link UCode}.ALREADY_EXISTS will be
     * returned.
     * 
     * @param topic The topic to subscribe to.
     * @param listener The listener to be called when a message is received on the topic.
     * @param options The call options for the subscription.
     * @param handler {@link SubscriptionChangeHandler} to handle changes to subscription states.
     * @return Returns the CompletionStage with {@link SubscriptionResponse} or exception with the failure
     * reason as {@link UStatus}. {@link UCode}.ALREADY_EXISTS will be returned if you call this API multiple
     * times passing a different handler. 
     */
    @Override
    public CompletionStage<SubscriptionResponse> subscribe(UUri topic, UListener listener, CallOptions options,
        SubscriptionChangeHandler handler) {
        Objects.requireNonNull(topic, "Subscribe topic missing");
        Objects.requireNonNull(listener, "Request listener missing");
        options = Objects.requireNonNullElse(options, CallOptions.DEFAULT);

        final SubscriptionRequest request = SubscriptionRequest.newBuilder()
            .setTopic(topic)
            .setSubscriber(SubscriberInfo.newBuilder().setUri(transport.getSource()).build())
            .build();
        
        // Send the subscription request and handle the response
        return RpcMapper.mapResponse(rpcClient.invokeMethod(
                SUBSCRIBE_METHOD, UPayload.pack(request), options), SubscriptionResponse.class)
            
            // Then register the listener to be called when messages are received
            .thenCompose(response -> {
                if ( response.getStatus().getState() == SubscriptionStatus.State.SUBSCRIBED ||
                    response.getStatus().getState() == SubscriptionStatus.State.SUBSCRIBE_PENDING) {
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
     * Unsubscribe to a given topic. <br>
     * 
     * The subscriber no longer wishes to be subscribed to said topic so we issue a unsubscribe
     * request to the USubscription service. The API will return a {@link CompletionStage} with the
     * {@link UStatus} of the result. If we are unable to unsubscribe to the topic with USubscription
     * service, the listener and handler (if any) will remain registered.
     * 
     * @param topic The topic to unsubscribe to.
     * @param listener The listener to be called when a message is received on the topic.
     * @param options The call options for the subscription.
     * @return Returns {@link UStatus} with the result from the unsubscribe request.
     */
    @Override
    public CompletionStage<UStatus> unsubscribe(UUri topic, UListener listener, CallOptions options) {
        Objects.requireNonNull(topic, "Unsubscribe topic missing");
        Objects.requireNonNull(listener, "listener missing");

        final UnsubscribeRequest unsubscribeRequest = UnsubscribeRequest.newBuilder().setTopic(topic).build();
        
        return RpcMapper.mapResponseToResult(
            // Send the unsubscribe request
            rpcClient.invokeMethod(UNSUBSCRIBE_METHOD, UPayload.pack(unsubscribeRequest), options),     
                UnsubscribeResponse.class)
            // Then unregister the listener
            .thenCompose( response ->  {
                if (response.isSuccess()) {
                    return transport.unregisterListener(topic, listener);
                }
                return CompletableFuture.completedFuture(response.failureValue());
            })
            // Remove the handler regardless if unregisterListener() succeeds or not
            .whenComplete((status, exception) ->  mHandlers.remove(topic));       
    }


    /**
     * Unregister a listener and remove any registered {@link SubscriptionChangeHandler} for the topic. <br>
     * 
     * This method is used to remove handlers/listeners without notifying the uSubscription service 
     * so that we can be persistently subscribed even when the uE is not running.
     * 
     * @param topic The topic to subscribe to.
     * @param listener The listener to be called when a message is received on the topic.
     * @return Returns {@link UStatus} with the status of the listener unregister request.
     */
    @Override
    public CompletionStage<UStatus> unregisterListener(UUri topic, UListener listener) {
        Objects.requireNonNull(topic, "Unsubscribe topic missing");
        Objects.requireNonNull(listener, "Request listener missing");
        return transport.unregisterListener(topic, listener)
            .whenComplete((status, exception) ->  mHandlers.remove(topic));
    }

    public void close() {
        mHandlers.clear();
        notifier.unregisterNotificationListener(NOTIFICATION_TOPIC, mNotificationListener)
            .toCompletableFuture().join();
    }


    /**
     * Handle incoming notifications from the USubscription service.
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
