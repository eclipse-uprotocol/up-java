/*
 * Copyright (c) 2023 General Motors GTO LLC
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
 */

package org.eclipse.uprotocol.utransport;

import org.eclipse.uprotocol.uri.datamodel.UEntity;
import org.eclipse.uprotocol.uri.datamodel.UUri;
import org.eclipse.uprotocol.utransport.datamodel.UAttributes;
import org.eclipse.uprotocol.utransport.datamodel.UListener;
import org.eclipse.uprotocol.utransport.datamodel.UPayload;
import org.eclipse.uprotocol.utransport.datamodel.UStatus;

/**
 * UTransport is the  uP-L1 interface that provides a common API for uE developers to send and receive messages.
 * UTransport implementations contain the details for connecting to the underlying transport technology and sending UMessage using the configured technology.
 * @param <T> The type of the UriFormat that the UTransport implementation will use.
 * @param <S> The primitive type for the UriFormat (string for long/short or byte[] for micro).
 */

public interface UTransport {

    /**
     * API to register the calling uE with the underlining transport implementation.
     * @param uEntity uProtocol UEntity information
     * @param token Deployment specific token used to authenticate the calling uE
     * @return Returns Status if the registration is successful or not.
     */
    UStatus register (UEntity uEntity, byte[] token) ;


    /**
     * Transmit UPayload to the topic using the attributes defined in UTransportAttributes.
     * @param topic topic to send the payload to.
     * @param payload Actual payload.
     * @param attributes Additional transport attributes.
     * @return Returns an Status if managed to send to the underlying communication technology or not.
     */
    UStatus send(UUri topic, UPayload payload, UAttributes attributes);

    /**
     * Register a method that will be called when a message comes in on the specific topic.
     * @param topic Topic the message that arrived via the underlying transport technology.
     * @param listener The method to execute to process the date for the topic.
     * @return Returns an Ack if the method is registered successfully.
     */
    UStatus registerListener(UUri topic, UListener listener);

    /**
     * Unregister a method on a topic. Messages arriving on this topic will no longer be processed by this listener.
     * @param topic Topic the message that arrived via the underlying transport technology.
     * @param listener The method to execute to process the date for the topic.
     * @return Returns an Ack if the method is removed successfully.
     * 
     */
    UStatus unregisterListener(UUri topic, UListener listener);
}