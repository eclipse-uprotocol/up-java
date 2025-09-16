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
package org.eclipse.uprotocol.uuid.serializer;

import java.util.Objects;

import org.eclipse.uprotocol.uuid.factory.UuidUtils;
import org.eclipse.uprotocol.v1.UUID;

/**
 * Helper for de-/serializing UUIDs from/to its hyphenated string form.
 */
// [impl->req~uuid-hex-and-dash~1]
public final class UuidSerializer {

    private UuidSerializer() {
        // utility class
    }

    /**
     * Creates a uProtocol UUID from its hyphenated string format.
     * 
     * @param stringUuid The hyphenated string.
     * @return The UUID.
     * @throws IllegalArgumentException if the string does not represent a valid uProtocol UUID.
     */
    public static UUID deserialize(String stringUuid) {
        Objects.requireNonNull(stringUuid);
        // will throw IllegalArgumentException if the string is not hex-and-dash
        java.util.UUID uuidJava = java.util.UUID.fromString(stringUuid);
        var uuid = UUID.newBuilder()
            .setMsb(uuidJava.getMostSignificantBits())
            .setLsb(uuidJava.getLeastSignificantBits())
            .build();
        if (UuidUtils.isUProtocol(uuid)) {
            return uuid;
        } else {
            throw new IllegalArgumentException("String does not represent a uProtocol UUID");
        }
    }

    /**
     * Serializes uProtocol UUID to its hyphenated string representation.
     *
     * @param uuid The UUID.
     * @return The hyphenated string representation of the UUID.
     * @throws NullPointerException if the UUID is {@code null}.
     */
    public static String serialize(UUID uuid) {
        Objects.requireNonNull(uuid);
        return new java.util.UUID(uuid.getMsb(), uuid.getLsb()).toString();
    }
}
