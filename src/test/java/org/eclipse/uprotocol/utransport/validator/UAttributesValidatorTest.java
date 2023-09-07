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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.eclipse.uprotocol.uri.datamodel.UUri;
import org.eclipse.uprotocol.uri.factory.UriFactory;
import org.eclipse.uprotocol.utransport.datamodel.UAttributes;
import org.eclipse.uprotocol.utransport.datamodel.UMessageType;
import org.eclipse.uprotocol.utransport.datamodel.UPriority;
import org.eclipse.uprotocol.utransport.datamodel.UStatus;
import org.eclipse.uprotocol.utransport.datamodel.UAttributes.UAttributesBuilder;
import org.eclipse.uprotocol.utransport.datamodel.UStatus.Code;
import org.eclipse.uprotocol.utransport.validate.UAttributesValidator;
import org.eclipse.uprotocol.uuid.factory.UUIDFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


class UAttributesValidatorTest {

    @Test
    @DisplayName("test fetching validator for valid types")
    public void test_fetching_validator_for_valid_types() {

        UAttributesValidator publish = UAttributesValidator.getValidator(new UAttributesBuilder()
            .withId(UUIDFactory.Factories.UPROTOCOL.factory().create())
            .withPriority(UPriority.LOW)
            .withType(UMessageType.PUBLISH)
            .build());
        assert(publish instanceof UAttributesValidator);

        UAttributesValidator request = UAttributesValidator.getValidator(new UAttributesBuilder()
            .withId(UUIDFactory.Factories.UPROTOCOL.factory().create())
            .withPriority(UPriority.LOW)
            .withType(UMessageType.REQUEST)
            .build());
        assert(request instanceof UAttributesValidator);

        UAttributesValidator response = UAttributesValidator.getValidator(new UAttributesBuilder()
            .withId(UUIDFactory.Factories.UPROTOCOL.factory().create())
            .withPriority(UPriority.LOW)
            .withType(UMessageType.RESPONSE)
            .build());
        assert(response instanceof UAttributesValidator);

        UAttributesValidator invalid = UAttributesValidator.getValidator(new UAttributesBuilder()
            .withId(UUIDFactory.Factories.UPROTOCOL.factory().create())
            .withPriority(UPriority.LOW)
            .build());
        assert(invalid instanceof UAttributesValidator);
    }

