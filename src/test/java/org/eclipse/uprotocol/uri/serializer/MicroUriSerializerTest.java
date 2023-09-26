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
package org.eclipse.uprotocol.uri.serializer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.eclipse.uprotocol.uri.datamodel.UAuthority;
import org.eclipse.uprotocol.uri.datamodel.UEntity;
import org.eclipse.uprotocol.uri.datamodel.UResource;
import org.eclipse.uprotocol.uri.datamodel.UUri;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class MicroUriSerializerTest
 {
    
    @Test
    @DisplayName("Test serialize and deserialize empty content")
    public void test_empty() {
        UUri uri = UUri.empty();
        byte[] bytes = MicroUriSerializer.instance().serialize(uri);

        assertEquals(bytes.length, 0);

        UUri uri2 = MicroUriSerializer.instance().deserialize(bytes);
        assertTrue(uri2.isEmpty());
    }

    @Test
    @DisplayName("Test serialize and deserialize null content")
    public void test_null() {
        byte[] bytes = MicroUriSerializer.instance().serialize(null);

        assertEquals(bytes.length, 0);

        UUri uri2 = MicroUriSerializer.instance().deserialize(null);
        assertTrue(uri2.isEmpty());
    }

    @Test
    @DisplayName("Test happy path Byte serialization of local UUri")
    public void test_serialize_uri() {
        UAuthority uAuthority = UAuthority.local();
        UEntity use = UEntity.microFormat((short)2, 1);
        UResource uResource = UResource.microFormat((short)3);

        UUri uri = new UUri(uAuthority, use, uResource);

        byte[] bytes = MicroUriSerializer.instance().serialize(uri);
        UUri uri2 = MicroUriSerializer.instance().deserialize(bytes);

        assertEquals(uri, uri2);
    }
    

    @Test
    @DisplayName("Test happy path with null version")
    public void test_serialize_uri_without_version() {
        UAuthority uAuthority = UAuthority.local();
        UEntity use = UEntity.microFormat((short)2, null);
        UResource uResource = UResource.microFormat((short)3);

        UUri uri = new UUri(uAuthority, use, uResource);

        byte[] bytes = MicroUriSerializer.instance().serialize(uri);
        UUri uri2 = MicroUriSerializer.instance().deserialize(bytes);

        assertEquals(uri, uri2);
    }

    @Test
    @DisplayName("Test serialize invalid UUris")
    public void test_serialize_invalid_uuris() {
        UUri uri = new UUri(UAuthority.local(), UEntity.microFormat((short)1, null), UResource.empty());
        byte[] bytes = MicroUriSerializer.instance().serialize(uri);
        assertEquals(bytes.length, 0);

        UUri uri2 = new UUri(UAuthority.local(), UEntity.longFormat("", null), UResource.forRpcRequest("", (short)1));
        byte[] bytes2 = MicroUriSerializer.instance().serialize(uri2);
        assertEquals(bytes2.length, 0);

        UUri uri3 = new UUri(UAuthority.longRemote("null", "null"), UEntity.longFormat("", null), UResource.forRpcRequest("", (short)1));
        byte[] bytes3 = MicroUriSerializer.instance().serialize(uri3);
        assertEquals(bytes3.length, 0);

        UUri uri4 = new UUri(UAuthority.resolvedRemote("vcu", "vin", null), UEntity.longFormat("", null), UResource.forRpcRequest("", (short)1));
        byte[] bytes4 = MicroUriSerializer.instance().serialize(uri4);
        assertEquals(bytes4.length, 0);
    }

    @Test
    @DisplayName("Test serialize and deserialize IPv4 UUris")
    public void test_serialize_ipv4_uri() throws UnknownHostException {
        UAuthority uAuthority = UAuthority.microRemote(InetAddress.getByName("192.168.1.100"));
        UEntity use = UEntity.microFormat((short)2, 1);
        UResource uResource = UResource.microFormat((short)3);
        UUri uri = new UUri(uAuthority, use, uResource);

        byte[] bytes = MicroUriSerializer.instance().serialize(uri);
        UUri uri2 = MicroUriSerializer.instance().deserialize(bytes);
        assertEquals(uri, uri2);
    }

    @Test
    @DisplayName("Test serialize and deserialize IPv6 UUris")
    public void test_serialize_ipv6_uri() throws UnknownHostException {
        UAuthority uAuthority = UAuthority.microRemote(InetAddress.getByName("2001:db8:85a3:0:0:8a2e:370:7334"));
        UEntity use = UEntity.microFormat((short)2, 1);
        UResource uResource = UResource.microFormat((short)3);
        UUri uri = new UUri(uAuthority, use, uResource);

        byte[] bytes = MicroUriSerializer.instance().serialize(uri);
        UUri uri2 = MicroUriSerializer.instance().deserialize(bytes);
        assertEquals(uri, uri2);
    }

    @Test
    @DisplayName("Test deserialize with missing information")
    public void test_deserialize_with_missing_information() throws UnknownHostException {
        UAuthority uAuthority = UAuthority.microRemote(InetAddress.getByName("2001:db8:85a3:0:0:8a2e:370:7334"));
        UEntity use = UEntity.microFormat((short)2, 1);
        UResource uResource = UResource.microFormat((short)3);
        UUri uri = new UUri(uAuthority, use, uResource);
        byte[] bytes = MicroUriSerializer.instance().serialize(uri);
        
        // invalid version
        byte[] byte1 = Arrays.copyOf(bytes, bytes.length);
        byte1[0] = 0x0;
        UUri uri1 = MicroUriSerializer.instance().deserialize(byte1);
        assertTrue(uri1.isEmpty());

        // Invalid type
        byte[] byte2 = Arrays.copyOf(bytes, bytes.length);
        byte2[1] = Byte.MAX_VALUE;
        UUri uri2 = MicroUriSerializer.instance().deserialize(byte2);
        assertTrue(uri2.isEmpty());

        // Wrong size (local)
        byte[] byte3 = new byte[] {0x1, 0x0, 0x0, 0x0};
        UUri uri3 = MicroUriSerializer.instance().deserialize(byte3);
        assertTrue(uri3.isEmpty());

        // Wrong size (ipv4)
        byte[] byte4 = new byte[] {0x1, 0x1, 0x0, 0x0};
        UUri uri4 = MicroUriSerializer.instance().deserialize(byte4);
        assertTrue(uri4.isEmpty());

        // Wrong size (ipv6)
        byte[] byte5 = new byte[] {0x1, 0x1, 0x0, 0x0};
        UUri uri5 = MicroUriSerializer.instance().deserialize(byte5);
        assertTrue(uri5.isEmpty());

        // Right local size (local)
        byte[] byte6 = new byte[] {0x1, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0};
        UUri uri6 = MicroUriSerializer.instance().deserialize(byte6);
        assertFalse(uri6.isEmpty());

        // IPv4 type local size
        byte[] byte7 = new byte[] {0x1, 0x1, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0};
        UUri uri7 = MicroUriSerializer.instance().deserialize(byte7);
        assertTrue(uri7.isEmpty());

        // IPv6 type local size
        byte[] byte8 = new byte[] {0x1, 0x2, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0};
        UUri uri8 = MicroUriSerializer.instance().deserialize(byte8);
        assertTrue(uri8.isEmpty());


        // Local type but too large
        byte[] byte9 = new byte[] {0x1, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0};
        UUri uri9 = MicroUriSerializer.instance().deserialize(byte9);
        assertTrue(uri9.isEmpty());
    }

}
