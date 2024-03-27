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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.eclipse.uprotocol.uri.factory.UResourceBuilder;
import org.eclipse.uprotocol.v1.UAuthority;
import org.eclipse.uprotocol.v1.UEntity;
import org.eclipse.uprotocol.v1.UResource;
import org.eclipse.uprotocol.v1.UUri;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.protobuf.ByteString;

public class ShortUriSerializerTest {
    
    @Test
    @DisplayName("Test serialize with null uri")
    public void testSerializeWithNullUri() {
        final String strUri = ShortUriSerializer.instance().serialize(null);
        assertEquals("", strUri);
    }

    @Test
    @DisplayName("Test serialize with empty uri")
    public void testSerializeWithEmptyUri() {
        final String strUri = ShortUriSerializer.instance().serialize(UUri.getDefaultInstance());
        assertEquals("", strUri);
    }


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
                .setAuthority(UAuthority.newBuilder().setIp(ByteString.copyFrom(IpAddress.toBytes("192.168.1.100"))))
                .setResource(UResourceBuilder.fromId(20000))
                .build();

        final String strUri = ShortUriSerializer.instance().serialize(uri);
        assertEquals("//192.168.1.100/1/1/20000", strUri);
        final UUri uri2 = ShortUriSerializer.instance().deserialize(strUri);
        assertEquals(uri, uri2);
    }
    
    @Test
    @DisplayName("Test short serializing a URI that doesn't have a resource")
    public void testShortSerializingUriWithoutResource() {
        final UUri uri = UUri.newBuilder()
                .setEntity(UEntity.newBuilder().setId(1).setVersionMajor(1))
                .build();
        final String strUri = ShortUriSerializer.instance().serialize(uri);
        assertEquals(strUri, "/1/1"); 
    }
    
    @Test
    @DisplayName("Test short serializing a URI that have a negative number for uEntity version major")
    public void testShortSerializingUriWithNegativeVersionMajor() {
        final UUri uri = UUri.newBuilder()
                .setEntity(UEntity.newBuilder().setId(1).setVersionMajor(-1))
                .setResource(UResourceBuilder.fromId(20000))
                .build();
        final String strUri = ShortUriSerializer.instance().serialize(uri);
        assertEquals(strUri, "/1//20000"); 
    }
 
    @Test
    @DisplayName("Test short deserialize a null URI")
    public void testShortDeserializeNullUri() {
        final UUri uri = ShortUriSerializer.instance().deserialize(null);
        assertEquals(uri, UUri.getDefaultInstance());
    }

    @Test
    @DisplayName("Test short deserialize an empty URI")
    public void testShortDeserializeEmptyUri() {
        final UUri uri = ShortUriSerializer.instance().deserialize("");
        assertEquals(uri, UUri.getDefaultInstance());
    }
  
    @Test
    @DisplayName("Test short deserialize of a valid URI with scheme")
    public void testShortDeserializeUriWithSchemeAndAuthority() {
        final UUri uri = ShortUriSerializer.instance().deserialize("up://mypc/1/1/1");
        assertTrue(uri.hasAuthority());
        assertEquals(uri.getAuthority().getId(), ByteString.copyFromUtf8("mypc"));
        assertFalse(uri.getAuthority().hasName());
        assertFalse(uri.getAuthority().hasIp());
        assertTrue(uri.hasEntity());
        assertEquals(uri.getEntity().getId(), 1);
        assertEquals(uri.getEntity().getVersionMajor(), 1);
        assertTrue(uri.hasResource());
        assertEquals(uri.getResource().getId(), 1);
    }

    @Test
    @DisplayName("Test short deserialize of a valid URI without scheme")
    public void testShortDeserializeUriWithoutScheme() {
        final UUri uri = ShortUriSerializer.instance().deserialize("//mypc/1/1/1");
        assertTrue(uri.hasAuthority());
        assertEquals(uri.getAuthority().getId(), ByteString.copyFromUtf8("mypc"));
        assertFalse(uri.getAuthority().hasName());
        assertFalse(uri.getAuthority().hasIp());
        assertTrue(uri.hasEntity());
        assertEquals(uri.getEntity().getId(), 1);
        assertEquals(uri.getEntity().getVersionMajor(), 1);
        assertTrue(uri.hasResource());
        assertEquals(uri.getResource().getId(), 1);
    }

    @Test
    @DisplayName("Test short deserialize a uri that only contains //")
    public void testShortDeserializeUriWithOnlyAuthority() {
        final UUri uri = ShortUriSerializer.instance().deserialize("//");
        assertEquals(uri, UUri.getDefaultInstance());
    }

    @Test
    @DisplayName("Test short deserialize a uri with scheme and only contains //")
    public void testShortDeserializeUriWithSchemeAndOnlyAuthority() {
        final UUri uri = ShortUriSerializer.instance().deserialize("up://");
        assertEquals(uri, UUri.getDefaultInstance());
    }

    @Test
    @DisplayName("Test short serialize with UAuthority ip address that is invalid")
    public void testShortSerializeWithInvalidIpAddress() {
        final UUri uri = UUri.newBuilder()
                .setEntity(UEntity.newBuilder().setId(1).setVersionMajor(1))
                .setAuthority(UAuthority.newBuilder().setIp(ByteString.copyFromUtf8("34823748273")))
                .build();
        final String uriString = ShortUriSerializer.instance().serialize(uri);
        assertEquals(uriString, "");
    }

    @Test
    @DisplayName("Test short serialize with UAuthority that only have name and not ip or id")
    public void testShortSerializeWithAuthorityOnlyName() {
        final UUri uri = UUri.newBuilder()
                .setEntity(UEntity.newBuilder().setId(1).setVersionMajor(1))
                .setAuthority(UAuthority.newBuilder().setName("mypc"))
                .build();
        final String uriString = ShortUriSerializer.instance().serialize(uri);
        assertEquals(uriString, "");
    }

    @Test
    @DisplayName("Test short deserialize of a local URI that has too many parts")
    public void testShortDeserializeLocalUriWithTooManyParts() {
        final UUri uri = ShortUriSerializer.instance().deserialize("/1/1/1/1");
        assertEquals(uri, UUri.getDefaultInstance());
    }

    @Test
    @DisplayName("Test short deserialize of a local URI that only has 2 parts")
    public void testShortDeserializeLocalUriWithOnlyTwoParts() {
        final UUri uri = ShortUriSerializer.instance().deserialize("/1/1");
        assertTrue(uri.hasEntity());
        assertEquals(uri.getEntity().getId(), 1);
        assertEquals(uri.getEntity().getVersionMajor(), 1);
    }

    @Test
    @DisplayName("Test short deserialize of a local URI that has 2 parts")
    public void testShortDeserializeLocalUriWithThreeParts() {
        final UUri uri = ShortUriSerializer.instance().deserialize("/1");
        assertTrue(uri.hasEntity());
        assertEquals(uri.getEntity().getId(), 1);
        assertFalse(uri.hasResource());
    }

    @Test
    @DisplayName("Test short deserialize with a blank authority")
    public void testShortDeserializeWithBlankAuthority() {
        final UUri uri = ShortUriSerializer.instance().deserialize("///1/1/1");
        assertEquals(uri, UUri.getDefaultInstance());
    }

    @Test
    @DisplayName("Test short deserialize with a remote authority that is an IP address and too many parts in the uri")
    public void testShortDeserializeWithRemoteAuthorityIpAndTooManyParts() {
        final UUri uri = ShortUriSerializer.instance().deserialize("//192.168.1.100/1/1/1/1");
        assertEquals(uri, UUri.getDefaultInstance());
    }

    @Test
    @DisplayName("Test short deserialize with a remote authority that is an IP address and right number of parts")
    public void testShortDeserializeWithRemoteAuthorityIpAndRightNumberOfParts() {
        final UUri uri = ShortUriSerializer.instance().deserialize("//192.168.1.100/1/1/1");
        assertTrue(uri.hasAuthority());
        assertTrue(uri.getAuthority().hasIp());
        assertTrue(uri.getAuthority().getIp().equals(ByteString.copyFrom(IpAddress.toBytes("192.168.1.100"))));
        assertTrue(uri.hasEntity());
        assertEquals(uri.getEntity().getId(), 1);
        assertEquals(uri.getEntity().getVersionMajor(), 1);
        assertTrue(uri.hasResource());
        assertEquals(uri.getResource().getId(), 1);
    }
    
    
    @Test
    @DisplayName("Test short deserialize with a remote authority that is an IP address but missing resource")
    public void testShortDeserializeWithRemoteAuthorityIpAddressMissingResource() {
        final UUri uri = ShortUriSerializer.instance().deserialize("//192.168.1.100/1/1");
        assertTrue(uri.hasAuthority());
        assertTrue(uri.getAuthority().hasIp());
        assertTrue(uri.getAuthority().getIp().equals(ByteString.copyFrom(IpAddress.toBytes("192.168.1.100"))));
        assertTrue(uri.hasEntity());
        assertEquals(uri.getEntity().getId(), 1);
        assertEquals(uri.getEntity().getVersionMajor(), 1);
        assertFalse(uri.hasResource());
    }

    @Test
    @DisplayName("Test short deserialize with a remote authority that is an IP address but missing resource and major version")
    public void testShortDeserializeWithRemoteAuthorityIpAddressMissingResourceAndVersionMajor() {
        final UUri uri = ShortUriSerializer.instance().deserialize("//192.168.1.100/1");
        assertTrue(uri.hasAuthority());
        assertTrue(uri.getAuthority().hasIp());
        assertTrue(uri.getAuthority().getIp().equals(ByteString.copyFrom(IpAddress.toBytes("192.168.1.100"))));
        assertTrue(uri.hasEntity());
        assertEquals(uri.getEntity().getId(), 1);
        assertFalse(uri.getEntity().hasVersionMajor());
    }

    @Test
    @DisplayName("Test short deserialize with a remote authority that is an IP address but missing resource and major version")
    public void testShortDeserializeWithRemoteAuthorityIpAddressMissingResourceAndVersionMajorAndUeId() {
        final UUri uri = ShortUriSerializer.instance().deserialize("//192.168.1.100//");
        assertTrue(uri.hasAuthority());
        assertTrue(uri.getAuthority().hasIp());
        assertTrue(uri.getAuthority().getIp().equals(ByteString.copyFrom(IpAddress.toBytes("192.168.1.100"))));
        assertFalse(uri.hasEntity());
    }

    @Test
    @DisplayName("Test short deserialize with a remote authority and blank ueversion and ueid")
    public void testShortDeserializeWithRemoteAuthorityAndBlankUeVersionAndUeId() {
        final UUri uri = ShortUriSerializer.instance().deserialize("//mypc//1/");
        assertTrue(uri.hasAuthority());
        assertTrue(uri.getAuthority().hasId());
        assertEquals(uri.getAuthority().getId(), ByteString.copyFromUtf8("mypc"));
        assertTrue(uri.hasEntity());
    }

    @Test
    @DisplayName("Test short deserialize with a remote authority and missing the other parts")
    public void testShortDeserializeWithRemoteAuthorityAndMissingParts() {
        final UUri uri = ShortUriSerializer.instance().deserialize("//mypc");
        assertTrue(uri.hasAuthority());
        assertTrue(uri.getAuthority().hasId());
        assertEquals(uri.getAuthority().getId(), ByteString.copyFromUtf8("mypc"));
    }

    @Test
    @DisplayName("Test short deserialize with a remote authority and invalid characters for entity id and version")
    public void testShortDeserializeWithRemoteAuthorityAndInvalidEntityIdAndVersion() {
        final UUri uri = ShortUriSerializer.instance().deserialize("//mypc/abc/def");
        assertEquals(uri, UUri.getDefaultInstance());
    }

    @Test
    @DisplayName("Test short deserialize with a remote authority and invalid characters for resource id")
    public void testShortDeserializeWithRemoteAuthorityAndInvalidResourceId() {
        final UUri uri = ShortUriSerializer.instance().deserialize("//mypc/1/1/abc");
        assertEquals(uri.getResource(), UResource.getDefaultInstance());
    }
}
