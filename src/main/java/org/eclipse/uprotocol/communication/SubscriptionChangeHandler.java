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

import org.eclipse.uprotocol.core.usubscription.v3.SubscriptionStatus;
import org.eclipse.uprotocol.v1.UUri;

/**
 * Communication Layer (uP-L2) Subscription Change Handler interface.<BR>
 * 
 * This interface provides APIs to handle subscription state changes for a given topic.
 */
// [impl->dsn~communication-layer-api-declaration~1]
public interface SubscriptionChangeHandler {
    /**
     * Method called to handle/process subscription state changes for a given topic.
     * 
     * @param topic The topic that the subscription state changed for.
     * @param status The new status of the subscription.
     */
    void handleSubscriptionChange(UUri topic, SubscriptionStatus status);
}
