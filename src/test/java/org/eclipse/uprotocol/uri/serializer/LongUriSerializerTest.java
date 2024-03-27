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

import org.eclipse.uprotocol.uri.factory.UResourceBuilder;
import org.eclipse.uprotocol.uri.validator.UriValidator;
import org.eclipse.uprotocol.v1.UAuthority;
import org.eclipse.uprotocol.v1.UEntity;
import org.eclipse.uprotocol.v1.UResource;
import org.eclipse.uprotocol.v1.UUri;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


public class LongUriSerializerTest {


    @Test
    @DisplayName("Test using the serializers")
    public void test_using_the_serializers() {
        final UUri uri = UUri.newBuilder().setEntity(UEntity.newBuilder().setName("hartley")).setResource(UResourceBuilder.forRpcRequest("raise")).build();
        final String strUri = LongUriSerializer.instance().serialize(uri);
        assertEquals("/hartley//rpc.raise", strUri);
        final UUri uri2 = LongUriSerializer.instance().deserialize(strUri);
        assertEquals(uri, uri2);
    }

    @Test
    @DisplayName("Test parse uProtocol uri that is null")
    public void test_parse_protocol_uri_when_is_null() {
        UUri uri = LongUriSerializer.instance().deserialize(null);
        assertTrue(UriValidator.isEmpty(uri));
        assertFalse(UriValidator.isResolved(uri));
        assertFalse(UriValidator.isLongForm(uri));
    }

