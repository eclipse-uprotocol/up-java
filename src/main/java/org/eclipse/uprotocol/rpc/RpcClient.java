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
 * SPDX-FileType: SOURCE
 * SPDX-FileCopyrightText: 2023 General Motors GTO LLC
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.uprotocol.rpc;

import java.util.concurrent.CompletionStage;

import org.eclipse.uprotocol.v1.*;

/**
 * RpcClient is an interface used by code generators for uProtocol services defined in proto files such as
 * the core uProtocol services found in https://github.com/eclipse-uprotocol/uprotocol-core-api. the interface 
 * provides a clean contract for mapping a RPC request to a response. For more details please refer to
 * https://github.com/eclipse-uprotocol/uprotocol-spec/blob/main/up-l2/README.adoc[RpcClient Specifications]
 */
public interface RpcClient {

    /**
     * API for clients to invoke a method (send an RPC request) and receive the response (the returned 
     * {@link CompletionStage} {@link UPayload}. <br>
     * Client will set method to be the URI of the method they want to invoke, 
     * payload to the request message, and attributes with the various metadata for the 
     * method invocation.
     * @param methodUri The method URI to be invoked (i.e. the name of the API we are calling).
     * @param requestPayload The request message to be sent to the server.
     * @param options RPC method invocation call options, see {@link CallOptions}
     * @return Returns the CompletionStage with the response message (payload) or exception with the failure
     * reason as {@link UStatus}.
     */
    CompletionStage<UPayload> invokeMethod(UUri methodUri, UPayload requestPayload, CallOptions options);
}
