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
package org.eclipse.uprotocol.uri.serializer;

import com.google.protobuf.ByteString;
import org.eclipse.uprotocol.uri.builder.UResourceBuilder;
import org.eclipse.uprotocol.uri.validator.UriValidator;
import org.eclipse.uprotocol.v1.UAuthority;
import org.eclipse.uprotocol.v1.UEntity;
import org.eclipse.uprotocol.v1.UResource;
import org.eclipse.uprotocol.v1.UUri;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MicroUriSerializerTest {


    @Test
    @DisplayName("Test serialize and deserialize empty content")
    public void test_empty() {

        byte[] bytes = MicroUriSerializer.instance().serialize(UUri.getDefaultInstance());

        assertEquals(bytes.length, 0);

        UUri uri2 = MicroUriSerializer.instance().deserialize(bytes);
        assertTrue(UriValidator.isEmpty(uri2));
    }

    @Test
    @DisplayName("Test serialize and deserialize null content")
    public void test_null() {
        byte[] bytes = MicroUriSerializer.instance().serialize(null);

        assertEquals(bytes.length, 0);

        UUri uri2 = MicroUriSerializer.instance().deserialize(null);
        assertTrue(UriValidator.isEmpty(uri2));
    }

    @Test
    @DisplayName("Test happy path Byte serialization of local UUri")
    public void test_serialize_uri() {

        UUri uri = UUri.newBuilder().setEntity(UEntity.newBuilder().setId(29999).setVersionMajor(254).build()).setResource(UResource.newBuilder().setId(19999).build()).build();


        byte[] bytes = MicroUriSerializer.instance().serialize(uri);
        UUri uri2 = MicroUriSerializer.instance().deserialize(bytes);
        assertTrue(UriValidator.isMicroForm(uri));
        assertTrue(bytes.length > 0);
        assertEquals(uri, uri2);
    }

    @Test
    @DisplayName("Test Serialize a remote UUri to micro without the address")
    public void test_serialize_remote_uri_without_address() {
        UUri uri = UUri.newBuilder().setAuthority(UAuthority.newBuilder().setName("vcu.vin").build()).setEntity(UEntity.newBuilder().setId(29999).setVersionMajor(254).build()).setResource(UResource.newBuilder().setId(19999).build()).build();

        byte[] bytes = MicroUriSerializer.instance().serialize(uri);
        assertTrue(bytes.length == 0);
    }

    @Test
    @DisplayName("Test serialize Uri missing uE ID")
    public void test_serialize_uri_missing_ids() {
        UUri uri = UUri.newBuilder().setEntity(UEntity.newBuilder().setName("hartley").build()).setResource(UResourceBuilder.forRpcResponse()).build();

        byte[] bytes = MicroUriSerializer.instance().serialize(uri);
        assertTrue(bytes.length == 0);
    }

    @Test
    @DisplayName("Test serialize Uri missing resource")
    public void test_serialize_uri_missing_resource_id() {
        UUri uri = UUri.newBuilder().setEntity(UEntity.newBuilder().setName("hartley").build()).build();

        byte[] bytes = MicroUriSerializer.instance().serialize(uri);
        assertTrue(bytes.length == 0);
    }


    @Test
    @DisplayName("Test deserialize bad micro uri - length")
    public void test_deserialize_bad_microuri_length() {
        byte[] badMicroUUri = new byte[]{0x1, 0x0, 0x0, 0x0, 0x0};
        UUri uuri = MicroUriSerializer.instance().deserialize(badMicroUUri);
        assertTrue(UriValidator.isEmpty(uuri));
        badMicroUUri = new byte[]{0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0};
        uuri = MicroUriSerializer.instance().deserialize(badMicroUUri);
        assertTrue(UriValidator.isEmpty(uuri));
    }

    @Test
    @DisplayName("Test deserialize bad micro uri - not version 1")
    public void test_deserialize_bad_microuri_not_version_1() {
        byte[] badMicroUUri = new byte[]{0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0};
        UUri uuri = MicroUriSerializer.instance().deserialize(badMicroUUri);
        assertTrue(UriValidator.isEmpty(uuri));
    }

    @Test
    @DisplayName("Test deserialize bad micro uri - not valid address type")
    public void test_deserialize_bad_microuri_not_valid_address_type() {
        byte[] badMicroUUri = new byte[]{0x1, 0x5, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0};
        UUri uuri = MicroUriSerializer.instance().deserialize(badMicroUUri);
        assertTrue(UriValidator.isEmpty(uuri));
    }

    @Test
    @DisplayName("Test deserialize bad micro uri - valid address type and invalid length")
    public void test_deserialize_bad_microuri_valid_address_type_invalid_length() {
        byte[] badMicroUUri = new byte[]{0x1, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0};
        UUri uuri = MicroUriSerializer.instance().deserialize(badMicroUUri);
        assertTrue(UriValidator.isEmpty(uuri));

        badMicroUUri = new byte[]{0x1, 0x1, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0};
        uuri = MicroUriSerializer.instance().deserialize(badMicroUUri);
        assertTrue(UriValidator.isEmpty(uuri));

        badMicroUUri = new byte[]{0x1, 0x2, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0};
        uuri = MicroUriSerializer.instance().deserialize(badMicroUUri);
        assertTrue(UriValidator.isEmpty(uuri));

    }

    @Test
    @DisplayName("Test serialize with good IPv4 based authority")
    public void test_serialize_good_ipv4_based_authority() throws UnknownHostException {
        UUri uri = UUri.newBuilder().setAuthority(UAuthority.newBuilder().setIp(ByteString.copyFrom(InetAddress.getByName("10.0.3.3").getAddress())).build()).setEntity(UEntity.newBuilder().setId(29999).setVersionMajor(254).build()).setResource(UResourceBuilder.forRpcRequest(99)).build();
        byte[] bytes = MicroUriSerializer.instance().serialize(uri);
        UUri uri2 = MicroUriSerializer.instance().deserialize(bytes);
        assertTrue(bytes.length > 0);
        assertTrue(UriValidator.isMicroForm(uri));
        assertTrue(UriValidator.isMicroForm(uri2));
        assertEquals(uri.toString(), uri2.toString());
        assertTrue(uri.equals(uri2));
    }

    @Test
    @DisplayName("Test serialize with good IPv6 based authority")
    public void test_serialize_good_ipv6_based_authority() throws UnknownHostException {
        UUri uri = UUri.newBuilder().setAuthority(UAuthority.newBuilder().setIp(ByteString.copyFrom(InetAddress.getByName("2001:0db8:85a3:0000:0000:8a2e:0370:7334").getAddress())).build()).setEntity(UEntity.newBuilder().setId(29999).setVersionMajor(254).build()).setResource(UResource.newBuilder().setId(19999).build()).build();
        byte[] bytes = MicroUriSerializer.instance().serialize(uri);
        UUri uri2 = MicroUriSerializer.instance().deserialize(bytes);
        assertTrue(UriValidator.isMicroForm(uri));
        assertTrue(bytes.length > 0);
        assertTrue(uri.equals(uri2));
    }


    @Test
    @DisplayName("Test serialize with ID based authority")
    public void test_serialize_id_based_authority() {
        int size = 13;
        byte[] byteArray = new byte[size];
        // Assign values to the elements of the byte array
        for (int i = 0; i < size; i++) {
            byteArray[i] = (byte) (i);
        }
        UUri uri = UUri.newBuilder().setAuthority(UAuthority.newBuilder().setId(ByteString.copyFrom(byteArray)).build()).setEntity(UEntity.newBuilder().setId(29999).setVersionMajor(254).build()).setResource(UResource.newBuilder().setId(19999).build()).build();
        byte[] bytes = MicroUriSerializer.instance().serialize(uri);
        UUri uri2 = MicroUriSerializer.instance().deserialize(bytes);
        assertTrue(UriValidator.isMicroForm(uri));
        assertTrue(bytes.length > 0);
        assertTrue(uri.equals(uri2));
    }

    @Test
    @DisplayName("Test serialize with bad length IP based authority")
    public void test_serialize_bad_length_ip_based_authority() throws UnknownHostException {
        byte[] byteArray = {127, 1, 23, 123, 12, 6};
        UUri uri = UUri.newBuilder().setAuthority(UAuthority.newBuilder().setIp(ByteString.copyFrom(byteArray)).build()).setEntity(UEntity.newBuilder().setId(29999).setVersionMajor(254).build()).setResource(UResource.newBuilder().setId(19999).build()).build();
        byte[] bytes = MicroUriSerializer.instance().serialize(uri);
        assertTrue(bytes.length == 0);
    }


    @Test
    @DisplayName("Test serialize with ID based authority")
    public void test_serialize_id_size_255_based_authority() {
        int size = 129;
        byte[] byteArray = new byte[size];
        // Assign values to the elements of the byte array
        for (int i = 0; i < size; i++) {
            byteArray[i] = (byte) (i);
        }
        UUri uri = UUri.newBuilder().setAuthority(UAuthority.newBuilder().setId(ByteString.copyFrom(byteArray)).build()).setEntity(UEntity.newBuilder().setId(29999).setVersionMajor(254).build()).setResource(UResource.newBuilder().setId(19999).build()).build();
        byte[] bytes = MicroUriSerializer.instance().serialize(uri);
        assertEquals(bytes.length, 9+size);
        UUri uri2 = MicroUriSerializer.instance().deserialize(bytes);
        assertTrue(UriValidator.isMicroForm(uri));
        assertTrue(uri.equals(uri2));
    }

}
