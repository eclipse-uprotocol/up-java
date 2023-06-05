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
 * uProtocol L2 Event Consumer Interface.
 * 
 * Interface that is used to register/unregister event listeners in order to 
 * Receive events from the dispatcher.
 */
public interface Subscriber {

    /**
     * Register to receive published/notification events per topic
     * 
     * @param topic What topic the receiver will receive events for
     * @param receiver The Receiver to receive events (push method)
     * @return Status The status of the API
     */
    Status registerEventListener(UUri topic, EventListener listener);

    /**
     * Unregister Receiver per topic
     * 
     * @param topic What topic the receiver will receive events for
     * @param receiver The Receiver to receive events (push method) 
     * @return Status of the API
     */
    Status unregisterEventListener(UUri topic, EventListener listener);

    /**
     * Subscribe to a given topic passing the SubscriptionAttributes
     * 
     * @param topic What topic the receiver will receive events for
     * @param receiver The Receiver to receive events (push method) 
     * @return Status of the API
     */
    Status subscribe(UUri topic, SubscriptionAttributes atributes);
}
