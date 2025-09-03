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
package org.eclipse.uprotocol.uuid.serializer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.uprotocol.v1.UUID;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class UuidSerializerTest {

    @ParameterizedTest(name = "Test deserializing an invalid UUID string [{index}]: {arguments}")
    @NullSource
    @EmptySource
    @ValueSource(strings = {"invalid-uuid-string"})
    void testDeserializeHandlesInvalidString(String uuidString) {
        final var uuid = UuidSerializer.deserialize(uuidString);
        assertEquals(UUID.getDefaultInstance(), uuid);
    }
}
