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
package org.eclipse.uprotocol.v1;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

import com.google.protobuf.Any;
import com.google.protobuf.StringValue;

class UStatusTest {
    @Test
    // [utest->req~ustatus-data-model-impl~1]
    void testUStatusHasRequiredFields() {
        var details = Any.pack(StringValue.of("Hello"));
        var otherDetails = Any.pack(StringValue.of("there"));
        UStatus status = UStatus.newBuilder()
            .setCode(UCode.INTERNAL)
            .setMessage("Internal error")
            .addDetails(0, details)
            .addDetails(otherDetails)
            .build();
        assertEquals(UCode.INTERNAL, status.getCode());
        assertEquals("Internal error", status.getMessage());
        assertEquals(details, status.getDetails(0));
        assertEquals(otherDetails, status.getDetails(1));
    }

    @Test
    // [utest->req~ustatus-data-model-proto~1]
    void testProtoSerialization() {
        var ustatus = UStatus.newBuilder()
            .setCode(UCode.CANCELLED)
            .setMessage("the message")
            .addDetails(Any.pack(StringValue.of("Hello")))
            .build();
        var proto = ustatus.toByteString();
        assertDoesNotThrow(
            () -> {
                var deserializedStatus = UStatus.parseFrom(proto);
                assertEquals(ustatus, deserializedStatus);
            });
    }
}
