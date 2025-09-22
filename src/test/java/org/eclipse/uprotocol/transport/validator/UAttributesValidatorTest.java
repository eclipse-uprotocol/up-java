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
package org.eclipse.uprotocol.transport.validator;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Stream;

import org.eclipse.uprotocol.v1.UAttributes;
import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UMessageType;
import org.eclipse.uprotocol.v1.UPriority;
import org.eclipse.uprotocol.v1.UUID;
import org.eclipse.uprotocol.transport.builder.UMessageBuilder;
import org.eclipse.uprotocol.uuid.factory.UuidFactory;
import org.eclipse.uprotocol.v1.UUri;
import org.eclipse.uprotocol.validation.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

public class UAttributesValidatorTest {

    private static final UUri UURI_DEFAULT = UUri.newBuilder().setUeId(1).setUeVersionMajor(1).setResourceId(0).build();

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

    private static final UUri UURI_WILDCARD_RESOURCE = UUri.newBuilder()
        .setUeId(1)
        .setUeVersionMajor(1)
        .setResourceId(0xFFFF)
        .build();

    private static final UUID UUID_INVALID = UUID.newBuilder()
        .setMsb(0x000000000001C000)
        .setLsb(0x0000000000000000)
        .build();

    @Test
    void testValidateTypeFailsForUnexpectedTypeCode() {
        UAttributes attributes = UAttributes.newBuilder()
            .setType(UMessageType.UMESSAGE_TYPE_UNSPECIFIED)
            .build();
        assertThrows(
            ValidationException.class,
            () -> UAttributesValidator.Validators.PUBLISH.validator().validate(attributes));
        assertThrows(
            ValidationException.class,
            () -> UAttributesValidator.Validators.NOTIFICATION.validator().validate(attributes));
        assertThrows(
            ValidationException.class,
            () -> UAttributesValidator.Validators.REQUEST.validator().validate(attributes));
        assertThrows(
            ValidationException.class,
            () -> UAttributesValidator.Validators.RESPONSE.validator().validate(attributes));
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
        messageType,                               expectedValidatorType
        # succeeds for Unspecified message
        UMESSAGE_TYPE_UNSPECIFIED,                 UMESSAGE_TYPE_PUBLISH
        # succeeds for Publish message
        UMESSAGE_TYPE_PUBLISH,                     UMESSAGE_TYPE_PUBLISH
        # succeeds for Notification message
        UMESSAGE_TYPE_NOTIFICATION,                UMESSAGE_TYPE_NOTIFICATION
        # succeeds for Request message
        UMESSAGE_TYPE_REQUEST,                     UMESSAGE_TYPE_REQUEST
        # succeeds for Response message
        UMESSAGE_TYPE_RESPONSE,                    UMESSAGE_TYPE_RESPONSE
        """)
    void testGetValidatorReturnsMatchingValidator(
        UMessageType messageType,
        UMessageType expectedValidatorType
    ) {
        var validator = UAttributesValidator.getValidator(messageType);
        assertEquals(expectedValidatorType, validator.messageType());
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
        TTL,    expectedIsExpired
        0,      false
        10000,  true
        29999,  true
        30000,  true
        40000,  false
        """)
    void testIsExpired(int ttl, boolean expectedIsExpired) {
        var now = Instant.now();
        var uuid = UuidFactory.create(now.minusMillis(30_000));
        var message = UMessageBuilder.publish(UURI_TOPIC)
            .withMessageId(uuid)
            .withTtl(ttl)
            .build();

        var validator = UAttributesValidator.getValidator(message.getAttributes());
        assertEquals(expectedIsExpired, validator.isExpired(message.getAttributes()));
    }

