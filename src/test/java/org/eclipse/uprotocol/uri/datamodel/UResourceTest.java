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
    @DisplayName("Test creating a empty Resource")
    public void test_create_empty_Resource() {
        UResource uResource = UResource.empty();
        assertTrue(uResource.name().isBlank());
        assertTrue(uResource.instance().isEmpty());
        assertTrue(uResource.message().isEmpty());
        assertTrue(uResource.id().isEmpty());
        assertTrue(uResource.isEmpty());
        assertFalse(uResource.isResolved());
        assertFalse(uResource.isLongForm());
        assertFalse(uResource.isMicroForm());
        assertFalse(uResource.isRPCMethod());
    }

    @Test
    @DisplayName("Test creating a empty Resource")
    public void test_create_empty_Resource2() {
        UResource uResource = UResource.longFormat(" ", null, null);
        assertTrue(uResource.name().isBlank());
        assertTrue(uResource.instance().isEmpty());
        assertTrue(uResource.message().isEmpty());
        assertTrue(uResource.id().isEmpty());
        assertTrue(uResource.isEmpty());
        assertFalse(uResource.isResolved());
        assertFalse(uResource.isLongForm());
        assertFalse(uResource.isMicroForm());
        assertFalse(uResource.isRPCMethod());
    }

    @Test
    @DisplayName("Test creating a Resource to be used in long formatted UUri")
    public void test_create_Resource_long_format_uuri() {
        UResource uResource = UResource.longFormat("door", "front_left", "Door");
        assertEquals("door", uResource.name());
        assertEquals("front_left", uResource.instance().orElse(""));
        assertEquals("Door", uResource.message().orElse(""));
        assertTrue(uResource.id().isEmpty());
        assertFalse(uResource.isEmpty());
        assertFalse(uResource.isResolved());
        assertTrue(uResource.isLongForm());
        assertFalse(uResource.isMicroForm());
        assertFalse(uResource.isRPCMethod());
    }

    @Test
    @DisplayName("Test creating a Resource to be used in long formatted UUri null instance")
    public void test_create_Resource_long_format_uuri_null_instance() {
        UResource uResource = UResource.longFormat("door", null, "Door");
        assertEquals("door", uResource.name());
        assertTrue(uResource.instance().isEmpty());
        assertEquals("Door", uResource.message().orElse(""));
        assertTrue(uResource.id().isEmpty());
        assertFalse(uResource.isEmpty());
        assertFalse(uResource.isResolved());
        assertTrue(uResource.isLongForm());
        assertFalse(uResource.isMicroForm());
        assertFalse(uResource.isRPCMethod());
    }

    @Test
    @DisplayName("Test creating a Resource to be used in long formatted UUri null message")
    public void test_create_Resource_long_format_uuri_null_message() {
        UResource uResource = UResource.longFormat("door", "front_left", null);
        assertEquals("door", uResource.name());
        assertEquals("front_left", uResource.instance().orElse(""));
        assertTrue(uResource.message().isEmpty());
        assertTrue(uResource.id().isEmpty());
        assertFalse(uResource.isEmpty());
        assertFalse(uResource.isResolved());
        assertTrue(uResource.isLongForm());
        assertFalse(uResource.isMicroForm());
        assertFalse(uResource.isRPCMethod());
    }

    @Test
    @DisplayName("Test creating a Resource to be used in long formatted UUri all values empty")
    public void test_create_Resource_long_format_uuri_all_empty_values() {
        UResource uResource = UResource.longFormat(null, "  ", " ");
        assertTrue(uResource.name().isBlank());
        assertTrue(uResource.instance().isEmpty());
        assertTrue(uResource.message().isEmpty());
        assertTrue(uResource.id().isEmpty());
        assertTrue(uResource.isEmpty());
        assertFalse(uResource.isResolved());
        assertFalse(uResource.isLongForm());
        assertFalse(uResource.isMicroForm());
        assertFalse(uResource.isRPCMethod());
    }

    @Test
    @DisplayName("Test creating a Resource to be used in long formatted UUri blank instance and blank message")
    public void test_create_Resource_long_format_uuri_all_empty_values_blank_instance_and_message() {
        UResource uResource = UResource.longFormat("  ", "  ", "   ");
        assertTrue(uResource.name().isBlank());
        assertTrue(uResource.instance().isEmpty());
        assertTrue(uResource.message().isEmpty());
        assertTrue(uResource.id().isEmpty());
        assertTrue(uResource.isEmpty());
        assertFalse(uResource.isResolved());
        assertFalse(uResource.isLongForm());
        assertFalse(uResource.isMicroForm());
        assertFalse(uResource.isRPCMethod());
    }

    @Test
    @DisplayName("Test creating a Resource to be used in long formatted UUri only name")
    public void test_create_Resource_long_format_uuri_only_name() {
        UResource uResource = UResource.longFormat("door");
        assertEquals("door", uResource.name());
        assertTrue(uResource.instance().isEmpty());
        assertTrue(uResource.message().isEmpty());
        assertTrue(uResource.id().isEmpty());
        assertFalse(uResource.isEmpty());
        assertFalse(uResource.isResolved());
        assertTrue(uResource.isLongForm());
        assertFalse(uResource.isMicroForm());
        assertFalse(uResource.isRPCMethod());
    }

    @Test
    @DisplayName("Test creating a Resource to be used in long formatted UUri only name but null")
    public void test_create_Resource_long_format_uuri_only_name_when_null() {
        UResource uResource = UResource.longFormat(null);
        assertTrue(uResource.name().isBlank());
        assertTrue(uResource.instance().isEmpty());
        assertTrue(uResource.message().isEmpty());
        assertTrue(uResource.id().isEmpty());
        assertTrue(uResource.isEmpty());
        assertFalse(uResource.isResolved());
        assertFalse(uResource.isLongForm());
        assertFalse(uResource.isMicroForm());
        assertFalse(uResource.isRPCMethod());
    }

    @Test
    @DisplayName("Test creating a Resource to be used in long formatted UUri only name but blank")
    public void test_create_Resource_long_format_uuri_only_name_when_blank() {
        UResource uResource = UResource.longFormat("  ");
        assertTrue(uResource.name().isBlank());
        assertTrue(uResource.instance().isEmpty());
        assertTrue(uResource.message().isEmpty());
        assertTrue(uResource.id().isEmpty());
        assertTrue(uResource.isEmpty());
        assertFalse(uResource.isResolved());
        assertFalse(uResource.isLongForm());
        assertFalse(uResource.isMicroForm());
        assertFalse(uResource.isRPCMethod());
    }

    @Test
    @DisplayName("Test creating a Resource to be used in micro formatted UUri")
    public void test_create_Resource_micro_format_uuri() {
        Short id = 42;
        Short notused = 0;
        UResource uResource = UResource.microFormat(id);
        assertTrue(uResource.name().isEmpty());
        assertTrue(uResource.instance().isEmpty());
        assertTrue(uResource.message().isEmpty());
        assertEquals(id, uResource.id().orElse(notused));
        assertFalse(uResource.isEmpty());
        assertFalse(uResource.isResolved());
        assertFalse(uResource.isLongForm());
        assertTrue(uResource.isMicroForm());
        assertFalse(uResource.isRPCMethod());
    }

    @Test
    @DisplayName("Test creating a Resource to be used in micro formatted UUri id is null")
    public void test_create_Resource_micro_format_uuri_id_is_null() {
        UResource uResource = UResource.microFormat(null);
        assertTrue(uResource.name().isEmpty());
        assertTrue(uResource.instance().isEmpty());
        assertTrue(uResource.message().isEmpty());
        assertTrue(uResource.id().isEmpty());
        assertTrue(uResource.isEmpty());
        assertFalse(uResource.isResolved());
        assertFalse(uResource.isLongForm());
        assertFalse(uResource.isMicroForm());
        assertFalse(uResource.isRPCMethod());
    }

    @Test
    @DisplayName("Test creating a fully resolved Resource to be used in long and micro formatted UUri")
    public void test_create_resolved_Resource_long_and_micro_format_uuri() {
        Short id = 42;
        Short notused = 0;
        UResource uResource = UResource.resolvedFormat("door", "front_left", "Door", id);
        assertEquals("door", uResource.name());
        assertEquals("front_left", uResource.instance().orElse(""));
        assertEquals("Door", uResource.message().orElse(""));
        assertEquals(id, uResource.id().orElse(notused));
        assertFalse(uResource.isEmpty());
        assertTrue(uResource.isResolved());
        assertTrue(uResource.isLongForm());
        assertTrue(uResource.isMicroForm());
        assertFalse(uResource.isRPCMethod());
    }

    @Test
    @DisplayName("Test creating a fully resolved Resource to be used in long and micro formatted UUri empty name")
    public void test_create_resolved_Resource_long_and_micro_format_uuri_empty_name() {
        Short id = 42;
        Short notused = 0;
        UResource uResource = UResource.resolvedFormat("  ", "front_left", "Door", id);
        assertTrue(uResource.name().isBlank());
        assertEquals("front_left", uResource.instance().orElse(""));
        assertEquals("Door", uResource.message().orElse(""));
        assertEquals(id, uResource.id().orElse(notused));
        assertFalse(uResource.isEmpty());
        assertFalse(uResource.isResolved());
        assertFalse(uResource.isLongForm());
        assertTrue(uResource.isMicroForm());
        assertFalse(uResource.isRPCMethod());
    }

    @Test
    @DisplayName("Test creating a fully resolved Resource to be used in long and micro formatted UUri empty instance")
    public void test_create_resolved_Resource_long_and_micro_format_uuri_empty_instance() {
        Short id = 42;
        Short notused = 0;
        UResource uResource = UResource.resolvedFormat("door", null, "Door", id);
        assertEquals("door", uResource.name());
        assertTrue(uResource.instance().isEmpty());
        assertEquals("Door", uResource.message().orElse(""));
        assertEquals(id, uResource.id().orElse(notused));
        assertFalse(uResource.isEmpty());
        assertTrue(uResource.isResolved());
        assertTrue(uResource.isLongForm());
        assertTrue(uResource.isMicroForm());
        assertFalse(uResource.isRPCMethod());
    }

    @Test
    @DisplayName("Test creating a fully resolved Resource to be used in long and micro formatted UUri empty id")
    public void test_create_resolved_Resource_long_and_micro_format_uuri_empty_id() {
        UResource uResource = UResource.resolvedFormat("door", "front_left", "Door", null);
        assertEquals("door", uResource.name());
        assertEquals("front_left", uResource.instance().orElse(""));
        assertEquals("Door", uResource.message().orElse(""));
        assertTrue(uResource.id().isEmpty());
        assertFalse(uResource.isEmpty());
        assertFalse(uResource.isResolved());
        assertTrue(uResource.isLongForm());
        assertFalse(uResource.isMicroForm());
        assertFalse(uResource.isRPCMethod());
    }

    @Test
    @DisplayName("Test creating a fully resolved Resource to be used in long and micro formatted UUri empty all")
    public void test_create_resolved_Resource_long_and_micro_format_uuri_empty_all() {
        UResource uResource = UResource.resolvedFormat(null, "  ", "  ", null);
        assertTrue(uResource.name().isBlank());
        assertTrue(uResource.instance().isEmpty());
        assertTrue(uResource.message().isEmpty());
        assertTrue(uResource.id().isEmpty());
        assertTrue(uResource.isEmpty());
        assertFalse(uResource.isResolved());
        assertFalse(uResource.isLongForm());
        assertFalse(uResource.isMicroForm());
        assertFalse(uResource.isRPCMethod());
    }

    @Test
    @DisplayName("Test creating rpc request for long formatted UUri")
    public void test_create_rpc_request_long_format() {
        UResource uResource = UResource.forRpcRequest("ExecuteDoorCommand");
        assertEquals("rpc", uResource.name());
        assertEquals("ExecuteDoorCommand", uResource.instance().orElse(""));
        assertTrue(uResource.message().isEmpty());
        assertTrue(uResource.id().isEmpty());
        assertFalse(uResource.isEmpty());
        assertFalse(uResource.isResolved());
        assertTrue(uResource.isLongForm());
        assertFalse(uResource.isMicroForm());
        assertTrue(uResource.isRPCMethod());
    }

    @Test
    @DisplayName("Test creating rpc request for long formatted UUri empty command name")
    public void test_create_rpc_request_long_format_empty_command_Name() {
        UResource uResource = UResource.forRpcRequest("  ");
        assertEquals("rpc", uResource.name());
        assertTrue(uResource.instance().isEmpty());
        assertTrue(uResource.message().isEmpty());
        assertTrue(uResource.id().isEmpty());
        assertTrue(uResource.isEmpty());
        assertFalse(uResource.isResolved());
        assertFalse(uResource.isLongForm());
        assertFalse(uResource.isMicroForm());
        assertFalse(uResource.isRPCMethod());
    }

    @Test
    @DisplayName("Test creating rpc request for long formatted UUri null command name")
    public void test_create_rpc_request_long_format_null_command_Name() {
        String commandName = null;
        UResource uResource = UResource.forRpcRequest(commandName);
        assertEquals("rpc", uResource.name());
        assertTrue(uResource.instance().isEmpty());
        assertTrue(uResource.message().isEmpty());
        assertTrue(uResource.id().isEmpty());
        assertTrue(uResource.isEmpty());
        assertFalse(uResource.isResolved());
        assertFalse(uResource.isLongForm());
        assertFalse(uResource.isMicroForm());
        assertFalse(uResource.isRPCMethod());
    }

    @Test
    @DisplayName("Test creating rpc request for micro formatted UUri")
    public void test_create_rpc_request_micro_format() {
        Short id = 42;
        Short notused = 0;
        UResource uResource = UResource.forRpcRequest(id);
        assertEquals("rpc", uResource.name());
        assertTrue(uResource.instance().isEmpty());
        assertTrue(uResource.message().isEmpty());
        assertEquals(id, uResource.id().orElse(notused));
        assertFalse(uResource.isEmpty());
        assertFalse(uResource.isResolved());
        assertFalse(uResource.isLongForm());
        assertTrue(uResource.isMicroForm());
        assertTrue(uResource.isRPCMethod());
    }

    @Test
    @DisplayName("Test creating rpc request for micro formatted UUri null id")
    public void test_create_rpc_request_micro_format_null_id() {
        Short id = null;
        UResource uResource = UResource.forRpcRequest(id);
        assertEquals("rpc", uResource.name());
        assertTrue(uResource.instance().isEmpty());
        assertTrue(uResource.message().isEmpty());
        assertTrue(uResource.id().isEmpty());
        assertTrue(uResource.isEmpty());
        assertFalse(uResource.isResolved());
        assertFalse(uResource.isLongForm());
        assertFalse(uResource.isMicroForm());
        assertFalse(uResource.isRPCMethod());
    }

    @Test
    @DisplayName("Test creating resolved rpc request for long and micro formatted UUri")
    public void test_create_resolved_rpc_request_long_and_micro_format() {
        Short id = 42;
        Short notused = 0;
        UResource uResource = UResource.forRpcRequest("ExecuteDoorCommand", id);
        assertEquals("rpc", uResource.name());
        assertEquals("ExecuteDoorCommand", uResource.instance().orElse(""));
        assertTrue(uResource.message().isEmpty());
        assertEquals(id, uResource.id().orElse(notused));
        assertFalse(uResource.isEmpty());
        assertTrue(uResource.isResolved());
        assertTrue(uResource.isLongForm());
        assertTrue(uResource.isMicroForm());
        assertTrue(uResource.isRPCMethod());
    }

    @Test
    @DisplayName("Test creating resolved rpc request for long and micro formatted UUri id is null")
    public void test_create_resolved_rpc_request_long_and_micro_format_id_null() {
        Short id = null;
        UResource uResource = UResource.forRpcRequest("ExecuteDoorCommand", id);
        assertEquals("rpc", uResource.name());
        assertEquals("ExecuteDoorCommand", uResource.instance().orElse(""));
        assertTrue(uResource.message().isEmpty());
        assertTrue(uResource.id().isEmpty());
        assertFalse(uResource.isEmpty());
        assertFalse(uResource.isResolved());
        assertTrue(uResource.isLongForm());
        assertFalse(uResource.isMicroForm());
        assertTrue(uResource.isRPCMethod());
    }

    @Test
    @DisplayName("Test creating resolved rpc request for long and micro formatted UUri null method name")
    public void test_create_resolved_rpc_request_long_and_micro_format_null_method_name() {
        Short id = 42;
        Short notused = 0;
        UResource uResource = UResource.forRpcRequest(" ", id);
        assertEquals("rpc", uResource.name());
        assertTrue(uResource.instance().isEmpty());
        assertTrue(uResource.message().isEmpty());
        assertEquals(id, uResource.id().orElse(notused));
        assertFalse(uResource.isEmpty());
        assertFalse(uResource.isResolved());
        assertFalse(uResource.isLongForm());
        assertTrue(uResource.isMicroForm());
        assertTrue(uResource.isRPCMethod());
    }

    @Test
    @DisplayName("Test creating resolved rpc request for long and micro formatted UUri missing values")
    public void test_create_resolved_rpc_request_long_and_micro_format_missing_values() {
        UResource uResource = UResource.forRpcRequest(null, null);
        assertEquals("rpc", uResource.name());
        assertTrue(uResource.instance().isEmpty());
        assertTrue(uResource.message().isEmpty());
        assertTrue(uResource.id().isEmpty());
        assertTrue(uResource.isEmpty());
        assertFalse(uResource.isResolved());
        assertFalse(uResource.isLongForm());
        assertFalse(uResource.isMicroForm());
        assertFalse(uResource.isRPCMethod());
    }

    @Test
    @DisplayName("Test creating rpc response")
    public void test_create_rpc_response() {
        Short id = 0;
        Short notused = 42;
        UResource uResource = UResource.forRpcResponse();
        assertEquals("rpc", uResource.name());
        assertEquals("response", uResource.instance().orElse(""));
        assertTrue(uResource.message().isEmpty());
        assertEquals(id, uResource.id().orElse(notused));
        assertFalse(uResource.isEmpty());
        assertTrue(uResource.isResolved());
        assertTrue(uResource.isLongForm());
        assertTrue(uResource.isMicroForm());
        assertTrue(uResource.isRPCMethod());
    }


}