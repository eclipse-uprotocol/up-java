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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UriTest {

    @Test
    @DisplayName("Make sure the equals and hash code works")
    public void testHashCodeEquals() {
        EqualsVerifier.forClass(UUri.class).usingGetClass().verify();
    }

    @Test
    @DisplayName("Make sure the toString works")
    public void testToString() {
        UAuthority uAuthorityLocal = UAuthority.local();
        UAuthority uAuthorityRemote = UAuthority.remote("VCU", "MY_VIN");
        UEntity use = new UEntity("body.access", "1");
        UResource uResource = UResource.fromNameWithInstance("door", "front_left");

        UUri uri = new UUri(uAuthorityLocal, use, uResource);

        String expected = "Uri{uAuthority=UAuthority{device='null', domain='null', markedRemote=false}, " +
                "uEntity=UEntity{name='body.access', version='1'}, " +
                "uResource=UResource{name='door', instance='front_left', message='null'}}";
        assertEquals(expected, uri.toString());

        UUri uriRemote = new UUri(uAuthorityRemote, use, uResource);
        String expectedRemote = "Uri{uAuthority=UAuthority{device='vcu', domain='my_vin', markedRemote=true}, " +
                "uEntity=UEntity{name='body.access', version='1'}, " +
                "uResource=UResource{name='door', instance='front_left', message='null'}}";
        assertEquals(expectedRemote, uriRemote.toString());

        UUri uri2 = new UUri(uAuthorityRemote, use, UResource.empty());
        String expectedUri2 = "Uri{uAuthority=UAuthority{device='vcu', domain='my_vin', markedRemote=true}, " +
                "uEntity=UEntity{name='body.access', version='1'}, " +
                "uResource=UResource{name='', instance='null', message='null'}}";
        assertEquals(expectedUri2, uri2.toString());
    }

    @Test
    @DisplayName("Test creating full local uri")
    public void test_create_full_local_uri() {
        UAuthority uAuthority = UAuthority.local();
        UEntity use = UEntity.fromName("body.access");
        UResource uResource = UResource.fromNameWithInstance("door", "front_left");

        UUri uri = new UUri(uAuthority, use, uResource);

        assertEquals(uAuthority, uri.uAuthority());
        assertEquals(use, uri.uEntity());
        assertEquals(uResource, uri.uResource());
    }

    @Test
    @DisplayName("Test creating full remote uri")
    public void test_create_full_remote_uri() {
        UAuthority uAuthority = UAuthority.remote("VCU", "MY_VIN");
        UEntity use = new UEntity("body.access", "1");
        UResource uResource = new UResource("door", "front_left", "Door");

        UUri uri = new UUri(uAuthority, use, uResource);

        assertEquals(uAuthority, uri.uAuthority());
        assertEquals(use, uri.uEntity());
        assertEquals(uResource, uri.uResource());
    }


    @Test
    @DisplayName("Test creating full uri with resource but no message using the constructor")
    public void test_create_uri_no_message_with_constructor() {
        UAuthority uAuthority = UAuthority.remote("VCU", "MY_VIN");
        UEntity use = new UEntity("body.access", "1");
        UResource uResource = UResource.fromName("door");

        UUri uri = new UUri(uAuthority, use, "door");

        assertEquals(uAuthority, uri.uAuthority());
        assertEquals(use, uri.uEntity());
        assertEquals(uResource, uri.uResource());
    }

    @Test
    @DisplayName("Test creating a uri with a null  authority, expect creation with an empty  authority")
    public void test_create_uri_null_authority() {
        UEntity use = new UEntity("body.access", "1");
        UResource uResource = UResource.fromNameWithInstance("door", "front_left");

        UUri uri = new UUri(null, use, uResource);
        assertEquals(UAuthority.empty(), uri.uAuthority());
    }

    @Test
    @DisplayName("Test creating a uri with a null  software entity, expect creation with an empty  software entity")
    public void test_create_uri_null_use() {
        UAuthority uAuthority = UAuthority.remote("VCU", "MY_VIN");
        UResource uResource = UResource.fromNameWithInstance("door", "front_left");

        UUri uri = new UUri(uAuthority, null, uResource);
        assertEquals(UEntity.empty(), uri.uEntity());
    }

    @Test
    @DisplayName("Test creating a uri with a null ulitfi resource, expect creation with an empty  resource")
    public void test_create_uri_null_uResource() {
        UAuthority uAuthority = UAuthority.remote("VCU", "MY_VIN");
        UEntity use = new UEntity("body.access", "1");
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
    @DisplayName("Test lazy initialization of the uProtocol routing string")
    public void test_lazy_initialization_of_uprotocol_routing_string() {
        UAuthority uAuthorityRemote = UAuthority.remote("VCU", "MY_VIN");
        UEntity use = new UEntity("body.access", "1");
        UResource uResource = UResource.fromNameWithInstance("door", "front_left");
        UUri uri = new UUri(uAuthorityRemote, use, uResource);

        assertEquals("//vcu.my_vin/body.access/1/door.front_left", uri.uProtocolUri());

        // call it again, should not call the function, but there is not really a way to test it.
        assertEquals("//vcu.my_vin/body.access/1/door.front_left", uri.uProtocolUri());
    }

}