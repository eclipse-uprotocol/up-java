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

import java.util.concurrent.CompletionStage;

import org.eclipse.uprotocol.transport.UListener;
import org.eclipse.uprotocol.v1.UStatus;
import org.eclipse.uprotocol.v1.UUri;

/**
 * uP-L2 Subscriber interface.<BR>
 */
public interface Subscriber {
    /**
     * API to subscribe to a given topic. <br>
     * 
     * @param topicUri The topic to subscribe to.
     * @param listener The listener to be called when a message is received on the topic.
     * @return Returns the CompletionStage with the response UMessage or exception with the failure
     * reason as {@link UStatus}.
     */
    CompletionStage<UStatus> subscribe(UUri topicUri, UListener listener);


    /**
     * API to unsubscribe to a given topic. <br>
     * 
     * @param topicUri The topic to subscribe to.
     * @param listener The listener to be called when a message is received on the topic.
     * @return Returns the CompletionStage with the response UMessage or exception with the failure
     * reason as {@link UStatus}.
     */
    UStatus unsubscribe(UUri topicUri, UListener listener);
}
