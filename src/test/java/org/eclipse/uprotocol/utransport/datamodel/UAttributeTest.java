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

package org.eclipse.uprotocol.utransport.datamodel;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.eclipse.uprotocol.uri.datamodel.UAuthority;
import org.eclipse.uprotocol.uri.datamodel.UEntity;
import org.eclipse.uprotocol.uri.datamodel.UResource;
import org.eclipse.uprotocol.uri.datamodel.UUri;
import org.eclipse.uprotocol.uuid.factory.UUIDFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class UAttributeTest {

    @Test
    @DisplayName("Make sure the equals and hash code works")
    public void testHashCodeEquals() {
        EqualsVerifier.forClass(UAttributes.class).usingGetClass().verify();
    }

    @Test
    @DisplayName("Make sure the toString works on empty")
    public void testToString_with_empty() {
        UAttributes uAttributes = UAttributes.empty();
        assertEquals("UAttributes{id=null, type=null, priority=null, ttl=null, token='null', sink=null, plevel=null, commstatus=null, reqid=null}",
                uAttributes.toString());
    }

    @Test
    @DisplayName("Make sure the toString works")
    public void testToString() {
        final UUID id = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final UUID requestId = UUID.randomUUID();
        final UAttributes uAttributes = new UAttributes.UAttributesBuilder(id,
                UMessageType.RESPONSE, UPriority.LOW)
                .withSink(new UUri(UAuthority.local(), UEntity.fromName("body.access"), UResource.empty()))
                .withTtl(1000)
                .withToken("someToken")
                .withPermissionLevel(1)
                .withReqId(requestId)
                .withCommStatus(5)
                .build();
        assertEquals(String.format("UAttributes{id=%s, type=RESPONSE, priority=LOW, ttl=1000, token='someToken', " +
                                "sink=Uri{uAuthority=UAuthority{device='null', domain='null', address='null', markedRemote=false}, " +
                                "uEntity=UEntity{name='body.access', version='latest', id='null'}, " +
                                "uResource=UResource{name='', instance='null', message='null', id='null'}}, " +
                                "plevel=1, commstatus=5, reqid=%s}",
                        id,requestId),
                uAttributes.toString());
    }

    @Test
    @DisplayName("Test creating a complete UAttributes")
    public void testCreatingUattributes() {
        final UUID id = UUIDFactory.Factories.UPROTOCOL.factory().create();
        final UUID requestId = UUID.randomUUID();
        final UUri sink = new UUri(UAuthority.local(), UEntity.fromName("body.access"), UResource.empty());
        final UAttributes uAttributes = new UAttributes.UAttributesBuilder(id,
                UMessageType.RESPONSE, UPriority.REALTIME_INTERACTIVE)
                .withSink(sink)
                .withTtl(1000)
                .withToken("someToken")
                .withPermissionLevel(1)
                .withReqId(requestId)
                .withCommStatus(5)
                .build();
        assertEquals(id, uAttributes.id());
        assertEquals(UMessageType.RESPONSE, uAttributes.type());
        assertEquals(UPriority.REALTIME_INTERACTIVE, uAttributes.priority());
        assertEquals(sink, uAttributes.sink().orElse(UUri.empty()));
        assertEquals(1000, uAttributes.ttl().orElse(0));
        assertEquals(1, uAttributes.plevel().orElse(3));
        assertEquals("someToken", uAttributes.token().orElse(""));
        assertEquals(5, uAttributes.commstatus().orElse(0));
        assertEquals(requestId, uAttributes.reqid().orElse(UUID.randomUUID()));
    }

}
