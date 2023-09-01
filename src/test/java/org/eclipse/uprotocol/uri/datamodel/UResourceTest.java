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

class UResourceTest {

    @Test
    @DisplayName("Make sure the equals and hash code works")
    public void testHashCodeEquals() {
        EqualsVerifier.forClass(UResource.class).usingGetClass().verify();
    }

    @Test
    @DisplayName("Make sure the toString works")
    public void testToString() {
        UResource uResource = new UResource("door", "front_left", "Door");
        String expected = "UResource{name='door', instance='front_left', message='Door', id='unknown'}";
        assertEquals(expected, uResource.toString());
    }

    @Test
    @DisplayName("Test creating a complete  Resource")
    public void test_create_Resource() {
        UResource uResource = new UResource("door", "front_left", "Door");
        assertEquals("door", uResource.name());
        assertTrue(uResource.instance().isPresent());
        assertEquals("front_left", uResource.instance().get());
        assertTrue(uResource.message().isPresent());
        assertEquals("Door", uResource.message().get());
    }

    @Test
    @DisplayName("Test creating a complete  Resource with a null name, expect exception")
    public void test_create_Resource_null_name() {
        Exception exception = assertThrows(NullPointerException.class, () -> new UResource(null, "front_left", "Door"));
        assertTrue(exception.getMessage().contains(" Resource must have a name."));
    }

    @Test
    @DisplayName("Test creating an  Resource for RPC command with a null command name, expect exception")
    public void test_create_Resource_for_rpc_command_null_name() {
        Exception exception = assertThrows(NullPointerException.class, () -> UResource.forRpc(null));
        assertTrue(exception.getMessage().contains(" Resource must have a command name."));
    }

    @Test
    @DisplayName("Test creating a  Resource with no instance and no message")
    public void test_create_Resource_with_no_instance_and_no_message() {
        UResource uResource = new UResource("door", " ", " ");
        assertEquals("door", uResource.name());
        assertTrue(uResource.instance().isEmpty());
        assertTrue(uResource.message().isEmpty());

        UResource uResource2 = new UResource("door", null, null);
        assertEquals("door", uResource2.name());
        assertTrue(uResource.instance().isEmpty());
        assertTrue(uResource.message().isEmpty());
    }

    @Test
    @DisplayName("Test creating a  Resource using the fromName static method")
    public void test_create_Resource_with_no_instance_and_no_message_using_fromName() {
        UResource uResource = UResource.fromName("door");
        assertEquals("door", uResource.name());
        assertTrue(uResource.instance().isEmpty());
        assertTrue(uResource.message().isEmpty());
    }

    @Test
    @DisplayName("Test creating a  Resource using the fromNameWithInstance static method")
    public void test_create_Resource_with_no_message_using_fromName() {
        UResource uResource = UResource.fromNameWithInstance("door", "front_left");
        assertEquals("door", uResource.name());
        assertTrue(uResource.instance().isPresent());
        assertEquals("front_left", uResource.instance().get());
        assertTrue(uResource.message().isEmpty());
    }

    @Test
    @DisplayName("Test creating a  Resource for an RPC command on the resource")
    public void test_create_Resource_for_rpc_commands() {
        UResource uResource = UResource.forRpc("UpdateDoor");
        assertEquals("rpc", uResource.name());
        assertTrue(uResource.instance().isPresent());
        assertEquals("UpdateDoor", uResource.instance().get());
        assertTrue(uResource.isRPCMethod());
    }

    @Test
    @DisplayName("Test if the  resource represents an RPC method call")
    public void test_Resource_represents_an_rpc_method_call() {
        UResource uResource = UResource.fromNameWithInstance("rpc", "UpdateDoor");
        assertTrue(uResource.isRPCMethod());
    }

    @Test
    @DisplayName("Test if the  resource represents a resource and not an RPC method call")
    public void test_Resource_represents_a_resource_and_not_an_rpc_method_call() {
        UResource uResource = UResource.fromName("door");
        assertFalse(uResource.isRPCMethod());
    }

    @Test
    @DisplayName("Test returning a name with instance when both name and instance are configured")
    public void test_returning_a_name_with_instance_from_uResource_when_name_and_instance_are_configured() {
        UResource uResource = UResource.fromNameWithInstance("doors", "front_left");
        final String nameWithInstance = uResource.nameWithInstance();
        assertEquals("doors.front_left", nameWithInstance);
    }

    @Test
    @DisplayName("Test returning a name with instance when only name is configured")
    public void test_returning_a_name_with_instance_from_uResource_when_only_name_is_configured() {
        UResource uResource = UResource.fromName("door");
        final String nameWithInstance = uResource.nameWithInstance();
        assertEquals("door", nameWithInstance);
    }

    @Test
    @DisplayName("Test returning a name with instance when all properties are configured")
    public void test_returning_a_name_with_instance_from_uResource_when_all_properties_are_configured() {
        UResource uResource = new UResource("doors", "front_left", "Door");
        final String nameWithInstance = uResource.nameWithInstance();
        assertEquals("doors.front_left", nameWithInstance);
    }

