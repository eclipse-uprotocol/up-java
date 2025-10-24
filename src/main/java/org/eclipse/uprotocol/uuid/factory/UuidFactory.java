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
package org.eclipse.uprotocol.uuid.factory;

import org.eclipse.uprotocol.v1.UUID;

import java.time.Instant;
import java.util.Objects;
import java.util.Random;

/**
 * A factory for creating uProtocol UUIDs.
 *
 * @see <a href="https://github.com/eclipse-uprotocol/up-spec/blob/v1.6.0-alpha.7/basics/uuid.adoc">
 * uProtocol UUID Specification</a>
 */
// [impl->dsn~uuid-spec~1]
public final class UuidFactory {

    private UuidFactory() {
        // utility class
    }

    /**
     * Creates a UUID based on the current system time.
     *
     * @return The UUID.
     */
    public static UUID create() {
        return create(Instant.now());
    }

    /**
     * Creates a UUID for a given point in time.
     *
     * @param instant The timestamp to use, or {@code null} for the current time.
     * @return The UUID.
     */
    public static UUID create(Instant instant) {
        final long time = Objects.requireNonNullElse(instant, Instant.now()).toEpochMilli();

        final int randA = new Random().nextInt() & 0x0fff; // keep 4 msb clear for version
        final long randB = new Random().nextLong() & 0x3fffffffffffffffL; // keep 2 msb clear for variant

        return UUID.newBuilder()
            .setMsb((time << 16) | 0b0111L << 12 | randA) // version 7
            .setLsb(randB | 0b10L << 62) // variant RFC 4122
            .build();
    }
}
