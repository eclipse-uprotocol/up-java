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
        UResource uResource = UResource.longFormat("door", "front_left", "Door");
        String expected = "UResource{name='door', instance='front_left', message='Door', id=null, markedResolved=false}";
        assertEquals(expected, uResource.toString());
        assertFalse(uResource.isEmpty());
    }

    @Test
    @DisplayName("Test creating a complete  Resource")
    public void test_create_Resource() {
        UResource uResource = UResource.longFormat("door", "front_left", "Door");
        assertEquals("door", uResource.name());
        assertTrue(uResource.instance().isPresent());
        assertEquals("front_left", uResource.instance().get());
        assertTrue(uResource.message().isPresent());
        assertEquals("Door", uResource.message().get());
        assertFalse(uResource.isEmpty());
    }

    @Test
    @DisplayName("Test creating a  Resource with no instance and no message")
    public void test_create_Resource_with_no_instance_and_no_message() {
        UResource uResource = UResource.longFormat("door", " ", " ");
        assertEquals("door", uResource.name());
        assertTrue(uResource.instance().isEmpty());
        assertTrue(uResource.message().isEmpty());
        assertFalse(uResource.isEmpty());

        UResource uResource2 = UResource.longFormat("door", null, null);
        assertEquals("door", uResource2.name());
        assertTrue(uResource.instance().isEmpty());
        assertTrue(uResource.message().isEmpty());
        assertFalse(uResource.isEmpty());
    }

    @Test
    @DisplayName("Test creating a  Resource using the fromName static method")
    public void test_create_Resource_with_no_instance_and_no_message_using_fromName() {
        UResource uResource = UResource.longFormat("door");
        assertEquals("door", uResource.name());
        assertTrue(uResource.instance().isEmpty());
        assertTrue(uResource.message().isEmpty());
        assertFalse(uResource.isEmpty());
    }

    @Test
    @DisplayName("Test creating a  Resource using the fromNameWithInstance static method")
    public void test_create_Resource_with_no_message_using_fromName() {
        UResource uResource = UResource.longFormat("door", "front_left", null);
        assertEquals("door", uResource.name());
        assertTrue(uResource.instance().isPresent());
        assertEquals("front_left", uResource.instance().get());
        assertTrue(uResource.message().isEmpty());
        assertFalse(uResource.isEmpty());
    }

    @Test
    @DisplayName("Test creating a  Resource for an RPC command on the resource")
    public void test_create_Resource_for_rpc_commands() {
        UResource uResource = UResource.forRpcRequest("UpdateDoor");
        assertEquals("rpc", uResource.name());
        assertTrue(uResource.instance().isPresent());
        assertEquals("UpdateDoor", uResource.instance().get());
        assertTrue(uResource.isRPCMethod());
        assertFalse(uResource.isEmpty());
    }

    @Test
    @DisplayName("Test if the  resource represents an RPC method call")
    public void test_Resource_represents_an_rpc_method_call() {
        UResource uResource = UResource.forRpcRequest("UpdateDoor");
        assertTrue(uResource.isRPCMethod());
    }

    @Test
    @DisplayName("Test if the  resource represents a resource and not an RPC method call")
    public void test_Resource_represents_a_resource_and_not_an_rpc_method_call() {
        UResource uResource = UResource.longFormat("door");
        assertFalse(uResource.isRPCMethod());
        assertFalse(uResource.isEmpty());
    }



    @Test
    @DisplayName("Test creating an empty  Resource using the empty static method")
    public void test_create_empty_using_empty() {
        UResource uResource = UResource.empty();
        assertTrue(uResource.name().isEmpty());
        assertTrue(uResource.instance().isEmpty());
        assertTrue(uResource.message().isEmpty());
        assertTrue(uResource.isEmpty());
    }

    @Test
    @DisplayName("Test the isEmpty static method")
    public void test_is_empty() {
        UResource uResource = UResource.empty();
        assertTrue(uResource.isEmpty());

        UResource uResource2 = UResource.longFormat("", null, null);
        assertTrue(uResource2.isEmpty());

        UResource uResource3 = UResource.longFormat("", "front_left", null);
        assertFalse(uResource3.isEmpty());

        UResource uResource4 = UResource.longFormat("", null, "Door");
        assertFalse(uResource4.isEmpty());
    }

    @Test
    @DisplayName("Test creating an RPC response  Resource using the response static method")
    public void test_create_rpc_response_using_response_method() {
        UResource uResource = UResource.forRpcResponse();
        assertFalse(uResource.name().isEmpty());
        assertEquals("rpc", uResource.name());
        assertEquals("response", uResource.instance().orElse(""));
        assertTrue(uResource.message().isEmpty());
    }

    @Test
    @DisplayName("Test creating an UResource with valid id")
    public void test_create_UResource_with_valid_id() {
        UResource uResource = UResource.resolvedFormat("door", "front_left", "Door", (short)5);
        assertEquals("door", uResource.name());
        assertTrue(uResource.instance().isPresent());
        assertEquals("front_left", uResource.instance().get());
        assertTrue(uResource.message().isPresent());
        assertEquals("Door", uResource.message().get());
        assertTrue(uResource.id().isPresent());
        assertEquals((int)5, (int)uResource.id().get());
        assertEquals("UResource{name='door', instance='front_left', message='Door', id=5, markedResolved=true}", uResource.toString());
        assertFalse(uResource.isEmpty());
    }

    @Test
    @DisplayName("Test creating an UResource with invalid id")
    public void test_create_UResource_with_invalid_id() {
        UResource uResource = UResource.resolvedFormat("door", "front_left", "Door", null);
        assertEquals("door", uResource.name());
        assertTrue(uResource.instance().isPresent());
        assertEquals("front_left", uResource.instance().get());
        assertTrue(uResource.message().isPresent());
        assertEquals("Door", uResource.message().get());
        assertFalse(uResource.id().isPresent());
        assertEquals("UResource{name='door', instance='front_left', message='Door', id=null, markedResolved=false}", uResource.toString());
        assertFalse(uResource.isEmpty());
    }

    @Test
    @DisplayName("Test creating an UResource by calling fromId static method")
    public void test_create_UResource_by_calling_fromId_static_method() {
        UResource uResource = UResource.microFormat((short)5);
        assertEquals("", uResource.name());
        assertTrue(uResource.instance().isEmpty());
        assertTrue(uResource.message().isEmpty());
        assertTrue(uResource.id().isPresent());
        assertEquals((int)5, (int)uResource.id().get());
        assertEquals("UResource{name='', instance='null', message='null', id=5, markedResolved=false}", uResource.toString());
        assertFalse(uResource.isEmpty());
    }

    @Test
    @DisplayName("Test creating a response UResource by calling fromId")
    public void test_create_response_UResource_by_calling_fromId() {
        UResource uResource = UResource.forRpcResponse();
        assertEquals("rpc", uResource.name());
        assertTrue(uResource.instance().isPresent());
        assertEquals("response", uResource.instance().get());
        assertTrue(uResource.message().isEmpty());
        assertTrue(uResource.id().isPresent());
        assertEquals((int)0, (int)uResource.id().get());
        assertEquals("UResource{name='rpc', instance='response', message='null', id=0, markedResolved=true}", uResource.toString());
        assertFalse(uResource.isEmpty());
    }

    @Test
    @DisplayName("Test creating a response UResource passing name, instance, and id")
    public void test_create_response_UResource_passing_name_instance_and_id() {
        UResource uResource = UResource.resolvedFormat("rpc", "response", null, (short)0);
        assertEquals("rpc", uResource.name());
        assertTrue(uResource.instance().isPresent());
        assertEquals("response", uResource.instance().get());
        assertTrue(uResource.message().isEmpty());
        assertTrue(uResource.id().isPresent());
        assertEquals((int)0, (int)uResource.id().get());
        assertEquals("UResource{name='rpc', instance='response', message='null', id=0, markedResolved=true}", uResource.toString());
        assertFalse(uResource.isEmpty());
    }

    @Test
    @DisplayName("Test creating a request UResource passing name, instance, and id")
    public void test_create_request_UResource_passing_name_instance_and_id() {
        UResource uResource = UResource.resolvedFormat("rpc", null, null, (short)0);
        assertEquals("rpc", uResource.name());
        assertTrue(uResource.instance().isEmpty());
        assertTrue(uResource.message().isEmpty());
        assertTrue(uResource.id().isPresent());
        assertEquals((int)0, (int)uResource.id().get());
        assertEquals("UResource{name='rpc', instance='null', message='null', id=0, markedResolved=false}", uResource.toString());
        assertFalse(uResource.isEmpty());
    }

    @Test
    @DisplayName("Test isResolved with resolved UResources")
    public void test_isResolved_with_resolved_UResources() {
        UResource uResource = UResource.resolvedFormat("door", "front_left", "Door", (short)5);
        assertTrue(uResource.isResolved());
        UResource uResource2 = UResource.forRpcResponse();
        assertTrue(uResource2.isResolved());
        UResource uResource3 = UResource.forRpcRequest("UpdateDoor", (short)5);
        assertTrue(uResource3.isResolved());
    }

    @Test
    @DisplayName("Test isResolved and isLongForm with unresolved UResources")
    public void test_isResolved_with_unresolved_UResources() {
        UResource uResource = UResource.resolvedFormat("door", "front_left", "Door", null);
        assertFalse(uResource.isResolved());
        assertTrue(uResource.isLongForm());
        UResource uResource2 = UResource.longFormat("door");
        assertFalse(uResource2.isResolved());
        assertFalse(uResource2.isLongForm());
        UResource uResource3 = UResource.forRpcRequest("UpdateDoor");
        assertFalse(uResource3.isResolved());
        assertTrue(uResource3.isLongForm());
        UResource uResource4 = UResource.microFormat((short)4);
        assertFalse(uResource4.isResolved());
        assertFalse(uResource4.isLongForm());

        UResource uResource5 = UResource.longFormat("door", "front_left", null);
        assertFalse(uResource5.isResolved());
        assertTrue(uResource5.isLongForm());

    }

    @Test
    @DisplayName("Test resolved API with all possible combinations of the APIs passed valid and invalid")
    public void test_resolved_API_with_all_possible_combinations_of_the_APIs_passed_valid_and_invalid() {
        UResource uResource = UResource.resolvedFormat("door", "front_left", "Door", (short)5);
        assertTrue(uResource.isResolved());
        UResource uResource2 = UResource.resolvedFormat("door", "front_left", "Door", null);
        assertFalse(uResource2.isResolved());
        UResource uResource3 = UResource.resolvedFormat("door", "front_left", null, (short)5);
        assertTrue(uResource3.isResolved());
        UResource uResource4 = UResource.resolvedFormat("door", "front_left", null, null);
        assertFalse(uResource4.isResolved());
        UResource uResource5 = UResource.resolvedFormat("door", null, "Door", (short)5);
        assertFalse(uResource5.isResolved());
        UResource uResource6 = UResource.resolvedFormat("door", null, "Door", null);
        assertFalse(uResource6.isResolved());
        UResource uResource7 = UResource.resolvedFormat("door", null, null, (short)5);
        assertFalse(uResource7.isResolved());
        UResource uResource8 = UResource.resolvedFormat("door", null, null, null);
        assertFalse(uResource8.isResolved());
        UResource uResource9 = UResource.resolvedFormat(null, "front_left", "Door", (short)5);
        assertFalse(uResource9.isResolved());
        UResource uResource10 = UResource.resolvedFormat(null, "front_left", "Door", null);
        assertFalse(uResource10.isResolved());
        UResource uResource11 = UResource.resolvedFormat(null, "front_left", null, (short)5);
        assertFalse(uResource11.isResolved());
        UResource uResource12 = UResource.resolvedFormat(null, "front_left", null, null);
        assertFalse(uResource12.isResolved());
        UResource uResource13 = UResource.resolvedFormat(null, null, "Door", (short)5);
        assertFalse(uResource13.isResolved());
        UResource uResource14 = UResource.resolvedFormat(null, null, "Door", null);
        assertFalse(uResource14.isResolved());
        UResource uResource15 = UResource.resolvedFormat(null, null, null, (short)5);
        assertFalse(uResource15.isResolved());
        UResource uResource16 = UResource.resolvedFormat(null, null, null, null);
        assertFalse(uResource16.isResolved());
     }

    @Test
    @DisplayName("Test forRpcRequest with null and valid short value")
    public void test_forRpcRequest_with_null_and_valid_short_value() {
        UResource uResource = UResource.forRpcRequest("UpdateDoor", (short)5);
        assertEquals("rpc", uResource.name());
        assertTrue(uResource.instance().isPresent());
        assertEquals("UpdateDoor", uResource.instance().get());
        assertTrue(uResource.message().isEmpty());
        assertTrue(uResource.id().isPresent());
        assertEquals((int)5, (int)uResource.id().get());
        assertTrue(uResource.isResolved());
        assertTrue(uResource.isLongForm());
        assertTrue(uResource.isRPCMethod());
        assertTrue(uResource.isMicroForm());

        UResource uResource2 = UResource.forRpcRequest((Short)null);
        assertEquals("rpc", uResource2.name());
        assertFalse(uResource2.instance().isPresent());
        assertTrue(uResource2.message().isEmpty());
        assertFalse(uResource2.id().isPresent());
        assertFalse(uResource2.isResolved());
        assertFalse(uResource2.isLongForm());
        assertTrue(uResource2.isRPCMethod());

        UResource uResource3 = UResource.forRpcRequest(null, null);
        assertEquals("rpc", uResource3.name());
        assertFalse(uResource3.instance().isPresent());
        assertTrue(uResource3.message().isEmpty());
        assertFalse(uResource3.id().isPresent());
        assertFalse(uResource3.isResolved());
        assertFalse(uResource3.isLongForm());
        assertTrue(uResource3.isRPCMethod());

        UResource uResource4 = UResource.forRpcRequest("",  null);
        assertEquals("rpc", uResource4.name());
        assertFalse(uResource4.instance().isPresent());
        assertTrue(uResource4.message().isEmpty());
        assertFalse(uResource4.id().isPresent());
        assertFalse(uResource4.isResolved());
        assertFalse(uResource4.isLongForm());
        assertTrue(uResource4.isRPCMethod());

        UResource uResource5 = UResource.forRpcRequest(null,  (short)2);
        assertEquals("rpc", uResource5.name());
        assertFalse(uResource5.instance().isPresent());
        assertTrue(uResource5.message().isEmpty());
        assertTrue(uResource5.id().isPresent());
        assertFalse(uResource5.isResolved());
        assertFalse(uResource5.isLongForm());
        assertTrue(uResource5.isRPCMethod());

    }
}