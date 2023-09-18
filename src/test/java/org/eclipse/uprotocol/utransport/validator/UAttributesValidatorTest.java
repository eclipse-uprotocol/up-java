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

package org.eclipse.uprotocol.utransport.validator;

import org.eclipse.uprotocol.uri.datamodel.UAuthority;
import org.eclipse.uprotocol.uri.datamodel.UEntity;
import org.eclipse.uprotocol.uri.datamodel.UResource;
import org.eclipse.uprotocol.uri.datamodel.UUri;
import org.eclipse.uprotocol.uri.serializer.UriSerializer;
import org.eclipse.uprotocol.utransport.datamodel.UAttributes;
import org.eclipse.uprotocol.utransport.datamodel.UAttributes.UAttributesBuilder;
import org.eclipse.uprotocol.utransport.datamodel.UMessageType;
import org.eclipse.uprotocol.utransport.datamodel.UPriority;
import org.eclipse.uprotocol.utransport.datamodel.UStatus;
import org.eclipse.uprotocol.utransport.datamodel.UStatus.Code;
import org.eclipse.uprotocol.utransport.validate.UAttributesValidator;
import org.eclipse.uprotocol.uuid.factory.UUIDFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class UAttributesValidatorTest {

    @Test
    @DisplayName("test fetching validator for valid types")
    public void test_fetching_validator_for_valid_types() {

        UAttributesValidator publish = UAttributesValidator.getValidator(
                new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW).build());
        assertEquals("UAttributesValidator.Publish", publish.toString());

        UAttributesValidator request = UAttributesValidator.getValidator(
                new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                        UMessageType.REQUEST, UPriority.REALTIME_INTERACTIVE).build());
        assertEquals("UAttributesValidator.Request", request.toString());

        UAttributesValidator response = UAttributesValidator.getValidator(
                new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                        UMessageType.RESPONSE, UPriority.REALTIME_INTERACTIVE).build());
        assertEquals("UAttributesValidator.Response", response.toString());
    }

    @Test
    @DisplayName("test fetching validator when message type is null")
    public void test_fetching_validator_when_message_type_is_null() {

        UAttributesValidator publish = UAttributesValidator.getValidator(
                new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                        null, UPriority.LOW).build());
        assertEquals("UAttributesValidator.Publish", publish.toString());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published")
    public void test_validate_uAttributes_for_publish_message_payload() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isSuccess());
        assertEquals("ok", status.msg());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published with all values")
    public void test_validate_uAttributes_for_publish_message_payload_all_values() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW)
                .withTtl(1000)
                .withSink(new UUri(UAuthority.local(), UEntity.fromName("body.access"), new UResource("door", "front_left", "Door")))
                .withPermissionLevel(2)
                .withCommStatus(3)
                .withReqId(UUIDFactory.Factories.UPROTOCOL.factory().create())
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isSuccess());
        assertEquals("ok", status.msg());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published with invalid id")
    public void test_validate_uAttributes_for_publish_message_payload_invalid_id() {
        final UAttributes attributes = new UAttributesBuilder(null,
                UMessageType.PUBLISH, UPriority.LOW).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isFailed());
        assertTrue(status.msg().contains("Invalid UUID [null]"));
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published with invalid type")
    public void test_validate_uAttributes_for_publish_message_payload_invalid_type() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.RESPONSE, UPriority.LOW).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isFailed());
        assertEquals("Wrong Attribute Type [RESPONSE]", status.msg());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published with invalid priority")
    public void test_validate_uAttributes_for_publish_message_payload_invalid_priority() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, null).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isFailed());
        assertEquals("Priority is missing", status.msg());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published with invalid time to live")
    public void test_validate_uAttributes_for_publish_message_payload_invalid_ttl() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW)
                .withTtl(-1)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isFailed());
        assertEquals("Invalid TTL [-1]", status.msg());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published with invalid sink")
    public void test_validate_uAttributes_for_publish_message_payload_invalid_sink() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW)
                .withSink(new UUri(UAuthority.local(), UEntity.empty(), UResource.empty()))
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isFailed());
        assertEquals("UriPart is empty.", status.msg());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published with invalid permission level")
    public void test_validate_uAttributes_for_publish_message_payload_invalid_permission_level() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW)
                .withPermissionLevel(-42)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isFailed());
        assertEquals("Invalid Permission Level", status.msg());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published with invalid communication status")
    public void test_validate_uAttributes_for_publish_message_payload_invalid_communication_status() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW)
                .withCommStatus(-42)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isFailed());
        assertEquals("Invalid Communication Status Code", status.msg());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published with invalid request id")
    public void test_validate_uAttributes_for_publish_message_payload_invalid_request_id() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW)
                .withReqId(UUID.randomUUID())
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isFailed());
        assertEquals("Invalid UUID", status.msg());
    }

    // ----

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC request")
    public void test_validate_uAttributes_for_rpc_request_message_payload() {
        final UUri sink = new UUri(UAuthority.longRemote("vcu", String.format("%s.veh.ultifi.gm.com", "someVin")),
                new UEntity("petapp.ultifi.gm.com",1), UResource.fromNameWithInstance("rpc", "response"));
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.REQUEST, UPriority.REALTIME_INTERACTIVE)
                .withSink(sink)
                .withTtl(1000)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.REQUEST.validator();
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isSuccess());
        assertEquals("ok", status.msg());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC request with all values")
    public void test_validate_uAttributes_for_rpc_request_message_payload_all_values() {
        final UUri sink = new UUri(UAuthority.longRemote("vcu", String.format("%s.veh.ultifi.gm.com", "someVin")),
                new UEntity("petapp.ultifi.gm.com",1), UResource.fromNameWithInstance("rpc", "response"));
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.REQUEST, UPriority.REALTIME_INTERACTIVE)
                .withSink(sink)
                .withTtl(1000)
                .withPermissionLevel(2)
                .withCommStatus(3)
                .withReqId(UUIDFactory.Factories.UPROTOCOL.factory().create())
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.REQUEST.validator();
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isSuccess());
        assertEquals("ok", status.msg());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC request with invalid id")
    public void test_validate_uAttributes_for_rpc_request_message_payload_invalid_id() {
        final UUri sink = new UUri(UAuthority.longRemote("vcu", String.format("%s.veh.ultifi.gm.com", "someVin")),
                new UEntity("petapp.ultifi.gm.com",1), UResource.fromNameWithInstance("rpc", "response"));
        final UAttributes attributes = new UAttributesBuilder(null,
                UMessageType.REQUEST, UPriority.REALTIME_INTERACTIVE)
                .withSink(sink)
                .withTtl(1000)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.REQUEST.validator();
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isFailed());
        assertTrue(status.msg().contains("Invalid UUID [null]"));
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC request with invalid type")
    public void test_validate_uAttributes_for_rpc_request_message_payload_invalid_type() {
        final UUri sink = new UUri(UAuthority.longRemote("vcu", String.format("%s.veh.ultifi.gm.com", "someVin")),
                new UEntity("petapp.ultifi.gm.com",1), UResource.fromNameWithInstance("rpc", "response"));
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.RESPONSE, UPriority.REALTIME_INTERACTIVE)
                .withSink(sink)
                .withTtl(1000)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.REQUEST.validator();
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isFailed());
        assertEquals("Wrong Attribute Type [RESPONSE]", status.msg());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC request with invalid priority")
    public void test_validate_uAttributes_for_rpc_request_message_payload_invalid_priority() {
        final UUri sink = new UUri(UAuthority.longRemote("vcu", String.format("%s.veh.ultifi.gm.com", "someVin")),
                new UEntity("petapp.ultifi.gm.com",1), UResource.fromNameWithInstance("rpc", "response"));
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.REQUEST, null)
                .withSink(sink)
                .withTtl(1000)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.REQUEST.validator();
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isFailed());
        assertEquals("Priority is missing", status.msg());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC request with missing time to live")
    public void test_validate_uAttributes_for_rpc_request_message_payload_missing_ttl() {
        final UUri sink = new UUri(UAuthority.longRemote("vcu", String.format("%s.veh.ultifi.gm.com", "someVin")),
                new UEntity("petapp.ultifi.gm.com",1), UResource.fromNameWithInstance("rpc", "response"));
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.REQUEST, UPriority.REALTIME_INTERACTIVE)
                .withSink(sink)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.REQUEST.validator();
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isFailed());
        assertEquals("Missing TTL", status.msg());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC request with invalid time to live")
    public void test_validate_uAttributes_for_rpc_request_message_payload_invalid_ttl() {
        final UUri sink = new UUri(UAuthority.longRemote("vcu", String.format("%s.veh.ultifi.gm.com", "someVin")),
                new UEntity("petapp.ultifi.gm.com",1), UResource.fromNameWithInstance("rpc", "response"));
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.REQUEST, UPriority.REALTIME_INTERACTIVE)
                .withSink(sink)
                .withTtl(-1)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.REQUEST.validator();
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isFailed());
        assertEquals("Invalid TTL [-1]", status.msg());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC request with missing sink and missing ttl")
    public void test_validate_uAttributes_for_rpc_request_message_payload_missing_sink_and_missing_ttl() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.REQUEST, UPriority.REALTIME_INTERACTIVE)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.REQUEST.validator();
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isFailed());
        assertEquals("Missing TTL,Missing Sink", status.msg());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC request with invalid sink")
    public void test_validate_uAttributes_for_rpc_request_message_payload_invalid_sink() {
        final UUri sink = new UUri(UAuthority.local(), UEntity.fromName("body.access"), UResource.forRpc("ExecuteWindowCommand"));
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.REQUEST, UPriority.REALTIME_INTERACTIVE)
                .withSink(sink)
                .withTtl(1000)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.REQUEST.validator();
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isFailed());
        assertEquals("Invalid RPC response type.", status.msg());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC request with invalid permission level")
    public void test_validate_uAttributes_for_rpc_request_message_payload_invalid_permission_level() {
        final UUri sink = new UUri(UAuthority.longRemote("vcu", String.format("%s.veh.ultifi.gm.com", "someVin")),
                new UEntity("petapp.ultifi.gm.com",1), UResource.fromNameWithInstance("rpc", "response"));
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.REQUEST, UPriority.REALTIME_INTERACTIVE)
                .withSink(sink)
                .withTtl(1000)
                .withPermissionLevel(-42)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.REQUEST.validator();
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isFailed());
        assertEquals("Invalid Permission Level", status.msg());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC request with invalid communication status")
    public void test_validate_uAttributes_for_rpc_request_message_payload_invalid_communication_status() {
        final UUri sink = new UUri(UAuthority.longRemote("vcu", String.format("%s.veh.ultifi.gm.com", "someVin")),
                new UEntity("petapp.ultifi.gm.com",1), UResource.fromNameWithInstance("rpc", "response"));
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.REQUEST, UPriority.REALTIME_INTERACTIVE)
                .withSink(sink)
                .withTtl(1000)
                .withCommStatus(-42)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.REQUEST.validator();
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isFailed());
        assertEquals("Invalid Communication Status Code", status.msg());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC request with invalid request id")
    public void test_validate_uAttributes_for_rpc_request_message_payload_invalid_request_id() {
        final UUri sink = new UUri(UAuthority.longRemote("vcu", String.format("%s.veh.ultifi.gm.com", "someVin")),
                new UEntity("petapp.ultifi.gm.com",1), UResource.fromNameWithInstance("rpc", "response"));
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.REQUEST, UPriority.REALTIME_INTERACTIVE)
                .withSink(sink)
                .withTtl(1000)
                .withReqId(UUID.randomUUID())
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.REQUEST.validator();
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isFailed());
        assertEquals("Invalid UUID", status.msg());
    }

    // ----

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC response")
    public void test_validate_uAttributes_for_rpc_response_message_payload() {
        final UUri sink = new UUri(UAuthority.longRemote("vcu", String.format("%s.veh.ultifi.gm.com", "someVin")),
                new UEntity("petapp.ultifi.gm.com",1), UResource.fromNameWithInstance("rpc", "response"));
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.RESPONSE, UPriority.REALTIME_INTERACTIVE)
                .withSink(sink)
                .withReqId(UUIDFactory.Factories.UPROTOCOL.factory().create())
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.RESPONSE.validator();
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isSuccess());
        assertEquals("ok", status.msg());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC response with all values")
    public void test_validate_uAttributes_for_rpc_response_message_payload_all_values() {
        final UUri sink = new UUri(UAuthority.longRemote("vcu", String.format("%s.veh.ultifi.gm.com", "someVin")),
                new UEntity("petapp.ultifi.gm.com",1), UResource.fromNameWithInstance("rpc", "response"));
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.RESPONSE, UPriority.REALTIME_INTERACTIVE)
                .withSink(sink)
                .withReqId(UUIDFactory.Factories.UPROTOCOL.factory().create())
                .withPermissionLevel(2)
                .withCommStatus(3)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.RESPONSE.validator();
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isSuccess());
        assertEquals("ok", status.msg());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC response with invalid id")
    public void test_validate_uAttributes_for_rpc_response_message_payload_invalid_id() {
        final UUri sink = new UUri(UAuthority.longRemote("vcu", String.format("%s.veh.ultifi.gm.com", "someVin")),
                new UEntity("petapp.ultifi.gm.com",1), UResource.fromNameWithInstance("rpc", "response"));
        final UAttributes attributes = new UAttributesBuilder(null,
                UMessageType.RESPONSE, UPriority.REALTIME_INTERACTIVE)
                .withSink(sink)
                .withReqId(UUIDFactory.Factories.UPROTOCOL.factory().create())
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.RESPONSE.validator();
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isFailed());
        assertTrue(status.msg().contains("Invalid UUID [null]"));
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC response with invalid type")
    public void test_validate_uAttributes_for_rpc_response_message_payload_invalid_type() {
        final UUri sink = new UUri(UAuthority.longRemote("vcu", String.format("%s.veh.ultifi.gm.com", "someVin")),
                new UEntity("petapp.ultifi.gm.com",1), UResource.fromNameWithInstance("rpc", "response"));
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.REALTIME_INTERACTIVE)
                .withSink(sink)
                .withReqId(UUIDFactory.Factories.UPROTOCOL.factory().create())
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.RESPONSE.validator();
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isFailed());
        assertEquals("Wrong Attribute Type [PUBLISH]", status.msg());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC response with invalid priority")
    public void test_validate_uAttributes_for_rpc_response_message_payload_invalid_priority() {
        final UUri sink = new UUri(UAuthority.longRemote("vcu", String.format("%s.veh.ultifi.gm.com", "someVin")),
                new UEntity("petapp.ultifi.gm.com",1), UResource.fromNameWithInstance("rpc", "response"));
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.RESPONSE, null)
                .withSink(sink)
                .withReqId(UUIDFactory.Factories.UPROTOCOL.factory().create())
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.RESPONSE.validator();
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isFailed());
        assertEquals("Priority is missing", status.msg());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC response with invalid time to live")
    public void test_validate_uAttributes_for_rpc_response_message_payload_invalid_ttl() {
        final UUri sink = new UUri(UAuthority.longRemote("vcu", String.format("%s.veh.ultifi.gm.com", "someVin")),
                new UEntity("petapp.ultifi.gm.com",1), UResource.fromNameWithInstance("rpc", "response"));
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.RESPONSE, UPriority.REALTIME_INTERACTIVE)
                .withSink(sink)
                .withReqId(UUIDFactory.Factories.UPROTOCOL.factory().create())
                .withTtl(-1)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.RESPONSE.validator();
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isFailed());
        assertEquals("Invalid TTL [-1]", status.msg());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC response with missing sink and missing request id")
    public void test_validate_uAttributes_for_rpc_response_message_payload_missing_sink_and_missing_requestId() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.RESPONSE, UPriority.REALTIME_INTERACTIVE)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.RESPONSE.validator();
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isFailed());
        assertEquals("Missing Sink,Missing correlationId", status.msg());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC response with invalid sink")
    public void test_validate_uAttributes_for_rpc_response_message_payload_invalid_sink() {
        final UUri sink = new UUri(UAuthority.local(), UEntity.empty(), UResource.forRpc("ExecuteWindowCommand"));
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.RESPONSE, UPriority.REALTIME_INTERACTIVE)
                .withSink(sink)
                .withReqId(UUIDFactory.Factories.UPROTOCOL.factory().create())
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.RESPONSE.validator();
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isFailed());
        assertEquals("UriPart is missing uSoftware Entity name.", status.msg());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC response with invalid permission level")
    public void test_validate_uAttributes_for_rpc_response_message_payload_invalid_permission_level() {
        final UUri sink = new UUri(UAuthority.longRemote("vcu", String.format("%s.veh.ultifi.gm.com", "someVin")),
                new UEntity("petapp.ultifi.gm.com",1), UResource.fromNameWithInstance("rpc", "response"));
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.RESPONSE, UPriority.REALTIME_INTERACTIVE)
                .withSink(sink)
                .withReqId(UUIDFactory.Factories.UPROTOCOL.factory().create())
                .withPermissionLevel(-42)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.RESPONSE.validator();
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isFailed());
        assertEquals("Invalid Permission Level", status.msg());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC response with invalid communication status")
    public void test_validate_uAttributes_for_rpc_response_message_payload_invalid_communication_status() {
        final UUri sink = new UUri(UAuthority.longRemote("vcu", String.format("%s.veh.ultifi.gm.com", "someVin")),
                new UEntity("petapp.ultifi.gm.com",1), UResource.fromNameWithInstance("rpc", "response"));
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.RESPONSE, UPriority.REALTIME_INTERACTIVE)
                .withSink(sink)
                .withReqId(UUIDFactory.Factories.UPROTOCOL.factory().create())
                .withCommStatus(-42)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.RESPONSE.validator();
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isFailed());
        assertEquals("Invalid Communication Status Code", status.msg());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC response with missing request id")
    public void test_validate_uAttributes_for_rpc_response_message_payload_missing_request_id() {
        final UUri sink = new UUri(UAuthority.longRemote("vcu", String.format("%s.veh.ultifi.gm.com", "someVin")),
                new UEntity("petapp.ultifi.gm.com",1), UResource.fromNameWithInstance("rpc", "response"));
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.RESPONSE, UPriority.REALTIME_INTERACTIVE)
                .withSink(sink)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.RESPONSE.validator();
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isFailed());
        assertEquals("Missing correlationId", status.msg());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC response with invalid request id")
    public void test_validate_uAttributes_for_rpc_response_message_payload_invalid_request_id() {
        final UUri sink = new UUri(UAuthority.longRemote("vcu", String.format("%s.veh.ultifi.gm.com", "someVin")),
                new UEntity("petapp.ultifi.gm.com",1), UResource.fromNameWithInstance("rpc", "response"));
        final UUID reqid = UUID.randomUUID();
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.RESPONSE, UPriority.REALTIME_INTERACTIVE)
                .withSink(sink)
                .withReqId(reqid)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.RESPONSE.validator();
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isFailed());
        assertEquals(String.format("Invalid correlationId [%s]", reqid), status.msg());
    }

    // ----

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published not expired")
    public void test_validate_uAttributes_for_publish_message_payload_not_expired() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final UStatus status = validator.isExpired(attributes);
        assertTrue(status.isSuccess());
        assertEquals("Not Expired", status.msg());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published not expired with ttl zero")
    public void test_validate_uAttributes_for_publish_message_payload_not_expired_with_ttl_zero() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW)
                .withTtl(0)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final UStatus status = validator.isExpired(attributes);
        assertTrue(status.isSuccess());
        assertEquals("Not Expired", status.msg());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published not expired with ttl")
    public void test_validate_uAttributes_for_publish_message_payload_not_expired_with_ttl() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW)
                .withTtl(10000)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final UStatus status = validator.isExpired(attributes);
        assertTrue(status.isSuccess());
        assertEquals("Not Expired", status.msg());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published expired with ttl")
    public void test_validate_uAttributes_for_publish_message_payload_expired_with_ttl() throws InterruptedException {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW)
                .withTtl(1)
                .build();

        Thread.sleep(800);

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final UStatus status = validator.isExpired(attributes);
        assertTrue(status.isFailed());
        assertEquals("Payload is expired", status.msg());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published not expired cant calculate bad UUID")
    public void test_validate_uAttributes_for_publish_message_payload_not_expired_cant_calculate_bad_uuid() {
        final UAttributes attributes = new UAttributesBuilder(UUID.randomUUID(),
                UMessageType.PUBLISH, UPriority.LOW)
                .withTtl(10000)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final UStatus status = validator.isExpired(attributes);
        assertTrue(status.isSuccess());
        assertEquals("Not Expired", status.msg());
    }

    // ----

    @Test
    @DisplayName("test validating publish invalid ttl attribute")
    public void test_validating_publish_invalid_ttl_attribute() {

        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW)
                .withTtl(-1).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final UStatus status = validator.validateTtl(attributes);
        assertTrue(status.isFailed());
        assertEquals(status.getCode(), Code.INVALID_ARGUMENT.value());
        assertEquals("Invalid TTL [-1]", status.msg());
    }

    @Test
    @DisplayName("test validating publish valid ttl attribute")
    public void test_validating_valid_ttl_attribute() {

        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW)
                .withTtl(100).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final UStatus status = validator.validateTtl(attributes);
        assertEquals(UStatus.ok(), status);
    }

    @Test
    @DisplayName("test validating invalid id attribute")
    public void test_validating_invalid_id_attribute() {

        final UAttributes attributes = new UAttributesBuilder(null,
                UMessageType.PUBLISH, UPriority.LOW).build();

        final UAttributes attributes1 = new UAttributesBuilder(UUID.randomUUID(),
                UMessageType.PUBLISH, UPriority.LOW).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        UStatus status = validator.validateId(attributes);
        assertTrue(status.isFailed());
        assertEquals(status.getCode(), Code.INVALID_ARGUMENT.value());
        assertTrue(status.msg().contains("Invalid UUID [null]"));

        UStatus status1 = validator.validateId(attributes1);
        assertTrue(status1.isFailed());
        assertEquals(status1.getCode(), Code.INVALID_ARGUMENT.value());
        assertTrue(status.msg().contains("Invalid UUID [null]"));
    }

    @Test
    @DisplayName("test validating valid id attribute")
    public void test_validating_valid_id_attribute() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final UStatus status = validator.validateId(attributes);
        assertEquals(UStatus.ok(), status);
    }

    @Test
    @DisplayName("test validating invalid sink attribute")
    public void test_validating_invalid_sink_attribute() {
        final UUri uri = UriSerializer.LONG.deserialize("//");
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW).withSink(uri).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final UStatus status = validator.validateSink(attributes);

        assertTrue(status.isFailed());
        assertEquals(status.getCode(), Code.INVALID_ARGUMENT.value());
        assertEquals("UriPart is empty.", status.msg());
    }

    @Test
    @DisplayName("test validating valid sink attribute")
    public void test_validating_valid_sink_attribute() {
        final UUri uri = UriSerializer.LONG.deserialize("/haartley/1");
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW).withSink(uri).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final UStatus status = validator.validateSink(attributes);
        assertEquals(UStatus.ok(), status);
    }

    @Test
    @DisplayName("test validating invalid ReqId attribute")
    public void test_validating_invalid_ReqId_attribute() {

        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW).withReqId(UUID.randomUUID()).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final UStatus status = validator.validateReqId(attributes);
        assertTrue(status.isFailed());
        assertEquals(status.getCode(), Code.INVALID_ARGUMENT.value());
        assertEquals("Invalid UUID", status.msg());
    }

    @Test
    @DisplayName("test validating valid ReqId attribute")
    public void test_validating_valid_ReqId_attribute() {

        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW)
                .withReqId(UUIDFactory.Factories.UPROTOCOL.factory().create())
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final UStatus status = validator.validateReqId(attributes);
        assertEquals(UStatus.ok(), status);
    }


    @Test
    @DisplayName("test validating invalid PermissionLevel attribute")
    public void test_validating_invalid_PermissionLevel_attribute() {

        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW)
                .withPermissionLevel(-1)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final UStatus status = validator.validatePermissionLevel(attributes);
        assertTrue(status.isFailed());
        assertEquals(status.getCode(), Code.INVALID_ARGUMENT.value());
        assertEquals("Invalid Permission Level", status.msg());
    }

    @Test
    @DisplayName("test validating valid PermissionLevel attribute")
    public void test_validating_valid_PermissionLevel_attribute() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW)
                .withPermissionLevel(3)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final UStatus status = validator.validatePermissionLevel(attributes);
        assertEquals(UStatus.ok(), status);
    }

    @Test
    @DisplayName("test validating valid PermissionLevel attribute")
    public void test_validating_valid_PermissionLevel_attribute_invalid() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW)
                .withPermissionLevel(0)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final UStatus status = validator.validatePermissionLevel(attributes);
        assertTrue(status.isFailed());
        assertEquals("Invalid Permission Level", status.msg());
        assertEquals(3, status.getCode());
    }

    @Test
    @DisplayName("test validating invalid commstatus attribute")
    public void test_validating_invalid_commstatus_attribute() {

        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW)
                .withCommStatus(100)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final UStatus status = validator.validateCommStatus(attributes);
        assertTrue(status.isFailed());
        assertEquals(status.getCode(), Code.INVALID_ARGUMENT.value());
        assertEquals( "Invalid Communication Status Code", status.msg());
    }

    @Test
    @DisplayName("test validating valid commstatus attribute")
    public void test_validating_valid_commstatus_attribute() {

        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW)
                .withCommStatus(Code.ABORTED.value())
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final UStatus status = validator.validateCommStatus(attributes);
        assertEquals(UStatus.ok(), status);
    }


    @Test
    @DisplayName("test validating request message types")
    public void test_validating_request_message_types() {
        final UUri sink = UriSerializer.LONG.deserialize("/hartley/1/rpc.response");

        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.REQUEST, UPriority.NETWORK_CONTROL)
                .withSink(sink)
                .withTtl(100)
                .build();

        final UAttributesValidator validator = UAttributesValidator.getValidator(attributes);
        assertEquals("UAttributesValidator.Request", validator.toString());
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isSuccess());
        assertEquals("ok", status.msg());
    }

    @Test
    @DisplayName("test validating request validator using wrong messagetype")
    public void test_validating_request_validator_with_wrong_messagetype() {

        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.NETWORK_CONTROL)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.REQUEST.validator();
        assertEquals("UAttributesValidator.Request", validator.toString());
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isFailed());
        assertEquals(status.getCode(), Code.INVALID_ARGUMENT.value());
        assertEquals("Wrong Attribute Type [PUBLISH],Missing TTL,Missing Sink", status.msg());
    }

    @Test
    @DisplayName("test validating request validator using bad ttl")
    public void test_validating_request_validator_with_wrong_bad_ttl() {

        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.REQUEST, UPriority.NETWORK_CONTROL)
                .withSink(UriSerializer.LONG.deserialize("/hartley/1/rpc.response"))
                .withTtl(-1)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.REQUEST.validator();
        assertEquals("UAttributesValidator.Request", validator.toString());
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isFailed());
        assertEquals(status.getCode(), Code.INVALID_ARGUMENT.value());
        assertEquals("Invalid TTL [-1]", status.msg());
    }

    @Test
    @DisplayName("test validating response validator using bad ttl")
    public void test_validating_response_validator_with_wrong_bad_ttl() {

        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.RESPONSE, UPriority.NETWORK_CONTROL)
                .withSink(UriSerializer.LONG.deserialize("/hartley/1/rpc.response"))
                .withTtl(-1)
                .withReqId(UUIDFactory.Factories.UPROTOCOL.factory().create())
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.RESPONSE.validator();
        assertEquals("UAttributesValidator.Response", validator.toString());
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isFailed());
        assertEquals(status.getCode(), Code.INVALID_ARGUMENT.value());
        assertEquals("Invalid TTL [-1]", status.msg());
    }

    @Test
    @DisplayName("test validating response validator using bad UUID")
    public void test_validating_response_validator_with_bad_reqid() {

        final UUID id = UUID.randomUUID();
        final UAttributes attributes = new UAttributesBuilder(id,
                UMessageType.RESPONSE, UPriority.NETWORK_CONTROL)
                .withSink(UriSerializer.LONG.deserialize("/hartley/1/rpc.response"))
                .withTtl(100)
                .withReqId(UUIDFactory.Factories.UPROTOCOL.factory().create())
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.RESPONSE.validator();
        assertEquals("UAttributesValidator.Response", validator.toString());
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isFailed());
        assertEquals(status.getCode(), Code.INVALID_ARGUMENT.value());
        assertEquals(String.format("Invalid UUID [%s]", id), status.msg());
    }


    @Test
    @DisplayName("test validating publish validator with wrong messagetype")
    public void test_validating_publish_validator_with_wrong_messagetype() {

        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.REQUEST, UPriority.NETWORK_CONTROL)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        assertEquals("UAttributesValidator.Publish", validator.toString());
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isFailed());
        assertEquals(status.getCode(), Code.INVALID_ARGUMENT.value());
        assertEquals("Wrong Attribute Type [REQUEST]", status.msg());
    }

    @Test
    @DisplayName("test validating response validator with wrong messagetype")
    public void test_validating_response_validator_with_wrong_messagetype() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.NETWORK_CONTROL)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.RESPONSE.validator();
        assertEquals("UAttributesValidator.Response", validator.toString());
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isFailed());
        assertEquals(status.getCode(), Code.INVALID_ARGUMENT.value());
        assertEquals("Wrong Attribute Type [PUBLISH],Missing Sink,Missing correlationId", status.msg());
    }


    @Test
    @DisplayName("test validating request containing token")
    public void test_validating_request_containing_token() {

        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW)
                .withToken("null")
                .build();

        final UAttributesValidator validator = UAttributesValidator.getValidator(attributes);
        assertEquals("UAttributesValidator.Publish", validator.toString());
        final UStatus status = validator.validate(attributes);
        assertEquals(UStatus.ok(), status);
    }
}
