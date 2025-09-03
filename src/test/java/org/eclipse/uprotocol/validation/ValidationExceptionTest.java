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
package org.eclipse.uprotocol.validation;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;

class ValidationExceptionTest {

    @Test
    void testGetCauses() {
        final List<Exception> causes = List.of(
            new ValidationException("First cause"),
            new IllegalArgumentException("Second cause")
        );
        ValidationException exception = new ValidationException(causes);
        Truth.assertThat(exception.getCauses()).containsExactlyElementsIn(causes);
    }

    @Test
    void testGetMessage() {
        final List<Exception> causes = List.of(
            new ValidationException("First cause"),
            new IllegalArgumentException("Second cause")
        );
        ValidationException exception = new ValidationException(causes);
        String errorMessages = exception.getMessage();
        assertEquals("First cause,Second cause", errorMessages);
    }
}
