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
package org.eclipse.uprotocol.transport;

import org.eclipse.uprotocol.status.datamodel.UStatus;

import io.cloudevents.CloudEvent;



/** uProtocol Transport Layer Interface (uP-L1)
 * Provides a standard interface to send and receive uProtocol messages
 * The API is to be impemented by all the L1 transports (ex. HTTP, MQTT, etc..)
 */
public interface UTransport {
    /**
	 * Send a uProtocol message.
     * 
	 * @param message  uProtocol message
	 * @return Status The result from the send()
	 */
	UStatus send(CloudEvent ce);


    /**
	 * Receive uProtocol messages
     * API to fetch 0 to n messages from the sender used when the transport supports 
	 * @return Array of CloudEvents
	 */
	CloudEvent receive();
}