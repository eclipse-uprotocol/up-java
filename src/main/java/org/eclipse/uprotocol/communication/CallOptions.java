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
package org.eclipse.uprotocol.communication;

import java.util.Objects;
import java.util.Optional;

import org.eclipse.uprotocol.transport.builder.UMessageBuilder;
import org.eclipse.uprotocol.v1.UPriority;

/**
 * This class is used to pass metadata to method invocation on the client side.
 */
public record CallOptions (Integer timeout, UPriority priority, String token) {
    public static final int TIMEOUT_DEFAULT = 10000; // Default timeout of 10 seconds
 
    // Default instance.
    public static final CallOptions DEFAULT = new CallOptions(TIMEOUT_DEFAULT, UPriority.UPRIORITY_CS4, null);

    /**
     * Check to ensure CallOptions is not null.
     */
    public CallOptions {
        Objects.requireNonNull(timeout);
        Objects.requireNonNull(priority);
    }

    /**
     * Constructor for CallOptions.
     * 
     * @param timeout The timeout for the method invocation.
     * @param priority The priority of the method invocation.
     */
    public CallOptions(Integer timeout, UPriority priority) {
        this(timeout, priority, null);
    }

    /**
     * Constructor for CallOptions.
     * 
     * @param timeout The timeout for the method invocation.
     */
    public CallOptions(Integer timeout) {
        this(timeout, UPriority.UPRIORITY_CS4, null);
    }

    /**
     * Constructor for CallOptions.
     */
    public CallOptions() {
        this(TIMEOUT_DEFAULT, UPriority.UPRIORITY_CS4, null);
    }

    /**
     * Adds these call options to a message.
     *
     * @param builder The message builder.
     */
    public void applyToMessage(UMessageBuilder builder) {
        Optional.ofNullable(this.priority()).ifPresent(builder::withPriority);
        Optional.ofNullable(this.timeout()).ifPresent(builder::withTtl);
        Optional.ofNullable(this.token()).ifPresent(builder::withToken);
    }
}