    static Stream<Arguments> publishMessageArgProvider() {
        return Stream.of(
            Arguments.of(
                Optional.of(UuidFactory.create()),
                Optional.of(UURI_TOPIC),
                Optional.empty(),
                OptionalInt.empty(),
                true
            ),
            // fails for message containing destination
            // [utest->dsn~up-attributes-publish-sink~1]
            Arguments.of(
                Optional.of(UuidFactory.create()),
                Optional.of(UURI_TOPIC),
                Optional.of(UURI_DEFAULT),
                OptionalInt.empty(),
                false
            ),
            // succeeds for valid attributes
            Arguments.of(
                Optional.of(UuidFactory.create()),
                Optional.of(UURI_TOPIC),
                Optional.empty(),
                OptionalInt.of(100),
                true
            ),
            // fails for missing topic
            // [utest->dsn~up-attributes-publish-source~1]
            Arguments.of(
                Optional.of(UuidFactory.create()),
                Optional.empty(),
                Optional.empty(),
                OptionalInt.empty(),
                false
            ),
            // fails for invalid topic
            // [utest->dsn~up-attributes-publish-source~1]
            Arguments.of(
                Optional.of(UuidFactory.create()),
                Optional.of(UURI_METHOD),
                Optional.empty(),
                OptionalInt.empty(),
                false
            ),
            // fails for source with wildcard
            // [utest->dsn~up-attributes-publish-source~1]
            Arguments.of(
                Optional.of(UuidFactory.create()),
                Optional.of(UURI_WILDCARD_RESOURCE),
                Optional.empty(),
                OptionalInt.empty(),
                false
            ),
            // fails for missing message ID
            // [utest->dsn~up-attributes-id~1]
            Arguments.of(
                Optional.empty(),
                Optional.of(UURI_TOPIC),
                Optional.empty(),
                OptionalInt.empty(),
                false
            ),
            // fails for invalid message ID
            // [utest->dsn~up-attributes-id~1]
            Arguments.of(
                Optional.of(UUID_INVALID),
                Optional.of(UURI_TOPIC),
                Optional.empty(),
                OptionalInt.empty(),
                false
            ),
            // fails for invalid (negative) TTL
            Arguments.of(
                Optional.of(UUID_INVALID),
                Optional.of(UURI_TOPIC),
                Optional.empty(),
                OptionalInt.of(-1),
                false
            ));
    }
    @ParameterizedTest
    @MethodSource("publishMessageArgProvider")
    void testValidateAttributesForPublishMessage(
        Optional<UUID> id,
        Optional<UUri> source,
        Optional<UUri> sink,
        OptionalInt ttl,
        boolean shouldSucceed
    ) {
        var attribsBuilder = UAttributes.newBuilder();
        attribsBuilder.setType(UMessageType.UMESSAGE_TYPE_PUBLISH);
        id.ifPresent(attribsBuilder::setId);
        source.ifPresent(attribsBuilder::setSource);
        sink.ifPresent(attribsBuilder::setSink);
        ttl.ifPresent(attribsBuilder::setTtl);
        var attribs = attribsBuilder.build();
        if (shouldSucceed) {
            UAttributesValidator.Validators.PUBLISH.validator().validate(attribs);
            assertThrows(
                ValidationException.class,
                () -> UAttributesValidator.Validators.NOTIFICATION.validator().validate(attribs)
            );
            assertThrows(
                ValidationException.class,
                () -> UAttributesValidator.Validators.REQUEST.validator().validate(attribs)
            );
            assertThrows(
                ValidationException.class,
                () -> UAttributesValidator.Validators.RESPONSE.validator().validate(attribs)
            );
        } else {
            assertThrows(
                ValidationException.class,
                () -> UAttributesValidator.Validators.PUBLISH.validator().validate(attribs));
        }
    }

