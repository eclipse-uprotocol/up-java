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


/**
 * A client for performing Remote Procedure Calls (RPC) on (other) uEntities.
 *
 * @see <a href="https://github.com/eclipse-uprotocol/up-spec/blob/v1.6.0-alpha.7/up-l2/api.adoc">
 * Communication Layer API specification</a>
 */
// [impl->dsn~communication-layer-api-declaration~1]
public interface RpcClient {
    /**
     * Invokes a method on a service.
     * 
     * @param methodUri The method to be invoked.
     * @param requestPayload The payload to include in the RPC request message to be sent
     * to the server. Use {@link UPayload#EMPTY} if no payload is required.
     * @param options RPC method invocation call options. Use {@link CallOptions#DEFAULT} for default options.
     * @return The outcome of the method invocation. The stage will either succeed with the
     * response payload, or it will be failed with a {@link UStatusException} if the method invocation
     * did not succeed.
     * @throws NullPointerException if any of the arguments are {@code null}.
     */
    CompletionStage<UPayload> invokeMethod(UUri methodUri, UPayload requestPayload, CallOptions options);
}
