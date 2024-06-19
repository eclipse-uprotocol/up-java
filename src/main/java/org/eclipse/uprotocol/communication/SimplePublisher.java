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
import org.eclipse.uprotocol.transport.UTransport;
import org.eclipse.uprotocol.transport.builder.UMessageBuilder;
import org.eclipse.uprotocol.v1.UStatus;
import org.eclipse.uprotocol.v1.UUri;

/**
 * The following is an example implementation of the {@link Publisher} interface that
 * wraps the {@link UTransport} for implementing the notification pattern to send 
 * notifications.
 * 
 * *NOTE:* Developers are not required to use these APIs, they can implement their own
 *  or directly use the {@link UTransport} to send notifications and register listeners.
 */
public class SimplePublisher implements Publisher {
    // The transport to use for sending the RPC requests
    private final UTransport transport;

    /**
     * Constructor for the DefaultPublisher.
     * 
     * @param transport the transport to use for sending the notifications
     */
    public SimplePublisher (UTransport transport) {
        Objects.requireNonNull(transport, UTransport.TRANSPORT_NULL_ERROR);
        this.transport = transport;
    }

    /**
     * Publish a message to a topic passing {@link UPayload} as the payload.
     * 
     * @param topic The topic to publish to.
     * @param payload The {@link UPayload} to publish.
     * @return
     */
    @Override
    public UStatus publish(UUri topic, UPayload payload) {
        Objects.requireNonNull(topic, "Publish topic missing");
        UMessageBuilder builder = UMessageBuilder.publish(topic);

        return transport.send(builder.build(payload));
    }
}
