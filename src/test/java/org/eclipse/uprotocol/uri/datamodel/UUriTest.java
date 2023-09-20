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

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;

class UriPartTest {

    @Test
    @DisplayName("Make sure the equals and hash code works")
    public void testHashCodeEquals() {
        EqualsVerifier.forClass(UUri.class).usingGetClass().verify();
    }

    @Test
    @DisplayName("Make sure the toString works")
    public void testToString() {
        UAuthority uAuthorityLocal = UAuthority.local();
        UAuthority uAuthorityRemote = UAuthority.longRemote("VCU", "MY_VIN");
        UEntity use = UEntity.longFormat("body.access", 1);
        UResource uResource = UResource.longFormat("door", "front_left", null);

        UUri uri = new UUri(uAuthorityLocal, use, uResource);

        String expected = "UriPart{uAuthority=UAuthority{device='null', domain='null', address='null', markedRemote=false}, " +
                "uEntity=UEntity{name='body.access', version=1, id=null, markedResolved=false}, " +
                "uResource=UResource{name='door', instance='front_left', message='null', id=null, markedResolved=false}}";
        assertEquals(expected, uri.toString());

        UUri uriRemote = new UUri(uAuthorityRemote, use, uResource);
        String expectedRemote = "UriPart{uAuthority=UAuthority{device='vcu', domain='my_vin', address='null', markedRemote=true}, " +
                "uEntity=UEntity{name='body.access', version=1, id=null, markedResolved=false}, " +
                "uResource=UResource{name='door', instance='front_left', message='null', id=null, markedResolved=false}}";
        assertEquals(expectedRemote, uriRemote.toString());

        UUri uri2 = new UUri(uAuthorityRemote, use, UResource.empty());
        String expectedUri2 = "UriPart{uAuthority=UAuthority{device='vcu', domain='my_vin', address='null', markedRemote=true}, " +
                "uEntity=UEntity{name='body.access', version=1, id=null, markedResolved=false}, " +
                "uResource=UResource{name='', instance='null', message='null', id=null, markedResolved=false}}";
        assertEquals(expectedUri2, uri2.toString());
    }

    @Test
    @DisplayName("Test creating full local uri")
    public void test_create_full_local_uri() {
        UAuthority uAuthority = UAuthority.local();
        UEntity use = UEntity.longFormat("body.access");
        UResource uResource = UResource.longFormat("door", "front_left", null);

        UUri uri = new UUri(uAuthority, use, uResource);

        assertEquals(uAuthority, uri.uAuthority());
        assertEquals(use, uri.uEntity());
        assertEquals(uResource, uri.uResource());
    }

    @Test
    @DisplayName("Test creating full microRemote uri")
    public void test_create_full_remote_uri() {
        UAuthority uAuthority = UAuthority.longRemote("VCU", "MY_VIN");
        UEntity use = UEntity.longFormat("body.access", 1);
        UResource uResource = UResource.longFormat("door", "front_left", "Door");

        UUri uri = new UUri(uAuthority, use, uResource);

        assertEquals(uAuthority, uri.uAuthority());
        assertEquals(use, uri.uEntity());
        assertEquals(uResource, uri.uResource());
    }


    @Test
    @DisplayName("Test creating full uri with resource but no message using the constructor")
    public void test_create_uri_no_message_with_constructor() {
        UAuthority uAuthority = UAuthority.longRemote("VCU", "MY_VIN");
        UEntity use = UEntity.longFormat("body.access", 1);
        UResource uResource = UResource.longFormat("door");

        UUri uri = new UUri(uAuthority, use, "door");

        assertEquals(uAuthority, uri.uAuthority());
        assertEquals(use, uri.uEntity());
        assertEquals(uResource, uri.uResource());
    }

