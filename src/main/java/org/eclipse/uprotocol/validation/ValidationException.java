/**
 * SPDX-FileCopyrightText: 2025 Contributors to the Eclipse Foundation
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

import java.util.List;
import java.util.stream.Collectors;

/**
 * Indicates an error that has occurred during validation of a uProtocol type.
 */
public class ValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private final List<Exception> causes;

    /**
     * Creates a new exception for an error message.
     *
     * @param message The message.
     */
    public ValidationException(String message) {
        super(message);
        this.causes = List.of();
    }

    /**
     * Creates a new exception for multiple causes.
     *
     * @param causes The causes for the validation failure.
     */
    public ValidationException(List<Exception> causes) {
        super("Multiple validation errors");
        this.causes = List.copyOf(causes);
    }

    @Override
    public String getMessage() {
        if (causes.isEmpty()) {
            return super.getMessage();
        }
        return causes.stream()
            .map(Exception::getMessage)
            .collect(Collectors.joining(","));
    }

    /**
     * Gets the causes of this exception.
     *
     * @return An unmodifiable view of the causes.
     */
    public final List<Exception> getCauses() {
        return causes;
    }
}
