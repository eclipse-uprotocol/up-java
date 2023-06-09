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

import java.util.concurrent.CompletableFuture;

import org.eclipse.uprotocol.status.datamodel.UStatus;
import org.eclipse.uprotocol.uri.datamodel.UUri;

import com.google.protobuf.Any;

import io.cloudevents.CloudEvent;

public interface UBus {
    /**
     * Send a publish event to the bus
     * 
     * @param topic The topic the event is published too
     * @param data The event data being published
     * @param attributes Various additional attributes
     * @return Returns google.rpc.Status for sending the published event to the bus
     */
    UStatus publish(CloudEvent ce);


    /**
     * Send a notification to the bus
     * 
     * @param topic The topic the event is published too
     * @param sink  The destination for the notification event
     * @param data The event data being published
     * @param attributes Various additional attributes
     * @return Returns google.rpc.Status for sending the published event to the bus
     */
    UStatus notify(CloudEvent ce);


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
     */
    CompletableFuture<Any> request(CloudEvent ce);


    /**
     * Send a response to a request
     * 
     * @param responder Who we are sending the response too
     * @param data Data passed in the response message. 
     * @param attributes Additional attributes for sending the message. 
     */
    UStatus response(CloudEvent ce);


    /**
     * Register a listener to be called when an event is received.
     * The event can for any URI (publish, notification, request)
     * 
     * @param uri The uri of the publish/notification/request event
     * @param listener The event listener
     * @return Status The status of the API
     */
    UStatus registerEventListener(UUri uri, EventListener listener);


    /**
     * Unregister a event listener.
     * 
     * @param uri The uri of the publish/notification/request event
     * @param listener The event listener
     * @return Status The status of the API
     */
    UStatus unregisterEventListener(UUri topic, EventListener listener); 
}
