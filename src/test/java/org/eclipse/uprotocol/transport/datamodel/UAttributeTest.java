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

package org.eclipse.uprotocol.transport.datamodel;

import nl.jqno.equalsverifier.EqualsVerifier;

import org.eclipse.uprotocol.transport.datamodel.UAttributes;
import org.eclipse.uprotocol.transport.datamodel.UMessageType;
import org.eclipse.uprotocol.transport.datamodel.UPriority;
import org.eclipse.uprotocol.uri.datamodel.UAuthority;
import org.eclipse.uprotocol.uri.datamodel.UEntity;
import org.eclipse.uprotocol.uri.datamodel.UResource;
import org.eclipse.uprotocol.uri.datamodel.UUri;
import org.eclipse.uprotocol.uuid.factory.UUIDFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


public class UAttributeTest {

    @Test
    @DisplayName("Make sure the equals and hash code works")
    public void testHashCodeEquals() {
        EqualsVerifier.forClass(UAttributes.class).usingGetClass().verify();
    }

    @Test
    @DisplayName("Make sure the toString works on empty")
    public void testToString_with_empty() {
        UAttributes uAttributes = UAttributes.empty();
        assertEquals("UAttributes{id=null, type=null, priority=null, ttl=null, token='null', sink=null, plevel=null, commstatus=null, reqid=null}",
                uAttributes.toString());
    }

    @Test
    @DisplayName("Make sure the toString works")
    public void testToString() {
        final UUID id = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final UUID requestId = UUID.randomUUID();
        final UAttributes uAttributes = new UAttributes.UAttributesBuilder(id,
                UMessageType.RESPONSE, UPriority.LOW)
                .withSink(new UUri(UAuthority.local(), UEntity.longFormat("body.access"), UResource.empty()))
                .withTtl(1000)
                .withToken("someToken")
                .withPermissionLevel(1)
                .withReqId(requestId)
                .withCommStatus(5)
                .build();
        assertEquals(String.format("UAttributes{id=%s, type=RESPONSE, priority=LOW, ttl=1000, token='someToken', " +
                "sink=UriPart{uAuthority=UAuthority{device='null', domain='null', markedRemote=false, address=null, markedResolved=true}, " +
                "uEntity=UEntity{name='body.access', version=null, id=null, markedResolved=false}, " +
                "uResource=UResource{name='', instance='null', message='null', id=null, markedResolved=false}}, plevel=1, commstatus=5, " +
                "reqid=%s}",id, requestId),uAttributes.toString());

    }

    @Test
    @DisplayName("Test creating a complete UAttributes")
    public void testCreatingUattributes() {
        final UUID id = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final UUID requestId = UUID.randomUUID();
        final UUri sink = new UUri(UAuthority.local(), UEntity.longFormat("body.access"), UResource.empty());
        final UAttributes uAttributes = new UAttributes.UAttributesBuilder(id,
                UMessageType.RESPONSE, UPriority.REALTIME_INTERACTIVE)
                .withSink(sink)
                .withTtl(1000)
                .withToken("someToken")
                .withPermissionLevel(1)
                .withReqId(requestId)
                .withCommStatus(5)
                .build();
        assertEquals(id, uAttributes.id());
        assertEquals(UMessageType.RESPONSE, uAttributes.type());
        assertEquals(UPriority.REALTIME_INTERACTIVE, uAttributes.priority());
        assertTrue(uAttributes.ttl().isPresent());
        assertTrue(uAttributes.token().isPresent());
        assertTrue(uAttributes.sink().isPresent());
        assertTrue(uAttributes.plevel().isPresent());
        assertTrue(uAttributes.commstatus().isPresent());
        assertTrue(uAttributes.reqid().isPresent());
        assertEquals(sink, uAttributes.sink().orElse(UUri.empty()));
        assertEquals(1000, uAttributes.ttl().orElse(0));
        assertEquals(1, uAttributes.plevel().orElse(3));
        assertEquals("someToken", uAttributes.token().orElse(""));
        assertEquals(5, uAttributes.commstatus().orElse(0));
        assertEquals(requestId, uAttributes.reqid().orElse(UUID.randomUUID()));
    }

