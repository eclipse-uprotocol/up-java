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


import org.eclipse.uprotocol.communication.UStatusException;
import org.eclipse.uprotocol.uri.serializer.UriSerializer;
import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UUri;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.stream.Stream;

class UriValidatorTest {

    @ParameterizedTest(name = "Test validate URI: {index} {arguments}")
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
        authorityName,             ueId,         version, resourceId, should succeed
        *,                         -1,           0xFF,    0xFFFF,     true
        myhost,                    0x0000_0A1B,  0x01,    0x2341,     true
        192.168.1.1,               0x0000_0A1B,  0x01,    0x2341,     true
        [2001::7],                 0x0000_0A1B,  0x01,    0x2341,     true
        invalid<<[],               0x0000_0A1B,  0x01,    0x2341,     false
        myhost:5555,               0x0000_0A1B,  0x01,    0x2341,     false
        user:passwd@myhost,        0x0000_0A1B,  0x01,    0x2341,     false
        MYHOST,                    0x0000_0A1B,  0x01,    0x2341,     false
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
        var authorityName = "a".repeat(authorityNameLength);
        var uri = UUri.newBuilder()
            .setAuthorityName(authorityName)
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

    static Stream<Arguments> verifyFilterCriteriaProvider() {
        var templateUriA = UUri.newBuilder()
            .setAuthorityName("vehicle1")
            .setUeId(0xaa)
            .setUeVersionMajor(0x01)
            .build();
        var templateUriB = UUri.newBuilder()
            .setAuthorityName("vehicle2")
            .setUeId(0xbb)
            .setUeVersionMajor(0x01)
            .build();
        return Stream.of(
            // source has authority name with upper-case letters
            Arguments.of(
                UUri.newBuilder(templateUriA).setAuthorityName("VEHICLE1").setResourceId(0x9000).build(),
                Optional.of(UUri.newBuilder(templateUriB).setResourceId(0x0000).build()),
                false),
            Arguments.of(
                UUri.newBuilder(templateUriA).setResourceId(0xFFFF).build(),
                Optional.of(UUri.newBuilder(templateUriB).setResourceId(0xFFFF).build()),
                true),
            Arguments.of(
                UUri.newBuilder(templateUriA).setResourceId(0x9000).build(),
                Optional.of(UUri.newBuilder(templateUriB).setResourceId(0x0000).build()),
                true),
            Arguments.of(
                UUri.newBuilder(templateUriA).setResourceId(0x0000).build(),
                Optional.of(UUri.newBuilder(templateUriB).setResourceId(0x0001).build()),
                true),
            // source and sink both have resource ID 0
            Arguments.of(
                UUri.newBuilder(templateUriA).setResourceId(0x0000).build(),
                Optional.of(UUri.newBuilder(templateUriB).setResourceId(0x0000).build()),
                false),
            Arguments.of(
                UUri.newBuilder(templateUriA).setResourceId(0xFFFF).build(),
                Optional.of(UUri.newBuilder(templateUriB).setResourceId(0x001a).build()),
                true),
            Arguments.of(
                UUri.newBuilder(templateUriA).setResourceId(0x0000).build(),
                Optional.of(UUri.newBuilder(templateUriB).setResourceId(0x001a).build()),
                true),
            // sink is RPC but source has invalid resource ID
            Arguments.of(
                UUri.newBuilder(templateUriA).setResourceId(0x00cc).build(),
                Optional.of(UUri.newBuilder(templateUriB).setResourceId(0x001a).build()),
                false),
            Arguments.of(
                UUri.newBuilder(templateUriA).setResourceId(0x9000).build(),
                Optional.empty(),
                true),
            Arguments.of(
                UUri.newBuilder(templateUriA).setResourceId(0xFFFF).build(),
                Optional.empty(),
                true),
            // sink is empty but source has non-topic resource ID
            Arguments.of(
                UUri.newBuilder(templateUriA).setResourceId(0x00cc).build(),
                Optional.empty(),
                false)
        );
    }

    @ParameterizedTest(name = "Test verifyFilterCriteria: {index} {arguments}")
    @MethodSource("verifyFilterCriteriaProvider")
    void testVerifyFilterCriteriaFails(UUri sourceFilter, Optional<UUri> sinkFilter, boolean shouldSucceed) {
        if (shouldSucceed) {
            assertDoesNotThrow(() -> UriValidator.verifyFilterCriteria(sourceFilter, sinkFilter));
        } else {
            UStatusException exception = assertThrows(
                UStatusException.class,
                () -> UriValidator.verifyFilterCriteria(sourceFilter, sinkFilter));
            assertEquals(UCode.INVALID_ARGUMENT, exception.getCode());
        }
    }
}
