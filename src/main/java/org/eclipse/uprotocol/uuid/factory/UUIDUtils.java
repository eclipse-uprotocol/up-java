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

import com.github.f4b6a3.uuid.util.UuidTime;
import com.github.f4b6a3.uuid.util.UuidUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

public class UUIDUtils {

    public enum Version {

        /**
         * An unknown version.
         */
        VERSION_UNKNOWN(0),
        /**
         * The time-based version with gregorian epoch specified in RFC-4122.
         */
        VERSION_TIME_BASED(1),
        /**
         * The DCE Security version, with embedded POSIX UIDs.
         */
        VERSION_DCE_SECURITY(2),
        /**
         * The name-based version specified in RFC-4122 that uses MD5 hashing.
         */
        VERSION_NAME_BASED_MD5(3),
        /**
         * The randomly or pseudo-randomly generated version specified in RFC-4122.
         */
        VERSION_RANDOM_BASED(4),
        /**
         * The name-based version specified in RFC-4122 that uses SHA-1 hashing.
         */
        VERSION_NAME_BASED_SHA1(5),
        /**
         * The time-ordered version with gregorian epoch proposed by Peabody and Davis.
         */
        VERSION_TIME_ORDERED(6),
        /**
         * The time-ordered version with Unix epoch proposed by Peabody and Davis.
         */
        VERSION_TIME_ORDERED_EPOCH(7),
        /**
         * The custom or free-form version proposed by Peabody and Davis.
         */
        VERSION_UPROTOCOL(8);

        private final int value;

        Version(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }

        public static Version getVersion(int value) {
            Version result = VERSION_UNKNOWN;
            for (Version version : Version.values()) {
                if (version.getValue() == value) {
                    result = version;
                    break;
                }
            }
            return result;
        }
    }

    private UUIDUtils() {}

    /**
     * Convert the UUID to a String
     * @return String representation of the UUID
     */
    public static String toString(UUID uuid) {
        return uuid.toString();
    }

    /**
     * Convert the UUID to byte array
     * @return The byte array
     */
    public static byte[] toBytes(UUID uuid) {
        byte[] b = new byte[16];
        return ByteBuffer.wrap(b).order(ByteOrder.BIG_ENDIAN).putLong(uuid.getMostSignificantBits())
                .putLong(uuid.getLeastSignificantBits()).array();
    }

    /**
     * Convert the byte array to a UUID
     * @param bytes The UUID in bytes format
     * @return  UUIDv8 object built from the byte array
     */
    public static UUID fromBytes(byte[] bytes) {
        final ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        return new UUID(byteBuffer.getLong(), byteBuffer.getLong());
    }

    /**
     * Create a UUID from the passed string
     * @param string the string representation of the uuid
     * @return The UUIDv8 representation of the string
     */
    public static UUID fromString(String string) {
        return UUID.fromString(string);
    }

    /**
     * Fetch the UUID version
     *
     * @return The UUID version
     */
    public static Version getVersion(UUID uuid) {
        return Version.getVersion(uuid.version());
    }

    /**
     * Verify uuid is either v6 or v8
     * @return true if is UUID version 6 or 8
     */
    public static boolean isUuid(UUID uuid) { return isUProtocol(uuid) || isUuidv6(uuid); }

    /**
     * Verify if version is a formal UUIDv8 uProtocol ID
     * @return true if is a UUIDv8 for uProtocol
     */
    public static boolean isUProtocol(UUID uuid) {
        return getVersion(uuid) == Version.VERSION_UPROTOCOL;
    }

    /**
     * Verify if version is This one
     * @return true if is UUID version 6
     */
    public static boolean isUuidv6(UUID uuid) {
        return getVersion(uuid) == Version.VERSION_TIME_ORDERED;
    }

    /**
     *  Return the number of miliseconds since unix epoch
     * @param uuid passed uuid to fetch the time
     * @return number of miliseconds since unix epoch
     */
    public static long getTime(UUID uuid) {
        final long time;
        switch (getVersion(uuid)) {
            case VERSION_UPROTOCOL:
                time = uuid.getMostSignificantBits() >> 16;
                break;
            case VERSION_TIME_ORDERED:
                time = UuidTime.toUnixTimestamp(UuidUtil.getTimestamp(uuid));
                break;
            default:
                throw new IllegalArgumentException("Unsupported UUID");
        }
        return time;
    }
}
