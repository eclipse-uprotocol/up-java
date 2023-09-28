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

import nl.jqno.equalsverifier.EqualsVerifier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class UPayloadTest {

    @Test
    @DisplayName("Make sure the equals and hash code works")
    public void testHashCodeEquals() {
        EqualsVerifier.forClass(UPayload.class).usingGetClass().verify();
    }

    @Test
    @DisplayName("Make sure the toString works on empty")
    public void testToString_with_empty() {
        UPayload uPayload = UPayload.empty();
        assertEquals("UPayload{data=[], hint=UNKNOWN}", uPayload.toString());
        assertEquals(USerializationHint.UNKNOWN, uPayload.hint());
    }


    @Test
    @DisplayName("Create an empty UPayload")
    public void create_an_empty_upayload() {
        UPayload uPayload = UPayload.empty();
        assertEquals(0, uPayload.data().length);
        assertTrue(uPayload.isEmpty());
    }

    @Test
    @DisplayName("Create a UPayload with null")
    public void create_upayload_with_null() {
        UPayload uPayload = new UPayload(null, null);
        assertEquals(0, uPayload.data().length);
        assertTrue(uPayload.isEmpty());
        assertEquals(USerializationHint.UNKNOWN, uPayload.hint());
    }

    @Test
    @DisplayName("Create a UPayload from string with hint")
    public void create_upayload_from_string_with_hint() {
        String stringData = "hello";
        UPayload uPayload = new UPayload(stringData.getBytes(StandardCharsets.UTF_8), USerializationHint.TEXT);
        assertEquals(stringData.length(), uPayload.data().length);
        assertFalse(uPayload.isEmpty());
        assertEquals(USerializationHint.TEXT, uPayload.hint());
        assertEquals(stringData, new String(uPayload.data()));
    }

    @Test
    @DisplayName("Create a UPayload from some string without hint")
    public void create_upayload_from_string_without_hint() {
        String stringData = "hello";
        UPayload uPayload = new UPayload(stringData.getBytes(StandardCharsets.UTF_8), null);
        assertEquals(stringData.length(), uPayload.data().length);
        assertFalse(uPayload.isEmpty());
        assertEquals(USerializationHint.UNKNOWN, uPayload.hint());
    }

    @Test
    @DisplayName("Create a UPayload without a byte array but with some weird hint")
    public void create_upayload_without_byte_array_but_with_weird_hint() {
        UPayload uPayload = new UPayload(null, USerializationHint.PROTOBUF);
        assertEquals(0, uPayload.data().length);
        assertTrue(uPayload.isEmpty());
        assertEquals(USerializationHint.PROTOBUF, uPayload.hint());
        assertFalse(UPayload.empty().equals(uPayload));
    }
}