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

import com.github.f4b6a3.uuid.enums.UuidVariant;
import com.github.f4b6a3.uuid.util.UuidTime;
import com.github.f4b6a3.uuid.util.UuidUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Optional;
import java.util.UUID;

/**
 * UUID Utils class that provides utility methods for uProtocol IDs
 */
 public interface UUIDUtils {

    /**
     * UUID Version
     */
    public enum Version {

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

        private Version(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }

        /**
         * Get the Version from the passed integer representation of the version.
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
    }


    /**
     * Convert the UUID to a String.
     * @return String representation of the UUID or Optional.empty() if the UUID is null.
     */
    public static Optional<String> toString(UUID uuid) {
        return uuid == null ? Optional.empty() : Optional.of(uuid.toString());
    }

    /**
     * Convert the UUID to byte array.
     * @return The byte array or Optional.empty() if the UUID is null.
     */
    public static Optional<byte[]> toBytes(UUID uuid) {
        if (uuid == null) {
            return Optional.empty();
        }
        byte[] b = new byte[16];
        return Optional.of(ByteBuffer.wrap(b).order(ByteOrder.BIG_ENDIAN).putLong(uuid.getMostSignificantBits())
                .putLong(uuid.getLeastSignificantBits()).array());
    }

    /**
     * Convert the byte array to a UUID.
     * @param bytes The UUID in bytes format.
     * @return UUIDv8 object built from the byte array or Optional.empty() 
     *         if the byte array is null or not 16 bytes long.
     */
    public static Optional<UUID> fromBytes(byte[] bytes) {
        if (bytes == null || bytes.length != 16) {
            return Optional.empty();
        }

        final ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        return Optional.of(new UUID(byteBuffer.getLong(), byteBuffer.getLong()));
    }


    /**
     * Create a UUID from the passed string.
     * @param string the string representation of the uuid.
     * @return The UUID object representation of the string or Optional.empty()
     *         if the string is null, empty, or invalid.
     */
    public static Optional<UUID> fromString(String string) {
        UUID uuid = null;
        if (string == null || string.isBlank()) {
            return Optional.empty();
        }
        try {
            uuid = UUID.fromString(string);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
        return Optional.ofNullable(uuid);
    }

    /**
     * Fetch the UUID version.
     * @param uuid The UUID to fetch the version from.
     * @return the UUID version from the UUID object or Optional.empty() if the uuid is null.
     */
    public static Optional<Version> getVersion(UUID uuid) {
        return uuid == null ? Optional.empty() : Version.getVersion(uuid.version());
    }

    /**
     * Fetch the Variant from the passed UUID.
     * @param uuid The UUID to fetch the variant from.
     * @return UUID variant or Empty if uuid is null.
     */
    public static Optional<Integer> getVariant(UUID uuid) {
        return uuid == null ? Optional.empty() : Optional.of(uuid.variant());
    }
    
    /**
     * Verify if version is a formal UUIDv8 uProtocol ID.
     * @return true if is a uProtocol UUID or false if uuid passed is null
     *         or the UUID is not uProtocol format.
     */
    public static boolean isUProtocol(UUID uuid) {
        final Optional<Version> version = getVersion(uuid);
        return uuid == null || version.isEmpty() ? false : version.get() == Version.VERSION_UPROTOCOL;
    }

    /**
     * Verify if version is UUIDv6
     * @return true if is UUID version 6 or false if uuid is null or not version 6
     */
    public static boolean isUuidv6(UUID uuid) {
        final Optional<Version> version = getVersion(uuid);
        final Optional<Integer> variant = getVariant(uuid);
        return uuid != null && !version.isEmpty() ? version.get() == Version.VERSION_TIME_ORDERED && 
            variant.get() == UuidVariant.VARIANT_RFC_4122.getValue() : false;
    }

    /**
     * Verify uuid is either v6 or v8
     * @return true if is UUID version 6 or 8
     */
    public static boolean isUuid(UUID uuid) { return isUProtocol(uuid) || isUuidv6(uuid); }

    /**
     * Return the number of milliseconds since unix epoch from a passed UUID.
     * @param uuid passed uuid to fetch the time.
     * @return number of milliseconds since unix epoch or empty if uuid is null.
     */
    public static Optional<Long> getTime(UUID uuid) {
        Long time = null;
        Optional<Version> version = getVersion(uuid);
        if (uuid == null || version.isEmpty()) {
            return Optional.empty();
        }

        switch (version.get()) {
            case VERSION_UPROTOCOL:
                time = uuid.getMostSignificantBits() >> 16;
                break;
            case VERSION_TIME_ORDERED:
                //convert Ticks to Millis
                try {
                    time =  UuidTime.toUnixTimestamp(UuidUtil.getTimestamp(uuid)) / UuidTime.TICKS_PER_MILLI;
                } catch (IllegalArgumentException e) {
                    return Optional.empty();
                }
                break;
            default:
                break;
        }

        return Optional.ofNullable(time);
    }
}

