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

import java.util.concurrent.CompletionStage;

import org.eclipse.uprotocol.communication.Notifier;
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
import org.eclipse.uprotocol.core.usubscription.v3.UnsubscribeRequest;
import org.eclipse.uprotocol.core.usubscription.v3.UnsubscribeResponse;
import org.eclipse.uprotocol.v1.UUri;

/**
 * The client-side interface for interacting with a USubscription service instance.
 *
 * @see <a href="https://github.com/eclipse-uprotocol/up-spec/tree/v1.6.0-alpha.7/up-l3/usubscription/v3">
 * USubscription service specification</a>
 */
public interface USubscriptionClient {

    /**
     * Gets the topic that the USubscription service instance uses for sending subscription change notifications.
     * <p>
     * Clients can use this topic to register a listener for these notifications using
     * {@link Notifier#registerNotificationListener(UUri, org.eclipse.uprotocol.transport.UListener)}.
     *
     * @return The topic.
     */
    UUri getSubscriptionServiceNotificationTopic();

    /**
     * Subscribes to a given topic.
     *
     * @param request The request to send.
     * @return The outcome of the operation. The stage will be completed with a {@link UStatusException}
     * if the request has failed.
     * @throws NullPointerException if request is {@code null}.
     */
    CompletionStage<SubscriptionResponse> subscribe(SubscriptionRequest request);

    /**
     * Unsubscribes from a given topic.
     *
     * @param request The request to send.
     * @return The outcome of the operation. The stage will be completed with a {@link UStatusException}
     * if the request has failed.
     * @throws NullPointerException if request is {@code null}.
     */
    CompletionStage<UnsubscribeResponse> unsubscribe(UnsubscribeRequest request);

    /**
     * Fetches a list of subscribers that are currently subscribed to a given topic.
     * 
     * @param request The request to send.
     * @return The outcome of the operation. The stage will be completed with a {@link UStatusException}
     * if the request has failed.
     * @throws NullPointerException if request is {@code null}.
     */
    CompletionStage<FetchSubscribersResponse> fetchSubscribers(FetchSubscribersRequest request);

    /**
     * Fetches all subscriptions for a given topic or subscriber.
     * <p>
     * API provides more information than {@code #fetchSubscribers(UUri)} in that it also returns  
     * {@link SubscribeAttributes} per subscriber that might be useful to the producer to know.
     * 
     * @param request The request to send.
     * @return The outcome of the operation. The stage will be completed with a {@link UStatusException}
     * if the request has failed.
     * @throws NullPointerException if request is {@code null}.
     */
    CompletionStage<FetchSubscriptionsResponse> fetchSubscriptions(FetchSubscriptionsRequest request);

    /**
     * Registers for notifications about changes to the subscription status for a given topic.
     * <p>
     * This API allows producers to register to receive subscription change notifications for
     * topics that they produce only. 
     * 
     * NOTE: Subscribers are automatically registered to receive notifications when they call
     * {@link #subscribe(SubscriptionRequest)}.
     * 
     * @param request The request to send.
     * @return The outcome of the operation. The stage will be completed with a {@link UStatusException}
     * if the request has failed.
     * @throws NullPointerException if request is {@code null}.
     */
    CompletionStage<NotificationsResponse> registerForNotifications(NotificationsRequest request);

    /**
     * Cancels a registration for subscription change notifications.
     * 
     * @param request The request to send.
     * @return The outcome of the operation. The stage will be completed with a {@link UStatusException}
     * if the request has failed.
     * @throws NullPointerException if request is {@code null}.
     */
    CompletionStage<NotificationsResponse> unregisterForNotifications(NotificationsRequest request);
}
