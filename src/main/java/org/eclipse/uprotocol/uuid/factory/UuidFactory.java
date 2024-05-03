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

public abstract class UuidFactory {

    public UUID create() {
        return this.create(Instant.now());
    }

    public abstract UUID create(Instant instant);

    public enum Factories {
        UUIDV6(new UuidFactory.Uuidv6Factory()),

        UPROTOCOL(new UuidFactory.Uuidv8Factory());

        private final UuidFactory factory;

        private Factories(UuidFactory factory) {
            this.factory = factory;
        }

        public UuidFactory factory() {
            return factory;
        }

    }

    private static class Uuidv6Factory extends UuidFactory {
        public UUID create(Instant instant) {
            java.util.UUID uuid_java = UuidCreator.getTimeOrdered(Objects.requireNonNullElse(instant, Instant.now()),
                    null, null);
            return UUID.newBuilder().setMsb(uuid_java.getMostSignificantBits())
                    .setLsb(uuid_java.getLeastSignificantBits()).build();
        }
    }

    /**
     * uProtocol UUIDv8 data model
     * UUIDv8 can only be built using the static factory methods of the class
     * given that the UUIDv8 datamodel is based off the previous UUID generated.
     * The UUID is based off the draft-ietf-uuidrev-rfc4122bis and UUIDv7 with
     * some modifications that are discussed below. The diagram below shows the
     * specification for the UUID:
     * 0                   1                   2                   3
     * 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * |                         unix_ts_ms                            |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * |           unix_ts_ms          |  ver  |         counter       |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * |var|                          rand_b                           |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * |                           rand_b                              |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * <p>
     * | Field      | RFC2119 |
     * | -----      | --------|
     * | unix_ts_ms | 48 bit big-endian unsigned number of Unix epoch timestamp in milliseconds as per Section 6.1
     * of RFC
     * | ver        | MUST be 8 per Section 4.2 of draft-ietf-uuidrev-rfc4122bis
     * | counter    | MUST be a 12 bit counter field that is reset at each unix_ts_ms tick, and incremented for each
     * UUID generated
     * within the 1ms precision of unix_ts_ms The counter provides the ability to generate 4096 events within 1ms
     * however the precision of the clock is still 1ms accuracy
     * | var        | MUST be the The 2 bit variant defined by Section 4.1 of RFC |
     * |rand_b      | MUST 62 bits random number that is generated at initialization time of the uE only and reused
     * otherwise |
     */
    private static class Uuidv8Factory extends UuidFactory {
        public static final int UUIDV8_VERSION = 8;
        private static final int MAX_COUNT = 0xfff;
        private static final long lsb = (new Random().nextLong() & 0x3fffffffffffffffL) | 0x8000000000000000L;
        // Keep track of the time and counters
        private static long msb = UUIDV8_VERSION << 12; // Version is 8

        synchronized public UUID create(Instant instant) {
            final long time = Objects.requireNonNullElse(instant, Instant.now()).toEpochMilli();

            // Check if the current time is the same as the previous time
            if (time == (msb >> 16)) {
                // Increment the counter if we are not at MAX_COUNT
                if ((msb & 0xFFFL) < MAX_COUNT) {
                    msb++;
                }

                // The previous time is not the same tick as the current so we reset msb
            } else {
                msb = (time << 16) | 8L << 12;
            }

            return UUID.newBuilder().setMsb(msb).setLsb(lsb).build();
        }
    }
}