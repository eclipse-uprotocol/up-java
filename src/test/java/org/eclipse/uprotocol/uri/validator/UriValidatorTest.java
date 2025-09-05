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
package org.eclipse.uprotocol.uri.validator;


import org.eclipse.uprotocol.uri.serializer.UriSerializer;
import org.eclipse.uprotocol.v1.UUri;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

class UriValidatorTest {

    @ParameterizedTest(name = "Test validate URI: {index} {arguments}")
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
        authorityName,             ueId,         version, resourceId, should succeed
        *,                         -1,           0xFF,    0xFFFF,     true
        myhost,                    0x0000_0A1B,  0x01,    0x2341,     true
        invalid<<[],               0x0000_0A1B,  0x01,    0x2341,     false
        myhost:5555,               0x0000_0A1B,  0x01,    0x2341,     false
        user:passwd@myhost,        0x0000_0A1B,  0x01,    0x2341,     false
        myhost,                    0x0000_0A1B,  -1,      0x2341,     false
        myhost,                    0x0000_0A1B,  0x100,   0x2341,     false
        myhost,                    0x0000_0A1B,  0x01,    -1,         false
        myhost,                    0x0000_0A1B,  0x01,    0x10000,    false
        """
    )
    void testValidate(
        String authorityName,
        int ueId,
        int ueVersionMajor,
        int resourceId,
        boolean shouldSucceed) {
        UUri uuri = UUri.newBuilder()
            .setAuthorityName(authorityName)
            .setUeId(ueId)
            .setUeVersionMajor(ueVersionMajor)
            .setResourceId(resourceId)
            .build();
        if (shouldSucceed) {
            assertDoesNotThrow(() -> UriValidator.validate(uuri));
        } else {
            assertThrows(IllegalArgumentException.class, () -> {
                UriValidator.validate(uuri);
            });
        }
    }

    @ParameterizedTest(name = "Test validate checks maximum length of authority name: {index} - {arguments}")
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
        authorityNameLength, should succeed
        128,                  true
        129,                  false
        """)
    void testValidateFailsForAuthorityExceedingMaxLength(int authorityNameLength, boolean shouldSucceed) {
        var authorityName = new char[authorityNameLength];
        Arrays.fill(authorityName, 'A');
        var uri = UUri.newBuilder()
            .setAuthorityName(new String(authorityName))
            .setUeId(0x1234)
            .setUeVersionMajor(0x01)
            .setResourceId(0x0001)
            .build();
        if (shouldSucceed) {
            assertDoesNotThrow(() -> UriValidator.validate(uri));
        } else {
            assertThrows(IllegalArgumentException.class, () -> UriValidator.validate(uri));
        }
    }

    @ParameterizedTest(name = "Test isRpcMethod: {index} {arguments}")
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
        uri,                    should succeed
        //myhost/A1B/1/0,       false
        //myhost/A1B/1/1,       true
        //myhost/A1B/1/2341,    true
        //myhost/A1B/1/7FFF,    true
        //myhost/A1B/1/8000,    false
        //myhost/A1B/1/FFFF,    false
        """
    )
    void testIsRpcMethod(String uri, boolean shouldSucceed) {
        UUri uuri = UriSerializer.deserialize(uri);
        if (shouldSucceed) {
            assertTrue(UriValidator.isRpcMethod(uuri));
        } else {
            assertFalse(UriValidator.isRpcMethod(uuri));
        }
    }

    @ParameterizedTest(name = "Test isRpcResponse: {index} {arguments}")
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
        uri,                    should succeed
        //myhost/A1B/1/0,       true
        //myhost/A1B/1/1,       false
        //myhost/A1B/1/7FFF,    false
        //myhost/A1B/1/8000,    false
        //myhost/A1B/1/FFFF,    false
        """
    )
    void testIsRpcResponse(String uri, boolean shouldSucceed) {
        UUri uuri = UriSerializer.deserialize(uri);
        if (shouldSucceed) {
            assertTrue(UriValidator.isRpcResponse(uuri));
        } else {
            assertFalse(UriValidator.isRpcResponse(uuri));
        }
    }

    @ParameterizedTest(name = "Test isTopic: {index} {arguments}")
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
        uri,                    should succeed
        //myhost/A1B/1/0,       false
        //myhost/A1B/1/1,       false
        //myhost/A1B/1/7FFF,    false
        //myhost/A1B/1/8000,    true
        //myhost/A1B/1/ABCD,    true
        //myhost/A1B/1/FFFE,    true
        //myhost/A1B/1/FFFF,    false
        """
    )
    void testIsTopic(String uri, boolean shouldSucceed) {
        UUri uuri = UriSerializer.deserialize(uri);
        if (shouldSucceed) {
            assertTrue(UriValidator.isTopic(uuri));
        } else {
            assertFalse(UriValidator.isTopic(uuri));
        }
    }

    @ParameterizedTest(name = "Test hasWildcard: {index} {arguments}")
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
        uri,                    should succeed
        //myhost/A1B/1/0,       false
        //*/A1B/1/1,            true
        //myhost/FFFF/1/2,      true
        //myhost/FFFF0000/1/2,  true
        //myhost/FFFFFFFF/1/1,  true
        //myhost/A1B/FF/8000,   true
        //myhost/A1B/1/FFFF,    true
        //*/FFFFFFFF/FF/FFFF,   true
        """
    )
    void testHasWildcard(String uri, boolean shouldSucceed) {
        UUri uuri = UriSerializer.deserialize(uri);
        if (shouldSucceed) {
            assertTrue(UriValidator.hasWildcard(uuri));
        } else {
            assertFalse(UriValidator.hasWildcard(uuri));
        }
    }

    @ParameterizedTest(name = "Test matches: {index} {arguments}")
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
        pattern,                     candidate,                 should match
        //authority/A410/3/1003,     //authority/A410/3/1003,   true
        //authority/2A410/3/1003,    //authority/2A410/3/1003,  true
        //*/A410/3/1003,             //authority/A410/3/1003,   true
        //*/A410/3/1003,             /A410/3/1003,              true
        //authority/FFFF/3/1003,     //authority/A410/3/1003,   true
        //authority/FFFFA410/3/1003, //authority/2A410/3/1003,  true
        //authority/A410/FF/1003,    //authority/A410/3/1003,   true
        //authority/A410/3/FFFF,     //authority/A410/3/1003,   true
        //Authority/A410/3/1003,     //authority/A410/3/1003,   false
        //other/A410/3/1003,         //authority/A410/3/1003,   false
        /A410/3/1003,                //authority/A410/3/1003,   false
        //authority/45/3/1003,       //authority/A410/3/1003,   false
        //authority/2A410/3/1003,    //authority/A410/3/1003,   false
        //authority/A410/1/1003,     //authority/A410/3/1003,   false
        //authority/A410/3/ABCD,     //authority/A410/3/1003,   false
        """
    )
    void testMatches(String pattern, String candidate, boolean shouldMatch) {
        UUri patternUri = UriSerializer.deserialize(pattern);
        UUri candidateUri = UriSerializer.deserialize(candidate);
        if (shouldMatch) {
            assertTrue(UriValidator.matches(patternUri, candidateUri));
        } else {
            assertFalse(UriValidator.matches(patternUri, candidateUri));
        }
    }
}