    @Test
    @DisplayName("Test creating a basic UAttributes, only required values")
    public void test_basic_uattribues_only_required_values() {
        final UUID id = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final UAttributes uAttributes = new UAttributes.UAttributesBuilder(id,
                UMessageType.PUBLISH, UPriority.LOW)
                .build();
        assertEquals(id, uAttributes.id());
        assertEquals(UMessageType.PUBLISH, uAttributes.type());
        assertEquals(UPriority.LOW, uAttributes.priority());
        assertTrue(uAttributes.ttl().isEmpty());
        assertTrue(uAttributes.token().isEmpty());
        assertTrue(uAttributes.sink().isEmpty());
        assertTrue(uAttributes.plevel().isEmpty());
        assertTrue(uAttributes.commstatus().isEmpty());
        assertTrue(uAttributes.reqid().isEmpty());

    }

    @Test
    @DisplayName("Test creating UAttributes builder with static factory method for a basic RPC request")
    public void test_create_uattributes_builder_for_basic_rpc_request() {
        final UUID id = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final UUri sink = new UUri(UAuthority.local(), UEntity.longFormat("body.access"), UResource.forRpcRequest("ExecuteWindowCommand"));
        final UAttributes uAttributes = UAttributes.forRpcRequest(id, sink)
                .withToken("someToken")
                .withTtl(10000)
                .build();
        assertTrue(uAttributes.isRpcRequest());
        assertEquals(id, uAttributes.id());
        assertEquals(UMessageType.REQUEST, uAttributes.type());
        assertEquals(UPriority.REALTIME_INTERACTIVE, uAttributes.priority());
        assertEquals(sink, uAttributes.sink().orElse(UUri.empty()));
        assertEquals("someToken", uAttributes.token().orElse(""));
        assertEquals(10000, uAttributes.ttl().orElse(0));
    }

    @Test
    @DisplayName("Test creating UAttributes builder with static factory method for a basic RPC request with values")
    public void test_create_uattributes_builder_for_basic_rpc_request_with_values() {
        final UUID id = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final UAttributes uAttributes = UAttributes.forRpcRequest(id, UAuthority.local(), UEntity.longFormat("body.access"), "ExecuteWindowCommand")
                .withToken("someToken")
                .withTtl(10000)
                .build();
        assertTrue(uAttributes.isRpcRequest());
        assertEquals(id, uAttributes.id());
        assertEquals(UMessageType.REQUEST, uAttributes.type());
        assertEquals(UPriority.REALTIME_INTERACTIVE, uAttributes.priority());
        assertEquals("body.access", uAttributes.sink().orElse(UUri.empty()).uEntity().name());
        assertEquals("someToken", uAttributes.token().orElse(""));
        assertEquals(10000, uAttributes.ttl().orElse(0));
    }

    @Test
    @DisplayName("Test creating UAttributes builder with static factory method for a basic RPC response")
    public void test_create_uattributes_builder_for_basic_rpc_response() {
        final UUID id = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final UUri sink = new UUri(UAuthority.longRemote("vcu", String.format("%s.veh.ultifi.gm.com", "someVin")),
                UEntity.longFormat("petapp.ultifi.gm.com",1), UResource.forRpcResponse());
        final UUID requestId = UUID.randomUUID();
        final UAttributes uAttributes = UAttributes.forRpcResponse(id, sink, requestId)
                .withToken("someToken")
                .withTtl(10000)
                .build();
        assertTrue(uAttributes.isRpcResponse());
        assertEquals(id, uAttributes.id());
        assertEquals(UMessageType.RESPONSE, uAttributes.type());
        assertEquals(UPriority.REALTIME_INTERACTIVE, uAttributes.priority());
        assertEquals("petapp.ultifi.gm.com", uAttributes.sink().orElse(UUri.empty()).uEntity().name());
        assertEquals("someToken", uAttributes.token().orElse(""));
        assertEquals(10000, uAttributes.ttl().orElse(0));
    }

