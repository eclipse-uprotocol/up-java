/*
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
package org.eclipse.uprotocol.ubus;

import javax.annotation.Nonnull;

import org.eclipse.uprotocol.cloudevent.datamodel.UCloudEventAttributes;
import org.eclipse.uprotocol.uri.datamodel.UUri;

import com.google.rpc.Status;

/**
 * uProtocol Layer 2 Responder Interface
 * 
 * Responder is a SW entity that responds to RPC requests. A responder can 
 * Register a request listener (to be notified when requests are received)
 * and then send the response message.
 */
public interface Responder {
    /**
     * Register Request Listener
     * 
     * @param request the UUri of the request to register the listener for
     * @param listener The listener to be called when the request is received
     * @return Status The status of the API
     */
    Status registerRequestListener(@Nonnull UUri request, @Nonnull RequestListener listener);

    /**
     * Unregister Request Listener
     * 
     * @param request the UUri of the request to unregister the listener for
     * @param listener The registered listener to unregister
     * @return Status of the API
     */
    Status unregisterRequestListener(@Nonnull UUri request, @Nonnull RequestListener listener);
    
    /**
     * Send a response to a request
     * 
     * @param responder Who we are sending the response too
     * @param data Data passed in the response message. 
     * @param attributes Additional attributes for sending the message. 
     * @throws StatusException if there are any communication errors with sending the request
     */
    Status sendResponse(UUri responder, byte[] data, UCloudEventAttributes attributes);
}
