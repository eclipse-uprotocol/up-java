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
import org.eclipse.uprotocol.validation.ValidationException;
import org.eclipse.uprotocol.validation.ValidationUtils;

import java.util.Objects;
import java.util.Optional;

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

    /**
     * Checks if a UUID is valid according to the specific variant/version of the UUID.
     *
     * @param uuid The UUID to validate.
     * @throws NullPointerException if the UUID is null.
     * @throws ValidationException if the UUID is invalid.
     */
    public void validate(UUID uuid) {
        Objects.requireNonNull(uuid);
        final var errors = ValidationUtils.collectErrors(uuid,
            this::validateVersion,
            this::validateTime
        );
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    /**
     * Validates the version of the UUID.
     *
     * @param uuid The UUID to check.
     * @throws ValidationException if the UUID is invalid.
     */
    public abstract void validateVersion(UUID uuid);

    public void validateTime(UUID uuid) {
        final Optional<Long> time = UuidUtils.getTime(uuid);
        if (time.isPresent() && (time.get() > 0)) {
            return;
        }
        throw new ValidationException("Invalid UUID Time");
    }

    private static class InvalidValidator extends UuidValidator {

        @Override
        public void validateVersion(UUID uuid) {
            throw new ValidationException("Invalid UUID Version");
        }
    }

    private static class UUIDv6Validator extends UuidValidator {
        @Override
        public void validateVersion(UUID uuid) {
            if (!UuidUtils.isUuidv6(uuid)) {
                throw new ValidationException("Not a UUIDv6 Version");
            }
        }
    }

    private static class UUIDv7Validator extends UuidValidator {
        @Override
        public void validateVersion(UUID uuid) {
            if (!UuidUtils.isUProtocol(uuid)) {
                throw new ValidationException("Invalid UUIDv7 Version");
            }
        }
    }
}
