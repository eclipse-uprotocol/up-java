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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

import java.util.concurrent.CompletableFuture;

import org.eclipse.uprotocol.transport.LocalUriProvider;
import org.eclipse.uprotocol.transport.StaticUriProvider;
import org.eclipse.uprotocol.transport.UListener;
import org.eclipse.uprotocol.transport.UTransport;
import org.eclipse.uprotocol.v1.UMessage;
import org.eclipse.uprotocol.v1.UUri;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class CommunicationLayerClientTestBase {
    protected static final UUri TRANSPORT_SOURCE = UUri.newBuilder()
            .setAuthorityName("my-vehicle")
            .setUeId(0xa1)
            .setUeVersionMajor(0x01)
            .setResourceId(0x0000)
            .build();
    protected static final UUri TOPIC_URI = UUri.newBuilder(TRANSPORT_SOURCE)
            .setResourceId(0xa100)
            .build();
    protected static final UUri DESTINATION_URI = UUri.newBuilder()
            .setAuthorityName("other-vehicle")
            .setUeId(0x2bbbb)
            .setUeVersionMajor(0x02)
            .setResourceId(0x0000)
            .build();
    protected static final UUri METHOD_URI = UUri.newBuilder(TRANSPORT_SOURCE)
            .setResourceId(0x00a)
            .build();

    protected UTransport transport;
    protected LocalUriProvider uriProvider;
    protected ArgumentCaptor<UListener> responseListener;
    protected ArgumentCaptor<UMessage> requestMessage;

    @BeforeEach
    void setUpTransport() {
        transport = mock(UTransport.class);
        Mockito.lenient().when(transport.registerListener(any(UUri.class), any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(null));
        Mockito.lenient().when(transport.unregisterListener(any(UUri.class), any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(null));
        Mockito.lenient().when(transport.send(any(UMessage.class)))
            .thenReturn(CompletableFuture.completedFuture(null));
        uriProvider = StaticUriProvider.of(TRANSPORT_SOURCE);
        responseListener = ArgumentCaptor.forClass(UListener.class);
        requestMessage = ArgumentCaptor.forClass(UMessage.class);
    }
}
