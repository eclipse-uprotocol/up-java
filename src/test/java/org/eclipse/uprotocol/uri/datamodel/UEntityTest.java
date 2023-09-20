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

package org.eclipse.uprotocol.uri.datamodel;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


class UEntityTest {

    @Test
    @DisplayName("Make sure the equals and hash code works")
    public void testHashCodeEquals() {
        EqualsVerifier.forClass(UEntity.class).usingGetClass().verify();
    }

    @Test
    @DisplayName("Make sure the toString works")
    public void testToString() {
        UEntity use = UEntity.longFormat("body.access", 1);
        assertEquals("body.access", use.name());
        assertTrue(use.version().isPresent());
        assertEquals(1, use.version().get());

        String expected = "UEntity{name='body.access', version=1, id=null, markedResolved=false}";
        assertEquals(expected, use.toString());

        UEntity use1 = UEntity.longFormat("body.access");
        assertEquals("UEntity{name='body.access', version=null, id=null, markedResolved=false}", use1.toString());
    }

    @Test
    @DisplayName("Test creating a software entity for use in long format UUri with name")
    public void test_create_use_for_use_with_long_format_uuri_with_name() {
        UEntity use = UEntity.longFormat("body.access");
        assertEquals("body.access", use.name());
        assertTrue(use.version().isEmpty());
        assertTrue(use.id().isEmpty());
        assertFalse(use.isEmpty());
        assertFalse(use.isResolved());
        assertTrue(use.isLongForm());
        assertFalse(use.isMicroForm());
    }

    @Test
    @DisplayName("Test creating a software entity for use in long format UUri with name that is blank")
    public void test_create_use_for_use_with_long_format_uuri_with_name_that_is_blank() {
        UEntity use = UEntity.longFormat("  ");
        assertEquals("  ", use.name());
        assertTrue(use.version().isEmpty());
        assertTrue(use.id().isEmpty());
        assertTrue(use.isEmpty());
        assertFalse(use.isResolved());
        assertFalse(use.isLongForm());
        assertFalse(use.isMicroForm());
    }

    @Test
    @DisplayName("Test creating a software entity for use in long format UUri with name that is null, expect exception")
    public void test_create_use_for_use_with_long_format_uuri_with_name_that_is_null() {
        Exception exception = assertThrows(NullPointerException.class, () -> UEntity.longFormat(null));
        assertTrue(exception.getMessage().contains(" Software Entity must have a name"));
    }

    @Test
    @DisplayName("Test creating a software entity for use in long format UUri with name and version")
    public void test_create_use_for_use_with_long_format_uuri_with_name_and_version() {
        UEntity use = UEntity.longFormat("body.access", 1);
        assertEquals("body.access", use.name());
        assertEquals(1, use.version().orElse(-1));
        assertTrue(use.id().isEmpty());
        assertFalse(use.isEmpty());
        assertFalse(use.isResolved());
        assertTrue(use.isLongForm());
        assertFalse(use.isMicroForm());
    }

    @Test
    @DisplayName("Test creating a software entity for use in long format UUri with blank name and null version")
    public void test_create_use_for_use_with_long_format_uuri_with_blank_name_and_no_version() {
        UEntity use = UEntity.longFormat("", null);
        assertEquals("", use.name());
        assertTrue(use.version().isEmpty());
        assertTrue(use.id().isEmpty());
        assertTrue(use.isEmpty());
        assertFalse(use.isResolved());
        assertFalse(use.isLongForm());
        assertFalse(use.isMicroForm());
    }

    @Test
    @DisplayName("Test creating a software entity for use in long format UUri with blank name and null version")
    public void test_create_use_for_use_with_long_format_uuri_with_name_and_no_version() {
        UEntity use = UEntity.longFormat("body.access", null);
        assertEquals("body.access", use.name());
        assertTrue(use.version().isEmpty());
        assertTrue(use.id().isEmpty());
        assertFalse(use.isEmpty());
        assertFalse(use.isResolved());
        assertTrue(use.isLongForm());
        assertFalse(use.isMicroForm());
    }

    @Test
    @DisplayName("Test creating a software entity for use in long format UUri with name and version, null name, expect exception")
    public void test_create_use_for_use_with_long_format_uuri_with_name_and_version_null_name() {
        Exception exception = assertThrows(NullPointerException.class, () -> UEntity.longFormat(null, 1));
        assertTrue(exception.getMessage().contains(" Software Entity must have a name"));
    }

    @Test
    @DisplayName("Test creating an empty USE using the empty static method")
    public void test_create_empty_using_empty() {
        UEntity use = UEntity.empty();
        assertTrue(use.name().isEmpty());
        assertTrue(use.version().isEmpty());
        assertTrue(use.id().isEmpty());
        assertTrue(use.isEmpty());
        assertFalse(use.isResolved());
        assertFalse(use.isLongForm());
        assertFalse(use.isMicroForm());
    }

    @Test
    @DisplayName("Test creating a software entity for use in micro format UUri with id")
    public void test_create_use_for_use_with_micro_format_uuri_with_id() {
        Short id = 42;
        Short defaultNotUsed = 0;
        UEntity use = UEntity.microFormat(id);
        assertTrue(use.name().isBlank());
        assertTrue(use.version().isEmpty());
        assertEquals(id, use.id().orElse(defaultNotUsed));
        assertFalse(use.isEmpty());
        assertFalse(use.isResolved());
        assertFalse(use.isLongForm());
        assertTrue(use.isMicroForm());
    }

