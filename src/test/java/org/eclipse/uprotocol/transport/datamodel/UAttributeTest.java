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

import org.eclipse.uprotocol.uri.builder.UResourceBuilder;
import org.eclipse.uprotocol.uuid.factory.UUIDFactory;
import org.eclipse.uprotocol.v1.UAuthority;
import org.eclipse.uprotocol.v1.UEntity;
import org.eclipse.uprotocol.v1.UUri;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


public class UAttributeTest {

    @Test
    @DisplayName("Make sure the equals and hash code works")
    public void testHashCodeEquals() {
        final UUID id = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final UUID requestId = UUID.randomUUID();
        final UAttributes uAttributes = new UAttributes.UAttributesBuilder(id, UMessageType.RESPONSE, UPriority.LOW).withSink(UUri.newBuilder().setEntity(UEntity.newBuilder().setName("body.access")).build()).withTtl(1000).withToken("someToken").withPermissionLevel(1).withReqId(requestId).withCommStatus(5).build();
        final UAttributes uAttributes1 = new UAttributes.UAttributesBuilder(id, UMessageType.RESPONSE, UPriority.LOW).withSink(UUri.newBuilder().setEntity(UEntity.newBuilder().setName("body.access")).build()).withTtl(1000).withToken("someToken").withPermissionLevel(1).withReqId(requestId).withCommStatus(5).build();
        assertTrue(uAttributes.equals(uAttributes));
        assertTrue(uAttributes.equals(uAttributes1));

        assertEquals(uAttributes.hashCode(), uAttributes1.hashCode());
    }

    @Test
    @DisplayName("Make sure the equals works when reqid is different")
    public void test_equals_reqid_different() {
        final UUID id = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final UAttributes uAttributes = new UAttributes.UAttributesBuilder(id, UMessageType.RESPONSE, UPriority.LOW).withSink(UUri.newBuilder().setEntity(UEntity.newBuilder().setName("body.access")).build()).withTtl(1000).withToken("someToken").withPermissionLevel(1).withCommStatus(5).withReqId(UUID.randomUUID()).build();
        final UAttributes uAttributes1 = new UAttributes.UAttributesBuilder(id, UMessageType.RESPONSE, UPriority.LOW).withSink(UUri.newBuilder().setEntity(UEntity.newBuilder().setName("body.access")).build()).withTtl(1000).withToken("someToken").withPermissionLevel(1).withCommStatus(5).withReqId(UUID.randomUUID()).build();
        assertFalse(uAttributes.equals(uAttributes1));
    }

    @Test
    @DisplayName("Make sure the equals works when commstatus is different")
    public void test_equals_commstatus_different() {
        final UUID id = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final UUID reqid = UUID.randomUUID();
        final UAttributes uAttributes = new UAttributes.UAttributesBuilder(id, UMessageType.RESPONSE, UPriority.LOW).withSink(UUri.newBuilder().setEntity(UEntity.newBuilder().setName("body.access")).build()).withTtl(1000).withToken("someToken").withPermissionLevel(1).withCommStatus(5).withReqId(reqid).build();
        final UAttributes uAttributes1 = new UAttributes.UAttributesBuilder(id, UMessageType.RESPONSE, UPriority.LOW).withSink(UUri.newBuilder().setEntity(UEntity.newBuilder().setName("body.access")).build()).withTtl(1000).withToken("someToken").withPermissionLevel(1).withReqId(reqid).withCommStatus(4).build();
        assertFalse(uAttributes.equals(uAttributes1));
    }

    @Test
    @DisplayName("Make sure the equals works when permissionlevel is different")
    public void test_equals_permissionlevel_different() {
        final UUID id = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final UUID reqid = UUID.randomUUID();
        final UAttributes uAttributes = new UAttributes.UAttributesBuilder(id, UMessageType.RESPONSE, UPriority.LOW).withSink(UUri.newBuilder().setEntity(UEntity.newBuilder().setName("body.access")).build()).withTtl(1000).withToken("someToken").withPermissionLevel(1).withCommStatus(5).withReqId(reqid).build();
        final UAttributes uAttributes1 = new UAttributes.UAttributesBuilder(id, UMessageType.RESPONSE, UPriority.LOW).withSink(UUri.newBuilder().setEntity(UEntity.newBuilder().setName("body.access")).build()).withTtl(1000).withToken("someToken").withPermissionLevel(2).withReqId(reqid).withCommStatus(5).build();
        assertFalse(uAttributes.equals(uAttributes1));
    }

