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

import org.eclipse.uprotocol.uri.validator.UriValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;


class UriSerializerTest {

    @Test
    @DisplayName("Test serializing a null UUri fails")
    void testSerializingANullUuri() {
        assertThrows(NullPointerException.class, () -> UriSerializer.serialize(null));
    }

    @Test
    @DisplayName("Test deserializing a null UUri fails")
    void testDeserializingANullUuriFails() {
        assertThrows(NullPointerException.class, () -> UriSerializer.deserialize((String) null));
        assertThrows(NullPointerException.class, () -> UriSerializer.deserialize((URI) null));
    }

    @Test
    @DisplayName("Test deserializing a UUri with authority name exceeding max length fails")
    // [utest->dsn~uri-authority-name-length~1]
    void testDeserializeRejectsAuthorityNameExceedingMaxLength() {
        String authority = "a".repeat(UriValidator.AUTHORITY_NAME_MAX_LENGTH);
        String validUri = "up://%s/ABCD/1/1001".formatted(authority);
        assertDoesNotThrow(() -> UriSerializer.deserialize(validUri));

        authority = "a".repeat(UriValidator.AUTHORITY_NAME_MAX_LENGTH + 1);
        var invalidUri = "up://%s/ABCD/1/1001".formatted(authority);
        assertThrows(IllegalArgumentException.class, () -> UriSerializer.deserialize(invalidUri));
    }
}
