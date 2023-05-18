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

package org.eclipse.uprotocol.rpc;

import com.google.protobuf.Any;
import io.cloudevents.CloudEvent;

import java.util.concurrent.CompletableFuture;

/**
 * Interface for transport layers that want to implement RPC over  for Layer 2.
 */
public interface Rpc {

    /**
     * Support for RPC method invocation.
     * @param requestEvent req.v1 CloudEvent.
     * @return Returns the CompletableFuture with the result or exception. Tamara would rather have a CompletionStage since she likes it better
     *  when you program to an interface and not an implementation.
     */
    CompletableFuture<Any> invokeMethod(CloudEvent requestEvent);

    /**
     * This is the URI of the calling client, used for routing the response back to the caller.
     * @return Returns the uri of the uE invoking the rpc.
     */
    String  getResponseUri();
}
