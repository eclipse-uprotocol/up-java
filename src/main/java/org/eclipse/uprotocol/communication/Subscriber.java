/**
 * SPDX-FileCopyrightText: 2025 Contributors to the Eclipse Foundation
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
import java.util.concurrent.CompletionStage;

import org.eclipse.uprotocol.core.usubscription.v3.NotificationsResponse;
import org.eclipse.uprotocol.core.usubscription.v3.SubscriptionResponse;
import org.eclipse.uprotocol.core.usubscription.v3.SubscriptionStatus;
import org.eclipse.uprotocol.core.usubscription.v3.UnsubscribeResponse;
import org.eclipse.uprotocol.transport.UListener;
import org.eclipse.uprotocol.v1.UUri;

/**
 * A client for subscribing to topics.
 *
 * @see <a href="https://github.com/eclipse-uprotocol/up-spec/blob/v1.6.0-alpha.7/up-l2/api.adoc">
 * Communication Layer API Specifications</a>
 */
// [impl->dsn~communication-layer-api-declaration~1]
public interface Subscriber {
    /**
     * Registers a handler to invoke for messages that have been published to a given topic.
     * <p>
     * More than one handler can be registered for the same topic.
     * The same handler can be registered for multiple topics.
     *
     * @param topic The topic to subscribe to. The topic must not contain any wildcards.
     * @param handler The handler to invoke for each message that has been published to the topic.
     * @param subscriptionChangeHandler A handler to invoke for any subscription state changes for
     * the given topic, like a transition from {@link SubscriptionStatus.State#SUBSCRIBE_PENDING} to
     * {@link SubscriptionStatus.State#SUBSCRIBED} that occurs when the client subscribes to a
     * <em>remote</em> topic for which no other local subscribers exist yet.
     * @return The outcome of the operation. The stage will be failed with a {@link UStatusException}
     * if subscribing to the topic failed.
     * @throws NullPointerException if any of the arguments are {@code null}.
     */
    CompletionStage<SubscriptionResponse> subscribe(
        UUri topic,
        UListener handler,
        Optional<SubscriptionChangeHandler> subscriptionChangeHandler);

    /**
     * Deregisters a previously {@link #subscribe(UUri, UListener, Optional) registered handler}.
     *
     * @param topic The topic that the handler had been registered for.
     * @param handler The handler to unregister.
     * @return The outcome of the operation. The stage will be failed with a {@link UStatusException}
     * if unsubscribing from the topic failed.
     * @throws NullPointerException if any of the arguments are {@code null}.
     */
    CompletionStage<UnsubscribeResponse> unsubscribe(UUri topic, UListener handler);

    /**
     * Registers a handler for receiving subscription change notifications for a topic.
     * <p>
     * This method can be used by event producers to get notified about other uEntities'
     * attempts to subscribe to topics that they publish to.
     *
     * @param topic The topic to get subscription change notifications for.
     * @param handler The handler to invoke for subscription changes.
     * @return The outcome of the operation. The stage will be failed with a {@link UStatusException}
     * if unsubscribing from the topic failed.
     * @throws NullPointerException if any of the arguments are {@code null}.
     */
    CompletionStage<NotificationsResponse> registerSubscriptionChangeHandler(
        UUri topic,
        SubscriptionChangeHandler handler);

    /**
     * Unregisters a {@link #registerSubscriptionChangeHandler(UUri, SubscriptionChangeHandler) previously registered}
     * subscription change handler.
     *
     * @param topic The topic that the handler had been registered for.
     * @return The outcome of the operation. The stage will be failed with a {@link UStatusException}
     * if unsubscribing from the topic failed.
     * @throws NullPointerException if topic is {@code null}.
     */
    CompletionStage<NotificationsResponse> unregisterSubscriptionChangeHandler(UUri topic);
}
