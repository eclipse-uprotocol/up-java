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
package org.eclipse.uprotocol.sdk;

import java.util.concurrent.CompletableFuture;

import org.eclipse.uprotocol.cloudevent.datamodel.UCloudEventAttributes;
import org.eclipse.uprotocol.uri.datamodel.UUri;


/**
 * uProtocol Layer 2 RPC Requester Interface
 * 
 * API called by uEs that wish to send an RPC Request to another uE
 */
public interface Requester {
    /**
     * Issue a Request (Invoke a method).
     * 
     * The Requester calls this API to issue a request, what is returned is a completable future
     * that the caller can either block or wait for the future to be completed.
     * 
     * @param request URI of the request method
     * @param data Request message data 
     * @param attributes Additional request attributes
     * @return Returns the CompletableFuture with the result or exception.
     * @throws StatusException if there are any communication errors with sending the request
     */
    CompletableFuture<byte[]> request(UUri request, byte[] data, UCloudEventAttributes attributes);
}