    @Test
    @DisplayName("Test creating UAttributes builder with static factory method for a basic RPC response with values")
    public void test_create_uattributes_builder_for_basic_rpc_response_with_values() {
        final UUID id = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final UUID requestId = UUID.randomUUID();
        final UAttributes uAttributes = UAttributes.forRpcResponse(id, UAuthority.longRemote("vcu", String.format("%s.veh.ultifi.gm.com", "someVin")),
                        UEntity.longFormat("petapp.ultifi.gm.com",1), requestId)
                .withToken("someToken")
                .withTtl(10000)
                .build();
        assertTrue(uAttributes.isRpcResponse());
        assertEquals(id, uAttributes.id());
        assertEquals(UMessageType.RESPONSE, uAttributes.type());
        assertEquals(UPriority.REALTIME_INTERACTIVE, uAttributes.priority());
        assertEquals("petapp.ultifi.gm.com", uAttributes.sink().orElse(UUri.empty()).uEntity().name());
        assertEquals("someToken", uAttributes.token().orElse(""));
        assertEquals(10000, uAttributes.ttl().orElse(0));
    }

    @Test
    @DisplayName("Test creating UAttributes with null required attribute id")
    public void test_create_uattributes_missing_required_attribute_id() {
        final UAttributes uAttributes = new UAttributes.UAttributesBuilder(null,
                UMessageType.RESPONSE, UPriority.REALTIME_INTERACTIVE)
                .build();
        assertNull(uAttributes.id());
        assertEquals(UMessageType.RESPONSE, uAttributes.type());
        assertEquals(UPriority.REALTIME_INTERACTIVE, uAttributes.priority());
    }

    @Test
    @DisplayName("Test creating UAttributes with null required attribute type")
    public void test_create_uattributes_missing_required_attribute_type() {
        final UUID id = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final UAttributes uAttributes = new UAttributes.UAttributesBuilder(id,
                null, UPriority.REALTIME_INTERACTIVE)
                .build();
        assertEquals(id, uAttributes.id());
        assertNull(uAttributes.type());
        assertEquals(UPriority.REALTIME_INTERACTIVE, uAttributes.priority());
    }

    @Test
    @DisplayName("Test creating UAttributes with null required attribute type priority")
    public void test_create_uattributes_missing_required_attribute_priority() {
        final UUID id = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final UAttributes uAttributes = new UAttributes.UAttributesBuilder(id,
                UMessageType.REQUEST, null)
                .build();
        assertEquals(id, uAttributes.id());
        assertEquals(UMessageType.REQUEST, uAttributes.type());
        assertNull(uAttributes.priority());
    }

    @Test
    @DisplayName("Test creating UAttribues with a null ttl")
    public void test_create_uattributes_with_null_ttl() {
        final UUID id = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final UAttributes uAttributes = new UAttributes.UAttributesBuilder(id,
                UMessageType.RESPONSE, UPriority.REALTIME_INTERACTIVE)
                .withTtl(null)
                .build();
        assertEquals(id, uAttributes.id());
        assertEquals(UMessageType.RESPONSE, uAttributes.type());
        assertEquals(UPriority.REALTIME_INTERACTIVE, uAttributes.priority());
        assertTrue(uAttributes.ttl().isEmpty());

    }

    @Test
    @DisplayName("Test creating UAttribues with a null or blank token")
    public void test_create_uattributes_with_null_or_blank_token() {
        final UUID id = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final UAttributes uAttributes = new UAttributes.UAttributesBuilder(id,
                UMessageType.RESPONSE, UPriority.REALTIME_INTERACTIVE)
                .withToken(null)
                .build();
        assertEquals(id, uAttributes.id());
        assertEquals(UMessageType.RESPONSE, uAttributes.type());
        assertEquals(UPriority.REALTIME_INTERACTIVE, uAttributes.priority());
        assertTrue(uAttributes.token().isEmpty());

        final UAttributes uAttributes2 = new UAttributes.UAttributesBuilder(id,
                UMessageType.RESPONSE, UPriority.REALTIME_INTERACTIVE)
                .withToken("  ")
                .build();
        assertEquals(id, uAttributes2.id());
        assertEquals(UMessageType.RESPONSE, uAttributes2.type());
        assertEquals(UPriority.REALTIME_INTERACTIVE, uAttributes2.priority());
        assertTrue(uAttributes2.token().isEmpty());
    }