    @Test
    @DisplayName("Make sure the equals works when token is different")
    public void test_equals_token_different() {
        final UUID id = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final UUID reqid = UUID.randomUUID();
        final UAttributes uAttributes = new UAttributes.UAttributesBuilder(id, UMessageType.RESPONSE, UPriority.LOW).withSink(UUri.newBuilder().setEntity(UEntity.newBuilder().setName("body.access")).build()).withTtl(1000).withToken("someToken").withPermissionLevel(1).withCommStatus(5).withReqId(reqid).build();
        final UAttributes uAttributes1 = new UAttributes.UAttributesBuilder(id, UMessageType.RESPONSE, UPriority.LOW).withSink(UUri.newBuilder().setEntity(UEntity.newBuilder().setName("body.access")).build()).withTtl(1000).withToken("someToken1").withPermissionLevel(1).withReqId(reqid).withCommStatus(5).build();
        assertFalse(uAttributes.equals(uAttributes1));
    }

    @Test
    @DisplayName("Make sure the equals works when ttl is different")
    public void test_equals_ttl_different() {
        final UUID id = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final UUID reqid = UUID.randomUUID();
        final UAttributes uAttributes = new UAttributes.UAttributesBuilder(id, UMessageType.RESPONSE, UPriority.LOW).withSink(UUri.newBuilder().setEntity(UEntity.newBuilder().setName("body.access")).build()).withTtl(1000).withToken("someToken").withPermissionLevel(1).withCommStatus(5).withReqId(reqid).build();
        final UAttributes uAttributes1 = new UAttributes.UAttributesBuilder(id, UMessageType.RESPONSE, UPriority.LOW).withSink(UUri.newBuilder().setEntity(UEntity.newBuilder().setName("body.access")).build()).withTtl(2000).withToken("someToken").withPermissionLevel(1).withReqId(reqid).withCommStatus(5).build();
        assertFalse(uAttributes.equals(uAttributes1));
    }

    @Test
    @DisplayName("Make sure the equals works when entity is different")
    public void test_equals_entity_different() {
        final UUID id = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final UUID reqid = UUID.randomUUID();
        final UAttributes uAttributes = new UAttributes.UAttributesBuilder(id, UMessageType.RESPONSE, UPriority.LOW).withSink(UUri.newBuilder().setEntity(UEntity.newBuilder().setName("body.access")).build()).withTtl(1000).withToken("someToken").withPermissionLevel(1).withCommStatus(5).withReqId(reqid).build();
        final UAttributes uAttributes1 = new UAttributes.UAttributesBuilder(id, UMessageType.RESPONSE, UPriority.LOW).withSink(UUri.newBuilder().setEntity(UEntity.newBuilder().setName("body.cabin")).build()).withTtl(1000).withToken("someToken").withPermissionLevel(1).withReqId(reqid).withCommStatus(5).build();
        assertFalse(uAttributes.equals(uAttributes1));
    }

    @Test
    @DisplayName("Make sure the equals works when id is different")
    public void test_equals_id_different() {
        final UUID id = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final UUID id1 = UUIDFactory.Factories.UPROTOCOL.factory().create();

        final UUID reqid = UUID.randomUUID();
        final UAttributes uAttributes = new UAttributes.UAttributesBuilder(id, UMessageType.RESPONSE, UPriority.LOW).withSink(UUri.newBuilder().setEntity(UEntity.newBuilder().setName("body.access")).build()).withTtl(1000).withToken("someToken").withPermissionLevel(1).withCommStatus(5).withReqId(reqid).build();
        final UAttributes uAttributes1 = new UAttributes.UAttributesBuilder(id1, UMessageType.RESPONSE, UPriority.LOW).withSink(UUri.newBuilder().setEntity(UEntity.newBuilder().setName("body.access")).build()).withTtl(1000).withToken("someToken").withPermissionLevel(1).withReqId(reqid).withCommStatus(5).build();
        assertFalse(uAttributes.equals(uAttributes1));
    }

