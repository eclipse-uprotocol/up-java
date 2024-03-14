/*
 * Copyright (c) 2024 General Motors GTO LLC
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
 * 
 * SPDX-FileType: SOURCE
 * SPDX-FileCopyrightText: 2024 General Motors GTO LLC
 * SPDX-License-Identifier: Apache-2.0
 */

 package org.eclipse.uprotocol.uri.serializer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.eclipse.uprotocol.uri.factory.UResourceBuilder;
import org.eclipse.uprotocol.v1.UAuthority;
import org.eclipse.uprotocol.v1.UEntity;
import org.eclipse.uprotocol.v1.UUri;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.protobuf.ByteString;

public class ShortUriSerializerTest {
    
    @Test
    @DisplayName("Test Creating and using the ShortUriSerializer")
    public void testCreatingShortUriSerializer() {
        final UUri uri = UUri.newBuilder()
                .setEntity(UEntity.newBuilder().setId(1).setVersionMajor(1))
                .setResource(UResourceBuilder.forRpcResponse())
                .build();

        final String strUri = ShortUriSerializer.instance().serialize(uri);
        assertEquals("/1/1/0", strUri);
        final UUri uri2 = ShortUriSerializer.instance().deserialize(strUri);
        assertEquals(uri, uri2);
    }

    @Test
    @DisplayName("Test Creating and using the ShortUriSerializer with a method")
    public void testCreatingShortUriSerializerWithMethod() {
        final UUri uri = UUri.newBuilder()
                .setEntity(UEntity.newBuilder().setId(1).setVersionMajor(1))
                .setResource(UResourceBuilder.forRpcRequest(10))
                .build();

        final String strUri = ShortUriSerializer.instance().serialize(uri);
        assertEquals("/1/1/10", strUri);
        final UUri uri2 = ShortUriSerializer.instance().deserialize(strUri);
        assertEquals(uri, uri2);
    }

    @Test
    @DisplayName("Test Creating and using the ShortUriSerializer with a topic")
    public void testCreatingShortUriSerializerWithTopic() {
        final UUri uri = UUri.newBuilder()
                .setEntity(UEntity.newBuilder().setId(1).setVersionMajor(1))
                .setResource(UResourceBuilder.fromId(20000))
                .build();

        final String strUri = ShortUriSerializer.instance().serialize(uri);
        assertEquals("/1/1/20000", strUri);
        final UUri uri2 = ShortUriSerializer.instance().deserialize(strUri);
        assertEquals(uri, uri2);
    }

    @Test
    @DisplayName("Test Creating and using the ShortUriSerializer with id authority")
    public void testCreatingShortUriSerializerWithAuthority() {
        final UUri uri = UUri.newBuilder()
                .setEntity(UEntity.newBuilder().setId(1).setVersionMajor(1))
                .setAuthority(UAuthority.newBuilder().setId(ByteString.copyFromUtf8("19UYA31581L000000")))
                .setResource(UResourceBuilder.fromId(20000))
                .build();

        final String strUri = ShortUriSerializer.instance().serialize(uri);
        assertEquals("//19UYA31581L000000/1/1/20000", strUri);
        final UUri uri2 = ShortUriSerializer.instance().deserialize(strUri);
        assertEquals(uri, uri2);
    }

    @Test
    @DisplayName("Test Creating and using the ShortUriSerializer with ip authority")
    public void testCreatingShortUriSerializerWithIpAuthority() throws UnknownHostException {
        final UUri uri = UUri.newBuilder()
                .setEntity(UEntity.newBuilder().setId(1).setVersionMajor(1))
                .setAuthority(UAuthority.newBuilder().setIp(ByteString.copyFrom(InetAddress.getByName("192.168.1.100").getAddress())))
                .setResource(UResourceBuilder.fromId(20000))
                .build();

        final String strUri = ShortUriSerializer.instance().serialize(uri);
        assertEquals("//192.168.1.100/1/1/20000", strUri);
        final UUri uri2 = ShortUriSerializer.instance().deserialize(strUri);
        assertEquals(uri, uri2);
    }    
}
