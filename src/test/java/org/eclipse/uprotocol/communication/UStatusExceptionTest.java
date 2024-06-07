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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UStatus;

public class UStatusExceptionTest {
    
    @Test
    @DisplayName("Test UStatusException constructor")
    public void testUStatusExceptionConstructor() {
        UStatusException exception = new UStatusException(UCode.INVALID_ARGUMENT, "Invalid message type");

        assertEquals(UCode.INVALID_ARGUMENT, exception.getCode());
        assertEquals("Invalid message type", exception.getMessage());
    }

    @Test
    @DisplayName("Test UStatusException constructor passing null")
    public void testUStatusExceptionConstructorNull() {
        UStatusException exception = new UStatusException(null);

        assertEquals(UCode.UNKNOWN, exception.getCode());
        assertEquals("", exception.getMessage());
    }

    @Test
    @DisplayName("Test UStatusException constructor passing a UStatus")
    public void testUStatusExceptionConstructorUStatus() {
        UStatus status = UStatus.newBuilder()
            .setCode(UCode.INVALID_ARGUMENT)
            .setMessage("Invalid message type")
            .build();
        UStatusException exception = new UStatusException(status);

        assertEquals(UCode.INVALID_ARGUMENT, exception.getCode());
        assertEquals("Invalid message type", exception.getMessage());
    }

    @Test
    @DisplayName("Test UStatusException getStatus")
    public void testGetStatus() {
        UStatus status = UStatus.newBuilder()
            .setCode(UCode.INVALID_ARGUMENT)
            .setMessage("Invalid message type")
            .build();
        UStatusException exception = new UStatusException(status);

        assertEquals(status, exception.getStatus());
    }

    @Test
    @DisplayName("Test UStatusException padding a throwable cause")
    public void testUStatusExceptionThrowable() {
        Throwable cause = new Throwable("This is a cause");
        UStatusException exception = new UStatusException(UCode.INVALID_ARGUMENT, "Invalid message type", cause);

        assertEquals(UCode.INVALID_ARGUMENT, exception.getCode());
        assertEquals("Invalid message type", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

}
