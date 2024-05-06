/*
 * SPDX-FileCopyrightText: Copyright (c) 2024 Contributors to the Eclipse Foundation
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

import org.eclipse.uprotocol.v1.UPayload;
import org.eclipse.uprotocol.v1.UStatus;
import org.eclipse.uprotocol.v1.UUri;

/**
 * uP-L2 interface and data models for Java.<BR>
 * 
 * uP-L1 interfaces implements the core uProtocol across various the communication middlewares
 * and programming languages while uP-L2 API are the client-facing APIs that wrap the transport
 * functionality into easy to use, language specific, APIs to do the most common functionality
 * of the protocol (subscribe, publish, notify, invoke a Method, or handle RPC requests).
 */
public interface Publisher {
    /**
     * API for clients to publish a message to a given topic. <br>
     * 
     * @param topicUri The topic URI to publish the message to.
     * @param payload The message to be published to the topic.
     * @return Returns the UStatus with the status of the publish operation.
     */
    UStatus publish(UUri topicUri, UPayload payload);


    /**
     * API for clients to send a notification to a given destination (sink). <br>
     * @param topicUri The topic for the notification (source).
     * @param sink Who to send the notification to (destination).
     * @param payload The message that is in the notification.
     * @return Returns the UStatus with the status of the notify operation.
     */
    UStatus notify(UUri topicUri, UUri sink, UPayload payload);
}
