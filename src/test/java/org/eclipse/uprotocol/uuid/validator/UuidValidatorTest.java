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
 */

package org.eclipse.uprotocol.uuid.validator;

import org.eclipse.uprotocol.cloudevent.validate.ValidationResult;
import org.eclipse.uprotocol.uuid.factory.UUIDFactory;
import org.eclipse.uprotocol.uuid.factory.UUIDUtils;
import org.eclipse.uprotocol.uuid.validate.UuidValidator;
import com.google.rpc.Code;
import com.google.rpc.Status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UuidValidatorTest {
    @Test
    @DisplayName("Test UUIDv8 validator with good uuid")
    void test_uuidv8_validator_with_good_uuid() {
        final UUID uuid = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final Status status = UuidValidator.getValidator(uuid).validate(uuid);
        assertEquals(ValidationResult.STATUS_SUCCESS, status);
    }

    @Test
    @DisplayName("Test UUIDv6 validator with good uuid")
    void test_uuidv6_validator_with_good_uuid() {
        final UUID uuid = UUIDFactory.Factories.UUIDV6.factory().create();
        final Status status = UuidValidator.getValidator(uuid).validate(uuid);
        System.out.println("UUIDv6 is" + uuid.toString());
        assertEquals(ValidationResult.STATUS_SUCCESS, status);
    }

    @Test
    @DisplayName("Test Good uuid Check")
    void test_good_uuid_string() {
        final Status status = UuidValidator.Validators.UPROTOCOL.validator().validate(UUIDFactory.Factories.UPROTOCOL.factory().create());
        assertEquals(status, ValidationResult.STATUS_SUCCESS);
    }
    @Test
    @DisplayName("Test null uuid")
    void test_null_uuid() {
        final UUID uuid = new UUID(0,0);
        final String str = UUIDUtils.toString(uuid);
        try {
            final UUID uuid2 = UUIDUtils.fromString(str);
            assertEquals(uuid, uuid2);
        } catch(IllegalArgumentException e) {

        }
    }

    @Test
    @DisplayName("Test using wrong validator for version")
    void test_invalid_validator_uuid() {
        final UUID uuidv8 = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final UUID uuidv6 = UUIDFactory.Factories.UUIDV6.factory().create();
        final Status statusv6 = UuidValidator.Validators.UUIDV6.validator().validate(uuidv8);
        final Status statusv8 = UuidValidator.Validators.UPROTOCOL.validator().validate(uuidv6);

        assertEquals(Code.INVALID_ARGUMENT_VALUE, statusv6.getCode());
        assertEquals("Invalid Version 8", statusv6.getMessage());

        assertEquals(Code.INVALID_ARGUMENT_VALUE, statusv8.getCode());
        assertEquals("Invalid Version 6", statusv8.getMessage());
    }
}