    @Test
    @DisplayName("test validating invalid types")
    public void test_validator_invalid_types() {
        final UAttributes attributes = new UAttributesBuilder()
            .withId(UUIDFactory.Factories.UPROTOCOL.factory().create())
            .withPriority(UPriority.LOW)
            .build();

        final UAttributesValidator validator = UAttributesValidator.getValidator(attributes);
        assert(validator instanceof UAttributesValidator);
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isFailed());
        assertEquals(status.getCode(), Code.INVALID_ARGUMENT.value());
        assertEquals(status.msg(), "Wrong Attribute Type");
    }

    
    @Test
    @DisplayName("test validating_valid_publish_messagetypes")
    public void test_validating_valid_publish_messagetypes() {
        final UAttributes attributes = new UAttributesBuilder()
            .withId(UUIDFactory.Factories.UPROTOCOL.factory().create())
            .withPriority(UPriority.LOW)
            .withType(UMessageType.PUBLISH)
            .build();

        final UAttributesValidator validator = UAttributesValidator.getValidator(attributes);
        assert(validator instanceof UAttributesValidator);
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isSuccess());
        assertEquals(status.msg(), "ok");
    }

    @Test
    @DisplayName("test validating invalid priority attribute")
    public void test_validating_invalid_priority_attribute() {
        final UAttributes attributes = new UAttributesBuilder()
            .withPriority(null)
            .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        
        final UStatus status = validator.validatePriority(attributes);
        assertTrue(status.isFailed());
        assertEquals(status.getCode(), Code.INVALID_ARGUMENT.value());
        assertEquals(status.msg(), "Invalid Priority");
    }

    @Test
    @DisplayName("test validating valid priority attribute")
    public void test_validating_valid_priority_attribute() {
        final UAttributes attributes = new UAttributesBuilder()
            .withPriority(UPriority.LOW)
            .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final UStatus status = validator.validatePriority(attributes);
        assertEquals(status, UStatus.ok());
    }

    @Test
    @DisplayName("test validating publish invalid ttl attribute")
    public void test_validating_publish_invalid_ttl_attribute() {
        final UAttributes attributes = new UAttributesBuilder()
            .withTtl(-1)
            .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final UStatus status = validator.validateTtl(attributes);
        assertTrue(status.isFailed());
        assertEquals(status.getCode(), Code.INVALID_ARGUMENT.value());
        assertEquals(status.msg(), "Invalid TTL");
    }

    @Test
    @DisplayName("test validating publish valid ttl attribute")
    public void test_validating_valid_ttl_attribute() {
        final UAttributes attributes = new UAttributesBuilder()
            .withTtl(100)
            .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final UStatus status = validator.validateTtl(attributes);
        assertEquals(status, UStatus.ok());
    }

    @Test
    @DisplayName("test validating invalid id attribute")
    public void test_validating_invalid_id_attribute() {
        final UAttributes attributes = new UAttributesBuilder()
            .withId(null)
            .build();
        final UAttributes attributes1 = new UAttributesBuilder()
            .withId(UUID.randomUUID())
            .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        UStatus status = validator.validateId(attributes);
        assertTrue(status.isFailed());
        assertEquals(status.getCode(), Code.INVALID_ARGUMENT.value());
        assertEquals(status.msg(), "Invalid UUID");

        status = validator.validateId(attributes1);
        assertTrue(status.isFailed());
        assertEquals(status.getCode(), Code.INVALID_ARGUMENT.value());
        assertEquals(status.msg(), "Invalid UUID");
    }

    @Test
    @DisplayName("test validating valid id attribute")
    public void test_validating_valid_id_attribute() {
        final UAttributes attributes = new UAttributesBuilder()
            .withId(UUIDFactory.Factories.UPROTOCOL.factory().create())
            .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final UStatus status = validator.validateId(attributes);
        assertEquals(status, UStatus.ok());
    }

    @Test
    @DisplayName("test validating invalid sink attribute")
    public void test_validating_invalid_sink_attribute() {
        final UUri uri = UriFactory.parseFromUri("//");
        final UAttributes attributes = new UAttributesBuilder()
            .withSink(uri)
            .build();

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
        final UAttributes attributes = new UAttributesBuilder()
            .withSink(uri)
            .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final UStatus status = validator.validateSink(attributes);
        assertEquals(status, UStatus.ok());
    }

    @Test
    @DisplayName("test validating invalid ReqId attribute")
    public void test_validating_invalid_ReqId_attribute() {
        final UAttributes attributes = new UAttributesBuilder()
            .withReqId(UUID.randomUUID())
            .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final UStatus status = validator.validateReqId(attributes);
        assertTrue(status.isFailed());
        assertEquals(status.getCode(), Code.INVALID_ARGUMENT.value());
        assertEquals(status.msg(), "Invalid UUID");
    }

    @Test
    @DisplayName("test validating valid ReqId attribute")
    public void test_validating_valid_ReqId_attribute() {
        final UAttributes attributes = new UAttributesBuilder()
            .withReqId(UUIDFactory.Factories.UPROTOCOL.factory().create())
            .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final UStatus status = validator.validateReqId(attributes);
        assertEquals(status, UStatus.ok());
    }


    @Test
    @DisplayName("test validating invalid PermissionLevel attribute")
    public void test_validating_invalid_PermissionLevel_attribute() {
        final UAttributes attributes = new UAttributesBuilder()
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
        final UAttributes attributes = new UAttributesBuilder()
            .withPermissionLevel(0)
            .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        final UStatus status = validator.validatePermissionLevel(attributes);
        assertEquals(status, UStatus.ok());
    }

    @Test
    @DisplayName("test validating invalid commstatus attribute")
    public void test_validating_invalid_commstatus_attribute() {
        final UAttributes attributes = new UAttributesBuilder()
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
        final UAttributes attributes = new UAttributesBuilder()
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
        final UAttributes attributes = new UAttributesBuilder()
            .withId(UUIDFactory.Factories.UPROTOCOL.factory().create())
            .withPriority(UPriority.NETWORK_CONTROL)
            .withType(UMessageType.REQUEST)
            .withSink(sink)
            .withTtl(100)
            .build();

        final UAttributesValidator validator = UAttributesValidator.getValidator(attributes);
        assert(validator instanceof UAttributesValidator);
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isSuccess());
        assertEquals(status.msg(), "ok");
    }

    @Test
    @DisplayName("test validating request validator using wrong messagetype")
    public void test_validating_request_validator_with_wrong_messagetype() {
        final UAttributes attributes = new UAttributesBuilder()
            .withType(UMessageType.PUBLISH)
            .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.REQUEST.validator();
        assert(validator instanceof UAttributesValidator);
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isFailed());
        assertEquals(status.getCode(), Code.INVALID_ARGUMENT.value());
        assertEquals(status.msg(), "Wrong Attribute Type,Invalid UUID,Missing Sink,Invalid Priority,Missing TTL");
    }

    @Test
    @DisplayName("test validating request validator using bad ttl")
    public void test_validating_request_validator_with_wrong_bad_ttl() {
        final UAttributes attributes = new UAttributesBuilder()
            .withId(UUIDFactory.Factories.UPROTOCOL.factory().create())
            .withSink(UriFactory.parseFromUri("/hartley/1/rpc.response"))
            .withPriority(UPriority.NETWORK_CONTROL)
            .withType(UMessageType.REQUEST)
            .withTtl(-1)
            .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.REQUEST.validator();
        assert(validator instanceof UAttributesValidator);
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isFailed());
        assertEquals(status.getCode(), Code.INVALID_ARGUMENT.value());
        assertEquals(status.msg(), "Invalid TTL");
    }

    @Test
    @DisplayName("test validating response validator using bad ttl")
    public void test_validating_response_validator_with_wrong_bad_ttl() {
        final UAttributes attributes = new UAttributesBuilder()
            .withId(UUIDFactory.Factories.UPROTOCOL.factory().create())
            .withSink(UriFactory.parseFromUri("/hartley/1/rpc.response"))
            .withPriority(UPriority.NETWORK_CONTROL)
            .withTtl(-1)
            .withType(UMessageType.RESPONSE)
            .withReqId(UUIDFactory.Factories.UPROTOCOL.factory().create())
            .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.RESPONSE.validator();
        assert(validator instanceof UAttributesValidator);
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isFailed());
        assertEquals(status.getCode(), Code.INVALID_ARGUMENT.value());
        assertEquals(status.msg(), "Invalid TTL");
    }

    @Test
    @DisplayName("test validating response validator using bad UUID")
    public void test_validating_response_validator_with_bad_reqid() {
        final UAttributes attributes = new UAttributesBuilder()
            .withId(UUIDFactory.Factories.UPROTOCOL.factory().create())
            .withSink(UriFactory.parseFromUri("/hartley/1/rpc.response"))
            .withPriority(UPriority.NETWORK_CONTROL)
            .withTtl(100)
            .withType(UMessageType.RESPONSE)
            .withReqId(UUID.randomUUID())
            .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.RESPONSE.validator();
        assert(validator instanceof UAttributesValidator);
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isFailed());
        assertEquals(status.getCode(), Code.INVALID_ARGUMENT.value());
        assertEquals(status.msg(), "Invalid UUID");
    }


    @Test
    @DisplayName("test validating publish validator with wrong messagetype")
    public void test_validating_publish_validator_with_wrong_messagetype() {
        final UAttributes attributes = new UAttributesBuilder()
            .withType(UMessageType.REQUEST)
            .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        assert(validator instanceof UAttributesValidator);
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isFailed());
        assertEquals(status.getCode(), Code.INVALID_ARGUMENT.value());
        assertEquals(status.msg(), "Wrong Attribute Type,Invalid UUID,Invalid Priority");
    }

    @Test
    @DisplayName("test validating response validator with wrong messagetype")
    public void test_validating_response_validator_with_wrong_messagetype() {
        final UAttributes attributes = new UAttributesBuilder()
            .withType(UMessageType.PUBLISH)
            .build();

        final UAttributesValidator validator = UAttributesValidator.Validators.RESPONSE.validator();
        assert(validator instanceof UAttributesValidator);
        final UStatus status = validator.validate(attributes);
        assertTrue(status.isFailed());
        assertEquals(status.getCode(), Code.INVALID_ARGUMENT.value());
        assertEquals(status.msg(), "Invalid Type,Invalid UUID,Missing Sink,Invalid Priority,Missing correlationId");
    }


    @Test
    @DisplayName("test validating request containing token")
    public void test_validating_request_containing_token() {
        final UAttributes attributes = new UAttributesBuilder()
            .withId(UUIDFactory.Factories.UPROTOCOL.factory().create())
            .withPriority(UPriority.LOW)
            .withType(UMessageType.PUBLISH)
            .withToken("null")
            .build();

        final UAttributesValidator validator = UAttributesValidator.getValidator(attributes);
        assert(validator instanceof UAttributesValidator);
        final UStatus status = validator.validate(attributes);
        assertEquals(status, UStatus.ok());
    }
}
