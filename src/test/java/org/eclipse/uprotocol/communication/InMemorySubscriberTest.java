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

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import org.mockito.junit.jupiter.MockitoExtension;

import org.eclipse.uprotocol.client.usubscription.v3.USubscriptionClient;
import org.eclipse.uprotocol.core.usubscription.v3.NotificationsRequest;
import org.eclipse.uprotocol.core.usubscription.v3.NotificationsResponse;
import org.eclipse.uprotocol.core.usubscription.v3.SubscriptionRequest;
import org.eclipse.uprotocol.core.usubscription.v3.SubscriptionResponse;
import org.eclipse.uprotocol.core.usubscription.v3.SubscriptionStatus;
import org.eclipse.uprotocol.core.usubscription.v3.UnsubscribeRequest;
import org.eclipse.uprotocol.core.usubscription.v3.UnsubscribeResponse;
import org.eclipse.uprotocol.core.usubscription.v3.Update;
import org.eclipse.uprotocol.core.usubscription.v3.SubscriptionStatus.State;
import org.eclipse.uprotocol.transport.LocalUriProvider;
import org.eclipse.uprotocol.transport.StaticUriProvider;
import org.eclipse.uprotocol.transport.UListener;
import org.eclipse.uprotocol.transport.UTransport;
import org.eclipse.uprotocol.transport.builder.UMessageBuilder;
import org.eclipse.uprotocol.uuid.factory.UuidFactory;
import org.eclipse.uprotocol.v1.UAttributes;
import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UMessage;
import org.eclipse.uprotocol.v1.UMessageType;
import org.eclipse.uprotocol.v1.UUri;

@ExtendWith(MockitoExtension.class)
// [utest->dsn~communication-layer-impl-default~1]
class InMemorySubscriberTest {
    private static final UUri SUBSCRIPTION_SERVICE_URI = UUri.newBuilder()
        .setAuthorityName("some-host")
        .setUeId(0x0002_0000)
        .setUeVersionMajor(0x03)
        .build();
    private static final UUri SUBSCRIPTION_NOTIFICATION_TOPIC_URI = UUri.newBuilder(SUBSCRIPTION_SERVICE_URI)
        .setResourceId(0x8000)
        .build();
    private static final UUri TOPIC = UUri.newBuilder()
        .setAuthorityName("my-vehicle")
        .setUeId(0xa103)
        .setUeVersionMajor(0x06)
        .setResourceId(0xa10f)
        .build();

    private static final UUri SOURCE = UUri.newBuilder()
        .setAuthorityName("my-vehicle")
        .setUeId(0x0004)
        .setUeVersionMajor(0x01)
        .build();

    private UTransport transport;
    private LocalUriProvider uriProvider;
    private Notifier notifier;
    private USubscriptionClient subscriptionClient;

    @Mock
    private SubscriptionChangeHandler subscriptionChangeHandler;
    private ArgumentCaptor<UListener> notificationListener;

