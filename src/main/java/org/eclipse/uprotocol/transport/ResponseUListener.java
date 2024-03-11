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

/**
 * Specific Response type of UListener to handle incoming Response messages. <br>
 * 
 * Response messages have {@link UMessageType} is {@link UMESSAGE_TYPE_RESPONSE} and the
 * {@link UAttributes} source is set to the method UUri the client invoked. <br>
 * 
 * The {@link UUri} passed to {@link org.eclipse.uprotocol.transport.UTransport.registerListener} 
 * is the method UUri.
 *
 * @see org.eclipse.uprotocol.transport.UTransport#registerListener(UUri, UListener)
 */

public interface ResponseUListener extends UListener {
}
