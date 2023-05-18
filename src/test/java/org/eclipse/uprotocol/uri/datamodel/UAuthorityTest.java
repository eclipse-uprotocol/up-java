/*
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

class UAuthorityTest {

    @Test
    @DisplayName("Make sure the equals and hash code works")
    public void testHashCodeEquals() {
        EqualsVerifier.forClass(UAuthority.class).usingGetClass().verify();
    }

    @Test
    @DisplayName("Make sure the toString works")
    public void testToString() {
        UAuthority uAuthority = UAuthority.remote("VCU", "my_VIN");
        String sRemote = uAuthority.toString();
        String expectedRemote = "UAuthority{device='vcu', domain='my_vin', markedRemote=true}";
        assertEquals(expectedRemote, sRemote);

        UAuthority local = UAuthority.local();
        String sLocal = local.toString();
        String expectedLocal = "UAuthority{device='null', domain='null', markedRemote=false}";
        assertEquals(expectedLocal, sLocal);

    }

    @Test
    @DisplayName("Make sure the toString works with case sensitivity")
    public void testToString_case_sensitivity() {
        UAuthority uAuthority = UAuthority.remote("vcU", "my_VIN");
        String sRemote = uAuthority.toString();
        String expectedRemote = "UAuthority{device='vcu', domain='my_vin', markedRemote=true}";
        assertEquals(expectedRemote, sRemote);

        UAuthority local = UAuthority.local();
        String sLocal = local.toString();
        String expectedLocal = "UAuthority{device='null', domain='null', markedRemote=false}";
        assertEquals(expectedLocal, sLocal);

    }

    @Test
    @DisplayName("Test a local uAuthority")
    public void test_local_uAuthority() {
        UAuthority uAuthority = UAuthority.local();
        assertTrue(uAuthority.device().isEmpty());
        assertTrue(uAuthority.domain().isEmpty());
        assertTrue(uAuthority.isLocal());
        assertFalse(uAuthority.isMarkedRemote());
    }

    @Test
    @DisplayName("Test a local uAuthority when one part is empty")
    public void test_local_uAuthority_one_part_empty() {
        UAuthority uAuthority = UAuthority.remote("", "My_VIN");
        assertFalse(uAuthority.isLocal());
        UAuthority uAuthority2 = UAuthority.remote("VCU", "");
        assertFalse(uAuthority2.isLocal());
    }

    @Test
    @DisplayName("Test a remote uAuthority")
    public void test_remote_uAuthority() {
        UAuthority uAuthority = UAuthority.remote("VCU", "my_VIN");
        assertTrue(uAuthority.device().isPresent());
        assertEquals("vcu", uAuthority.device().get());
        assertTrue(uAuthority.domain().isPresent());
        assertEquals("my_vin", uAuthority.domain().get());
        assertTrue(uAuthority.isRemote());
        assertTrue(uAuthority.isMarkedRemote());
    }

    @Test
    @DisplayName("Test a remote uAuthority with case sensitivity")
    public void test_remote_uAuthority_case_sensitive() {
        UAuthority uAuthority = UAuthority.remote("VCu", "my_VIN");
        assertTrue(uAuthority.device().isPresent());
        assertEquals("vcu", uAuthority.device().get());
        assertTrue(uAuthority.domain().isPresent());
        assertEquals("my_vin", uAuthority.domain().get());
        assertTrue(uAuthority.isRemote());
        assertTrue(uAuthority.isMarkedRemote());
    }

    @Test
    @DisplayName("Test a blank remote uAuthority is actually local")
    public void test_blank_remote_uAuthority_is_local() {
        UAuthority uAuthority = UAuthority.remote(" ", " ");
        assertTrue(uAuthority.device().isEmpty());
        assertTrue(uAuthority.domain().isEmpty());
        assertTrue(uAuthority.isLocal());
        assertFalse(uAuthority.isRemote());
        assertTrue(uAuthority.isMarkedRemote());
    }

    @Test
    @DisplayName("Make sure the empty() works")
    public void testEmpty() {
        UAuthority uAuthority = UAuthority.empty();
        assertTrue(uAuthority.device().isEmpty());
        assertTrue(uAuthority.domain().isEmpty());
    }

    @Test
    @DisplayName("Make sure the isLocal() works")
    public void test_isLocal() {
        UAuthority local = UAuthority.local();
        assertTrue(local.isLocal());
        assertFalse(local.isRemote());
        assertFalse(local.isMarkedRemote());
    }

    @Test
    @DisplayName("Make sure the isRemote() works")
    public void test_isRemote() {
        UAuthority remote = UAuthority.remote("VCU", "my_VIN");
        assertFalse(remote.isLocal());
        assertTrue(remote.isRemote());
        assertTrue(remote.isMarkedRemote());
    }
}