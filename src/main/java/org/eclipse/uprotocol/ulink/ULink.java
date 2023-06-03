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

package org.eclipse.uprotocol.ulink;

import java.util.concurrent.CompletableFuture;
import org.eclipse.uprotocol.cloudevent.datamodel.UCloudEventAttributes;
import org.eclipse.uprotocol.uri.datamodel.UUri;

import com.google.protobuf.Any;
import com.google.rpc.Status;


/* 
 * Abstract bass class forall uBus implmentations. 
 * Specific deployments (i.e. Android, Linux, Cloud, etc..) will inherit from this class and
 * Helper Base class for all uBus implementations. It is expected that you class that is used by uApps and uServices to interface with uBus.
 * uBus. 
 */
public interface ULink {
    /**
     * Invoke (call) and RPC
     * @param method The UUri of the method that is to be invoked
     * @param data The information that is to be send in teh request
     * @param attributes Various attributes used by uEs
     * @return Returns the CompletableFuture with the result or exception
     */
    CompletableFuture<Any> invokeRPC(UUri method, Any data, UCloudEventAttributes attributes);
    

    /**
     * Send a Publish event
     * @param topic The topic the event is published too
     * @param data The event data benig published
     * @param attributes Various additional attributes
     * @return Returns google.rpc.Status for sending the published event to the bus
     */
    Status publish(UUri topic, Any data, UCloudEventAttributes attributes);

    /**
     * Send a Notification to a specific consumer
     * @param topic The topic the event is published too
     * @param destination who the event is being sent to
     * @param data The event data benig published
     * @param attributes Various additional attributes
     * @return Returns google.rpc.Status for sending the published event to the bus
     */
    Status notify(UUri topic, UUri destination, Any data, UCloudEventAttributes attributes);

    /**
     * Register to receive published/notification events per topic
     * 
     * @param topic What topic the receiver will receive events for
     * @param receiver The Receiver to receive events (push method) 
     */
    Status registerEventListener(UUri topic, EventListener listener);

    /**
     * Unregister Receiver per topic
     * 
     * @param topic What topic the receiver will receive events for
     * @param receiver The Receiver to receive events (push method) 
     */
    Status unregisterEventListener(UUri topic, EventListener listener);
}