    @Test
    @DisplayName("Test creating a uri with a null  authority, expect creation with an empty  authority")
    public void test_create_uri_null_authority() {
        UEntity use = UEntity.longFormat("body.access", 1);
        UResource uResource = UResource.longFormat("door", "front_left", null);

        UUri uri = new UUri(null, use, uResource);
        assertEquals(UAuthority.empty(), uri.uAuthority());
    }

    @Test
    @DisplayName("Test creating a uri with a null  software entity, expect creation with an empty  software entity")
    public void test_create_uri_null_use() {
        UAuthority uAuthority = UAuthority.longRemote("VCU", "MY_VIN");
        UResource uResource = UResource.longFormat("door", "front_left", null);

        UUri uri = new UUri(uAuthority, null, uResource);
        assertEquals(UEntity.empty(), uri.uEntity());
    }

    @Test
    @DisplayName("Test creating a uri with a null ulitfi resource, expect creation with an empty  resource")
    public void test_create_uri_null_uResource() {
        UAuthority uAuthority = UAuthority.longRemote("VCU", "MY_VIN");
        UEntity use = UEntity.longFormat("body.access", 1);
        UResource uResource = UResource.empty();

        UUri uri = new UUri(uAuthority, use, uResource);
        assertEquals(UResource.empty(), uri.uResource());
    }

    @Test
    @DisplayName("Test creating an empty uri using the empty static method")
    public void test_create_empty_using_empty() {
        UUri uri = UUri.empty();

        assertTrue(uri.uAuthority().isLocal());
        assertTrue(uri.uEntity().isEmpty());
        assertTrue(uri.uResource().isEmpty());
    }

    @Test
    @DisplayName("Test the isEmpty static method")
    public void test_is_empty() {
        UUri uri = UUri.empty();
        assertTrue(uri.isEmpty());

        UAuthority uAuthority = UAuthority.empty();
        UEntity use = UEntity.empty();
        UResource uResource = UResource.empty();

        UUri uri2 = new UUri(uAuthority, use, uResource);
        assertTrue(uri2.isEmpty());
    }

