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

import org.eclipse.uprotocol.uri.factory.UriFactory;
import org.eclipse.uprotocol.v1.UUri;


/**
 * A server for exposing Remote Procedure Call (RPC) endpoints.
 *
 * @see <a href="https://github.com/eclipse-uprotocol/up-spec/blob/v1.6.0-alpha.4/up-l2/api.adoc">
 * Communication Layer API specification</a>
 */
public interface RpcServer {
    /**
     * Registers an endpoint for RPC requests.
     * <p>
     * Note that only a single endpoint can be registered for a given resource ID.
     * However, the same request handler can be registered for multiple endpoints.
     *
     * @param originFilter A pattern defining origin addresses to accept requests from. Use {@link UriFactory#ANY}
     * to match all origin addresses.
     * @param resourceId The resource identifier of the (local) method to accept requests for.
     * @param handler The handler to invoke for each incoming request that originates from a
     * source matching the origin filter.
     * @return The outcome of the registration. The stage will be completed with a {@link UStatusException} if
     * registration has failed.
     * @throws NullPointerException if any of the parameters is {@code null}.
     */
    CompletionStage<Void> registerRequestHandler(UUri originFilter, int resourceId, RequestHandler handler);

    /**
     * Deregisters a previously {@link #registerRequestHandler(UUri, int, RequestHandler) registered endpoint}.
     * 
     * @param originFilter The origin pattern that the endpoint had been registered for.
     * @param resourceId The (local) resource identifier that the endpoint had been registered for.
     * @param handler The handler to unregister.
     * @return The outcome of the registration. The stage will be completed with a {@link UStatusException} if
     * registration has failed.
     * @throws NullPointerException if any of the parameters is {@code null}.
     */
    CompletionStage<Void> unregisterRequestHandler(UUri originFilter, int resourceId, RequestHandler handler);
}