    @Test
    @DisplayName("Make sure the equals works when type is different")
    public void test_equals_type_different() {
        final UUID id = UUIDFactory.Factories.UPROTOCOL.factory().create();

        final UUID reqid = UUID.randomUUID();
        final UAttributes uAttributes = new UAttributes.UAttributesBuilder(id, UMessageType.RESPONSE, UPriority.LOW).withSink(UUri.newBuilder().setEntity(UEntity.newBuilder().setName("body.access")).build()).withTtl(1000).withToken("someToken").withPermissionLevel(1).withCommStatus(5).withReqId(reqid).build();
        final UAttributes uAttributes1 = new UAttributes.UAttributesBuilder(id, UMessageType.PUBLISH, UPriority.LOW).withSink(UUri.newBuilder().setEntity(UEntity.newBuilder().setName("body.access")).build()).withTtl(1000).withToken("someToken").withPermissionLevel(1).withReqId(reqid).withCommStatus(5).build();
        assertFalse(uAttributes.equals(uAttributes1));
    }
    @Test
    @DisplayName("Make sure the equals works when object is not UAttributes")
    public void test_equals_not_uattribute_object() {
        final UUID id = UUIDFactory.Factories.UPROTOCOL.factory().create();

        final UUID reqid = UUID.randomUUID();
        final UAttributes uAttributes = new UAttributes.UAttributesBuilder(id, UMessageType.RESPONSE, UPriority.LOW).withSink(UUri.newBuilder().setEntity(UEntity.newBuilder().setName("body.access")).build()).withTtl(1000).withToken("someToken").withPermissionLevel(1).withCommStatus(5).withReqId(reqid).build();
        UPayload uPayload = new UPayload(null, USerializationHint.PROTOBUF);
        assertFalse(uAttributes.equals(uPayload));
    }
    @Test
    @DisplayName("Make sure the equals works when null uattributes")
    public void test_equals_null_uattribute() {
        final UUID id = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final UUID reqid = UUID.randomUUID();
        final UAttributes uAttributes = new UAttributes.UAttributesBuilder(id, UMessageType.RESPONSE, UPriority.LOW).withSink(UUri.newBuilder().setEntity(UEntity.newBuilder().setName("body.access")).build()).withTtl(1000).withToken("someToken").withPermissionLevel(1).withCommStatus(5).withReqId(reqid).build();
        assertFalse(uAttributes.equals(null));
    }

    @Test
    @DisplayName("Create a UPayload without a byte array but with some weird hint")
    public void create_upayload_without_byte_array_but_with_weird_hint() {
        UPayload uPayload = new UPayload(null, USerializationHint.PROTOBUF);
        assertEquals(0, uPayload.data().length);
        assertTrue(uPayload.isEmpty());
        assertEquals(USerializationHint.PROTOBUF, uPayload.hint());
        assertNotEquals(UPayload.empty(), uPayload);

    }

    @Test
    @DisplayName("Make sure the toString works on empty")
    public void testToString_with_empty() {
        UAttributes uAttributes = UAttributes.empty();
        assertEquals("UAttributes{id=null, type=null, priority=null, ttl=null, token='null', sink=null, plevel=null, commstatus=null, reqid=null}", uAttributes.toString());
    }

    @Test
    @DisplayName("Make sure the toString works")
    public void testToString() {
        final UUID id = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final UUID requestId = UUID.randomUUID();
        final UAttributes uAttributes = new UAttributes.UAttributesBuilder(id, UMessageType.RESPONSE, UPriority.LOW).withSink(UUri.newBuilder().setEntity(UEntity.newBuilder().setName("body.access")).build()).withTtl(1000).withToken("someToken").withPermissionLevel(1).withReqId(requestId).withCommStatus(5).build();
        assertEquals(String.format("UAttributes{id=%s, type=RESPONSE, priority=LOW, ttl=1000, token='someToken', " + "sink=entity {\n  name: \"body.access\"\n}\n" + ", plevel=1, commstatus=5, " + "reqid=%s}", id, requestId), uAttributes.toString());

    }