    @Test
    @DisplayName("Test creating a software entity for use in micro format UUri with null id")
    public void test_create_use_for_use_with_micro_format_uuri_with_null_id() {
        UEntity use = UEntity.microFormat(null);
        assertTrue(use.name().isBlank());
        assertTrue(use.version().isEmpty());
        assertTrue(use.id().isEmpty());
        assertTrue(use.isEmpty());
        assertFalse(use.isResolved());
        assertFalse(use.isLongForm());
        assertFalse(use.isMicroForm());
    }

    @Test
    @DisplayName("Test creating a software entity for use in micro format UUri with id and version")
    public void test_create_use_for_use_with_micro_format_uuri_with_id_and_version() {
        Short id = 42;
        Short defaultNotUsed = 0;
        UEntity use = UEntity.microFormat(id, 1);
        assertTrue(use.name().isBlank());
        assertEquals(1, use.version().orElse(-1));
        assertEquals(id, use.id().orElse(defaultNotUsed));
        assertFalse(use.isEmpty());
        assertFalse(use.isResolved());
        assertFalse(use.isLongForm());
        assertTrue(use.isMicroForm());
    }

    @Test
    @DisplayName("Test creating a software entity for use in micro format UUri with id and null version")
    public void test_create_use_for_use_with_micro_format_uuri_with_id_and_null_version() {
        Short id = 42;
        Short defaultNotUsed = 0;
        UEntity use = UEntity.microFormat(id, null);
        assertTrue(use.name().isBlank());
        assertTrue(use.version().isEmpty());
        assertEquals(id, use.id().orElse(defaultNotUsed));
        assertFalse(use.isEmpty());
        assertFalse(use.isResolved());
        assertFalse(use.isLongForm());
        assertTrue(use.isMicroForm());
    }

    @Test
    @DisplayName("Test creating a software entity for use in micro format UUri with null id and version")
    public void test_create_use_for_use_with_micro_format_uuri_with_null_id_and_version() {
        UEntity use = UEntity.microFormat(null, 1);
        assertTrue(use.name().isBlank());
        assertEquals(1, use.version().orElse(-1));
        assertTrue(use.id().isEmpty());
        assertFalse(use.isEmpty());
        assertFalse(use.isResolved());
        assertFalse(use.isLongForm());
        assertFalse(use.isMicroForm());
    }

    @Test
    @DisplayName("Test creating a resolved software entity for use in long format and micro format UUri")
    public void test_create_resolved_use_for_use_with_long_format_uuri_and_micro_format_uuri() {
        Short id = 42;
        Short defaultNotUsed = 0;
        UEntity use = UEntity.resolvedFormat("body.access", 1, id);
        assertEquals("body.access", use.name());
        assertEquals(1, use.version().orElse(-1));
        assertEquals(id, use.id().orElse(defaultNotUsed));
        assertFalse(use.isEmpty());
        assertTrue(use.isResolved());
        assertTrue(use.isLongForm());
        assertTrue(use.isMicroForm());
    }

    @Test
    @DisplayName("Test creating a resolved software entity for use in long format and micro format UUri when name is empty")
    public void test_create_resolved_use_for_use_with_long_format_uuri_and_micro_format_uuri_when_name_is_empty() {
        Short id = 42;
        Short defaultNotUsed = 0;
        UEntity use = UEntity.resolvedFormat("", 1, id);
        assertEquals("", use.name());
        assertEquals(1, use.version().orElse(-1));
        assertEquals(id, use.id().orElse(defaultNotUsed));
        assertFalse(use.isEmpty());
        assertFalse(use.isResolved());
        assertFalse(use.isLongForm());
        assertTrue(use.isMicroForm());
    }

    @Test
    @DisplayName("Test creating a resolved software entity for use in long format and micro format UUri with missing version")
    public void test_create_resolved_use_for_use_with_long_format_uuri_and_micro_format_uuri_version_is_missing() {
        Short id = 42;
        Short defaultNotUsed = 0;
        UEntity use = UEntity.resolvedFormat("body.access", null, id);
        assertEquals("body.access", use.name());
        assertTrue(use.version().isEmpty());
        assertEquals(id, use.id().orElse(defaultNotUsed));
        assertFalse(use.isEmpty());
        assertTrue(use.isResolved());
        assertTrue(use.isLongForm());
        assertTrue(use.isMicroForm());
    }

    @Test
    @DisplayName("Test creating a resolved software entity for use in long format and micro format UUri when all elements are empty")
    public void test_create_resolved_use_for_use_with_long_format_uuri_and_micro_format_uuri_all_empty_elements() {
        UEntity use = UEntity.resolvedFormat("", null, null);
        assertEquals("", use.name());
        assertTrue(use.version().isEmpty());
        assertTrue(use.id().isEmpty());
        assertTrue(use.isEmpty());
        assertFalse(use.isResolved());
        assertFalse(use.isLongForm());
        assertFalse(use.isMicroForm());
    }

}