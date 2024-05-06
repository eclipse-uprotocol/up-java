/*
 * SPDX-FileCopyrightText:  Copyright (c) 2024 Contributors to the Eclipse Foundation
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
 * SPDX-License-Identifier: Apache-2.0
 */
package org.eclipse.uprotocol.upclient;

import org.eclipse.uprotocol.v1.*;

/**
 * RpcServer is an interface called by uServices to register method listeners for incoming RPC requests
 * from clients.
 */
public interface RpcServer {
    /**
     * Register a listener for a particular method URI to be notified when requests are sent against said method.
     *
     * <p>Note: Only one listener is allowed to be registered per method URI.
     *
     * @param method Uri for the method to register the listener for.
     * @param listener The listener for handling the request method.
     * @return Returns the status of registering the RpcListener.
     */
    UStatus registerRpcListener(UUri method, URpcListener listener);

    /**
     * Unregister an RPC listener for a given method Uri. Messages arriving on this topic will no longer be processed
     * by this listener.
     * @param method Resolved UUri for where the listener was registered to receive messages from.
     * @param listener The method to execute to process the date for the topic.
     * @return Returns status of registering the RpcListener.
     */
    UStatus unregisterRpcListener(UUri method, URpcListener listener);
}