    @Test
    @DisplayName("Test creating a complete UAttributes")
    public void testCreatingUattributes() {
        final UUID id = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final UUID requestId = UUID.randomUUID();
        final UUri sink = UUri.newBuilder().setEntity(UEntity.newBuilder().setName("body.access")).build();
        final UAttributes uAttributes = new UAttributes.UAttributesBuilder(id, UMessageType.RESPONSE, UPriority.REALTIME_INTERACTIVE).withSink(sink).withTtl(1000).withToken("someToken").withPermissionLevel(1).withReqId(requestId).withCommStatus(5).build();
        assertEquals(id, uAttributes.id());
        assertEquals(UMessageType.RESPONSE, uAttributes.type());
        assertEquals(UPriority.REALTIME_INTERACTIVE, uAttributes.priority());
        assertTrue(uAttributes.ttl().isPresent());
        assertTrue(uAttributes.token().isPresent());
        assertTrue(uAttributes.sink().isPresent());
        assertTrue(uAttributes.plevel().isPresent());
        assertTrue(uAttributes.commstatus().isPresent());
        assertTrue(uAttributes.reqid().isPresent());
        assertEquals(sink, uAttributes.sink().orElse(UUri.getDefaultInstance()));
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
        final UAttributes uAttributes = new UAttributes.UAttributesBuilder(id, UMessageType.PUBLISH, UPriority.LOW).build();
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

        // source
        final UUri sink = UUri.newBuilder().setEntity(UEntity.newBuilder().setName("body.access")).setResource(UResourceBuilder.forRpcRequest("ExecuteWindowCommand")).build();

        final UAttributes uAttributes = UAttributes.forRpcRequest(id, sink).withToken("someToken").withTtl(10000).build();
        assertTrue(uAttributes.isRpcRequest());
        assertEquals(id, uAttributes.id());
        assertEquals(UMessageType.REQUEST, uAttributes.type());
        assertEquals(UPriority.REALTIME_INTERACTIVE, uAttributes.priority());
        assertEquals(sink, uAttributes.sink().orElse(UUri.getDefaultInstance()));
        assertEquals("someToken", uAttributes.token().orElse(""));
        assertEquals(10000, uAttributes.ttl().orElse(0));
    }


    @Test
    @DisplayName("Test creating UAttributes builder with static factory method for a basic RPC response")
    public void test_create_uattributes_builder_for_basic_rpc_response() {
        final UUID id = UUIDFactory.Factories.UPROTOCOL.factory().create();

        final UUri sink = UUri.newBuilder().setAuthority(UAuthority.newBuilder().setName("vcu.veh.ultifi.gm.com")).setEntity(UEntity.newBuilder().setName("petapp.ultifi.gm.com").setVersionMajor(1)).setResource(UResourceBuilder.forRpcResponse()).build();

        final UUID requestId = UUID.randomUUID();
        final UAttributes uAttributes = UAttributes.forRpcResponse(id, sink, requestId).withToken("someToken").withTtl(10000).build();
        assertTrue(uAttributes.isRpcResponse());
        assertEquals(id, uAttributes.id());
        assertEquals(UMessageType.RESPONSE, uAttributes.type());
        assertEquals(UPriority.REALTIME_INTERACTIVE, uAttributes.priority());
        assertEquals("petapp.ultifi.gm.com", uAttributes.sink().orElse(UUri.getDefaultInstance()).getEntity().getName());
        assertEquals("someToken", uAttributes.token().orElse(""));
        assertEquals(10000, uAttributes.ttl().orElse(0));
    }

    @Test
    @DisplayName("Test creating UAttributes with null required attribute id")
    public void test_create_uattributes_missing_required_attribute_id() {
        final UAttributes uAttributes = new UAttributes.UAttributesBuilder(null, UMessageType.RESPONSE, UPriority.REALTIME_INTERACTIVE).build();
        assertNull(uAttributes.id());
        assertEquals(UMessageType.RESPONSE, uAttributes.type());
        assertEquals(UPriority.REALTIME_INTERACTIVE, uAttributes.priority());
    }

    @Test
    @DisplayName("Test creating UAttributes with null required attribute type")
    public void test_create_uattributes_missing_required_attribute_type() {
        final UUID id = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final UAttributes uAttributes = new UAttributes.UAttributesBuilder(id, null, UPriority.REALTIME_INTERACTIVE).build();
        assertEquals(id, uAttributes.id());
        assertNull(uAttributes.type());
        assertEquals(UPriority.REALTIME_INTERACTIVE, uAttributes.priority());
    }

    @Test
    @DisplayName("Test creating UAttributes with null required attribute type priority")
    public void test_create_uattributes_missing_required_attribute_priority() {
        final UUID id = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final UAttributes uAttributes = new UAttributes.UAttributesBuilder(id, UMessageType.REQUEST, null).build();
        assertEquals(id, uAttributes.id());
        assertEquals(UMessageType.REQUEST, uAttributes.type());
        assertNull(uAttributes.priority());
    }

    @Test
    @DisplayName("Test creating UAttribues with a null ttl")
    public void test_create_uattributes_with_null_ttl() {
        final UUID id = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final UAttributes uAttributes = new UAttributes.UAttributesBuilder(id, UMessageType.RESPONSE, UPriority.REALTIME_INTERACTIVE).withTtl(null).build();
        assertEquals(id, uAttributes.id());
        assertEquals(UMessageType.RESPONSE, uAttributes.type());
        assertEquals(UPriority.REALTIME_INTERACTIVE, uAttributes.priority());
        assertTrue(uAttributes.ttl().isEmpty());

    }

