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

package org.eclipse.uprotocol.uuid.validate;

import org.eclipse.uprotocol.cloudevent.validate.ValidationResult;
import org.eclipse.uprotocol.uuid.factory.UUIDUtils;

import com.google.rpc.Code;
import com.google.rpc.Status;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * UUIDv8 Validator class
 * Validates type v6 or v8 UUIDs
 */
public abstract class UuidValidator {

    public static UuidValidator getValidator(UUID uuid){
        switch (UUIDUtils.getVersion(uuid)){
            case VERSION_TIME_ORDERED:
                return UuidValidator.Validators.UUIDV6.validator();
        }
        return UuidValidator.Validators.UPROTOCOL.validator();
    }


    public enum Validators {
        UUIDV6 (new UuidValidator.UUIDv6Validator()),

        UPROTOCOL (new UuidValidator.UUIDv8Validator());

        private final UuidValidator uuidValidator;

        public UuidValidator validator() {
            return uuidValidator;
        }

        Validators(UuidValidator uuidValidator) {
            this.uuidValidator = uuidValidator;
        }
    }

    public Status validate(UUID uuid) {
        final String errorMessage = Stream.of(validateVersion(uuid))
                .filter(ValidationResult::isFailure)
                .map(ValidationResult::getMessage)
                .collect(Collectors.joining(","));
        return errorMessage.isBlank() ? ValidationResult.success().toStatus() :
                Status.newBuilder().setCode(Code.INVALID_ARGUMENT_VALUE).setMessage(errorMessage).build();
    }

    public abstract ValidationResult validateVersion(UUID uuid);

    private static class UUIDv6Validator extends UuidValidator {
        public ValidationResult validateVersion(UUID uuid) {
            final UUIDUtils.Version version = UUIDUtils.getVersion(uuid);
            return version == UUIDUtils.Version.VERSION_TIME_ORDERED ?
                    ValidationResult.success() : ValidationResult.failure(String.format("Invalid Version %d", version.getValue()));
        }
    }

    private static class UUIDv8Validator extends UuidValidator {
        public ValidationResult validateVersion(UUID uuid) {
            final UUIDUtils.Version version = UUIDUtils.getVersion(uuid);
            return version == UUIDUtils.Version.VERSION_UPROTOCOL ?
                    ValidationResult.success() : ValidationResult.failure(String.format("Invalid Version %d", version.getValue()));
        }
    }
}
