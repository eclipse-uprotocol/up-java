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

import com.github.f4b6a3.uuid.UuidCreator;
import org.eclipse.uprotocol.v1.UUID;

import java.time.Instant;
import java.util.Objects;
import java.util.Random;

/**
 * A factory for creating UUIDs based on <a href="https://www.rfc-editor.org/rfc/rfc9562">RFC 9562</a>.
 * <p>
 * The factory provides two implementations, UUIDv6 (used for older versions of the protocol),
 * and UUIDv7.
 */
public abstract class UuidFactory {

    /**
     * Create a UUID based on the current time.
     *
     * @return a UUID
     */
    public UUID create() {
        return this.create(Instant.now());
    }

    /**
     * Create a UUID based on the given time.
     *
     * @param instant the time
     * @return a UUID
     */
    public abstract UUID create(Instant instant);

    /**
     * The Factories enum provides a list of factories that can be used to create
     * UUIDs.
     */
    public enum Factories {
        UUIDV6(new UuidFactory.Uuidv6Factory()),

        UPROTOCOL(new UuidFactory.Uuidv7Factory());

        private final UuidFactory factory;

        Factories(UuidFactory factory) {
            this.factory = factory;
        }

        public UuidFactory factory() {
            return factory;
        }

    }

    /**
     * The Uuidv6Factory class is an implementation of the UuidFactory class that
     * creates UUIDs based on the UUIDv6 version of the protocol.
     */
    private static class Uuidv6Factory extends UuidFactory {
        public UUID create(Instant instant) {
            java.util.UUID uuidJava = UuidCreator.getTimeOrdered(Objects.requireNonNullElse(instant, Instant.now()),
                    null, null);
            return UUID.newBuilder().setMsb(uuidJava.getMostSignificantBits())
                    .setLsb(uuidJava.getLeastSignificantBits()).build();
        }
    }

    /**
     * The Uuidv7Factory class is an implementation of the UuidFactory class that
     * creates UUIDs based on the UUIDv7 version of the protocol.
     */
    private static class Uuidv7Factory extends UuidFactory {
        public UUID create(Instant instant) {
            final long time = Objects.requireNonNullElse(instant, Instant.now()).toEpochMilli();

            final int rand_a = new Random().nextInt() & 0xfff;
            final long rand_b = new Random().nextLong() & 0x3fffffffffffffffL;

            return UUID.newBuilder()
                .setMsb((time << 16) | 7L << 12 | rand_a)
                .setLsb(rand_b | 1L << 63).build();
        }
    }
}