    static Stream<Arguments> notificationMessageArgProvider() {
        return Stream.of(
            // succeeds for both origin and destination
            Arguments.of(
                Optional.of(UuidFactory.create()),
                Optional.of(UURI_TOPIC),
                Optional.of(UURI_DEFAULT),
                OptionalInt.empty(),
                OptionalInt.empty(),
                true
            ),
            // succeeds for valid attributes
            Arguments.of(
                Optional.of(UuidFactory.create()),
                Optional.of(UURI_TOPIC),
                Optional.of(UURI_DEFAULT),
                OptionalInt.of(100),
                OptionalInt.of(UPriority.UPRIORITY_CS1_VALUE),
                true
            ),
            // fails for missing destination
            // [utest->dsn~up-attributes-notification-sink~1]
            Arguments.of(
                Optional.of(UuidFactory.create()),
                Optional.of(UURI_TOPIC),
                Optional.empty(),
                OptionalInt.empty(),
                OptionalInt.empty(),
                false
            ),
            // fails for missing origin
            // [utest->dsn~up-attributes-notification-source~1]
            Arguments.of(
                Optional.of(UuidFactory.create()),
                Optional.empty(),
                Optional.of(UURI_DEFAULT),
                OptionalInt.empty(),
                OptionalInt.empty(),
                false
            ),
            // fails for invalid origin
            // [utest->dsn~up-attributes-notification-source~1]
            Arguments.of(
                Optional.of(UuidFactory.create()),
                Optional.of(UURI_METHOD),
                Optional.of(UURI_DEFAULT),
                OptionalInt.empty(),
                OptionalInt.empty(),
                false
            ),
            // fails for origin with wildcard
            // [utest->dsn~up-attributes-notification-source~1]
            Arguments.of(
                Optional.of(UuidFactory.create()),
                Optional.of(UURI_WILDCARD_RESOURCE),
                Optional.of(UURI_DEFAULT),
                OptionalInt.empty(),
                OptionalInt.empty(),
                false
            ),
            // fails for invalid destination
            // [utest->dsn~up-attributes-notification-sink~1]
            Arguments.of(
                Optional.of(UuidFactory.create()),
                Optional.of(UURI_TOPIC),
                Optional.of(UURI_METHOD),
                OptionalInt.empty(),
                OptionalInt.empty(),
                false
            ),
            // fails for destination with wildcard
            // [utest->dsn~up-attributes-notification-sink~1]
            Arguments.of(
                Optional.of(UuidFactory.create()),
                Optional.of(UURI_TOPIC),
                Optional.of(UURI_WILDCARD_RESOURCE),
                OptionalInt.empty(),
                OptionalInt.empty(),
                false
            ),
            // fails for neither origin nor destination
            // [utest->dsn~up-attributes-notification-source~1]
            // [utest->dsn~up-attributes-notification-sink~1]
            Arguments.of(
                Optional.of(UuidFactory.create()),
                Optional.empty(),
                Optional.empty(),
                OptionalInt.empty(),
                OptionalInt.empty(),
                false
            ),
            // fails for unknown priority
            Arguments.of(
                Optional.empty(),
                Optional.of(UURI_TOPIC),
                Optional.of(UURI_DEFAULT),
                OptionalInt.empty(),
                OptionalInt.of(20),
                false
            ),
            // fails for missing message ID
            // [utest->dsn~up-attributes-id~1]
            Arguments.of(
                Optional.empty(),
                Optional.of(UURI_TOPIC),
                Optional.of(UURI_DEFAULT),
                OptionalInt.empty(),
                OptionalInt.empty(),
                false
            ),
            // fails for invalid message ID
            // [utest->dsn~up-attributes-id~1]
            Arguments.of(
                Optional.of(UUID_INVALID),
                Optional.of(UURI_TOPIC),
                Optional.of(UURI_DEFAULT),
                OptionalInt.empty(),
                OptionalInt.empty(),
                false
            ));
    }

    @ParameterizedTest
    @MethodSource("notificationMessageArgProvider")
    void testValidateAttributesForNotificationMessage(
        Optional<UUID> id,
        Optional<UUri> source,
        Optional<UUri> sink,
        OptionalInt ttl,
        OptionalInt priority,
        boolean shouldSucceed
    ) {
        var attribsBuilder = UAttributes.newBuilder();
        attribsBuilder.setType(UMessageType.UMESSAGE_TYPE_NOTIFICATION);
        id.ifPresent(attribsBuilder::setId);
        source.ifPresent(attribsBuilder::setSource);
        sink.ifPresent(attribsBuilder::setSink);
        ttl.ifPresent(attribsBuilder::setTtl);
        priority.ifPresent(attribsBuilder::setPriorityValue);
        var attribs = attribsBuilder.build();
        if (shouldSucceed) {
            UAttributesValidator.Validators.NOTIFICATION.validator().validate(attribs);
            assertThrows(
                ValidationException.class,
                () -> UAttributesValidator.Validators.PUBLISH.validator().validate(attribs)
            );
            assertThrows(
                ValidationException.class,
                () -> UAttributesValidator.Validators.REQUEST.validator().validate(attribs)
            );
            assertThrows(
                ValidationException.class,
                () -> UAttributesValidator.Validators.RESPONSE.validator().validate(attribs)
            );
        } else {
            assertThrows(
                ValidationException.class,
                () -> UAttributesValidator.Validators.NOTIFICATION.validator().validate(attribs));
        }
    }

