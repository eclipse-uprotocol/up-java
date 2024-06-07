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

import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UPriority;
import org.eclipse.uprotocol.v1.UUri;
import org.eclipse.uprotocol.v1.UMessage;
import org.eclipse.uprotocol.v1.UMessageType;
import org.eclipse.uprotocol.v1.UUID;
import org.eclipse.uprotocol.v1.UPayloadFormat;
import org.eclipse.uprotocol.v1.UAttributes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.protobuf.Any;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class UMessageBuilderTest {

    @Test
    public void testPublish() {
        UMessage publish = UMessageBuilder.publish(buildSource()).build();
        assertNotNull(publish);
        assertEquals(UMessageType.UMESSAGE_TYPE_PUBLISH, publish.getAttributes().getType());
        assertEquals(UPriority.UPRIORITY_CS1, publish.getAttributes().getPriority());
    }

    @Test
    public void testNotification() {
        UUri sink = buildSink();
        UMessage notification = UMessageBuilder.notification(buildSource(), sink).build();
        assertNotNull(notification);
        assertEquals(UMessageType.UMESSAGE_TYPE_NOTIFICATION, notification.getAttributes().getType());
        assertEquals(UPriority.UPRIORITY_CS1, notification.getAttributes().getPriority());
        assertEquals(sink, notification.getAttributes().getSink());
    }

    @Test
    public void testRequest() {
        UUri sink = buildSink();
        Integer ttl = 1000;
        UMessage request = UMessageBuilder.request(buildSource(), sink, ttl).build();
        assertNotNull(request);
        assertEquals(UMessageType.UMESSAGE_TYPE_REQUEST, request.getAttributes().getType());
        assertEquals(UPriority.UPRIORITY_CS4, request.getAttributes().getPriority());
        assertEquals(sink, request.getAttributes().getSink());
        assertEquals(ttl, request.getAttributes().getTtl());
    }

    @Test
    public void testResponse() {
        UUri sink = buildSink();
        UUID reqId = getUUID();
        UMessage response = UMessageBuilder.response(buildSource(), sink, reqId).build();
        assertNotNull(response);
        assertEquals(UMessageType.UMESSAGE_TYPE_RESPONSE, response.getAttributes().getType());
        assertEquals(UPriority.UPRIORITY_CS4, response.getAttributes().getPriority());
        assertEquals(sink, response.getAttributes().getSink());
        assertEquals(reqId, response.getAttributes().getReqid());
    }

    @Test
    @DisplayName("Test response with existing request")
    public void testResponseWithExistingRequest() {
        UMessage request = UMessageBuilder.request(buildSource(), buildSink(), 1000).build();
        UMessage response = UMessageBuilder.response(request.getAttributes()).build();
        assertNotNull(response);
        assertEquals(UMessageType.UMESSAGE_TYPE_RESPONSE, response.getAttributes().getType());
        assertEquals(UPriority.UPRIORITY_CS4, request.getAttributes().getPriority());
        assertEquals(UPriority.UPRIORITY_CS4, response.getAttributes().getPriority());
        assertEquals(request.getAttributes().getSource(), response.getAttributes().getSink());
        assertEquals(request.getAttributes().getSink(), response.getAttributes().getSource());
        assertEquals(request.getAttributes().getId(), response.getAttributes().getReqid());
    }

    @Test
    public void testBuild() {
        UMessageBuilder builder = UMessageBuilder.publish(buildSource())
                .withToken("test_token")
                .withPermissionLevel(2)
                .withCommStatus(UCode.CANCELLED)
                .withTraceparent("myParents");
        UMessage message = builder.build();
        UAttributes attributes = message.getAttributes();
        assertNotNull(message);
        assertNotNull(attributes);
        assertEquals(UMessageType.UMESSAGE_TYPE_PUBLISH, attributes.getType());
        assertEquals(UPriority.UPRIORITY_CS1, attributes.getPriority());
        assertEquals("test_token", attributes.getToken());
        assertEquals(2, attributes.getPermissionLevel());
        assertEquals(UCode.CANCELLED, attributes.getCommstatus());
        assertEquals("myParents", attributes.getTraceparent());
    }

    @Test
    @DisplayName("Test building UMessage with google.protobuf.Message payload")
    public void testBuildWithPayload() {
        UMessage message = UMessageBuilder.publish(buildSource())
                .build(buildSink());
        assertNotNull(message);
        assertNotNull(message.getPayload());
        assertEquals(UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF, message.getAttributes().getPayloadFormat());
        assertEquals(message.getPayload(), buildSink().toByteString());
    }

    @Test
    @DisplayName("Test building UMessage with UPayload payload")
    public void testBuildWithUPayload() {
        UMessage message = UMessageBuilder.publish(buildSource())
                .build(UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF, buildSink().toByteString());
        assertNotNull(message);
        assertNotNull(message.getPayload());
        assertEquals(UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF, message.getAttributes().getPayloadFormat());
        assertEquals(message.getPayload(), buildSink().toByteString());
    }

    @Test
    @DisplayName("Test building UMessage with google.protobuf.Any payload")
    public void testBuildWithAnyPayload() {
        UMessage message = UMessageBuilder.publish(buildSource())
                .build(Any.getDefaultInstance());
        assertNotNull(message);
        assertNotNull(message.getPayload());
        assertEquals(UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF_WRAPPED_IN_ANY, message.getAttributes().getPayloadFormat());
        assertEquals(message.getPayload(), Any.getDefaultInstance().toByteString());
    }


    @Test
    @DisplayName("Test building response message with the wrong priority value of UPRIORITY_CS3")
    public void testBuildResponseWithWrongPriority() {
        UUri sink = buildSink();
        UUID reqId = getUUID();
        UMessage response = UMessageBuilder.response(buildSource(), sink, reqId)
                .withPriority(UPriority.UPRIORITY_CS3)
                .build();
        assertNotNull(response);
        assertEquals(UMessageType.UMESSAGE_TYPE_RESPONSE, response.getAttributes().getType());
        assertEquals(UPriority.UPRIORITY_CS4, response.getAttributes().getPriority());
        assertEquals(sink, response.getAttributes().getSink());
        assertEquals(reqId, response.getAttributes().getReqid());
    }

    @Test
    @DisplayName("Test building request message with the wrong priority value of UPRIORITY_CS3")
    public void testBuildRequestWithWrongPriority() {
        UUri sink = buildSink();
        Integer ttl = 1000;
        UMessage request = UMessageBuilder.request(buildSource(), sink, ttl)
                .withPriority(UPriority.UPRIORITY_CS3)
                .build();
        assertNotNull(request);
        assertEquals(UMessageType.UMESSAGE_TYPE_REQUEST, request.getAttributes().getType());
        assertEquals(UPriority.UPRIORITY_CS4, request.getAttributes().getPriority());
        assertEquals(sink, request.getAttributes().getSink());
        assertEquals(ttl, request.getAttributes().getTtl());
    }

    @Test
    @DisplayName("Test building notification message with the wrong priority value of UPRIORITY_CS0")
    public void testBuildNotificationWithWrongPriority() {
        UUri sink = buildSink();
        UMessage notification = UMessageBuilder.notification(buildSource(), sink)
                .withPriority(UPriority.UPRIORITY_CS0)
                .build();
        assertNotNull(notification);
        assertEquals(UMessageType.UMESSAGE_TYPE_NOTIFICATION, notification.getAttributes().getType());
        assertEquals(UPriority.UPRIORITY_CS1, notification.getAttributes().getPriority());
        assertEquals(sink, notification.getAttributes().getSink());
    }

    @Test
    @DisplayName("Test building publish message with the wrong priority value of UPRIORITY_CS0")
    public void testBuildPublishWithWrongPriority() {
        UMessage publish = UMessageBuilder.publish(buildSource())
                .withPriority(UPriority.UPRIORITY_CS0)
                .build();
        assertNotNull(publish);
        assertEquals(UMessageType.UMESSAGE_TYPE_PUBLISH, publish.getAttributes().getType());
        assertEquals(UPriority.UPRIORITY_CS1, publish.getAttributes().getPriority());
    }

    @Test
    @DisplayName("Test building publish message with the priority value of UPRIORITY_CS4")
    public void testBuildPublishWithPriority() {
        UMessage publish = UMessageBuilder.publish(buildSource())
                .withPriority(UPriority.UPRIORITY_CS4)
                .build();
        assertNotNull(publish);
        assertEquals(UMessageType.UMESSAGE_TYPE_PUBLISH, publish.getAttributes().getType());
        assertEquals(UPriority.UPRIORITY_CS4, publish.getAttributes().getPriority());
    }


    private UUri buildSink() {
        return UUri.newBuilder().setAuthorityName("vcu.someVin.veh.ultifi.gm.com")
                .setUeId(1)
                .setUeVersionMajor(1)
                .setResourceId(0).build();
    }

    private UUID getUUID() {
        java.util.UUID uuid_java = java.util.UUID.randomUUID();
        return UUID.newBuilder().setMsb(uuid_java.getMostSignificantBits()).setLsb(uuid_java.getLeastSignificantBits())
                .build();
    }

    private UUri buildSource() {
        return UUri.newBuilder().setUeId(2).setUeVersionMajor(1).setResourceId(0).build();
    }
}
