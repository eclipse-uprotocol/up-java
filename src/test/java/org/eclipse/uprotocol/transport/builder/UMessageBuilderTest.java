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
import org.eclipse.uprotocol.uuid.factory.UuidFactory;
import org.eclipse.uprotocol.v1.UAttributes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UMessageBuilderTest {

    @Test
    public void testPublish() {
        UMessage publish = UMessageBuilder.publish(buildTopic()).build();
        assertNotNull(publish);
        assertEquals(UMessageType.UMESSAGE_TYPE_PUBLISH, publish.getAttributes().getType());
        assertEquals(UPriority.UPRIORITY_CS1, publish.getAttributes().getPriority());
    }

    @Test
    public void testNotification() {
        UUri sink = buildSink();
        UMessage notification = UMessageBuilder.notification(buildTopic(), sink).build();
        assertNotNull(notification);
        assertEquals(UMessageType.UMESSAGE_TYPE_NOTIFICATION, notification.getAttributes().getType());
        assertEquals(UPriority.UPRIORITY_CS1, notification.getAttributes().getPriority());
        assertEquals(sink, notification.getAttributes().getSink());
    }

    @Test
    public void testRequest() {
        UUri sink = buildMethod();
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
        UUID reqId = UuidFactory.Factories.UPROTOCOL.factory().create();
        UMessage response = UMessageBuilder.response(buildMethod(), sink, reqId).build();
        assertNotNull(response);
        assertEquals(UMessageType.UMESSAGE_TYPE_RESPONSE, response.getAttributes().getType());
        assertEquals(UPriority.UPRIORITY_CS4, response.getAttributes().getPriority());
        assertEquals(sink, response.getAttributes().getSink());
        assertEquals(reqId, response.getAttributes().getReqid());
    }

    @Test
    @DisplayName("Test response with existing request")
    public void testResponseWithExistingRequest() {
        UMessage request = UMessageBuilder.request(buildSource(), buildMethod(), 1000).build();
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
        UMessageBuilder builder = UMessageBuilder.publish(buildTopic())
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
    @DisplayName("Test building UMessage with empty payload")
    public void testBuildWithAnyPayload() {
        UMessage message = UMessageBuilder.publish(buildTopic())
                .build();
        assertNotNull(message);
        assertFalse(message.hasPayload());
    }


    @Test
    @DisplayName("Test building response message with the wrong priority value of UPRIORITY_CS3")
    public void testBuildResponseWithWrongPriority() {
        UUri sink = buildSink();
        UUID reqId = UuidFactory.Factories.UPROTOCOL.factory().create();
        UMessage response = UMessageBuilder.response(buildMethod(), sink, reqId)
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
        UUri sink = buildMethod();
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
        UMessage notification = UMessageBuilder.notification(buildTopic(), sink)
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
        UMessage publish = UMessageBuilder.publish(buildTopic())
                .withPriority(UPriority.UPRIORITY_CS0)
                .build();
        assertNotNull(publish);
        assertEquals(UMessageType.UMESSAGE_TYPE_PUBLISH, publish.getAttributes().getType());
        assertEquals(UPriority.UPRIORITY_CS1, publish.getAttributes().getPriority());
    }

    @Test
    @DisplayName("Test building publish message with the priority value of UPRIORITY_CS4")
    public void testBuildPublishWithPriority() {
        UMessage publish = UMessageBuilder.publish(buildTopic())
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


    @Test
    @DisplayName("Test publish when source is not a valid topic")
    public void testPublishWithInvalidSource() {
        assertThrows(IllegalArgumentException.class, 
            () -> UMessageBuilder.publish(buildSource()).build());
    }


    @Test
    @DisplayName("Test notification when source is not a valid topic")
    public void testNotificationWithInvalidSource() {
        assertThrows(IllegalArgumentException.class, 
            () -> UMessageBuilder.notification(buildSource(), buildSink()).build());
    }


    @Test
    @DisplayName("Test notification when sink is not a valid UUri")
    public void testNotificationWithInvalidSink() {
        assertThrows(IllegalArgumentException.class, 
            () -> UMessageBuilder.notification(buildTopic(), buildTopic()).build());
    }


    @Test
    @DisplayName("Test request when source is not valid")
    public void testRequestWithInvalidSource() {
        assertThrows(IllegalArgumentException.class, 
            () -> UMessageBuilder.request(buildMethod(), buildMethod(), 1000).build());
    }

    @Test
    @DisplayName("Test request when sink is not valid")
    public void testRequestWithInvalidSink() {
        assertThrows(IllegalArgumentException.class, 
            () -> UMessageBuilder.request(buildSource(), buildSource(), 1000).build());
    }

    @Test
    @DisplayName("Test request when source and sink are not valid")
    public void testRequestWithInvalidSourceAndSink() {
        assertThrows(IllegalArgumentException.class, 
            () -> UMessageBuilder.request(buildMethod(), buildSource(), 1000).build());
    }


    @Test
    @DisplayName("Test request when the ttl is null")
    public void testRequestWithNullTtl() {
        assertThrows(NullPointerException.class, 
            () -> UMessageBuilder.request(buildSource(), buildMethod(), null).build());
    }


    @Test
    @DisplayName("Test request when ttl is negative")
    public void testRequestWithNegativeTtl() {
        assertThrows(IllegalArgumentException.class, 
            () -> UMessageBuilder.request(buildSource(), buildMethod(), -1).build());
    }

    
    @Test
    @DisplayName("Test response when source is not valid")
    public void testResponseWithInvalidSource() {
        assertThrows(IllegalArgumentException.class, 
            () -> UMessageBuilder.response(buildSink(), buildSink(), 
                UuidFactory.Factories.UPROTOCOL.factory().create()).build());
    }


    @Test
    @DisplayName("Test response when sink is not valid")
    public void testResponseWithInvalidSink() {
        assertThrows(IllegalArgumentException.class, 
            () -> UMessageBuilder.response(buildMethod(), buildMethod(), 
                UuidFactory.Factories.UPROTOCOL.factory().create()).build());
    }

    @Test
    @DisplayName("Test response when source and sink are not valid")
    public void testResponseWithInvalidSourceAndSink() {
        assertThrows(IllegalArgumentException.class, 
            () -> UMessageBuilder.response(buildSource(), buildSource(), 
                UuidFactory.Factories.UPROTOCOL.factory().create()).build());
    }


    @Test
    @DisplayName("Test response when reqId is null")
    public void testResponseWithNullReqId() {
        assertThrows(NullPointerException.class, 
            () -> UMessageBuilder.response(buildMethod(), buildSink(), null).build());
    }


    @Test
    @DisplayName("Test response when we pass an invalid reqid")
    public void testResponseWithInvalidReqId() {
        assertThrows(IllegalArgumentException.class, 
            () -> UMessageBuilder.response(buildMethod(), buildSink(), UUID.getDefaultInstance()).build());
    }


    @Test
    @DisplayName("Test notification when source is not a valid topic and and sink is not valid")
    public void testNotificationWithInvalidSourceAndSink() {
        assertThrows(IllegalArgumentException.class, 
            () -> UMessageBuilder.notification(buildSink(), buildSource()).build());
    }


    @Test
    @DisplayName("Test response builder when we pass UAttributes that is not a valid request type")
    public void testResponseBuilderWithInvalidRequestType() {
        assertThrows(IllegalArgumentException.class, 
            () -> UMessageBuilder.response(UAttributes.getDefaultInstance()).build());
    }
    

    private UUri buildSource() {
        return UUri.newBuilder().setUeId(2).setUeVersionMajor(1).setResourceId(0).build();
    }

    private UUri buildTopic() {
        return UUri.newBuilder().setUeId(2).setUeVersionMajor(1).setResourceId(0x8000).build();
    }

    private UUri buildMethod() {
        return UUri.newBuilder().setUeId(2).setUeVersionMajor(1).setResourceId(1).build();
    }
}
