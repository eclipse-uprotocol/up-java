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
 * SPDX-FileType: SOURCE
 * SPDX-FileCopyrightText: 2023 General Motors GTO LLC
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
    public static Optional<Version> getVersion(UUID uuid) {
        // Version is bits masked by 0x000000000000F000 in MS long
        return uuid == null ? Optional.empty() : Version.getVersion((int) ((uuid.getMsb() >> 12) & 0x0f));
    }

    /**
     * Fetch the Variant from the passed UUID.
     *
     * @param uuid The UUID to fetch the variant from.
     * @return UUID variant or Empty if uuid is null.
     */
    public static Optional<Integer> getVariant(UUID uuid) {
        return uuid == null ? Optional.empty() : Optional.of(
                (int) ((uuid.getLsb() >>> (64 - (uuid.getLsb() >>> 62))) & (uuid.getLsb() >> 63)));
    }

    /**
     * Verify if version is a formal UUIDv8 uProtocol ID.
     *
     * @return true if is a uProtocol UUID or false if uuid passed is null
     * or the UUID is not uProtocol format.
     */
    public static boolean isUProtocol(UUID uuid) {
        final Optional<Version> version = getVersion(uuid);
        return uuid != null && version.isPresent() && version.get() == Version.VERSION_UPROTOCOL;
    }

    /**
     * Verify if version is UUIDv6
     *
     * @return true if is UUID version 6 or false if uuid is null or not version 6
     */
    public static boolean isUuidv6(UUID uuid) {
        final Optional<Version> version = getVersion(uuid);
        final Optional<Integer> variant = getVariant(uuid);
        return uuid != null && version.isPresent() && version.get() == Version.VERSION_TIME_ORDERED && variant.get() == UuidVariant.VARIANT_RFC_4122.getValue();
    }

    /**
     * Verify uuid is either v6 or v8
     *
     * @return true if is UUID version 6 or 8
     */
    public static boolean isUuid(UUID uuid) {
        return isUProtocol(uuid) || isUuidv6(uuid);
    }

    /**
     * Return the number of milliseconds since unix epoch from a passed UUID.
     *
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