/*
 * Copyright (c) 2023 General Motors GTO LLC
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.eclipse.uprotocol.uuid.factory;


import com.github.f4b6a3.uuid.UuidCreator;

import java.time.Instant;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public abstract class UUIDFactory {

    public enum Factories {
        UUIDV6 (new UUIDFactory.UUIDv6Factory()),

        UPROTOCOL (new UUIDFactory.UUIDv8Factory());

        private final UUIDFactory factory;
        public UUIDFactory factory() {
            return factory;
        }

        private Factories(UUIDFactory factory) {
            this.factory = factory;
        }
        
    }

    public UUID create() {
        return this.create(Instant.now());
    }

    public abstract UUID create(Instant instant);


    private static class UUIDv6Factory extends UUIDFactory {
        public UUID create(Instant instant) {
            return UuidCreator.getTimeOrdered(Objects.requireNonNullElse(instant, Instant.now()), null, null);
        }
    }

    /**
     *  uProtocol UUIDv8 data model
     *  UUIDv8 can only be built using the static factory methods of the class
     *  given that the UUIDv8 datamodel is based off the previous UUID generated.
     *  The UUID is based off the draft-ietf-uuidrev-rfc4122bis and UUIDv7 with
     *  some modifications that are discussed below. The diagram below shows the
     *  specification for the UUID:
     *      0                   1                   2                   3
     *      0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     *     |                         unix_ts_ms                            |
     *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     *     |           unix_ts_ms          |  ver  |         counter       |
     *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     *     |var|                          rand_b                           |
     *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     *     |                           rand_b                              |
     *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     *
     * | Field      | RFC2119 |
     * | -----      | --------|
     * | unix_ts_ms | 48 bit big-endian unsigned number of Unix epoch timestamp in milliseconds as per Section 6.1  of RFC
     * | ver        | MUST be 8 per Section 4.2 of draft-ietf-uuidrev-rfc4122bis
     * | counter    | MUST be a 12 bit counter field that is reset at each unix_ts_ms tick, and incremented for each UUID generated
     *                within the 1ms precision of unix_ts_ms The counter provides the ability to generate 4096 events within 1ms
     *                however the precision of the clock is still 1ms accuracy
     * | var        | MUST be the The 2 bit variant defined by Section 4.1 of RFC |
     * |rand_b      | MUST 62 bits random number that is generated at initialization time of the uE only and reused otherwise |
     *
     */
    private static class UUIDv8Factory extends UUIDFactory {
        private static final int MAX_COUNT = 0xfff;

        public static final int UUIDV8_VERSION = 8;

        // Keep track of the time and counters
        private static long msb = UUIDV8_VERSION << 12; // Version is 8

        private static final long lsb = (new Random().nextLong() & 0x3fffffffffffffffL) | 0x8000000000000000L;

        synchronized public UUID create(Instant instant) {
            final long time = Objects.requireNonNullElse(instant, Instant.now()).toEpochMilli();
            
            // Check if the current time is the same as the previous time
            if (time == (msb >>16)) {
                // Increment the counter if we are not at MAX_COUNT
                if ((msb & 0xFFFL) < MAX_COUNT) {
                    msb++;
                }

                // The previous time is not the same tick as the current so we reset msb
            } else {
                msb = (time << 16) | 8L << 12;
            }

            return new UUID(msb, lsb);
        }
    }
}
