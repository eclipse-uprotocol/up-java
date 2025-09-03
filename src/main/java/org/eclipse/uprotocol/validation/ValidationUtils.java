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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class ValidationUtils {

    private ValidationUtils() {
        // Utility class
    }

    /**
     * Collects all exception thrown by checks performed on a given subject.
     *
     * @param <T> The type of the subject.
     * @param subject The subject to perform checks on.
     * @param checks The validation checks to perform.
     * @return A list of exceptions thrown during validation.
     */
    @SafeVarargs
    public static <T> List<Exception> collectErrors(T subject, Consumer<T>... checks) {
        Objects.requireNonNull(subject);
        Objects.requireNonNull(checks);

        return Arrays.stream(checks)
            .map(check -> {
                try {
                    check.accept(subject);
                    return null; // No exception = valid
                } catch (Exception e) {
                    return e;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
}
