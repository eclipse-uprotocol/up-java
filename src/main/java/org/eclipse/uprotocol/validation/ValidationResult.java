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
package org.eclipse.uprotocol.validation;

import org.eclipse.uprotocol.v1.UStatus;
import org.eclipse.uprotocol.v1.UCode;

import java.util.Objects;

/**
 * Class wrapping a ValidationResult of success or failure wrapping the value of a google.rpc.Status.
 */
public abstract class ValidationResult {

    public static final UStatus STATUS_SUCCESS = UStatus.newBuilder().setCode(UCode.OK).setMessage("OK").build();

    private static final ValidationResult SUCCESS = new Success();

    private ValidationResult(){}

    public abstract UStatus toStatus();

    public abstract boolean isSuccess();

    public boolean isFailure() {
        return !isSuccess();
    }

    public abstract String getMessage();

    /**
     * Implementation for failure, wrapping the message.
     */
    private static class Failure extends ValidationResult {
        private final String message;

        private Failure(String message) {
            this.message = Objects.requireNonNullElse(message, "Validation Failed.");
        }

        @Override
        public UStatus toStatus() {
            return UStatus.newBuilder().setCode(UCode.INVALID_ARGUMENT).setMessage(message).build();
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return "ValidationResult.Failure(" + "message='" + message + '\'' + ')';
        }
    }

    /**
     * Implementation for success, wrapping a UStatus with Code 0 for success.
     */
    private static class Success extends ValidationResult {

        @Override
        public UStatus toStatus() {
            return STATUS_SUCCESS;
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public String getMessage() {
            return "";
        }

        @Override
        public String toString() {
            return "ValidationResult.Success()";
        }
    }

    public static ValidationResult success() {
        return SUCCESS;
    }

    public static ValidationResult failure(String message) {
        return new Failure(message);
    }

}
