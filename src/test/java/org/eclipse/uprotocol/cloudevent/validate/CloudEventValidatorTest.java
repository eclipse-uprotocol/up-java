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

import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventAttributes;
import io.cloudevents.core.builder.CloudEventBuilder;
import org.eclipse.uprotocol.cloudevent.datamodel.UCloudEventAttributes;
import org.eclipse.uprotocol.cloudevent.factory.CloudEventFactory;
import org.eclipse.uprotocol.cloudevent.factory.UCloudEvent;
import org.eclipse.uprotocol.uri.serializer.UriSerializer;
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
                .setSpecVersion("1.0").setId("hello").setSource("/1").setType("example.demo")
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
        final ValidationResult result = validator.validate(cloudEvent);
        assertTrue(result.isSuccess());
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
        final ValidationResult result = validator.validate(cloudEvent);
        assertTrue(result.isSuccess());
        assertTrue(UCloudEvent.isExpired(cloudEvent));
    }


    private String buildLongUriForTest() {
        return UriSerializer.serialize(buildUUriForTest());
    }

    private UUri buildUUriForTest() {
        return UUri.newBuilder().setUeId(1)
                .setResourceId(0x8000)
                .build();
    }

    @Test
    @DisplayName("Test empty event type validator")
    void test_empty_event_type_validator() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest().withType("");
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.getValidator(cloudEvent);
        final UStatus status = validator.validateType(cloudEvent).toStatus();
        assertEquals(status.getCode(), UCode.INVALID_ARGUMENT);
        assertEquals("CloudEventValidator.Publish", validator.toString());
    }

    @Test
    @DisplayName("Test fetching the notification validator")
    void test_fetching_the_notification_validator() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest();
        builder.withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_NOTIFICATION));
        CloudEvent cloudEvent = builder.build();
        final CloudEventValidator validator = CloudEventValidator.getValidator(cloudEvent);
        final UStatus status = validator.validateType(cloudEvent).toStatus();
        assertEquals(status, ValidationResult.STATUS_SUCCESS);
        assertEquals("CloudEventValidator.Notification", validator.toString());
    }

}