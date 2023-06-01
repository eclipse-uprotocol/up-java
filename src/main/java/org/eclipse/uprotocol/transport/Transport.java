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

import org.eclipse.uprotocol.receiver.Receiver;

import com.google.rpc.Status;

import io.cloudevents.CloudEvent;


/** uProtocol Transport Layer Interface (uP-L1)
 * Interface is to be implemented by the various transport technologies (ex. MQTT, Binder, HTTP, etc...)
 * NOTE: SW developers do not call these APIs, they interract with the boundary object ULink.java
 */
public interface Transport {
    /**
	 * Send a CloudEvent to the connected uE
     * The send command returns immediately and means that your request is valid and will
	 * be sent to the platform or it is not valid and not sent on.
	 * @param ce  Cloudevent to send
	 * @return Status The result from the send()
	 */
	Status send(CloudEvent cloudEvent);
  

     /*
	  * Register message Receier
      * When the transport supports push type delivery method, the caller invokes this
	  * method to register a listener to receive the events 
	  * @param receiver The message reciver
	  * @return Status The result from the send()
	 */
	Status registerReceiver(Receiver receiver);


    /*
	 * Unregister Message Receier
     * When the transport supports push type delivery method, the caller invokes this
	 * method to unregister a message Receiver to receive the events 
	 * @param receiver to unregister
	 * @return Status The result from the send()
	 */
	Status unregisterReceiver(Receiver receiver);

    /**
	 * Receive CloudEvents (messages)
     * API to fetch 0 to n messages from the sender used when the transport supports 
	 * @return Array of CloudEvents
	 */
	CloudEvent[] Receive();
}