    @Test
    @DisplayName("Test parse uProtocol uri that is empty string")
    public void test_parse_protocol_uri_when_is_empty_string() {
        String uri = "";
        UUri uuri = LongUriSerializer.instance().deserialize(uri);
        assertTrue(UriValidator.isEmpty(uuri));

        String uri2 = LongUriSerializer.instance().serialize(null);
        assertTrue(uri2.isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with schema and slash")
    public void test_parse_protocol_uri_with_schema_and_slash() {
        String uri = "/";
        UUri uuri = LongUriSerializer.instance().deserialize(uri);
        assertFalse(uuri.hasAuthority());
        assertTrue(UriValidator.isEmpty(uuri));
        assertFalse(uuri.hasResource());
        assertFalse(uuri.hasEntity());

        String uri2 = LongUriSerializer.instance().serialize(UUri.newBuilder().build());
        assertTrue(uri2.isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with schema and double slash")
    public void test_parse_protocol_uri_with_schema_and_double_slash() {
        String uri = "//";
        UUri uuri = LongUriSerializer.instance().deserialize(uri);
        assertFalse(uuri.hasAuthority());
        assertFalse(uuri.hasResource());
        assertFalse(uuri.hasEntity());
        assertTrue(UriValidator.isEmpty(uuri));
    }

    @Test
    @DisplayName("Test parse uProtocol uri with schema and 3 slash and something")
    public void test_parse_protocol_uri_with_schema_and_3_slash_and_something() {
        String uri = "///body.access";
        UUri uuri = LongUriSerializer.instance().deserialize(uri);
        assertFalse(uuri.hasAuthority());
        assertFalse(uuri.hasResource());
        assertFalse(uuri.hasEntity());
        assertTrue(UriValidator.isEmpty(uuri));
        assertNotEquals("body.access", uuri.getEntity().getName());
        assertEquals(0, uuri.getEntity().getVersionMajor());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with schema and 4 slash and something")
    public void test_parse_protocol_uri_with_schema_and_4_slash_and_something() {
        String uri = "////body.access";
        UUri uuri = LongUriSerializer.instance().deserialize(uri);
        assertFalse(UriValidator.isRemote(uuri.getAuthority()));
        assertFalse(uuri.hasResource());
        assertFalse(uuri.hasEntity());
        assertTrue(uuri.getEntity().getName().isBlank());
        assertEquals(0, uuri.getEntity().getVersionMajor());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with schema and 5 slash and something")
    public void test_parse_protocol_uri_with_schema_and_5_slash_and_something() {
        String uri = "/////body.access";
        UUri uuri = LongUriSerializer.instance().deserialize(uri);
        assertFalse(UriValidator.isRemote(uuri.getAuthority()));
        assertFalse(uuri.hasResource());
        assertFalse(uuri.hasEntity());
        assertTrue(UriValidator.isEmpty(uuri));
    }

    @Test
    @DisplayName("Test parse uProtocol uri with schema and 6 slash and something")
    public void test_parse_protocol_uri_with_schema_and_6_slash_and_something() {
        String uri = "//////body.access";
        UUri uuri = LongUriSerializer.instance().deserialize(uri);
        assertFalse(UriValidator.isRemote(uuri.getAuthority()));
        assertTrue(UriValidator.isEmpty(uuri));

    }

    @Test
    @DisplayName("Test parse uProtocol uri with local service no version")
    public void test_parse_protocol_uri_with_local_service_no_version() {
        String uri = "/body.access";
        UUri uuri = LongUriSerializer.instance().deserialize(uri);
        assertFalse(UriValidator.isRemote(uuri.getAuthority()));
        assertEquals("body.access", uuri.getEntity().getName());
        assertEquals(0, uuri.getEntity().getVersionMajor());
        assertEquals(0, uuri.getEntity().getVersionMinor());
        assertFalse(uuri.hasResource());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with local service with version")
    public void test_parse_protocol_uri_with_local_service_with_version() {
        String uri = "/body.access/1";
        UUri uuri = LongUriSerializer.instance().deserialize(uri);
        assertFalse(UriValidator.isRemote(uuri.getAuthority()));
        assertEquals("body.access", uuri.getEntity().getName());
        assertEquals(1, uuri.getEntity().getVersionMajor());
        assertFalse(uuri.hasResource());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with local service no version with resource name only")
    public void test_parse_protocol_uri_with_local_service_no_version_with_resource_name_only() {
        String uri = "/body.access//door";
        UUri uuri = LongUriSerializer.instance().deserialize(uri);
        assertFalse(UriValidator.isRemote(uuri.getAuthority()));
        assertEquals("body.access", uuri.getEntity().getName());
        assertEquals(0, uuri.getEntity().getVersionMajor());
        assertEquals(0, uuri.getEntity().getVersionMinor());
        assertEquals("door", uuri.getResource().getName());
        assertTrue(uuri.getResource().getInstance().isEmpty());
        assertTrue(uuri.getResource().getMessage().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with local service with version with resource name only")
    public void test_parse_protocol_uri_with_local_service_with_version_with_resource_name_only() {
        String uri = "/body.access/1/door";
        UUri uuri = LongUriSerializer.instance().deserialize(uri);
        assertFalse(UriValidator.isRemote(uuri.getAuthority()));
        assertEquals("body.access", uuri.getEntity().getName());
        assertEquals(1, uuri.getEntity().getVersionMajor());
        assertEquals("door", uuri.getResource().getName());
        assertTrue(uuri.getResource().getInstance().isEmpty());
        assertTrue(uuri.getResource().getMessage().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with local service no version with resource and instance only")
    public void test_parse_protocol_uri_with_local_service_no_version_with_resource_with_instance() {
        String uri = "/body.access//door.front_left";
        UUri uuri = LongUriSerializer.instance().deserialize(uri);
        assertFalse(UriValidator.isRemote(uuri.getAuthority()));
        assertEquals("body.access", uuri.getEntity().getName());
        assertEquals(0, uuri.getEntity().getVersionMajor());
        assertEquals("door", uuri.getResource().getName());
        assertFalse(uuri.getResource().getInstance().isEmpty());
        assertEquals("front_left", uuri.getResource().getInstance());
        assertTrue(uuri.getResource().getMessage().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with local service with version with resource and instance only")
    public void test_parse_protocol_uri_with_local_service_with_version_with_resource_with_getMessage() {
        String uri = "/body.access/1/door.front_left";
        UUri uuri = LongUriSerializer.instance().deserialize(uri);
        assertFalse(UriValidator.isRemote(uuri.getAuthority()));
        assertEquals("body.access", uuri.getEntity().getName());
        assertNotEquals(0, uuri.getEntity().getVersionMajor());
        assertEquals(1, uuri.getEntity().getVersionMajor());
        assertEquals("door", uuri.getResource().getName());
        assertFalse(uuri.getResource().getInstance().isEmpty());
        assertEquals("front_left", uuri.getResource().getInstance());
        assertTrue(uuri.getResource().getMessage().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with local service no version with resource with instance and message")
    public void test_parse_protocol_uri_with_local_service_no_version_with_resource_with_instance_and_getMessage() {
        String uri = "/body.access//door.front_left#Door";
        UUri uuri = LongUriSerializer.instance().deserialize(uri);
        assertFalse(UriValidator.isRemote(uuri.getAuthority()));
        assertEquals("body.access", uuri.getEntity().getName());
        assertEquals(0, uuri.getEntity().getVersionMajor());
        assertEquals("door", uuri.getResource().getName());
        assertFalse(uuri.getResource().getInstance().isEmpty());
        assertEquals("front_left", uuri.getResource().getInstance());
        assertFalse(uuri.getResource().getMessage().isEmpty());
        assertEquals("Door", uuri.getResource().getMessage());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with local service with version with resource with instance and message")
    public void test_parse_protocol_uri_with_local_service_with_version_with_resource_with_instance_and_getMessage() {
        String uri = "/body.access/1/door.front_left#Door";
        UUri uuri = LongUriSerializer.instance().deserialize(uri);
        assertFalse(UriValidator.isRemote(uuri.getAuthority()));
        assertEquals("body.access", uuri.getEntity().getName());
        assertNotEquals(0, uuri.getEntity().getVersionMajor());
        assertEquals(1, uuri.getEntity().getVersionMajor());
        assertEquals("door", uuri.getResource().getName());
        assertFalse(uuri.getResource().getInstance().isEmpty());
        assertEquals("front_left", uuri.getResource().getInstance());
        assertFalse(uuri.getResource().getMessage().isEmpty());
        assertEquals("Door", uuri.getResource().getMessage());
    }

    @Test
    @DisplayName("Test parse uProtocol RPC uri with local service no version")
    public void test_parse_protocol_rpc_uri_with_local_service_no_version() {
        String uri = "/petapp//rpc.response";
        UUri uuri = LongUriSerializer.instance().deserialize(uri);
        assertFalse(UriValidator.isRemote(uuri.getAuthority()));
        assertEquals("petapp", uuri.getEntity().getName());
        assertEquals(0, uuri.getEntity().getVersionMajor());
        assertEquals("rpc", uuri.getResource().getName());
        assertFalse(uuri.getResource().getInstance().isEmpty());
        assertEquals("response", uuri.getResource().getInstance());
        assertTrue(uuri.getResource().getMessage().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol RPC uri with local service with version")
    public void test_parse_protocol_rpc_uri_with_local_service_with_version() {
        String uri = "/petapp/1/rpc.response";
        UUri uuri = LongUriSerializer.instance().deserialize(uri);
        assertFalse(UriValidator.isRemote(uuri.getAuthority()));
        assertEquals("petapp", uuri.getEntity().getName());
        assertNotEquals(0, uuri.getEntity().getVersionMajor());
        assertEquals(1, uuri.getEntity().getVersionMajor());
        assertEquals("rpc", uuri.getResource().getName());
        assertFalse(uuri.getResource().getInstance().isEmpty());
        assertEquals("response", uuri.getResource().getInstance());
        assertTrue(uuri.getResource().getMessage().isEmpty());
    }


    @Test
    @DisplayName("Test parse uProtocol uri with microRemote service , name with device and domain")
    public void test_parse_protocol_uri_with_remote_service_only_device_and_domain() {
        String uri = "//VCU.MY_CAR_VIN";
        UUri uuri = LongUriSerializer.instance().deserialize(uri);
        assertTrue(UriValidator.isRemote(uuri.getAuthority()));
        assertFalse(uuri.getAuthority().getName().isBlank());
        assertEquals("VCU.MY_CAR_VIN", uuri.getAuthority().getName());

    }

    @Test
    @DisplayName("Test parse uProtocol uri with microRemote service only device and cloud domain")
    public void test_parse_protocol_uri_with_remote_service_only_device_and_cloud_domain() {
        String uri = "//cloud.uprotocol.example.com";
        UUri uuri = LongUriSerializer.instance().deserialize(uri);
        assertTrue(UriValidator.isRemote(uuri.getAuthority()));
        assertFalse(uuri.getAuthority().getName().isBlank());
        assertEquals("cloud.uprotocol.example.com", uuri.getAuthority().getName());
        assertFalse(uuri.hasEntity());
        assertFalse(uuri.hasResource());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with microRemote service no version")
    public void test_parse_protocol_uri_with_remote_service_no_version() {
        String uri = "//VCU.MY_CAR_VIN/body.access";
        UUri uuri = LongUriSerializer.instance().deserialize(uri);
        assertTrue(UriValidator.isRemote(uuri.getAuthority()));
        assertFalse(uuri.getAuthority().getName().isBlank());
        assertEquals("VCU.MY_CAR_VIN", uuri.getAuthority().getName());
        assertFalse(uuri.getAuthority().getName().isBlank());
        assertEquals("body.access", uuri.getEntity().getName());
        assertEquals(0, uuri.getEntity().getVersionMajor());
        assertFalse(uuri.hasResource());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with microRemote cloud service no version")
    public void test_parse_protocol_uri_with_remote_cloud_service_no_version() {
        String uri = "//cloud.uprotocol.example.com/body.access";
        UUri uuri = LongUriSerializer.instance().deserialize(uri);
        assertTrue(UriValidator.isRemote(uuri.getAuthority()));
        assertFalse(uuri.getAuthority().getName().isBlank());
        assertEquals("cloud.uprotocol.example.com", uuri.getAuthority().getName());
        assertFalse(uuri.getAuthority().getName().isBlank());
        assertEquals("body.access", uuri.getEntity().getName());
        assertEquals(0, uuri.getEntity().getVersionMajor());
        assertFalse(uuri.hasResource());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with microRemote service with version")
    public void test_parse_protocol_uri_with_remote_service_with_version() {
        String uri = "//VCU.MY_CAR_VIN/body.access/1";
        UUri uuri = LongUriSerializer.instance().deserialize(uri);
        assertTrue(UriValidator.isRemote(uuri.getAuthority()));
        assertFalse(uuri.getAuthority().getName().isBlank());
        assertEquals("VCU.MY_CAR_VIN", uuri.getAuthority().getName());
        assertFalse(uuri.getAuthority().getName().isBlank());
        assertEquals("body.access", uuri.getEntity().getName());
        assertNotEquals(0, uuri.getEntity().getVersionMajor());
        assertEquals(1, uuri.getEntity().getVersionMajor());
        assertFalse(uuri.hasResource());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with microRemote cloud service with version")
    public void test_parse_protocol_uri_with_remote_cloud_service_with_version() {
        String uri = "//cloud.uprotocol.example.com/body.access/1";
        UUri uuri = LongUriSerializer.instance().deserialize(uri);
        assertTrue(UriValidator.isRemote(uuri.getAuthority()));
        assertFalse(uuri.getAuthority().getName().isBlank());
        assertEquals("cloud.uprotocol.example.com", uuri.getAuthority().getName());
        assertFalse(uuri.getAuthority().getName().isBlank());
        assertEquals("body.access", uuri.getEntity().getName());
        assertNotEquals(0, uuri.getEntity().getVersionMajor());
        assertEquals(1, uuri.getEntity().getVersionMajor());
        assertFalse(uuri.hasResource());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with microRemote service no version with resource name only")
    public void test_parse_protocol_uri_with_remote_service_no_version_with_resource_name_only() {
        String uri = "//VCU.MY_CAR_VIN/body.access//door";
        UUri uuri = LongUriSerializer.instance().deserialize(uri);
        assertTrue(UriValidator.isRemote(uuri.getAuthority()));
        assertFalse(uuri.getAuthority().getName().isBlank());
        assertEquals("VCU.MY_CAR_VIN", uuri.getAuthority().getName());
        assertFalse(uuri.getAuthority().getName().isBlank());
        assertEquals("body.access", uuri.getEntity().getName());
        assertEquals(0, uuri.getEntity().getVersionMajor());
        assertEquals("door", uuri.getResource().getName());
        assertTrue(uuri.getResource().getInstance().isEmpty());
        assertTrue(uuri.getResource().getMessage().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with microRemote cloud service no version with resource name only")
    public void test_parse_protocol_uri_with_remote_cloud_service_no_version_with_resource_name_only() {
        String uri = "//cloud.uprotocol.example.com/body.access//door";
        UUri uuri = LongUriSerializer.instance().deserialize(uri);
        assertTrue(UriValidator.isRemote(uuri.getAuthority()));
        assertFalse(uuri.getAuthority().getName().isBlank());
        assertEquals("cloud.uprotocol.example.com", uuri.getAuthority().getName());
        assertFalse(uuri.getAuthority().getName().isBlank());
        assertEquals("body.access", uuri.getEntity().getName());
        assertEquals(0, uuri.getEntity().getVersionMajor());
        assertEquals("door", uuri.getResource().getName());
        assertTrue(uuri.getResource().getInstance().isEmpty());
        assertTrue(uuri.getResource().getMessage().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with microRemote service with version with resource name only")
    public void test_parse_protocol_uri_with_remote_service_with_version_with_resource_name_only() {
        String uri = "//VCU.MY_CAR_VIN/body.access/1/door";
        UUri uuri = LongUriSerializer.instance().deserialize(uri);
        assertTrue(UriValidator.isRemote(uuri.getAuthority()));
        assertFalse(uuri.getAuthority().getName().isBlank());
        assertEquals("VCU.MY_CAR_VIN", uuri.getAuthority().getName());
        assertFalse(uuri.getAuthority().getName().isBlank());
        assertEquals("body.access", uuri.getEntity().getName());
        assertNotEquals(0, uuri.getEntity().getVersionMajor());
        assertEquals(1, uuri.getEntity().getVersionMajor());
        assertEquals("door", uuri.getResource().getName());
        assertTrue(uuri.getResource().getInstance().isEmpty());
        assertTrue(uuri.getResource().getMessage().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with microRemote cloud service with version with resource name only")
    public void test_parse_protocol_uri_with_remote_service_cloud_with_version_with_resource_name_only() {
        String uri = "//cloud.uprotocol.example.com/body.access/1/door";
        UUri uuri = LongUriSerializer.instance().deserialize(uri);
        assertTrue(UriValidator.isRemote(uuri.getAuthority()));
        assertFalse(uuri.getAuthority().getName().isBlank());
        assertEquals("cloud.uprotocol.example.com", uuri.getAuthority().getName());
        assertFalse(uuri.getAuthority().getName().isBlank());
        assertEquals("body.access", uuri.getEntity().getName());
        assertNotEquals(0, uuri.getEntity().getVersionMajor());
        assertEquals(1, uuri.getEntity().getVersionMajor());
        assertEquals("door", uuri.getResource().getName());
        assertTrue(uuri.getResource().getInstance().isEmpty());
        assertTrue(uuri.getResource().getMessage().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with microRemote service no version with resource and instance no message")
    public void test_parse_protocol_uri_with_remote_service_no_version_with_resource_and_instance_no_getMessage() {
        String uri = "//VCU.MY_CAR_VIN/body.access//door.front_left";
        UUri uuri = LongUriSerializer.instance().deserialize(uri);
        assertTrue(UriValidator.isRemote(uuri.getAuthority()));
        assertFalse(uuri.getAuthority().getName().isBlank());
        assertEquals("VCU.MY_CAR_VIN", uuri.getAuthority().getName());
        assertFalse(uuri.getAuthority().getName().isBlank());
        assertEquals("body.access", uuri.getEntity().getName());
        assertEquals(0, uuri.getEntity().getVersionMajor());
        assertEquals("door", uuri.getResource().getName());
        assertFalse(uuri.getResource().getInstance().isEmpty());
        assertEquals("front_left", uuri.getResource().getInstance());
        assertTrue(uuri.getResource().getMessage().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with microRemote service with version with resource and instance no message")
    public void test_parse_protocol_uri_with_remote_service_with_version_with_resource_and_instance_no_getMessage() {
        String uri = "//VCU.MY_CAR_VIN/body.access/1/door.front_left";
        UUri uuri = LongUriSerializer.instance().deserialize(uri);
        assertTrue(UriValidator.isRemote(uuri.getAuthority()));
        assertFalse(uuri.getAuthority().getName().isBlank());
        assertEquals("VCU.MY_CAR_VIN", uuri.getAuthority().getName());
        assertFalse(uuri.getAuthority().getName().isBlank());
        assertEquals("body.access", uuri.getEntity().getName());
        assertNotEquals(0, uuri.getEntity().getVersionMajor());
        assertEquals(1, uuri.getEntity().getVersionMajor());
        assertEquals("door", uuri.getResource().getName());
        assertFalse(uuri.getResource().getInstance().isEmpty());
        assertEquals("front_left", uuri.getResource().getInstance());
        assertTrue(uuri.getResource().getMessage().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with microRemote service no version with resource and instance and message")
    public void test_parse_protocol_uri_with_remote_service_no_version_with_resource_and_instance_and_getMessage() {
        String uri = "//VCU.MY_CAR_VIN/body.access//door.front_left#Door";
        UUri uuri = LongUriSerializer.instance().deserialize(uri);
        assertTrue(UriValidator.isRemote(uuri.getAuthority()));
        assertFalse(uuri.getAuthority().getName().isBlank());
        assertEquals("VCU.MY_CAR_VIN", uuri.getAuthority().getName());
        assertFalse(uuri.getAuthority().getName().isBlank());
        assertEquals("body.access", uuri.getEntity().getName());
        assertEquals(0, uuri.getEntity().getVersionMajor());
        assertEquals("door", uuri.getResource().getName());
        assertFalse(uuri.getResource().getInstance().isEmpty());
        assertEquals("front_left", uuri.getResource().getInstance());
        assertFalse(uuri.getResource().getMessage().isEmpty());
        assertEquals("Door", uuri.getResource().getMessage());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with microRemote cloud service no version with resource and instance and message")
    public void test_parse_protocol_uri_with_remote_cloud_service_no_version_with_resource_and_instance_and_getMessage() {
        String uri = "//cloud.uprotocol.example.com/body.access//door.front_left#Door";
        UUri uuri = LongUriSerializer.instance().deserialize(uri);
        assertTrue(UriValidator.isRemote(uuri.getAuthority()));
        assertFalse(uuri.getAuthority().getName().isBlank());
        assertEquals("cloud.uprotocol.example.com", uuri.getAuthority().getName());
        assertFalse(uuri.getAuthority().getName().isBlank());
        assertEquals("body.access", uuri.getEntity().getName());
        assertEquals(0, uuri.getEntity().getVersionMajor());
        assertEquals("door", uuri.getResource().getName());
        assertFalse(uuri.getResource().getInstance().isEmpty());
        assertEquals("front_left", uuri.getResource().getInstance());
        assertFalse(uuri.getResource().getMessage().isEmpty());
        assertEquals("Door", uuri.getResource().getMessage());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with microRemote service with version with resource and instance and message")
    public void test_parse_protocol_uri_with_remote_service_with_version_with_resource_and_instance_and_getMessage() {
        String uri = "//VCU.MY_CAR_VIN/body.access/1/door.front_left#Door";
        UUri uuri = LongUriSerializer.instance().deserialize(uri);
        assertTrue(UriValidator.isRemote(uuri.getAuthority()));
        assertFalse(uuri.getAuthority().getName().isBlank());
        assertEquals("VCU.MY_CAR_VIN", uuri.getAuthority().getName());
        assertFalse(uuri.getAuthority().getName().isBlank());
        assertEquals("body.access", uuri.getEntity().getName());
        assertNotEquals(0, uuri.getEntity().getVersionMajor());
        assertEquals(1, uuri.getEntity().getVersionMajor());
        assertEquals("door", uuri.getResource().getName());
        assertFalse(uuri.getResource().getInstance().isEmpty());
        assertEquals("front_left", uuri.getResource().getInstance());
        assertFalse(uuri.getResource().getMessage().isEmpty());
        assertEquals("Door", uuri.getResource().getMessage());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with microRemote cloud service with version with resource and instance and message")
    public void test_parse_protocol_uri_with_remote_cloud_service_with_version_with_resource_and_instance_and_getMessage() {
        String uri = "//cloud.uprotocol.example.com/body.access/1/door.front_left#Door";
        UUri uuri = LongUriSerializer.instance().deserialize(uri);
        assertTrue(UriValidator.isRemote(uuri.getAuthority()));
        assertFalse(uuri.getAuthority().getName().isBlank());
        assertEquals("cloud.uprotocol.example.com", uuri.getAuthority().getName());
        assertFalse(uuri.getAuthority().getName().isBlank());
        assertEquals("body.access", uuri.getEntity().getName());
        assertNotEquals(0, uuri.getEntity().getVersionMajor());
        assertEquals(1, uuri.getEntity().getVersionMajor());
        assertEquals("door", uuri.getResource().getName());
        assertFalse(uuri.getResource().getInstance().isEmpty());
        assertEquals("front_left", uuri.getResource().getInstance());
        assertFalse(uuri.getResource().getMessage().isEmpty());
        assertEquals("Door", uuri.getResource().getMessage());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with microRemote service with version with resource with message when there is only device, no domain")
    public void test_parse_protocol_uri_with_remote_service_with_version_with_resource_with_message_device_no_domain() {
        String uri = "//VCU/body.access/1/door.front_left";
        UUri uuri = LongUriSerializer.instance().deserialize(uri);
        assertTrue(UriValidator.isRemote(uuri.getAuthority()));
        assertFalse(uuri.getAuthority().getName().isBlank());
        assertEquals("VCU", uuri.getAuthority().getName());
        assertFalse(uuri.getAuthority().getName().isEmpty());
        assertEquals("body.access", uuri.getEntity().getName());
        assertNotEquals(0, uuri.getEntity().getVersionMajor());
        assertEquals(1, uuri.getEntity().getVersionMajor());
        assertEquals("door", uuri.getResource().getName());
        assertFalse(uuri.getResource().getInstance().isEmpty());
        assertEquals("front_left", uuri.getResource().getInstance());
        assertTrue(uuri.getResource().getMessage().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol RPC uri with microRemote service no version")
    public void test_parse_protocol_rpc_uri_with_remote_service_no_version() {
        String uri = "//bo.cloud/petapp//rpc.response";
        UUri uuri = LongUriSerializer.instance().deserialize(uri);
        assertTrue(UriValidator.isRemote(uuri.getAuthority()));
        assertFalse(uuri.getAuthority().getName().isBlank());
        assertEquals("bo.cloud", uuri.getAuthority().getName());
        assertFalse(uuri.getAuthority().getName().isBlank());
        assertEquals("petapp", uuri.getEntity().getName());
        assertEquals(0, uuri.getEntity().getVersionMajor());
        assertEquals("rpc", uuri.getResource().getName());
        assertFalse(uuri.getResource().getInstance().isEmpty());
        assertEquals("response", uuri.getResource().getInstance());
        assertTrue(uuri.getResource().getMessage().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol RPC uri with microRemote service with version")
    public void test_parse_protocol_rpc_uri_with_remote_service_with_version() {
        String uri = "//bo.cloud/petapp/1/rpc.response";
        UUri uuri = LongUriSerializer.instance().deserialize(uri);
        assertTrue(UriValidator.isRemote(uuri.getAuthority()));
        assertFalse(uuri.getAuthority().getName().isBlank());
        assertEquals("bo.cloud", uuri.getAuthority().getName());
        assertFalse(uuri.getAuthority().getName().isBlank());
        assertEquals("petapp", uuri.getEntity().getName());
        assertNotEquals(0, uuri.getEntity().getVersionMajor());
        assertEquals(1, uuri.getEntity().getVersionMajor());
        assertEquals("rpc", uuri.getResource().getName());
        assertFalse(uuri.getResource().getInstance().isEmpty());
        assertEquals("response", uuri.getResource().getInstance());
        assertTrue(uuri.getResource().getMessage().isEmpty());
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from null")
    public void test_build_protocol_uri_from__uri_when__uri_isnull() {
        String uProtocolUri = LongUriSerializer.instance().serialize(null);
        assertEquals("", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an empty  URI Object")
    public void test_build_protocol_uri_from__uri_when__uri_isEmpty() {
        UUri uuri = UUri.newBuilder().build();
        String uProtocolUri = LongUriSerializer.instance().serialize(uuri);
        assertEquals("", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI object with an empty USE")
    public void test_build_protocol_uri_from__uri_when__uri_has_empty_use() {
        UEntity use = UEntity.newBuilder().build();
        UUri uuri = UUri.newBuilder().setAuthority(UAuthority.newBuilder().build()).setEntity(use).setResource(UResource.newBuilder().setName("door").build()).build();
        String uProtocolUri = LongUriSerializer.instance().serialize(uuri);
        assertEquals("/////door", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a local authority with service no version")
    public void test_build_protocol_uri_from__uri_when__uri_has_local_authority_service_no_version() {
        UUri uuri = UUri.newBuilder().setEntity(UEntity.newBuilder().setName("body.access").build()).build();
        String uProtocolUri = LongUriSerializer.instance().serialize(uuri);
        assertEquals("/body.access", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a local authority with service and version")
    public void test_build_protocol_uri_from__uri_when__uri_has_local_authority_service_and_version() {
        UEntity use = UEntity.newBuilder().setName("body.access").setVersionMajor(1).build();
        UUri uuri = UUri.newBuilder().setEntity(use).setResource(UResource.newBuilder().build()).build();
        String uProtocolUri = LongUriSerializer.instance().serialize(uuri);
        assertEquals("/body.access/1", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a local authority with service no version with resource")
    public void test_build_protocol_uri_from__uri_when__uri_has_local_authority_service_no_version_with_resource() {
        UEntity use = UEntity.newBuilder().setName("body.access").build();
        UUri uuri = UUri.newBuilder().setEntity(use).setResource(UResource.newBuilder().setName("door").build()).build();
        String uProtocolUri = LongUriSerializer.instance().serialize(uuri);
        assertEquals("/body.access//door", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a local authority with service and version with resource")
    public void test_build_protocol_uri_from__uri_when__uri_has_local_authority_service_and_version_with_resource() {
        UEntity use = UEntity.newBuilder().setName("body.access").setVersionMajor(1).build();
        UUri uuri = UUri.newBuilder().setEntity(use).setResource(UResource.newBuilder().setName("door").build()).build();
        String uProtocolUri = LongUriSerializer.instance().serialize(uuri);
        assertEquals("/body.access/1/door", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a local authority with service no version with resource with instance no message")
    public void test_build_protocol_uri_from__uri_when__uri_has_local_authority_service_no_version_with_resource_with_instance_no_getMessage() {
        UEntity use = UEntity.newBuilder().setName("body.access").build();
        UUri uuri = UUri.newBuilder().setEntity(use).setResource(UResource.newBuilder().setName("door").setInstance("front_left").build()).build();
        String uProtocolUri = LongUriSerializer.instance().serialize(uuri);
        assertEquals("/body.access//door.front_left", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a local authority with service and version with resource with instance no message")
    public void test_build_protocol_uri_from__uri_when__uri_has_local_authority_service_and_version_with_resource_with_instance_no_getMessage() {
        UEntity use = UEntity.newBuilder().setName("body.access").setVersionMajor(1).build();
        UUri uuri = UUri.newBuilder().setEntity(use).setResource(UResource.newBuilder().setName("door").setInstance("front_left").build()).build();
        String uProtocolUri = LongUriSerializer.instance().serialize(uuri);
        assertEquals("/body.access/1/door.front_left", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a local authority with service no version with resource with instance and message")
    public void test_build_protocol_uri_from__uri_when__uri_has_local_authority_service_no_version_with_resource_with_instance_with_getMessage() {
        UEntity use = UEntity.newBuilder().setName("body.access").build();
        UUri uuri = UUri.newBuilder().setEntity(use).setResource(UResource.newBuilder().setName("door").setInstance("front_left").setMessage("Door").build()).build();
        String uProtocolUri = LongUriSerializer.instance().serialize(uuri);
        assertEquals("/body.access//door.front_left#Door", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a local authority with service and version with resource with instance and message")
    public void test_build_protocol_uri_from__uri_when__uri_has_local_authority_service_and_version_with_resource_with_instance_with_getMessage() {
        UEntity use = UEntity.newBuilder().setName("body.access").setVersionMajor(1).build();
        UUri uuri = UUri.newBuilder().setEntity(use).setResource(UResource.newBuilder().setName("door").setInstance("front_left").setMessage("Door").build()).build();
        String uProtocolUri = LongUriSerializer.instance().serialize(uuri);
        assertEquals("/body.access/1/door.front_left#Door", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a microRemote authority with service no version")
    public void test_build_protocol_uri_from__uri_when__uri_has_remote_authority_service_no_version() {
        UEntity use = UEntity.newBuilder().setName("body.access").build();
        UUri uuri = UUri.newBuilder().setAuthority(UAuthority.newBuilder().setName("vcu.my_car_vin").build()).setEntity(use).build();
        String uProtocolUri = LongUriSerializer.instance().serialize(uuri);
        assertEquals("//vcu.my_car_vin/body.access", uProtocolUri);
    }


    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a microRemote authority with service and version")
    public void test_build_protocol_uri_from__uri_when__uri_has_remote_authority_service_and_version() {
        UEntity use = UEntity.newBuilder().setName("body.access").setVersionMajor(1).build();
        UUri uuri = UUri.newBuilder().setAuthority(UAuthority.newBuilder().setName("vcu.my_car_vin").build()).setEntity(use).build();
        String uProtocolUri = LongUriSerializer.instance().serialize(uuri);
        assertEquals("//vcu.my_car_vin/body.access/1", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a microRemote cloud authority with service and version")
    public void test_build_protocol_uri_from__uri_when__uri_has_remote_cloud_authority_service_and_version() {
        UEntity use = UEntity.newBuilder().setName("body.access").setVersionMajor(1).build();
        UUri uuri = UUri.newBuilder().setAuthority(UAuthority.newBuilder().setName("cloud.uprotocol.example.com").build()).setEntity(use).build();
        String uProtocolUri = LongUriSerializer.instance().serialize(uuri);
        assertEquals("//cloud.uprotocol.example.com/body.access/1", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a microRemote authority with service and version with resource")
    public void test_build_protocol_uri_from__uri_when__uri_has_remote_authority_service_and_version_with_resource() {
        UEntity use = UEntity.newBuilder().setName("body.access").setVersionMajor(1).build();
        UUri uuri = UUri.newBuilder().setAuthority(UAuthority.newBuilder().setName("vcu.my_car_vin").build()).setEntity(use).setResource(UResource.newBuilder().setName("door").build()).build();
        String uProtocolUri = LongUriSerializer.instance().serialize(uuri);
        assertEquals("//vcu.my_car_vin/body.access/1/door", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a microRemote authority with service no version with resource")
    public void test_build_protocol_uri_from__uri_when__uri_has_remote_authority_service_no_version_with_resource() {
        UEntity use = UEntity.newBuilder().setName("body.access").build();
        UUri uuri = UUri.newBuilder().setAuthority(UAuthority.newBuilder().setName("vcu.my_car_vin").build()).setEntity(use).setResource(UResource.newBuilder().setName("door").build()).build();
        String uProtocolUri = LongUriSerializer.instance().serialize(uuri);
        assertEquals("//vcu.my_car_vin/body.access//door", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a microRemote authority with service and version with resource with instance no message")
    public void test_build_protocol_uri_from__uri_when__uri_has_remote_authority_service_and_version_with_resource_with_instance_no_getMessage() {
        UEntity use = UEntity.newBuilder().setName("body.access").setVersionMajor(1).build();
        UUri uuri = UUri.newBuilder().setAuthority(UAuthority.newBuilder().setName("vcu.my_car_vin").build()).setEntity(use).setResource(UResource.newBuilder().setName("door").setInstance("front_left").build()).build();
        String uProtocolUri = LongUriSerializer.instance().serialize(uuri);
        assertEquals("//vcu.my_car_vin/body.access/1/door.front_left", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a microRemote cloud authority with service and version with resource with instance no message")
    public void test_build_protocol_uri_from__uri_when__uri_has_remote_cloud_authority_service_and_version_with_resource_with_instance_no_getMessage() {
        UEntity use = UEntity.newBuilder().setName("body.access").setVersionMajor(1).build();
        UUri uuri = UUri.newBuilder().setAuthority(UAuthority.newBuilder().setName("cloud.uprotocol.example.com").build()).setEntity(use).setResource(

                UResource.newBuilder().setName("door").setInstance("front_left").build()).build();
        String uProtocolUri = LongUriSerializer.instance().serialize(uuri);
        assertEquals("//cloud.uprotocol.example.com/body.access/1/door.front_left", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a microRemote authority with service no version with resource with instance no message")
    public void test_build_protocol_uri_from__uri_when__uri_has_remote_authority_service_no_version_with_resource_with_instance_no_getMessage() {
        UEntity use = UEntity.newBuilder().setName("body.access").build();
        UUri uuri = UUri.newBuilder().setAuthority(UAuthority.newBuilder().setName("vcu.my_car_vin").build()).setEntity(use).setResource(UResource.newBuilder().setName("door").setInstance("front_left").build()).build();
        String uProtocolUri = LongUriSerializer.instance().serialize(uuri);
        assertEquals("//vcu.my_car_vin/body.access//door.front_left", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a microRemote authority with service and version with resource with instance and message")
    public void test_build_protocol_uri_from__uri_when__uri_has_remote_authority_service_and_version_with_resource_with_instance_and_getMessage() {
        UEntity use = UEntity.newBuilder().setName("body.access").setVersionMajor(1).build();
        UUri uuri = UUri.newBuilder().setAuthority(UAuthority.newBuilder().setName("vcu.my_car_vin").build()).setEntity(use).setResource(UResource.newBuilder().setName("door").setInstance("front_left").setMessage("Door").build()).build();
        String uProtocolUri = LongUriSerializer.instance().serialize(uuri);
        assertEquals("//vcu.my_car_vin/body.access/1/door.front_left#Door", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a microRemote authority with service no version with resource with instance and message")
    public void test_build_protocol_uri_from__uri_when__uri_has_remote_authority_service_no_version_with_resource_with_instance_and_getMessage() {
        UEntity use = UEntity.newBuilder().setName("body.access").build();
        UUri uuri = UUri.newBuilder().setAuthority(UAuthority.newBuilder().setName("vcu.my_car_vin").build()).setEntity(use).setResource(UResource.newBuilder().setName("door").setInstance("front_left").setMessage("Door").build()).build();
        String uProtocolUri = LongUriSerializer.instance().serialize(uuri);
        assertEquals("//vcu.my_car_vin/body.access//door.front_left#Door", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI for the source part of an RPC request, where the source is local")
    public void test_build_protocol_uri_for_source_part_of_rpc_request_where_source_is_local() {
        UEntity use = UEntity.newBuilder().setName("petapp").setVersionMajor(1).build();
        UResource resource = UResource.newBuilder().setName("rpc").setInstance("response").build();
        String uProtocolUri = LongUriSerializer.instance().serialize(UUri.newBuilder().setEntity(use).setResource(resource).build());
        assertEquals("/petapp/1/rpc.response", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI for the source part of an RPC request, where the source is microRemote")
    public void test_build_protocol_uri_for_source_part_of_rpc_request_where_source_is_remote() {
        UAuthority uAuthority = UAuthority.newBuilder().setName("cloud.uprotocol.example.com").build();
        UEntity use = UEntity.newBuilder().setName("petapp").build();
        UResource resource = UResource.newBuilder().setName("rpc").setInstance("response").build();

        String uProtocolUri = LongUriSerializer.instance().serialize(UUri.newBuilder().setAuthority(uAuthority).setEntity(use).setResource(resource).build());
        assertEquals("//cloud.uprotocol.example.com/petapp//rpc.response", uProtocolUri);
    }


    @Test
    @DisplayName("Test Create a uProtocol URI from the parts of  URI Object with a microRemote authority with service and version with resource")
    public void test_build_protocol_uri_from__uri_parts_when__uri_has_remote_authority_service_and_version_with_resource() {
        UAuthority uAuthority = UAuthority.newBuilder().setName("vcu.my_car_vin").build();
        UEntity use = UEntity.newBuilder().setName("body.access").setVersionMajor(1).build();
        UResource uResource = UResource.newBuilder().setName("door").build();
        String uProtocolUri = LongUriSerializer.instance().serialize(UUri.newBuilder().setAuthority(uAuthority).setEntity(use).setResource(uResource).build());
        assertEquals("//vcu.my_car_vin/body.access/1/door", uProtocolUri);
    }



    @Test
    @DisplayName("Test Create a custom URI using no scheme")
    public void test_custom_scheme_no_scheme() {
        UAuthority uAuthority = UAuthority.newBuilder().setName("vcu.my_car_vin").build();
        UEntity use = UEntity.newBuilder().setName("body.access").setVersionMajor(1).build();
        UResource uResource = UResource.newBuilder().setName("door").build();
        String ucustomUri = LongUriSerializer.instance().serialize(UUri.newBuilder().setAuthority(uAuthority)
                        .setEntity(use).setResource(uResource).build());
        assertEquals("//vcu.my_car_vin/body.access/1/door", ucustomUri);
    }

    @Test
    @DisplayName("Test parse local uProtocol uri with custom scheme")
    public void test_parse_local_protocol_uri_with_custom_scheme() {
        String uri = "custom:/body.access//door.front_left#Door";
        UUri uuri = LongUriSerializer.instance().deserialize(uri);
        assertFalse(UriValidator.isRemote(uuri.getAuthority()));
        assertEquals("body.access", uuri.getEntity().getName());
        assertEquals(0, uuri.getEntity().getVersionMajor());
        assertEquals("door", uuri.getResource().getName());
        assertFalse(uuri.getResource().getInstance().isEmpty());
        assertEquals("front_left", uuri.getResource().getInstance());
        assertFalse(uuri.getResource().getMessage().isEmpty());
        assertEquals("Door", uuri.getResource().getMessage());
    }

    @Test
    @DisplayName("Test parse microRemote uProtocol uri with custom scheme")
    public void test_parse_remote_protocol_uri_with_custom_scheme() {
        String uri = "custom://vcu.vin/body.access//door.front_left#Door";
        String uri2 = "//vcu.vin/body.access//door.front_left#Door";
        UUri uuri = LongUriSerializer.instance().deserialize(uri);
        assertTrue(UriValidator.isRemote(uuri.getAuthority()));
        assertEquals("vcu.vin", uuri.getAuthority().getName());
        assertEquals("body.access", uuri.getEntity().getName());
        assertEquals(0, uuri.getEntity().getVersionMajor());
        assertEquals("door", uuri.getResource().getName());
        assertEquals("front_left", uuri.getResource().getInstance());
        assertEquals("Door", uuri.getResource().getMessage());
        assertEquals(uri2, LongUriSerializer.instance().serialize(uuri));
    }



}