    @Test
    @DisplayName("Test creating UAttribues with a null sink")
    public void test_create_uattributes_with_null_sink() {
        final UUID id = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final UAttributes uAttributes = new UAttributes.UAttributesBuilder(id,
                UMessageType.RESPONSE, UPriority.REALTIME_INTERACTIVE)
                .withSink(null)
                .build();
        assertEquals(id, uAttributes.id());
        assertEquals(UMessageType.RESPONSE, uAttributes.type());
        assertEquals(UPriority.REALTIME_INTERACTIVE, uAttributes.priority());
        assertTrue(uAttributes.sink().isEmpty());
    }

    @Test
    @DisplayName("Test creating UAttribues with a null permission level")
    public void test_create_uattributes_with_null_permission_level() {
        final UUID id = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final UAttributes uAttributes = new UAttributes.UAttributesBuilder(id,
                UMessageType.RESPONSE, UPriority.REALTIME_INTERACTIVE)
                .withPermissionLevel(null)
                .build();
        assertEquals(id, uAttributes.id());
        assertEquals(UMessageType.RESPONSE, uAttributes.type());
        assertEquals(UPriority.REALTIME_INTERACTIVE, uAttributes.priority());
        assertTrue(uAttributes.plevel().isEmpty());
    }

    @Test
    @DisplayName("Test creating UAttribues with a null communication status")
    public void test_create_uattributes_with_null_comm_status() {
        final UUID id = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final UAttributes uAttributes = new UAttributes.UAttributesBuilder(id,
                UMessageType.RESPONSE, UPriority.REALTIME_INTERACTIVE)
                .withCommStatus(null)
                .build();
        assertEquals(id, uAttributes.id());
        assertEquals(UMessageType.RESPONSE, uAttributes.type());
        assertEquals(UPriority.REALTIME_INTERACTIVE, uAttributes.priority());
        assertTrue(uAttributes.commstatus().isEmpty());
    }

    @Test
    @DisplayName("Test creating UAttribues with a null request id")
    public void test_create_uattributes_with_null_request_id() {
        final UUID id = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final UAttributes uAttributes = new UAttributes.UAttributesBuilder(id,
                UMessageType.RESPONSE, UPriority.REALTIME_INTERACTIVE)
                .withReqId(null)
                .build();
        assertEquals(id, uAttributes.id());
        assertEquals(UMessageType.RESPONSE, uAttributes.type());
        assertEquals(UPriority.REALTIME_INTERACTIVE, uAttributes.priority());
        assertTrue(uAttributes.commstatus().isEmpty());
    }

    @Test
    @DisplayName("Test is this UAttributes configured for an RPC request payload")
    public void test_is_uattributes_configured_for_rpc_request_payload() {
        final UUID id = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final UUri sink = new UUri(UAuthority.local(), UEntity.longFormat("body.access"), UResource.forRpcRequest("ExecuteWindowCommand"));
        final UAttributes uAttributes = new UAttributes.UAttributesBuilder(id,
                UMessageType.REQUEST, UPriority.REALTIME_INTERACTIVE)
                .withSink(sink)
                .build();
        assertTrue(uAttributes.isRpcRequest());
    }

