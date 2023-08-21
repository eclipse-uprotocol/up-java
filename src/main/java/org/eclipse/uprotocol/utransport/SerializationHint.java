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

package org.eclipse.uprotocol.utransport;

public enum SerializationHint {
    UNKNOWN(0, ""),
    PROTOBUF (1, "application/x-protobuf"),   // data is a Base64 encoded protobuf string
    JSON(2, "application/json"),       // data is a UTF-8 string containing a JSON structure
    SOMEIP(3, "application/x-someip"),       // data is a UTF-8 string containing a JSON structure
    RAW(4, "application/octet-stream");   // data is a Base64 encoded protobuf string of an Any object with the payload inside

    private final int hintNumber;
    private final String mimeType;
    
    public int hintNumber() {
        return hintNumber;
    }

    public String mimeType() {
        return mimeType;
    }

    SerializationHint(int hintNumber, String mimeType) {
        this.hintNumber = hintNumber;
        this.mimeType = mimeType;
    }
}