    static Stream<Arguments> requestMessageArgProvider() {
        return Stream.of(
            // succeeds for mandatory attributes
            Arguments.of(
                Optional.of(UuidFactory.create()),
                Optional.of(UURI_METHOD),
                Optional.of(UURI_DEFAULT),
                OptionalInt.empty(),
                OptionalInt.of(2000),
                OptionalInt.of(UPriority.UPRIORITY_CS4_VALUE),
                Optional.empty(),
                true),
            // succeeds for valid attributes
            Arguments.of(
                Optional.of(UuidFactory.create()),
                Optional.of(UURI_METHOD),
                Optional.of(UURI_DEFAULT),
                OptionalInt.of(1),
                OptionalInt.of(2000),
                OptionalInt.of(UPriority.UPRIORITY_CS4_VALUE),
                Optional.of("mytoken"),
                true),
            // fails for missing message ID
            // [utest->dsn~up-attributes-id~1]
            Arguments.of(
                Optional.empty(),
                Optional.of(UURI_METHOD),
                Optional.of(UURI_DEFAULT),
                OptionalInt.of(1),
                OptionalInt.of(2000),
                OptionalInt.of(UPriority.UPRIORITY_CS4_VALUE),
                Optional.of("mytoken"),
                false),
            // fails for invalid message ID
            // [utest->dsn~up-attributes-id~1]
            Arguments.of(
                Optional.of(UUID_INVALID),
                Optional.of(UURI_METHOD),
                Optional.of(UURI_DEFAULT),
                OptionalInt.empty(),
                OptionalInt.of(2000),
                OptionalInt.of(UPriority.UPRIORITY_CS4_VALUE),
                Optional.empty(),
                false),
            // fails for missing reply-to-address
            // [utest->dsn~up-attributes-request-source~1]
            Arguments.of(
                Optional.of(UuidFactory.create()),
                Optional.of(UURI_METHOD),
                Optional.empty(),
                OptionalInt.empty(),
                OptionalInt.of(2000),
                OptionalInt.of(UPriority.UPRIORITY_CS4_VALUE),
                Optional.empty(),
                false),
            // fails for invalid reply-to-address
            // [utest->dsn~up-attributes-request-source~1]
            Arguments.of(
                Optional.of(UuidFactory.create()),
                Optional.of(UURI_METHOD),
                Optional.of(UURI_TOPIC),
                OptionalInt.empty(),
                OptionalInt.of(2000),
                OptionalInt.of(UPriority.UPRIORITY_CS4_VALUE),
                Optional.empty(),
                false),
            // fails for reply-to-address with wildcard
            // [utest->dsn~up-attributes-request-source~1]
            Arguments.of(
                Optional.of(UuidFactory.create()),
                Optional.of(UURI_METHOD),
                Optional.of(UURI_WILDCARD_RESOURCE),
                OptionalInt.empty(),
                OptionalInt.of(2000),
                OptionalInt.of(UPriority.UPRIORITY_CS4_VALUE),
                Optional.empty(),
                false),
            // fails for missing method-to-invoke
            // [utest->dsn~up-attributes-request-sink~1]
            Arguments.of(
                Optional.of(UuidFactory.create()),
                Optional.empty(),
                Optional.of(UURI_DEFAULT),
                OptionalInt.empty(),
                OptionalInt.of(2000),
                OptionalInt.of(UPriority.UPRIORITY_CS4_VALUE),
                Optional.empty(),
                false),
            // fails for invalid method-to-invoke
            // [utest->dsn~up-attributes-request-sink~1]
            Arguments.of(
                Optional.of(UuidFactory.create()),
                Optional.of(UURI_TOPIC),
                Optional.of(UURI_DEFAULT),
                OptionalInt.empty(),
                OptionalInt.of(2000),
                OptionalInt.of(UPriority.UPRIORITY_CS4_VALUE),
                Optional.empty(),
                false),
            // fails for method-to-invoke with wildcard
            // [utest->dsn~up-attributes-request-sink~1]
            Arguments.of(
                Optional.of(UuidFactory.create()),
                Optional.of(UURI_WILDCARD_RESOURCE),
                Optional.of(UURI_DEFAULT),
                OptionalInt.empty(),
                OptionalInt.of(2000),
                OptionalInt.of(UPriority.UPRIORITY_CS4_VALUE),
                Optional.empty(),
                false),
            // fails for missing priority
            Arguments.of(
                Optional.of(UuidFactory.create()),
                Optional.of(UURI_METHOD),
                Optional.of(UURI_DEFAULT),
                OptionalInt.of(1),
                OptionalInt.of(2000),
                OptionalInt.empty(),
                Optional.empty(),
                false),
            // fails for invalid priority
            Arguments.of(
                Optional.of(UuidFactory.create()),
                Optional.of(UURI_METHOD),
                Optional.of(UURI_DEFAULT),
                OptionalInt.of(1),
                OptionalInt.of(2000),
                OptionalInt.of(UPriority.UPRIORITY_CS3_VALUE),
                Optional.empty(),
                false),
            // fails for unknown priority
            Arguments.of(
                Optional.of(UuidFactory.create()),
                Optional.of(UURI_METHOD),
                Optional.of(UURI_DEFAULT),
                OptionalInt.of(1),
                OptionalInt.of(2000),
                OptionalInt.of(20),
                Optional.empty(),
                false),
            // fails for missing ttl
            // [utest->dsn~up-attributes-request-ttl~1]
            Arguments.of(
                Optional.of(UuidFactory.create()),
                Optional.of(UURI_METHOD),
                Optional.of(UURI_DEFAULT),
                OptionalInt.empty(),
                OptionalInt.empty(),
                OptionalInt.of(UPriority.UPRIORITY_CS4_VALUE),
                Optional.empty(),
                false),
            // fails for ttl = 0
            // [utest->dsn~up-attributes-request-ttl~1]
            Arguments.of(
                Optional.of(UuidFactory.create()),
                Optional.of(UURI_METHOD),
                Optional.of(UURI_DEFAULT),
                OptionalInt.empty(),
                OptionalInt.of(0),
                OptionalInt.of(UPriority.UPRIORITY_CS4_VALUE),
                Optional.empty(),
                false),
            // fails for invalid (negative) permission level
            Arguments.of(
                Optional.of(UuidFactory.create()),
                Optional.of(UURI_METHOD),
                Optional.of(UURI_DEFAULT),
                OptionalInt.of(-1),
                OptionalInt.of(2000),
                OptionalInt.of(UPriority.UPRIORITY_CS4_VALUE),
                Optional.empty(),
                false)
        );
    }

