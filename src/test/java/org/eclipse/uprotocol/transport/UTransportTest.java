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
package org.eclipse.uprotocol.transport;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.eclipse.uprotocol.uri.factory.UriFactory;
import org.eclipse.uprotocol.v1.UUri;

/**
 * Tests implementing and using uTransport API.
 */
class UTransportTest {
    private static final UUri SOURCE_FILTER = UUri.newBuilder()
            .setAuthorityName("my-vehicle")
            .setUeId(0x1a54)
            .setUeVersionMajor(0x02)
            .setResourceId(0xFFFF)
            .build();

    private UTransport transport;
    private UListener listener;

    @BeforeEach
    void setUp() {
        transport = mock(UTransport.class);
        Mockito.lenient().when(transport.registerListener(any(UUri.class), any(UListener.class)))
            .thenCallRealMethod();
        Mockito.lenient().when(transport.unregisterListener(any(UUri.class), any(UListener.class)))
            .thenCallRealMethod();
        listener = mock(UListener.class);
    }

    @Test
    @DisplayName("Test default implementation of registerListener")
    @SuppressWarnings("unchecked")
    void testRegisterListener() {
        when(transport.registerListener(any(UUri.class), any(Optional.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(null));

        transport.registerListener(SOURCE_FILTER, listener).toCompletableFuture().join();
        verify(transport).registerListener(eq(SOURCE_FILTER), eq(Optional.of(UriFactory.ANY)), eq(listener));
    }

    @Test
    @DisplayName("Test happy path unregister listener")
    @SuppressWarnings("unchecked")
    void testUnregisterListener() {
        when(transport.unregisterListener(any(UUri.class), any(Optional.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(null));
        transport.unregisterListener(SOURCE_FILTER, listener).toCompletableFuture().join();
        verify(transport).unregisterListener(eq(SOURCE_FILTER), eq(Optional.of(UriFactory.ANY)), eq(listener));
    }
}
