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

package org.eclipse.uprotocol.cloudevent.validate;

import com.google.protobuf.Any;
import com.google.rpc.Code;
import com.google.rpc.Status;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import org.eclipse.uprotocol.cloudevent.datamodel.UCloudEventAttributes;
import org.eclipse.uprotocol.cloudevent.factory.CloudEventFactory;
import org.eclipse.uprotocol.cloudevent.factory.UCloudEvent;
import org.eclipse.uprotocol.uri.serializer.LongUriSerializer;
import org.eclipse.uprotocol.uuid.factory.UuidFactory;
import org.eclipse.uprotocol.uuid.serializer.LongUuidSerializer;
import org.eclipse.uprotocol.v1.*;
import org.eclipse.uprotocol.validation.ValidationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class CloudEventValidatorTest {

    @Test
    @DisplayName("Test get a publish cloud event validator")
    void test_get_a_publish_cloud_event_validator() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withType("pub.v1");
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.getValidator(cloudEvent);
        final Status status = validator.validateType(cloudEvent).toStatus();
        assertEquals(status, ValidationResult.STATUS_SUCCESS);
        assertEquals("CloudEventValidator.Publish", validator.toString());
    }

    @Test
    @DisplayName("Test get a notification cloud event validator")
    void test_get_a_notification_cloud_event_validator() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withExtension("sink", "//bo.cloud/petapp")
                .withType("pub.v1");
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.Validators.NOTIFICATION.validator();
        final Status status = validator.validateType(cloudEvent).toStatus();
        assertEquals(status, ValidationResult.STATUS_SUCCESS);
        assertEquals("CloudEventValidator.Notification", validator.toString());
    }

    @Test
    @DisplayName("Test publish cloud event type")
    void test_publish_cloud_event_type() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withType("res.v1");
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.Validators.PUBLISH.validator();
        final Status status = validator.validateType(cloudEvent).toStatus();
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("Invalid CloudEvent type [res.v1]. CloudEvent of type Publish must have a type of 'pub.v1'",
                status.getMessage());
    }

    @Test
    @DisplayName("Test notification cloud event type")
    void test_notification_cloud_event_type() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withType("res.v1");
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.Validators.NOTIFICATION.validator();
        final Status status = validator.validateType(cloudEvent).toStatus();
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("Invalid CloudEvent type [res.v1]. CloudEvent of type Publish must have a type of 'pub.v1'",
                status.getMessage());
    }


    @Test
    @DisplayName("Test get a request cloud event validator")
    void test_get_a_request_cloud_event_validator() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withType("req.v1");
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.getValidator(cloudEvent);
        final Status status = validator.validateType(cloudEvent).toStatus();
        assertEquals(status, ValidationResult.STATUS_SUCCESS);
        assertEquals("CloudEventValidator.Request", validator.toString());
    }

    @Test
    @DisplayName("Test request cloud event type")
    void test_request_cloud_event_type() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withType("pub.v1");
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.Validators.REQUEST.validator();
        final Status status = validator.validateType(cloudEvent).toStatus();
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("Invalid CloudEvent type [pub.v1]. CloudEvent of type Request must have a type of 'req.v1'",
                status.getMessage());
    }

    @Test
    @DisplayName("Test get a response cloud event validator")
    void test_get_a_response_cloud_event_validator() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withType("res.v1");
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.getValidator(cloudEvent);
        final Status status = validator.validateType(cloudEvent).toStatus();
        assertEquals(status, ValidationResult.STATUS_SUCCESS);
        assertEquals("CloudEventValidator.Response", validator.toString());
    }

    @Test
    @DisplayName("Test response cloud event type")
    void test_response_cloud_event_type() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withType("pub.v1");
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.Validators.RESPONSE.validator();
        final Status status = validator.validateType(cloudEvent).toStatus();
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("Invalid CloudEvent type [pub.v1]. CloudEvent of type Response must have a type of 'res.v1'",
                status.getMessage());
    }

    @Test
    @DisplayName("Test get a publish cloud event validator when cloud event type is unknown")
    void test_get_a_publish_cloud_event_validator_when_cloud_event_type_is_unknown() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withType("lala.v1");
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.getValidator(cloudEvent);
        assertEquals("CloudEventValidator.Publish", validator.toString());
    }

    @Test
    @DisplayName("Test validate version")
    void validate_cloud_event_version_when_valid() {
        UUID uuid = UuidFactory.Factories.UPROTOCOL.factory().create();
        String str_uuid = LongUuidSerializer.instance().serialize(uuid);
        
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH))
                .withId(str_uuid);
        CloudEvent cloudEvent = builder.build();
        final Status status = CloudEventValidator.validateVersion(cloudEvent).toStatus();

        assertEquals(status, ValidationResult.STATUS_SUCCESS);
    }

    @Test
    @DisplayName("Test validate version when not valid")
    void validate_cloud_event_version_when_not_valid() {
        final Any payloadForTest = buildProtoPayloadForTest();
        final CloudEventBuilder builder = CloudEventBuilder.v03().withId("id").withType("pub.v1")
                .withSource(URI.create("/body.access")).withDataContentType("application/protobuf")
                .withDataSchema(URI.create(payloadForTest.getTypeUrl())).withData(payloadForTest.toByteArray());

        CloudEvent cloudEvent = builder.build();
        final Status status = CloudEventValidator.validateVersion(cloudEvent).toStatus();
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("Invalid CloudEvent version [0.3]. CloudEvent version must be 1.0.", status.getMessage());
    }

    @Test
    @DisplayName("Test validate cloudevent id when valid")
    void validate_cloud_event_id_when_valid() {
        UUID uuid = UuidFactory.Factories.UPROTOCOL.factory().create();
        String str_uuid = LongUuidSerializer.instance().serialize(uuid);
        
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH))
                .withId(str_uuid);
        CloudEvent cloudEvent = builder.build();
        final Status status = CloudEventValidator.validateId(cloudEvent).toStatus();

        assertEquals(status, ValidationResult.STATUS_SUCCESS);
    }

    @Test
    @DisplayName("Test validate cloudevent id when not UUIDv8 type id")
    void validate_cloud_event_id_when_not_uuidv6_type_id() {
        final java.util.UUID uuid_java = java.util.UUID.randomUUID();
        UUID uuid = UUID.newBuilder().setMsb(uuid_java.getMostSignificantBits())
                .setLsb(uuid_java.getLeastSignificantBits()).build();
        String str_uuid = LongUuidSerializer.instance().serialize(uuid);
        
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH))
                .withId(str_uuid);
        CloudEvent cloudEvent = builder.build();
        final Status status = CloudEventValidator.validateId(cloudEvent).toStatus();
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("Invalid CloudEvent Id [" + str_uuid + "]. CloudEvent Id must be of type UUIDv8.",
                status.getMessage());
    }

    @Test
    @DisplayName("Test validate cloudevent id when not valid")
    void validate_cloud_event_id_when_not_valid() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH))
                .withId("testme");
        CloudEvent cloudEvent = builder.build();
        final Status status = CloudEventValidator.validateId(cloudEvent).toStatus();
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("Invalid CloudEvent Id [testme]. CloudEvent Id must be of type UUIDv8.", status.getMessage());
    }

    @Test
    @DisplayName("Test local Publish type CloudEvent is valid everything is valid")
    void test_publish_type_cloudevent_is_valid_when_everything_is_valid_local() {
        UUID uuid = UuidFactory.Factories.UPROTOCOL.factory().create();
        String str_uuid = LongUuidSerializer.instance().serialize(uuid);
        
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withId(str_uuid)
                .withSource(URI.create("/body.access/1/door.front_left#Door")).withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH));
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.Validators.PUBLISH.validator();
        final Status status = validator.validate(cloudEvent);
        assertEquals(ValidationResult.STATUS_SUCCESS, status);
    }

    @Test
    @DisplayName("Test microRemote Publish type CloudEvent is valid everything is valid")
    void test_publish_type_cloudevent_is_valid_when_everything_is_valid_remote() {
        UUID uuid = UuidFactory.Factories.UPROTOCOL.factory().create();
        String str_uuid = LongUuidSerializer.instance().serialize(uuid);
        
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withId(str_uuid)
                .withSource(URI.create("//VCU.myvin/body.access/1/door.front_left#Door"))
                .withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH));
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.Validators.PUBLISH.validator();
        final Status status = validator.validate(cloudEvent);
        assertEquals(ValidationResult.STATUS_SUCCESS, status);
    }

    @Test
    @DisplayName("Test microRemote Publish type CloudEvent is valid everything is valid with a sink")
    void test_publish_type_cloudevent_is_valid_when_everything_is_valid_remote_with_a_sink() {
        UUID uuid = UuidFactory.Factories.UPROTOCOL.factory().create();
        String str_uuid = LongUuidSerializer.instance().serialize(uuid);
        
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withId(str_uuid)
                .withSource(URI.create("//VCU.myvin/body.access/1/door.front_left#Door"))
                .withExtension("sink", "//bo.cloud/petapp").withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH));
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.Validators.PUBLISH.validator();
        final Status status = validator.validate(cloudEvent);
        assertEquals(ValidationResult.STATUS_SUCCESS, status);
    }

    @Test
    @DisplayName("Test microRemote Publish type CloudEvent is not valid everything is valid with invalid sink")
    void test_publish_type_cloudevent_is_not_valid_when_remote_with_invalid_sink() {
        UUID uuid = UuidFactory.Factories.UPROTOCOL.factory().create();
        String str_uuid = LongUuidSerializer.instance().serialize(uuid);
        
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withId(str_uuid)
                .withSource(URI.create("//VCU.myvin/body.access/1/door.front_left#Door"))
                .withExtension("sink", "//bo.cloud").withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH));
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.Validators.PUBLISH.validator();
        final Status status = validator.validate(cloudEvent);
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("Invalid CloudEvent sink [//bo.cloud]. Uri is missing uSoftware Entity name.",
                status.getMessage());
    }

    @Test
    @DisplayName("Test Publish type CloudEvent is not valid when source is empty")
    void test_publish_type_cloudevent_is_not_valid_when_source_is_empty() {
        UUID uuid = UuidFactory.Factories.UPROTOCOL.factory().create();
        String str_uuid = LongUuidSerializer.instance().serialize(uuid);
        
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withId(str_uuid)
                .withSource(URI.create("/")).withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH));
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.Validators.PUBLISH.validator();
        final Status status = validator.validate(cloudEvent);
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("Invalid Publish type CloudEvent source [/]. Uri is empty.", status.getMessage());
    }

    @Test
    @DisplayName("Test Publish type CloudEvent is not valid when source is invalid and id invalid")
    void test_publish_type_cloudevent_is_not_valid_when_source_is_missing_authority() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withId("testme")
                .withSource(URI.create("/body.access")).withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH));
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.Validators.PUBLISH.validator();
        final Status status = validator.validate(cloudEvent);
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals(
                "Invalid CloudEvent Id [testme]. CloudEvent Id must be of type UUIDv8.," + "Invalid Publish type " +
                        "CloudEvent source [/body.access]. UriPart is missing uResource name.",
                status.getMessage());
    }

    @Test
    @DisplayName("Test Publish type CloudEvent is not valid when source is invalid missing message information")
    void test_publish_type_cloudevent_is_not_valid_when_source_is_missing_message_info() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withId("testme")
                .withSource(URI.create("/body.access/1/door.front_left")).withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH));
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.Validators.PUBLISH.validator();
        final Status status = validator.validate(cloudEvent);
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals(
                "Invalid CloudEvent Id [testme]. CloudEvent Id must be of type UUIDv8.," + "Invalid Publish type " +
                        "CloudEvent source [/body.access/1/door.front_left]. UriPart is missing Message information.",
                status.getMessage());
    }

    @Test
    @DisplayName("Test Notification type CloudEvent is valid everything is valid")
    void test_notification_type_cloudevent_is_valid_when_everything_is_valid() {
        UUID uuid = UuidFactory.Factories.UPROTOCOL.factory().create();
        String str_uuid = LongUuidSerializer.instance().serialize(uuid);
        
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withId(str_uuid)
                .withSource(URI.create("/body.access/1/door.front_left#Door")).withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH))
                .withExtension("sink", "//bo.cloud/petapp");
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.Validators.NOTIFICATION.validator();
        final Status status = validator.validate(cloudEvent);
        assertEquals(ValidationResult.STATUS_SUCCESS, status);
    }

    @Test
    @DisplayName("Test Notification type CloudEvent is not valid missing sink")
    void test_notification_type_cloudevent_is_not_valid_missing_sink() {
        UUID uuid = UuidFactory.Factories.UPROTOCOL.factory().create();
        String str_uuid = LongUuidSerializer.instance().serialize(uuid);
        
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withId(str_uuid)
                .withSource(URI.create("/body.access/1/door.front_left#Door")).withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH));
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.Validators.NOTIFICATION.validator();
        final Status status = validator.validate(cloudEvent);
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("Invalid CloudEvent sink. Notification CloudEvent sink must be an  uri.", status.getMessage());
    }

    @Test
    @DisplayName("Test Notification type CloudEvent is not valid invalid sink")
    void test_notification_type_cloudevent_is_not_valid_invalid_sink() {
        UUID uuid = UuidFactory.Factories.UPROTOCOL.factory().create();
        String str_uuid = LongUuidSerializer.instance().serialize(uuid);
        
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withId(str_uuid)
                .withSource(URI.create("/body.access/1/door.front_left#Door")).withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH))
                .withExtension("sink", "//bo.cloud");
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.Validators.NOTIFICATION.validator();
        final Status status = validator.validate(cloudEvent);
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals("Invalid Notification type CloudEvent sink [//bo.cloud]. Uri is missing uSoftware Entity name.",
                status.getMessage());
    }


    @Test
    @DisplayName("Test Request type CloudEvent is valid everything is valid")
    void test_request_type_cloudevent_is_valid_when_everything_is_valid() {
        UUID uuid = UuidFactory.Factories.UPROTOCOL.factory().create();
        String str_uuid = LongUuidSerializer.instance().serialize(uuid);
        
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withId(str_uuid)
                .withSource(URI.create("//bo.cloud/petapp//rpc.response")).withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_REQUEST))
                .withExtension("sink", "//VCU.myvin/body.access/1/rpc.UpdateDoor");
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.Validators.REQUEST.validator();
        final Status status = validator.validate(cloudEvent);
        assertEquals(ValidationResult.STATUS_SUCCESS, status);
    }

    @Test
    @DisplayName("Test Request type CloudEvent is not valid invalid source")
    void test_request_type_cloudevent_is_not_valid_invalid_source() {
        UUID uuid = UuidFactory.Factories.UPROTOCOL.factory().create();
        String str_uuid = LongUuidSerializer.instance().serialize(uuid);
        
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withId(str_uuid)
                .withSource(URI.create("//bo.cloud/petapp//dog"))
                .withExtension("sink", "//VCU.myvin/body.access/1/rpc.UpdateDoor")
                .withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_REQUEST));
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.Validators.REQUEST.validator();
        final Status status = validator.validate(cloudEvent);
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals(
                "Invalid RPC Request CloudEvent source [//bo.cloud/petapp//dog]. " + "Invalid RPC uri application " +
                        "response topic. UriPart is missing rpc.response.",
                status.getMessage());
    }

    @Test
    @DisplayName("Test Request type CloudEvent is not valid missing sink")
    void test_request_type_cloudevent_is_not_valid_missing_sink() {
        UUID uuid = UuidFactory.Factories.UPROTOCOL.factory().create();
        String str_uuid = LongUuidSerializer.instance().serialize(uuid);
        
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withId(str_uuid)
                .withSource(URI.create("//bo.cloud/petapp//rpc.response")).withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_REQUEST));
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.Validators.REQUEST.validator();
        final Status status = validator.validate(cloudEvent);
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals(
                "Invalid RPC Request CloudEvent sink. Request CloudEvent sink must be uri for the method to be called.",
                status.getMessage());
    }

    @Test
    @DisplayName("Test Request type CloudEvent is not valid sink not rpc command")
    void test_request_type_cloudevent_is_not_valid_invalid_sink_not_rpc_command() {
        UUID uuid = UuidFactory.Factories.UPROTOCOL.factory().create();
        String str_uuid = LongUuidSerializer.instance().serialize(uuid);
        
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withId(str_uuid)
                .withSource(URI.create("//bo.cloud/petapp//rpc.response")).withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_REQUEST))
                .withExtension("sink", "//VCU.myvin/body.access/1/UpdateDoor");
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.Validators.REQUEST.validator();
        final Status status = validator.validate(cloudEvent);
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals(
                "Invalid RPC Request CloudEvent sink [//VCU.myvin/body.access/1/UpdateDoor]. " + "Invalid RPC method " +
                        "uri. UriPart should be the method to be called, or method from response.",
                status.getMessage());
    }

    @Test
    @DisplayName("Test Response type CloudEvent is valid everything is valid")
    void test_response_type_cloudevent_is_valid_when_everything_is_valid() {
        UUID uuid = UuidFactory.Factories.UPROTOCOL.factory().create();
        String str_uuid = LongUuidSerializer.instance().serialize(uuid);
        
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withId(str_uuid)
                .withSource(URI.create("//VCU.myvin/body.access/1/rpc.UpdateDoor"))
                .withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_RESPONSE)).withExtension("sink", "//bo.cloud/petapp//rpc.response");
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.Validators.RESPONSE.validator();
        final Status status = validator.validate(cloudEvent);
        assertEquals(ValidationResult.STATUS_SUCCESS, status);
    }

    @Test
    @DisplayName("Test Response type CloudEvent is not valid invalid source")
    void test_response_type_cloudevent_is_not_valid_invalid_source() {
        UUID uuid = UuidFactory.Factories.UPROTOCOL.factory().create();
        String str_uuid = LongUuidSerializer.instance().serialize(uuid);
        
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withId(str_uuid)
                .withSource(URI.create("//VCU.myvin/body.access/1/UpdateDoor"))
                .withExtension("sink", "//bo.cloud/petapp//rpc.response").withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_RESPONSE));
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.Validators.RESPONSE.validator();
        final Status status = validator.validate(cloudEvent);
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals(
                "Invalid RPC Response CloudEvent source [//VCU.myvin/body.access/1/UpdateDoor]. " + "Invalid RPC " +
                        "method uri. UriPart should be the method to be called, or method from response.",
                status.getMessage());
    }

    @Test
    @DisplayName("Test Response type CloudEvent is not valid missing sink and invalid source")
    void test_response_type_cloudevent_is_not_valid_missing_sink_and_invalid_source() {
        UUID uuid = UuidFactory.Factories.UPROTOCOL.factory().create();
        String str_uuid = LongUuidSerializer.instance().serialize(uuid);
        
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withId(str_uuid)
                .withSource(URI.create("//VCU.myvin/body.access/1/UpdateDoor"))
                .withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_RESPONSE));
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.Validators.RESPONSE.validator();
        final Status status = validator.validate(cloudEvent);
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals(
                "Invalid RPC Response CloudEvent source [//VCU.myvin/body.access/1/UpdateDoor]. " + "Invalid RPC " +
                        "method uri. UriPart should be the method to be called, or method from response.," + "Invalid" +
                        " CloudEvent sink. Response CloudEvent sink must be uri the destination of the response.",
                status.getMessage());
    }

    @Test
    @DisplayName("Test Response type CloudEvent is not valid sink and source, missing entity name.")
    void test_response_type_cloudevent_is_not_valid_invalid_sink() {
        UUID uuid = UuidFactory.Factories.UPROTOCOL.factory().create();
        String str_uuid = LongUuidSerializer.instance().serialize(uuid);
        
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withId(str_uuid)
                .withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_RESPONSE)).withSource(URI.create("//VCU.myvin"))
                .withExtension("sink", "//bo.cloud");
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.Validators.RESPONSE.validator();
        final Status status = validator.validate(cloudEvent);
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals(
                "Invalid RPC Response CloudEvent source [//VCU.myvin]. Invalid RPC method uri. Uri is missing " +
                        "uSoftware Entity name.,Invalid RPC Response CloudEvent sink [//bo.cloud]. Invalid RPC uri " +
                        "application response topic. Uri is missing uSoftware Entity name.",
                status.getMessage());
    }

    @Test
    @DisplayName("Test Response type CloudEvent is not valid source not rpc command")
    void test_response_type_cloudevent_is_not_valid_invalid_source_not_rpc_command() {
        UUID uuid = UuidFactory.Factories.UPROTOCOL.factory().create();
        String str_uuid = LongUuidSerializer.instance().serialize(uuid);
        
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withId(str_uuid)
                .withSource(URI.create("//bo.cloud/petapp/1/dog")).withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_RESPONSE))
                .withExtension("sink", "//VCU.myvin/body.access/1/UpdateDoor");
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.Validators.RESPONSE.validator();
        final Status status = validator.validate(cloudEvent);
        assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
        assertEquals(
                "Invalid RPC Response CloudEvent source [//bo.cloud/petapp/1/dog]. Invalid RPC method uri. UriPart " +
                        "should be the method to be called, or method from response.," + "Invalid RPC Response " +
                        "CloudEvent sink [//VCU.myvin/body.access/1/UpdateDoor]. " + "Invalid RPC uri application " +
                        "response topic. UriPart is missing rpc.response.",
                status.getMessage());
    }

    private CloudEventBuilder buildBaseCloudEventBuilderForTest() {
        // source
        String source = buildLongUriForTest();

        // fake payload
        final Any protoPayload = buildProtoPayloadForTest();

        // additional attributes
        final UCloudEventAttributes uCloudEventAttributes =
                new UCloudEventAttributes.UCloudEventAttributesBuilder().withHash(
                        "somehash").withPriority(UPriority.UPRIORITY_CS1).withTtl(3).withToken(
                                "someOAuthToken")
                .build();

        // build the cloud event
        final CloudEventBuilder cloudEventBuilder = CloudEventFactory.buildBaseCloudEvent("testme", source,
                protoPayload.toByteArray(), protoPayload.getTypeUrl(), uCloudEventAttributes);
        cloudEventBuilder.withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH));

        return cloudEventBuilder;
    }

    private Any buildProtoPayloadForTest() {
        io.cloudevents.v1.proto.CloudEvent cloudEventProto = io.cloudevents.v1.proto.CloudEvent.newBuilder()
                .setSpecVersion("1.0").setId("hello").setSource("/body.access").setType("example.demo")
                .setProtoData(Any.newBuilder().build()).build();
        return Any.pack(cloudEventProto);
    }

    @Test
    @DisplayName("Test create a v6 Cloudevent and validate it works with this SDK")
    public void test_create_a_v6_cloudevent_and_validate_it_against_sdk() {

        // source
        String source = buildLongUriForTest();
        UUID uuid = UuidFactory.Factories.UUIDV6.factory().create();
        String id = LongUuidSerializer.instance().serialize(uuid);
        
        // fake payload
        final Any protoPayload = buildProtoPayloadForTest();

        final UCloudEventAttributes attributes = new UCloudEventAttributes.UCloudEventAttributesBuilder().withPriority(
                        UPriority.UPRIORITY_CS0).withTtl(1000) // live for 1 second
                .build();

        // build the cloud event
        final CloudEvent cloudEvent = CloudEventFactory.buildBaseCloudEvent(id, source, protoPayload.toByteArray(),
                protoPayload.getTypeUrl(), attributes).withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH)).build();

        final CloudEventValidator validator = CloudEventValidator.Validators.PUBLISH.validator();
        final Status status = validator.validate(cloudEvent);
        assertEquals(Code.OK_VALUE, status.getCode());
        assertFalse(UCloudEvent.isExpired(cloudEvent));
    }

    @Test
    @DisplayName("Test create an expired v6 Cloudevent to ensure we report the expiration")
    public void test_create_an_expired_v6_cloudevent() {

        // source
        String source = buildLongUriForTest();
        UUID uuid = UuidFactory.Factories.UUIDV6.factory().create(Instant.now().minusSeconds(100));
        String id = LongUuidSerializer.instance().serialize(uuid);
        
        // fake payload
        final Any protoPayload = buildProtoPayloadForTest();

        final UCloudEventAttributes attributes = new UCloudEventAttributes.UCloudEventAttributesBuilder().withPriority(
                        UPriority.UPRIORITY_CS0).withTtl(1000) // live for 1 second
                .build();

        // build the cloud event
        final CloudEvent cloudEvent = CloudEventFactory.buildBaseCloudEvent(id, source, protoPayload.toByteArray(),
                protoPayload.getTypeUrl(), attributes).withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH)).build();

        final CloudEventValidator validator = CloudEventValidator.Validators.PUBLISH.validator();
        final Status status = validator.validate(cloudEvent);
        assertEquals(Code.OK_VALUE, status.getCode());
        assertTrue(UCloudEvent.isExpired(cloudEvent));
    }


    private String buildLongUriForTest() {
        return LongUriSerializer.instance().serialize(buildUUriForTest());
    }

    private UUri buildUUriForTest() {
        return UUri.newBuilder().setEntity(UEntity.newBuilder().setName("body.access"))
                .setResource(UResource.newBuilder().setName("door").setInstance("front_left").setMessage("Door"))
                .build();
    }

    @Test
    @DisplayName("Test empty event type validator")
    void test_empty_event_type_validator() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withType("");
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.getValidator(cloudEvent);
        final Status status = validator.validateType(cloudEvent).toStatus();
        assertEquals(status.getCode(), 3);
        assertEquals("CloudEventValidator.Publish", validator.toString());
    }
}