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
 * SPDX-FileType: SOURCE
 * SPDX-FileCopyrightText: 2023 General Motors GTO LLC
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.uprotocol.transport.validator;

import org.eclipse.uprotocol.transport.builder.UAttributesBuilder;
import org.eclipse.uprotocol.transport.validate.UAttributesValidator;
import org.eclipse.uprotocol.uri.factory.UResourceBuilder;
import org.eclipse.uprotocol.uri.serializer.LongUriSerializer;
import org.eclipse.uprotocol.uuid.factory.UuidFactory;
import org.eclipse.uprotocol.v1.*;
import org.eclipse.uprotocol.validation.ValidationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class UAttributesValidatorTest {

    @Test
    @DisplayName("test fetching validator for valid types")
    public void test_fetching_validator_for_valid_types() {

        UAttributesValidator publish = UAttributesValidator.getValidator(
                UAttributesBuilder.publish(buildSource(), UPriority.UPRIORITY_CS0).build());
        assertEquals("UAttributesValidator.Publish", publish.toString());

        UAttributesValidator request = UAttributesValidator.getValidator(
                UAttributesBuilder.request(buildSource(), UUri.newBuilder().build(), UPriority.UPRIORITY_CS4, 1000).build());
        assertEquals("UAttributesValidator.Request", request.toString());

        UAttributesValidator response = UAttributesValidator.getValidator(
                UAttributesBuilder.response(buildSource(), UUri.newBuilder().build(), UPriority.UPRIORITY_CS4,
                        UuidFactory.Factories.UPROTOCOL.factory().create()).build());
        assertEquals("UAttributesValidator.Response", response.toString());

        UAttributesValidator notification = UAttributesValidator.getValidator(
                UAttributesBuilder.notification(buildSource(), UUri.newBuilder().build(), UPriority.UPRIORITY_CS4).build());
        assertEquals("UAttributesValidator.Notification", notification.toString());
    }

    @Test
    @DisplayName("test using notification validator for publish type message")
    public void test_using_notification_validator_for_publish_type_message() {
        UAttributes attributes = UAttributesBuilder.publish(buildSource(), UPriority.UPRIORITY_CS0).build();
        UAttributesValidator validator = UAttributesValidator.Validators.NOTIFICATION.validator();
        ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Wrong Attribute Type [UMESSAGE_TYPE_PUBLISH],Missing Sink", status.getMessage());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published")
    public void test_validate_uAttributes_for_publish_message_payload() {
        final UAttributes attributes = UAttributesBuilder.publish(buildSource(), UPriority.UPRIORITY_CS0).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isSuccess());
        assertEquals("", status.getMessage());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published with all values")
    public void test_validate_uAttributes_for_publish_message_payload_all_values() {
        final UAttributes attributes = UAttributesBuilder.publish(buildSource(), UPriority.UPRIORITY_CS0).withTtl(1000).withSink(buildSink())
                .withPermissionLevel(2).withCommStatus(UCode.INVALID_ARGUMENT).withReqId(UuidFactory.Factories.UPROTOCOL.factory().create())
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isSuccess());
        assertEquals("", status.getMessage());
    }


    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published with invalid type")
    public void test_validate_uAttributes_for_publish_message_payload_invalid_type() {
        final UAttributes attributes = UAttributesBuilder.response(buildSource(), buildSink(), UPriority.UPRIORITY_CS0,
                UuidFactory.Factories.UPROTOCOL.factory().create()).build();
        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Wrong Attribute Type [UMESSAGE_TYPE_RESPONSE]", status.getMessage());
    }


    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published with invalid time to live")
    public void test_validate_uAttributes_for_publish_message_payload_invalid_ttl() {
        final UAttributes attributes = UAttributesBuilder.publish(buildSource(), UPriority.UPRIORITY_CS0).withTtl(-1).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Invalid TTL [-1]", status.getMessage());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published with invalid sink")
    public void test_validate_uAttributes_for_publish_message_payload_invalid_sink() {
        final UAttributes attributes = UAttributesBuilder.publish(buildSource(), UPriority.UPRIORITY_CS0).withSink(UUri.getDefaultInstance())
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Uri is empty.", status.getMessage());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published with invalid permission level")
    public void test_validate_uAttributes_for_publish_message_payload_invalid_permission_level() {
        final UAttributes attributes = UAttributesBuilder.publish(buildSource(), UPriority.UPRIORITY_CS0).withPermissionLevel(-42).build();
        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Invalid Permission Level", status.getMessage());
    }

    
    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published with invalid request id")
    public void test_validate_uAttributes_for_publish_message_payload_invalid_request_id() {
        final java.util.UUID uuid_java = java.util.UUID.randomUUID();

        final UAttributes attributes = UAttributesBuilder.publish(buildSource(), UPriority.UPRIORITY_CS0).withReqId(
                UUID.newBuilder().setMsb(uuid_java.getMostSignificantBits()).setLsb(uuid_java.getLeastSignificantBits())
                        .build()).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Invalid UUID", status.getMessage());
    }

    // ----

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC request")
    public void test_validate_uAttributes_for_rpc_request_message_payload() {
        final UAttributes attributes = UAttributesBuilder.request(buildSource(), buildSink(), UPriority.UPRIORITY_CS4, 1000).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.REQUEST.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isSuccess());
        assertEquals("", status.getMessage());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC request with all values")
    public void test_validate_uAttributes_for_rpc_request_message_payload_all_values() {
        final UAttributes attributes = UAttributesBuilder.request(buildSource(), buildSink(), UPriority.UPRIORITY_CS4, 1000)
                .withPermissionLevel(2).withCommStatus(UCode.INVALID_ARGUMENT).withReqId(UuidFactory.Factories.UPROTOCOL.factory().create())
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.REQUEST.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isSuccess());
        assertEquals("", status.getMessage());
    }


    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC request with invalid type")
    public void test_validate_uAttributes_for_rpc_request_message_payload_invalid_type() {
        final UAttributes attributes = UAttributesBuilder.response(buildSource(), buildSink(), UPriority.UPRIORITY_CS4,
                UuidFactory.Factories.UPROTOCOL.factory().create()).withTtl(1000).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.REQUEST.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Wrong Attribute Type [UMESSAGE_TYPE_RESPONSE]", status.getMessage());
    }


    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC request with invalid time to live")
    public void test_validate_uAttributes_for_rpc_request_message_payload_invalid_ttl() {
        final UAttributes attributes = UAttributesBuilder.request(buildSource(), buildSink(), UPriority.UPRIORITY_CS4, -1).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.REQUEST.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Invalid TTL [-1]", status.getMessage());
    }


    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC request with invalid sink")
    public void test_validate_uAttributes_for_rpc_request_message_payload_invalid_sink() {
        final UAttributes attributes = UAttributesBuilder.request(buildSource(), UUri.getDefaultInstance(), UPriority.UPRIORITY_CS4, 1000)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.REQUEST.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Uri is empty.", status.getMessage());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC request with invalid permission level")
    public void test_validate_uAttributes_for_rpc_request_message_payload_invalid_permission_level() {
        final UAttributes attributes = UAttributesBuilder.request(buildSource(), buildSink(), UPriority.UPRIORITY_CS4, 1000)
                .withPermissionLevel(-42).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.REQUEST.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Invalid Permission Level", status.getMessage());
    }

   
    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC request with invalid request id")
    public void test_validate_uAttributes_for_rpc_request_message_payload_invalid_request_id() {
        final java.util.UUID uuid_java = java.util.UUID.randomUUID();

        final UAttributes attributes = UAttributesBuilder.request(buildSource(), buildSink(), UPriority.UPRIORITY_CS4, 1000).withReqId(
                UUID.newBuilder().setMsb(uuid_java.getMostSignificantBits()).setLsb(uuid_java.getLeastSignificantBits())
                        .build()).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.REQUEST.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Invalid UUID", status.getMessage());
    }

    // ----

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC response")
    public void test_validate_uAttributes_for_rpc_response_message_payload() {
        final UAttributes attributes = UAttributesBuilder.response(buildSource(), buildSink(), UPriority.UPRIORITY_CS4,
                UuidFactory.Factories.UPROTOCOL.factory().create()).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.RESPONSE.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isSuccess());
        assertEquals("", status.getMessage());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC response with all values")
    public void test_validate_uAttributes_for_rpc_response_message_payload_all_values() {
        final UAttributes attributes = UAttributesBuilder.response(buildSource(), buildSink(), UPriority.UPRIORITY_CS4,
                UuidFactory.Factories.UPROTOCOL.factory().create()).withPermissionLevel(2).withCommStatus(UCode.INVALID_ARGUMENT).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.RESPONSE.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isSuccess());
        assertEquals("", status.getMessage());
    }


    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC response with invalid type")
    public void test_validate_uAttributes_for_rpc_response_message_payload_invalid_type() {
        final UAttributes attributes = UAttributesBuilder.notification(buildSource(),buildSink(), UPriority.UPRIORITY_CS4).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.RESPONSE.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Wrong Attribute Type [UMESSAGE_TYPE_NOTIFICATION],Missing correlationId", status.getMessage());
    }


    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC response with invalid time to live")
    public void test_validate_uAttributes_for_rpc_response_message_payload_invalid_ttl() {
        final UAttributes attributes = UAttributesBuilder.response(buildSource(), buildSink(), UPriority.UPRIORITY_CS4,
                UuidFactory.Factories.UPROTOCOL.factory().create()).withTtl(-1).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.RESPONSE.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Invalid TTL [-1]", status.getMessage());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC response with missing sink and " +
            "missing request id")
    public void test_validate_uAttributes_for_rpc_response_message_payload_missing_sink_and_missing_requestId() {
        final UAttributes attributes =
                UAttributesBuilder.response(buildSource(), UUri.getDefaultInstance(),UPriority.UPRIORITY_CS4,UUID.getDefaultInstance()).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.RESPONSE.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Missing Sink,Missing correlationId", status.getMessage());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC response with invalid permission level")
    public void test_validate_uAttributes_for_rpc_response_message_payload_invalid_permission_level() {
        final UAttributes attributes = UAttributesBuilder.response(buildSource(), buildSink(), UPriority.UPRIORITY_CS4,
                UuidFactory.Factories.UPROTOCOL.factory().create()).withPermissionLevel(-42).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.RESPONSE.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Invalid Permission Level", status.getMessage());
    }


    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC response with missing request id")
    public void test_validate_uAttributes_for_rpc_response_message_payload_missing_request_id() {
        final UAttributes attributes =  UAttributesBuilder.response(buildSource(),buildSink(), UPriority.UPRIORITY_CS4,UUID.getDefaultInstance()).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.RESPONSE.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Missing correlationId", status.getMessage());
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be an RPC response with invalid request id")
    public void test_validate_uAttributes_for_rpc_response_message_payload_invalid_request_id() {
        final java.util.UUID uuid_java = java.util.UUID.randomUUID();

        final UUID reqid = UUID.newBuilder().setMsb(uuid_java.getMostSignificantBits())
                .setLsb(uuid_java.getLeastSignificantBits()).build();
        final UAttributes attributes = UAttributesBuilder.response(buildSource(), buildSink(), UPriority.UPRIORITY_CS4, reqid).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.RESPONSE.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals(String.format("Invalid correlationId [%s]", reqid), status.getMessage());
    }

    // ----

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published not expired")
    public void test_validate_uAttributes_for_publish_message_payload_not_expired() {
        final UAttributes attributes = UAttributesBuilder.publish(buildSource(), UPriority.UPRIORITY_CS0).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        assertFalse(validator.isExpired(attributes));
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published not expired with ttl zero")
    public void test_validate_uAttributes_for_publish_message_payload_not_expired_with_ttl_zero() {
        final UAttributes attributes = UAttributesBuilder.publish(buildSource(), UPriority.UPRIORITY_CS0).withTtl(0).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        assertFalse(validator.isExpired(attributes));
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published not expired with ttl")
    public void test_validate_uAttributes_for_publish_message_payload_not_expired_with_ttl() {
        final UAttributes attributes = UAttributesBuilder.publish(buildSource(), UPriority.UPRIORITY_CS0).withTtl(10000).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        assertFalse(validator.isExpired(attributes));
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published not expired with ttl")
    public void test_validate_uAttributes_for_publish_message_payload_with_negative_ttl() {
        final UAttributes attributes = UAttributesBuilder.publish(buildSource(), UPriority.UPRIORITY_CS0).withTtl(-1).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        assertFalse(validator.isExpired(attributes));
    }

    @Test
    @DisplayName("Validate a UAttributes for payload that is meant to be published expired with ttl")
    public void test_validate_uAttributes_for_publish_message_payload_expired_with_ttl() throws InterruptedException {
        final UAttributes attributes = UAttributesBuilder.publish(buildSource(), UPriority.UPRIORITY_CS0).withTtl(1).build();

        Thread.sleep(800);

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        assertTrue(validator.isExpired(attributes));
    }


    // ----

    @Test
    @DisplayName("test validating publish invalid ttl attribute")
    public void test_validating_publish_invalid_ttl_attribute() {

        final UAttributes attributes = UAttributesBuilder.publish(buildSource(), UPriority.UPRIORITY_CS0).withTtl(-1).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final ValidationResult status = validator.validateTtl(attributes);
        assertTrue(status.isFailure());
        assertEquals("Invalid TTL [-1]", status.getMessage());
    }

    @Test
    @DisplayName("test validating publish valid ttl attribute")
    public void test_validating_valid_ttl_attribute() {

        final UAttributes attributes = UAttributesBuilder.publish(buildSource(), UPriority.UPRIORITY_CS0).withTtl(100).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final ValidationResult status = validator.validateTtl(attributes);
        assertEquals(ValidationResult.success(), status);
    }



    @Test
    @DisplayName("test validating invalid sink attribute")
    public void test_validating_invalid_sink_attribute() {
        final UUri uri = LongUriSerializer.instance().deserialize("//");
        final UAttributes attributes = UAttributesBuilder.publish(buildSource(), UPriority.UPRIORITY_CS0).withSink(uri).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final ValidationResult status = validator.validateSink(attributes);

        assertTrue(status.isFailure());
        assertEquals("Uri is empty.", status.getMessage());
    }

    @Test
    @DisplayName("test validating valid sink attribute")
    public void test_validating_valid_sink_attribute() {
        final UUri uri = LongUriSerializer.instance().deserialize("/haartley/1");
        final UAttributes attributes = UAttributesBuilder.publish(buildSource(), UPriority.UPRIORITY_CS0).withSink(uri).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final ValidationResult status = validator.validateSink(attributes);
        assertEquals(ValidationResult.success(), status);
    }

    @Test
    @DisplayName("test validating invalid ReqId attribute")
    public void test_validating_invalid_ReqId_attribute() {
        final java.util.UUID uuid_java = java.util.UUID.randomUUID();

        final UAttributes attributes = UAttributesBuilder.publish(buildSource(), UPriority.UPRIORITY_CS0).withReqId(
                UUID.newBuilder().setMsb(uuid_java.getMostSignificantBits()).setLsb(uuid_java.getLeastSignificantBits())
                        .build()).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final ValidationResult status = validator.validateReqId(attributes);
        assertTrue(status.isFailure());
        assertEquals("Invalid UUID", status.getMessage());
    }

    @Test
    @DisplayName("test validating valid ReqId attribute")
    public void test_validating_valid_ReqId_attribute() {

        final UAttributes attributes = UAttributesBuilder.publish(buildSource(), UPriority.UPRIORITY_CS0)
                .withReqId(UuidFactory.Factories.UPROTOCOL.factory().create()).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final ValidationResult status = validator.validateReqId(attributes);
        assertEquals(ValidationResult.success(), status);
    }


    @Test
    @DisplayName("test validating invalid PermissionLevel attribute")
    public void test_validating_invalid_PermissionLevel_attribute() {

        final UAttributes attributes = UAttributesBuilder.publish(buildSource(), UPriority.UPRIORITY_CS0).withPermissionLevel(-1).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final ValidationResult status = validator.validatePermissionLevel(attributes);
        assertTrue(status.isFailure());
        assertEquals("Invalid Permission Level", status.getMessage());
    }

    @Test
    @DisplayName("test validating valid PermissionLevel attribute")
    public void test_validating_valid_PermissionLevel_attribute() {
        final UAttributes attributes = UAttributesBuilder.publish(buildSource(), UPriority.UPRIORITY_CS0).withPermissionLevel(3).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final ValidationResult status = validator.validatePermissionLevel(attributes);
        assertEquals(ValidationResult.success(), status);
    }

    @Test
    @DisplayName("test validating valid PermissionLevel attribute")
    public void test_validating_valid_PermissionLevel_attribute_invalid() {
        final UAttributes attributes = UAttributesBuilder.publish(buildSource(), UPriority.UPRIORITY_CS0).withPermissionLevel(0).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final ValidationResult status = validator.validatePermissionLevel(attributes);
        assertTrue(status.isFailure());
        assertEquals("Invalid Permission Level", status.getMessage());
    }


    @Test
    @DisplayName("test validating request message types")
    public void test_validating_request_message_types() {
        final UAttributes attributes = UAttributesBuilder.request(buildSource(), buildSink(), UPriority.UPRIORITY_CS6, 100).build();

        final UAttributesValidator validator = UAttributesValidator.getValidator(attributes);
        assertEquals("UAttributesValidator.Request", validator.toString());
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isSuccess());
        assertEquals("", status.getMessage());
    }

    @Test
    @DisplayName("test validating request validator using wrong messagetype")
    public void test_validating_request_validator_with_wrong_messagetype() {

        final UAttributes attributes = UAttributesBuilder.publish(buildSource(), UPriority.UPRIORITY_CS6).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.REQUEST.validator();
        assertEquals("UAttributesValidator.Request", validator.toString());
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Wrong Attribute Type [UMESSAGE_TYPE_PUBLISH],Missing TTL,Missing Sink", status.getMessage());
    }

    @Test
    @DisplayName("test validating request validator using bad ttl")
    public void test_validating_request_validator_with_wrong_bad_ttl() {

        final UAttributes attributes = UAttributesBuilder.request(buildSource(),
                LongUriSerializer.instance().deserialize("/hartley/1/rpc.response"),UPriority.UPRIORITY_CS6, -1).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.REQUEST.validator();
        assertEquals("UAttributesValidator.Request", validator.toString());
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Invalid TTL [-1]", status.getMessage());
    }

    @Test
    @DisplayName("test validating response validator using bad ttl")
    public void test_validating_response_validator_with_wrong_bad_ttl() {

        final UAttributes attributes = UAttributesBuilder.response(buildSource(),
                LongUriSerializer.instance().deserialize("/hartley/1/rpc.response"), UPriority.UPRIORITY_CS6,
                UuidFactory.Factories.UPROTOCOL.factory().create()).withTtl(-1).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.RESPONSE.validator();
        assertEquals("UAttributesValidator.Response", validator.toString());
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Invalid TTL [-1]", status.getMessage());
    }


    @Test
    @DisplayName("test validating publish validator with wrong messagetype")
    public void test_validating_publish_validator_with_wrong_messagetype() {

        final UAttributes attributes =  UAttributesBuilder.request(buildSource(), buildSink(), UPriority.UPRIORITY_CS6, 1000).build();
        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        assertEquals("UAttributesValidator.Publish", validator.toString());
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Wrong Attribute Type [UMESSAGE_TYPE_REQUEST]", status.getMessage());
    }

    @Test
    @DisplayName("test validating response validator with wrong messagetype")
    public void test_validating_response_validator_with_wrong_messagetype() {
        final UAttributes attributes =  UAttributesBuilder.publish(buildSource(), UPriority.UPRIORITY_CS6).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.RESPONSE.validator();
        assertEquals("UAttributesValidator.Response", validator.toString());
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Wrong Attribute Type [UMESSAGE_TYPE_PUBLISH],Missing Sink,Missing correlationId", status.getMessage());
    }


    @Test
    @DisplayName("test validating request containing token")
    public void test_validating_request_containing_token() {

        final UAttributes attributes =  UAttributesBuilder.publish(buildSource(), UPriority.UPRIORITY_CS0).withToken("null").build();

        final UAttributesValidator validator = UAttributesValidator.getValidator(attributes);
        assertEquals("UAttributesValidator.Publish", validator.toString());
        final ValidationResult status = validator.validate(attributes);
        assertEquals(ValidationResult.success(), status);
    }

    @Test
    @DisplayName("test_valid_request_methoduri_in_sink")
    public void test_valid_request_methoduri_in_sink(){
        final UUri sink= LongUriSerializer.instance().deserialize("/test.service/1/rpc.method");
        final UAttributes attributes =
                UAttributesBuilder.request(buildSource(), sink, UPriority.UPRIORITY_CS0,3000).build();
        final UAttributesValidator validator = UAttributesValidator.getValidator(attributes);
        assertEquals("UAttributesValidator.Request", validator.toString());
        final ValidationResult status = validator.validate(attributes);
        assertEquals(ValidationResult.success(), status);
    }

    @Test
    @DisplayName("test_invalid_request_methoduri_in_sink")
    public void test_invalid_request_methoduri_in_sink(){
        final UUri sink= LongUriSerializer.instance().deserialize("/test.client/1/test.response");
        final UAttributes attributes =
                UAttributesBuilder.request(buildSource(), sink,UPriority.UPRIORITY_CS0,3000).build();
        final UAttributesValidator validator = UAttributesValidator.getValidator(attributes);
        assertEquals("UAttributesValidator.Request", validator.toString());
        final ValidationResult status = validator.validate(attributes);
        assertEquals("Invalid RPC method uri. Uri should be the method to be called, or method from response.", status.getMessage());
    }

    @Test
    @DisplayName("test_valid_response_uri_in_sink")
    public void test_valid_response_uri_in_sink(){
        final UUri sink= LongUriSerializer.instance().deserialize("/test.client/1/rpc.response");
        final UAttributes attributes =
                UAttributesBuilder.response(buildSource(), sink,UPriority.UPRIORITY_CS0,UuidFactory.Factories.UPROTOCOL.factory().create()).build();
        final UAttributesValidator validator = UAttributesValidator.getValidator(attributes);
        assertEquals("UAttributesValidator.Response", validator.toString());
        final ValidationResult status = validator.validate(attributes);
        assertEquals(ValidationResult.success(), status);
    }

    @Test
    @DisplayName("test_invalid_response_uri_in_sink")
    public void test_invalid_response_uri_in_sink(){
        final UUri sink= LongUriSerializer.instance().deserialize("/test.client/1/rpc.method");
        final UAttributes attributes =
                UAttributesBuilder.response(buildSource(), sink,UPriority.UPRIORITY_CS0,UuidFactory.Factories.UPROTOCOL.factory().create()).build();
        final UAttributesValidator validator = UAttributesValidator.getValidator(attributes);
        assertEquals("UAttributesValidator.Response", validator.toString());
        final ValidationResult status = validator.validate(attributes);
        assertEquals("Invalid RPC response type.", status.getMessage());
    }

    @Test
    @DisplayName("test notification validation with missing sink")
    public void test_notification_validation_with_missing_sink() {
        final UAttributes attributes = UAttributesBuilder.notification(buildSource(), UUri.getDefaultInstance(), UPriority.UPRIORITY_CS0).build();
        final UAttributesValidator validator = UAttributesValidator.Validators.NOTIFICATION.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Missing Sink", status.getMessage());
    }

    @Test
    @DisplayName("test notification validation using publish validator")
    public void test_notification_validation_using_publish_validator() {
        final UAttributes attributes = UAttributesBuilder.notification(buildSource(), buildSink(), UPriority.UPRIORITY_CS0).build();
        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Wrong Attribute Type [UMESSAGE_TYPE_NOTIFICATION]", status.getMessage());
    }

    @Test
    @DisplayName("test notification validation when sink is missing")
    public void test_notification_validation_when_sink_is_missing() {
        final UAttributes attributes = UAttributes.newBuilder()
            .setId(UuidFactory.Factories.UPROTOCOL.factory().create())
            .setSource(buildSource())
            .setType(UMessageType.UMESSAGE_TYPE_NOTIFICATION)
            .setPriority(UPriority.UPRIORITY_CS0)
            .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.NOTIFICATION.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isFailure());
        assertEquals("Missing Sink", status.getMessage());
    }

    @Test
    @DisplayName("test notification validation with a valid notification UAttributes")
    public void test_notification_validation_with_a_valid_notification_UAttributes() {
        final UAttributes attributes = UAttributesBuilder.notification(buildSource(), buildSink(), UPriority.UPRIORITY_CS0).build();
        final UAttributesValidator validator = UAttributesValidator.Validators.NOTIFICATION.validator();
        final ValidationResult status = validator.validate(attributes);
        assertTrue(status.isSuccess());
        assertEquals("", status.getMessage());
    }

    private UUri buildSink() {
        return UUri.newBuilder().setAuthority(UAuthority.newBuilder().setName("vcu.someVin.veh.ultifi.gm.com"))
                .setEntity(UEntity.newBuilder().setName("petapp.ultifi.gm.com").setVersionMajor(1))
                .setResource(UResourceBuilder.forRpcResponse()).build();
    }

    private UUri buildSource() {
        return UUri.newBuilder()
                .setEntity(UEntity.newBuilder().setName("hartley_app").setVersionMajor(1))
                .setResource(UResourceBuilder.forRpcResponse()).build();
    }

}
