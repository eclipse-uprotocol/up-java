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

import org.eclipse.uprotocol.transport.UTransport;
import org.eclipse.uprotocol.v1.UAuthority;

/**
 * Route is defined as a combination of UAuthority and UTransport as routes are at the UAuthority level.
 *
 */
public class Route {
    private UAuthority authority;
    private UTransport transport;

    /**
     * Constructor
     * @param authority 
     * @param transport
     */
    Route(UAuthority authority, UTransport transport) {
        this.authority = authority;
        this.transport = transport;
    }

    /** 
     * Fetch the authority
     * @return UAuthority
     */
    public UAuthority getAuthority() {
        return authority;
    }
    
    /**
     * Fetch the transport
     * @return UTransport
     */
    public UTransport getTransport() {
        return transport;
    }
}
