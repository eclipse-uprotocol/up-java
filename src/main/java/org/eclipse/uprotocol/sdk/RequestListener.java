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

import org.eclipse.uprotocol.uri.datamodel.UUri;

import com.google.rpc.Status;

/**
 * Request Listener Interface.
 * 
 * Responders use this interface to listen for incoming requests so that
 * it can invoke the request and issue a response.
 */
public interface RequestListener {

	/**
     * Receive Events from the bus
	 * When messages are received, this function is called.
	 * @param request The request UURI (method) to be invoked
	 * @param requester UUri of the requester (who requested the method to be invoked)
	 * @param data Request message data
	 * @return Status
     */
	Status onRequest(UUri request, UUri requester, byte[] data);
}