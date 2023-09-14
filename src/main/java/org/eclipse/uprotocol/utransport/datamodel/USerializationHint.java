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

package org.eclipse.uprotocol.utransport.datamodel;

import java.util.Arrays;
import java.util.Optional;

public enum USerializationHint {
    // Serialization hint is unknown
    UNKNOWN(0, ""),

    // serialized com.google.protobuf.Any type
    PROTOBUF (1, "application/x-protobuf"),
    
    // data is a UTF-8 string containing a JSON structure
    JSON(2, "application/json"),

    // data is a UTF-8 string containing a JSON structure
    SOMEIP(3, "application/x-someip"),

    // Raw binary data that has not been serialized
    RAW(4, "application/octet-stream");

    private final int hintNumber;
    private final String mimeType;
    
    public int hintNumber() {
        return hintNumber;
    }

    public String mimeType() {
        return mimeType;
    }

    USerializationHint(int hintNumber, String mimeType) {
        this.hintNumber = hintNumber;
        this.mimeType = mimeType;
    }

    /**
     * Find the serialization hint matching the mimeType value. Mind you, it might not exist.
     * @param value numeric hint value.
     * @return Returns the USerializationHint matching the numeric value.
     */
    public static Optional<USerializationHint> from(int value) {
        return Arrays.stream(USerializationHint.values())
                .filter(p -> p.hintNumber() == value)
                .findAny();
    }

    /**
     * Find the serialization hint  matching the String value. Mind you, it might not exist.
     * @param value String hint value.
     * @return Returns the USerializationHint matching the String value.
     */
    public static Optional<USerializationHint> from(String value) {
        return Arrays.stream(USerializationHint.values())
                .filter(p -> p.mimeType().equals(value))
                .findAny();
    }
}

