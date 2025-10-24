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

import java.util.concurrent.CompletionStage;

import org.eclipse.uprotocol.v1.UUri;

import org.eclipse.uprotocol.transport.UListener;

/**
 * A client for sending Notification messages to a uEntity.
 *
 * @see <a href="https://github.com/eclipse-uprotocol/up-spec/blob/v1.6.0-alpha.7/up-l2/api.adoc">
 * Communication Layer API Specifications</a>
 */
// [impl->dsn~communication-layer-api-declaration~1]
public interface Notifier {

    /**
     * Sends a notification to a uEntity.
     * <p>
     * This default implementation invokes {@link #notify(int, UUri, CallOptions, UPayload)} with the
     * given resource ID, destination, {@link CallOptions#DEFAULT default options} and an
     * {@link UPayload#EMPTY empty payload}.
     *
     * @param resourceId The (local) resource identifier representing the origin of the notification.
     * @param destination A URI representing the uEntity that the notification should be sent to.
     * @return The outcome of the operation. The stage will be failed with a {@link UStatusException}
     * if the notification could not be sent.
     * @throws NullPointerException if any of the arguments are {@code null}.
     */
    default CompletionStage<Void> notify(int resourceId, UUri destination) {
        return notify(resourceId, destination, CallOptions.DEFAULT, UPayload.EMPTY);
    }

    /**
     * Sends a notification to a uEntity.
     * <p>
     * This default implementation invokes {@link #notify(int, UUri, CallOptions, UPayload)} with the
     * given resource ID, destination, options and an {@link UPayload#EMPTY empty payload}.
     *
     * @param resourceId The (local) resource identifier representing the origin of the notification.
     * @param destination The destination to send the notification to.
     * @param options Options to include in the notification message. {@link CallOptions#DEFAULT} can
     * be used for default options.
     * @return The outcome of the operation. The stage will be failed with a {@link UStatusException}
     * if the notification could not be sent.
     * @throws NullPointerException if any of the arguments are {@code null}.
     */
    default CompletionStage<Void> notify(int resourceId, UUri destination, CallOptions options) {
        return notify(resourceId, destination, options, UPayload.EMPTY);
    }

    /**
     * Sends a notification to a uEntity.
     * <p>
     * This default implementation invokes {@link #notify(int, UUri, CallOptions, UPayload)} with the
     * given resource ID, destination, payload and {@link CallOptions#DEFAULT default options}.
     *
     * @param resourceId The (local) resource identifier representing the origin of the notification.
     * @param destination The destination to send the notification to.
     * @param payload The payload to include in the notification message. {@link UPayload#EMPTY}
     * can be used if the notification has no payload.
     * @return The outcome of the operation. The stage will be failed with a {@link UStatusException}
     * if the notification could not be sent.
     * @throws NullPointerException if any of the arguments are {@code null}.
     */
    default CompletionStage<Void> notify(int resourceId, UUri destination, UPayload payload) {
        return notify(resourceId, destination, CallOptions.DEFAULT, payload);
    }

    /**
     * Sends a notification to a uEntity.
     *
     * @param resourceId The (local) resource identifier representing the origin of the notification.
     * @param destination A URI representing the uEntity that the notification should be sent to.
     * @param options Options to include in the notification message. {@link CallOptions#DEFAULT} can
     * be used for default options.
     * @param payload The payload to include in the notification message. {@link UPayload#EMPTY}
     * can be used if the notification has no payload.
     * @return The outcome of the operation. The stage will be failed with a {@link UStatusException}
     * if the notification could not be sent.
     * @throws NullPointerException if any of the arguments are {@code null}.
     */
    CompletionStage<Void> notify(int resourceId, UUri destination, CallOptions options, UPayload payload);

    /**
     * Starts listening to a notification topic.
     * <p>
     * More than one handler can be registered for the same topic.
     * The same handler can be registered for multiple topics.
     *
     * @param topic The topic to listen to. The topic must not contain any wildcards.
     * @param listener The handler to invoke for each notification that has been sent on the topic.
     * @return The outcome of the operation. The stage will be failed with a {@link UStatusException}
     * if the listener could not be registered.
     * @throws NullPointerException if any of the arguments are {@code null}.
     */
    CompletionStage<Void> registerNotificationListener(UUri topic, UListener listener);

    /**
     * Unregisters a previously {@link #registerNotificationListener(UUri, UListener) registered handler}
     * for listening to notifications.
     *
     * @param topic The topic that the handler had been registered for.
     * @param listener The listener to unregister.
     * @return The outcome of the operation. The stage will be failed with a {@link UStatusException}
     * if the listener could not be unregistered.
     * @throws NullPointerException if any of the arguments are {@code null}.
     */
    CompletionStage<Void> unregisterNotificationListener(UUri topic, UListener listener);
}
