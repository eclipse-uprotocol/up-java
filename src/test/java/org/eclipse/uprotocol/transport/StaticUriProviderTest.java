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
package org.eclipse.uprotocol.transport;

import static org.junit.Assert.assertEquals;

import org.eclipse.uprotocol.v1.UUri;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StaticUriProviderTest {

    private static UUri LOCAL_URI = UUri.newBuilder()
        .setAuthorityName("my-authority")
        .setUeId(0x1234)
        .setUeVersionMajor(0x01)
        .build();

    private LocalUriProvider uriProvider;

    @BeforeEach
    void createUriProvider() {
        uriProvider = StaticUriProvider.of("my-authority", 0x1234, 0x01);
    }

    @Test
    void testFactoryMethod() {
        var provider = StaticUriProvider.of(LOCAL_URI);
        assertEquals(LOCAL_URI, provider.getSource());
    }

    @Test
    void testGetAuthorityReturnsAuthorityName() {
        assertEquals("my-authority", uriProvider.getAuthority());
    }

    @Test
    void testGetSourceReturnsUri() {
        var source = uriProvider.getSource();
        assertEquals("my-authority", source.getAuthorityName());
        assertEquals(0x1234, source.getUeId());
        assertEquals(0x01, source.getUeVersionMajor());
        assertEquals(0, source.getResourceId());
    }

    @Test
    void testGetResourceReturnsUri() {
        var resource = uriProvider.getResource(0xabcd);
        assertEquals("my-authority", resource.getAuthorityName());
        assertEquals(0x1234, resource.getUeId());
        assertEquals(0x01, resource.getUeVersionMajor());
        assertEquals(0xabcd, resource.getResourceId());
    }
}
