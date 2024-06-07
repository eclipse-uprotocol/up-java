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

public class DefaultPublisher implements Publisher {
    // The transport to use for sending the RPC requests
    private final UTransport transport;

    /**
     * Constructor for the DefaultPublisher.
     * 
     * @param transport the transport to use for sending the notifications
     */
    public DefaultPublisher (UTransport transport) {
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
