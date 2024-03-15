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
import org.eclipse.uprotocol.v1.UStatus;
import org.eclipse.uprotocol.v1.UUri;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Sample implementation of UStreamer written in Java for demonstration purposes only
 * 
 * The streamer provides APIs to simply add remove routing rules.
 */
public class UStreamer {

    List<TransportListener> listeners;


    /**
     * Constructor
     */
    public UStreamer() {
        listeners = new ArrayList<>();
    }


    /**
     * Add a forwarding rule to the streamer. 
     * The input route is the route to listen to and the output route is the route to forward the message to.
     * The forwarding works by registering a listener on the input route and forwarding the message to the output route.
     * 
     * NOTE: Default routing rule (where UAuthority.name is "*") *MUST* be added at the end of the list to avoid overriding other rules.
     * 
     * @param in input {@code Route} that the streamer listens to
     * @param out output {@code Route} that the streamer forwards the message to
     * @return {@code UStatus} with UCode.OK if the forwarding rule was added successfully
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

        TransportListener listener = new TransportListener(in, out);
        UUri uri = UUri.newBuilder().setAuthority(out.getAuthority()).build();

        UStatus result = in.getTransport().registerListener(uri, listener);

        if (result.getCode() != UCode.OK) {
            return result;
        }

        listeners.add(listener);

        return result;
    }

    public UStatus deleteForwardingRule(Route in, Route out) {
        Objects.requireNonNull(in, "input cannot be null.");
        Objects.requireNonNull(out, "output cannot be null.");
        
        if (in.equals(out)) {
            return UStatus.newBuilder().setCode(UCode.INVALID_ARGUMENT).build();
        }

        if (listeners.removeIf(p -> p.getInputRoute().equals(in) && p.getOutputRoute().equals(out))) {
            return UStatus.newBuilder().setCode(UCode.OK).build();
        }
        return UStatus.newBuilder().setCode(UCode.NOT_FOUND).build();
    }

}
