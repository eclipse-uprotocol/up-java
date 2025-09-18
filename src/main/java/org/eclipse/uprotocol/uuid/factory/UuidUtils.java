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
import java.util.Optional;

/**
 * Utility methods for uProtocol UUIDs.
 */
// [impl->dsn~uuid-spec~1]
// [impl->req~uuid-type~1]
public final class UuidUtils {

    private static final int VARIANT_RFC_9562 = 0b10;
    private static final int VERSION_UPROTOCOL = 7;

    private UuidUtils() {
        // Utility class
    }

    private static byte getVersion(UUID uuid) {
        // Version is bits masked by 0x000000000000F000 in MS long
        return (byte) ((uuid.getMsb() >> 12) & 0x0f);
    }

    /**
     * Checks if a UUID is in the RFC 9562 variant.
     *
     * @param uuid The UUID to check.
     * @return {@code true} if the UUID is in the RFC 9562 variant, {@code false} if uuid is {@code null}.
     */
    private static boolean isRfc9562Variant(UUID uuid) {
        return Optional.ofNullable(uuid)
            .map(id -> (byte) (id.getLsb() >>> 62))
            .map(variant -> variant == VARIANT_RFC_9562)
            .orElse(false);
    }

    /**
     * Checks if a UUID is a uProtocol UUID.
     *
     * @param uuid The UUID to check.
     * @return {@code true} if is a uProtocol UUID or {@code false} if uuid is {@code null}
     *         or is not a v7 UUID.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9562.html">RFC 9562</a>
     * @see <a href="https://github.com/eclipse-uprotocol/up-spec/blob/v1.6.0-alpha.6/basics/uuid.adoc">
     * uProtocol UUID specification</a>
     */
    public static boolean isUProtocol(UUID uuid) {
        return Optional.ofNullable(uuid)
                .filter(UuidUtils::isRfc9562Variant)
                .map(UuidUtils::getVersion)
                .map(version -> version == VERSION_UPROTOCOL)
                .orElse(false);
    }

    /**
     * Gets a UUID's (creation) timestamp.
     *
     * @param uuid The UUID.
     * @return The point in time as the number of milliseconds since the Unix epoch.
     * @throws NullPointerException if the UUID is {@code null}.
     * @throws IllegalArgumentException if the UUID is not a uProtocol UUID.
     */
    public static long getTime(UUID uuid) {
        Objects.requireNonNull(uuid);
        if (!isUProtocol(uuid)) {
            throw new IllegalArgumentException("UUID is not a uProtocol UUID");
        }
        return uuid.getMsb() >> 16;
    }

    /**
     * Gets the amount of time that has elapsed between the creation of a UUID and a given
     * point in time.
     *
     * @param id The UUID.
     * @param now The reference point in time as the number of milliseconds since the Unix epoch,
     * or {@code null} to use the current point in time.
     * @return The amount of time in number of milliseconds. The value will be negative if the
     *         given point in time is before the creation time of the UUID.
     * @throws NullPointerException if the UUID is {@code null}.
     * @throws IllegalArgumentException if the UUID is not a uProtocol UUID.
     */
    public static long getElapsedTime(UUID id, Instant now) {
        Objects.requireNonNull(id);
        if (!isUProtocol(id)) {
            throw new IllegalArgumentException("UUID is not a uProtocol UUID");
        }
        final var creationTime = getTime(id);
        final var referenceTime = now != null ? now.toEpochMilli() : Instant.now().toEpochMilli();
        return referenceTime - creationTime;
    }

    /**
     * Gets the amount of time after which the object identified by a given UUID should
     * be considered expired.
     *
     * @param id  The UUID.
     * @param ttl The object's time-to-live (TTL) in milliseconds.
     * @param now The reference point in time that the calculation should be based on, given
     * as the number of milliseconds since the Unix epoch, or {@code null} to use the current point in time.
     * @return The amount of time in milliseconds. The value will be zero if the object
     *         has already expired.
     * @throws NullPointerException if the UUID is {@code null}.
     * @throws IllegalArgumentException if the UUID is not a uProtocol UUID.
     * @throws IllegalArgumentException if the TTL is non-positive.
     */
    public static long getRemainingTime(UUID id, int ttl, Instant now) {
        Objects.requireNonNull(id);
        if (!isUProtocol(id)) {
            throw new IllegalArgumentException("UUID is not a uProtocol UUID");
        }
        if (ttl <= 0) {
            throw new IllegalArgumentException("TTL must be positive");
        }
        final var creationTime = getTime(id);
        final var referenceTime = now != null ? now.toEpochMilli() : Instant.now().toEpochMilli();
        final var elapsedTime = referenceTime - creationTime;
        if (elapsedTime >= ttl) {
            return 0;
        } else {
            return ttl - elapsedTime;
        }
    }

    /**
     * Checks if an object identified by a given UUID should be considered expired.
     *
     * @param id  The UUID.
     * @param ttl The object's time-to-live (TTL) in milliseconds.
     * @param now The reference point in time that the calculation should be based on, given
     * as the number of milliseconds since the Unix epoch, or {@code null} to use the current point in time.
     * @return {@code true} if the object's TTL has already expired.
     * @throws NullPointerException if the UUID is {@code null}.
     * @throws IllegalArgumentException if the UUID is not a uProtocol UUID.
     * @throws IllegalArgumentException if the TTL is negative.
     */
    public static boolean isExpired(UUID id, int ttl, Instant now) {
        Objects.requireNonNull(id);
        if (!isUProtocol(id)) {
            throw new IllegalArgumentException("UUID is not a uProtocol UUID");
        }
        if (ttl < 0) {
            throw new IllegalArgumentException("TTL must be non-negative");
        }
        return ttl > 0 && getRemainingTime(id, ttl, now) == 0;
    }
}
