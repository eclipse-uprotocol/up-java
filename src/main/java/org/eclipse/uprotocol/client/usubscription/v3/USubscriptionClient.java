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
package org.eclipse.uprotocol.client.usubscription.v3;

import java.util.concurrent.CompletionStage;
import org.eclipse.uprotocol.core.usubscription.v3.FetchSubscribersResponse;
import org.eclipse.uprotocol.core.usubscription.v3.FetchSubscriptionsRequest;
import org.eclipse.uprotocol.core.usubscription.v3.FetchSubscriptionsResponse;
import org.eclipse.uprotocol.core.usubscription.v3.NotificationsResponse;
import org.eclipse.uprotocol.core.usubscription.v3.SubscriptionResponse;
import org.eclipse.uprotocol.core.usubscription.v3.SubscriptionStatus;
import org.eclipse.uprotocol.transport.UListener;
import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UStatus;
import org.eclipse.uprotocol.v1.UUri;

/**
 * The Client-side interface for talking to usubscription service and to 
 * implement the subscriber pattern. 
 */
public interface USubscriptionClient {

    /**
     * Subscribes to a given topic.
     * 
     * The API will return a {@link CompletionStage} with the response {@link SubscriptionResponse} or exception
     * with the failure if the subscription was not successful. 
     * 
     * @param topic The topic to subscribe to.
     * @param listener The listener to be called when a message is received on the topic.
     * @return Returns the CompletionStage with {@link SubscriptionResponse} or exception with the failure.
     */
    default CompletionStage<SubscriptionResponse> subscribe(UUri topic, UListener listener) {
        return subscribe(topic, listener, null);
    }


    /**
     * Subscribes to a given topic.
     * 
     * The API will return a {@link CompletionStage} with the response {@link SubscriptionResponse} or exception
     * with the failure if the subscription was not successful. The optional passed {@link SubscriptionChangeHandler}
     * is used to receive notifications of changes to the subscription status like a transition from
     * {@link SubscriptionStatus.State.SUBSCRIBE_PENDING} to {@link SubscriptionStatus.State.SUBSCRIBED} that
     * occurs when we subscribe to remote topics that the device we are on has not yet a subscriber that has
     * subscribed to said topic. 
     * 
     * @param topic The topic to subscribe to.
     * @param listener The listener to be called when a message is received on the topic.
     * @param handler {@link SubscriptionChangeHandler} to handle changes to subscription states.
     * @return Returns the CompletionStage with {@link SubscriptionResponse} or exception with the failure
     * reason as {@link UStatus}. {@link UCode.ALREADY_EXISTS} will be returned if you call this API multiple
     * times passing a different handler. 
     */
    CompletionStage<SubscriptionResponse> subscribe(UUri topic, UListener listener, 
        SubscriptionChangeHandler handler);


    /**
     * Unsubscribes from a given topic.
     * 
     * The subscriber no longer wishes to be subscribed to said topic so we issue a unsubscribe
     * request to the USubscription service. The API will return a {@link CompletionStage} with the
     * {@link UStatus} of the result. If we are unable to unsubscribe to the topic with USubscription
     * service, the listener and handler (if any) will remain registered.
     * 
     * @param topic The topic to unsubscribe to.
     * @param listener The listener to be called when a message is received on the topic.
     * @return Returns {@link UStatus} with the result from the unsubscribe request.
     */
    CompletionStage<UStatus> unsubscribe(UUri topic, UListener listener);

    /**
     * Unregisters a listener and removes any registered {@link SubscriptionChangeHandler} for the topic.
     * 
     * This method is used to remove handlers/listeners without notifying the uSubscription service 
     * so that we can be persistently subscribed even when the uE is not running.
     * 
     * @param topic The topic to subscribe to.
     * @param listener The listener to be called when a message is received on the topic.
     * @return Returns {@link UStatus} with the status of the listener unregister request.
     */
    CompletionStage<UStatus> unregisterListener(UUri topic, UListener listener);


    /**
     * Register for Subscription Change Notifications.
     * 
     * This API allows producers to register to receive subscription change notifications for
     * topics that they produce only.
     * 
     * @param topic The topic to register for notifications.
     * @param handler The {@link SubscriptionChangeHandler} to handle the subscription changes.
     * @return {@link CompletionStage} completed successfully if uSubscription service accepts the
     *         request to register the caller to be notified of subscription changes, or 
     *         the CompletionStage completes exceptionally with {@link UStatus} that indicates
     *         the failure reason. {@link UCode.PERMISSION_DENIED} is returned if the topic ue_id does 
     *         not equal the callers ue_id. 
     */
    CompletionStage<NotificationsResponse> registerForNotifications(UUri topic, SubscriptionChangeHandler handler);


    /**
     * Unregister for subscription change notifications.
     * 
     * @param topic The topic to unregister for notifications.
     * @param handler The {@link SubscriptionChangeHandler} to handle the subscription changes.
     * @return {@link CompletionStage} completed successfully with {@link NotificationResponse} with
     *         the status of the API call to uSubscription service, or completed unsuccessfully with
     *         {@link UStatus} with the reason for the failure. {@link UCode.PERMISSION_DENIED} is
     *         returned if the topic ue_id does not equal the callers ue_id. 
     */
    CompletionStage<NotificationsResponse> unregisterForNotifications(UUri topic, SubscriptionChangeHandler handler);


    /**
     * Fetch subscribers to a given topic.
     * 
     * API is used by producers to find out the list of subscribers (their URI) for a a given topic
     * that I produce.
     * 
     * @param topic The topic to fetch the subscribers for.
     * @return {@link CompletionStage} completed successfully with {@link FetchSubscribersResponse} with
     *         the list of subscribers, or completed unsuccessfully with {@link UStatus} with the reason
     *         for the failure. {@link UCode.PERMISSION_DENIED} is returned if the topic ue_id does 
     *         not equal the callers ue_id. 
     */
    CompletionStage<FetchSubscribersResponse> fetchSubscribers(UUri topic);


    /**
     * Fetch list of Subscriptions for a given topic. 
     * 
     * API provides more information than {@code fetchSubscribers()} in that it also returns  
     * {@link SubscribeAttributes} per subscriber that might be useful to the producer to know.
     * 
     * @param topic The topic to fetch subscriptions for.
     * @return {@link CompletionStage} completed successfully with {@link FetchSubscriptionsResponse} that
     *         contains the subscription information per subscriber to the topic or completed unsuccessfully with
     *      {@link UStatus} with the reason for the failure. {@link UCode.PERMISSION_DENIED} is returned if the
     *      topic ue_id does not equal the callers ue_id. 
     */
    CompletionStage<FetchSubscriptionsResponse> fetchSubscriptions(FetchSubscriptionsRequest request);
}
