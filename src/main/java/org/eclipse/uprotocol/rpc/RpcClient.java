/*
 * Copyright (c) 2023 General Motors GTO LLC
 *
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

import java.util.concurrent.CompletionStage;

import org.eclipse.uprotocol.uri.datamodel.UUri;
import org.eclipse.uprotocol.utransport.datamodel.UAttributes;
import org.eclipse.uprotocol.utransport.datamodel.UPayload;

/**
 * The RpcClient interface defines the RPC client API per the uProtocol specification
 * https://github.com/eclipse-uprotocol/uprotocol-spec/blob/main/up-l2/README.adoc
 * 
 */
public interface RpcClient {

    /**
     * Support for RPC method invocation.
     * @param topic The topic to invoke the method on.
     * @param payload The payload to send.
     * @param attributes The attributes to send.
     * @return Returns the CompletableFuture with the result or exception. 
     */
    CompletionStage<UPayload> invokeMethod(UUri topic, UPayload payload, UAttributes attributes);
}
