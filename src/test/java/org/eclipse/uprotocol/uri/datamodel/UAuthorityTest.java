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

import java.net.Inet6Address;
import java.net.InetAddress;

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
        UAuthority uAuthority = UAuthority.longRemote("VCU", "my_VIN");
        String sRemote = uAuthority.toString();
        String expectedRemote = "UAuthority{device='vcu', domain='my_vin', markedRemote=true, address=null, markedResolved=false}";
        assertEquals(expectedRemote, sRemote);

        final InetAddress address = createAddressForMicroDeviceForTest();
        UAuthority microRemote = UAuthority.microRemote(address);
        String sMicroRemote = microRemote.toString();
        String expectedMicroRemote = "UAuthority{device='null', domain='null', markedRemote=true, address=localhost/127.0.0.1, markedResolved=false}";
        assertEquals(expectedMicroRemote, sMicroRemote);

        UAuthority resolvedRemote = UAuthority.resolvedRemote("VCU", "MY_VIN", address);
        assertEquals("UAuthority{device='vcu', domain='my_vin', markedRemote=true, address=localhost/127.0.0.1, markedResolved=true}", resolvedRemote.toString());

        UAuthority local = UAuthority.local();
        String sLocal = local.toString();
        String expectedLocal = "UAuthority{device='null', domain='null', markedRemote=false, address=null, markedResolved=true}";
        assertEquals(expectedLocal, sLocal);

        UAuthority empty = UAuthority.empty();
        assertEquals("UAuthority{device='null', domain='null', markedRemote=false, address=null, markedResolved=true}", empty.toString());

    }

    @Test
    @DisplayName("Make sure the toString works with case sensitivity")
    public void testToString_case_sensitivity() {
        UAuthority uAuthority = UAuthority.longRemote("vcU", "my_VIN");
        String sRemote = uAuthority.toString();
        String expectedRemote = "UAuthority{device='vcu', domain='my_vin', markedRemote=true, address=null, markedResolved=false}";
        assertEquals(expectedRemote, sRemote);
    }

    @Test
    @DisplayName("Test create a empty uAuthority")
    public void test_create_empty_uAuthority() {
        UAuthority uAuthority = UAuthority.empty();
        assertTrue(uAuthority.device().isEmpty());
        assertTrue(uAuthority.domain().isEmpty());
        assertTrue(uAuthority.address().isEmpty());
        assertTrue(uAuthority.isLocal());
        assertFalse(uAuthority.isRemote());
        assertFalse(uAuthority.isMarkedRemote());
        assertTrue(uAuthority.isResolved());
        assertTrue(uAuthority.isEmpty());
        assertTrue(uAuthority.isMicroForm());
        assertTrue(uAuthority.isLongForm());
    }

    @Test
    @DisplayName("Test create a local uAuthority")
    public void test_create_local_uAuthority() {
        UAuthority uAuthority = UAuthority.local();
        assertTrue(uAuthority.device().isEmpty());
        assertTrue(uAuthority.domain().isEmpty());
        assertTrue(uAuthority.address().isEmpty());
        assertTrue(uAuthority.isLocal());
        assertFalse(uAuthority.isRemote());
        assertFalse(uAuthority.isMarkedRemote());
        assertTrue(uAuthority.isResolved());
        assertTrue(uAuthority.isEmpty());
        assertTrue(uAuthority.isMicroForm());
        assertTrue(uAuthority.isLongForm());
    }

    @Test
    @DisplayName("Test create a remote uAuthority that supports long UUris")
    public void test_create_remote_uAuthority_that_supports_long_uuri() {
        String device = "vcu";
        String domain = "myvin";
        UAuthority uAuthority = UAuthority.longRemote(device, domain);
        assertEquals(device, uAuthority.device().orElse(""));
        assertEquals(domain, uAuthority.domain().orElse(""));
        assertTrue(uAuthority.address().isEmpty());
        assertFalse(uAuthority.isLocal());
        assertTrue(uAuthority.isRemote());
        assertTrue(uAuthority.isMarkedRemote());
        assertFalse(uAuthority.isResolved());
        assertFalse(uAuthority.isEmpty());
        assertFalse(uAuthority.isMicroForm());
        assertTrue(uAuthority.isLongForm());
    }

    @Test
    @DisplayName("Test create a remote uAuthority that supports long UUris null device")
    public void test_create_remote_uAuthority_that_supports_long_uuri_null_device() {
        String domain = "myvin";
        UAuthority uAuthority = UAuthority.longRemote(null, domain);
        assertTrue(uAuthority.device().isEmpty());
        assertEquals(domain, uAuthority.domain().orElse(""));
        assertTrue(uAuthority.address().isEmpty());
        assertFalse(uAuthority.isLocal());
        assertTrue(uAuthority.isRemote());
        assertTrue(uAuthority.isMarkedRemote());
        assertFalse(uAuthority.isResolved());
        assertFalse(uAuthority.isEmpty());
        assertFalse(uAuthority.isMicroForm());
        assertFalse(uAuthority.isLongForm());
    }

    @Test
    @DisplayName("Test create a remote uAuthority that supports long UUris missing device")
    public void test_create_remote_uAuthority_that_supports_long_uuri_missing_device() {
        String device = " ";
        String domain = "myvin";
        UAuthority uAuthority = UAuthority.longRemote(device, domain);
        assertTrue(uAuthority.device().isEmpty());
        assertEquals(domain, uAuthority.domain().orElse(""));
        assertTrue(uAuthority.address().isEmpty());
        assertFalse(uAuthority.isLocal());
        assertTrue(uAuthority.isRemote());
        assertTrue(uAuthority.isMarkedRemote());
        assertFalse(uAuthority.isResolved());
        assertFalse(uAuthority.isEmpty());
        assertFalse(uAuthority.isMicroForm());
        assertFalse(uAuthority.isLongForm());
    }

    @Test
    @DisplayName("Test create a remote uAuthority that supports long UUris null domain")
    public void test_create_remote_uAuthority_that_supports_long_uuri_null_domain() {
        String device = "vcu";
        UAuthority uAuthority = UAuthority.longRemote(device, null);
        assertEquals(device, uAuthority.device().orElse(""));
        assertTrue(uAuthority.domain().isEmpty());
        assertTrue(uAuthority.address().isEmpty());
        assertFalse(uAuthority.isLocal());
        assertTrue(uAuthority.isRemote());
        assertTrue(uAuthority.isMarkedRemote());
        assertFalse(uAuthority.isResolved());
        assertFalse(uAuthority.isEmpty());
        assertFalse(uAuthority.isMicroForm());
        assertTrue(uAuthority.isLongForm());
    }

    @Test
    @DisplayName("Test create a remote uAuthority that supports micro UUris")
    public void test_create_remote_uAuthority_that_supports_micro_uuri() {
        final InetAddress address = createAddressForMicroDeviceForTest();
        UAuthority uAuthority = UAuthority.microRemote(address);
        assertTrue(uAuthority.device().isEmpty());
        assertTrue(uAuthority.domain().isEmpty());
        assertEquals(address, uAuthority.address().orElse(null));
        assertFalse(uAuthority.isLocal());
        assertTrue(uAuthority.isRemote());
        assertTrue(uAuthority.isMarkedRemote());
        assertFalse(uAuthority.isResolved());
        assertFalse(uAuthority.isEmpty());
        assertTrue(uAuthority.isMicroForm());
        assertFalse(uAuthority.isLongForm());
    }

    @Test
    @DisplayName("Test create a remote uAuthority that supports micro UUris with null address")
    public void test_create_remote_uAuthority_that_supports_micro_uuri_with_null_address() {
        UAuthority uAuthority = UAuthority.microRemote(null);
        assertTrue(uAuthority.device().isEmpty());
        assertTrue(uAuthority.domain().isEmpty());
        assertTrue(uAuthority.address().isEmpty());
        assertFalse(uAuthority.isLocal());
        assertTrue(uAuthority.isRemote());
        assertTrue(uAuthority.isMarkedRemote());
        assertFalse(uAuthority.isResolved());
        assertTrue(uAuthority.isEmpty());
        assertFalse(uAuthority.isMicroForm());
        assertFalse(uAuthority.isLongForm());
    }

    @Test
    @DisplayName("Test create a remote resolved uAuthority that supports both long and micro UUris")
    public void test_create_remote_resolved_uAuthority_that_supports_long_and_micro_uuri() {
        String device = "vcu";
        String domain = "myvin";
        final InetAddress address = createAddressForMicroDeviceForTest();
        UAuthority uAuthority = UAuthority.resolvedRemote(device, domain, address);
        assertEquals(device, uAuthority.device().orElse(""));
        assertEquals(domain, uAuthority.domain().orElse(""));
        assertEquals(address, uAuthority.address().orElse(null));
        assertFalse(uAuthority.isLocal());
        assertTrue(uAuthority.isRemote());
        assertTrue(uAuthority.isMarkedRemote());
        assertTrue(uAuthority.isResolved());
        assertFalse(uAuthority.isEmpty());
        assertTrue(uAuthority.isMicroForm());
        assertTrue(uAuthority.isLongForm());
    }

    @Test
    @DisplayName("Test create a remote resolved uAuthority that supports both long and micro UUris with null device")
    public void test_create_remote_resolved_uAuthority_that_supports_long_and_micro_uuri_null_device() {
        String domain = "myvin";
        final InetAddress address = createAddressForMicroDeviceForTest();
        UAuthority uAuthority = UAuthority.resolvedRemote(null, domain, address);
        assertTrue(uAuthority.device().isEmpty());
        assertEquals(domain, uAuthority.domain().orElse(""));
        assertEquals(address, uAuthority.address().orElse(null));
        assertFalse(uAuthority.isLocal());
        assertTrue(uAuthority.isRemote());
        assertTrue(uAuthority.isMarkedRemote());
        assertFalse(uAuthority.isResolved());
        assertFalse(uAuthority.isEmpty());
        assertTrue(uAuthority.isMicroForm());
        assertFalse(uAuthority.isLongForm());
    }

    @Test
    @DisplayName("Test create a remote resolved uAuthority that supports both long and micro UUris with blank device")
    public void test_create_remote_resolved_uAuthority_that_supports_long_and_micro_uuri_blank_device() {
        String device = "  ";
        String domain = "myvin";
        final InetAddress address = createAddressForMicroDeviceForTest();
        UAuthority uAuthority = UAuthority.resolvedRemote(device, domain, address);
        assertTrue(uAuthority.device().isEmpty());
        assertEquals(domain, uAuthority.domain().orElse(""));
        assertEquals(address, uAuthority.address().orElse(null));
        assertFalse(uAuthority.isLocal());
        assertTrue(uAuthority.isRemote());
        assertTrue(uAuthority.isMarkedRemote());
        assertFalse(uAuthority.isResolved());
        assertFalse(uAuthority.isEmpty());
        assertTrue(uAuthority.isMicroForm());
        assertFalse(uAuthority.isLongForm());
    }

    @Test
    @DisplayName("Test create a remote resolved uAuthority that supports both long and micro UUris with missing address")
    public void test_create_remote_resolved_uAuthority_that_supports_long_and_micro_uuri_missing_address() {
        String device = "vcu";
        String domain = "myvin";
        UAuthority uAuthority = UAuthority.resolvedRemote(device, domain, null);
        assertEquals(device, uAuthority.device().orElse(""));
        assertEquals(domain, uAuthority.domain().orElse(""));
        assertTrue(uAuthority.address().isEmpty());
        assertFalse(uAuthority.isLocal());
        assertTrue(uAuthority.isRemote());
        assertTrue(uAuthority.isMarkedRemote());
        assertFalse(uAuthority.isResolved());
        assertFalse(uAuthority.isEmpty());
        assertFalse(uAuthority.isMicroForm());
        assertTrue(uAuthority.isLongForm());
    }

    @Test
    @DisplayName("Test create a remote resolved uAuthority that supports both long and micro UUris with missing data")
    public void test_create_remote_resolved_uAuthority_that_supports_long_and_micro_uuri_missing_all_data() {
        String device = "";
        String domain = "";
        UAuthority uAuthority = UAuthority.resolvedRemote(device, domain, null);
        assertTrue(uAuthority.device().isEmpty());
        assertTrue(uAuthority.domain().isEmpty());
        assertTrue(uAuthority.address().isEmpty());
        assertFalse(uAuthority.isLocal());
        assertTrue(uAuthority.isRemote());
        assertTrue(uAuthority.isMarkedRemote());
        assertFalse(uAuthority.isResolved());
        assertTrue(uAuthority.isEmpty());
        assertFalse(uAuthority.isMicroForm());
        assertFalse(uAuthority.isLongForm());
    }

    private InetAddress createAddressForMicroDeviceForTest() {
        return Inet6Address.getLoopbackAddress();
    }

}