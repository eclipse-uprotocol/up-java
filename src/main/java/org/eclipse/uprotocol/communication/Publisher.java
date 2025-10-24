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

/**
 * A client for publishing messages to a topic.
 *
 * @see <a href="https://github.com/eclipse-uprotocol/up-spec/blob/v1.6.0-alpha.7/up-l2/api.adoc">
 * Communication Layer API Specifications</a>
 */
// [impl->dsn~communication-layer-api-declaration~1]
public interface Publisher {
    /**
     * Publishes a message to a topic.
     * <p>
     * This default implementation invokes {@link #publish(int, CallOptions, UPayload)} with the
     * given resource ID, {@link CallOptions#DEFAULT default options} and an
     * {@link UPayload#EMPTY empty payload}.

     * @param resourceId The (local) resource ID of the topic to publish to.
     * @return The outcome of the operation. The stage will be failed with a {@link UStatusException}
     * if the message could not be published.
     * @throws NullPointerException if any of the arguments are {@code null}.
     */
    default CompletionStage<Void> publish(int resourceId) {
        return publish(resourceId, CallOptions.DEFAULT, UPayload.EMPTY);
    }

    /**
     * Publishes a message to a topic.
     * <p>
     * This default implementation invokes {@link #publish(int, CallOptions, UPayload)} with the
     * given resource ID, options and an {@link UPayload#EMPTY empty payload}.
     * 
     * @param resourceId The (local) resource ID of the topic to publish to.
     * @param options The {@link CallOptions} for the publish.
     * @return The outcome of the operation. The stage will be failed with a {@link UStatusException}
     * if the message could not be published.
     * @throws NullPointerException if any of the arguments are {@code null}.
     */
    default CompletionStage<Void> publish(int resourceId, CallOptions options) {
        return publish(resourceId, options, UPayload.EMPTY);
    }

    /**
     * Publishes a message to a topic.
     * <p>
     * This default implementation invokes {@link #publish(int, CallOptions, UPayload)} with the
     * given resource ID, payload and {@link CallOptions#DEFAULT default options}.
     *
     * @param resourceId The (local) resource ID of the topic to publish to.
     * @param payload The {@link UPayload} to publish.
     * @return The outcome of the operation. The stage will be failed with a {@link UStatusException}
     * if the message could not be published.
     * @throws NullPointerException if any of the arguments are {@code null}.
     */
    default CompletionStage<Void> publish(int resourceId, UPayload payload) {
        return publish(resourceId, CallOptions.DEFAULT, payload);
    }

    /**
     * Publishes a message to a topic.
     *
     * @param resourceId The (local) resource ID of the topic to publish to.
     * @param options Options to include in the published message. {@link CallOptions#DEFAULT} can
     * be used for default options.
     * @param payload Payload to include in the published message. {@link UPayload#EMPTY}
     * can be used if the message has no payload.
     * @return The outcome of the operation. The stage will be failed with a {@link UStatusException}
     * if the message could not be published.
     * @throws NullPointerException if any of the arguments are {@code null}.
     */
    CompletionStage<Void> publish(int resourceId, CallOptions options, UPayload payload);
}
