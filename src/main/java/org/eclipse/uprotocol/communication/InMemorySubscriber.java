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
import java.util.function.Consumer;

import org.eclipse.uprotocol.client.usubscription.v3.RpcClientBasedUSubscriptionClient;
import org.eclipse.uprotocol.client.usubscription.v3.USubscriptionClient;
import org.eclipse.uprotocol.core.usubscription.v3.NotificationsRequest;
import org.eclipse.uprotocol.core.usubscription.v3.NotificationsResponse;
import org.eclipse.uprotocol.core.usubscription.v3.SubscriptionRequest;
import org.eclipse.uprotocol.core.usubscription.v3.SubscriptionResponse;
import org.eclipse.uprotocol.core.usubscription.v3.UnsubscribeRequest;
import org.eclipse.uprotocol.core.usubscription.v3.UnsubscribeResponse;
import org.eclipse.uprotocol.core.usubscription.v3.Update;
import org.eclipse.uprotocol.transport.LocalUriProvider;
import org.eclipse.uprotocol.transport.UListener;
import org.eclipse.uprotocol.transport.UTransport;
import org.eclipse.uprotocol.uri.serializer.UriSerializer;
import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UMessage;
import org.eclipse.uprotocol.v1.UMessageType;
import org.eclipse.uprotocol.v1.UUri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Subscriber which keeps all information about registered subscription change handlers in memory.
 * <p>
 * The subscriber requires a {@link USubscriptionClient implementation} in order to inform a
 * USubscription service instance about newly subscribed and unsubscribed topics. It also needs a
 * {@link Notifier} for receiving notifications about subscription status updates from the USubscription
 * service. Finally, it needs a {@link UTransport} for receiving events that have been published to
 * subscribed topics.
 * <p>
 * During startup the subscriber uses the Notifier to register a generic {@link UListener} for receiving
 * notifications from the USubscription service. The listener maintains an in-memory mapping
 * of subscribed topics to corresponding subscription change handlers.
 * <p>
 * When a client {@link #subscribe(UUri, UListener, Optional) subscribes to a topic}, the USubscription
 * service is informed about the new subscription and a (client provided) subscription change handler is
 * registered with the listener. When a subscription change notification arrives from the USubscription
 * service, the corresponding handler is being looked up and invoked.
 */
public final class InMemorySubscriber implements Subscriber {
    private static final Logger LOGGER = LoggerFactory.getLogger(InMemorySubscriber.class);

    private final UTransport transport;
    private final USubscriptionClient subscriptionClient;
    private final Notifier notifier;

    // topic URI -> subscription change notification handler
    private final Map<UUri, SubscriptionChangeHandler> subscriptionChangeHandlers = new ConcurrentHashMap<>();

    // listener for processing subscription change notifications
    private final UListener subscriptionChangeListener = this::handleSubscriptionChangeNotification;

    private Consumer<UMessage> unexpectedMessageHandler;

    /**
     * Creates a new USubscription client passing {@link UTransport} and {@link CallOptions}
     * used to provide additional options for the RPC requests to uSubscription service.
     * 
     * @param transport The transport to use for sending the notifications
     * @param uriProvider The URI provider to use for generating local resource URIs.
     * @param options The call options to use for the RPC requests.
     * @param subscriptionServiceInstanceId The instance of the subscription service to invoke,
     * {@code 0x000} to use the default instance.
     * @param subscriptionServiceAuthority The authority that the subscription service runs on,
     * or {@code null} if the instance runs on the local authority.
     */
    public InMemorySubscriber(
            UTransport transport,
            LocalUriProvider uriProvider,
            CallOptions options,
            int subscriptionServiceInstanceId,
            String subscriptionServiceAuthority) {
        this(
            transport,
            new RpcClientBasedUSubscriptionClient(
                new InMemoryRpcClient(transport, uriProvider),
                options,
                subscriptionServiceInstanceId,
                subscriptionServiceAuthority
            ),
            new SimpleNotifier(transport, uriProvider));
    }

    /**
     * Creates a new USubscription client.
     * <p>
     * Also registers a listener for subscription change notifications from the USubscription service
     * instance that the given USubscription client is
     * {@link USubscriptionClient#getSubscriptionServiceNotificationTopic() configured to use}.
     *
     * @param transport The transport to use for sending the notifications.
     * @param subscriptionClient The client to use for interacting with the USubscription service.
     * @param notifier The notifier to use for registering the notification listener.
     */
    public InMemorySubscriber (
            UTransport transport,
            USubscriptionClient subscriptionClient,
            Notifier notifier) {
        Objects.requireNonNull(transport, "Transport missing");
        Objects.requireNonNull(subscriptionClient, "SubscriptionClient missing");
        Objects.requireNonNull(notifier, "Notifier missing");
        this.transport = transport;
        this.subscriptionClient = subscriptionClient;
        this.notifier = notifier;

        // Register listener for receiving subscription change notifications
        notifier.registerNotificationListener(
                subscriptionClient.getSubscriptionServiceNotificationTopic(),
                subscriptionChangeListener)
            .toCompletableFuture().join();
    }

    void setUnexpectedMessageHandler(Consumer<UMessage> handler) {
        this.unexpectedMessageHandler = handler;
    }

    /**
     * Closes this client and cleans up resources.
     */
    public void close() {
        subscriptionChangeHandlers.clear();
        try {
            notifier.unregisterNotificationListener(
                    subscriptionClient.getSubscriptionServiceNotificationTopic(),
                    subscriptionChangeListener)
                .toCompletableFuture().join();
        } catch (CompletionException e) {
            LOGGER.debug("error while unregistering listener for subscription change notifications", e);
        }
    }

    void addSubscriptionChangeHandler(
            UUri topic,
            Optional<SubscriptionChangeHandler> handler) {

        handler.ifPresent(newHandler -> {

            subscriptionChangeHandlers.compute(topic, (k, existingHandler) -> {
                if (existingHandler != null && existingHandler != newHandler) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(
                            "Subscription state notification handler already registered for topic [{}]",
                            UriSerializer.serialize(topic));
                    }
                    throw new UStatusException(
                        UCode.ALREADY_EXISTS,
                        "Subscription state notification handler already registered");
                }
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(
                        "Registering subscription state notification handler for topic [{}]",
                        UriSerializer.serialize(topic));
                }
                return newHandler;
            });
        });
    }

    boolean hasSubscriptionChangeHandler(UUri topic) {
        return subscriptionChangeHandlers.containsKey(topic);
    }

    @Override
    public CompletionStage<SubscriptionResponse> subscribe(
            UUri topic,
            UListener listener,
            Optional<SubscriptionChangeHandler> handler) {
        Objects.requireNonNull(topic, "Subscribe topic missing");
        Objects.requireNonNull(listener, "Request listener missing");

        final var request = SubscriptionRequest.newBuilder().setTopic(topic).build();

        return subscriptionClient.subscribe(request)
            // add the subscription change handler (if the client provided one) so the client
            // can be notified of changes to the subscription state.
            .thenApply(subscriptionResponse -> {
                addSubscriptionChangeHandler(topic, handler);
                return subscriptionResponse;
            })
            .thenCompose(subscriptionResponse -> {
                switch (subscriptionResponse.getStatus().getState()) {
                    case SUBSCRIBED:
                    case SUBSCRIBE_PENDING:
                        return transport.registerListener(topic, listener)
                            .thenApply(ok -> subscriptionResponse)
                            .exceptionallyCompose(t -> {
                                // When registering the listener fails, we have ended up in a situation where we
                                // have successfully (logically) subscribed to the topic via the USubscription service
                                // but we have not been able to register the listener with the local transport.
                                // This means that events might start getting forwarded to the local authority which
                                // are not being consumed. Apart from this inefficiency, this does not pose a real
                                // problem and since we return a failed future, the client might be inclined to try
                                // again and (eventually) succeed in registering the listener as well.
                                if (LOGGER.isWarnEnabled()) {
                                    LOGGER.warn(
                                        "Failed to register listener for topic [{}]: {}",
                                        UriSerializer.serialize(topic), t.getMessage());
                                }
                                return CompletableFuture.failedStage(t);
                            });
                    default:
                        // The USubscription service should not return any other subscription state
                        return CompletableFuture.failedStage(new UStatusException(
                            UCode.INTERNAL,
                            "Subscription request resulted in invalid state"));
                }
            });
    }

    @Override
    public CompletionStage<UnsubscribeResponse> unsubscribe(UUri topic, UListener listener) {
        Objects.requireNonNull(topic, "Unsubscribe topic missing");
        Objects.requireNonNull(listener, "listener missing");

        final var request = UnsubscribeRequest.newBuilder().setTopic(topic).build();

        return subscriptionClient.unsubscribe(request)
            .thenApply(unsubscribeResponse -> {
                // remove subscription change handler (if one had been registered)
                subscriptionChangeHandlers.remove(topic);
                return unsubscribeResponse;
            })
            .thenCompose(unsubscribeResponse -> {
                // remove subscription change handler (if one had been registered)
                subscriptionChangeHandlers.remove(topic);
                // When this fails, we have ended up in a situation where we
                // have successfully (logically) unsubscribed from the topic via the USubscription service
                // but we have not been able to unregister the listener from the local transport.
                // This means that events originating from entities connected to a different transport
                // may no longer get forwarded to the local transport, resulting in the (still registered)
                // listener not being invoked for these events. We therefore return an error which should
                // trigger the client to try again and (eventually) succeed in unregistering the listener
                // as well.
                return transport.unregisterListener(topic, listener)
                    .whenComplete((ok, throwable) -> {
                        if (throwable != null) {
                            LOGGER.warn("Failed to unregister listener for topic {}: {}", topic, throwable);
                        }
                    })
                    .thenApply(ok -> unsubscribeResponse);
            });
    }

    @Override
    public CompletionStage<NotificationsResponse> registerSubscriptionChangeHandler(
            UUri topic,
            SubscriptionChangeHandler handler) {
        Objects.requireNonNull(topic, "Topic missing");
        Objects.requireNonNull(handler, "Handler missing");

        final var request = NotificationsRequest.newBuilder().setTopic(topic).build();
        return subscriptionClient.registerForNotifications(request)
            // Then add the handler so the client can be notified of 
            // changes to the subscription state.
            .thenApply(response -> {
                addSubscriptionChangeHandler(topic, Optional.of(handler));
                return response;
            });
    }

    @Override
    public CompletionStage<NotificationsResponse> unregisterSubscriptionChangeHandler(UUri topic) {
        Objects.requireNonNull(topic, "Topic missing");

        final var request = NotificationsRequest.newBuilder().setTopic(topic).build();
        return subscriptionClient.unregisterForNotifications(request)
            .thenApply(response -> {
                // remove subscription change handler (if one had been registered)
                subscriptionChangeHandlers.remove(topic);
                return response;
            });
    }

    /**
     * Handles incoming notifications from the USubscription service.
     * 
     * @param message The notification message from the USubscription service
     */
    private void handleSubscriptionChangeNotification(UMessage message) {
        // Ignore messages that are not notifications
        if (message.getAttributes().getType() != UMessageType.UMESSAGE_TYPE_NOTIFICATION) {
            Optional.ofNullable(unexpectedMessageHandler).ifPresent(handler -> handler.accept(message));
            return;
        }

        UPayload.unpack(message, Update.class)
            // Check if we have a handler registered for the subscription change notification for the specific
            // topic that triggered the subscription change notification. It is very possible that the client
            // did not register one to begin with (i.e they don't care to receive it)
            .ifPresent(subscriptionUpdate -> {
                final var topic = subscriptionUpdate.getTopic();
                Optional.ofNullable(subscriptionChangeHandlers.get(topic))
                    .ifPresentOrElse(handler -> {
                        try {
                            handler.handleSubscriptionChange(topic, subscriptionUpdate.getStatus());
                        } catch (Exception e) {
                            LOGGER.info("Error handling subscription update", e);
                        }
                    }, () -> Optional.ofNullable(unexpectedMessageHandler)
                                .ifPresent(handler -> handler.accept(message)));
            });
    }
}
