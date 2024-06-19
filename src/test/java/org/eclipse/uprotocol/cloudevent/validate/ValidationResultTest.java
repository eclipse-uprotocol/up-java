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
package org.eclipse.uprotocol.cloudevent.validate;

import org.eclipse.uprotocol.v1.UStatus;
import org.eclipse.uprotocol.v1.UCode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.uprotocol.validation.ValidationResult;

class ValidationResultTest {

    @Test
    @DisplayName("Test success validation result to string")
    void test_success_validation_result_toString() {
        ValidationResult success = ValidationResult.success();
        assertEquals("ValidationResult.Success()", success.toString());
    }

    @Test
    @DisplayName("Test failure validation result to string")
    void test_failure_validation_result_toString() {
        ValidationResult failure = ValidationResult.failure("boom");
        assertEquals("ValidationResult.Failure(message='boom')", failure.toString());
    }

    @Test
    @DisplayName("Test success validation result isSuccess")
    void test_success_validation_result_isSuccess() {
        ValidationResult success = ValidationResult.success();
        assertTrue(success.isSuccess());
    }

    @Test
    @DisplayName("Test failure validation result isSuccess")
    void test_failure_validation_result_isSuccess() {
        ValidationResult failure = ValidationResult.failure("boom");
        assertFalse(failure.isSuccess());
    }

    @Test
    @DisplayName("Test success message")
    void test_success_validation_result_getMessage() {
        ValidationResult success = ValidationResult.success();
        assertTrue(success.getMessage().isBlank());
    }

    @Test
    @DisplayName("Test failure message")
    void test_failure_validation_result_getMessage() {
        ValidationResult failure = ValidationResult.failure("boom");
        assertEquals("boom", failure.getMessage());
    }

    @Test
    @DisplayName("Test success toStatus")
    void test_success_validation_result_toStatus() {
        ValidationResult success = ValidationResult.success();
        assertEquals(ValidationResult.STATUS_SUCCESS, success.toStatus());
    }

    @Test
    @DisplayName("Test failure toStatus")
    void test_failure_validation_result_toStatus() {
        ValidationResult failure = ValidationResult.failure("boom");
        final UStatus status = UStatus.newBuilder().setCode(UCode.INVALID_ARGUMENT).setMessage("boom").build();
        assertEquals(status, failure.toStatus());
    }

}