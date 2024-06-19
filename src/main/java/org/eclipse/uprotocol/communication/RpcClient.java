/**
 * SPDX-FileCopyrightText: 2024 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.eclipse.uprotocol.communication;

import java.util.concurrent.CompletionStage;
import org.eclipse.uprotocol.v1.UUri;
import org.eclipse.uprotocol.v1.UStatus;


/**
 * Communication Layer (uP-L2) RPC Client Interface.<BR> 
 * 
 * clients use this API to invoke a method (send a request and wait for a reply).
 */
public interface RpcClient {
    /**
     * API for clients to invoke a method (send an RPC request) and receive the response (the returned 
     * {@link CompletionStage} {@link UPayload}. <br>
     * 
     * @param methodUri The method URI to be invoked.
     * @param requestPayload The request message to be sent to the server.
     * @param options RPC method invocation call options, see {@link CallOptions}
     * @return Returns the CompletionStage with the response payload or exception with the failure
     *         reason as {@link UStatus}.
     */
    CompletionStage<UPayload> invokeMethod(UUri methodUri, UPayload requestPayload, CallOptions options);
}
