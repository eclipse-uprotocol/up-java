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

package org.eclipse.uprotocol.transport.validator;

import org.eclipse.uprotocol.transport.datamodel.UAttributes;
import org.eclipse.uprotocol.transport.datamodel.UMessageType;
import org.eclipse.uprotocol.transport.datamodel.UPriority;
import org.eclipse.uprotocol.transport.datamodel.UAttributes.UAttributesBuilder;
import org.eclipse.uprotocol.transport.datamodel.UStatus.Code;
import org.eclipse.uprotocol.transport.validate.UAttributesValidator;
import org.eclipse.uprotocol.uri.builder.UResourceBuilder;
import org.eclipse.uprotocol.uri.serializer.LongUriSerializer;
import org.eclipse.uprotocol.uuid.factory.UUIDFactory;
import org.eclipse.uprotocol.v1.UAuthority;
import org.eclipse.uprotocol.v1.UEntity;
import org.eclipse.uprotocol.v1.UUri;
import org.eclipse.uprotocol.validation.ValidationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isSuccess());
        assertEquals("", status.getMessage());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published with all values")
    public void test_validate_uAttributes_for_publish_message_payload_all_values() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW)
                .withTtl(1000)
                .withSink(buildSink())
                .withPermissionLevel(2)
                .withCommStatus(3)
                .withReqId(UUIDFactory.Factories.UPROTOCOL.factory().create())
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isSuccess());
        assertEquals("", status.getMessage());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published with invalid id")
    public void test_validate_uAttributes_for_publish_message_payload_invalid_id() {
        final UAttributes attributes = new UAttributesBuilder(null,
                UMessageType.PUBLISH, UPriority.LOW).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertTrue(status.getMessage().contains("Invalid UUID [null]"));
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published with invalid type")
    public void test_validate_uAttributes_for_publish_message_payload_invalid_type() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.RESPONSE, UPriority.LOW).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Wrong Attribute Type [RESPONSE]", status.getMessage());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published with invalid priority")
    public void test_validate_uAttributes_for_publish_message_payload_invalid_priority() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, null).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Priority is missing", status.getMessage());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published with invalid time to live")
    public void test_validate_uAttributes_for_publish_message_payload_invalid_ttl() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW)
                .withTtl(-1)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Invalid TTL [-1]", status.getMessage());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published with invalid sink")
    public void test_validate_uAttributes_for_publish_message_payload_invalid_sink() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW)
                .withSink(UUri.getDefaultInstance())
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Uri is empty.", status.getMessage());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published with invalid permission level")
    public void test_validate_uAttributes_for_publish_message_payload_invalid_permission_level() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW)
                .withPermissionLevel(-42)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Invalid Permission Level", status.getMessage());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published with invalid communication status")
    public void test_validate_uAttributes_for_publish_message_payload_invalid_communication_status() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW)
                .withCommStatus(-42)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Invalid Communication Status Code", status.getMessage());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published with invalid request id")
    public void test_validate_uAttributes_for_publish_message_payload_invalid_request_id() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW)
                .withReqId(UUID.randomUUID())
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Invalid UUID", status.getMessage());
    }

    // ----

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC request")
    public void test_validate_uAttributes_for_rpc_request_message_payload() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.REQUEST, UPriority.REALTIME_INTERACTIVE)
                .withSink(buildSink())
                .withTtl(1000)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.REQUEST.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isSuccess());
        assertEquals("", status.getMessage());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC request with all values")
    public void test_validate_uAttributes_for_rpc_request_message_payload_all_values() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.REQUEST, UPriority.REALTIME_INTERACTIVE)
                .withSink(buildSink())
                .withTtl(1000)
                .withPermissionLevel(2)
                .withCommStatus(3)
                .withReqId(UUIDFactory.Factories.UPROTOCOL.factory().create())
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.REQUEST.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isSuccess());
        assertEquals("", status.getMessage());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC request with invalid id")
    public void test_validate_uAttributes_for_rpc_request_message_payload_invalid_id() {

        final UAttributes attributes = new UAttributesBuilder(null,
                UMessageType.REQUEST, UPriority.REALTIME_INTERACTIVE)
                .withSink(buildSink())
                .withTtl(1000)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.REQUEST.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertTrue(status.getMessage().contains("Invalid UUID [null]"));
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC request with invalid type")
    public void test_validate_uAttributes_for_rpc_request_message_payload_invalid_type() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.RESPONSE, UPriority.REALTIME_INTERACTIVE)
                .withSink(buildSink())
                .withTtl(1000)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.REQUEST.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Wrong Attribute Type [RESPONSE]", status.getMessage());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC request with invalid priority")
    public void test_validate_uAttributes_for_rpc_request_message_payload_invalid_priority() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.REQUEST, null)
                .withSink(buildSink())
                .withTtl(1000)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.REQUEST.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Priority is missing", status.getMessage());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC request with missing time to live")
    public void test_validate_uAttributes_for_rpc_request_message_payload_missing_ttl() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.REQUEST, UPriority.REALTIME_INTERACTIVE)
                .withSink(buildSink())
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.REQUEST.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Missing TTL", status.getMessage());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC request with invalid time to live")
    public void test_validate_uAttributes_for_rpc_request_message_payload_invalid_ttl() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.REQUEST, UPriority.REALTIME_INTERACTIVE)
                .withSink(buildSink())
                .withTtl(-1)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.REQUEST.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Invalid TTL [-1]", status.getMessage());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC request with missing sink and missing ttl")
    public void test_validate_uAttributes_for_rpc_request_message_payload_missing_sink_and_missing_ttl() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.REQUEST, UPriority.REALTIME_INTERACTIVE)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.REQUEST.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Missing TTL,Missing Sink", status.getMessage());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC request with invalid sink")
    public void test_validate_uAttributes_for_rpc_request_message_payload_invalid_sink() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.REQUEST, UPriority.REALTIME_INTERACTIVE)
                .withSink(UUri.getDefaultInstance())
                .withTtl(1000)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.REQUEST.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Uri is empty.", status.getMessage());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC request with invalid permission level")
    public void test_validate_uAttributes_for_rpc_request_message_payload_invalid_permission_level() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.REQUEST, UPriority.REALTIME_INTERACTIVE)
                .withSink(buildSink())
                .withTtl(1000)
                .withPermissionLevel(-42)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.REQUEST.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Invalid Permission Level", status.getMessage());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC request with invalid communication status")
    public void test_validate_uAttributes_for_rpc_request_message_payload_invalid_communication_status() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.REQUEST, UPriority.REALTIME_INTERACTIVE)
                .withSink(buildSink())
                .withTtl(1000)
                .withCommStatus(-42)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.REQUEST.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Invalid Communication Status Code", status.getMessage());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC request with invalid request id")
    public void test_validate_uAttributes_for_rpc_request_message_payload_invalid_request_id() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.REQUEST, UPriority.REALTIME_INTERACTIVE)
                .withSink(buildSink())
                .withTtl(1000)
                .withReqId(UUID.randomUUID())
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.REQUEST.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Invalid UUID", status.getMessage());
    }

    // ----

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC response")
    public void test_validate_uAttributes_for_rpc_response_message_payload() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.RESPONSE, UPriority.REALTIME_INTERACTIVE)
                .withSink(buildSink())
                .withReqId(UUIDFactory.Factories.UPROTOCOL.factory().create())
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.RESPONSE.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isSuccess());
        assertEquals("", status.getMessage());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC response with all values")
    public void test_validate_uAttributes_for_rpc_response_message_payload_all_values() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.RESPONSE, UPriority.REALTIME_INTERACTIVE)
                .withSink(buildSink())
                .withReqId(UUIDFactory.Factories.UPROTOCOL.factory().create())
                .withPermissionLevel(2)
                .withCommStatus(3)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.RESPONSE.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isSuccess());
        assertEquals("", status.getMessage());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC response with invalid id")
    public void test_validate_uAttributes_for_rpc_response_message_payload_invalid_id() {
        final UAttributes attributes = new UAttributesBuilder(null,
                UMessageType.RESPONSE, UPriority.REALTIME_INTERACTIVE)
                .withSink(buildSink())
                .withReqId(UUIDFactory.Factories.UPROTOCOL.factory().create())
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.RESPONSE.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertTrue(status.getMessage().contains("Invalid UUID [null]"));
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC response with invalid type")
    public void test_validate_uAttributes_for_rpc_response_message_payload_invalid_type() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.REALTIME_INTERACTIVE)
                .withSink(buildSink())
                .withReqId(UUIDFactory.Factories.UPROTOCOL.factory().create())
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.RESPONSE.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Wrong Attribute Type [PUBLISH]", status.getMessage());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC response with invalid priority")
    public void test_validate_uAttributes_for_rpc_response_message_payload_invalid_priority() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.RESPONSE, null)
                .withSink(buildSink())
                .withReqId(UUIDFactory.Factories.UPROTOCOL.factory().create())
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.RESPONSE.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Priority is missing", status.getMessage());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC response with invalid time to live")
    public void test_validate_uAttributes_for_rpc_response_message_payload_invalid_ttl() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.RESPONSE, UPriority.REALTIME_INTERACTIVE)
                .withSink(buildSink())
                .withReqId(UUIDFactory.Factories.UPROTOCOL.factory().create())
                .withTtl(-1)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.RESPONSE.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Invalid TTL [-1]", status.getMessage());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC response with missing sink and missing request id")
    public void test_validate_uAttributes_for_rpc_response_message_payload_missing_sink_and_missing_requestId() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.RESPONSE, UPriority.REALTIME_INTERACTIVE)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.RESPONSE.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Missing Sink,Missing correlationId", status.getMessage());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC response with invalid sink")
    public void test_validate_uAttributes_for_rpc_response_message_payload_invalid_sink() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.RESPONSE, UPriority.REALTIME_INTERACTIVE)
                .withSink(null)
                .withReqId(UUIDFactory.Factories.UPROTOCOL.factory().create())
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.RESPONSE.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Missing Sink", status.getMessage());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC response with invalid permission level")
    public void test_validate_uAttributes_for_rpc_response_message_payload_invalid_permission_level() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.RESPONSE, UPriority.REALTIME_INTERACTIVE)
                .withSink(buildSink())
                .withReqId(UUIDFactory.Factories.UPROTOCOL.factory().create())
                .withPermissionLevel(-42)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.RESPONSE.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Invalid Permission Level", status.getMessage());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC response with invalid communication status")
    public void test_validate_uAttributes_for_rpc_response_message_payload_invalid_communication_status() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.RESPONSE, UPriority.REALTIME_INTERACTIVE)
                .withSink(buildSink())
                .withReqId(UUIDFactory.Factories.UPROTOCOL.factory().create())
                .withCommStatus(-42)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.RESPONSE.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Invalid Communication Status Code", status.getMessage());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC response with missing request id")
    public void test_validate_uAttributes_for_rpc_response_message_payload_missing_request_id() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.RESPONSE, UPriority.REALTIME_INTERACTIVE)
                .withSink(buildSink())
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.RESPONSE.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Missing correlationId", status.getMessage());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC response with invalid request id")
    public void test_validate_uAttributes_for_rpc_response_message_payload_invalid_request_id() {
        final UUID reqid = UUID.randomUUID();
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.RESPONSE, UPriority.REALTIME_INTERACTIVE)
                .withSink(buildSink())
                .withReqId(reqid)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.RESPONSE.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals(String.format("Invalid correlationId [%s]", reqid), status.getMessage());
    }

    // ----

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published not expired")
    public void test_validate_uAttributes_for_publish_message_payload_not_expired() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final ValidationResult status = validator.isExpired(attributes);
        assertTrue(status.isSuccess());
        assertEquals("", status.getMessage());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published not expired with ttl zero")
    public void test_validate_uAttributes_for_publish_message_payload_not_expired_with_ttl_zero() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW)
                .withTtl(0)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final ValidationResult status = validator.isExpired(attributes);
        assertTrue(status.isSuccess());
        assertEquals("", status.getMessage());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published not expired with ttl")
    public void test_validate_uAttributes_for_publish_message_payload_not_expired_with_ttl() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW)
                .withTtl(10000)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final ValidationResult status = validator.isExpired(attributes);
        assertTrue(status.isSuccess());
        assertEquals("", status.getMessage());
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
        final ValidationResult status = validator.isExpired(attributes);
        assertTrue(status.isFailure());
        assertEquals("Payload is expired", status.getMessage());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published not expired cant calculate bad UUID")
    public void test_validate_uAttributes_for_publish_message_payload_not_expired_cant_calculate_bad_uuid() {
        final UAttributes attributes = new UAttributesBuilder(UUID.randomUUID(),
                UMessageType.PUBLISH, UPriority.LOW)
                .withTtl(10000)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final ValidationResult status = validator.isExpired(attributes);
        assertFalse(status.isSuccess());
        assertEquals("Invalid Time", status.getMessage());
    }

    // ----

    @Test
    @DisplayName("test validating publish invalid ttl attribute")
    public void test_validating_publish_invalid_ttl_attribute() {

        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW)
                .withTtl(-1).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final ValidationResult status = validator.validateTtl(attributes);
        assertTrue(status.isFailure());
        assertEquals("Invalid TTL [-1]", status.getMessage());
    }

    @Test
    @DisplayName("test validating publish valid ttl attribute")
    public void test_validating_valid_ttl_attribute() {

        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW)
                .withTtl(100).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final ValidationResult status = validator.validateTtl(attributes);
        assertEquals(ValidationResult.success(), status);
    }

    @Test
    @DisplayName("test validating invalid id attribute")
    public void test_validating_invalid_id_attribute() {

        final UAttributes attributes = new UAttributesBuilder(null,
                UMessageType.PUBLISH, UPriority.LOW).build();

        final UAttributes attributes1 = new UAttributesBuilder(UUID.randomUUID(),
                UMessageType.PUBLISH, UPriority.LOW).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        ValidationResult status = validator.validateId(attributes);
        assertTrue(status.isFailure());
        assertTrue(status.getMessage().contains("Invalid UUID [null]"));

        ValidationResult status1 = validator.validateId(attributes1);
        assertTrue(status1.isFailure());
        assertTrue(status.getMessage().contains("Invalid UUID [null]"));
    }

    @Test
    @DisplayName("test validating valid id attribute")
    public void test_validating_valid_id_attribute() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final ValidationResult status = validator.validateId(attributes);
        assertEquals(ValidationResult.success(), status);
    }

    @Test
    @DisplayName("test validating invalid sink attribute")
    public void test_validating_invalid_sink_attribute() {
        final UUri uri = LongUriSerializer.instance().deserialize("//");
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW).withSink(uri).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final ValidationResult status = validator.validateSink(attributes);

        assertTrue(status.isFailure());
        assertEquals("Uri is empty.", status.getMessage());
    }

    @Test
    @DisplayName("test validating valid sink attribute")
    public void test_validating_valid_sink_attribute() {
        final UUri uri = LongUriSerializer.instance().deserialize("/haartley/1");
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW).withSink(uri).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final ValidationResult status = validator.validateSink(attributes);
        assertEquals(ValidationResult.success(), status);
    }

    @Test
    @DisplayName("test validating invalid ReqId attribute")
    public void test_validating_invalid_ReqId_attribute() {

        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW).withReqId(UUID.randomUUID()).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final ValidationResult status = validator.validateReqId(attributes);
        assertTrue(status.isFailure());
        assertEquals("Invalid UUID", status.getMessage());
    }

    @Test
    @DisplayName("test validating valid ReqId attribute")
    public void test_validating_valid_ReqId_attribute() {

        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW)
                .withReqId(UUIDFactory.Factories.UPROTOCOL.factory().create())
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final ValidationResult status = validator.validateReqId(attributes);
        assertEquals(ValidationResult.success(), status);
    }


    @Test
    @DisplayName("test validating invalid PermissionLevel attribute")
    public void test_validating_invalid_PermissionLevel_attribute() {

        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW)
                .withPermissionLevel(-1)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final ValidationResult status = validator.validatePermissionLevel(attributes);
        assertTrue(status.isFailure());
        assertEquals("Invalid Permission Level", status.getMessage());
    }

    @Test
    @DisplayName("test validating valid PermissionLevel attribute")
    public void test_validating_valid_PermissionLevel_attribute() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW)
                .withPermissionLevel(3)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final ValidationResult status = validator.validatePermissionLevel(attributes);
        assertEquals(ValidationResult.success(), status);
    }

    @Test
    @DisplayName("test validating valid PermissionLevel attribute")
    public void test_validating_valid_PermissionLevel_attribute_invalid() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW)
                .withPermissionLevel(0)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final ValidationResult status = validator.validatePermissionLevel(attributes);
        assertTrue(status.isFailure());
        assertEquals("Invalid Permission Level", status.getMessage());
    }

    @Test
    @DisplayName("test validating invalid commstatus attribute")
    public void test_validating_invalid_commstatus_attribute() {

        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW)
                .withCommStatus(100)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final ValidationResult status = validator.validateCommStatus(attributes);
        assertTrue(status.isFailure());
        assertEquals( "Invalid Communication Status Code", status.getMessage());
    }

    @Test
    @DisplayName("test validating valid commstatus attribute")
    public void test_validating_valid_commstatus_attribute() {

        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW)
                .withCommStatus(Code.ABORTED.value())
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final ValidationResult status = validator.validateCommStatus(attributes);
        assertEquals(ValidationResult.success(), status);
    }


    @Test
    @DisplayName("test validating request message types")
    public void test_validating_request_message_types() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.REQUEST, UPriority.NETWORK_CONTROL)
                .withSink(buildSink())
                .withTtl(100)
                .build();

        final UAttributesValidator validator = UAttributesValidator.getValidator(attributes);
        assertEquals("UAttributesValidator.Request", validator.toString());
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isSuccess());
        assertEquals("", status.getMessage());
    }

    @Test
    @DisplayName("test validating request validator using wrong messagetype")
    public void test_validating_request_validator_with_wrong_messagetype() {

        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.NETWORK_CONTROL)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.REQUEST.validator();
        assertEquals("UAttributesValidator.Request", validator.toString());
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Wrong Attribute Type [PUBLISH],Missing TTL,Missing Sink", status.getMessage());
    }

    @Test
    @DisplayName("test validating request validator using bad ttl")
    public void test_validating_request_validator_with_wrong_bad_ttl() {

        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.REQUEST, UPriority.NETWORK_CONTROL)
                .withSink(LongUriSerializer.instance().deserialize("/hartley/1/rpc.response"))
                .withTtl(-1)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.REQUEST.validator();
        assertEquals("UAttributesValidator.Request", validator.toString());
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Invalid TTL [-1]", status.getMessage());
    }

    @Test
    @DisplayName("test validating response validator using bad ttl")
    public void test_validating_response_validator_with_wrong_bad_ttl() {

        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.RESPONSE, UPriority.NETWORK_CONTROL)
                .withSink(LongUriSerializer.instance().deserialize("/hartley/1/rpc.response"))
                .withTtl(-1)
                .withReqId(UUIDFactory.Factories.UPROTOCOL.factory().create())
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.RESPONSE.validator();
        assertEquals("UAttributesValidator.Response", validator.toString());
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Invalid TTL [-1]", status.getMessage());
    }

    @Test
    @DisplayName("test validating response validator using bad UUID")
    public void test_validating_response_validator_with_bad_reqid() {

        final UUID id = UUID.randomUUID();
        final UAttributes attributes = new UAttributesBuilder(id,
                UMessageType.RESPONSE, UPriority.NETWORK_CONTROL)
                .withSink(LongUriSerializer.instance().deserialize("/hartley/1/rpc.response"))
                .withTtl(100)
                .withReqId(UUIDFactory.Factories.UPROTOCOL.factory().create())
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.RESPONSE.validator();
        assertEquals("UAttributesValidator.Response", validator.toString());
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals(String.format("Invalid UUID [%s]", id), status.getMessage());
    }


    @Test
    @DisplayName("test validating publish validator with wrong messagetype")
    public void test_validating_publish_validator_with_wrong_messagetype() {

        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.REQUEST, UPriority.NETWORK_CONTROL)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        assertEquals("UAttributesValidator.Publish", validator.toString());
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Wrong Attribute Type [REQUEST]", status.getMessage());
    }

    @Test
    @DisplayName("test validating response validator with wrong messagetype")
    public void test_validating_response_validator_with_wrong_messagetype() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.NETWORK_CONTROL)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.RESPONSE.validator();
        assertEquals("UAttributesValidator.Response", validator.toString());
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Wrong Attribute Type [PUBLISH],Missing Sink,Missing correlationId", status.getMessage());
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
        final ValidationResult status = validator.validate(attributes);
        assertEquals(ValidationResult.success(), status);
    }

    private UUri buildSink() {
        return UUri.newBuilder()
                .setAuthority(UAuthority.newBuilder().setName("vcu.someVin.veh.ultifi.gm.com"))
                .setEntity(UEntity.newBuilder().setName("petapp.ultifi.gm.com").setVersionMajor(1))
                .setResource(UResourceBuilder.forRpcResponse())
                .build();
    }
    
}
