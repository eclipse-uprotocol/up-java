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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Base64;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class MicroUriSerializerTest
 {
    /*
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
        UEntity use = UEntity.microFormat((short)29999, 254);
        UResource uResource = UResource.microFormat((short)19999);

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
    @DisplayName("Test Serialize a remote UUri to micro without the address")
    public void test_serialize_remote_uri_without_address() {
        UAuthority uAuthority = UAuthority.longRemote("vcu", "vin");
        UEntity use = UEntity.microFormat((short)2, 1);
        UResource uResource = UResource.microFormat((short)3);

        UUri uri = new UUri(uAuthority, use, uResource);

        byte[] bytes = MicroUriSerializer.instance().serialize(uri);
        assertTrue(bytes.length == 0);
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
    @DisplayName("Test deserialize with invalid local micro uri")
    public void test_deserialize_with_valid_local_uri() {
        
        byte[] bytes = {0x1, 0x0, 0x0, 0x5, 0x0, 0x2, 0x1, 0x0};
        
        UUri uri = MicroUriSerializer.instance().deserialize(bytes);
        assertFalse(uri.isEmpty());
        assertTrue(uri.isMicroForm());
        assertFalse(uri.isResolved());
        assertFalse(uri.isLongForm());
        assertTrue(uri.getAuthority().isLocal());
        assertTrue(uri.getEntity().version().isPresent());
        assertEquals(uri.getEntity().version().get(),(short)1);
        assertTrue(uri.getEntity().id().isPresent());
        assertEquals(uri.getEntity().id().get(), (short)2);
        assertTrue(uri.getResource().id().isPresent());
        assertEquals(uri.getResource().id().get(), (short)5);
    }

    @Test
    @DisplayName("Test deserialize with valid IPv4 micro uri")
    public void test_deserialize_with_valid_ipv4_uri() {
        
        byte[] bytes = {0x1, 0x1, 0x0, 0x5, 0x0, 0x2, 0x1, 0x0, (byte)192, (byte)168, 1, (byte)100};
        
        UUri uri = MicroUriSerializer.instance().deserialize(bytes);
        assertFalse(uri.isEmpty());
        assertTrue(uri.isMicroForm());
        assertFalse(uri.isResolved());
        assertFalse(uri.isLongForm());
        assertTrue(uri.getAuthority().isRemote());
        assertTrue(uri.getEntity().version().isPresent());
        assertEquals(uri.getEntity().version().get(),(short)1);
        assertTrue(uri.getEntity().id().isPresent());
        assertEquals(uri.getEntity().id().get(), (short)2);
        assertTrue(uri.getResource().id().isPresent());
        assertEquals(uri.getResource().id().get(), (short)5);
        assertTrue(uri.getAuthority().address().isPresent());
        try {
            assertEquals(uri.getAuthority().address().get(), Inet4Address.getByName("192.168.1.100"));
        } catch (UnknownHostException e) {
            assertTrue(false);
        }
    }

    @Test
    @DisplayName("Test deserialize with valid IPv6 micro uri")
    public void test_deserialize_with_valid_ipv6_uri() {
        try {
            InetAddress ipv6 = InetAddress.getByName("2001:db8:85a3:0:0:8a2e:370:7334");
            byte[] ipv6Bytes = ipv6.getAddress();

            byte[] header = {0x1, 0x2, 0x0, 0x5, 0x0, 0x2, 0x1, 0x0};
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(header);
            outputStream.write(ipv6Bytes);

            byte[] bytes = outputStream.toByteArray();

            UUri uri = MicroUriSerializer.instance().deserialize(bytes);
            assertFalse(uri.isEmpty());
            assertTrue(uri.isMicroForm());
            assertFalse(uri.isResolved());
            assertFalse(uri.isLongForm());
            assertTrue(uri.getAuthority().isRemote());
            assertTrue(uri.getEntity().version().isPresent());
            assertEquals(uri.getEntity().version().get(),(short)1);
            assertTrue(uri.getEntity().id().isPresent());
            assertEquals(uri.getEntity().id().get(), (short)2);
            assertTrue(uri.getResource().id().isPresent());
            assertEquals(uri.getResource().id().get(), (short)5);
            assertTrue(uri.getAuthority().address().isPresent());
            assertEquals(uri.getAuthority().address().get(), ipv6);
        } catch (Exception e) {
            assertTrue(false);
        }
    }

    @Test
    @DisplayName("Test deserialize with invalid version")
    public void test_deserialize_with_invalid_version() {
        byte[] bytes = {0x9, 0x0, 0x0, 0x5, 0x0, 0x2, 0x1, 0x0};
        
        UUri uri = MicroUriSerializer.instance().deserialize(bytes);
        assertTrue(uri.isEmpty());
        assertFalse(uri.isMicroForm());
        assertFalse(uri.isResolved());
    }

    @Test
    @DisplayName("Test deserialize with invalid type")
    public void test_deserialize_with_invalid_type() {
        byte[] bytes = {0x1, 0x9, 0x0, 0x5, 0x0, 0x2, 0x1, 0x0};
        
        UUri uri = MicroUriSerializer.instance().deserialize(bytes);
        assertTrue(uri.isEmpty());
        assertFalse(uri.isMicroForm());
        assertFalse(uri.isResolved());
    }

    @Test
    @DisplayName("Test deserialize with wrong size for local micro URI")
    public void test_deserialize_with_wrong_size_for_local_micro_uri() {
        byte[] bytes = {0x1, 0x0, 0x0, 0x5, 0x0, 0x2, 0x1, 0x0, 0x0};
        
        UUri uri = MicroUriSerializer.instance().deserialize(bytes);
        assertTrue(uri.isEmpty());
        assertFalse(uri.isMicroForm());
        assertFalse(uri.isResolved());
    }

    @Test
    @DisplayName("Test deserialize with wrong size for IPv4 micro URI")
    public void test_deserialize_with_wrong_size_for_ipv4_micro_uri() {
        byte[] bytes = {0x1, 0x1, 0x0, 0x5, 0x0, 0x2, 0x1, 0x0, 0x0, (byte)192, (byte)168, 1, (byte)100};
        
        UUri uri = MicroUriSerializer.instance().deserialize(bytes);
        assertTrue(uri.isEmpty());
        assertFalse(uri.isMicroForm());
        assertFalse(uri.isResolved());
    }

    @Test
    @DisplayName("Test deserialize with wrong size for IPv6 micro URI")
    public void test_deserialize_with_wrong_size_for_ipv6_micro_uri() {
        try {
            byte[] ipv6Bytes = new byte[30];

            byte[] header = {0x1, 0x2, 0x0, 0x5, 0x0, 0x2, 0x1, 0x0};
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(header);
            outputStream.write(ipv6Bytes);

            byte[] bytes = outputStream.toByteArray();

            UUri uri = MicroUriSerializer.instance().deserialize(bytes);
            assertTrue(uri.isEmpty());
            assertFalse(uri.isMicroForm());
            assertFalse(uri.isResolved());
        } catch (Exception e) {
            assertTrue(false);
        }
    }

    @Test
    @DisplayName("Test reading characters in byte array")
    public void test_byte_tostring_contains_veh() {
        
        byte[] bytes = "1HGBH41JXMN109186.veh".getBytes();
        String source = new String(bytes);
        assertEquals(bytes.length, source.length());
        assertTrue(source.contains("veh"));
        assertTrue(source.contains("1HGBH41JXMN109186"));
        assertTrue(source.toLowerCase().contains("JXMN109186".toLowerCase()));
    }

    @Test
    @DisplayName("Test building whole UUri but replacing address for the vin.veh")
    public void test_bytearray_using_micro_uuri() throws IOException {
        
        // need to make it fit in the address portion till we implement the id type
        byte[] address = "41JXMN109186.veh".getBytes();
        assertEquals(address.length, 16);

        // Test building a whole 
        UAuthority uAuthority = UAuthority.microRemote(InetAddress.getByName("2001:db8:85a3:0:0:8a2e:370:7334"));
        UEntity use = UEntity.microFormat((short)29999, 254);
        UResource uResource = UResource.microFormat((short)19999);
        UUri uri = new UUri(uAuthority, use, uResource);

        byte[] bytes = MicroUriSerializer.instance().serialize(uri);
        
        // Below is ugly but it works for now to prove a point
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(bytes, 0, 8);
        outputStream.write(address);
        byte[] newBytes = outputStream.toByteArray();
        String source = Base64.getEncoder().encodeToString(newBytes);
        assertTrue(source.contains("veh"));
        assertTrue(source.contains("41JXMN109186"));

        UUri uri2 = MicroUriSerializer.instance().deserialize(Base64.getDecoder().decode(source));
        assertEquals(bytes.length, MicroUriSerializer.IPV6_MICRO_URI_LENGTH);
        assertEquals(newBytes.length, MicroUriSerializer.IPV6_MICRO_URI_LENGTH);
        
    
        // Validate reading bytes from the string
        assertFalse(uri2.isEmpty());
        assertTrue(uri2.isMicroForm());
        assertTrue(uri2.getAuthority().isRemote());
        assertTrue(uri2.getEntity().version().isPresent());
        assertEquals(uri2.getEntity().version().get(),254);
        assertTrue(uri2.getEntity().id().isPresent());
        assertEquals(uri2.getEntity().id().get(), (short)29999);
        assertTrue(uri2.getResource().id().isPresent());
        assertEquals(uri2.getResource().id().get(), (short)19999);
    }
 */
}
