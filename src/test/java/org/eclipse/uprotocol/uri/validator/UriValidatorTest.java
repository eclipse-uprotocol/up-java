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

    @ParameterizedTest(name = "Test URI matches pattern: {index} {arguments}")
    @CsvSource(useHeadersInDisplayName = true, delimiter = '|', textBlock = """
        uri                         | pattern
        /1/1/A1FB                   | /1/1/A1FB
        /1/1/A1FB                   | //*/1/1/A1FB
        /1/1/A1FB                   | /FFFF/1/A1FB
        /1/1/A1FB                   | //*/FFFF/1/A1FB
        /1/1/A1FB                   | /FFFFFFFF/1/A1FB
        /1/1/A1FB                   | //*/FFFFFFFF/1/A1FB
        /1/1/A1FB                   | /1/FF/A1FB
        /1/1/A1FB                   | //*/1/FF/A1FB
        /1/1/A1FB                   | /1/1/FFFF
        /1/1/A1FB                   | //*/1/1/FFFF
        /1/1/A1FB                   | /FFFFFFFF/FF/FFFF
        /1/1/A1FB                   | //*/FFFFFFFF/FF/FFFF
        /10001/1/A1FB               | /10001/1/A1FB
        /10001/1/A1FB               | //*/10001/1/A1FB
        /10001/1/A1FB               | /FFFFFFFF/1/A1FB
        /10001/1/A1FB               | //*/FFFFFFFF/1/A1FB
        /10001/1/A1FB               | /FFFFFFFF/FF/FFFF
        /10001/1/A1FB               | //*/FFFFFFFF/FF/FFFF
        //vcu.my_vin/1/1/A1FB       | //vcu.my_vin/1/1/A1FB
        //vcu.my_vin/1/1/A1FB       | //*/1/1/A1FB
        """
    )
    // TODO: replace with Cucumber based test in UuriTests.java
    // [utest->dsn~uri-pattern-matching~2]
    void testMatchesSucceeds(String uri, String pattern) {
        UUri patternUri = UriSerializer.deserialize(pattern);
        UUri candidateUri = UriSerializer.deserialize(uri);
        assertTrue(UriValidator.matches(patternUri, candidateUri));
    }

    @ParameterizedTest(name = "Test URI does not match pattern: {index} {arguments}")
    @CsvSource(useHeadersInDisplayName = true, delimiter = '|', textBlock = """
        uri                   | pattern
        /1/1/A1FB             | //mcu1/1/1/A1FB
        //vcu.my_vin/1/1/A1FB | //mcu1/1/1/A1FB
        //vcu/B1A5/1/A1FB     | //vc/FFFFFFFF/FF/FFFF
        /B1A5/1/A1FB          | //*/25B1/FF/FFFF
        /B1A5/1/A1FB          | //*/FFFFFFFF/2/FFFF
        /B1A5/1/A1FB          | //*/FFFFFFFF/FF/ABCD
        /B1A5/1/A1FB          | /25B1/1/A1FB
        /B1A5/1/A1FB          | /2B1A5/1/A1FB
        /10B1A5/1/A1FB        | /40B1A5/1/A1FB
        /B1A5/1/A1FB          | /B1A5/4/A1FB
        /B1A5/1/A1FB          | /B1A5/1/90FB
        """
    )
    // TODO: replace with Cucumber based test in UuriTests.java
    // [utest->dsn~uri-pattern-matching~2]
    void testMatchesFails(String uri, String pattern) {
        UUri patternUri = UriSerializer.deserialize(pattern);
        UUri candidateUri = UriSerializer.deserialize(uri);
        assertFalse(UriValidator.matches(patternUri, candidateUri));
    }
}