    @Test
    @DisplayName("Test creating an empty  Resource using the empty static method")
    public void test_create_empty_using_empty() {
        UResource uResource = UResource.empty();
        assertTrue(uResource.name().isEmpty());
        assertTrue(uResource.instance().isEmpty());
        assertTrue(uResource.message().isEmpty());
    }

    @Test
    @DisplayName("Test the isEmpty static method")
    public void test_is_empty() {
        UResource uResource = UResource.empty();
        assertTrue(uResource.isEmpty());

        UResource uResource2 = new UResource("", null, null);
        assertTrue(uResource2.isEmpty());

        UResource uResource3 = new UResource("", "front_left", null);
        assertFalse(uResource3.isEmpty());

        UResource uResource4 = new UResource("", null, "Door");
        assertFalse(uResource4.isEmpty());
    }

    @Test
    @DisplayName("Test creating an RPC response  Resource using the response static method")
    public void test_create_rpc_response_using_response_method() {
        UResource uResource = UResource.response();
        assertFalse(uResource.name().isEmpty());
        assertEquals("rpc", uResource.name());
        assertEquals("response", uResource.instance().orElse(""));
        assertTrue(uResource.message().isEmpty());
    }

    @Test
    @DisplayName("Test creating an UResource with valid id")
    public void test_create_UResource_with_valid_id() {
        UResource uResource = new UResource("door", "front_left", "Door", (short)5);
        assertEquals("door", uResource.name());
        assertTrue(uResource.instance().isPresent());
        assertEquals("front_left", uResource.instance().get());
        assertTrue(uResource.message().isPresent());
        assertEquals("Door", uResource.message().get());
        assertTrue(uResource.id().isPresent());
        assertEquals((int)5, (int)uResource.id().get());
        assertEquals("UResource{name='door', instance='front_left', message='Door', id='5'}", uResource.toString());
    }

    @Test
    @DisplayName("Test creating an UResource with invalid id")
    public void test_create_UResource_with_invalid_id() {
        UResource uResource = new UResource("door", "front_left", "Door", null);
        assertEquals("door", uResource.name());
        assertTrue(uResource.instance().isPresent());
        assertEquals("front_left", uResource.instance().get());
        assertTrue(uResource.message().isPresent());
        assertEquals("Door", uResource.message().get());
        assertFalse(uResource.id().isPresent());
        assertEquals("UResource{name='door', instance='front_left', message='Door', id='unknown'}", uResource.toString());
    }

    @Test
    @DisplayName("Test creating an UResource by calling fromId static method")
    public void test_create_UResource_by_calling_fromId_static_method() {
        UResource uResource = UResource.fromId((short)5);
        assertEquals("unknown", uResource.name());
        assertTrue(uResource.instance().isEmpty());
        assertTrue(uResource.message().isEmpty());
        assertTrue(uResource.id().isPresent());
        assertEquals((int)5, (int)uResource.id().get());
        assertEquals("UResource{name='unknown', instance='null', message='null', id='5'}", uResource.toString());
    }

    @Test
    @DisplayName("Test creating a response UResource by calling fromId")
    public void test_create_response_UResource_by_calling_fromId() {
        UResource uResource = UResource.fromId((short)0);
        assertEquals("rpc", uResource.name());
        assertTrue(uResource.instance().isPresent());
        assertEquals("response", uResource.instance().get());
        assertTrue(uResource.message().isEmpty());
        assertTrue(uResource.id().isPresent());
        assertEquals((int)0, (int)uResource.id().get());
        assertEquals("UResource{name='rpc', instance='response', message='null', id='0'}", uResource.toString());
    }

    @Test
    @DisplayName("Test creating a response UResource passing name, instance, and id")
    public void test_create_response_UResource_passing_name_instance_and_id() {
        UResource uResource = new UResource("rpc", "response", null, (short)0);
        assertEquals("rpc", uResource.name());
        assertTrue(uResource.instance().isPresent());
        assertEquals("response", uResource.instance().get());
        assertTrue(uResource.message().isEmpty());
        assertTrue(uResource.id().isPresent());
        assertEquals((int)0, (int)uResource.id().get());
        assertEquals("UResource{name='rpc', instance='response', message='null', id='0'}", uResource.toString());
    }

    @Test
    @DisplayName("Test creating a request UResource passing name, instance, and id")
    public void test_create_request_UResource_passing_name_instance_and_id() {
        UResource uResource = new UResource("rpc", null, null, (short)0);
        assertEquals("rpc", uResource.name());
        assertTrue(uResource.instance().isEmpty());
        assertTrue(uResource.message().isEmpty());
        assertTrue(uResource.id().isPresent());
        assertEquals((int)0, (int)uResource.id().get());
        assertEquals("UResource{name='rpc', instance='null', message='null', id='0'}", uResource.toString());
    }
}