    @ParameterizedTest
    @MethodSource("requestMessageArgProvider")
    void testValidateAttributesForRpcRequestMessage(
        Optional<UUID> id,
        Optional<UUri> methodToInvoke,
        Optional<UUri> replyToAddress,
        OptionalInt permLevel,
        OptionalInt ttl,
        OptionalInt priority,
        Optional<String> token,
        boolean shouldSucceed
    ) {
        var attribsBuilder = UAttributes.newBuilder();
        attribsBuilder.setType(UMessageType.UMESSAGE_TYPE_REQUEST);
        id.ifPresent(attribsBuilder::setId);
        methodToInvoke.ifPresent(attribsBuilder::setSink);
        replyToAddress.ifPresent(attribsBuilder::setSource);
        permLevel.ifPresent(attribsBuilder::setPermissionLevel);
        ttl.ifPresent(attribsBuilder::setTtl);
        priority.ifPresent(attribsBuilder::setPriorityValue);
        token.ifPresent(attribsBuilder::setToken);
        var attribs = attribsBuilder.build();
        if (shouldSucceed) {
            UAttributesValidator.Validators.REQUEST.validator().validate(attribs);
            assertThrows(
                ValidationException.class,
                () -> UAttributesValidator.Validators.PUBLISH.validator().validate(attribs)
            );
            assertThrows(
                ValidationException.class,
                () -> UAttributesValidator.Validators.NOTIFICATION.validator().validate(attribs)
            );
            assertThrows(
                ValidationException.class,
                () -> UAttributesValidator.Validators.RESPONSE.validator().validate(attribs)
            );
        } else {
            assertThrows(
                ValidationException.class,
                () -> UAttributesValidator.Validators.REQUEST.validator().validate(attribs));
        }
    }

