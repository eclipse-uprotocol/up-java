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
package org.eclipse.uprotocol.uuid.validate;

import org.eclipse.uprotocol.uuid.factory.UuidUtils;
import org.eclipse.uprotocol.v1.UUID;
import org.eclipse.uprotocol.v1.UStatus;
import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.validation.ValidationResult;

import com.github.f4b6a3.uuid.enums.UuidVariant;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * UUID Validator class that validates UUIDs
 */
public abstract class UuidValidator {

    public static UuidValidator getValidator(UUID uuid){
        if (UuidUtils.isUuidv6(uuid)) {
            return UuidValidator.Validators.UUIDV6.validator();
        } else if (UuidUtils.isUProtocol(uuid)) {
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

    public UStatus validate(UUID uuid) {
        final String errorMessage = Stream.of(validateVersion(uuid),
                        validateVariant(uuid),
                        validateTime(uuid))
                .filter(ValidationResult::isFailure)
                .map(ValidationResult::getMessage)
                .collect(Collectors.joining(","));
        return errorMessage.isBlank() ? ValidationResult.success().toStatus() :
                UStatus.newBuilder().setCode(UCode.INVALID_ARGUMENT).setMessage(errorMessage).build();
    }

    public abstract ValidationResult validateVersion(UUID uuid);

    public ValidationResult validateTime(UUID uuid) {
            final Optional<Long> time = UuidUtils.getTime(uuid);
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
            final Optional<UuidUtils.Version> version = UuidUtils.getVersion(uuid);
            return (version.isPresent() && version.get() == UuidUtils.Version.VERSION_TIME_ORDERED) ?
                    ValidationResult.success() : ValidationResult.failure(String.format("Not a UUIDv6 Version"));
        }

        @Override
        public ValidationResult validateVariant(UUID uuid) {
            final Optional<Integer> variant = UuidUtils.getVariant(uuid);
            return (variant.isPresent() && (variant.get() == UuidVariant.VARIANT_RFC_4122.getValue())) ?
                    ValidationResult.success() : ValidationResult.failure(String.format("Invalid UUIDv6 variant"));
        }
    }

    private static class UUIDv8Validator extends UuidValidator {
        @Override
        public ValidationResult validateVersion(UUID uuid) {
            final Optional<UuidUtils.Version> version = UuidUtils.getVersion(uuid);
            return version.isPresent() && version.get() == UuidUtils.Version.VERSION_UPROTOCOL ?
                    ValidationResult.success() : ValidationResult.failure(String.format("Invalid UUIDv8 Version"));
        }
        
        @Override
        public ValidationResult validateVariant(UUID uuid) {
            return ValidationResult.success();
        }
    }
}