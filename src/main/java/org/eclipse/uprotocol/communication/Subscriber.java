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
import org.eclipse.uprotocol.transport.UListener;
import org.eclipse.uprotocol.core.usubscription.v3.SubscriptionResponse;
import org.eclipse.uprotocol.v1.UStatus;
import org.eclipse.uprotocol.v1.UUri;


/**
 * Communication Layer (uP-L2) Subscriber interface.<BR>
 * 
 * This interface provides APIs to subscribe and unsubscribe to a given topic.
 */
public interface Subscriber {

    /**
     * Subscribe to a given topic. <br>
     * 
     * The API will return a {@link CompletionStage} with the response {@link SubscriptionResponse} or exception
     * with the failure if the subscription was not successful. The API will also register the listener to be
     * called when messages are received.
     * 
     * @param topic The topic to subscribe to.
     * @param listener The listener to be called when a message is received on the topic.
     * @param options The call options for the subscription.
     * @return Returns the CompletionStage with the response UMessage or exception with the failure
     * reason as {@link UStatus}.
     */
    CompletionStage<SubscriptionResponse> subscribe(UUri topic, UListener listener, CallOptions options);


    /**
     * Unsubscribe to a given topic. <br>
     * 
     * The subscriber no longer wishes to be subscribed to said topic so we issue a unsubscribe
     * request to the USubscription service.
     * 
     * @param topic The topic to unsubscribe to.
     * @param listener The listener to be called when a message is received on the topic.
     * @param options The call options for the subscription.
     * @return Returns {@link UStatus} with the result from the unsubscribe request.
     */
    UStatus unsubscribe(UUri topic, UListener listener, CallOptions options);


    /**
     * Unregister a listener from a topic. <br>
     * 
     * This method will only unregister the listener for a given subscription thus allowing a uE to stay
     * subscribed even if the listener is removed.
     * 
     * @param topic The topic to subscribe to.
     * @param listener The listener to be called when a message is received on the topic.
     * @return Returns {@link UStatus} with the status of the listener unregister request.
     */
    UStatus unregisterListener(UUri topic, UListener listener);
}