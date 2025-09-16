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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.eclipse.uprotocol.transport.LocalUriProvider;
import org.eclipse.uprotocol.transport.UListener;
import org.eclipse.uprotocol.transport.UTransport;
import org.eclipse.uprotocol.transport.builder.UMessageBuilder;
import org.eclipse.uprotocol.uri.validator.UriValidator;
import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UUri;

/**
 * A Notifier that uses the uProtocol Transport Layer API to send and receive
 * notifications to/from (other) uEntities.
 * <p>
 * <em>NOTE:</em> Developers are not required to use these APIs, they can implement
 * their own or directly use the {@link UTransport} to send notifications and register
 * listeners.
 */
// [impl->dsn~communication-layer-impl-default~1]
public class SimpleNotifier extends AbstractCommunicationLayerClient implements Notifier {

    /**
     * Creates a new notifier for a transport.
     * 
     * @param transport The transport to use for sending the notifications.
     * @param uriProvider The helper to use for creating local resource URIs.
     */
    public SimpleNotifier (UTransport transport, LocalUriProvider uriProvider) {
        super(transport, uriProvider);
    }

    @Override
    public CompletionStage<Void> notify(int resourceId, UUri destination, CallOptions options, UPayload payload) {
        Objects.requireNonNull(destination);
        Objects.requireNonNull(options);
        Objects.requireNonNull(payload);
        final var topic = getUriProvider().getResource(resourceId);
        if (!UriValidator.isTopic(topic)) {
            return CompletableFuture.failedFuture(new UStatusException(
                UCode.INVALID_ARGUMENT,
                "Resource ID does not map to a valid topic URI"));
        }
        UMessageBuilder builder = UMessageBuilder.notification(topic, destination);
        options.applyToMessage(builder);
        return getTransport().send(builder.build(payload));
    }


    @Override
    public CompletionStage<Void> registerNotificationListener(UUri topic, UListener listener) {
        return getTransport().registerListener(topic, getUriProvider().getSource(), listener);
    }

    @Override
    public CompletionStage<Void> unregisterNotificationListener(UUri topic, UListener listener) {
        return getTransport().unregisterListener(topic, getUriProvider().getSource(), listener);
    }
}
