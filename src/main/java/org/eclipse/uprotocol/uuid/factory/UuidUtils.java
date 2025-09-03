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

import com.github.f4b6a3.uuid.util.UuidTime;
import com.github.f4b6a3.uuid.util.UuidUtil;
import org.eclipse.uprotocol.v1.UUID;

import java.util.Optional;

/**
 * Utility methods for uProtocol UUIDs.
 */
public final class UuidUtils {

    private static final int VARIANT_RFC_9562 = 0b10;
    private static final int VERSION_UUIDV6 = 6;
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
    public static boolean isRfc9562Variant(UUID uuid) {
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
     * @see <a href="https://github.com/eclipse-uprotocol/up-spec/blob/v1.6.0-alpha.4/basics/uuid.adoc">
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
     * Checks if a UUID is a v6 UUID.
     *
     * @param uuid The UUID to check.
     * @return {@code true} if is a v6 UUID or {@code false} if uuid is {@code null}
     *         or is not a v6 UUID.
     * @see <a href="https://www.rfc-editor.org/rfc/rfc9562.html">RFC 9562</a>
     */
    public static boolean isUuidv6(UUID uuid) {
        return Optional.ofNullable(uuid)
                .filter(UuidUtils::isRfc9562Variant)
                .map(UuidUtils::getVersion)
                .map(version -> version == VERSION_UUIDV6)
                .orElse(false);
    }

    /**
     * Verify uuid is either v6 or v7.
     *
     * @param uuid The UUID to check.
     * @return true if is UUID version 6 or 7
     */
    public static boolean isUuid(UUID uuid) {
        return isUProtocol(uuid) || isUuidv6(uuid);
    }

    /**
     * Gets the number of milliseconds since unix epoch contained in a UUID.
     *
     * @param uuid The UUID.
     * @return The number of milliseconds, or empty if uuid is null or does not contain
     * a timestamp.
     */
    public static Optional<Long> getTime(UUID uuid) {
        if (uuid == null) {
            return Optional.empty();
        }

        final var version = getVersion(uuid);

        switch (version) {
            case VERSION_UPROTOCOL:
                return Optional.of(uuid.getMsb() >> 16);
            case VERSION_UUIDV6:
                // convert Ticks to Millis
                try {
                    java.util.UUID uuidJava = new java.util.UUID(uuid.getMsb(), uuid.getLsb());
                    return Optional.of(UuidTime.toUnixTimestamp(
                        UuidUtil.getTimestamp(uuidJava)) / UuidTime.TICKS_PER_MILLI);
                } catch (IllegalArgumentException e) {
                    return Optional.empty();
                }
            default:
                return Optional.empty();
        }
    }

    /**
     * Calculates the elapsed time since the creation of the specified UUID.
     *
     * @param id The UUID of the object whose creation time needs to be determined.
     * @return An Optional containing the elapsed time in milliseconds,
     *         or an empty Optional if the creation time cannot be determined.
     */
    public static Optional<Long> getElapsedTime(UUID id) {
        final long creationTime = getTime(id).orElse(-1L);
        if (creationTime < 0) {
            return Optional.empty();
        }
        final long now = System.currentTimeMillis();
        return now >= creationTime ? Optional.of(now - creationTime) : Optional.empty();
    }

    /**
     * Calculates the remaining time until the expiration of the event identified by
     * the given UUID.
     *
     * @param id  The UUID of the object whose remaining time needs to be
     *            determined.
     * @param ttl The time-to-live (TTL) in milliseconds.
     * @return An Optional containing the remaining time in milliseconds until the
     *         event expires,
     *         or an empty Optional if the UUID is null, TTL is non-positive, or the
     *         creation time cannot be determined.
     */
    public static Optional<Long> getRemainingTime(UUID id, int ttl) {
        if (id == null || ttl <= 0) {
            return Optional.empty();
        }
        return getElapsedTime(id).filter(elapsedTime -> ttl > elapsedTime).map(elapsedTime -> ttl - elapsedTime);
    }

    /**
     * Checks if the event identified by the given UUID has expired based on the
     * specified time-to-live (TTL).
     *
     * @param id  The UUID identifying the event.
     * @param ttl The time-to-live (TTL) in milliseconds for the event.
     * @return true if the event has expired, false otherwise. Returns false if TTL
     *         is non-positive or creation time
     *         cannot be determined.
     */
    public static boolean isExpired(UUID id, int ttl) {
        return ttl > 0 && getRemainingTime(id, ttl).isEmpty();
    }
}
