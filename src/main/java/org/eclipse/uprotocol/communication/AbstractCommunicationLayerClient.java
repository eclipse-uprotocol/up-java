/**
 * SPDX-FileCopyrightText: 2025 Contributors to the Eclipse Foundation
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

import java.util.Objects;

import org.eclipse.uprotocol.transport.LocalUriProvider;
import org.eclipse.uprotocol.transport.UTransport;

public class AbstractCommunicationLayerClient {
    private final UTransport transport;
    private final LocalUriProvider uriProvider;

    protected AbstractCommunicationLayerClient(UTransport transport, LocalUriProvider uriProvider) {
        this.transport = Objects.requireNonNull(transport);
        this.uriProvider = Objects.requireNonNull(uriProvider);
    }

    protected UTransport getTransport() {
        return transport;
    }

    protected LocalUriProvider getUriProvider() {
        return uriProvider;
    }
}
