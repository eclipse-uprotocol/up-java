/**
 * SPDX-FileCopyrightText: 2024 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.eclipse.uprotocol.cloudevent.validate;

import java.time.Instant;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.protobuf.Any;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;

import org.eclipse.uprotocol.cloudevent.datamodel.UCloudEventAttributes;
import org.eclipse.uprotocol.cloudevent.factory.CloudEventFactory;
import org.eclipse.uprotocol.cloudevent.factory.UCloudEvent;
import org.eclipse.uprotocol.uri.serializer.UriSerializer;
import org.eclipse.uprotocol.uuid.factory.UuidFactory;
import org.eclipse.uprotocol.uuid.serializer.UuidSerializer;
import org.eclipse.uprotocol.v1.UPriority;
import org.eclipse.uprotocol.v1.UUri;
import org.eclipse.uprotocol.v1.UMessageType;
import org.eclipse.uprotocol.v1.UUID;
import org.eclipse.uprotocol.validation.ValidationResult;

class CloudEventValidatorTest {

    private CloudEventBuilder buildBaseCloudEventBuilderForTest() {
        // source
        String source = buildUriForTest();

        // fake payload
        final Any protoPayload = buildProtoPayloadForTest();

        // additional attributes
        final UCloudEventAttributes uCloudEventAttributes =
                new UCloudEventAttributes.UCloudEventAttributesBuilder().withHash(
                        "somehash").withPriority(UPriority.UPRIORITY_CS1).withTtl(3).withToken(
                                "someOAuthToken")
                .build();

        final String id = UuidSerializer.serialize(UuidFactory.Factories.UPROTOCOL.factory().create());
        // build the cloud event
        final CloudEventBuilder cloudEventBuilder = CloudEventFactory.buildBaseCloudEvent(id, source,
                 protoPayload, uCloudEventAttributes);
        cloudEventBuilder.withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH));

        return cloudEventBuilder;
    }

    private Any buildProtoPayloadForTest() {
        io.cloudevents.v1.proto.CloudEvent cloudEventProto = io.cloudevents.v1.proto.CloudEvent.newBuilder()
                .setSpecVersion("1.0").setId("hello").setSource("/1").setType("example.demo")
                .setProtoData(Any.newBuilder().build()).build();
        return Any.pack(cloudEventProto);
    }

    @Test
    @DisplayName("Test create a v6 Cloudevent and validate it works with this SDK")
    public void test_create_a_v6_cloudevent_and_validate_it_against_sdk() {

        // source
        String source = buildUriForTest();
        UUID uuid = UuidFactory.Factories.UUIDV6.factory().create();
        String id = UuidSerializer.serialize(uuid);
        
        // fake payload
        final Any protoPayload = buildProtoPayloadForTest();

        final UCloudEventAttributes attributes = new UCloudEventAttributes.UCloudEventAttributesBuilder().withPriority(
                        UPriority.UPRIORITY_CS0).withTtl(1000) // live for 1 second
                .build();

        // build the cloud event
        final CloudEvent cloudEvent = CloudEventFactory.buildBaseCloudEvent(id, source, protoPayload,
            attributes).withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH)).build();

        final CloudEventValidator validator = CloudEventValidator.Validators.PUBLISH.validator();
        final ValidationResult result = validator.validate(cloudEvent);
        assertTrue(result.isSuccess());
        assertFalse(UCloudEvent.isExpired(cloudEvent));
    }

    @Test
    @DisplayName("Test create an expired v6 Cloudevent to ensure we report the expiration")
    public void test_create_an_expired_v6_cloudevent() {
        // source
        String source = buildUriForTest();
        UUID uuid = UuidFactory.Factories.UUIDV6.factory().create(Instant.now().minusSeconds(100));
        String id = UuidSerializer.serialize(uuid);
        
        // fake payload
        final Any protoPayload = buildProtoPayloadForTest();

        final UCloudEventAttributes attributes = new UCloudEventAttributes.UCloudEventAttributesBuilder().withPriority(
                        UPriority.UPRIORITY_CS0).withTtl(1000) // live for 1 second
                .build();

        // build the cloud event
        final CloudEvent cloudEvent = CloudEventFactory.buildBaseCloudEvent(id, source, protoPayload,
         attributes).withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH)).build();

        final CloudEventValidator validator = CloudEventValidator.Validators.PUBLISH.validator();
        final ValidationResult result = validator.validate(cloudEvent);
        assertTrue(result.isSuccess());
        assertTrue(UCloudEvent.isExpired(cloudEvent));
    }


    private String buildUriForTest() {
        return UriSerializer.serialize(buildUUriForTest());
    }

    private UUri buildUUriForTest() {
        return UUri.newBuilder().setUeId(1).setUeVersionMajor(1)
                .setResourceId(0x8000)
                .build();
    }

    private String buildDefaultTopicUriForTest() {
        return UriSerializer.serialize(UUri.newBuilder().setUeId(1).setUeVersionMajor(1).setResourceId(0).build());
    }

    private String buildMethodUriForTest() {
        return UriSerializer.serialize(UUri.newBuilder().setUeId(1).setUeVersionMajor(1).setResourceId(3).build());
    }


    @Test
    @DisplayName("Test the getValidator to fath the Response validator for a valid response message")
    public void test_getValidator_for_valid_response_type_message() {
        UCloudEventAttributes attributes = new UCloudEventAttributes.UCloudEventAttributesBuilder().withPriority(
                        UPriority.UPRIORITY_CS1).withTtl(1000) // live for 1 second
                .build();
        final CloudEvent cloudEvent = CloudEventFactory.response(
            buildDefaultTopicUriForTest(), buildMethodUriForTest(),
            UuidSerializer.serialize(UuidFactory.Factories.UPROTOCOL.factory().create()), null, attributes);
        final CloudEventValidator validator = CloudEventValidator.getValidator(cloudEvent);
        final ValidationResult result = validator.validate(cloudEvent);
        assertTrue(result.isSuccess());
        assertEquals(validator.toString(), "CloudEventValidator.Response");
    }

    @Test
    @DisplayName("Test the getValidator to fetch the Request validator for a valid request message")
    public void test_getValidator_for_valid_request_type_message() {
        UCloudEventAttributes attributes = new UCloudEventAttributes.UCloudEventAttributesBuilder().withPriority(
                        UPriority.UPRIORITY_CS1).withTtl(1000) // live for 1 second
                .build();
        final CloudEvent cloudEvent = CloudEventFactory.request(buildDefaultTopicUriForTest(), buildMethodUriForTest(), null, attributes);
        final CloudEventValidator validator = CloudEventValidator.getValidator(cloudEvent);
        final ValidationResult result = validator.validate(cloudEvent);
        assertTrue(result.isSuccess());
        assertEquals(validator.toString(), "CloudEventValidator.Request");
    }

    @Test
    @DisplayName("Test the getValidator to fetch the Publish validator for a valid publish message")
    public void test_getValidator_for_valid_publish_type_message() {
        final CloudEvent cloudEvent = buildBaseCloudEventBuilderForTest().build();
        final CloudEventValidator validator = CloudEventValidator.getValidator(cloudEvent);
        final ValidationResult result = validator.validate(cloudEvent);
        assertTrue(result.isSuccess());
        assertEquals(validator.toString(), "CloudEventValidator.Publish");
    }

    @Test
    @DisplayName("Test the getValidator to fetch a validator when type is not set")
    public void test_getValidator_for_invalid_type_message() {
        final CloudEvent cloudEvent = buildBaseCloudEventBuilderForTest().withType("").build();
        final CloudEventValidator validator = CloudEventValidator.getValidator(cloudEvent);
        assertEquals(validator.toString(), "CloudEventValidator.Publish");
    }

    @Test
    @DisplayName("Test validateId when the id is invalid")
    public void test_validateid_when_id_is_invalid() {
        final CloudEvent cloudEvent = buildBaseCloudEventBuilderForTest().withId("bad").build();
        final CloudEventValidator validator = CloudEventValidator.getValidator(cloudEvent);
        final ValidationResult result = validator.validate(cloudEvent);
        assertFalse(result.isSuccess());
        assertEquals(result.getMessage(), "Invalid CloudEvent Id [bad]. CloudEvent Id must be of type UUIDv8.");
    }

    @Test
    @DisplayName("Test the getValidator to fetch the Notification validator for a valid notification message")
    public void test_getValidator_for_valid_notification_type_message() {
        UCloudEventAttributes attributes = new UCloudEventAttributes.UCloudEventAttributesBuilder().withPriority(
                        UPriority.UPRIORITY_CS1).withTtl(1000) // live for 1 second
                .build();
        final CloudEvent cloudEvent = CloudEventFactory.notification(buildUriForTest(), buildDefaultTopicUriForTest(), null, attributes);
        final CloudEventValidator validator = CloudEventValidator.getValidator(cloudEvent);
        final ValidationResult result = validator.validate(cloudEvent);
        assertTrue(result.isSuccess());
        assertEquals(validator.toString(), "CloudEventValidator.Notification");
    }

    @Test
    @DisplayName("Test the Publish Validator with the wrong specification version of 0.3 in lieu of 1.0")
    public void test_publish_validator_for_invalid_spec_version() {
        // source
        final CloudEvent cloudEvent = CloudEventBuilder.v03()
            .withId(UuidSerializer.serialize(UuidFactory.Factories.UPROTOCOL.factory().create()))
            .withSource(URI.create(buildUriForTest()))
            .withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH)).build();
        final CloudEventValidator validator = CloudEventValidator.Validators.PUBLISH.validator();
        final ValidationResult result = validator.validate(cloudEvent);
        assertFalse(result.isSuccess());
        assertEquals(result.getMessage(), "Invalid CloudEvent version [0.3]. CloudEvent version must be 1.0.");
    }

    @Test
    @DisplayName("Test the Publish Validator when sink is passed")
    public void test_publish_validator_for_valid_publish_type_message() {
        // source
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withExtension("sink", URI.create(buildUriForTest()));
        final CloudEvent cloudEvent = builder.build();

        final CloudEventValidator validator = CloudEventValidator.Validators.PUBLISH.validator();
        final ValidationResult result = validator.validate(cloudEvent);
        assertFalse(result.isSuccess());
        assertEquals(result.getMessage(), "Publish should not have a sink");
    }

    @Test
    @DisplayName("Test the Publish Validator when the source has an invalid source topic")
    public void test_publish_validator_for_invalid_source_topic() {
        // source
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withSource(URI.create("invalid-source-topic"));
        final CloudEvent cloudEvent = builder.build();

        final CloudEventValidator validator = CloudEventValidator.Validators.PUBLISH.validator();
        final ValidationResult result = validator.validate(cloudEvent);
        assertFalse(result.isSuccess());
        assertEquals(result.getMessage(), "Invalid Publish type CloudEvent source [invalid-source-topic].");
    }

    @Test
    @DisplayName("Test the Publish Validator when being used to test the wrong type of CloudEvent type")
    public void test_publish_validator_for_invalid_type() {
        // source
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_NOTIFICATION));
        final CloudEvent cloudEvent = builder.build();

        final CloudEventValidator validator = CloudEventValidator.Validators.PUBLISH.validator();
        final ValidationResult result = validator.validate(cloudEvent);
        assertFalse(result.isSuccess());
        assertEquals(result.getMessage(), "Invalid CloudEvent type [not.v1]. CloudEvent of type Publish must have a type of 'pub.v1'");
    }

    @Test
    @DisplayName("Test the Notification Validator with a valid notification message")
    public void test_notification_validator_for_valid_notification_type_message() {

        UCloudEventAttributes attributes = new UCloudEventAttributes.UCloudEventAttributesBuilder().withPriority(
                        UPriority.UPRIORITY_CS1).withTtl(1000) // live for 1 second
                .build();
        final CloudEvent cloudEvent = CloudEventFactory.notification(buildUriForTest(), buildDefaultTopicUriForTest(), null, attributes);

        final CloudEventValidator validator = CloudEventValidator.Validators.NOTIFICATION.validator();
        final ValidationResult result = validator.validate(cloudEvent);
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Test the Notification Validator with an invalid sink")
    public void test_notification_validator_for_invalid_sink() {
        UCloudEventAttributes attributes = new UCloudEventAttributes.UCloudEventAttributesBuilder().withPriority(
                        UPriority.UPRIORITY_CS1).withTtl(1000) // live for 1 second
                .build();
        final CloudEvent cloudEvent = CloudEventFactory.notification(buildUriForTest(), "invalid-sink", null, attributes);

        final CloudEventValidator validator = CloudEventValidator.Validators.NOTIFICATION.validator();
        final ValidationResult result = validator.validate(cloudEvent);
        assertFalse(result.isSuccess());
        assertEquals(result.getMessage(), "Invalid Notification type CloudEvent sink [invalid-sink].");
    }

    @Test
    @DisplayName("Test the Notification Validator with an invalid source")
    public void test_notification_validator_for_invalid_source() {
        UCloudEventAttributes attributes = new UCloudEventAttributes.UCloudEventAttributesBuilder().withPriority(
                        UPriority.UPRIORITY_CS1).withTtl(1000) // live for 1 second
                .build();
        final CloudEvent cloudEvent = CloudEventFactory.notification("invalid-source", buildDefaultTopicUriForTest(), null, attributes);

        final CloudEventValidator validator = CloudEventValidator.Validators.NOTIFICATION.validator();
        final ValidationResult result = validator.validate(cloudEvent);
        assertFalse(result.isSuccess());
        assertEquals(result.getMessage(), "Invalid Notification type CloudEvent source [invalid-source].");
    }

    @Test
    @DisplayName("Test the Notification Validator when sink is null")
    public void test_notification_validator_for_null_sink() {
        UCloudEventAttributes attributes = new UCloudEventAttributes.UCloudEventAttributesBuilder().withPriority(
                        UPriority.UPRIORITY_CS1).withTtl(1000) // live for 1 second
                .build();
        final String id = UuidSerializer.serialize(UuidFactory.Factories.UPROTOCOL.factory().create());
        CloudEvent cloudEvent = CloudEventFactory.buildBaseCloudEvent(id, buildUriForTest(), 
            null, attributes )
            .withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_NOTIFICATION))
            .build();

        final CloudEventValidator validator = CloudEventValidator.Validators.NOTIFICATION.validator();
        final ValidationResult result = validator.validate(cloudEvent);
        assertFalse(result.isSuccess());
        assertEquals(result.getMessage(), "Invalid CloudEvent sink. Notification CloudEvent sink must be an uri.");
    }

    @Test
    @DisplayName("Test the Notification Validator when type is wrong is null")
    public void test_notification_validator_when_type_is_wrong() {
        UCloudEventAttributes attributes = new UCloudEventAttributes.UCloudEventAttributesBuilder().withPriority(
                        UPriority.UPRIORITY_CS1).withTtl(1000) // live for 1 second
                .build();
        final String id = UuidSerializer.serialize(UuidFactory.Factories.UPROTOCOL.factory().create());
        CloudEvent cloudEvent = CloudEventFactory.buildBaseCloudEvent(id, buildUriForTest(), 
            null, attributes )
            .withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH))
            .withExtension("sink", URI.create(buildDefaultTopicUriForTest()))
            .build();

        final CloudEventValidator validator = CloudEventValidator.Validators.NOTIFICATION.validator();
        final ValidationResult result = validator.validate(cloudEvent);
        assertFalse(result.isSuccess());
        assertEquals(result.getMessage(), "Invalid CloudEvent type [pub.v1]. CloudEvent of type Notification must have a type of 'not.v1'");
    }


    @Test
    @DisplayName("Test the Request  Validator happy path")
    public void test_request_validator_for_valid_request_type_message() {
        UCloudEventAttributes attributes = new UCloudEventAttributes.UCloudEventAttributesBuilder().withPriority(
                        UPriority.UPRIORITY_CS1).withTtl(1000) // live for 1 second
                .build();
        final CloudEvent cloudEvent = CloudEventFactory.request(buildDefaultTopicUriForTest(), buildMethodUriForTest(), null, attributes);

        final CloudEventValidator validator = CloudEventValidator.Validators.REQUEST.validator();
        final ValidationResult result = validator.validate(cloudEvent);
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Test the Request Validator with an invalid sink")
    public void test_request_validator_for_invalid_sink() {
        UCloudEventAttributes attributes = new UCloudEventAttributes.UCloudEventAttributesBuilder().withPriority(
                        UPriority.UPRIORITY_CS1).withTtl(1000) // live for 1 second
                .build();
        final CloudEvent cloudEvent = CloudEventFactory.request(buildDefaultTopicUriForTest(), "invalid-sink", null, attributes);

        final CloudEventValidator validator = CloudEventValidator.Validators.REQUEST.validator();
        final ValidationResult result = validator.validate(cloudEvent);
        assertFalse(result.isSuccess());
        assertEquals(result.getMessage(), "Invalid RPC Request type CloudEvent sink [invalid-sink].");
    }

    @Test
    @DisplayName("Test the Request Validator with an invalid source")
    public void test_request_validator_for_invalid_source() {
        UCloudEventAttributes attributes = new UCloudEventAttributes.UCloudEventAttributesBuilder().withPriority(
                        UPriority.UPRIORITY_CS1).withTtl(1000) // live for 1 second
                .build();
        final CloudEvent cloudEvent = CloudEventFactory.request("invalid-source", buildMethodUriForTest(), null, attributes);

        final CloudEventValidator validator = CloudEventValidator.Validators.REQUEST.validator();
        final ValidationResult result = validator.validate(cloudEvent);
        assertFalse(result.isSuccess());
        assertEquals(result.getMessage(), "Invalid RPC Request type CloudEvent source [invalid-source].");
    }

    @Test
    @DisplayName("Test the Request Validator when sink is null")
    public void test_request_validator_for_null_sink() {
        UCloudEventAttributes attributes = new UCloudEventAttributes.UCloudEventAttributesBuilder().withPriority(
                        UPriority.UPRIORITY_CS1).withTtl(1000) // live for 1 second
                .build();
        final String id = UuidSerializer.serialize(UuidFactory.Factories.UPROTOCOL.factory().create());
        CloudEvent cloudEvent = CloudEventFactory.buildBaseCloudEvent(id, buildDefaultTopicUriForTest(), 
            null, attributes )
            .withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_REQUEST))
            .build();

        final CloudEventValidator validator = CloudEventValidator.Validators.REQUEST.validator();
        final ValidationResult result = validator.validate(cloudEvent);
        assertFalse(result.isSuccess());
        assertEquals(result.getMessage(), "Invalid CloudEvent sink. RPC Request CloudEvent sink must be an uri.");
    }

    @Test
    @DisplayName("Test the Request Validator when type is wrong is null")
    public void test_request_validator_when_type_is_wrong() {
        UCloudEventAttributes attributes = new UCloudEventAttributes.UCloudEventAttributesBuilder().withPriority(
                        UPriority.UPRIORITY_CS1).withTtl(1000) // live for 1 second
                .build();
        final String id = UuidSerializer.serialize(UuidFactory.Factories.UPROTOCOL.factory().create());
        CloudEvent cloudEvent = CloudEventFactory.buildBaseCloudEvent(id, buildDefaultTopicUriForTest(), 
            null, attributes )
            .withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH))
            .withExtension("sink", URI.create(buildMethodUriForTest()))
            .build();

        final CloudEventValidator validator = CloudEventValidator.Validators.REQUEST.validator();
        final ValidationResult result = validator.validate(cloudEvent);
        assertFalse(result.isSuccess());
        assertEquals(result.getMessage(), "Invalid CloudEvent type [pub.v1]. CloudEvent of type Request must have a type of 'req.v1'");
    }

    @Test
    @DisplayName("Test the Response Validator happy path")
    public void test_response_validator_for_valid_response_type_message() {
        UCloudEventAttributes attributes = new UCloudEventAttributes.UCloudEventAttributesBuilder().withPriority(
                        UPriority.UPRIORITY_CS1).withTtl(1000) // live for 1 second
                .build();
        final CloudEvent cloudEvent = CloudEventFactory.response(buildDefaultTopicUriForTest(), buildMethodUriForTest(), 
            UuidSerializer.serialize(UuidFactory.Factories.UPROTOCOL.factory().create()), null, attributes);

        final CloudEventValidator validator = CloudEventValidator.Validators.RESPONSE.validator();
        final ValidationResult result = validator.validate(cloudEvent);
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("Test the Response Validator with an invalid sink")
    public void test_response_validator_for_invalid_sink() {
        UCloudEventAttributes attributes = new UCloudEventAttributes.UCloudEventAttributesBuilder().withPriority(
                        UPriority.UPRIORITY_CS1).withTtl(1000) // live for 1 second
                .build();
        final CloudEvent cloudEvent = CloudEventFactory.response("invalid-sink", buildMethodUriForTest(),
            UuidSerializer.serialize(UuidFactory.Factories.UPROTOCOL.factory().create()), null, attributes);

        final CloudEventValidator validator = CloudEventValidator.Validators.RESPONSE.validator();
        final ValidationResult result = validator.validate(cloudEvent);
        assertFalse(result.isSuccess());
        assertEquals(result.getMessage(), "Invalid RPC Response type CloudEvent sink [invalid-sink].");
    }

    @Test
    @DisplayName("Test the Response Validator with an invalid source")
    public void test_response_validator_for_invalid_source() {
        UCloudEventAttributes attributes = new UCloudEventAttributes.UCloudEventAttributesBuilder().withPriority(
                        UPriority.UPRIORITY_CS1).withTtl(1000) // live for 1 second
                .build();
        final CloudEvent cloudEvent = CloudEventFactory.response(buildDefaultTopicUriForTest(), "invalid-source", 
            UuidSerializer.serialize(UuidFactory.Factories.UPROTOCOL.factory().create()), null, attributes);

        final CloudEventValidator validator = CloudEventValidator.Validators.RESPONSE.validator();
        final ValidationResult result = validator.validate(cloudEvent);
        assertFalse(result.isSuccess());
        assertEquals(result.getMessage(), "Invalid RPC Response type CloudEvent source [invalid-source].");
    }

    @Test
    @DisplayName("Test the Response Validator when sink is null")
    public void test_response_validator_for_null_sink() {
        UCloudEventAttributes attributes = new UCloudEventAttributes.UCloudEventAttributesBuilder().withPriority(
                        UPriority.UPRIORITY_CS1).withTtl(1000) // live for 1 second
                .build();
        final String id = UuidSerializer.serialize(UuidFactory.Factories.UPROTOCOL.factory().create());
        CloudEvent cloudEvent = CloudEventFactory.buildBaseCloudEvent(id, buildMethodUriForTest(), 
            null, attributes )
            .withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_RESPONSE))
            .build();

        final CloudEventValidator validator = CloudEventValidator.Validators.RESPONSE.validator();
        final ValidationResult result = validator.validate(cloudEvent);
        assertFalse(result.isSuccess());
        assertEquals(result.getMessage(), "Invalid CloudEvent sink. RPC Response CloudEvent sink must be an uri.");
    }

    @Test
    @DisplayName("Test the Response Validator when type is wrong is null")
    public void test_response_validator_when_type_is_wrong() {
        UCloudEventAttributes attributes = new UCloudEventAttributes.UCloudEventAttributesBuilder().withPriority(
                        UPriority.UPRIORITY_CS1).withTtl(1000) // live for 1 second
                .build();
        final String id = UuidSerializer.serialize(UuidFactory.Factories.UPROTOCOL.factory().create());
        CloudEvent cloudEvent = CloudEventFactory.buildBaseCloudEvent(id, buildMethodUriForTest(), 
             null, attributes )
            .withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH))
            .withExtension("sink", URI.create(buildDefaultTopicUriForTest()))
            .build();

        final CloudEventValidator validator = CloudEventValidator.Validators.RESPONSE.validator();
        final ValidationResult result = validator.validate(cloudEvent);
        assertFalse(result.isSuccess());
        assertEquals(result.getMessage(), "Invalid CloudEvent type [pub.v1]. CloudEvent of type Response must have a type of 'res.v1'");
    }
}