    static Stream<Arguments> responseMessageArgProvider() {
        return Stream.of(
            // succeeds for mandatory attributes
            Arguments.of(
                Optional.of(UuidFactory.create()),
                Optional.of(UURI_DEFAULT),
                Optional.of(UURI_METHOD),
                Optional.of(UuidFactory.create()),
                OptionalInt.empty(),
                OptionalInt.empty(),
                Optional.of(UPriority.UPRIORITY_CS4),
                true),
            // succeeds for valid attributes
            Arguments.of(
                Optional.of(UuidFactory.create()),
                Optional.of(UURI_DEFAULT),
                Optional.of(UURI_METHOD),
                Optional.of(UuidFactory.create()),
                OptionalInt.of(UCode.CANCELLED_VALUE),
                OptionalInt.of(100),
                Optional.of(UPriority.UPRIORITY_CS4),
                true),
            // fails for missing message ID
            // [utest->dsn~up-attributes-id~1]
            Arguments.of(
                Optional.empty(),
                Optional.of(UURI_DEFAULT),
                Optional.of(UURI_METHOD),
                Optional.of(UuidFactory.create()),
                OptionalInt.of(UCode.CANCELLED_VALUE),
                OptionalInt.of(100),
                Optional.of(UPriority.UPRIORITY_CS4),
                false),
            // fails for invalid message ID
            // [utest->dsn~up-attributes-id~1]
            Arguments.of(
                Optional.of(UUID_INVALID),
                Optional.of(UURI_DEFAULT),
                Optional.of(UURI_METHOD),
                Optional.of(UuidFactory.create()),
                OptionalInt.empty(),
                OptionalInt.empty(),
                Optional.of(UPriority.UPRIORITY_CS4),
                false),
            // fails for missing reply-to-address
            // [utest->dsn~up-attributes-response-sink~1]
            Arguments.of(
                Optional.of(UuidFactory.create()),
                Optional.empty(),
                Optional.of(UURI_METHOD),
                Optional.of(UuidFactory.create()),
                OptionalInt.empty(),
                OptionalInt.empty(),
                Optional.of(UPriority.UPRIORITY_CS4),
                false),
            // fails for invalid reply-to-address
            // [utest->dsn~up-attributes-response-sink~1]
            Arguments.of(
                Optional.of(UuidFactory.create()),
                Optional.of(UURI_TOPIC),
                Optional.of(UURI_METHOD),
                Optional.of(UuidFactory.create()),
                OptionalInt.empty(),
                OptionalInt.empty(),
                Optional.of(UPriority.UPRIORITY_CS4),
                false),
            // fails for reply-to-address with wildcard
            // [utest->dsn~up-attributes-response-sink~1]
            Arguments.of(
                Optional.of(UuidFactory.create()),
                Optional.of(UURI_WILDCARD_RESOURCE),
                Optional.of(UURI_METHOD),
                Optional.of(UuidFactory.create()),
                OptionalInt.empty(),
                OptionalInt.empty(),
                Optional.of(UPriority.UPRIORITY_CS4),
                false),
            // fails for missing invoked-method
            // [utest->dsn~up-attributes-response-source~1]
            Arguments.of(
                Optional.of(UuidFactory.create()),
                Optional.of(UURI_DEFAULT),
                Optional.empty(),
                Optional.of(UuidFactory.create()),
                OptionalInt.empty(),
                OptionalInt.empty(),
                Optional.of(UPriority.UPRIORITY_CS4),
                false),
            // fails for invalid invoked-method
            // [utest->dsn~up-attributes-response-source~1]
            Arguments.of(
                Optional.of(UuidFactory.create()),
                Optional.of(UURI_DEFAULT),
                Optional.of(UURI_TOPIC),
                Optional.of(UuidFactory.create()),
                OptionalInt.empty(),
                OptionalInt.empty(),
                Optional.of(UPriority.UPRIORITY_CS4),
                false),
            // fails for invoked-method with wildcard
            // [utest->dsn~up-attributes-response-source~1]
            Arguments.of(
                Optional.of(UuidFactory.create()),
                Optional.of(UURI_DEFAULT),
                Optional.of(UURI_WILDCARD_RESOURCE),
                Optional.of(UuidFactory.create()),
                OptionalInt.empty(),
                OptionalInt.empty(),
                Optional.of(UPriority.UPRIORITY_CS4),
                false),
            // fails for invalid commstatus
            Arguments.of(
                Optional.of(UuidFactory.create()),
                Optional.of(UURI_DEFAULT),
                Optional.of(UURI_METHOD),
                Optional.of(UuidFactory.create()),
                OptionalInt.of(-189),
                OptionalInt.empty(),
                Optional.of(UPriority.UPRIORITY_CS4),
                false),
            // succeeds for ttl > 0
            Arguments.of(
                Optional.of(UuidFactory.create()),
                Optional.of(UURI_DEFAULT),
                Optional.of(UURI_METHOD),
                Optional.of(UuidFactory.create()),
                OptionalInt.empty(),
                OptionalInt.of(100),
                Optional.of(UPriority.UPRIORITY_CS4),
                true),
            // succeeds for ttl = 0
            Arguments.of(
                Optional.of(UuidFactory.create()),
                Optional.of(UURI_DEFAULT),
                Optional.of(UURI_METHOD),
                Optional.of(UuidFactory.create()),
                OptionalInt.empty(),
                OptionalInt.of(0),
                Optional.of(UPriority.UPRIORITY_CS4),
                true),
            // fails for missing priority
            Arguments.of(
                Optional.of(UuidFactory.create()),
                Optional.of(UURI_DEFAULT),
                Optional.of(UURI_METHOD),
                Optional.of(UuidFactory.create()),
                OptionalInt.of(UCode.CANCELLED_VALUE),
                OptionalInt.of(100),
                Optional.empty(),
                false),
            // fails for invalid priority
            Arguments.of(
                Optional.of(UuidFactory.create()),
                Optional.of(UURI_DEFAULT),
                Optional.of(UURI_METHOD),
                Optional.of(UuidFactory.create()),
                OptionalInt.of(UCode.CANCELLED_VALUE),
                OptionalInt.of(100),
                Optional.of(UPriority.UPRIORITY_CS3),
                false),
            // fails for missing request ID
            Arguments.of(
                Optional.of(UuidFactory.create()),
                Optional.of(UURI_DEFAULT),
                Optional.of(UURI_METHOD),
                Optional.empty(),
                OptionalInt.empty(),
                OptionalInt.empty(),
                Optional.of(UPriority.UPRIORITY_CS4),
                false),
            // fails for invalid request ID
            Arguments.of(
                Optional.of(UuidFactory.create()),
                Optional.of(UURI_DEFAULT),
                Optional.of(UURI_METHOD),
                Optional.of(UUID_INVALID),
                OptionalInt.empty(),
                OptionalInt.empty(),
                Optional.of(UPriority.UPRIORITY_CS4),
                false)
        );
    }