    @Test
    @DisplayName("Test creating UAttribues with a null or blank token")
    public void test_create_uattributes_with_null_or_blank_token() {
        final UUID id = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final UAttributes uAttributes = new UAttributes.UAttributesBuilder(id, UMessageType.RESPONSE, UPriority.REALTIME_INTERACTIVE).withToken(null).build();
        assertEquals(id, uAttributes.id());
        assertEquals(UMessageType.RESPONSE, uAttributes.type());
        assertEquals(UPriority.REALTIME_INTERACTIVE, uAttributes.priority());
        assertTrue(uAttributes.token().isEmpty());

        final UAttributes uAttributes2 = new UAttributes.UAttributesBuilder(id, UMessageType.RESPONSE, UPriority.REALTIME_INTERACTIVE).withToken("  ").build();
        assertEquals(id, uAttributes2.id());
        assertEquals(UMessageType.RESPONSE, uAttributes2.type());
        assertEquals(UPriority.REALTIME_INTERACTIVE, uAttributes2.priority());
        assertTrue(uAttributes2.token().isEmpty());
    }

    @Test
    @DisplayName("Test creating UAttribues with a null sink")
    public void test_create_uattributes_with_null_sink() {
        final UUID id = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final UAttributes uAttributes = new UAttributes.UAttributesBuilder(id, UMessageType.RESPONSE, UPriority.REALTIME_INTERACTIVE).withSink(null).build();
        assertEquals(id, uAttributes.id());
        assertEquals(UMessageType.RESPONSE, uAttributes.type());
        assertEquals(UPriority.REALTIME_INTERACTIVE, uAttributes.priority());
        assertTrue(uAttributes.sink().isEmpty());
    }

    @Test
    @DisplayName("Test creating UAttribues with a null permission level")
    public void test_create_uattributes_with_null_permission_level() {
        final UUID id = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final UAttributes uAttributes = new UAttributes.UAttributesBuilder(id, UMessageType.RESPONSE, UPriority.REALTIME_INTERACTIVE).withPermissionLevel(null).build();
        assertEquals(id, uAttributes.id());
        assertEquals(UMessageType.RESPONSE, uAttributes.type());
        assertEquals(UPriority.REALTIME_INTERACTIVE, uAttributes.priority());
        assertTrue(uAttributes.plevel().isEmpty());
    }

    @Test
    @DisplayName("Test creating UAttribues with a null communication status")
    public void test_create_uattributes_with_null_comm_status() {
        final UUID id = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final UAttributes uAttributes = new UAttributes.UAttributesBuilder(id, UMessageType.RESPONSE, UPriority.REALTIME_INTERACTIVE).withCommStatus(null).build();
        assertEquals(id, uAttributes.id());
        assertEquals(UMessageType.RESPONSE, uAttributes.type());
        assertEquals(UPriority.REALTIME_INTERACTIVE, uAttributes.priority());
        assertTrue(uAttributes.commstatus().isEmpty());
    }

    @Test
    @DisplayName("Test creating UAttribues with a null request id")
    public void test_create_uattributes_with_null_request_id() {
        final UUID id = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final UAttributes uAttributes = new UAttributes.UAttributesBuilder(id, UMessageType.RESPONSE, UPriority.REALTIME_INTERACTIVE).withReqId(null).build();
        assertEquals(id, uAttributes.id());
        assertEquals(UMessageType.RESPONSE, uAttributes.type());
        assertEquals(UPriority.REALTIME_INTERACTIVE, uAttributes.priority());
        assertTrue(uAttributes.commstatus().isEmpty());
    }

    @Test
    @DisplayName("Test is this UAttributes configured for an RPC request payload")
    public void test_is_uattributes_configured_for_rpc_request_payload() {
        final UUID id = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final UUri sink = UUri.newBuilder().setEntity(UEntity.newBuilder().setName("body.access")).setResource(UResourceBuilder.forRpcRequest("ExecuteWindowCommand")).build();

        final UAttributes uAttributes = new UAttributes.UAttributesBuilder(id, UMessageType.REQUEST, UPriority.REALTIME_INTERACTIVE).withSink(sink).build();
        assertTrue(uAttributes.isRpcRequest());
    }

