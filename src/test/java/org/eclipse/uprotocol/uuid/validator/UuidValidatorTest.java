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

package org.eclipse.uprotocol.uuid.validator;

import com.google.rpc.Code;
import com.google.rpc.Status;
import org.eclipse.uprotocol.uuid.factory.UUIDFactory;
import org.eclipse.uprotocol.uuid.factory.UUIDUtils;
import org.eclipse.uprotocol.uuid.validate.UuidValidator;
import org.eclipse.uprotocol.v1.UUID;
import org.eclipse.uprotocol.validation.ValidationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class UuidValidatorTest {
    @Test
    @DisplayName("Test validator with good uuid")
    void test_validator_with_good_uuid() {
        //final UuidValidator validator = new UuidValidator();
        final UUID uuid = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final Status status = UuidValidator.getValidator(uuid).validate(uuid);
        assertEquals(ValidationResult.STATUS_SUCCESS, status);
    }

    @Test
    @DisplayName("Test Good uuid Check")
    void test_good_uuid_string() {
        final Status status = UuidValidator.Validators.UPROTOCOL.validator()
                .validate(UUIDFactory.Factories.UPROTOCOL.factory().create());
        assertEquals(status, ValidationResult.STATUS_SUCCESS);
    }

    @Test
    @DisplayName("Test fetching the invalid Validator for when UUID passed is garbage")
    void test_invalid_uuid() {
        final UUID uuid = UUID.newBuilder().setMsb(0L).setLsb(0L).build();
        final Status status = UuidValidator.getValidator(uuid).validate(uuid);
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("Invalid UUID Version,Invalid UUID Variant,Invalid UUID Time", status.getMessage());
    }

    @Test
    @DisplayName("Test invalid time uuid")
    void test_invalid_time_uuid() {
        final UUID uuid = UUIDFactory.Factories.UPROTOCOL.factory().create(Instant.ofEpochSecond(0));
        final Status status = UuidValidator.Validators.UPROTOCOL.validator().validate(uuid);
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("Invalid UUID Time", status.getMessage());
    }

    @Test
    @DisplayName("Test UUIDv8 validator for null UUID")
    void test_uuidv8_with_invalid_uuids() {
        final UuidValidator validator = UuidValidator.Validators.UPROTOCOL.validator();
        assertNotNull(validator);
        final Status status = validator.validate(null);
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("Invalid UUIDv8 Version,Invalid UUID Time", status.getMessage());
    }

    @Test
    @DisplayName("Test UUIDv8 validator for invalid types")
    void test_uuidv8_with_invalid_types() {
        final UUID uuidv6 = UUIDFactory.Factories.UUIDV6.factory().create();
        final UUID uuid = UUID.newBuilder().setMsb(0L).setLsb(0L).build();
        final java.util.UUID uuid_java = java.util.UUID.randomUUID();
        final UUID uuidv4 = UUID.newBuilder().setMsb(uuid_java.getMostSignificantBits())
                .setLsb(uuid_java.getLeastSignificantBits()).build();

        final UuidValidator validator = UuidValidator.Validators.UPROTOCOL.validator();
        assertNotNull(validator);

        final Status status = validator.validate(uuidv6);
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("Invalid UUIDv8 Version", status.getMessage());

        final Status status1 = validator.validate(uuid);
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status1.getCode());
        assertEquals("Invalid UUIDv8 Version,Invalid UUID Time", status1.getMessage());

        final Status status2 = validator.validate(uuidv4);
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status2.getCode());
        assertEquals("Invalid UUIDv8 Version,Invalid UUID Time", status2.getMessage());
    }

    @Test
    @DisplayName("Test good UUIDv6")
    void test_good_uuidv6() {
        final UUID uuid = UUIDFactory.Factories.UUIDV6.factory().create();

        UuidValidator validator = UuidValidator.getValidator(uuid);
        assertNotNull(validator);
        assertTrue(UUIDUtils.isUuidv6(uuid));
        assertEquals(Code.OK_VALUE, validator.validate(uuid).getCode());
    }


    @Test
    @DisplayName("Test UUIDv6 with bad variant")
    void test_uuidv6_with_bad_variant() {
        final Optional<UUID> uuid = UUIDUtils.fromString("1ee57e66-d33a-65e0-4a77-3c3f061c1e9e");
        assertTrue(uuid.isPresent());
        final UuidValidator validator = UuidValidator.getValidator(uuid.get());
        assertNotNull(validator);
        final Status status = validator.validate(uuid.get());
        assertEquals("Invalid UUID Version,Invalid UUID Variant,Invalid UUID Time", status.getMessage());
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
    }

    @Test
    @DisplayName("Test UUIDv6 with invalid UUID")
    void test_uuidv6_with_invalid_uuid() {

        final UUID uuid = UUID.newBuilder().setMsb(9 << 12).setLsb(0L).build();
        final UuidValidator validator = UuidValidator.Validators.UUIDV6.validator();
        assertNotNull(validator);
        final Status status = validator.validate(uuid);
        assertEquals("Not a UUIDv6 Version,Invalid UUIDv6 variant,Invalid UUID Time", status.getMessage());
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
    }


    @Test
    @DisplayName("Test using UUIDv6 Validator to validate null UUID")
    void test_uuidv6_with_null_uuid() {
        final UuidValidator validator = UuidValidator.Validators.UUIDV6.validator();
        assertNotNull(validator);
        final Status status = validator.validate(null);
        assertEquals("Not a UUIDv6 Version,Invalid UUIDv6 variant,Invalid UUID Time", status.getMessage());
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
    }

    @Test
    @DisplayName("Test using UUIDv6 Validator to validate a different types of UUIDs")
    void test_uuidv6_with_uuidv8() {
        final UUID uuid = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final UuidValidator validator = UuidValidator.Validators.UUIDV6.validator();
        assertNotNull(validator);
        final Status status = validator.validate(uuid);
        assertEquals("Not a UUIDv6 Version", status.getMessage());
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
    }

}
