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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.uprotocol.core.usubscription.v3.FetchSubscribersRequest;
import org.eclipse.uprotocol.core.usubscription.v3.FetchSubscribersResponse;
import org.eclipse.uprotocol.core.usubscription.v3.FetchSubscriptionsRequest;
import org.eclipse.uprotocol.core.usubscription.v3.FetchSubscriptionsResponse;
import org.eclipse.uprotocol.core.usubscription.v3.NotificationsRequest;
import org.eclipse.uprotocol.core.usubscription.v3.ResetRequest;
import org.eclipse.uprotocol.core.usubscription.v3.SubscriptionRequest;
import org.eclipse.uprotocol.core.usubscription.v3.SubscriptionResponse;
import org.eclipse.uprotocol.core.usubscription.v3.USubscription;
import org.eclipse.uprotocol.core.usubscription.v3.UnsubscribeRequest;
import org.eclipse.uprotocol.transport.UListener;
import org.eclipse.uprotocol.transport.UTransport;
import org.eclipse.uprotocol.v1.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class UStreamerTest {


    /**
     * This is a simple test where we have a single input and output route.
     * We also test the addForwardingRule() and deleteForwardingRule() methods.
     */
    @Test
    @DisplayName("simple test with a single input and output route")
    public void simple_test_with_a_single_input_and_output_route() {

        // Local Route
        UAuthority localAuthority = UAuthority.newBuilder().setName("local").build();
        Route local = new Route(localAuthority, new LocalTransport());

        // A Remote Route
        UAuthority remoteAuthority = UAuthority.newBuilder().setName("remote").build();
        Route remote = new Route(remoteAuthority, new RemoteTransport());
        
        UStreamer streamer = new UStreamer(new LocalSubMgr());

        // Add forwarding rules to route local<->remote 
        assertEquals(streamer.addForwardingRule(local, remote), UStatus.newBuilder().setCode(UCode.OK).build());
        assertEquals(streamer.addForwardingRule(remote, local), UStatus.newBuilder().setCode(UCode.OK).build());

        // Add forwarding rules to route local<->local, should report an error
        assertEquals(streamer.addForwardingRule(local, local), UStatus.newBuilder().setCode(UCode.INVALID_ARGUMENT).build());

        // Rule already exists so it should report an error
        assertEquals(streamer.addForwardingRule(local, remote), UStatus.newBuilder().setCode(UCode.ALREADY_EXISTS).build());
        
        // Try and remove an invalid rule
        assertEquals(streamer.deleteForwardingRule(remote, remote), UStatus.newBuilder().setCode(UCode.INVALID_ARGUMENT).build());

        // remove valid routing rules
        assertEquals(streamer.deleteForwardingRule(local, remote), UStatus.newBuilder().setCode(UCode.OK).build());
        assertEquals(streamer.deleteForwardingRule(remote, local), UStatus.newBuilder().setCode(UCode.OK).build());

        // Try and remove a rule that doesn't exist, should report an error
        assertEquals(streamer.deleteForwardingRule(local, remote), UStatus.newBuilder().setCode(UCode.NOT_FOUND).build());

    }
    

    /**
     * This is an example where we need to set up multiple routes to different destinations.
     */
    @Test
    @DisplayName("advanced test where there is an local route and two remote routes")
    public void advanced_test_where_there_is_an_local_route_and_two_remote_routes() {

        // Local Route
        UAuthority localAuthority = UAuthority.newBuilder().setName("local").build();
        Route local = new Route(localAuthority, new LocalTransport());

        // A Remote Route
        UAuthority remoteAuthority1 = UAuthority.newBuilder().setName("remote1").build();

        Route remote1 = new Route(remoteAuthority1, new RemoteTransport());

        // A Remote Route
        UAuthority remoteAuthority2 = UAuthority.newBuilder().setName("remote2").build();
        Route remote2 = new Route(remoteAuthority2, new RemoteTransport());
        
        UStreamer streamer = new UStreamer(new LocalSubMgr());

        // Add forwarding rules to route local<->remote1 
        assertEquals(streamer.addForwardingRule(local, remote1), UStatus.newBuilder().setCode(UCode.OK).build());
        assertEquals(streamer.addForwardingRule(remote1, local), UStatus.newBuilder().setCode(UCode.OK).build());

        // Add forwarding rules to route local<->remote2 
        assertEquals(streamer.addForwardingRule(local, remote2), UStatus.newBuilder().setCode(UCode.OK).build());
        assertEquals(streamer.addForwardingRule(remote2, local), UStatus.newBuilder().setCode(UCode.OK).build());

        // Add forwarding rules to route remote1<->remote2 
        assertEquals(streamer.addForwardingRule(remote1, remote2), UStatus.newBuilder().setCode(UCode.OK).build());
        assertEquals(streamer.addForwardingRule(remote2, remote1), UStatus.newBuilder().setCode(UCode.OK).build());
    }

    /** 
     * This is an example where we need to set up multiple routes to different destinations but using the same 
     * remote UTransport (i.e. connecting to multiple remote servers using the same UTransport instance).
     */
    @Test
    @DisplayName("advanced test where there is an local route and two remote routes but the remote routes have the same instance of UTransport")
    public void advanced_test_where_there_is_an_local_route_and_two_remote_routes_but_the_remote_routes_have_the_same_instance_of_UTransport() {

        // Local Route
        UAuthority localAuthority = UAuthority.newBuilder().setName("local").build();
        Route local = new Route(localAuthority, new LocalTransport());

        // A Remote Route
        UAuthority remoteAuthority1 = UAuthority.newBuilder().setName("remote1").build();
        UTransport remoteTransport = new RemoteTransport();
        Route remote1 = new Route(remoteAuthority1, remoteTransport);

        // A Remote Route
        UAuthority remoteAuthority2 = UAuthority.newBuilder().setName("remote2").build();
        Route remote2 = new Route(remoteAuthority2, remoteTransport);
        
        UStreamer streamer = new UStreamer(new LocalSubMgr());

        // Add forwarding rules to route local<->remote1 
        assertEquals(streamer.addForwardingRule(local, remote1), UStatus.newBuilder().setCode(UCode.OK).build());
        assertEquals(streamer.addForwardingRule(remote1, local), UStatus.newBuilder().setCode(UCode.OK).build());

        // Add forwarding rules to route local<->remote2 
        assertEquals(streamer.addForwardingRule(local, remote2), UStatus.newBuilder().setCode(UCode.OK).build());
        assertEquals(streamer.addForwardingRule(remote2, local), UStatus.newBuilder().setCode(UCode.OK).build());
    }
        

    /**
     * This is an example where we need to set up multiple routes to different destinations where one of the
     * routes is the default route (ex. the cloud gateway) 
     */
    @Test
    @DisplayName("advanced test where there is a local route and two remote routes where the second route is the default route")
    public void advanced_test_where_there_is_a_local_route_and_two_remote_routes_where_the_second_route_is_the_default_route() {

        // Local Route
        UAuthority localAuthority = UAuthority.newBuilder().setName("local").build();
        Route local = new Route(localAuthority, new LocalTransport());

        // A Remote Route
        UAuthority remoteAuthority1 = UAuthority.newBuilder().setName("remote1").build();
        Route remote1 = new Route(remoteAuthority1, new RemoteTransport());

        // A Remote Route
        UAuthority remoteAuthority2 = UAuthority.newBuilder().setName("*").build();
        Route remote2 = new Route(remoteAuthority2, new RemoteTransport());
        
        UStreamer streamer = new UStreamer(new LocalSubMgr());

        // Add forwarding rules to route local<->remote1 
        assertEquals(streamer.addForwardingRule(local, remote1), UStatus.newBuilder().setCode(UCode.OK).build());
        assertEquals(streamer.addForwardingRule(remote1, local), UStatus.newBuilder().setCode(UCode.OK).build());

        // Add forwarding rules to route local<->remote2 
        assertEquals(streamer.addForwardingRule(local, remote2), UStatus.newBuilder().setCode(UCode.OK).build());
        assertEquals(streamer.addForwardingRule(remote2, local), UStatus.newBuilder().setCode(UCode.OK).build());

        // Add forwarding rules to route remote1<->remote2 
        assertEquals(streamer.addForwardingRule(remote1, remote2), UStatus.newBuilder().setCode(UCode.OK).build());
        assertEquals(streamer.addForwardingRule(remote2, remote1), UStatus.newBuilder().setCode(UCode.OK).build());
    }
    

    private class LocalTransport implements UTransport {
        @Override
        public UStatus send(UMessage message) {
            return UStatus.newBuilder().setCode(UCode.OK).build();
        }

        @Override
        public UStatus registerListener(UUri topic, UListener listener) {
            return UStatus.newBuilder().setCode(UCode.OK).build();
        }

        @Override
        public UStatus unregisterListener(UUri topic, UListener listener) {
            return UStatus.newBuilder().setCode(UCode.OK).build();
        }
    }


    private class RemoteTransport implements UTransport {

        @Override
        public UStatus send(UMessage message) {
            return UStatus.newBuilder().setCode(UCode.OK).build();
        }

        @Override
        public UStatus registerListener(UUri topic, UListener listener) {
            return UStatus.newBuilder().setCode(UCode.OK).build();
        }

        @Override
        public UStatus unregisterListener(UUri topic, UListener listener) {
            return UStatus.newBuilder().setCode(UCode.OK).build();
        }
    }

    class LocalSubMgr implements USubscription {

        @Override
        public FetchSubscriptionsResponse fetchSubscriptions(FetchSubscriptionsRequest request) {
            return FetchSubscriptionsResponse.getDefaultInstance();
        }

        @Override
        public UStatus registerForNotifications(NotificationsRequest request) {
            return UStatus.newBuilder().setCode(UCode.OK).build();
        }

        @Override
        public UStatus unregisterForNotifications(NotificationsRequest request) {
            return UStatus.newBuilder().setCode(UCode.OK).build();
        }

        @Override
        public SubscriptionResponse subscribe(SubscriptionRequest request) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'subscribe'");
        }

        @Override
        public UStatus unsubscribe(UnsubscribeRequest request) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'unsubscribe'");
        }

        @Override
        public FetchSubscribersResponse fetchSubscribers(FetchSubscribersRequest request) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'fetchSubscribers'");
        }

        @Override
        public UStatus reset(ResetRequest request) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'reset'");
        }
        
    }

}