    @Test
    @DisplayName("Test scenarios for UAttributes not configured for an RPC request payload")
    public void test_scenarios_for_uattributes_not_configured_for_rpc_request_payload() {
        final UUID id = UUIDFactory.Factories.UPROTOCOL.factory().create();

        final UUri sink = UUri.newBuilder().setAuthority(UAuthority.newBuilder().setName("someVin.veh.ultifi.gm.com")).setEntity(UEntity.newBuilder().setName("body.access")).setResource(UResourceBuilder.forRpcRequest("ExecuteWindowCommand")).build();

        final UAttributes uAttributesNoSink = new UAttributes.UAttributesBuilder(id, UMessageType.REQUEST, UPriority.REALTIME_INTERACTIVE).build();
        assertFalse(uAttributesNoSink.isRpcRequest());

        final UAttributes uAttributesWrongType = new UAttributes.UAttributesBuilder(id, UMessageType.PUBLISH, UPriority.REALTIME_INTERACTIVE).withSink(sink).build();
        assertFalse(uAttributesWrongType.isRpcRequest());
    }

    @Test
    @DisplayName("Test is this UAttributes configured for an RPC response payload")
    public void test_is_uattributes_configured_for_rpc_response_payload() {
        final UUID id = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final UUID requestId = UUID.randomUUID();

        final UUri sink = UUri.newBuilder().setAuthority(UAuthority.newBuilder().setName("azure.bo.ultifi.gm.com")).setEntity(UEntity.newBuilder().setName("petapp.ultifi.gm.com").setVersionMajor(1)).build();

        final UAttributes uAttributes = new UAttributes.UAttributesBuilder(id, UMessageType.RESPONSE, UPriority.REALTIME_INTERACTIVE).withSink(sink).withReqId(requestId).build();
        assertTrue(uAttributes.isRpcResponse());
    }

    @Test
    @DisplayName("Test scenarios for UAttributes not configured for an RPC response payload")
    public void test_scenarios_for_uattributes_not_configured_for_rpc_response_payload() {
        final UUID id = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final UUID requestId = UUID.randomUUID();
        final UUri sink = UUri.newBuilder().setAuthority(UAuthority.newBuilder().setName("azure.bo.ultifi.gm.com")).setEntity(UEntity.newBuilder().setName("petapp.ultifi.gm.com").setVersionMajor(1)).build();

        final UAttributes uAttributesNoSink = new UAttributes.UAttributesBuilder(id, UMessageType.RESPONSE, UPriority.REALTIME_INTERACTIVE).withReqId(requestId).build();
        assertFalse(uAttributesNoSink.isRpcResponse());

        final UAttributes uAttributesWrongType = new UAttributes.UAttributesBuilder(id, UMessageType.PUBLISH, UPriority.REALTIME_INTERACTIVE).withSink(sink).withReqId(requestId).build();
        assertFalse(uAttributesWrongType.isRpcResponse());

        final UAttributes uAttributesNoRequestId = new UAttributes.UAttributesBuilder(id, UMessageType.RESPONSE, UPriority.REALTIME_INTERACTIVE).withSink(sink).build();
        assertFalse(uAttributesNoRequestId.isRpcResponse());

        final UAttributes simplePublish = new UAttributes.UAttributesBuilder(id, UMessageType.PUBLISH, UPriority.REALTIME_INTERACTIVE).build();
        assertFalse(simplePublish.isRpcResponse());
    }

    @Test
    @DisplayName("Test is this UAttributes configured for payload where there was no platform error")
    public void test_is_uattributes_configured_for_payload_with_no_platform_error() {
        final UUID id = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final UAttributes uAttributes = new UAttributes.UAttributesBuilder(id, UMessageType.PUBLISH, UPriority.REALTIME_INTERACTIVE).build();
        assertTrue(uAttributes.isPlatformTransportSuccess());

        final UAttributes alsoOK = new UAttributes.UAttributesBuilder(id, UMessageType.PUBLISH, UPriority.REALTIME_INTERACTIVE).withCommStatus(0).build();
        assertTrue(alsoOK.isPlatformTransportSuccess());
    }

    @Test
    @DisplayName("Test is this UAttributes configured for payload where there was a platform error")
    public void test_is_uattributes_configured_for_payload_with_platform_error() {
        final UUID id = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final UAttributes uAttributes = new UAttributes.UAttributesBuilder(id, UMessageType.PUBLISH, UPriority.REALTIME_INTERACTIVE).withCommStatus(3).build();
        assertFalse(uAttributes.isPlatformTransportSuccess());
    }


}