    @Mock
    private UListener listener;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setup() {
        uriProvider = StaticUriProvider.of(SOURCE);

        transport = mock(UTransport.class);
        Mockito.lenient().when(transport.registerListener(any(UUri.class), any(Optional.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(null));
        Mockito.lenient().when(transport.registerListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(null));
        Mockito.lenient().when(transport.unregisterListener(any(UUri.class), any(Optional.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(null));
        Mockito.lenient().when(transport.unregisterListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(null));

        notifier = mock(Notifier.class);
        Mockito.lenient().when(notifier.registerNotificationListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(null));
        Mockito.lenient().when(notifier.unregisterNotificationListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(null));

        subscriptionClient = mock(USubscriptionClient.class);
        Mockito.lenient().when(subscriptionClient.getSubscriptionServiceNotificationTopic())
            .thenReturn(SUBSCRIPTION_NOTIFICATION_TOPIC_URI);

        notificationListener = ArgumentCaptor.forClass(UListener.class);
    }

    @Test
    @DisplayName("Test constructors reject invalid arguments")
    void testConstructorsRejectInvalidArguments() {
        assertThrows(NullPointerException.class, () -> new InMemorySubscriber(
            null,
            uriProvider,
            CallOptions.DEFAULT,
            0x0000,
            "local"));
        assertThrows(NullPointerException.class, () -> new InMemorySubscriber(
            transport,
            null,
            CallOptions.DEFAULT,
            0x0000,
            "local"));
        assertThrows(NullPointerException.class, () -> new InMemorySubscriber(
            transport,
            uriProvider,
            null,
            0x0000,
            "local"));
        assertThrows(IllegalArgumentException.class, () -> new InMemorySubscriber(
            transport,
            uriProvider,
            CallOptions.DEFAULT,
            0xFFFF,
            "local"));
        assertThrows(IllegalArgumentException.class, () -> new InMemorySubscriber(
            transport,
            uriProvider,
            CallOptions.DEFAULT,
            -1,
            "local"));

        assertThrows(NullPointerException.class, () -> new InMemorySubscriber(
            null,
            subscriptionClient,
            notifier));
        assertThrows(NullPointerException.class, () -> new InMemorySubscriber(
            transport,
            null,
            notifier));
        assertThrows(NullPointerException.class, () -> new InMemorySubscriber(
            transport,
            subscriptionClient,
            null));
    }

    @Test
    void testConstructorForTransportAndUriProvider() {
        new InMemorySubscriber(
            transport,
            uriProvider,
            CallOptions.DEFAULT,
            0x0002,
            SUBSCRIPTION_NOTIFICATION_TOPIC_URI.getAuthorityName());
        verify(transport).registerListener(
            eq(SUBSCRIPTION_NOTIFICATION_TOPIC_URI),
            eq(Optional.of(SOURCE)),
            any(UListener.class));
    }

    @Test
    void testSubscriberRegistersNotificationListener() {
        // WHEN trying to create a Subscriber
        new InMemorySubscriber(transport, subscriptionClient, notifier);
        // THEN the Subscriber registers a notification listener
        verify(notifier).registerNotificationListener(
            eq(SUBSCRIPTION_NOTIFICATION_TOPIC_URI),
            notificationListener.capture());
    }

    @Test
    void testSubscriberCreationFailsWhenNotifierFailsToRegisterListener() {
        // GIVEN a Notifier that is not connected to its transport
        when(notifier.registerNotificationListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.failedFuture(
                new UStatusException(UCode.UNAVAILABLE, "not available")));

        // WHEN trying to create a Subscriber for this Notifier
        final var exception = assertThrows(CompletionException.class, () -> new InMemorySubscriber(
            transport,
            subscriptionClient,
            notifier
        ));
        // THEN creation fails
        verify(notifier).registerNotificationListener(
            eq(SUBSCRIPTION_NOTIFICATION_TOPIC_URI),
            any(UListener.class));
        assertEquals(UCode.UNAVAILABLE, ((UStatusException) exception.getCause()).getStatus().getCode());
    }


    @Test
    void testCloseForSuccessfulUnregistration() {
        // GIVEN a Notifier that succeeds to unregister listeners
        when(notifier.unregisterNotificationListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(null));
        assertCloseIgnoresFailureToUnregisterNotificationListener(notifier);
    }

    @Test
    void testCloseForFailedUnregistration() {
        // GIVEN a Notifier that fails to unregister listeners
        when(notifier.unregisterNotificationListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.failedFuture(
                new UStatusException(UCode.UNAVAILABLE, "not available")));
        assertCloseIgnoresFailureToUnregisterNotificationListener(notifier);
    }

    private void assertCloseIgnoresFailureToUnregisterNotificationListener(Notifier notifier) {
        // GIVEN a Subscriber using the Notifier
        var subscriber = new InMemorySubscriber(transport, subscriptionClient, notifier);
        verify(notifier).registerNotificationListener(
            eq(SUBSCRIPTION_NOTIFICATION_TOPIC_URI),
            notificationListener.capture());

        // WHEN trying to close the Subscriber
        subscriber.close();

        // THEN the listener is getting unregistered
        verify(notifier).unregisterNotificationListener(
            SUBSCRIPTION_NOTIFICATION_TOPIC_URI,
            notificationListener.getValue());
    }

    @Test
    void testSubscribeFailsWhenUSubscriptionInvocationFails() {
        // GIVEN a USubscription client
        // that fails to perform subscription due to different reasons
        when(subscriptionClient.subscribe(any(SubscriptionRequest.class)))
            .thenReturn(CompletableFuture.failedFuture(
                new UStatusException(UCode.UNAVAILABLE, "not connected")))
            .thenReturn(CompletableFuture.completedFuture(
                SubscriptionResponse.newBuilder()
                    .setTopic(TOPIC)
                    .setStatus(SubscriptionStatus.newBuilder()
                        .setState(SubscriptionStatus.State.UNSUBSCRIBED)
                        .setMessage("unsupported topic")
                        .build())
                    .build()))
            .thenReturn(CompletableFuture.completedFuture(
                SubscriptionResponse.newBuilder()
                    .setTopic(TOPIC)
                    .setStatus(SubscriptionStatus.newBuilder()
                        .setMessage("unknown state")
                        .build())
                    .build()));

        // and a Subscriber using that USubscription client
        var subscriber = new InMemorySubscriber(transport, subscriptionClient, notifier);
        verify(notifier).registerNotificationListener(
            eq(SUBSCRIPTION_NOTIFICATION_TOPIC_URI),
            notificationListener.capture());

        // WHEN subscribing to a topic
        var attempt1 = subscriber.subscribe(TOPIC, listener, Optional.empty());

        // THEN the first attempt fails
        var exception = assertThrows(CompletionException.class, () -> attempt1.toCompletableFuture().join());
        assertEquals(UCode.UNAVAILABLE, ((UStatusException) exception.getCause()).getCode());

        // AND the second attempt fails as well
        var attempt2 = subscriber.subscribe(TOPIC, listener, Optional.empty());
        exception = assertThrows(CompletionException.class, () -> attempt2.toCompletableFuture().join());
        assertEquals(UCode.INTERNAL, ((UStatusException) exception.getCause()).getCode());

        // AND the third attempt fails as well
        var attempt3 = subscriber.subscribe(TOPIC, listener, Optional.empty());
        exception = assertThrows(CompletionException.class, () -> attempt3.toCompletableFuture().join());
        assertEquals(UCode.INTERNAL, ((UStatusException) exception.getCause()).getCode());

        verify(subscriptionClient, times(3)).subscribe(argThat(req -> req.getTopic().equals(TOPIC)));
    }

    @Test
    void testRepeatedSubscribeFailsForDifferentSubscriptionChangeHandlers() {
        // GIVEN  a USubscription client
        // that succeeds to subscribe to topics
        when(subscriptionClient.subscribe(any(SubscriptionRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(
                SubscriptionResponse.newBuilder()
                    .setTopic(TOPIC)
                    .setStatus(SubscriptionStatus.newBuilder()
                        .setState(SubscriptionStatus.State.SUBSCRIBED)
                        .build())
                    .build()));

        // AND a transport
        when(transport.registerListener(any(UUri.class), any(UListener.class)))
            // that fails to register a listener on the first attempt
            .thenReturn(CompletableFuture.failedFuture(
                new UStatusException(UCode.UNAVAILABLE, "not connected")))
            // but succeeds to do so on the second attempt
            .thenReturn(CompletableFuture.completedFuture(null));

        // AND a Subscriber using that USubscription client, Notifier and transport
        var subscriber = new InMemorySubscriber(transport, subscriptionClient, notifier);
        verify(notifier).registerNotificationListener(
            eq(SUBSCRIPTION_NOTIFICATION_TOPIC_URI),
            notificationListener.capture());

        // WHEN subscribing to a topic
        var attempt1 = subscriber.subscribe(TOPIC, listener, Optional.of(subscriptionChangeHandler));

        // THEN the first attempt fails due to the transport having failed
        var exception = assertThrows(CompletionException.class, () -> attempt1.toCompletableFuture().join());
        assertEquals(UCode.UNAVAILABLE, ((UStatusException) exception.getCause()).getCode());

        // AND a second attempt using a different subscription change handler
        var attempt2 = subscriber.subscribe(TOPIC, listener, Optional.of(mock(SubscriptionChangeHandler.class)));
        exception = assertThrows(CompletionException.class, () -> attempt2.toCompletableFuture().join());
        // fails with an ALREADY_EXISTS error
        assertEquals(UCode.ALREADY_EXISTS, ((UStatusException) exception.getCause()).getCode());

        verify(transport, times(1)).registerListener(TOPIC, listener);
        verify(subscriptionClient, times(2)).subscribe(argThat(req -> req.getTopic().equals(TOPIC)));
    }

    @Test
    void testSubscribeSucceedsOnSecondAttempt() {

        // GIVEN a USubscription client that succeeds to subscribe to topics
        when(subscriptionClient.subscribe(any(SubscriptionRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(
                SubscriptionResponse.newBuilder()
                    .setTopic(TOPIC)
                    .setStatus(SubscriptionStatus.newBuilder()
                        .setState(SubscriptionStatus.State.SUBSCRIBED)
                        .build())
                    .build()));

        // and a transport
        when(transport.registerListener(any(UUri.class), any(UListener.class)))
            // that first fails to register a listener
            .thenReturn(CompletableFuture.failedFuture(
                new UStatusException(UCode.UNAVAILABLE, "not connected")))
            // but succeeds on the second attempt
            .thenReturn(CompletableFuture.completedStage(null));


        // and a Subscriber using that USubscription client, Notifier and transport
        var subscriber = new InMemorySubscriber(transport, subscriptionClient, notifier);
        verify(notifier).registerNotificationListener(
            eq(SUBSCRIPTION_NOTIFICATION_TOPIC_URI),
            notificationListener.capture());

        // WHEN subscribing to a topic
        var attempt1 = subscriber.subscribe(TOPIC, listener, Optional.of(subscriptionChangeHandler));

        // THEN the first attempt fails due to the transport having failed
        var exception = assertThrows(CompletionException.class, () -> attempt1.toCompletableFuture().join());
        assertEquals(UCode.UNAVAILABLE, ((UStatusException) exception.getCause()).getCode());

        // but the second attempt succeeds
        subscriber.subscribe(TOPIC, listener, Optional.of(subscriptionChangeHandler))
            .toCompletableFuture().join();
        verify(subscriptionClient, times(2)).subscribe(argThat(req -> req.getTopic().equals(TOPIC)));
        verify(transport, times(2)).registerListener(TOPIC, listener);

        // and the subscription change handler receives notifications
        var update = Update.newBuilder()
            .setTopic(TOPIC)
            .setStatus(SubscriptionStatus.newBuilder()
                .setState(SubscriptionStatus.State.UNSUBSCRIBED)
                .build())
            .build();
        var subscriptionChange = UMessageBuilder.notification(SUBSCRIPTION_NOTIFICATION_TOPIC_URI, SOURCE)
            .build(UPayload.pack(update));
        notificationListener.getValue().onReceive(subscriptionChange);
        verify(subscriptionChangeHandler, times(1)).handleSubscriptionChange(
            eq(TOPIC), any(SubscriptionStatus.class));
    }

    @Test
    void testUnsubscribeFailsForUnknownListener() {

        // GIVEN a USubscription client
        // that succeeds to unsubscribe from topics
        when(subscriptionClient.unsubscribe(any(UnsubscribeRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(UnsubscribeResponse.newBuilder().build()));

        // AND a transport
        // which fails to unregister an unknown listener
        when(transport.unregisterListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.failedFuture(
                new UStatusException(UCode.NOT_FOUND, "no such listener")));

        // AND a Subscriber using that USubscription client, Notifier and transport
        var subscriber = new InMemorySubscriber(transport, subscriptionClient, notifier);
        verify(notifier).registerNotificationListener(
            eq(SUBSCRIPTION_NOTIFICATION_TOPIC_URI),
            notificationListener.capture());

        // WHEN unsubscribing from a topic for which no listener had been registered
        var attempt = subscriber.unsubscribe(TOPIC, listener);

        // THEN the attempt fails
        var exception = assertThrows(CompletionException.class, () -> attempt.toCompletableFuture().join());
        assertEquals(UCode.NOT_FOUND, ((UStatusException) exception.getCause()).getCode());
        verify(subscriptionClient).unsubscribe(argThat(req -> req.getTopic().equals(TOPIC)));
        verify(transport).unregisterListener(TOPIC, listener);
    }

    @Test
    void testUnsubscribeFailsIfUSubscriptionInvocationFails() {
        // GIVEN a USubscription client
        // that fails to unsubscribe from topics
        when(subscriptionClient.unsubscribe(any(UnsubscribeRequest.class)))
            .thenReturn(CompletableFuture.failedFuture(
                new UStatusException(UCode.UNAVAILABLE, "unknown")));

        // AND a transport
        // which succeeds to unregister listeners
        // (no need to stub explicitly, because this is the default)

        // AND a Subscriber using that USubscription client, Notifier and transport
        var subscriber = new InMemorySubscriber(transport, subscriptionClient, notifier);
        verify(notifier).registerNotificationListener(
            eq(SUBSCRIPTION_NOTIFICATION_TOPIC_URI),
            notificationListener.capture());
        // which already has a listener and corresponding subscription change handler
        // registered for a topic
        subscriber.addSubscriptionChangeHandler(TOPIC, Optional.of(subscriptionChangeHandler));
        assertTrue(subscriber.hasSubscriptionChangeHandler(TOPIC));

        // WHEN unsubscribing from the topic
        var attempt = subscriber.unsubscribe(TOPIC, listener);

        // THEN the the attempt fails
        var exception = assertThrows(
            CompletionException.class,
            () -> attempt.toCompletableFuture().join());
        assertEquals(UCode.UNAVAILABLE, ((UStatusException) exception.getCause()).getCode());
        verify(subscriptionClient).unsubscribe(argThat(req -> req.getTopic().equals(TOPIC)));
        verify(transport, never()).unregisterListener(TOPIC, listener);
        // AND the subscription change handler is still registered
        assertTrue(subscriber.hasSubscriptionChangeHandler(TOPIC));
    }

    @Test
    void testUnsubscribeSucceeds() {
        // GIVEN a USubscription client
        // that succeeds to unsubscribe from topics
        when(subscriptionClient.unsubscribe(any(UnsubscribeRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(UnsubscribeResponse.newBuilder().build()));

        // and a transport
        // which succeeds to unregister listeners
        // (no need to stub explicitly, because this is the default)

        // AND a Subscriber using that USubscription client, Notifier and transport
        var subscriber = new InMemorySubscriber(transport, subscriptionClient, notifier);
        verify(notifier).registerNotificationListener(
            eq(SUBSCRIPTION_NOTIFICATION_TOPIC_URI),
            notificationListener.capture());
        // which already has a listener and corresponding subscription change handler
        // registered for a topic
        subscriber.addSubscriptionChangeHandler(TOPIC, Optional.of(subscriptionChangeHandler));
        assertTrue(subscriber.hasSubscriptionChangeHandler(TOPIC));

        // WHEN unsubscribing from the topic
        var attempt = subscriber.unsubscribe(TOPIC, listener);

        // THEN the the attempt succeeds
        attempt.toCompletableFuture().join();
        verify(subscriptionClient).unsubscribe(argThat(req -> req.getTopic().equals(TOPIC)));
        verify(transport).unregisterListener(TOPIC, listener);
        // AND the subscription change handler is no longer registered
        assertFalse(subscriber.hasSubscriptionChangeHandler(TOPIC));
    }

    @Test
    void testUnsubscribeSucceedsOnSecondAttempt() {
        // GIVEN a USubscription client
        // that succeeds to unsubscribe from topics
        when(subscriptionClient.unsubscribe(any(UnsubscribeRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(UnsubscribeResponse.newBuilder().build()));

        // and a transport
        when(transport.unregisterListener(any(UUri.class), any(UListener.class)))
            // that first fails to unregister a listener
            .thenReturn(CompletableFuture.failedFuture(
                new UStatusException(UCode.UNAVAILABLE, "not connected")))
            // but succeeds on the second attempt
            .thenReturn(CompletableFuture.completedFuture(null));

        // AND a Subscriber using that USubscription client, Notifier and transport
        var subscriber = new InMemorySubscriber(transport, subscriptionClient, notifier);
        verify(notifier).registerNotificationListener(
            eq(SUBSCRIPTION_NOTIFICATION_TOPIC_URI),
            notificationListener.capture());
        // which already has a listener and corresponding subscription change handler
        // registered for a topic
        subscriber.addSubscriptionChangeHandler(TOPIC, Optional.of(subscriptionChangeHandler));
        assertTrue(subscriber.hasSubscriptionChangeHandler(TOPIC));

        // WHEN unsubscribing from a topic for which a listener had been registered before
        var attempt = subscriber.unsubscribe(TOPIC, listener);

        // THEN the first attempt fails
        var exception = assertThrows(
            CompletionException.class,
            () -> attempt.toCompletableFuture().join());
        assertEquals(UCode.UNAVAILABLE, ((UStatusException) exception.getCause()).getCode());

        // but the second attempt succeeds
        subscriber.unsubscribe(TOPIC, listener).toCompletableFuture().join();
        // AND the handler has been removed
        assertFalse(subscriber.hasSubscriptionChangeHandler(TOPIC));

        verify(subscriptionClient, times(2)).unsubscribe(argThat(req -> req.getTopic().equals(TOPIC)));
        verify(transport, times(2)).unregisterListener(TOPIC, listener);
    }

    @Test
    void testHandleSubscriptionChangeNotificationHandlesInvalidMessages() {
        // GIVEN a Subscriber
        var subscriber = new InMemorySubscriber(transport, subscriptionClient, notifier);
        verify(notifier).registerNotificationListener(
            eq(SUBSCRIPTION_NOTIFICATION_TOPIC_URI),
            notificationListener.capture());

        @SuppressWarnings("unchecked")
        Consumer<UMessage> unexpectedMessageHandler = mock(Consumer.class);
        subscriber.setUnexpectedMessageHandler(unexpectedMessageHandler);

        // WHEN a non-notification message is received
        var malformedPublishMessage = UMessage.newBuilder()
            .setAttributes(UAttributes.newBuilder()
                .setId(UuidFactory.create())
                .setType(UMessageType.UMESSAGE_TYPE_PUBLISH)
                .setSource(SUBSCRIPTION_NOTIFICATION_TOPIC_URI)
                .setSink(SOURCE)
                .build())
            .build();
        notificationListener.getValue().onReceive(malformedPublishMessage);

        // THEN the unexpected message handler is invoked
        verify(unexpectedMessageHandler).accept(malformedPublishMessage);

        // AND when a notification message is received for which no
        // handler had been registered
        var subscriptionChange = Update.newBuilder().setTopic(TOPIC).build();
        var notificationMessage = UMessageBuilder.notification(
                SUBSCRIPTION_NOTIFICATION_TOPIC_URI,
                SOURCE)
            .build(UPayload.pack(subscriptionChange));

        notificationListener.getValue().onReceive(notificationMessage);

        // THEN the unexpected message handler is invoked
        verify(unexpectedMessageHandler).accept(notificationMessage);
    }

    @Test
    void testHandleSubscriptionChangeNotificationIgnoresErroneousHandler() {
        // GIVEN a Subscriber
        var subscriber = new InMemorySubscriber(transport, subscriptionClient, notifier);
        verify(notifier).registerNotificationListener(
            eq(SUBSCRIPTION_NOTIFICATION_TOPIC_URI),
            notificationListener.capture());

        // with a subscription change handler registered for a topic
        subscriber.addSubscriptionChangeHandler(TOPIC, Optional.of(subscriptionChangeHandler));

        // WHEN the handler for an incoming subscription update notification throws an exception
        doThrow(new RuntimeException("boom"))
            .when(subscriptionChangeHandler).handleSubscriptionChange(
                eq(TOPIC), any(SubscriptionStatus.class));
        var subscriptionChange = Update.newBuilder().setTopic(TOPIC).build();
        var notificationMessage = UMessageBuilder.notification(
                SUBSCRIPTION_NOTIFICATION_TOPIC_URI,
                SOURCE)
            .build(UPayload.pack(subscriptionChange));

        // THEN the exception is caught and ignored
        assertDoesNotThrow(() -> notificationListener.getValue().onReceive(notificationMessage));
    }

    @Test
    void testRegisterSubscriptionChangeHandlerSucceeds() {
        // GIVEN a USubscription client that succeeds to register for notifications
        when(subscriptionClient.registerForNotifications(any(NotificationsRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(NotificationsResponse.getDefaultInstance()));
        // AND a subscriber using the client
        var subscriber = new InMemorySubscriber(transport, subscriptionClient, notifier);
        verify(notifier).registerNotificationListener(
            eq(SUBSCRIPTION_NOTIFICATION_TOPIC_URI),
            notificationListener.capture());

        // WHEN a subscription change handler is registered for a topic
        subscriber.registerSubscriptionChangeHandler(TOPIC, subscriptionChangeHandler)
            .toCompletableFuture().join();
        verify(subscriptionClient).registerForNotifications(argThat(req -> req.getTopic().equals(TOPIC)));

        // THEN the handler is getting invoked whenever a subscription change
        // for the topic of interest is received
        var subscriptionChange = Update.newBuilder()
            .setTopic(TOPIC)
            .setStatus(SubscriptionStatus.newBuilder().setState(State.UNSUBSCRIBED))
            .build();
        var notificationMessage = UMessageBuilder.notification(
                SUBSCRIPTION_NOTIFICATION_TOPIC_URI,
                SOURCE)
            .build(UPayload.pack(subscriptionChange));
        notificationListener.getValue().onReceive(notificationMessage);
        verify(subscriptionChangeHandler).handleSubscriptionChange(TOPIC, subscriptionChange.getStatus());
    }

    @Test
    void testUnregisterSubscriptionChangeHandlerSucceeds() {
        // GIVEN a USubscription client that succeeds to unregister for notifications
        when(subscriptionClient.unregisterForNotifications(any(NotificationsRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(NotificationsResponse.getDefaultInstance()));
        // AND a subscriber using the client
        var subscriber = new InMemorySubscriber(transport, subscriptionClient, notifier);
        verify(notifier).registerNotificationListener(
            eq(SUBSCRIPTION_NOTIFICATION_TOPIC_URI),
            notificationListener.capture());
        // that has a handler registered for subscription changes
        subscriber.addSubscriptionChangeHandler(TOPIC, Optional.of(subscriptionChangeHandler));

        // WHEN the handler is getting unregistered
        subscriber.unregisterSubscriptionChangeHandler(TOPIC)
            .toCompletableFuture().join();
        verify(subscriptionClient).unregisterForNotifications(argThat(req -> req.getTopic().equals(TOPIC)));

        // THEN the handler is no longer getting invoked when a subscription change
        // for the topic of interest is received
        var subscriptionChange = Update.newBuilder()
            .setTopic(TOPIC)
            .setStatus(SubscriptionStatus.newBuilder().setState(State.UNSUBSCRIBED))
            .build();
        var notificationMessage = UMessageBuilder.notification(
                SUBSCRIPTION_NOTIFICATION_TOPIC_URI,
                SOURCE)
            .build(UPayload.pack(subscriptionChange));
        notificationListener.getValue().onReceive(notificationMessage);
        verify(subscriptionChangeHandler, never()).handleSubscriptionChange(eq(TOPIC), any(SubscriptionStatus.class));
    }
}
