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

import org.eclipse.uprotocol.uri.datamodel.UUri;
import org.eclipse.uprotocol.uri.factory.UriFactory;
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
                        UMessageType.REQUEST, UPriority.LOW).build());
        assertEquals("UAttributesValidator.Request", request.toString());

        UAttributesValidator response = UAttributesValidator.getValidator(
                new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                        UMessageType.RESPONSE, UPriority.LOW).build());
        assertEquals("UAttributesValidator.Response", response.toString());
    }

    @Test
    @DisplayName("test validating_valid_publish_messagetypes")
    public void test_validating_valid_publish_messagetypes() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW).build();

        final UAttributesValidator validator = UAttributesValidator.getValidator(attributes);
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isSuccess());
        assertEquals(status.msg(), "ok");
    }


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
        assertEquals(status.msg(), "Invalid TTL");
    }

    @Test
    @DisplayName("test validating publish valid ttl attribute")
    public void test_validating_valid_ttl_attribute() {

        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW)
                .withTtl(100).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final UStatus status = validator.validateTtl(attributes);
        assertEquals(status, UStatus.ok());
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
        assertEquals(status.msg(), "Invalid UUID");

        UStatus status1 = validator.validateId(attributes1);
        assertTrue(status1.isFailed());
        assertEquals(status1.getCode(), Code.INVALID_ARGUMENT.value());
        assertEquals(status1.msg(), "Invalid UUID");
    }

    @Test
    @DisplayName("test validating valid id attribute")
    public void test_validating_valid_id_attribute() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final UStatus status = validator.validateId(attributes);
        assertEquals(status, UStatus.ok());
    }

    @Test
    @DisplayName("test validating invalid sink attribute")
    public void test_validating_invalid_sink_attribute() {
        final UUri uri = UriFactory.parseFromUri("//");
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW).withSink(uri).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final UStatus status = validator.validateSink(attributes);

        assertTrue(status.isFailed());
        assertEquals(status.getCode(), Code.INVALID_ARGUMENT.value());
        assertEquals(status.msg(), "Uri is empty.");
    }

    @Test
    @DisplayName("test validating valid sink attribute")
    public void test_validating_valid_sink_attribute() {
        final UUri uri = UriFactory.parseFromUri("/haartley/1");
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW).withSink(uri).build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final UStatus status = validator.validateSink(attributes);
        assertEquals(status, UStatus.ok());
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
        assertEquals(status.msg(), "Invalid UUID");
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
        assertEquals(status, UStatus.ok());
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
        assertEquals(status.msg(), "Invalid Permission Level");
    }

    @Test
    @DisplayName("test validating valid PermissionLevel attribute")
    public void test_validating_valid_PermissionLevel_attribute() {
        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.PUBLISH, UPriority.LOW)
                .withPermissionLevel(0)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final UStatus status = validator.validatePermissionLevel(attributes);
        assertEquals(status, UStatus.ok());
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
        assertEquals(status.msg(), "Invalid Communication Status Code");
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
        assertEquals(status, UStatus.ok());
    }


    @Test
    @DisplayName("test validating request message types")
    public void test_validating_request_message_types() {
        final UUri sink = UriFactory.parseFromUri("/hartley/1/rpc.response");

        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.REQUEST, UPriority.NETWORK_CONTROL)
                .withSink(sink)
                .withTtl(100)
                .build();

        final UAttributesValidator validator = UAttributesValidator.getValidator(attributes);
        assertEquals("UAttributesValidator.Request", validator.toString());
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isSuccess());
        assertEquals(status.msg(), "ok");
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
        assertEquals("Wrong Attribute Type [PUBLISH],Missing Sink,Missing TTL", status.msg());
    }

    @Test
    @DisplayName("test validating request validator using bad ttl")
    public void test_validating_request_validator_with_wrong_bad_ttl() {

        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.REQUEST, UPriority.NETWORK_CONTROL)
                .withSink(UriFactory.parseFromUri("/hartley/1/rpc.response"))
                .withTtl(-1)
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.REQUEST.validator();
        assertEquals("UAttributesValidator.Request", validator.toString());
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isFailed());
        assertEquals(status.getCode(), Code.INVALID_ARGUMENT.value());
        assertEquals(status.msg(), "Invalid TTL");
    }

    @Test
    @DisplayName("test validating response validator using bad ttl")
    public void test_validating_response_validator_with_wrong_bad_ttl() {

        final UAttributes attributes = new UAttributesBuilder(UUIDFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.RESPONSE, UPriority.NETWORK_CONTROL)
                .withSink(UriFactory.parseFromUri("/hartley/1/rpc.response"))
                .withTtl(-1)
                .withReqId(UUIDFactory.Factories.UPROTOCOL.factory().create())
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.RESPONSE.validator();
        assertEquals("UAttributesValidator.Response", validator.toString());
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isFailed());
        assertEquals(status.getCode(), Code.INVALID_ARGUMENT.value());
        assertEquals(status.msg(), "Invalid TTL");
    }

    @Test
    @DisplayName("test validating response validator using bad UUID")
    public void test_validating_response_validator_with_bad_reqid() {

        final UAttributes attributes = new UAttributesBuilder(UUID.randomUUID(),
                UMessageType.RESPONSE, UPriority.NETWORK_CONTROL)
                .withSink(UriFactory.parseFromUri("/hartley/1/rpc.response"))
                .withTtl(100)
                .withReqId(UUIDFactory.Factories.UPROTOCOL.factory().create())
                .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.RESPONSE.validator();
        assertEquals("UAttributesValidator.Response", validator.toString());
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isFailed());
        assertEquals(status.getCode(), Code.INVALID_ARGUMENT.value());
        assertEquals(status.msg(), "Invalid UUID");
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
        assertEquals("Invalid Type,Missing Sink,Missing correlationId", status.msg());
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
        assertEquals(status, UStatus.ok());
    }
}