    @Test
    @DisplayName("Test isResolved and isLongForm for valid URIs")
    public void test_isResolved_and_isLongForm() throws UnknownHostException {
        UUri uri = UUri.empty();
        
        assertFalse(uri.isResolved());
        assertFalse(uri.isLongForm());
        assertFalse(uri.isMicroForm());

        UUri uri2 = new UUri(UAuthority.local(), UEntity.longFormat("Hartley"), UResource.forRpcRequest("Raise"));
        assertFalse(uri2.isResolved());
        assertTrue(uri2.isLongForm());
        assertFalse(uri2.isMicroForm());

        UUri uri3 = new UUri(UAuthority.local(), UEntity.longFormat("Hartley"), UResource.resolvedFormat("Raise", "Salary", "Bonus", (short)1));
        assertFalse(uri3.isResolved());
        assertTrue(uri3.isLongForm());
        assertFalse(uri3.isMicroForm());

        UUri uri4 = new UUri(UAuthority.local(), UEntity.resolvedFormat("Hartley", null, (short)2), UResource.resolvedFormat("Raise", "Salary", "Bonus", (short)1));
        assertTrue(uri4.isResolved());
        assertTrue(uri4.isLongForm());
        assertFalse(uri3.isMicroForm());

        UUri uri11 = new UUri(UAuthority.local(), UEntity.resolvedFormat("Hartley", null, (short)2), UResource.forRpcRequest("Raise"));
        assertFalse(uri11.isResolved());
        assertTrue(uri11.isLongForm());
        assertFalse(uri11.isMicroForm());

        UUri uri5 = new UUri(UAuthority.resolvedRemote("vcu", "vin", null), UEntity.longFormat("Hartley"), UResource.forRpcRequest("Raise"));
        assertFalse(uri5.isResolved());
        assertTrue(uri5.isLongForm());
        assertFalse(uri5.isMicroForm());

        UUri uri6 = new UUri(UAuthority.resolvedRemote("vcu", "vin", null), UEntity.longFormat("Hartley"), UResource.resolvedFormat("Raise", "Salary", "Bonus", (short)1));
        assertFalse(uri6.isResolved());
        assertTrue(uri6.isLongForm());
        assertFalse(uri6.isMicroForm());

        UUri uri7 = new UUri(UAuthority.resolvedRemote("vcu", "vin", null), UEntity.longFormat("Hartley"), UResource.resolvedFormat("Raise", "Salary", "Bonus", (short)1));
        assertFalse(uri7.isResolved());
        assertTrue(uri7.isLongForm());
        assertFalse(uri7.isMicroForm());

        UUri uri14 = new UUri(UAuthority.resolvedRemote("vcu", "vin", null), UEntity.resolvedFormat("Hartley", 1, (short)2), UResource.resolvedFormat("Raise", "Salary", "Bonus", (short)1));
        assertFalse(uri14.isResolved());
        assertTrue(uri14.isLongForm());
        assertFalse(uri14.isMicroForm());


        UUri uri8 = new UUri(UAuthority.resolvedRemote("vcu", "vin", InetAddress.getByName("192.168.1.100")), UEntity.longFormat("Hartley"), UResource.forRpcRequest("Raise"));
        assertFalse(uri8.isResolved());
        assertTrue(uri8.isLongForm());
        assertFalse(uri8.isMicroForm());

        UUri uri9 = new UUri(UAuthority.resolvedRemote("vcu", "vin", InetAddress.getByName("192.168.1.100")), UEntity.longFormat("Hartley"), UResource.resolvedFormat("Raise", "Salary", "Bonus", (short)1));
        assertFalse(uri9.isResolved());
        assertTrue(uri9.isLongForm());
        assertFalse(uri9.isMicroForm());

        UUri uri10 = new UUri(UAuthority.resolvedRemote("vcu", "vin", InetAddress.getByName("192.168.1.100")), UEntity.resolvedFormat("Hartley", null, (short)2), UResource.resolvedFormat("Raise", "Salary", "Bonus", (short)1));
        assertTrue(uri10.isResolved());
        assertTrue(uri10.isLongForm());
        assertTrue(uri10.isMicroForm());

        UUri uri12 = new UUri(UAuthority.resolvedRemote("vcu", "vin", InetAddress.getByName("192.168.1.100")), UEntity.resolvedFormat("Hartley", null, (short)2), UResource.microFormat((short)2));
        assertFalse(uri12.isResolved());
        assertFalse(uri12.isLongForm());
        assertTrue(uri12.isMicroForm());

        UUri uri19 = new UUri(UAuthority.microRemote(InetAddress.getByName("192.168.1.100")), UEntity.resolvedFormat("Hartley", null, (short)2), UResource.microFormat((short)2));
        assertFalse(uri19.isResolved());
        assertFalse(uri19.isLongForm());
        assertTrue(uri19.isMicroForm());

        UUri uri16 = new UUri(UAuthority.local(), UEntity.microFormat((short)2, 1), UResource.microFormat((short)2));
        assertFalse(uri16.isResolved());
        assertFalse(uri16.isLongForm());
        assertTrue(uri16.isMicroForm());


        UUri uri17 = new UUri(UAuthority.resolvedRemote("vcu", "vin", InetAddress.getByName("192.168.1.100")), UEntity.microFormat((short)2, 1), UResource.resolvedFormat("Raise", "Salary", "Bonus", (short)1));
        assertFalse(uri17.isResolved());
        assertFalse(uri17.isLongForm());
        assertTrue(uri17.isMicroForm());

        UUri uri18 = new UUri(UAuthority.local(), UEntity.microFormat((short)2, 1), UResource.microFormat((short)2));
        assertFalse(uri18.isResolved());
        assertFalse(uri18.isLongForm());
        assertTrue(uri18.isMicroForm());

    }

}