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
import org.eclipse.uprotocol.transport.UTransport;
import org.eclipse.uprotocol.transport.builder.UMessageBuilder;
import org.eclipse.uprotocol.uri.validator.UriValidator;
import org.eclipse.uprotocol.v1.UCode;

/**
 * A Publisher that uses the uProtocol Transport Layer API for publishing events to topics.
 * <p>
 * <em>NOTE:</em> Developers are not required to use these APIs, they can implement their own
 *  or directly use the {@link UTransport} to send notifications and register listeners.
 */
public class SimplePublisher extends AbstractCommunicationLayerClient implements Publisher {

    /**
     * Creates a new publisher for a transport.
     *
     * @param transport the transport to use for sending the notifications
     * @param uriProvider the URI provider to use for creating local resource URIs
     */
    public SimplePublisher(UTransport transport, LocalUriProvider uriProvider) {
        super(transport, uriProvider);
    }

    @Override
    public CompletionStage<Void> publish(int resourceId, CallOptions options, UPayload payload) {
        Objects.requireNonNull(options);
        Objects.requireNonNull(payload);
        final var topic = getUriProvider().getResource(resourceId);
        if (!UriValidator.isTopic(topic)) {
            return CompletableFuture.failedFuture(new UStatusException(
                UCode.INVALID_ARGUMENT,
                "Resource ID does not map to a valid topic URI"));
        }
        UMessageBuilder builder = UMessageBuilder.publish(topic);
        options.applyToMessage(builder);
        return getTransport().send(builder.build(payload));
    }
}
