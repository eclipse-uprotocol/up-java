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
    @DisplayName("Test creating a complete USE")
    public void test_create_use() {
        UEntity use = UEntity.longFormat("body.access", 1);
        assertEquals("body.access", use.name());
        assertTrue(use.version().isPresent());
        assertEquals(1, use.version().get());
    }

    @Test
    @DisplayName("Test creating a complete USE with a null name, expect exception")
    public void test_create_use_null_name() {
        Exception exception = assertThrows(NullPointerException.class, () -> UEntity.longFormat(null, 1));
        assertTrue(exception.getMessage().contains(" Software Entity must have a name"));
    }

    @Test
    @DisplayName("Test creating a USE with no version")
    public void test_create_use_with_no_version() {
        UEntity use = UEntity.longFormat("body.access", null);
        assertEquals("body.access", use.name());
        assertTrue(use.version().isEmpty());

        UEntity use2 = UEntity.longFormat("body.access", null);
        assertEquals("body.access", use2.name());
        assertTrue(use2.version().isEmpty());
    }

    @Test
    @DisplayName("Test creating a USE using the longFormat static method")
    public void test_create_use_with_no_version_using_longFormat() {
        UEntity use = UEntity.longFormat("body.access");
        assertEquals("body.access", use.name());
        assertTrue(use.version().isEmpty());
    }

    @Test
    @DisplayName("Test creating an empty USE using the empty static method")
    public void test_create_empty_using_empty() {
        UEntity use = UEntity.empty();
        assertTrue(use.name().isEmpty());
        assertTrue(use.version().isEmpty());
    }

    @Test
    @DisplayName("Test the isEmpty static method")
    public void test_is_empty() {
        UEntity use = UEntity.empty();
        assertTrue(use.isEmpty());

        UEntity use2 = UEntity.longFormat("", null);
        assertTrue(use2.isEmpty());

        UEntity use3 = UEntity.longFormat("", 1);
        assertFalse(use3.isEmpty());

        UEntity use4 = UEntity.longFormat("petapp", null);
        assertFalse(use4.isEmpty());
    }

    @Test
    @DisplayName("Test creating UEntity with id")
    public void test_create_use_with_id() {
        UEntity use = UEntity.resolvedFormat("body.access", 1, (short)0);
        assertEquals("body.access", use.name());
        assertTrue(use.version().isPresent());
        assertEquals(1, use.version().get());
        assertTrue(use.id().isPresent());
        assertEquals((int)0, (int)use.id().get());
        assertEquals("UEntity{name='body.access', version=1, id=0, markedResolved=true}", use.toString());
    }

    @Test
    @DisplayName("Test creating UEntity with invalid id")
    public void test_create_use_with_invalid_id() {
        UEntity use = UEntity.resolvedFormat("body.access", 1, null);
        assertEquals("body.access", use.name());
        assertTrue(use.version().isPresent());
        assertEquals(1, use.version().get());
        assertFalse(use.id().isPresent());
        assertEquals("UEntity{name='body.access', version=1, id=null, markedResolved=false}", use.toString());
    }

    @Test
    @DisplayName("Test isResolved and isLongForm() with valid resolved information")
    public void test_isResolved_with_valid_resolved_data() {
        UEntity use = UEntity.resolvedFormat("body.access", 1, (short)0);
        assertTrue(use.isResolved());
        assertTrue(use.isLongForm());
        UEntity use3 = UEntity.resolvedFormat("2", null, (short)1);
        assertTrue(use3.isResolved());
        assertTrue(use3.isLongForm());
    }

    @Test
    @DisplayName("Test isResolved and isLongForm() with invalid resolved data")
    public void test_isResolved_with_invalid_resolved_data() {
        UEntity use = UEntity.resolvedFormat("body.access", 1, null);
        assertFalse(use.isResolved());
        assertTrue(use.isLongForm());
        assertFalse(use.isMicroForm());

        UEntity use2 = UEntity.microFormat((short)1, null);
        assertFalse(use2.isResolved());
        assertFalse(use2.isLongForm());
        assertTrue(use2.isMicroForm());

        UEntity use3 = UEntity.empty();
        assertFalse(use3.isResolved());
        assertFalse(use3.isLongForm());
        assertFalse(use3.isMicroForm());

        UEntity use4 = UEntity.resolvedFormat("body.access", 1, (short)4);
        assertTrue(use4.isResolved());
        assertTrue(use4.isLongForm());
        assertTrue(use4.isMicroForm());
    }

    @Test
    @DisplayName("Test create UEntity calling microFormat with valid id")
    public void test_create_use_with_valid_id() {
        UEntity use = UEntity.microFormat((short)1);
        assertEquals("UEntity{name='', version=null, id=1, markedResolved=false}", use.toString());
        assertTrue(use.id().isPresent());
        assertFalse(use.version().isPresent());
        assertFalse(use.isLongForm());
        assertFalse(use.isResolved());
        assertTrue(use.isMicroForm());
    }


    @Test
    @DisplayName("Test create resolvedFormat with various scenarios passed to the api")
    public void test_create_use_with_valid_id_and_version() {
        UEntity use = UEntity.resolvedFormat("body.access", 1, (short)1);
        assertEquals("UEntity{name='body.access', version=1, id=1, markedResolved=true}", use.toString());
        assertTrue(use.id().isPresent());
        assertEquals(use.version().get(), (short)1);
        assertTrue(use.isLongForm());
        assertTrue(use.isResolved());
        assertTrue(use.isMicroForm());

        UEntity use2 = UEntity.resolvedFormat("body.access", null, (short)1);
        assertEquals("UEntity{name='body.access', version=null, id=1, markedResolved=true}", use2.toString());
        assertTrue(use2.id().isPresent());
        assertFalse(use2.version().isPresent());
        assertTrue(use2.isLongForm());
        assertTrue(use2.isResolved());
        assertTrue(use2.isMicroForm());

        UEntity use3 = UEntity.resolvedFormat("body.access", 1, null);
        assertEquals("UEntity{name='body.access', version=1, id=null, markedResolved=false}", use3.toString());
        assertFalse(use3.id().isPresent());
        assertEquals(use3.version().get(), (short)1);
        assertTrue(use3.isLongForm());
        assertFalse(use3.isResolved());
        assertFalse(use3.isMicroForm());

        UEntity use4 = UEntity.resolvedFormat("body.access", null, null);
        assertEquals("UEntity{name='body.access', version=null, id=null, markedResolved=false}", use4.toString());
        assertFalse(use4.id().isPresent());
        assertFalse(use4.version().isPresent());
        assertTrue(use4.isLongForm());
        assertFalse(use4.isResolved());
        assertFalse(use4.isMicroForm());

        UEntity use5 = UEntity.resolvedFormat("", null, null);
        assertEquals("UEntity{name='', version=null, id=null, markedResolved=false}", use5.toString());
        assertTrue(use5.name().isEmpty());
        assertFalse(use5.id().isPresent());
        assertFalse(use5.version().isPresent());
        assertFalse(use5.isLongForm());
        assertFalse(use5.isResolved());
        assertFalse(use5.isMicroForm());

        try {
            UEntity use6 = UEntity.resolvedFormat(null, null, null);
        } catch (NullPointerException e) {
            assertTrue(e.getMessage().contains(" Software Entity must have a name"));
        }

    }
}