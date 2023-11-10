/*
 * Copyright (c) 2023 General Motors GTO LLC
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * SPDX-FileType: SOURCE
 * SPDX-FileCopyrightText: 2023 General Motors GTO LLC
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.uprotocol.cloudevent.validate;

import com.google.rpc.Code;
import com.google.rpc.Status;
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
        final Status status = Status.newBuilder().setCode(Code.INVALID_ARGUMENT_VALUE).setMessage("boom").build();
        assertEquals(status, failure.toStatus());
    }

}