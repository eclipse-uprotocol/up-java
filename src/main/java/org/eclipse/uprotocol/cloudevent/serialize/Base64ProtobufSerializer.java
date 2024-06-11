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
package org.eclipse.uprotocol.cloudevent.serialize;

import java.util.Base64;

/**
 * Helper for serializing Base64 protobuf data.
 */
public interface Base64ProtobufSerializer {

    /**
     * Deserialize a base64 protobuf payload into a Base64 String.
     * 
     * @param bytes byte[] data
     * @return Returns a String from the base64 protobuf payload.
     */
    static String deserialize(byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * Serialize a String into Base64 format.
     * 
     * @param stringToSerialize String to serialize.
     * @return Returns the Base64 formatted String as a byte[].
     */
    static byte[] serialize(String stringToSerialize) {
        if (stringToSerialize == null) {
            return new byte[0];
        }
        return Base64.getDecoder().decode(stringToSerialize.getBytes());
    }
}
