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

package org.eclipse.uprotocol.transport.builder;

import org.eclipse.uprotocol.v1.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class UAttributesBuilderTest {

    @Test
    public void testPublish() {
        UAttributesBuilder builder = UAttributesBuilder.publish(buildSource(), UPriority.UPRIORITY_CS1);
        assertNotNull(builder);
        UAttributes attributes = builder.build();
        assertNotNull(attributes);
        assertEquals(UMessageType.UMESSAGE_TYPE_PUBLISH, attributes.getType());
        assertEquals(UPriority.UPRIORITY_CS1, attributes.getPriority());
    }

    @Test
    public void testNotification() {
        UUri sink = buildSink();
        UAttributesBuilder builder = UAttributesBuilder.notification(buildSource(), sink, UPriority.UPRIORITY_CS1);
        assertNotNull(builder);
        UAttributes attributes = builder.build();
        assertNotNull(attributes);
        assertEquals(UMessageType.UMESSAGE_TYPE_NOTIFICATION, attributes.getType());
        assertEquals(UPriority.UPRIORITY_CS1, attributes.getPriority());
        assertEquals(sink, attributes.getSink());
    }

    @Test
    public void testRequest() {
        UUri sink = buildSink();
        Integer ttl = 1000;
        UAttributesBuilder builder = UAttributesBuilder.request(buildSource(), sink, UPriority.UPRIORITY_CS4, ttl);
        assertNotNull(builder);
        UAttributes attributes = builder.build();
        assertNotNull(attributes);
        assertEquals(UMessageType.UMESSAGE_TYPE_REQUEST, attributes.getType());
        assertEquals(UPriority.UPRIORITY_CS4, attributes.getPriority());
        assertEquals(sink, attributes.getSink());
        assertEquals(ttl, attributes.getTtl());
    }

    @Test
    public void testResponse() {
        UUri sink = buildSink();
        UUID reqId = getUUID();
        UAttributesBuilder builder = UAttributesBuilder.response(buildSource(), sink, UPriority.UPRIORITY_CS6, reqId);
        assertNotNull(builder);
        UAttributes attributes = builder.build();
        assertNotNull(attributes);
        assertEquals(UMessageType.UMESSAGE_TYPE_RESPONSE, attributes.getType());
        assertEquals(UPriority.UPRIORITY_CS6, attributes.getPriority());
        assertEquals(sink, attributes.getSink());
        assertEquals(reqId, attributes.getReqid());
    }

    @Test
    @DisplayName("Test response with existing request")
    public void testResponseWithExistingRequest() {
        UAttributes request = UAttributesBuilder.request(buildSource(), buildSink(), UPriority.UPRIORITY_CS6, 1000).build();
        UAttributesBuilder builder = UAttributesBuilder.response(request);
        assertNotNull(builder);
        UAttributes response = builder.build();
        assertNotNull(response);
        assertEquals(UMessageType.UMESSAGE_TYPE_RESPONSE, response.getType());
        assertEquals(UPriority.UPRIORITY_CS6, response.getPriority());
        assertEquals(request.getSource(), response.getSink());
        assertEquals(request.getSink(), response.getSource());
        assertEquals(request.getId(), response.getReqid());
    }

    @Test
    public void testBuild() {
        final UUID reqId = getUUID();

        UAttributesBuilder builder = UAttributesBuilder.publish(buildSource(), UPriority.UPRIORITY_CS1).withTtl(1000).withToken("test_token")
                .withSink(buildSink()).withPermissionLevel(2).withCommStatus(UCode.CANCELLED).withReqId(reqId).withTraceparent("myParents");
        UAttributes attributes = builder.build();
        assertNotNull(attributes);
        assertEquals(UMessageType.UMESSAGE_TYPE_PUBLISH, attributes.getType());
        assertEquals(UPriority.UPRIORITY_CS1, attributes.getPriority());
        assertEquals(1000, attributes.getTtl());
        assertEquals("test_token", attributes.getToken());
        assertEquals(buildSink(), attributes.getSink());
        assertEquals(2, attributes.getPermissionLevel());
        assertEquals(UCode.CANCELLED, attributes.getCommstatus());
        assertEquals(reqId, attributes.getReqid());
        assertEquals("myParents", attributes.getTraceparent());
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
