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
package org.eclipse.uprotocol.transport;

import org.eclipse.uprotocol.v1.UAttributes;
import org.eclipse.uprotocol.v1.UMessageType;
import org.eclipse.uprotocol.v1.UUri;

/**
 * Specific notification type of UListener to handle incoming notification messages. <br>
 * 
 * Notifications have {@link UMessageType} is {@link UMessageType.UMESSAGE_TYPE_PUBLISH} and
 * {@link UAttributes} sink is set to ourselves, this is because the published event is 
 * only destined to us. <br>
 * 
 * The {@link UUri} passed to {@link org.eclipse.uprotocol.transport.UTransport.registerListener} 
 * is the notification topic.
 *
 * @see org.eclipse.uprotocol.transport.UTransport#registerListener(UUri, UListener)
 */
public interface NotificationUListener extends UListener {

}
