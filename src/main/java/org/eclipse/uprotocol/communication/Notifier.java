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

import org.eclipse.uprotocol.v1.UStatus;
import org.eclipse.uprotocol.v1.UUri;

import org.eclipse.uprotocol.transport.UListener;

/**
 * Communication Layer (uP-L2) Notification Interface.<br>
 * 
 * Notifier is an interface that provides the APIs to send notifications (to a client) or 
 * register/unregister listeners to receive the notifications.
 */
public interface Notifier {

    /**
     * Send a notification to a given topic passing a payload. <br>
     * 
     * @param topic The topic to send the notification to.
     * @param destination The destination to send the notification to.
     * @param payload The payload to send with the notification.
     * @return Returns the {@link UStatus} with the status of the notification.
     */
    UStatus notify(UUri topic, UUri destination, UPayload payload);


    /**
     * Register a listener for a notification topic. <br>
     * 
     * @param topic The topic to register the listener to.
     * @param listener The listener to be called when a message is received on the topic.
     * @return Returns the {@link UStatus} with the status of the listener registration.
     */
    UStatus registerNotificationListener(UUri topic, UListener listener);


    /**
     * Unregister a listener from a notification topic. <br>
     * 
     * @param topic The topic to unregister the listener from.
     * @param listener The listener to be unregistered from the topic.
     * @return Returns the {@link UStatus} with the status of the listener that was unregistered.
     */
    UStatus unregisterNotificationListener(UUri topic, UListener listener);
}