    @ParameterizedTest
    @MethodSource("responseMessageArgProvider")
    void testValidateAttributesForRpcResponseMessage(
        Optional<UUID> id,
        Optional<UUri> replyToAddress,
        Optional<UUri> invokedMethod,
        Optional<UUID> reqid,
        OptionalInt commstatus,
        OptionalInt ttl,
        Optional<UPriority> priority,
        boolean shouldSucceed
    ) {
        var attribsBuilder = UAttributes.newBuilder();
        attribsBuilder.setType(UMessageType.UMESSAGE_TYPE_RESPONSE);
        id.ifPresent(attribsBuilder::setId);
        replyToAddress.ifPresent(attribsBuilder::setSink);
        invokedMethod.ifPresent(attribsBuilder::setSource);
        reqid.ifPresent(attribsBuilder::setReqid);
        commstatus.ifPresent(attribsBuilder::setCommstatusValue);
        ttl.ifPresent(attribsBuilder::setTtl);
        priority.ifPresent(attribsBuilder::setPriority);
        var attribs = attribsBuilder.build();
        if (shouldSucceed) {
            UAttributesValidator.Validators.RESPONSE.validator().validate(attribs);
            assertThrows(
                ValidationException.class,
                () -> UAttributesValidator.Validators.PUBLISH.validator().validate(attribs)
            );
            assertThrows(
                ValidationException.class,
                () -> UAttributesValidator.Validators.NOTIFICATION.validator().validate(attribs)
            );
            assertThrows(
                ValidationException.class,
                () -> UAttributesValidator.Validators.REQUEST.validator().validate(attribs)
            );
        } else {
            assertThrows(
                ValidationException.class,
                () -> UAttributesValidator.Validators.RESPONSE.validator().validate(attribs));
        }
    }
}
