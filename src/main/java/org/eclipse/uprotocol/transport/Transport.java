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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.rpc.Status;

import io.cloudevents.CloudEvent;


/** uProtocol Transport Layer Interface (uP-L1)
 * Provides a standard interface to send and receive CloudEvent messages
 * The API is to be impemented by all the L1 transports (ex. HTTP, MQTT, etc..)
 */
public interface Transport {
    /**
	 * Send a CloudEvent to the connected uE
     * The send command returns immediately and means that your request is valid and will
	 * be sent to the platform or it is not valid and not sent on.
	 * @param ce  Cloudevent to send
	 * @return Status The result from the send()
	 */
	Status send(@Nonnull CloudEvent cloudEvent);
  

    /**
	 * Receive CloudEvents (messages)
     * API to fetch 0 to n messages from the sender used when the transport supports 
	 * @return Array of CloudEvents
	 */
	CloudEvent Receive();
}
