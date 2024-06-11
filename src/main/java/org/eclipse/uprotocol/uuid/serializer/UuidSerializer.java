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

import org.eclipse.uprotocol.v1.UUID;

/**
 * UUID Serializer interface used to serialize/deserialize UUIDs to/from either
 * Long (string) or micro (bytes) form
 * 
 * @param <T> The data structure that the UUID will be serialized into. For
 *            example String or byte[].
 */
public interface UuidSerializer {

    /**
     * Deserialize from a specific serialization format to a {@link UUID}.
     * 
     * @param stringUuid The UUID in the transport serialized format.
     * @return Returns the {@link UUID} object.
     */
    static UUID deserialize(String stringUuid) {
        if (stringUuid == null || stringUuid.isBlank()) {
            return UUID.getDefaultInstance();
        }
        try {
            java.util.UUID uuidJava = java.util.UUID.fromString(stringUuid);
            return UUID.newBuilder().setMsb(uuidJava.getMostSignificantBits())
                    .setLsb(uuidJava.getLeastSignificantBits()).build();
        } catch (IllegalArgumentException e) {
            return UUID.getDefaultInstance();
        }
    }

    /**
     * Serialize from a {@link UUID} to a specific serialization format.
     * 
     * @param uuid The {@link UUID} object to serialize to a string.
     * @return Returns the {@link UUID} in the transport serialized format.
     */
    static String serialize(UUID uuid) {
        return uuid == null ? new String() : new java.util.UUID(uuid.getMsb(), uuid.getLsb()).toString();
    }
}
