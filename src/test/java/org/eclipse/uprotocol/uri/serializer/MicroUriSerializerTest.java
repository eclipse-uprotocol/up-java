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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Base64;

import com.google.protobuf.ByteString;
import org.eclipse.uprotocol.uri.builder.UResourceBuilder;
import org.eclipse.uprotocol.uri.validator.UriValidator;
import org.eclipse.uprotocol.v1.UAuthority;
import org.eclipse.uprotocol.v1.UEntity;
import org.eclipse.uprotocol.v1.UResource;
import org.eclipse.uprotocol.v1.UUri;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

import static org.junit.jupiter.api.Assertions.*;

public class MicroUriSerializerTest
 {

   
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
        
        UUri uri = UUri.newBuilder()
            .setEntity(UEntity.newBuilder().setId(29999).setVersionMajor(254).build())
            .setResource(UResource.newBuilder().setId(19999).build())
            .build();


        byte[] bytes = MicroUriSerializer.instance().serialize(uri);
        UUri uri2 = MicroUriSerializer.instance().deserialize(bytes);

        assertEquals(uri, uri2);
    }
    
    @Test
    @DisplayName("Test Serialize a remote UUri to micro without the address")
    public void test_serialize_remote_uri_without_address() {
        UUri uri = UUri.newBuilder()
            .setAuthority(UAuthority.newBuilder().setName("vcu.vin").build())
            .setEntity(UEntity.newBuilder().setId(29999).setVersionMajor(254).build())
            .setResource(UResource.newBuilder().setId(19999).build())
            .build();

        byte[] bytes = MicroUriSerializer.instance().serialize(uri);
        assertTrue(bytes.length == 0);
    }
    
    @Test
    @DisplayName("Test serialize Uri missing uE ID")
    public void test_serialize_uri_missing_ids() {
        UUri uri = UUri.newBuilder()
            .setEntity(UEntity.newBuilder().setName("hartley").build())
            .setResource(UResourceBuilder.forRpcResponse())
            .build();

        byte[] bytes = MicroUriSerializer.instance().serialize(uri);
        assertTrue(bytes.length == 0);
    }

    @Test
    @DisplayName("Test serialize Uri missing resource")
    public void test_serialize_uri_missing_resource_id() {
        UUri uri = UUri.newBuilder()
            .setEntity(UEntity.newBuilder().setName("hartley").build())
            .build();

        byte[] bytes = MicroUriSerializer.instance().serialize(uri);
        assertTrue(bytes.length == 0);
    }


     @Test
     @DisplayName("Test deserialize bad micro uri - length")
     public void test_deserialize_bad_microuri_length() {
         byte[] badMicroUUri = new byte[] {0x1, 0x0, 0x0, 0x0, 0x0};
         UUri uuri = MicroUriSerializer.instance().deserialize(badMicroUUri);
         assertTrue(UriValidator.isEmpty(uuri));
         badMicroUUri = new byte[] {0x0, 0x0, 0x0, 0x0, 0x0,0x0, 0x0, 0x0, 0x0, 0x0};
         uuri = MicroUriSerializer.instance().deserialize(badMicroUUri);
         assertTrue(UriValidator.isEmpty(uuri));
     }

     @Test
     @DisplayName("Test deserialize bad micro uri - not version 1")
     public void test_deserialize_bad_microuri_not_version_1() {
         byte[] badMicroUUri = new byte[] {0x0, 0x0, 0x0, 0x0, 0x0,0x0, 0x0, 0x0, 0x0, 0x0};
         UUri uuri = MicroUriSerializer.instance().deserialize(badMicroUUri);
         assertTrue(UriValidator.isEmpty(uuri));
     }
     @Test
     @DisplayName("Test deserialize bad micro uri - not valid address type")
     public void test_deserialize_bad_microuri_not_valid_address_type() {
         byte[] badMicroUUri = new byte[] {0x1, 0x5, 0x0, 0x0, 0x0,0x0, 0x0, 0x0, 0x0, 0x0};
         UUri uuri = MicroUriSerializer.instance().deserialize(badMicroUUri);
         assertTrue(UriValidator.isEmpty(uuri));
     }

     @Test
     @DisplayName("Test deserialize bad micro uri - valid address type and invalid length")
     public void test_deserialize_bad_microuri_valid_address_type_invalid_length() {
         byte[] badMicroUUri = new byte[] {0x1, 0x0, 0x0, 0x0, 0x0,0x0, 0x0, 0x0, 0x0, 0x0};
         UUri uuri = MicroUriSerializer.instance().deserialize(badMicroUUri);
         assertTrue(UriValidator.isEmpty(uuri));

         badMicroUUri = new byte[] {0x1, 0x1, 0x0, 0x0, 0x0,0x0, 0x0, 0x0, 0x0, 0x0};
         uuri= MicroUriSerializer.instance().deserialize(badMicroUUri);
         assertTrue(UriValidator.isEmpty(uuri));

         badMicroUUri = new byte[] {0x1, 0x2, 0x0, 0x0, 0x0,0x0, 0x0, 0x0, 0x0, 0x0};
         uuri= MicroUriSerializer.instance().deserialize(badMicroUUri);
         assertTrue(UriValidator.isEmpty(uuri));

     }

     @Test
     @DisplayName("Test serialize with good IPv4 based authority")
     public void test_serialize_ip_based_authority() throws UnknownHostException {

         UUri uri = UUri.newBuilder()
                 .setAuthority(UAuthority.newBuilder().setIp(ByteString.copyFrom(InetAddress.getByName("10.0.3.3").getAddress())).build())
                 .setEntity(UEntity.newBuilder().setId(29999).setVersionMajor(254).build())
                 .setResource(UResource.newBuilder().setId(19999).build())
                 .build();
         byte[] bytes = MicroUriSerializer.instance().serialize(uri);
         UUri uri2= MicroUriSerializer.instance().deserialize(bytes);
         assertTrue(UriValidator.isMicroForm(uri));
         assertTrue(bytes.length > 0);
         assertTrue(uri.equals(uri2));
     }

 }
