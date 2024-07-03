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
import java.util.concurrent.CompletionStage;

import org.eclipse.uprotocol.transport.UListener;
import org.eclipse.uprotocol.transport.UTransport;
import org.eclipse.uprotocol.transport.builder.UMessageBuilder;
import org.eclipse.uprotocol.v1.UStatus;
import org.eclipse.uprotocol.v1.UUri;

/**
 * The following is an example implementation of the {@link Notifier} interface that
 * wraps the {@link UTransport} for implementing the notification pattern to send 
 * notifications and register to receive notification events.
 * 
 * *NOTE:* Developers are not required to use these APIs, they can implement their own
 *  or directly use the {@link UTransport} to send notifications and register listeners.
 */
public class SimpleNotifier  implements Notifier {
    // The transport to use for sending the RPC requests
    private final UTransport transport;

    /**
     * Constructor for the DefaultNotifier.
     * 
     * @param transport the transport to use for sending the notifications
     */
    public SimpleNotifier (UTransport transport) {
        Objects.requireNonNull(transport, UTransport.TRANSPORT_NULL_ERROR);
        this.transport = transport;
    }


    /**
     * Send a notification to a given topic. <br>
     * 
     * @param topic The topic to send the notification to.
     * @param destination The destination to send the notification to.
     * @param payload The payload to send with the notification.
     * @return Returns the {@link UStatus} with the status of the notification.
     */
    @Override
    public CompletionStage<UStatus> notify(UUri topic, UUri destination, UPayload payload) {
        UMessageBuilder builder = UMessageBuilder.notification(topic, destination);
        return transport.send((payload == null) ? builder.build() : 
                builder.build(payload));
    }


    /**
     * Register a listener for a notification topic. <br>
     * 
     * @param topic The topic to register the listener to.
     * @param listener The listener to be called when a message is received on the topic.
     * @return Returns the {@link UStatus} with the status of the listener registration.
     */
    @Override
    public CompletionStage<UStatus> registerNotificationListener(UUri topic, UListener listener) {
        return transport.registerListener(topic, transport.getSource(), listener);
    }


    /**
     * Unregister a listener from a notification topic. <br>
     * 
     * @param topic The topic to unregister the listener from.
     * @param listener The listener to be unregistered from the topic.
     * @return Returns the {@link UStatus} with the status of the listener that was unregistered.
     */
    @Override
    public CompletionStage<UStatus> unregisterNotificationListener(UUri topic, UListener listener) {
        return transport.unregisterListener(topic, transport.getSource(), listener);
    }
}
