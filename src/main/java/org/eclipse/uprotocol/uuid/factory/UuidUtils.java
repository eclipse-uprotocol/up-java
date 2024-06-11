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

import com.github.f4b6a3.uuid.enums.UuidVariant;
import com.github.f4b6a3.uuid.util.UuidTime;
import com.github.f4b6a3.uuid.util.UuidUtil;
import org.eclipse.uprotocol.v1.UUID;

import java.util.Optional;

/**
 * UUID Utils class that provides utility methods for uProtocol IDs
 */
public interface UuidUtils {

    /**
     * Fetch the UUID version.
     *
     * @param uuid The UUID to fetch the version from.
     * @return the UUID version from the UUID object or Optional.empty() if the uuid is null.
     */
    static Optional<Version> getVersion(UUID uuid) {
        // Version is bits masked by 0x000000000000F000 in MS long
        return uuid == null ? Optional.empty() : Version.getVersion((int) ((uuid.getMsb() >> 12) & 0x0f));
    }

    /**
     * Fetch the Variant from the passed UUID.
     *
     * @param uuid The UUID to fetch the variant from.
     * @return UUID variant or Empty if uuid is null.
     */
    static Optional<Integer> getVariant(UUID uuid) {
        return uuid == null ? Optional.empty() : Optional.of(
                (int) ((uuid.getLsb() >>> (64 - (uuid.getLsb() >>> 62))) & (uuid.getLsb() >> 63)));
    }

    /**
     * Verify if version is a formal UUIDv8 uProtocol ID.
     *
     * @return true if is a uProtocol UUID or false if uuid passed is null
     * or the UUID is not uProtocol format.
     */
    static boolean isUProtocol(UUID uuid) {
        final Optional<Version> version = getVersion(uuid);
        return uuid != null && version.isPresent() && version.get() == Version.VERSION_UPROTOCOL;
    }

    /**
     * Verify if version is UUIDv6
     *
     * @return true if is UUID version 6 or false if uuid is null or not version 6
     */
    static boolean isUuidv6(UUID uuid) {
        final Optional<Version> version = getVersion(uuid);
        final Optional<Integer> variant = getVariant(uuid);
        return uuid != null && version.isPresent() && version.get() == Version.VERSION_TIME_ORDERED && variant.get() == UuidVariant.VARIANT_RFC_4122.getValue();
    }

    /**
     * Verify uuid is either v6 or v8
     *
     * @return true if is UUID version 6 or 8
     */
    static boolean isUuid(UUID uuid) {
        return isUProtocol(uuid) || isUuidv6(uuid);
    }

    /**
     * Return the number of milliseconds since unix epoch from a passed UUID.
     *
     * @param uuid passed uuid to fetch the time.
     * @return number of milliseconds since unix epoch or empty if uuid is null.
     */
    static Optional<Long> getTime(UUID uuid) {
        Long time = null;
        Optional<Version> version = getVersion(uuid);
        if (uuid == null || version.isEmpty()) {
            return Optional.empty();
        }

        switch (version.get()) {
            case VERSION_UPROTOCOL:
                time = uuid.getMsb() >> 16;
                break;
            case VERSION_TIME_ORDERED:
                //convert Ticks to Millis
                try {
                    java.util.UUID uuid_java=new java.util.UUID(uuid.getMsb(),uuid.getLsb());
                    time = UuidTime.toUnixTimestamp(UuidUtil.getTimestamp(uuid_java)) / UuidTime.TICKS_PER_MILLI;
                } catch (IllegalArgumentException e) {
                    return Optional.empty();
                }
                break;
            default:
                break;
        }

        return Optional.ofNullable(time);
    }

    /**
     * Calculates the elapsed time since the creation of the specified UUID.
     *
     * @param id The UUID of the object whose creation time needs to be determined.
     * @return An Optional containing the elapsed time in milliseconds,
     * or an empty Optional if the creation time cannot be determined.
     */
    static Optional<Long> getElapsedTime(UUID id) {
        final long creationTime = getTime(id).orElse(-1L);
        if (creationTime < 0) {
            return Optional.empty();
        }
        final long now = System.currentTimeMillis();
        return now >= creationTime ? Optional.of(now - creationTime) : Optional.empty();
    }

    /**
     * Calculates the remaining time until the expiration of the event identified by the given UUID.
     *
     * @param id  The UUID of the object whose remaining time needs to be determined.
     * @param ttl The time-to-live (TTL) in milliseconds.
     * @return An Optional containing the remaining time in milliseconds until the event expires,
     * or an empty Optional if the UUID is null, TTL is non-positive, or the creation time cannot be determined.
     */
    static Optional<Long> getRemainingTime(UUID id, int ttl) {
        if (id == null || ttl <= 0) {
            return Optional.empty();
        }
        return getElapsedTime(id).filter(elapsedTime -> ttl > elapsedTime).map(elapsedTime -> ttl - elapsedTime);
    }


    /**
     * Checks if the event identified by the given UUID has expired based on the specified time-to-live (TTL).
     *
     * @param id  The UUID identifying the event.
     * @param ttl The time-to-live (TTL) in milliseconds for the event.
     * @return true if the event has expired, false otherwise. Returns false if TTL is non-positive or creation time
     * cannot be determined.
     */
    static boolean isExpired(UUID id, int ttl) {
        return ttl > 0 && getRemainingTime(id, ttl).isEmpty();
    }


    /**
     * UUID Version
     */
    enum Version {

        /**
         * An unknown version.
         */
        VERSION_UNKNOWN(0),
        /**
         * The randomly or pseudo-randomly generated version specified in RFC-4122.
         */
        VERSION_RANDOM_BASED(4),
        /**
         * The time-ordered version with gregorian epoch proposed by Peabody and Davis.
         */
        VERSION_TIME_ORDERED(6),
        /**
         * The custom or free-form version proposed by Peabody and Davis.
         */
        VERSION_UPROTOCOL(8);

        private final int value;

        Version(int value) {
            this.value = value;
        }

        /**
         * Get the Version from the passed integer representation of the version.
         *
         * @param value The integer representation of the version.
         * @return The Version object or Optional.empty() if the value is not a valid version.
         */
        public static Optional<Version> getVersion(int value) {
            for (Version version : Version.values()) {
                if (version.getValue() == value) {
                    return Optional.of(version);
                }
            }
            return Optional.empty();
        }

        public int getValue() {
            return this.value;
        }
    }
}