    @Test
    @DisplayName("Test scenarios for UAttributes not configured for an RPC request payload")
    public void test_scenarios_for_uattributes_not_configured_for_rpc_request_payload() {
        final UUID id = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final String vin = "someVin";
        final UUri sink = new UUri(UAuthority.longRemote("vcu", String.format("%s.veh.ultifi.gm.com", vin)),
                UEntity.longFormat("body.access"), UResource.forRpcRequest("ExecuteWindowCommand"));
        final UAttributes uAttributesNoSink = new UAttributes.UAttributesBuilder(id,
                UMessageType.REQUEST, UPriority.REALTIME_INTERACTIVE)
                .build();
        assertFalse(uAttributesNoSink.isRpcRequest());

        final UAttributes uAttributesWrongType = new UAttributes.UAttributesBuilder(id,
                UMessageType.PUBLISH, UPriority.REALTIME_INTERACTIVE)
                .withSink(sink)
                .build();
        assertFalse(uAttributesWrongType.isRpcRequest());
    }

    @Test
    @DisplayName("Test is this UAttributes configured for an RPC response payload")
    public void test_is_uattributes_configured_for_rpc_response_payload() {
        final UUID id = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final UUID requestId = UUID.randomUUID();
        final UUri sink = new UUri(UAuthority.longRemote("azure", "bo.ultifi.gm.com"),
                UEntity.longFormat("petapp.ultifi.gm.com",1), UResource.empty());
        final UAttributes uAttributes = new UAttributes.UAttributesBuilder(id,
                UMessageType.RESPONSE, UPriority.REALTIME_INTERACTIVE)
                .withSink(sink)
                .withReqId(requestId)
                .build();
        assertTrue(uAttributes.isRpcResponse());
    }

    @Test
    @DisplayName("Test scenarios for UAttributes not configured for an RPC response payload")
    public void test_scenarios_for_uattributes_not_configured_for_rpc_response_payload() {
        final UUID id = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final UUID requestId = UUID.randomUUID();
        final UUri sink = new UUri(UAuthority.longRemote("azure", "bo.ultifi.gm.com"),
                UEntity.longFormat("petapp.ultifi.gm.com",1), UResource.empty());
        final UAttributes uAttributesNoSink = new UAttributes.UAttributesBuilder(id,
                UMessageType.RESPONSE, UPriority.REALTIME_INTERACTIVE)
                .withReqId(requestId)
                .build();
        assertFalse(uAttributesNoSink.isRpcResponse());

        final UAttributes uAttributesWrongType = new UAttributes.UAttributesBuilder(id,
                UMessageType.PUBLISH, UPriority.REALTIME_INTERACTIVE)
                .withSink(sink)
                .withReqId(requestId)
                .build();
        assertFalse(uAttributesWrongType.isRpcResponse());

        final UAttributes uAttributesNoRequestId = new UAttributes.UAttributesBuilder(id,
                UMessageType.RESPONSE, UPriority.REALTIME_INTERACTIVE)
                .withSink(sink)
                .build();
        assertFalse(uAttributesNoRequestId.isRpcResponse());

        final UAttributes simplePublish = new UAttributes.UAttributesBuilder(id,
                UMessageType.PUBLISH, UPriority.REALTIME_INTERACTIVE)
                .build();
        assertFalse(simplePublish.isRpcResponse());
    }

    @Test
    @DisplayName("Test is this UAttributes configured for payload where there was no platform error")
    public void test_is_uattributes_configured_for_payload_with_no_platform_error() {
        final UUID id = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final UAttributes uAttributes = new UAttributes.UAttributesBuilder(id,
                UMessageType.PUBLISH, UPriority.REALTIME_INTERACTIVE)
                .build();
        assertTrue(uAttributes.isPlatformTransportSuccess());

        final UAttributes alsoOK = new UAttributes.UAttributesBuilder(id,
                UMessageType.PUBLISH, UPriority.REALTIME_INTERACTIVE)
                .withCommStatus(0)
                .build();
        assertTrue(alsoOK.isPlatformTransportSuccess());
    }

    @Test
    @DisplayName("Test is this UAttributes configured for payload where there was a platform error")
    public void test_is_uattributes_configured_for_payload_with_platform_error() {
        final UUID id = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final UAttributes uAttributes = new UAttributes.UAttributesBuilder(id,
                UMessageType.PUBLISH, UPriority.REALTIME_INTERACTIVE)
                .withCommStatus(3)
                .build();
        assertFalse(uAttributes.isPlatformTransportSuccess());
    }

}
