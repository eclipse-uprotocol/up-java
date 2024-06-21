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

import org.eclipse.uprotocol.transport.UListener;
import org.eclipse.uprotocol.v1.UMessage;


/**
 * Transport Listener receives a message that is destine for another transport so 
 * it simply forwards the message to the output UTransport.
 */
class TransportListener implements UListener {

    private Route in;
    private Route out; 

    @Override
    public void onReceive(UMessage message) {
        out.getTransport().send(message);
    }

    /**
     * Constructor passing the input and output routes
     * @param in input Route
     * @param out output Route
     */
    public TransportListener(Route in, Route out) {
        this.in = in;
        this.out = out;
    }

    /**
     * Fetch the input route
     * @return input Route
     */
    public Route getInputRoute() {
        return in;
    }

    /** 
     * Fetch the output route
     * @return output Route
     */
    public Route getOutputRoute() {
        return out;
    }

}