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

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A Validator for uProtocol UUIDs.
 */
public abstract class UuidValidator {

    public static UuidValidator getValidator(UUID uuid) {
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
        UUIDV6(new UuidValidator.UUIDv6Validator()),
        UPROTOCOL(new UuidValidator.UUIDv7Validator());

        private final UuidValidator uuidValidator;

        public UuidValidator validator() {
            return uuidValidator;
        }

        Validators(UuidValidator uuidValidator) {
            this.uuidValidator = uuidValidator;
        }
    }

    public UStatus validate(UUID uuid) {
        final String errorMessage = Stream.of(
                validateVersion(uuid),
                validateTime(uuid))
                .filter(ValidationResult::isFailure)
            .map(ValidationResult::getMessage)
            .collect(Collectors.joining(","));
        return errorMessage.isBlank() ? ValidationResult.success().toStatus()
                : UStatus.newBuilder().setCode(UCode.INVALID_ARGUMENT).setMessage(errorMessage).build();
    }

    public abstract ValidationResult validateVersion(UUID uuid);

    public ValidationResult validateTime(UUID uuid) {
        final Optional<Long> time = UuidUtils.getTime(uuid);
        return time.isPresent() && (time.get() > 0) ? ValidationResult.success()
                : ValidationResult.failure(String.format("Invalid UUID Time"));
    }

    private static class InvalidValidator extends UuidValidator {

        @Override
        public ValidationResult validateVersion(UUID uuid) {
            return ValidationResult.failure(String.format("Invalid UUID Version"));
        }
    }

    private static class UUIDv6Validator extends UuidValidator {
        @Override
        public ValidationResult validateVersion(UUID uuid) {
            return UuidUtils.isUuidv6(uuid)
                    ? ValidationResult.success()
                    : ValidationResult.failure(String.format("Not a UUIDv6 Version"));
        }
    }

    private static class UUIDv7Validator extends UuidValidator {
        @Override
        public ValidationResult validateVersion(UUID uuid) {
            return UuidUtils.isUProtocol(uuid)
                    ? ValidationResult.success()
                    : ValidationResult.failure(String.format("Invalid UUIDv7 Version"));
        }
    }
}
