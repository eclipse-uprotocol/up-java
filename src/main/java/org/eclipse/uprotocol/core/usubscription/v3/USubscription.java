/*
 * Copyright (c) 2023 General Motors GTO LLC
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * SPDX-FileType: SOURCE
 * SPDX-FileCopyrightText: 2023 General Motors GTO LLC
 * SPDX-License-Identifier: Apache-2.0
 */
package org.eclipse.uprotocol.core.usubscription.v3;

import java.util.concurrent.CompletableFuture;

import org.eclipse.uprotocol.v1.*;

public interface USubscription {
    
    // A consumer (application) calls this API to subscribe to a topic.
    // What is passed is the SubscriptionRequest message containing the topic, the
    // subscriber's name, and any Subscription Attributes. This API returns a
    // SubscriptionResponse message containing the status of the request along with
    // any event delivery configuration
    // required to consume the event. Calling this API also registers the subscriber
    // to received subscription change notifications if ever the subscription state
    // changes.
    CompletableFuture<SubscriptionResponse> subscribe(SubscriptionRequest request);
  
    
    // The consumer no longer wishes to subscribe to a topic so it issues an
    // explicit unsubscribe request.
    CompletableFuture<UStatus> unsubscribe(UnsubscribeRequest request);

    // Fetch a list of subscriptions
    CompletableFuture<FetchSubscriptionsResponse> fetchSubscriptions(FetchSubscriptionsRequest request);


    //  API called by producers to register a topic. This API
    // informs the Subscription Service that to create the topic and it is ready to publish.
    CompletableFuture<UStatus> createTopic(CreateTopicRequest request);

    // Request deprecation of a topic. Producers call this to inform the uSubscription
    // that it will no longer produce to said topic. The topic is flagged as deprcated
    // which
    CompletableFuture<UStatus> deprecateTopic(DeprecateTopicRequest request);


    // Register to receive subscription change notifications that are published on the
    // 'up:/core.usubscription/3/subscriptions#Update'
    CompletableFuture<UStatus> registerForNotifications(NotificationsRequest request);

    // Unregister for subscription change events
    CompletableFuture<UStatus> unregisterForNotifications(NotificationsRequest request);

    // Fetch a list of subscribers that are currently subscribed to a given topic.
    CompletableFuture<FetchSubscribersResponse> fetchSubscribers(FetchSubscribersRequest request);

    // Reset subscriptions to and from the uSubscription Service. 
    // This API is used between uSubscription services in order to flush and 
    // reestablish subscriptions between devices. A uSubscription service might 
    // ned to call this API if its database is flushed or corrupted (ex. factory
    // reset).
    // **__NOTE:__** This is a private API only for uSubscription services,
    // uEs can call Unsubscribe() to flush their own subscriptions.
    CompletableFuture<UStatus> reset(ResetRequest request);
}
