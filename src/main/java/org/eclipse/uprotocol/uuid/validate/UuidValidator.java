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

import com.github.f4b6a3.uuid.enums.UuidVariant;
import com.google.rpc.Code;
import com.google.rpc.Status;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * UUID Validator class that validates UUIDs
 */
public abstract class UuidValidator {

    public static UuidValidator getValidator(UUID uuid){
        if (UUIDUtils.isUuidv6(uuid)) {
            return UuidValidator.Validators.UUIDV6.validator();
        } else if (UUIDUtils.isUProtocol(uuid)) {
            return UuidValidator.Validators.UPROTOCOL.validator();
        } else {
            return UuidValidator.Validators.UNKNOWN.validator();
        }
    }

    public enum Validators {
        UNKNOWN(new UuidValidator.InvalidValidator()),
        UUIDV6 (new UuidValidator.UUIDv6Validator()),

        UPROTOCOL (new UuidValidator.UUIDv8Validator());

        private final UuidValidator uuidValidator;

        public UuidValidator validator() {
            return uuidValidator;
        }

        private Validators(UuidValidator uuidValidator) {
            this.uuidValidator = uuidValidator;
        }
    }

    public Status validate(UUID uuid) {
        final String errorMessage = Stream.of(validateVersion(uuid),
                        validateVariant(uuid),
                        validateTime(uuid))
                .filter(ValidationResult::isFailure)
                .map(ValidationResult::getMessage)
                .collect(Collectors.joining(","));
        return errorMessage.isBlank() ? ValidationResult.success().toStatus() :
                Status.newBuilder().setCode(Code.INVALID_ARGUMENT_VALUE).setMessage(errorMessage).build();
    }

    public abstract ValidationResult validateVersion(UUID uuid);

    public ValidationResult validateTime(UUID uuid) {
            final Optional<Long> time = UUIDUtils.getTime(uuid);
            return time.isPresent() && (time.get() > 0) ?
                    ValidationResult.success() : ValidationResult.failure(String.format("Invalid UUID Time"));
    }

    public abstract ValidationResult validateVariant(UUID uuid);

    private static class InvalidValidator extends UuidValidator {
 
        @Override
        public ValidationResult validateVersion(UUID uuid) {
            return ValidationResult.failure(String.format("Invalid UUID Version"));
        }
  
        @Override
        public ValidationResult validateVariant(UUID uuid) {
            return ValidationResult.failure(String.format("Invalid UUID Variant"));
        }
    }

    private static class UUIDv6Validator extends UuidValidator {
        @Override
        public ValidationResult validateVersion(UUID uuid) {
            final Optional<UUIDUtils.Version> version = UUIDUtils.getVersion(uuid);
            return (version.isPresent() && version.get() == UUIDUtils.Version.VERSION_TIME_ORDERED) ?
                    ValidationResult.success() : ValidationResult.failure(String.format("Not a UUIDv6 Version"));
        }

        @Override
        public ValidationResult validateVariant(UUID uuid) {
            final Optional<Integer> variant = UUIDUtils.getVariant(uuid);
            return (variant.isPresent() && (variant.get() == UuidVariant.VARIANT_RFC_4122.getValue())) ?
                    ValidationResult.success() : ValidationResult.failure(String.format("Invalid UUIDv6 variant"));
        }
    }

    private static class UUIDv8Validator extends UuidValidator {
        @Override
        public ValidationResult validateVersion(UUID uuid) {
            final Optional<UUIDUtils.Version> version = UUIDUtils.getVersion(uuid);
            return version.isPresent() && version.get() == UUIDUtils.Version.VERSION_UPROTOCOL ?
                    ValidationResult.success() : ValidationResult.failure(String.format("Invalid UUIDv8 Version"));
        }
        
        @Override
        public ValidationResult validateVariant(UUID uuid) {
            return ValidationResult.success();
        }
    }
}