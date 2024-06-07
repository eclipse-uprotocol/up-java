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
package org.eclipse.uprotocol.uuid.validator;

import org.eclipse.uprotocol.uuid.factory.UuidFactory;
import org.eclipse.uprotocol.uuid.factory.UuidUtils;
import org.eclipse.uprotocol.uuid.serializer.UuidSerializer;
import org.eclipse.uprotocol.uuid.validate.UuidValidator;
import org.eclipse.uprotocol.v1.UUID;
import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UStatus;
import org.eclipse.uprotocol.validation.ValidationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

public class UuidValidatorTest {
    @Test
    @DisplayName("Test validator with good uuid")
    void test_validator_with_good_uuid() {
        //final UuidValidator validator = new UuidValidator();
        final UUID uuid = UuidFactory.Factories.UPROTOCOL.factory().create();
        final UStatus status = UuidValidator.getValidator(uuid).validate(uuid);
        assertEquals(ValidationResult.STATUS_SUCCESS, status);
    }

    @Test
    @DisplayName("Test Good uuid Check")
    void test_good_uuid_string() {
        final UStatus status = UuidValidator.Validators.UPROTOCOL.validator()
                .validate(UuidFactory.Factories.UPROTOCOL.factory().create());
        assertEquals(status, ValidationResult.STATUS_SUCCESS);
    }

    @Test
    @DisplayName("Test fetching the invalid Validator for when UUID passed is garbage")
    void test_invalid_uuid() {
        final UUID uuid = UUID.newBuilder().setMsb(0L).setLsb(0L).build();
        final UStatus status = UuidValidator.getValidator(uuid).validate(uuid);
        assertEquals(UCode.INVALID_ARGUMENT, status.getCode());
        assertEquals("Invalid UUID Version,Invalid UUID Variant,Invalid UUID Time", status.getMessage());
    }

    @Test
    @DisplayName("Test invalid time uuid")
    void test_invalid_time_uuid() {
        final UUID uuid = UuidFactory.Factories.UPROTOCOL.factory().create(Instant.ofEpochSecond(0));
        final UStatus status = UuidValidator.Validators.UPROTOCOL.validator().validate(uuid);
        assertEquals(UCode.INVALID_ARGUMENT, status.getCode());
        assertEquals("Invalid UUID Time", status.getMessage());
    }

    @Test
    @DisplayName("Test UUIDv8 validator for null UUID")
    void test_uuidv8_with_invalid_uuids() {
        final UuidValidator validator = UuidValidator.Validators.UPROTOCOL.validator();
        assertNotNull(validator);
        final UStatus status = validator.validate(null);
        assertEquals(UCode.INVALID_ARGUMENT, status.getCode());
        assertEquals("Invalid UUIDv8 Version,Invalid UUID Time", status.getMessage());
    }

    @Test
    @DisplayName("Test UUIDv8 validator for invalid types")
    void test_uuidv8_with_invalid_types() {
        final UUID uuidv6 = UuidFactory.Factories.UUIDV6.factory().create();
        final UUID uuid = UUID.newBuilder().setMsb(0L).setLsb(0L).build();
        final java.util.UUID uuid_java = java.util.UUID.randomUUID();
        final UUID uuidv4 = UUID.newBuilder().setMsb(uuid_java.getMostSignificantBits())
                .setLsb(uuid_java.getLeastSignificantBits()).build();

        final UuidValidator validator = UuidValidator.Validators.UPROTOCOL.validator();
        assertNotNull(validator);

        final UStatus status = validator.validate(uuidv6);
        assertEquals(UCode.INVALID_ARGUMENT, status.getCode());
        assertEquals("Invalid UUIDv8 Version", status.getMessage());

        final UStatus status1 = validator.validate(uuid);
        assertEquals(UCode.INVALID_ARGUMENT, status1.getCode());
        assertEquals("Invalid UUIDv8 Version,Invalid UUID Time", status1.getMessage());

        final UStatus status2 = validator.validate(uuidv4);
        assertEquals(UCode.INVALID_ARGUMENT, status2.getCode());
        assertEquals("Invalid UUIDv8 Version,Invalid UUID Time", status2.getMessage());
    }

    @Test
    @DisplayName("Test good UUIDv6")
    void test_good_uuidv6() {
        final UUID uuid = UuidFactory.Factories.UUIDV6.factory().create();

        UuidValidator validator = UuidValidator.getValidator(uuid);
        assertNotNull(validator);
        assertTrue(UuidUtils.isUuidv6(uuid));
        assertEquals(UCode.OK, validator.validate(uuid).getCode());
    }


    @Test
    @DisplayName("Test UUIDv6 with bad variant")
    void test_uuidv6_with_bad_variant() {
        final UUID uuid = UuidSerializer.deserialize("1ee57e66-d33a-65e0-4a77-3c3f061c1e9e");
        assertFalse(uuid.equals(UUID.getDefaultInstance()));
        final UuidValidator validator = UuidValidator.getValidator(uuid);
        assertNotNull(validator);
        final UStatus status = validator.validate(uuid);
        assertEquals("Invalid UUID Version,Invalid UUID Variant,Invalid UUID Time", status.getMessage());
        assertEquals(UCode.INVALID_ARGUMENT, status.getCode());
    }

    @Test
    @DisplayName("Test UUIDv6 with invalid UUID")
    void test_uuidv6_with_invalid_uuid() {

        final UUID uuid = UUID.newBuilder().setMsb(9 << 12).setLsb(0L).build();
        final UuidValidator validator = UuidValidator.Validators.UUIDV6.validator();
        assertNotNull(validator);
        final UStatus status = validator.validate(uuid);
        assertEquals("Not a UUIDv6 Version,Invalid UUIDv6 variant,Invalid UUID Time", status.getMessage());
        assertEquals(UCode.INVALID_ARGUMENT, status.getCode());
    }


    @Test
    @DisplayName("Test using UUIDv6 Validator to validate null UUID")
    void test_uuidv6_with_null_uuid() {
        final UuidValidator validator = UuidValidator.Validators.UUIDV6.validator();
        assertNotNull(validator);
        final UStatus status = validator.validate(null);
        assertEquals("Not a UUIDv6 Version,Invalid UUIDv6 variant,Invalid UUID Time", status.getMessage());
        assertEquals(UCode.INVALID_ARGUMENT, status.getCode());
    }

    @Test
    @DisplayName("Test using UUIDv6 Validator to validate a different types of UUIDs")
    void test_uuidv6_with_uuidv8() {
        final UUID uuid = UuidFactory.Factories.UPROTOCOL.factory().create();
        final UuidValidator validator = UuidValidator.Validators.UUIDV6.validator();
        assertNotNull(validator);
        final UStatus status = validator.validate(uuid);
        assertEquals("Not a UUIDv6 Version", status.getMessage());
        assertEquals(UCode.INVALID_ARGUMENT, status.getCode());
    }

}
