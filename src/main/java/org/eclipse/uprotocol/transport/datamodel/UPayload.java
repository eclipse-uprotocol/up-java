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

 package org.eclipse.uprotocol.transport.datamodel;

import org.eclipse.uprotocol.v1.SerializationHint;

import java.util.Arrays;
import java.util.Objects;

/**
 * The UPayload contains the clean Payload information along with its raw serialized structure of a byte[].
  */
public class UPayload {

    private static final UPayload EMPTY = new UPayload(new byte[0], SerializationHint.UNKNOWN);

    private final byte[] data;

    private final SerializationHint hint;  // Hint regarding the bytes contained within the UPayload


    /**
     * Create a UPayload.
     * @param data A byte array of the actual data.
     */
    public UPayload(byte[] data, SerializationHint hint) {
        this.data = Objects.requireNonNullElse(data, new byte[0]);
        this.hint = Objects.requireNonNullElse(hint, SerializationHint.UNKNOWN);
    }


    /**
     * The actual serialized or raw data, which can be deserialized or simply used as is.
     * @return Returns the actual serialized or raw data, which can be deserialized or simply used as is.
     */
    public byte[] data() {
        return this.data;
    }

    /**
     * The hint regarding the bytes contained within the UPayload.
     * @return Returns the hint regarding the bytes contained within the UPayload.
     */
    public SerializationHint hint() {
        return this.hint;
    }
    
    /**
     * @return Returns an empty representation of UPayload.
     */
    public static UPayload empty() {
        return EMPTY;
    }


    /**
     * @return Returns true if the data in the UPayload is empty.
     */
    public boolean isEmpty() {
        return this.data.length == 0;
    }

    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UPayload uPayload = (UPayload) o;
        return Arrays.equals(data, uPayload.data) && this.hint == uPayload.hint;
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(data),hint);
    }

    @Override
    public String toString() {
        return "UPayload{" +
                "data=" + Arrays.toString(data()) + 
                ", hint=" + hint +'}';
    }
}
