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

package org.eclipse.uprotocol.uri.validator;


import org.eclipse.uprotocol.uri.datamodel.UUri;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.uprotocol.uri.datamodel.UAuthority;
import org.eclipse.uprotocol.uri.datamodel.UEntity;
import org.eclipse.uprotocol.uri.datamodel.UResource;

import org.eclipse.uprotocol.uri.serializer.UriSerializer;
import org.eclipse.uprotocol.utransport.datamodel.UStatus;
import org.eclipse.uprotocol.utransport.datamodel.UStatus.Code;

class UriPartValidatorTest {

    @Test
    @DisplayName("Test validate blank uri")
    public void test_validate_blank_uri() {
        final UUri uri = UriSerializer.LONG.deserialize(null);
        final UStatus status = UriValidator.validate(uri);
        assertTrue(uri.isEmpty());
        assertEquals(Code.INVALID_ARGUMENT.value(), status.getCode());
        assertEquals("UriPart is empty.", status.msg());
    }

    @Test
    @DisplayName("Test validate uri with no device name")
    public void test_validate_uri_with_no_entity_name() {
        final UUri uri = UriSerializer.LONG.deserialize("//");
        final UStatus status = UriValidator.validate(uri);
        assertTrue(uri.isEmpty());
        assertEquals(Code.INVALID_ARGUMENT.value(), status.getCode());
        assertEquals("UriPart is empty.", status.msg());
    }

    @Test
    @DisplayName("Test validate uri with uEntity")
    public void test_validate_uri_with_uEntity() {
        final UUri uri = UriSerializer.LONG.deserialize("/hartley");
        final UStatus status = UriValidator.validate(uri);
        assertEquals(UStatus.ok(), status);
    }

    @Test
    @DisplayName("Test validate with malformed URI")
    public void test_validate_with_malformed_uri() {
        final UUri uri = UriSerializer.LONG.deserialize("hartley");
        final UStatus status = UriValidator.validate(uri);
        assertTrue(uri.isEmpty());
        assertEquals(Code.INVALID_ARGUMENT.value(), status.getCode());
        assertEquals("UriPart is empty.", status.msg());
    }
   

    @Test
    @DisplayName("Test validate with blank UEntity Name")
    public void test_validate_with_blank_uentity_name_uri() {
        final UUri uri = new UUri(UAuthority.local(), UEntity.empty(), UResource.forRpc("echo"));
        final UStatus status = UriValidator.validate(uri);
        assertFalse(uri.isEmpty());
        assertEquals(Code.INVALID_ARGUMENT.value(), status.getCode());
        assertEquals("UriPart is missing uSoftware Entity name.", status.msg());
    }

    @Test
    @DisplayName("Test validateRpcMethod with valid URI")
    public void test_validateRpcMethod_with_valid_uri() {
        final UUri uri = UriSerializer.LONG.deserialize("/hartley//rpc.echo");
        final UStatus status = UriValidator.validateRpcMethod(uri);
        assertEquals(UStatus.ok(), status);
    }
    
    @Test
    @DisplayName("Test validateRpcMethod with valid URI")
    public void test_validateRpcMethod_with_invalid_uri() {
        final UUri uri = UriSerializer.LONG.deserialize("/hartley/echo");
        final UStatus status = UriValidator.validateRpcMethod(uri);
        assertEquals(Code.INVALID_ARGUMENT.value(), status.getCode());
        assertEquals("Invalid RPC method uri. UriPart should be the method to be called, or method from response.", status.msg());
    }

    @Test
    @DisplayName("Test validateRpcMethod with malformed URI")
    public void test_validateRpcMethod_with_malformed_uri() {
        final UUri uri = UriSerializer.LONG.deserialize("hartley");
        final UStatus status = UriValidator.validateRpcMethod(uri);
        assertTrue(uri.isEmpty());
        assertEquals(Code.INVALID_ARGUMENT.value(), status.getCode());
        assertEquals("UriPart is empty.", status.msg());
    }

    @Test
    @DisplayName("Test validateRpcResponse with valid URI")
    public void test_validateRpcResponse_with_valid_uri() {
        final UUri uri = UriSerializer.LONG.deserialize("/hartley//rpc.response");
        final UStatus status = UriValidator.validateRpcResponse(uri);
        assertEquals(UStatus.ok(), status);
    }

    @Test
    @DisplayName("Test validateRpcResponse with malformed URI")
    public void test_validateRpcResponse_with_malformed_uri() {
        final UUri uri = UriSerializer.LONG.deserialize("hartley");
        final UStatus status = UriValidator.validateRpcResponse(uri);
        assertTrue(uri.isEmpty());
        assertEquals(Code.INVALID_ARGUMENT.value(), status.getCode());
        assertEquals("UriPart is empty.", status.msg());
    }

    @Test
    @DisplayName("Test validateRpcResponse with rpc type")
    public void test_validateRpcResponse_with_rpc_type() {
        final UUri uri = UriSerializer.LONG.deserialize("/hartley//dummy.wrong");
        final UStatus status = UriValidator.validateRpcResponse(uri);
        assertEquals(Code.INVALID_ARGUMENT.value(), status.getCode());
        assertEquals("Invalid RPC response type.", status.msg());
    }

    @Test
    @DisplayName("Test validateRpcResponse with invalid rpc response type")
    public void test_validateRpcResponse_with_invalid_rpc_response_type() {
        final UUri uri = UriSerializer.LONG.deserialize("/hartley//rpc.wrong");
        final UStatus status = UriValidator.validateRpcResponse(uri);
        assertEquals(Code.INVALID_ARGUMENT.value(), status.getCode());
        assertEquals("Invalid RPC response type.", status.msg());
    }

    @Test
    @DisplayName("Test validateLongUUri with valid URI")
    public void test_validateLongUUri_with_valid_uri() {
        final UUri uri = UriSerializer.LONG.deserialize("/hartley//rpc.echo");
        final UStatus status = UriValidator.validateLongUUri(UriSerializer.LONG.serialize(uri));
        assertEquals(UStatus.ok(), status);
    }

}
