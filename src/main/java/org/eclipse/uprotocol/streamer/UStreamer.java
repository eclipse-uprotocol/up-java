/*
 * Copyright (c) 2024 General Motors GTO LLC
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
 * 
 * SPDX-FileType: SOURCE
 * SPDX-FileCopyrightText: 2024 General Motors GTO LLC
 * SPDX-License-Identifier: Apache-2.0
 */
package org.eclipse.uprotocol.streamer;

import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UMessage;
import org.eclipse.uprotocol.v1.UStatus;
import org.eclipse.uprotocol.v1.UUri;
import org.eclipse.uprotocol.transport.UListener;

import org.eclipse.uprotocol.core.usubscription.v3.FetchSubscribersRequest;
import org.eclipse.uprotocol.core.usubscription.v3.FetchSubscriptionsRequest;
import org.eclipse.uprotocol.core.usubscription.v3.FetchSubscriptionsResponse;
import org.eclipse.uprotocol.core.usubscription.v3.NotificationsRequest;
import org.eclipse.uprotocol.core.usubscription.v3.SubscriberInfo;
import org.eclipse.uprotocol.core.usubscription.v3.USubscription;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** 
 * Example implementation of "Pluggable Streamer" written in Java.
 * 
 * The streamer provides APIs to simply add remove forwarding rules. A rule is defined as a 
 * combination of input and output routes. The input route is the route to listen to and the output route is the route to forward the message to.
 * The forwarding works by registering a listener on the input route and forwarding the message to the output route.
 */
public class UStreamer {

    // List of listeners (routes) that the streamer listens to
    private List<TransportListener> listeners;

    private USubscription submgr;

    // Listener to handle dynamic changes to subscriptions for anyone
    private class SubscriptionChangeListener implements UListener {

        @Override
        public void onReceive(UMessage message) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'onReceive'");
        }

    }

    /**
     * Constructor
     */
    public UStreamer(USubscription submgr) {
        Objects.requireNonNull(submgr, "summgr cannot be null");
        this.submgr = submgr;
        listeners = new ArrayList<>();

        // TODO: Register to be notified of any and all subscription changes
        //submgr.registerForNotifications(NotificationsRequest.newBuilder().setTopic())
    }


    /**
     * Add a forwarding rule to the streamer. 
     * The input route is the route to listen to and the output route is the route to forward the message to.
     * The forwarding works by registering a listener on the input route and forwarding the message to the output route.
     * 
     * Routing rules work on destination address for notifications, requests, and response messages however for
     * publish messages (that do not have a destination address) we use uSubscription service to fetch remote
     * subscriptions. 
     * NOTE: Default routing rule (where UAuthority.name is "*") *MUST* be added at the end of the list to avoid overriding other rules.
     * 
     * @param in input {@code Route} that the streamer listens to
     * @param out output {@code Route} that the streamer forwards the message to
     * @return {@code UStatus} with UCode.OK if the forwarding rule was added successfully,
     *        {@code UStatus} with UCode.ALREADY_EXISTS if the forwarding rule already exists,
     *       {@code UStatus} with UCode.INVALID_ARGUMENT if the input and output routes are the same
     */
    public UStatus addForwardingRule(Route in, Route out) {
        Objects.requireNonNull(in, "input cannot be null.");
        Objects.requireNonNull(out, "output cannot be null.");
        
        // Cannot route to itself
        if (in.equals(out)) {
            return UStatus.newBuilder().setCode(UCode.INVALID_ARGUMENT).build();
        }

        // check if the rule already exists in the list
        if (listeners.stream().anyMatch(p -> p.getInputRoute().equals(in) && p.getOutputRoute().equals(out))) {
            return UStatus.newBuilder().setCode(UCode.ALREADY_EXISTS).build();
        }

        // Setup the forwarding rule for any messages who's destination (sink) is 
        // out authority with the in uTransport
        TransportListener listener = new TransportListener(in, out);
        UUri out_uri = UUri.newBuilder().setAuthority(out.getAuthority()).build();
        UStatus result = in.getTransport().registerListener(out_uri, listener);
        if (result.getCode() != UCode.OK) {
            return result;
        }

        listeners.add(listener);


        // Published messages do not contain a sink so we cannot setup simple static forwarding rules based
        // on the sink address like notifications, requests and responses. Registering a listener for all
        // published events would mean that the streamer gets each and ever single published topic on the IN
        // Route (not what we want). What we need to know is what published topics does the streamer really 
        // need to care about (i.e register a listener for) so we don't get everything, this is only published
        // topics that there is a subscriber in OUT UAuthority that has subscribed to said topic.
        //
        // The good thing is that the subscribers information (URI) is recorded in usubscription service when 
        // the subscriber calls subscribe(), all we need to do is fetch ths list of subscriptions that match the 
        // IN UAuthority when the subscriber is the OUT UAuthority and we have the magic list we need!
        FetchSubscriptionsRequest request = FetchSubscriptionsRequest.newBuilder()
            .setTopic(UUri.newBuilder().setAuthority(in.getAuthority()))
            .setSubscriber(SubscriberInfo.newBuilder().setUri(UUri.newBuilder().setAuthority(out.getAuthority())))
            .build();

        // Get the list of subscriptions
        FetchSubscriptionsResponse response = submgr.fetchSubscriptions(request);
        
        // Make the streamer register for the published events that the 
        // OUT UAuthority wanted 
        response.getSubscriptionsList().forEach(p -> {
            in.getTransport().registerListener(p.getTopic(), listener);
        });

        // Any changes to subscription will require dynamic add/removal of listeners
        return result;
    }

    
    /**
    * Remove a forwarding rule from the streamer. 
    * 
    * @param in input {@code Route} that the streamer listens to
    * @param out output {@code Route} that the streamer forwards the message to
    * @return {@code UStatus} with UCode.OK if the forwarding rule was removed successfully,
    *         {@code UStatus} with UCode.NOT_FOUND if the forwarding rule was not found,
    *         {@code UStatus} with UCode.INVALID_ARGUMENT if the input and output routes are the same
    */
    public UStatus deleteForwardingRule(Route in, Route out) {
        Objects.requireNonNull(in, "input cannot be null.");
        Objects.requireNonNull(out, "output cannot be null.");
        
        if (in.equals(out)) {
            return UStatus.newBuilder().setCode(UCode.INVALID_ARGUMENT).build();
        }


        // Unregister the listener with the transport
        listeners.stream()
            .filter(p -> p.getInputRoute().equals(in) && p.getOutputRoute().equals(out))
            .forEach(p -> in.getTransport().unregisterListener(UUri.newBuilder().setAuthority(out.getAuthority()).build(), p));

        // Remove the listener from the list
        if (listeners.removeIf(p -> p.getInputRoute().equals(in) && p.getOutputRoute().equals(out))) {
            return UStatus.newBuilder().setCode(UCode.OK).build();
        }
        return UStatus.newBuilder().setCode(UCode.NOT_FOUND).build();
    }

}
