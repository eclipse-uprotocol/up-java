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
package org.eclipse.uprotocol.transport.builder;

import org.eclipse.uprotocol.v1.UAttributes;
import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UPriority;
import org.eclipse.uprotocol.v1.UUri;
import org.eclipse.uprotocol.v1.UMessage;
import org.eclipse.uprotocol.v1.UMessageType;
import org.eclipse.uprotocol.v1.UPayloadFormat;
import org.eclipse.uprotocol.v1.UUID;
import org.eclipse.uprotocol.communication.UPayload;
import org.eclipse.uprotocol.transport.validator.UAttributesValidator;
import org.eclipse.uprotocol.uuid.factory.UuidFactory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.google.protobuf.ByteString;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UMessageBuilderTest {

    private static final UUri UURI_DEFAULT = UUri.newBuilder()
        .setUeId(1)
        .setUeVersionMajor(1)
        .setResourceId(0)
        .build();

    private static final UUri UURI_METHOD = UUri.newBuilder()
        .setUeId(0x001a_1a5b)
        .setUeVersionMajor(0x04)
        .setResourceId(0x7a5f)
        .build();

    private static final UUri UURI_TOPIC = UUri.newBuilder()
        .setUeId(1)
        .setUeVersionMajor(1)
        .setResourceId(0x8000)
        .build();

    @Test
    void testWithMessageIdPanicsForInvalidUuid() {
        UUID invalidMessageId = UUID.newBuilder()
            .setMsb(0x00000000000000abL)
            .setLsb(0x0000000000018000L)
            .build();
        assertThrows(
            IllegalArgumentException.class, () -> {
                UMessageBuilder.publish(UURI_TOPIC).withMessageId(invalidMessageId);
            });
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
        permLevel, commStatus, token
        # // [utest->dsn~up-attributes-permission-level~1]
        5,         ,
        ,          NOT_FOUND,
        # // [utest->dsn~up-attributes-request-token~1]
        ,          ,           my-token
        """)
    void testPublishMessageBuilderRejectsInvalidAttributes(
        Integer permLevel,
        UCode commStatus,
        String token
    ) {
        var builder = UMessageBuilder.publish(UURI_TOPIC);
        assertBuilderPanics(builder, permLevel, commStatus, token);
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
        permLevel, commStatus, token
        # // [utest->dsn~up-attributes-permission-level~1]
        5,         ,
        ,          NOT_FOUND,
        # // [utest->dsn~up-attributes-request-token~1]
        ,          ,           my-token
        """)
    void testNotificationMessageBuilderRejectsInvalidAttributes(
        Integer permLevel,
        UCode commStatus,
        String token
    ) {
        var builder = UMessageBuilder.notification(UURI_TOPIC, UURI_DEFAULT);
        assertBuilderPanics(builder, permLevel, commStatus, token);
    }

    void assertBuilderPanics(
        UMessageBuilder builder,
        Integer permLevel,
        UCode commStatus,
        String token
    ) {
        if (permLevel != null) {
            assertThrows(
                IllegalStateException.class,
                () -> builder.withPermissionLevel(permLevel)
            );
        } else if (commStatus != null) {
            assertThrows(
                IllegalStateException.class,
                () -> builder.withCommStatus(commStatus)
            );
        } else if (token != null) {
            assertThrows(
                IllegalStateException.class,
                () -> builder.withToken(token)
            );
        }
    }

    @Test
    void testRequestRejectsInvalidTtl() {
        assertThrows(
            IllegalArgumentException.class,
            () -> UMessageBuilder.request(UURI_DEFAULT, UURI_METHOD, 0)
        );
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
        commStatus, priority
        NOT_FOUND,
        # // [utest->dsn~up-attributes-request-priority~1]
        ,          UPRIORITY_UNSPECIFIED
        # // [utest->dsn~up-attributes-request-priority~1]
        ,          UPRIORITY_CS0
        # // [utest->dsn~up-attributes-request-priority~1]
        ,          UPRIORITY_CS1
        # // [utest->dsn~up-attributes-request-priority~1]
        ,          UPRIORITY_CS2
        # // [utest->dsn~up-attributes-request-priority~1]
        ,          UPRIORITY_CS3
        """)
    void testRequestMessageBuilderRejectsInvalidAttributes(
        UCode commStatus,
        UPriority priority
    ) {
        var builder = UMessageBuilder.request(UURI_DEFAULT, UURI_METHOD, 5000);

        if (commStatus != null) {
            assertThrows(
                IllegalStateException.class,
                () -> builder.withCommStatus(commStatus)
            );
        } else if (priority != null) {
            assertThrows(
                IllegalArgumentException.class,
                () -> builder.withPriority(priority)
            );
        }
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
        permLevel, token,    priority
        # // [utest->dsn~up-attributes-permission-level~1]
        5,         ,
        # // [utest->dsn~up-attributes-request-token~1]
        ,          my-token,
        # // [utest->dsn~up-attributes-request-priority~1]
        ,          ,         UPRIORITY_UNSPECIFIED
        # // [utest->dsn~up-attributes-request-priority~1]
        ,          ,         UPRIORITY_CS0
        # // [utest->dsn~up-attributes-request-priority~1]
        ,          ,         UPRIORITY_CS1
        # // [utest->dsn~up-attributes-request-priority~1]
        ,          ,         UPRIORITY_CS2
        # // [utest->dsn~up-attributes-request-priority~1]
        ,          ,         UPRIORITY_CS3
        """)
    void testResponseMessageBuilderRejectsInvalidAttributes(
        Integer permLevel,
        String token,
        UPriority priority
    ) {
        var builder = UMessageBuilder.response(
            UURI_METHOD,
            UURI_DEFAULT,
            UuidFactory.create());

        if (permLevel != null) {
            assertThrows(
                IllegalStateException.class,
                () -> builder.withPermissionLevel(permLevel)
            );
        } else if (token != null) {
            assertThrows(
                IllegalStateException.class,
                () -> builder.withToken(token)
            );
        } else if (priority != null) {
            assertThrows(
                IllegalArgumentException.class,
                () -> builder.withPriority(priority)
            );
        }
    }

    @Test
    void testResponseRejectsNonRequestAttributes() {
        UAttributes attributes = UAttributes.newBuilder()
            .setType(UMessageType.UMESSAGE_TYPE_PUBLISH)
            .setId(UuidFactory.create())
            .setSource(UURI_TOPIC)
            .build();
        UAttributesValidator.getValidator(attributes).validate(attributes);

        assertThrows(
            IllegalArgumentException.class,
            () -> UMessageBuilder.response(attributes)
        );
    }

    @Test
    void testBuildSupportsRepeatedInvocation() {
        UMessageBuilder builder = UMessageBuilder.publish(UURI_TOPIC);
        UMessage messageOne = builder
            .withMessageId(UuidFactory.create())
            .build(UPayload.pack(ByteString.copyFromUtf8("locked"), UPayloadFormat.UPAYLOAD_FORMAT_TEXT));
        UMessage messageTwo = builder
            .withMessageId(UuidFactory.create())
            .build(UPayload.pack(ByteString.copyFromUtf8("unlocked"), UPayloadFormat.UPAYLOAD_FORMAT_TEXT));
        assertEquals(messageOne.getAttributes().getType(), messageTwo.getAttributes().getType());
        assertNotEquals(messageOne.getAttributes().getId(), messageTwo.getAttributes().getId());
        assertEquals(messageOne.getAttributes().getSource(), messageTwo.getAttributes().getSource());
        assertNotEquals(messageOne.getPayload(), messageTwo.getPayload());
    }

    @Test
    // [utest->req~uattributes-data-model-impl~1]
    // [utest->req~umessage-data-model-impl~1]
    void testBuildRetainsAllPublishAttributes() {
        var messageId = UuidFactory.create();
        String traceparent = "traceparent";
        UUri topic = UURI_TOPIC;
        UMessage message = UMessageBuilder.publish(topic)
            .withMessageId(messageId)
            .withTtl(5000)
            .withTraceparent(traceparent)
            .build(UPayload.pack(ByteString.copyFromUtf8("locked"), UPayloadFormat.UPAYLOAD_FORMAT_TEXT));

        // [utest->dsn~up-attributes-id~1]
        assertEquals(messageId, message.getAttributes().getId());
        assertEquals(UPriority.UPRIORITY_UNSPECIFIED, message.getAttributes().getPriority());
        // [utest->dsn~up-attributes-publish-source~1]
        assertEquals(topic, message.getAttributes().getSource());
        // [utest->dsn~up-attributes-publish-sink~1]
        assertFalse(message.getAttributes().hasSink());
        assertEquals(5000, message.getAttributes().getTtl());
        // [utest->dsn~up-attributes-traceparent~1]
        assertEquals(traceparent, message.getAttributes().getTraceparent());
        // [utest->dsn~up-attributes-publish-type~1]
        assertEquals(UMessageType.UMESSAGE_TYPE_PUBLISH, message.getAttributes().getType());
        // [utest->dsn~up-attributes-payload-format~1]
        assertEquals(UPayloadFormat.UPAYLOAD_FORMAT_TEXT, message.getAttributes().getPayloadFormat());

        // [utest->req~uattributes-data-model-proto~1]
        // [utest->req~umessage-data-model-proto~1]
        var proto = message.toByteString();
        assertDoesNotThrow(() -> {
            var deserializedMessage = UMessage.parseFrom(proto);
            assertEquals(message, deserializedMessage);
        });
    }

    @Test
    // [utest->req~uattributes-data-model-impl~1]
    // [utest->req~umessage-data-model-impl~1]
    void testBuildRetainsAllNotificationAttributes() {
        var messageId = UuidFactory.create();
        String traceparent = "traceparent";
        var origin = UURI_TOPIC;
        var destination = UURI_DEFAULT;
        UMessage message = UMessageBuilder.notification(origin, destination)
            .withMessageId(messageId)
            .withPriority(UPriority.UPRIORITY_CS2)
            .withTtl(0xFF00_FFFF) // test unsigned integer
            .withTraceparent(traceparent)
            .build(UPayload.pack(ByteString.copyFromUtf8("locked"), UPayloadFormat.UPAYLOAD_FORMAT_TEXT));

        // [utest->dsn~up-attributes-id~1]
        assertEquals(messageId, message.getAttributes().getId());
        assertEquals(UPriority.UPRIORITY_CS2, message.getAttributes().getPriority());
        assertEquals(origin, message.getAttributes().getSource());
        assertEquals(destination, message.getAttributes().getSink());
        assertEquals(0xFF00_FFFF, message.getAttributes().getTtl());
        // [utest->dsn~up-attributes-traceparent~1]
        assertEquals(traceparent, message.getAttributes().getTraceparent());
        // [utest->dsn~up-attributes-notification-type~1]
        assertEquals(UMessageType.UMESSAGE_TYPE_NOTIFICATION, message.getAttributes().getType());
        // [utest->dsn~up-attributes-payload-format~1]
        assertEquals(UPayloadFormat.UPAYLOAD_FORMAT_TEXT, message.getAttributes().getPayloadFormat());

        // [utest->req~uattributes-data-model-proto~1]
        // [utest->req~umessage-data-model-proto~1]
        var proto = message.toByteString();
        assertDoesNotThrow(() -> {
            var deserializedMessage = UMessage.parseFrom(proto);
            assertEquals(message, deserializedMessage);
        });
    }

    @Test
    // [utest->req~uattributes-data-model-impl~1]
    // [utest->req~umessage-data-model-impl~1]
    void testBuildRetainsAllRequestAttributes() {
        var messageId = UuidFactory.create();
        var token = "token";
        String traceparent = "traceparent";
        var methodToInvoke = UURI_METHOD;
        var replyToAddress = UURI_DEFAULT;
        UMessage message = UMessageBuilder.request(replyToAddress, methodToInvoke, 5000)
            .withMessageId(messageId)
            .withPermissionLevel(0xFF00_FFFF) // test unsigned integer
            .withPriority(UPriority.UPRIORITY_CS4)
            .withToken(token)
            .withTraceparent(traceparent)
            .build(UPayload.pack(ByteString.copyFromUtf8("locked"), UPayloadFormat.UPAYLOAD_FORMAT_TEXT));

        // [utest->dsn~up-attributes-id~1]
        assertEquals(messageId, message.getAttributes().getId());
        // [utest->dsn~up-attributes-permission-level~1]
        assertEquals(0xFF00_FFFF, message.getAttributes().getPermissionLevel());
        assertEquals(UPriority.UPRIORITY_CS4, message.getAttributes().getPriority());
        assertEquals(replyToAddress, message.getAttributes().getSource());
        assertEquals(methodToInvoke, message.getAttributes().getSink());
        // [utest->dsn~up-attributes-request-token~1]
        assertEquals(token, message.getAttributes().getToken());
        assertEquals(5000, message.getAttributes().getTtl());
        // [utest->dsn~up-attributes-traceparent~1]
        assertEquals(traceparent, message.getAttributes().getTraceparent());
        // [utest->dsn~up-attributes-request-type~1]
        assertEquals(UMessageType.UMESSAGE_TYPE_REQUEST, message.getAttributes().getType());
        // [utest->dsn~up-attributes-payload-format~1]
        assertEquals(UPayloadFormat.UPAYLOAD_FORMAT_TEXT, message.getAttributes().getPayloadFormat());

        // [utest->req~uattributes-data-model-proto~1]
        // [utest->req~umessage-data-model-proto~1]
        var proto = message.toByteString();
        assertDoesNotThrow(() -> {
            var deserializedMessage = UMessage.parseFrom(proto);
            assertEquals(message, deserializedMessage);
        });
    }

    @Test
    void testBuilderCopiesRequestAttributes() {
        var requestMessageId = UuidFactory.create();
        var responseMessageId = UuidFactory.create();
        var methodToInvoke = UURI_METHOD;
        var replyToAddress = UURI_DEFAULT;
        UMessage requestMessage = UMessageBuilder.request(replyToAddress, methodToInvoke, 5000)
            .withMessageId(requestMessageId)
            .withPriority(UPriority.UPRIORITY_CS5)
            .build();
        UMessage message = UMessageBuilder.response(requestMessage.getAttributes())
            .withMessageId(responseMessageId)
            .withCommStatus(UCode.DEADLINE_EXCEEDED)
            .build();

        // [utest->dsn~up-attributes-id~1]
        assertEquals(responseMessageId, message.getAttributes().getId());
        assertEquals(UCode.DEADLINE_EXCEEDED, message.getAttributes().getCommstatus());
        assertEquals(UPriority.UPRIORITY_CS5, message.getAttributes().getPriority());
        assertEquals(requestMessageId, message.getAttributes().getReqid());
        // [utest->dsn~up-attributes-response-source~1]
        assertEquals(methodToInvoke, message.getAttributes().getSource());
        // [utest->dsn~up-attributes-response-sink~1]
        assertEquals(replyToAddress, message.getAttributes().getSink());
        assertEquals(5000, message.getAttributes().getTtl());
        // [utest->dsn~up-attributes-request-token~1]
        assertFalse(message.getAttributes().hasToken());
        // [utest->dsn~up-attributes-response-type~1]
        assertEquals(UMessageType.UMESSAGE_TYPE_RESPONSE, message.getAttributes().getType());
        // [utest->dsn~up-attributes-payload-format~1]
        assertEquals(UPayloadFormat.UPAYLOAD_FORMAT_UNSPECIFIED, message.getAttributes().getPayloadFormat());
        assertTrue(message.getPayload().isEmpty());
    }

    @Test
    // [utest->req~uattributes-data-model-impl~1]
    // [utest->req~umessage-data-model-impl~1]
    void testBuildRetainsAllResponseAttributes() {
        var messageId = UuidFactory.create();
        var requestId = UuidFactory.create();
        var traceparent = "traceparent";
        var methodToInvoke = UURI_METHOD;
        var replyToAddress = UURI_DEFAULT;
        UMessage message = UMessageBuilder.response(methodToInvoke, replyToAddress, requestId)
            .withMessageId(messageId)
            .withCommStatus(UCode.DEADLINE_EXCEEDED)
            .withPriority(UPriority.UPRIORITY_CS5)
            .withTtl(4000)
            .withTraceparent(traceparent)
            .build();

        // [utest->dsn~up-attributes-id~1]
        assertEquals(messageId, message.getAttributes().getId());
        assertEquals(UCode.DEADLINE_EXCEEDED, message.getAttributes().getCommstatus());
        assertEquals(UPriority.UPRIORITY_CS5, message.getAttributes().getPriority());
        assertEquals(requestId, message.getAttributes().getReqid());
        // [utest->dsn~up-attributes-response-source~1]
        assertEquals(methodToInvoke, message.getAttributes().getSource());
        // [utest->dsn~up-attributes-response-sink~1]
        assertEquals(replyToAddress, message.getAttributes().getSink());
        assertEquals(4000, message.getAttributes().getTtl());
        // [utest->dsn~up-attributes-traceparent~1]
        assertEquals(traceparent, message.getAttributes().getTraceparent());
        // [utest->dsn~up-attributes-response-type~1]
        assertEquals(UMessageType.UMESSAGE_TYPE_RESPONSE, message.getAttributes().getType());
        // [utest->dsn~up-attributes-payload-format~1]
        assertEquals(UPayloadFormat.UPAYLOAD_FORMAT_UNSPECIFIED, message.getAttributes().getPayloadFormat());
        assertTrue(message.getPayload().isEmpty());

        // [utest->req~uattributes-data-model-proto~1]
        // [utest->req~umessage-data-model-proto~1]
        var proto = message.toByteString();
        assertDoesNotThrow(() -> {
            var deserializedMessage = UMessage.parseFrom(proto);
            assertEquals(message, deserializedMessage);
        });
    }
}
