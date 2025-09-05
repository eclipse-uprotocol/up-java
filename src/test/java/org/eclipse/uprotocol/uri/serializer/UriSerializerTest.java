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
package org.eclipse.uprotocol.uri.serializer;

import org.eclipse.uprotocol.v1.UUri;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.util.Optional;


class UriSerializerTest {

    @ParameterizedTest(name = "Test serializing a valid UUri succeeds [{index}] {arguments}")
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
            authority,     ueId,          ueVersion, resourceId, expectedUri
            ,              ,              ,          ,           /0/0/0
            myAuthority,   ,              ,          ,           //myAuthority/0/0/0
            myAuthority,   0x0000_abcd,   ,          ,           //myAuthority/ABCD/0/0
            myAuthority,   0x0000_abcd,   0x02,      ,           //myAuthority/ABCD/2/0
            myAuthority,   0x001f_abcd,   0x02,      0x3d4b,     //myAuthority/1FABCD/2/3D4B
            *,             0xb1a,         0x01,      0x8aa1,     //*/B1A/1/8AA1
            myAuthority,   0x0000_ffff,   0x01,      0x8aa1,     //myAuthority/FFFF/1/8AA1
            # using -62694 to represent 0xffff_0b1a which fails to be parsed by CsvSource
            # because CsvSource does not support parsing hex strings as unsigned integers
            myAuthority,   -62694,        0x01,      0x8aa1,     //myAuthority/FFFF0B1A/1/8AA1
            myAuthority,   0xb1a,         0xff,      0x8aa1,     //myAuthority/B1A/FF/8AA1
            myAuthority,   0xb1a,         0x01,      0xffff,     //myAuthority/B1A/1/FFFF
            # using -1 (2s complement) to represent 0xffff_ffff which fails to be parsed by CsvSource
            # because CsvSource does not support parsing hex strings as unsigned integers
            *,             -1,            0xff,      0xffff,     //*/FFFFFFFF/FF/FFFF
            """)
    void testSerializingValidUUriSucceeds(
            String authority,
            Integer ueId,
            Integer ueVersion,
            Integer resourceId,
            String expectedUri) {
        final var builder = UUri.newBuilder();
        Optional.ofNullable(authority).ifPresent(builder::setAuthorityName);
        Optional.ofNullable(ueId).ifPresent(builder::setUeId);
        Optional.ofNullable(ueVersion).ifPresent(builder::setUeVersionMajor);
        Optional.ofNullable(resourceId).ifPresent(builder::setResourceId);
        UUri originalUuri = builder.build();

        String correspondingUri = UriSerializer.serialize(originalUuri);
        assertEquals(expectedUri, correspondingUri);
        assertEquals(originalUuri, UriSerializer.deserialize(correspondingUri));
    }

    @Test
    @DisplayName("Test serializing a null UUri fails")
    void testSerializingANullUuri() {
        assertThrows(NullPointerException.class, () -> {
            UriSerializer.serialize(null);
        });
    }

    //
    // tests for deserializing URIs
    //

    @ParameterizedTest(name = "Test deserializing a valid Uri succeeds [{index}] {arguments}")
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
            URI,                    expectedAuthority, expectedUeId, expectedVersion, expectedResourceId
            /0/0/0,                 ,                  0x0000_0000,  0x00,            0x0000
            up:/0/0/0,              ,                  0x0000_0000,  0x00,            0x0000
            //auth.dev/0/0/0,       auth.dev,          0x0000_0000,  0x00,            0x0000
            //192.168.1.0/ABCD/0/0, 192.168.1.0,       0x0000_abcd,  0x00,            0x0000
            //auth/ABCD/2/0,        auth,              0x0000_abcd,  0x02,            0x0000
            up://auth/ABCD/2/3D4B,  auth,              0x0000_abcd,  0x02,            0x3d4b
            /1234/1/5678,           ,                  0x0000_1234,  0x01,            0x5678
            //*/1234/1/5678,        *,                 0x0000_1234,  0x01,            0x5678
            //auth/FFFF/1/5678,     auth,              0x0000_ffff,  0x01,            0x5678
            # using -62694 to represent 0xffff_0b1a which fails to be parsed by CsvSource
            # because CsvSource does not support parsing hex strings as unsigned integers
            //auth/FFFF0B1A/1/5678, auth,              -62694,       0x01,            0x5678
            //auth/1234/FF/5678,    auth,              0x0000_1234,  0xff,            0x5678
            //auth/1234/1/FFFF,     auth,              0x0000_1234,  0x01,            0xffff
            # using -1 to represent 0xffff_ffff which fails to be parsed by CsvSource
            # because CsvSource does not support parsing hex strings as unsigned integers
            //*/FFFFFFFF/FF/FFFF,   *,                 -1,           0xff,            0xffff
            """)
    void testDeserializeValidUriSucceeds(
            String uri,
            String expectedAuthority,
            Integer expectedUeId,
            Integer expectedVersion,
            Integer expectedResourceId) {

        final var uuri = UriSerializer.deserialize(uri);
        Optional.ofNullable(expectedAuthority).ifPresent(s -> assertEquals(s, uuri.getAuthorityName()));
        Optional.ofNullable(expectedUeId).ifPresent(s -> assertEquals(s, uuri.getUeId()));
        Optional.ofNullable(expectedVersion).ifPresent(s -> assertEquals(s, uuri.getUeVersionMajor()));
        Optional.ofNullable(expectedResourceId).ifPresent(s -> assertEquals(s, uuri.getResourceId()));
    }

    @Test
    @DisplayName("Test deserializing a null UUri fails")
    public void testDeserializingANullUuriFails() {
        assertThrows(NullPointerException.class, () -> {
            UriSerializer.deserialize((String) null);
        });
        assertThrows(NullPointerException.class, () -> {
            UriSerializer.deserialize((URI) null);
        });
    }

    @ParameterizedTest(name = "Test deserializing an invalid URI fails [{index}] {arguments}")
    @ValueSource(strings = {
        "  ",
        "$$",
        "up://",
        "up://just_an_authority",
        "/ABC",
        "/ABC/1",
        "//myhost/ABC",
        "//myhost/ABC/1",
        "//myhost//1/A000",
        "//myhost/ABC//A000",
        "//myhost/ABC/1//",
        "//myhost/not-hex/1/2341",
        "//myhost/1/not-hex/2341",
        "//myhost/1/1/not-hex",
        "//myhost/-1/1/2341",
        "invalidscheme://myhost/A000/1/2341",
        "up://myhost/A000/1/2341#invalid",
        "up://myhost/A000/1/2341?param=invalid",
        "up://myhost/100000000/1/2341",
        "//myhost/A1B/-1/2341",
        "//myhost/A1B/100/2341",
        "up://myhost/A1B/1/-1",
        "//myhost/A1B/1/10000"
    })
    void testDeserializeInvalidUriFails(String uri) {
        assertThrows(IllegalArgumentException.class, () -> {
            UriSerializer.deserialize(uri);
        });
    }
}
