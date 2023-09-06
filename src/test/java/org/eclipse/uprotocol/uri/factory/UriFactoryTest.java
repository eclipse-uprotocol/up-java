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

package org.eclipse.uprotocol.uri.factory;

import org.eclipse.uprotocol.uri.datamodel.UAuthority;
import org.eclipse.uprotocol.uri.datamodel.UEntity;
import org.eclipse.uprotocol.uri.datamodel.UResource;
import org.eclipse.uprotocol.uri.datamodel.UUri;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

class UriFactoryTest {

    @Test
    @DisplayName("Test parse uProtocol uri that is null")
    public void test_parse_protocol_uri_when_is_null() {
        UUri Uri = UriFactory.parseFromUri(null);
        assertTrue(Uri.isEmpty());
    }

    
    @Test
    @DisplayName("Test parse uProtocol uri that is empty string")
    public void test_parse_protocol_uri_when_is_empty_string() {
        String uri = "";
        UUri Uri = UriFactory.parseFromUri(uri);
        assertTrue(Uri.isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with schema and slash")
    public void test_parse_protocol_uri_with_schema_and_slash() {
        String uri = "/";
        UUri Uri = UriFactory.parseFromUri(uri);
        assertTrue(Uri.uAuthority().isLocal());
        assertFalse(Uri.uAuthority().isMarkedRemote());
        assertTrue(Uri.isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with schema and double slash")
    public void test_parse_protocol_uri_with_schema_and_double_slash() {
        String uri = "//";
        UUri Uri = UriFactory.parseFromUri(uri);
        assertTrue(Uri.uAuthority().isLocal());
        assertTrue(Uri.uAuthority().isMarkedRemote());
        assertTrue(Uri.isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with schema and 3 slash and something")
    public void test_parse_protocol_uri_with_schema_and_3_slash_and_something() {
        String uri = "///body.access";
        UUri Uri = UriFactory.parseFromUri(uri);
        assertTrue(Uri.uAuthority().isLocal());
        assertTrue(Uri.uAuthority().isMarkedRemote());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isEmpty());
        assertTrue(Uri.uResource().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with schema and 4 slash and something")
    public void test_parse_protocol_uri_with_schema_and_4_slash_and_something() {
        String uri = "////body.access";
        UUri Uri = UriFactory.parseFromUri(uri);
        assertTrue(Uri.uAuthority().isLocal());
        assertTrue(Uri.uAuthority().isMarkedRemote());
        assertTrue(Uri.uEntity().name().isBlank());
        assertTrue(Uri.uEntity().version().isPresent());
        assertEquals("body.access", Uri.uEntity().version().get());
        assertTrue(Uri.uResource().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with schema and 5 slash and something")
    public void test_parse_protocol_uri_with_schema_and_5_slash_and_something() {
        String uri = "/////body.access";
        UUri Uri = UriFactory.parseFromUri(uri);
        assertTrue(Uri.uAuthority().isLocal());
        assertTrue(Uri.uAuthority().isMarkedRemote());
        assertTrue(Uri.uEntity().isEmpty());
        assertEquals("body", Uri.uResource().name());
        assertTrue(Uri.uResource().instance().isPresent());
        assertEquals("access", Uri.uResource().instance().get());
        assertTrue(Uri.uResource().message().isEmpty());
    }
 
    @Test
    @DisplayName("Test parse uProtocol uri with schema and 6 slash and something")
    public void test_parse_protocol_uri_with_schema_and_6_slash_and_something() {
        String uri = "//////body.access";
        UUri Uri = UriFactory.parseFromUri(uri);
        assertTrue(Uri.uAuthority().isLocal());
        assertTrue(Uri.uAuthority().isMarkedRemote());
        assertTrue(Uri.isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with local service no version")
    public void test_parse_protocol_uri_with_local_service_no_version() {
        String uri = "/body.access";
        UUri Uri = UriFactory.parseFromUri(uri);
        assertTrue(Uri.uAuthority().isLocal());
        assertFalse(Uri.uAuthority().isMarkedRemote());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isEmpty());
        assertTrue(Uri.uResource().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with local service with version")
    public void test_parse_protocol_uri_with_local_service_with_version() {
        String uri = "/body.access/1";
        UUri Uri = UriFactory.parseFromUri(uri);
        assertTrue(Uri.uAuthority().isLocal());
        assertFalse(Uri.uAuthority().isMarkedRemote());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isPresent());
        assertEquals("1", Uri.uEntity().version().get());
        assertTrue(Uri.uResource().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with local service no version with resource name only")
    public void test_parse_protocol_uri_with_local_service_no_version_with_resource_name_only() {
        String uri = "/body.access//door";
        UUri Uri = UriFactory.parseFromUri(uri);
        assertTrue(Uri.uAuthority().isLocal());
        assertFalse(Uri.uAuthority().isMarkedRemote());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isEmpty());
        assertEquals("door", Uri.uResource().name());
        assertTrue(Uri.uResource().instance().isEmpty());
        assertTrue(Uri.uResource().message().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with local service with version with resource name only")
    public void test_parse_protocol_uri_with_local_service_with_version_with_resource_name_only() {
        String uri = "/body.access/1/door";
        UUri Uri = UriFactory.parseFromUri(uri);
        assertTrue(Uri.uAuthority().isLocal());
        assertFalse(Uri.uAuthority().isMarkedRemote());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isPresent());
        assertEquals("1", Uri.uEntity().version().get());
        assertEquals("door", Uri.uResource().name());
        assertTrue(Uri.uResource().instance().isEmpty());
        assertTrue(Uri.uResource().message().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with local service no version with resource and instance only")
    public void test_parse_protocol_uri_with_local_service_no_version_with_resource_with_instance() {
        String uri = "/body.access//door.front_left";
        UUri Uri = UriFactory.parseFromUri(uri);
        assertTrue(Uri.uAuthority().isLocal());
        assertFalse(Uri.uAuthority().isMarkedRemote());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isEmpty());
        assertEquals("door", Uri.uResource().name());
        assertTrue(Uri.uResource().instance().isPresent());
        assertEquals("front_left", Uri.uResource().instance().get());
        assertTrue(Uri.uResource().message().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with local service with version with resource and instance only")
    public void test_parse_protocol_uri_with_local_service_with_version_with_resource_with_message() {
        String uri = "/body.access/1/door.front_left";
        UUri Uri = UriFactory.parseFromUri(uri);
        assertTrue(Uri.uAuthority().isLocal());
        assertFalse(Uri.uAuthority().isMarkedRemote());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isPresent());
        assertEquals("1", Uri.uEntity().version().get());
        assertEquals("door", Uri.uResource().name());
        assertTrue(Uri.uResource().instance().isPresent());
        assertEquals("front_left", Uri.uResource().instance().get());
        assertTrue(Uri.uResource().message().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with local service no version with resource with instance and message")
    public void test_parse_protocol_uri_with_local_service_no_version_with_resource_with_instance_and_message() {
        String uri = "/body.access//door.front_left#Door";
        UUri Uri = UriFactory.parseFromUri(uri);
        assertTrue(Uri.uAuthority().isLocal());
        assertFalse(Uri.uAuthority().isMarkedRemote());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isEmpty());
        assertEquals("door", Uri.uResource().name());
        assertTrue(Uri.uResource().instance().isPresent());
        assertEquals("front_left", Uri.uResource().instance().get());
        assertTrue(Uri.uResource().message().isPresent());
        assertEquals("Door", Uri.uResource().message().get());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with local service with version with resource with instance and message")
    public void test_parse_protocol_uri_with_local_service_with_version_with_resource_with_instance_and_message() {
        String uri = "/body.access/1/door.front_left#Door";
        UUri Uri = UriFactory.parseFromUri(uri);
        assertTrue(Uri.uAuthority().isLocal());
        assertFalse(Uri.uAuthority().isMarkedRemote());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isPresent());
        assertEquals("1", Uri.uEntity().version().get());
        assertEquals("door", Uri.uResource().name());
        assertTrue(Uri.uResource().instance().isPresent());
        assertEquals("front_left", Uri.uResource().instance().get());
        assertTrue(Uri.uResource().message().isPresent());
        assertEquals("Door", Uri.uResource().message().get());
    }

    @Test
    @DisplayName("Test parse uProtocol RPC uri with local service no version")
    public void test_parse_protocol_rpc_uri_with_local_service_no_version() {
        String uri = "/petapp//rpc.response";
        UUri Uri = UriFactory.parseFromUri(uri);
        assertTrue(Uri.uAuthority().isLocal());
        assertFalse(Uri.uAuthority().isMarkedRemote());
        assertEquals("petapp", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isEmpty());
        assertEquals("rpc", Uri.uResource().name());
        assertTrue(Uri.uResource().instance().isPresent());
        assertEquals("response", Uri.uResource().instance().get());
        assertTrue(Uri.uResource().message().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol RPC uri with local service with version")
    public void test_parse_protocol_rpc_uri_with_local_service_with_version() {
        String uri = "/petapp/1/rpc.response";
        UUri Uri = UriFactory.parseFromUri(uri);
        assertTrue(Uri.uAuthority().isLocal());
        assertFalse(Uri.uAuthority().isMarkedRemote());
        assertEquals("petapp", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isPresent());
        assertEquals("1", Uri.uEntity().version().get());
        assertEquals("rpc", Uri.uResource().name());
        assertTrue(Uri.uResource().instance().isPresent());
        assertEquals("response", Uri.uResource().instance().get());
        assertTrue(Uri.uResource().message().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with remote service only device no domain")
    public void test_parse_protocol_uri_with_remote_service_only_device_no_domain() {
        String uri = "//VCU";
        UUri Uri = UriFactory.parseFromUri(uri);
        assertTrue(Uri.uAuthority().isRemote());
        assertTrue(Uri.uAuthority().device().isPresent());
        assertEquals("vcu", Uri.uAuthority().device().get());
        assertTrue(Uri.uAuthority().domain().isEmpty());
        assertTrue(Uri.uEntity().isEmpty());
        assertTrue(Uri.uResource().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with remote service only device and domain")
    public void test_parse_protocol_uri_with_remote_service_only_device_and_domain() {
        String uri = "//VCU.MY_CAR_VIN";
        UUri Uri = UriFactory.parseFromUri(uri);
        assertTrue(Uri.uAuthority().isRemote());
        assertTrue(Uri.uAuthority().device().isPresent());
        assertEquals("vcu", Uri.uAuthority().device().get());
        assertTrue(Uri.uAuthority().domain().isPresent());
        assertEquals("my_car_vin", Uri.uAuthority().domain().get());
        assertTrue(Uri.uEntity().isEmpty());
        assertTrue(Uri.uResource().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with remote service only device and cloud domain")
    public void test_parse_protocol_uri_with_remote_service_only_device_and_cloud_domain() {
        String uri = "//cloud.uprotocol.example.com";
        UUri Uri = UriFactory.parseFromUri(uri);
        assertTrue(Uri.uAuthority().isRemote());
        assertTrue(Uri.uAuthority().device().isPresent());
        assertEquals("cloud", Uri.uAuthority().device().get());
        assertTrue(Uri.uAuthority().domain().isPresent());
        assertEquals("uprotocol.example.com", Uri.uAuthority().domain().get());
        assertTrue(Uri.uEntity().isEmpty());
        assertTrue(Uri.uResource().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with remote service no version")
    public void test_parse_protocol_uri_with_remote_service_no_version() {
        String uri = "//VCU.MY_CAR_VIN/body.access";
        UUri Uri = UriFactory.parseFromUri(uri);
        assertTrue(Uri.uAuthority().isRemote());
        assertTrue(Uri.uAuthority().device().isPresent());
        assertEquals("vcu", Uri.uAuthority().device().get());
        assertTrue(Uri.uAuthority().domain().isPresent());
        assertEquals("my_car_vin", Uri.uAuthority().domain().get());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isEmpty());
        assertTrue(Uri.uResource().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with remote cloud service no version")
    public void test_parse_protocol_uri_with_remote_cloud_service_no_version() {
        String uri = "//cloud.uprotocol.example.com/body.access";
        UUri Uri = UriFactory.parseFromUri(uri);
        assertTrue(Uri.uAuthority().isRemote());
        assertTrue(Uri.uAuthority().device().isPresent());
        assertEquals("cloud", Uri.uAuthority().device().get());
        assertTrue(Uri.uAuthority().domain().isPresent());
        assertEquals("uprotocol.example.com", Uri.uAuthority().domain().get());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isEmpty());
        assertTrue(Uri.uResource().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with remote service with version")
    public void test_parse_protocol_uri_with_remote_service_with_version() {
        String uri = "//VCU.MY_CAR_VIN/body.access/1";
        UUri Uri = UriFactory.parseFromUri(uri);
        assertTrue(Uri.uAuthority().isRemote());
        assertTrue(Uri.uAuthority().device().isPresent());
        assertEquals("vcu", Uri.uAuthority().device().get());
        assertTrue(Uri.uAuthority().domain().isPresent());
        assertEquals("my_car_vin", Uri.uAuthority().domain().get());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isPresent());
        assertEquals("1", Uri.uEntity().version().get());
        assertTrue(Uri.uResource().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with remote cloud service with version")
    public void test_parse_protocol_uri_with_remote_cloud_service_with_version() {
        String uri = "//cloud.uprotocol.example.com/body.access/1";
        UUri Uri = UriFactory.parseFromUri(uri);
        assertTrue(Uri.uAuthority().isRemote());
        assertTrue(Uri.uAuthority().device().isPresent());
        assertEquals("cloud", Uri.uAuthority().device().get());
        assertTrue(Uri.uAuthority().domain().isPresent());
        assertEquals("uprotocol.example.com", Uri.uAuthority().domain().get());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isPresent());
        assertEquals("1", Uri.uEntity().version().get());
        assertTrue(Uri.uResource().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with remote service no version with resource name only")
    public void test_parse_protocol_uri_with_remote_service_no_version_with_resource_name_only() {
        String uri = "//VCU.MY_CAR_VIN/body.access//door";
        UUri Uri = UriFactory.parseFromUri(uri);
        assertTrue(Uri.uAuthority().isRemote());
        assertTrue(Uri.uAuthority().device().isPresent());
        assertEquals("vcu", Uri.uAuthority().device().get());
        assertTrue(Uri.uAuthority().domain().isPresent());
        assertEquals("my_car_vin", Uri.uAuthority().domain().get());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isEmpty());
        assertEquals("door", Uri.uResource().name());
        assertTrue(Uri.uResource().instance().isEmpty());
        assertTrue(Uri.uResource().message().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with remote cloud service no version with resource name only")
    public void test_parse_protocol_uri_with_remote_cloud_service_no_version_with_resource_name_only() {
        String uri = "//cloud.uprotocol.example.com/body.access//door";
        UUri Uri = UriFactory.parseFromUri(uri);
        assertTrue(Uri.uAuthority().isRemote());
        assertTrue(Uri.uAuthority().device().isPresent());
        assertEquals("cloud", Uri.uAuthority().device().get());
        assertTrue(Uri.uAuthority().domain().isPresent());
        assertEquals("uprotocol.example.com", Uri.uAuthority().domain().get());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isEmpty());
        assertEquals("door", Uri.uResource().name());
        assertTrue(Uri.uResource().instance().isEmpty());
        assertTrue(Uri.uResource().message().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with remote service with version with resource name only")
    public void test_parse_protocol_uri_with_remote_service_with_version_with_resource_name_only() {
        String uri = "//VCU.MY_CAR_VIN/body.access/1/door";
        UUri Uri = UriFactory.parseFromUri(uri);
        assertTrue(Uri.uAuthority().isRemote());
        assertTrue(Uri.uAuthority().device().isPresent());
        assertEquals("vcu", Uri.uAuthority().device().get());
        assertTrue(Uri.uAuthority().domain().isPresent());
        assertEquals("my_car_vin", Uri.uAuthority().domain().get());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isPresent());
        assertEquals("1", Uri.uEntity().version().get());
        assertEquals("door", Uri.uResource().name());
        assertTrue(Uri.uResource().instance().isEmpty());
        assertTrue(Uri.uResource().message().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with remote cloud service with version with resource name only")
    public void test_parse_protocol_uri_with_remote_service_cloud_with_version_with_resource_name_only() {
        String uri = "//cloud.uprotocol.example.com/body.access/1/door";
        UUri Uri = UriFactory.parseFromUri(uri);
        assertTrue(Uri.uAuthority().isRemote());
        assertTrue(Uri.uAuthority().device().isPresent());
        assertEquals("cloud", Uri.uAuthority().device().get());
        assertTrue(Uri.uAuthority().domain().isPresent());
        assertEquals("uprotocol.example.com", Uri.uAuthority().domain().get());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isPresent());
        assertEquals("1", Uri.uEntity().version().get());
        assertEquals("door", Uri.uResource().name());
        assertTrue(Uri.uResource().instance().isEmpty());
        assertTrue(Uri.uResource().message().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with remote service no version with resource and instance no message")
    public void test_parse_protocol_uri_with_remote_service_no_version_with_resource_and_instance_no_message() {
        String uri = "//VCU.MY_CAR_VIN/body.access//door.front_left";
        UUri Uri = UriFactory.parseFromUri(uri);
        assertTrue(Uri.uAuthority().isRemote());
        assertTrue(Uri.uAuthority().device().isPresent());
        assertEquals("vcu", Uri.uAuthority().device().get());
        assertTrue(Uri.uAuthority().domain().isPresent());
        assertEquals("my_car_vin", Uri.uAuthority().domain().get());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isEmpty());
        assertEquals("door", Uri.uResource().name());
        assertTrue(Uri.uResource().instance().isPresent());
        assertEquals("front_left", Uri.uResource().instance().get());
        assertTrue(Uri.uResource().message().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with remote service with version with resource and instance no message")
    public void test_parse_protocol_uri_with_remote_service_with_version_with_resource_and_instance_no_message() {
        String uri = "//VCU.MY_CAR_VIN/body.access/1/door.front_left";
        UUri Uri = UriFactory.parseFromUri(uri);
        assertTrue(Uri.uAuthority().isRemote());
        assertTrue(Uri.uAuthority().device().isPresent());
        assertEquals("vcu", Uri.uAuthority().device().get());
        assertTrue(Uri.uAuthority().domain().isPresent());
        assertEquals("my_car_vin", Uri.uAuthority().domain().get());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isPresent());
        assertEquals("1", Uri.uEntity().version().get());
        assertEquals("door", Uri.uResource().name());
        assertTrue(Uri.uResource().instance().isPresent());
        assertEquals("front_left", Uri.uResource().instance().get());
        assertTrue(Uri.uResource().message().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with remote service no version with resource and instance and message")
    public void test_parse_protocol_uri_with_remote_service_no_version_with_resource_and_instance_and_message() {
        String uri = "//VCU.MY_CAR_VIN/body.access//door.front_left#Door";
        UUri Uri = UriFactory.parseFromUri(uri);
        assertTrue(Uri.uAuthority().isRemote());
        assertTrue(Uri.uAuthority().device().isPresent());
        assertEquals("vcu", Uri.uAuthority().device().get());
        assertTrue(Uri.uAuthority().domain().isPresent());
        assertEquals("my_car_vin", Uri.uAuthority().domain().get());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isEmpty());
        assertEquals("door", Uri.uResource().name());
        assertTrue(Uri.uResource().instance().isPresent());
        assertEquals("front_left", Uri.uResource().instance().get());
        assertTrue(Uri.uResource().message().isPresent());
        assertEquals("Door", Uri.uResource().message().get());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with remote cloud service no version with resource and instance and message")
    public void test_parse_protocol_uri_with_remote_cloud_service_no_version_with_resource_and_instance_and_message() {
        String uri = "//cloud.uprotocol.example.com/body.access//door.front_left#Door";
        UUri Uri = UriFactory.parseFromUri(uri);
        assertTrue(Uri.uAuthority().isRemote());
        assertTrue(Uri.uAuthority().device().isPresent());
        assertEquals("cloud", Uri.uAuthority().device().get());
        assertTrue(Uri.uAuthority().domain().isPresent());
        assertEquals("uprotocol.example.com", Uri.uAuthority().domain().get());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isEmpty());
        assertEquals("door", Uri.uResource().name());
        assertTrue(Uri.uResource().instance().isPresent());
        assertEquals("front_left", Uri.uResource().instance().get());
        assertTrue(Uri.uResource().message().isPresent());
        assertEquals("Door", Uri.uResource().message().get());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with remote service with version with resource and instance and message")
    public void test_parse_protocol_uri_with_remote_service_with_version_with_resource_and_instance_and_message() {
        String uri = "//VCU.MY_CAR_VIN/body.access/1/door.front_left#Door";
        UUri Uri = UriFactory.parseFromUri(uri);
        assertTrue(Uri.uAuthority().isRemote());
        assertTrue(Uri.uAuthority().device().isPresent());
        assertEquals("vcu", Uri.uAuthority().device().get());
        assertTrue(Uri.uAuthority().domain().isPresent());
        assertEquals("my_car_vin", Uri.uAuthority().domain().get());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isPresent());
        assertEquals("1", Uri.uEntity().version().get());
        assertEquals("door", Uri.uResource().name());
        assertTrue(Uri.uResource().instance().isPresent());
        assertEquals("front_left", Uri.uResource().instance().get());
        assertTrue(Uri.uResource().message().isPresent());
        assertEquals("Door", Uri.uResource().message().get());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with remote cloud service with version with resource and instance and message")
    public void test_parse_protocol_uri_with_remote_cloud_service_with_version_with_resource_and_instance_and_message() {
        String uri = "//cloud.uprotocol.example.com/body.access/1/door.front_left#Door";
        UUri Uri = UriFactory.parseFromUri(uri);
        assertTrue(Uri.uAuthority().isRemote());
        assertTrue(Uri.uAuthority().device().isPresent());
        assertEquals("cloud", Uri.uAuthority().device().get());
        assertTrue(Uri.uAuthority().domain().isPresent());
        assertEquals("uprotocol.example.com", Uri.uAuthority().domain().get());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isPresent());
        assertEquals("1", Uri.uEntity().version().get());
        assertEquals("door", Uri.uResource().name());
        assertTrue(Uri.uResource().instance().isPresent());
        assertEquals("front_left", Uri.uResource().instance().get());
        assertTrue(Uri.uResource().message().isPresent());
        assertEquals("Door", Uri.uResource().message().get());
    }

    @Test
    @DisplayName("Test parse uProtocol uri with remote service with version with resource with message when there is only device, no domain")
    public void test_parse_protocol_uri_with_remote_service_with_version_with_resource_with_message_device_no_domain() {
        String uri = "//VCU/body.access/1/door.front_left";
        UUri Uri = UriFactory.parseFromUri(uri);
        assertTrue(Uri.uAuthority().isRemote());
        assertTrue(Uri.uAuthority().device().isPresent());
        assertEquals("vcu", Uri.uAuthority().device().get());
        assertTrue(Uri.uAuthority().domain().isEmpty());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isPresent());
        assertEquals("1", Uri.uEntity().version().get());
        assertEquals("door", Uri.uResource().name());
        assertTrue(Uri.uResource().instance().isPresent());
        assertEquals("front_left", Uri.uResource().instance().get());
        assertTrue(Uri.uResource().message().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol RPC uri with remote service no version")
    public void test_parse_protocol_rpc_uri_with_remote_service_no_version() {
        String uri = "//bo.cloud/petapp//rpc.response";
        UUri Uri = UriFactory.parseFromUri(uri);
        assertTrue(Uri.uAuthority().isRemote());
        assertTrue(Uri.uAuthority().device().isPresent());
        assertEquals("bo", Uri.uAuthority().device().get());
        assertTrue(Uri.uAuthority().domain().isPresent());
        assertEquals("cloud", Uri.uAuthority().domain().get());
        assertEquals("petapp", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isEmpty());
        assertEquals("rpc", Uri.uResource().name());
        assertTrue(Uri.uResource().instance().isPresent());
        assertEquals("response", Uri.uResource().instance().get());
        assertTrue(Uri.uResource().message().isEmpty());
    }

    @Test
    @DisplayName("Test parse uProtocol RPC uri with remote service with version")
    public void test_parse_protocol_rpc_uri_with_remote_service_with_version() {
        String uri = "//bo.cloud/petapp/1/rpc.response";
        UUri Uri = UriFactory.parseFromUri(uri);
        assertTrue(Uri.uAuthority().isRemote());
        assertTrue(Uri.uAuthority().device().isPresent());
        assertEquals("bo", Uri.uAuthority().device().get());
        assertTrue(Uri.uAuthority().domain().isPresent());
        assertEquals("cloud", Uri.uAuthority().domain().get());
        assertEquals("petapp", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isPresent());
        assertEquals("1", Uri.uEntity().version().get());
        assertEquals("rpc", Uri.uResource().name());
        assertTrue(Uri.uResource().instance().isPresent());
        assertEquals("response", Uri.uResource().instance().get());
        assertTrue(Uri.uResource().message().isEmpty());
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from null")
    public void test_build_protocol_uri_from__uri_when__uri_isnull() {
        String uProtocolUri = UriFactory.buildUProtocolUri(null);
        assertEquals("", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an empty  URI Object")
    public void test_build_protocol_uri_from__uri_when__uri_isEmpty() {
        UUri Uri = UUri.empty();
        String uProtocolUri = UriFactory.buildUProtocolUri(Uri);
        assertEquals("", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI object with an empty USE")
    public void test_build_protocol_uri_from__uri_when__uri_has_empty_use() {
        UEntity use = UEntity.empty();
        UUri Uri = new UUri(UAuthority.local(), use, UResource.fromName("door"));
        String uProtocolUri = UriFactory.buildUProtocolUri(Uri);
        assertEquals("/", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a local authority with service no version")
    public void test_build_protocol_uri_from__uri_when__uri_has_local_authority_service_no_version() {
        UEntity use = UEntity.fromName("body.access");
        UUri Uri = new UUri(UAuthority.local(), use, UResource.empty());
        String uProtocolUri = UriFactory.buildUProtocolUri(Uri);
        assertEquals("/body.access", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a local authority with service and version")
    public void test_build_protocol_uri_from__uri_when__uri_has_local_authority_service_and_version() {
        UEntity use = new UEntity("body.access", "1");
        UUri Uri = new UUri(UAuthority.local(), use, UResource.empty());
        String uProtocolUri = UriFactory.buildUProtocolUri(Uri);
        assertEquals("/body.access/1", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a local authority with service no version with resource")
    public void test_build_protocol_uri_from__uri_when__uri_has_local_authority_service_no_version_with_resource() {
        UEntity use = UEntity.fromName("body.access");
        UUri Uri = new UUri(UAuthority.local(), use, UResource.fromName("door"));
        String uProtocolUri = UriFactory.buildUProtocolUri(Uri);
        assertEquals("/body.access//door", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a local authority with service and version with resource")
    public void test_build_protocol_uri_from__uri_when__uri_has_local_authority_service_and_version_with_resource() {
        UEntity use = new UEntity("body.access", "1");
        UUri Uri = new UUri(UAuthority.local(), use, UResource.fromName("door"));
        String uProtocolUri = UriFactory.buildUProtocolUri(Uri);
        assertEquals("/body.access/1/door", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a local authority with service no version with resource with instance no message")
    public void test_build_protocol_uri_from__uri_when__uri_has_local_authority_service_no_version_with_resource_with_instance_no_message() {
        UEntity use = UEntity.fromName("body.access");
        UUri Uri = new UUri(UAuthority.local(), use, UResource.fromNameWithInstance("door", "front_left"));
        String uProtocolUri = UriFactory.buildUProtocolUri(Uri);
        assertEquals("/body.access//door.front_left", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a local authority with service and version with resource with instance no message")
    public void test_build_protocol_uri_from__uri_when__uri_has_local_authority_service_and_version_with_resource_with_instance_no_message() {
        UEntity use = new UEntity("body.access", "1");
        UUri Uri = new UUri(UAuthority.local(), use, UResource.fromNameWithInstance("door", "front_left"));
        String uProtocolUri = UriFactory.buildUProtocolUri(Uri);
        assertEquals("/body.access/1/door.front_left", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a local authority with service no version with resource with instance and message")
    public void test_build_protocol_uri_from__uri_when__uri_has_local_authority_service_no_version_with_resource_with_instance_with_message() {
        UEntity use = UEntity.fromName("body.access");
        UUri Uri = new UUri(UAuthority.local(), use, new UResource("door", "front_left", "Door"));
        String uProtocolUri = UriFactory.buildUProtocolUri(Uri);
        assertEquals("/body.access//door.front_left#Door", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a local authority with service and version with resource with instance and message")
    public void test_build_protocol_uri_from__uri_when__uri_has_local_authority_service_and_version_with_resource_with_instance_with_message() {
        UEntity use = new UEntity("body.access", "1");
        UUri Uri = new UUri(UAuthority.local(), use, new UResource("door", "front_left", "Door"));
        String uProtocolUri = UriFactory.buildUProtocolUri(Uri);
        assertEquals("/body.access/1/door.front_left#Door", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a remote authority with service no version")
    public void test_build_protocol_uri_from__uri_when__uri_has_remote_authority_service_no_version() {
        UEntity use = UEntity.fromName("body.access");
        UUri Uri = new UUri(UAuthority.remote("VCU", "MY_CAR_VIN"), use, UResource.empty());
        String uProtocolUri = UriFactory.buildUProtocolUri(Uri);
        assertEquals("//vcu.my_car_vin/body.access", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a remote authority no device with domain with service no version")
    public void test_build_protocol_uri_from__uri_when__uri_has_remote_authority_no_device_with_domain_with_service_no_version() {
        UEntity use = UEntity.fromName("body.access");
        UUri Uri = new UUri(UAuthority.remote("", "MY_CAR_VIN"), use, UResource.empty());
        String uProtocolUri = UriFactory.buildUProtocolUri(Uri);
        assertEquals("//my_car_vin/body.access", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a remote authority with service and version")
    public void test_build_protocol_uri_from__uri_when__uri_has_remote_authority_service_and_version() {
        UEntity use = new UEntity("body.access", "1");
        UUri Uri = new UUri(UAuthority.remote("VCU", "MY_CAR_VIN"), use, UResource.empty());
        String uProtocolUri = UriFactory.buildUProtocolUri(Uri);
        assertEquals("//vcu.my_car_vin/body.access/1", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a remote cloud authority with service and version")
    public void test_build_protocol_uri_from__uri_when__uri_has_remote_cloud_authority_service_and_version() {
        UEntity use = new UEntity("body.access", "1");
        UUri Uri = new UUri(UAuthority.remote("cloud", "uprotocol.example.com"), use, UResource.empty());
        String uProtocolUri = UriFactory.buildUProtocolUri(Uri);
        assertEquals("//cloud.uprotocol.example.com/body.access/1", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a remote authority with service and version with resource")
    public void test_build_protocol_uri_from__uri_when__uri_has_remote_authority_service_and_version_with_resource() {
        UEntity use = new UEntity("body.access", "1");
        UUri Uri = new UUri(UAuthority.remote("VCU", "MY_CAR_VIN"), use, UResource.fromName("door"));
        String uProtocolUri = UriFactory.buildUProtocolUri(Uri);
        assertEquals("//vcu.my_car_vin/body.access/1/door", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a remote authority with service no version with resource")
    public void test_build_protocol_uri_from__uri_when__uri_has_remote_authority_service_no_version_with_resource() {
        UEntity use = UEntity.fromName("body.access");
        UUri Uri = new UUri(UAuthority.remote("VCU", "MY_CAR_VIN"), use, UResource.fromName("door"));
        String uProtocolUri = UriFactory.buildUProtocolUri(Uri);
        assertEquals("//vcu.my_car_vin/body.access//door", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a remote authority with service and version with resource with instance no message")
    public void test_build_protocol_uri_from__uri_when__uri_has_remote_authority_service_and_version_with_resource_with_instance_no_message() {
        UEntity use = new UEntity("body.access", "1");
        UUri Uri = new UUri(UAuthority.remote("VCU", "MY_CAR_VIN"), use, UResource.fromNameWithInstance("door", "front_left"));
        String uProtocolUri = UriFactory.buildUProtocolUri(Uri);
        assertEquals("//vcu.my_car_vin/body.access/1/door.front_left", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a remote cloud authority with service and version with resource with instance no message")
    public void test_build_protocol_uri_from__uri_when__uri_has_remote_cloud_authority_service_and_version_with_resource_with_instance_no_message() {
        UEntity use = new UEntity("body.access", "1");
        UUri Uri = new UUri(UAuthority.remote("cloud", "uprotocol.example.com"), use, UResource.fromNameWithInstance("door", "front_left"));
        String uProtocolUri = UriFactory.buildUProtocolUri(Uri);
        assertEquals("//cloud.uprotocol.example.com/body.access/1/door.front_left", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a remote authority with service no version with resource with instance no message")
    public void test_build_protocol_uri_from__uri_when__uri_has_remote_authority_service_no_version_with_resource_with_instance_no_message() {
        UEntity use = UEntity.fromName("body.access");
        UUri Uri = new UUri(UAuthority.remote("VCU", "MY_CAR_VIN"), use, UResource.fromNameWithInstance("door", "front_left"));
        String uProtocolUri = UriFactory.buildUProtocolUri(Uri);
        assertEquals("//vcu.my_car_vin/body.access//door.front_left", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a remote authority with service and version with resource with instance and message")
    public void test_build_protocol_uri_from__uri_when__uri_has_remote_authority_service_and_version_with_resource_with_instance_and_message() {
        UEntity use = new UEntity("body.access", "1");
        UUri Uri = new UUri(UAuthority.remote("VCU", "MY_CAR_VIN"), use, new UResource("door", "front_left", "Door"));
        String uProtocolUri = UriFactory.buildUProtocolUri(Uri);
        assertEquals("//vcu.my_car_vin/body.access/1/door.front_left#Door", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from an  URI Object with a remote authority with service no version with resource with instance and message")
    public void test_build_protocol_uri_from__uri_when__uri_has_remote_authority_service_no_version_with_resource_with_instance_and_message() {
        UEntity use = UEntity.fromName("body.access");
        UUri Uri = new UUri(UAuthority.remote("VCU", "MY_CAR_VIN"), use, new UResource("door", "front_left", "Door"));
        String uProtocolUri = UriFactory.buildUProtocolUri(Uri);
        assertEquals("//vcu.my_car_vin/body.access//door.front_left#Door", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI for the source part of an RPC request, where the source is local")
    public void test_build_protocol_uri_for_source_part_of_rpc_request_where_source_is_local() {
        UAuthority uAuthority = UAuthority.local();
        UEntity use = new UEntity("petapp", "1");
        String uProtocolUri = UriFactory.buildUriForRpc(uAuthority, use);
        assertEquals("/petapp/1/rpc.response", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI for the source part of an RPC request, where the source is remote")
    public void test_build_protocol_uri_for_source_part_of_rpc_request_where_source_is_remote() {
        UAuthority uAuthority = UAuthority.remote("cloud", "uprotocol.example.com");
        UEntity use = UEntity.fromName("petapp");
        String uProtocolUri = UriFactory.buildUriForRpc(uAuthority, use);
        assertEquals("//cloud.uprotocol.example.com/petapp//rpc.response", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI for the service accepting the rpc, when authority is local with software entity no version")
    public void test_build_protocol_uri_for_service_accepting_rpc_local_uauthority_with_use_no_version() {
        UAuthority uAuthority = UAuthority.local();
        UEntity use = UEntity.fromName("body.access");
        String methodName = "UpdateDoor";
        String uProtocolUri = UriFactory.buildMethodUri(uAuthority, use, methodName);
        assertEquals("/body.access//rpc.UpdateDoor", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI for the service accepting the rpc, when authority is local with software entity with version")
    public void test_build_protocol_uri_for_service_accepting_rpc_local_uauthority_with_use_with_version() {
        UAuthority uAuthority = UAuthority.local();
        UEntity use = new UEntity("body.access", "1");
        String methodName = "UpdateDoor";
        String uProtocolUri = UriFactory.buildMethodUri(uAuthority, use, methodName);
        assertEquals("/body.access/1/rpc.UpdateDoor", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI for the service accepting the rpc, when authority is local, software entity is empty")
    public void test_build_protocol_uri_for_service_accepting_rpc_local_uauthority_empty_use() {
        UAuthority uAuthority = UAuthority.local();
        UEntity use = UEntity.fromName(" ");
        String methodName = "UpdateDoor";
        String uProtocolUri = UriFactory.buildMethodUri(uAuthority, use, methodName);
        assertEquals("///rpc.UpdateDoor", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI for the service accepting the rpc, when authority is remote with software entity no version")
    public void test_build_protocol_uri_for_service_accepting_rpc_remote_uauthority_with_use_no_version() {
        UAuthority uAuthority = UAuthority.remote("VCU", "MY_VIN");
        UEntity use = UEntity.fromName("body.access");
        String methodName = "UpdateDoor";
        String uProtocolUri = UriFactory.buildMethodUri(uAuthority, use, methodName);
        assertEquals("//vcu.my_vin/body.access//rpc.UpdateDoor", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI for the service accepting the rpc, when authority is remote with software entity with version")
    public void test_build_protocol_uri_for_service_accepting_rpc_remote_uauthority_with_use_with_version() {
        UAuthority uAuthority = UAuthority.remote("VCU", "MY_VIN");
        UEntity use = new UEntity("body.access", "1");
        String methodName = "UpdateDoor";
        String uProtocolUri = UriFactory.buildMethodUri(uAuthority, use, methodName);
        assertEquals("//vcu.my_vin/body.access/1/rpc.UpdateDoor", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI for the service accepting the rpc, when authority is remote cloud with software entity with version")
    public void test_build_protocol_uri_for_service_accepting_rpc_remote_cloud_uauthority_with_use_with_version() {
        UAuthority uAuthority = UAuthority.remote("cloud", "uprotocol.example.com");
        UEntity use = new UEntity("body.access", "1");
        String methodName = "UpdateDoor";
        String uProtocolUri = UriFactory.buildMethodUri(uAuthority, use, methodName);
        assertEquals("//cloud.uprotocol.example.com/body.access/1/rpc.UpdateDoor", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI for the service accepting the rpc, when authority is remote, software entity is empty")
    public void test_build_protocol_uri_for_service_accepting_rpc_remote_uauthority_empty_use() {
        UAuthority uAuthority = UAuthority.remote("VCU", "MY_VIN");
        UEntity use = UEntity.fromName(" ");
        String methodName = "UpdateDoor";
        String uProtocolUri = UriFactory.buildMethodUri(uAuthority, use, methodName);
        assertEquals("//vcu.my_vin///rpc.UpdateDoor", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from parts that are null")
    public void test_build_protocol_uri_from_parts_when_they_are_null() {
        UAuthority uAuthority = null;
        UEntity uSoftwareEntity = null;
        UResource uResource = null;
        UUri Uri = new UUri(uAuthority, uSoftwareEntity, uResource);
        String uProtocolUri = UriFactory.buildUProtocolUri(Uri);
        assertEquals("", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol URI from the parts of  URI Object with a remote authority with service and version with resource")
    public void test_build_protocol_uri_from__uri_parts_when__uri_has_remote_authority_service_and_version_with_resource() {
        UAuthority uAuthority = UAuthority.remote("VCU", "MY_CAR_VIN");
        UEntity use = new UEntity("body.access", "1");
        UResource uResource = UResource.fromName("door");
        String uProtocolUri = UriFactory.buildUProtocolUri(uAuthority, use, uResource);
        assertEquals("//vcu.my_car_vin/body.access/1/door", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a URI using no scheme")
    public void test_custom_scheme_no_scheme_empty() {
        UAuthority uAuthority = null;
        UEntity uSoftwareEntity = null;
        UResource uResource = null;
        String customUri = UriFactory.buildUProtocolUri(uAuthority, uSoftwareEntity, uResource);
        assertTrue(customUri.isEmpty());
    }

    @Test
    @DisplayName("Test Create a custom URI using no scheme")
    public void test_custom_scheme_no_scheme() {
        UAuthority uAuthority = UAuthority.remote("VCU", "MY_CAR_VIN");
        UEntity use = new UEntity("body.access", "1");
        UResource uResource = UResource.fromName("door");
        String ucustomUri = UriFactory.buildUProtocolUri(uAuthority, use, uResource);
        assertEquals("//vcu.my_car_vin/body.access/1/door", ucustomUri);
    }

    @Test
    @DisplayName("Test parse local uProtocol uri with custom scheme")
    public void test_parse_local_protocol_uri_with_custom_scheme() {
        String uri = "custom:/body.access//door.front_left#Door";
        UUri Uri = UriFactory.parseFromUri(uri);
        assertTrue(Uri.uAuthority().isLocal());
        assertFalse(Uri.uAuthority().isMarkedRemote());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isEmpty());
        assertEquals("door", Uri.uResource().name());
        assertTrue(Uri.uResource().instance().isPresent());
        assertEquals("front_left", Uri.uResource().instance().get());
        assertTrue(Uri.uResource().message().isPresent());
        assertEquals("Door", Uri.uResource().message().get());
    }

    @Test
    @DisplayName("Test parse remote uProtocol uri with custom scheme")
    public void test_parse_remote_protocol_uri_with_custom_scheme() {
        String uri = "custom://vcu.vin/body.access//door.front_left#Door";
        String uri2 = "//vcu.vin/body.access//door.front_left#Door";
        UUri Uri = UriFactory.parseFromUri(uri);
        assertFalse(Uri.uAuthority().isLocal());
        assertTrue(Uri.uAuthority().isMarkedRemote());
        assertEquals("vcu", Uri.uAuthority().device().get());
        assertTrue(Uri.uAuthority().domain().isPresent());
        assertEquals("vin", Uri.uAuthority().domain().get());
        assertEquals("body.access", Uri.uEntity().name());
        assertTrue(Uri.uEntity().version().isEmpty());
        assertEquals("door", Uri.uResource().name());
        assertTrue(Uri.uResource().instance().isPresent());
        assertEquals("front_left", Uri.uResource().instance().get());
        assertTrue(Uri.uResource().message().isPresent());
        assertEquals("Door", Uri.uResource().message().get());
        assertEquals(uri2, Uri.uProtocolUri());
    }

    @Test
    @DisplayName("Test Create a uProtocol Short URI from a URI Object")
    public void test_build_short_uri_from_uri() {
        UAuthority uAuthority = UAuthority.local();
        UEntity use = new UEntity("body.access", "1", (short)5);
        UResource uResource = new UResource("door", "front_left", "Door", (short)3);

        UUri Uri = new UUri(uAuthority, use, uResource);
        String uProtocolUri = UriFactory.buildUProtocolShortUri(Uri);
        assertEquals("/5/1/3", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol Short URI for empty URI")
    public void test_build_short_uri_from_uri_missing_ids() {
        UUri Uri = UUri.empty();
        String uProtocolUri = UriFactory.buildUProtocolShortUri(Uri);
        assertEquals("", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol Short URI for null URI")
    public void test_build_short_uri_from_null_uri() {
        String uProtocolUri = UriFactory.buildUProtocolShortUri(null);
        assertEquals("", uProtocolUri);
    }


    @Test
    @DisplayName("Test Create a uProtocol Short URI with remote URI")
    public void test_build_short_uri_from_remote_uri() {
        String ipv6Address = "2001:db8:85a3:0:0:8a2e:370:7334";
        InetAddress address = null;
        try {
            address = InetAddress.getByName(ipv6Address);
        }  
        catch (UnknownHostException e) {
            e.printStackTrace();
        }
        UAuthority uAuthority = UAuthority.remote(address);
        UEntity use = new UEntity("body.access", "1", (short)5);
        UResource uResource = new UResource("door", "front_left", "Door", (short)3);

        String uProtocolUri = UriFactory.buildUProtocolShortUri(uAuthority, use, uResource);
        assertEquals("//2001:db8:85a3:0:0:8a2e:370:7334/5/1/3", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol Short URI with remote URI without uEntity")
    public void test_build_short_uri_from_remote_uri_missing_uentity() {
        String ipv6Address = "2001:db8:85a3:0:0:8a2e:370:7334";
        InetAddress address = null;
        try {
            address = InetAddress.getByName(ipv6Address);
        }  
        catch (UnknownHostException e) {
            e.printStackTrace();
        }
        UAuthority uAuthority = UAuthority.remote(address);

        String uProtocolUri = UriFactory.buildUProtocolShortUri(uAuthority, UEntity.empty(), UResource.empty());
        assertEquals("//2001:db8:85a3:0:0:8a2e:370:7334/", uProtocolUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol Short URI when address is missing")
    public void test_build_short_uri_from_remote_uri_missing_address() {
        UAuthority uAuthority = UAuthority.remote("VCU", "MY_CAR_VIN");
        UEntity use = new UEntity("body.access", "1");
        UResource uResource = UResource.fromName("door");
        String ucustomUri = UriFactory.buildUProtocolShortUri(uAuthority, use, uResource);
        assertEquals("///", ucustomUri);
    }

    @Test
    @DisplayName("Test Create a uProtocol Micro URI with empty URI")
    public void test_build_micro_uri_from_uri_missing_ids() {
        UUri Uri = UUri.empty();
        byte[] uProtocolUri = UriFactory.buildUProtocolMicroUri(Uri);
        assertEquals(0, uProtocolUri.length);
    }


    @Test
    @DisplayName("Test Create a uProtocol Micro URI with remote URI without uEntity")
    public void test_build_micro_uri_from_remote_uri_missing_uentity() {
        String ipv6Address = "2001:db8:85a3:0:0:8a2e:370:7334";
        InetAddress address = null;
        try {
            address = InetAddress.getByName(ipv6Address);
        }  
        catch (UnknownHostException e) {
            e.printStackTrace();
        }
        UAuthority uAuthority = UAuthority.remote(address);

        byte[] uProtocolUri = UriFactory.buildUProtocolMicroUri(new UUri(uAuthority, UEntity.empty(), UResource.empty()));
        assertEquals(0, uProtocolUri.length);
    }

    @Test
    @DisplayName("Test Create a uProtocol Micro URI for local UAuthority")
    public void test_build_micro_uri_from_local_uri_simple_version() {
        UAuthority uAuthority = UAuthority.local();
        UEntity use = new UEntity("body.access", "1", (short)5);
        UResource uResource = new UResource("door", "front_left", "Door", (short)3);

        byte[] uProtocolUri = UriFactory.buildUProtocolMicroUri(new UUri(uAuthority, use, uResource));
        assertEquals(8, uProtocolUri.length);
        assertEquals(1, uProtocolUri[0]); // version 1
        assertEquals(0, uProtocolUri[1]); // local
        assertEquals(0, uProtocolUri[2]); // UResource ID (MSB)
        assertEquals(3, uProtocolUri[3]); // UResource ID (LSB)
        assertEquals(0, uProtocolUri[4]); // UEntity ID (MSB)
        assertEquals(5, uProtocolUri[5]); // UEntity ID (LSB)
        assertEquals(1<<3, uProtocolUri[6]); // UEntity Version (MSB)
        assertEquals(0, uProtocolUri[7]); // UEntity Version (LSB)
    }

    @Test
    @DisplayName("Test Create a uProtocol Micro URI for local UAuthority")
    public void test_build_micro_uri_from_local_uri() {
        UAuthority uAuthority = UAuthority.local();
        UEntity use = new UEntity("body.access", "1.1", (short)5);
        UResource uResource = new UResource("door", "front_left", "Door", (short)3);

        byte[] uProtocolUri = UriFactory.buildUProtocolMicroUri(new UUri(uAuthority, use, uResource));
        assertEquals(8, uProtocolUri.length);
        assertEquals(1, uProtocolUri[0]); // version 1
        assertEquals(0, uProtocolUri[1]); // local
        assertEquals(0, uProtocolUri[2]); // UResource ID (MSB)
        assertEquals(3, uProtocolUri[3]); // UResource ID (LSB)
        assertEquals(0, uProtocolUri[4]); // UEntity ID (MSB)
        assertEquals(5, uProtocolUri[5]); // UEntity ID (LSB)
        assertEquals(1<<3, uProtocolUri[6]); // UEntity Version (MSB)
        assertEquals(1, uProtocolUri[7]); // UEntity Version (LSB)
    }

    @Test
    @DisplayName("Test Create a uProtocol Micro URI for local UAuthority large minor version")
    public void test_build_micro_uri_from_local_uri_large_minor_version() {
        UAuthority uAuthority = UAuthority.local();
        UEntity use = new UEntity("body.access", "1.599", (short)5);
        UResource uResource = new UResource("door", "front_left", "Door", (short)3);

        byte[] uProtocolUri = UriFactory.buildUProtocolMicroUri(new UUri(uAuthority, use, uResource));
        assertEquals(8, uProtocolUri.length);
        assertEquals(1, uProtocolUri[0]); // version 1
        assertEquals(0, uProtocolUri[1]); // local
        assertEquals(0, uProtocolUri[2]); // UResource ID (MSB)
        assertEquals(3, uProtocolUri[3]); // UResource ID (LSB)
        assertEquals(0, uProtocolUri[4]); // UEntity ID (MSB)
        assertEquals(5, uProtocolUri[5]); // UEntity ID (LSB)
        assertEquals(10, uProtocolUri[6]); // UEntity Version (MSB)
        assertEquals(599 & 0xff, uProtocolUri[7]); // UEntity Version (LSB)
    }


    @Test
    @DisplayName("Test Create a uProtocol Micro URI for local UAuthority no version")
    public void test_build_micro_uri_from_local_uri_no_version() {
        UAuthority uAuthority = UAuthority.local();
        UEntity use = new UEntity("body.access", null, (short)5);
        UResource uResource = new UResource("door", "front_left", "Door", (short)3);

        byte[] uProtocolUri = UriFactory.buildUProtocolMicroUri(new UUri(uAuthority, use, uResource));
        assertEquals(8, uProtocolUri.length);
        assertEquals(1, uProtocolUri[0]); // version 1
        assertEquals(0, uProtocolUri[1]); // local
        assertEquals(0, uProtocolUri[2]); // UResource ID (MSB)
        assertEquals(3, uProtocolUri[3]); // UResource ID (LSB)
        assertEquals(0, uProtocolUri[4]); // UEntity ID (MSB)
        assertEquals(5, uProtocolUri[5]); // UEntity ID (LSB)
        assertEquals((byte)Short.MAX_VALUE>>8, uProtocolUri[6]); // UEntity Version (MSB)
        assertEquals((byte)Short.MAX_VALUE, uProtocolUri[7]); // UEntity Version (LSB)
    }

    @Test
    @DisplayName("Test Create a uProtocol Micro URI to byte[] then call parseFromMicroUri to convert back to UUri")
    public void test_build_micro_uri_from_local_uri_then_parse_back_to_uri() {
        UAuthority uAuthority = UAuthority.local();
        UEntity use = new UEntity("", "1.599", (short)5);
        UResource uResource = UResource.fromId((short)3);

        byte[] uProtocolUri = UriFactory.buildUProtocolMicroUri(new UUri(uAuthority, use, uResource));
        UUri Uri = UriFactory.parseFromMicroUri(uProtocolUri);
        assertEquals(uAuthority, Uri.uAuthority());
        assertEquals(use, Uri.uEntity());
        assertEquals(uResource, Uri.uResource());
    }
    

}