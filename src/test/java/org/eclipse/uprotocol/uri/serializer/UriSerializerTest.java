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

import org.eclipse.uprotocol.uri.factory.UEntityFactory;
import org.eclipse.uprotocol.uri.factory.UResourceBuilder;
import org.eclipse.uprotocol.uri.validator.UriValidator;
import org.eclipse.uprotocol.v1.UAuthority;
import org.eclipse.uprotocol.v1.UEntity;
import org.eclipse.uprotocol.v1.UResource;
import org.eclipse.uprotocol.v1.UUri;
import org.eclipse.uprotocol.UprotocolOptions;
import org.eclipse.uprotocol.core.usubscription.v3.USubscriptionProto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.protobuf.ByteString;
import com.google.protobuf.DescriptorProtos.ServiceOptions;
import com.google.protobuf.Descriptors.ServiceDescriptor;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.*;

public class UriSerializerTest {


    @Test
    @DisplayName("Test building uSubscription Update Notification topic and comparing long, short, and micro URIs")
    public void test_build_resolved_full_information_compare() {
        
        ServiceDescriptor descriptor = USubscriptionProto.getDescriptor().getServices().get(0);

        UEntity entity = UEntityFactory.fromProto(descriptor);
        
        ServiceOptions options = descriptor.getOptions();

        UResource resource = options.getExtension(UprotocolOptions.notificationTopic)
            .stream()
            .filter(p -> p.getName().contains("SubscriptionChange"))
            .map(UResourceBuilder::fromUServiceTopic)
            .findFirst()
            .orElse(UResource.newBuilder().build());
        
        UUri uUri = UUri.newBuilder()
            .setEntity(entity)
            .setResource(resource)
            .build();

        assertFalse(UriValidator.isEmpty(uUri));
        assertTrue(UriValidator.isMicroForm(uUri));
        assertTrue(UriValidator.isLongForm(uUri));
        assertTrue(UriValidator.isShortForm(uUri));
        String longUri = LongUriSerializer.instance().serialize(uUri);
        byte[] microUri = MicroUriSerializer.instance().serialize(uUri);
        String shortUri = ShortUriSerializer.instance().serialize(uUri);
        
        assertEquals(longUri, "/core.usubscription/3/SubscriptionChange#Update");
        assertEquals(shortUri, "/0/3/32768");
        assertEquals(Arrays.toString(microUri), "[1, 0, -128, 0, 0, 0, 3, 0]");
    }


    @Test
    @DisplayName("Test building uSubscription Update Notification topic with IPv4 address UAuthority and comparing long, short, and micro URIs")
    public void test_build_resolved_full_information_compare_with_ipv4() throws UnknownHostException {
        
        ServiceDescriptor descriptor = USubscriptionProto.getDescriptor().getServices().get(0);
        UEntity entity = UEntityFactory.fromProto(descriptor);
        ServiceOptions options = descriptor.getOptions();

        UResource resource = options.getExtension(UprotocolOptions.notificationTopic)
            .stream()
            .filter(p -> p.getName().contains("SubscriptionChange"))
            .map(UResourceBuilder::fromUServiceTopic)
            .findFirst()
            .orElse(UResource.newBuilder().build());
        
        UUri uUri = UUri.newBuilder()
            .setEntity(entity)
            .setResource(resource)
            .setAuthority(UAuthority.newBuilder()
                .setName("vcu.veh.gm.com")
                .setIp(ByteString.copyFrom(InetAddress.getByName("192.168.1.100").getAddress())))
            .build();

        assertFalse(UriValidator.isEmpty(uUri));
        assertTrue(UriValidator.isMicroForm(uUri));
        assertTrue(UriValidator.isLongForm(uUri));
        assertTrue(UriValidator.isShortForm(uUri));
        String longUri = LongUriSerializer.instance().serialize(uUri);
        byte[] microUri = MicroUriSerializer.instance().serialize(uUri);
        String shortUri = ShortUriSerializer.instance().serialize(uUri);
        
        assertEquals(longUri, "//vcu.veh.gm.com/core.usubscription/3/SubscriptionChange#Update");
        assertEquals(shortUri, "//192.168.1.100/0/3/32768");
        assertEquals(Arrays.toString(microUri), "[1, 1, -128, 0, 0, 0, 3, 0, -64, -88, 1, 100]");
    }

    @Test
    @DisplayName("Test building uSubscription Update Notification topic with IPv6 address UAuthority and comparing long, short, and micro URIs")
    public void test_build_resolved_full_information_compare_with_ipv6() throws UnknownHostException {
        
        ServiceDescriptor descriptor = USubscriptionProto.getDescriptor().getServices().get(0);
        UEntity entity = UEntityFactory.fromProto(descriptor);
        ServiceOptions options = descriptor.getOptions();

        UResource resource = options.getExtension(UprotocolOptions.notificationTopic)
            .stream()
            .filter(p -> p.getName().contains("SubscriptionChange"))
            .map(UResourceBuilder::fromUServiceTopic)
            .findFirst()
            .orElse(UResource.newBuilder().build());
        
        UUri uUri = UUri.newBuilder()
            .setEntity(entity)
            .setResource(resource)
            .setAuthority(UAuthority.newBuilder()
                .setName("vcu.veh.gm.com")
                .setIp(ByteString.copyFrom(InetAddress.getByName("2001:db8:85a3:0:0:8a2e:370:7334").getAddress())))
            .build();

        assertFalse(UriValidator.isEmpty(uUri));
        assertTrue(UriValidator.isMicroForm(uUri));
        assertTrue(UriValidator.isLongForm(uUri));
        assertTrue(UriValidator.isShortForm(uUri));
        String longUri = LongUriSerializer.instance().serialize(uUri);
        byte[] microUri = MicroUriSerializer.instance().serialize(uUri);
        String shortUri = ShortUriSerializer.instance().serialize(uUri);
        
        assertEquals(longUri, "//vcu.veh.gm.com/core.usubscription/3/SubscriptionChange#Update");
        assertEquals(shortUri, "//2001:db8:85a3:0:0:8a2e:370:7334/0/3/32768");
        assertEquals(Arrays.toString(microUri), "[1, 2, -128, 0, 0, 0, 3, 0, 32, 1, 13, -72, -123, -93, 0, 0, 0, 0, -118, 46, 3, 112, 115, 52]");
    }

    @Test
    @DisplayName("Test building uSubscription Update Notification topic with id address UAuthority and comparing long, short, and micro URIs")
    public void test_build_resolved_full_information_compare_with_id() throws UnknownHostException {
        
        ServiceDescriptor descriptor = USubscriptionProto.getDescriptor().getServices().get(0);
        UEntity entity = UEntityFactory.fromProto(descriptor);
        ServiceOptions options = descriptor.getOptions();

        UResource resource = options.getExtension(UprotocolOptions.notificationTopic)
            .stream()
            .filter(p -> p.getName().contains("SubscriptionChange"))
            .map(UResourceBuilder::fromUServiceTopic)
            .findFirst()
            .orElse(UResource.newBuilder().build());
        
        UUri uUri = UUri.newBuilder()
            .setEntity(entity)
            .setResource(resource)
            .setAuthority(UAuthority.newBuilder()
                .setName("1G1YZ23J9P5800001.veh.gm.com")
                .setId(ByteString.copyFromUtf8("1G1YZ23J9P5800001")))
            .build();

        assertFalse(UriValidator.isEmpty(uUri));
        assertTrue(UriValidator.isMicroForm(uUri));
        assertTrue(UriValidator.isLongForm(uUri));
        assertTrue(UriValidator.isShortForm(uUri));
        String longUri = LongUriSerializer.instance().serialize(uUri);
        byte[] microUri = MicroUriSerializer.instance().serialize(uUri);
        String shortUri = ShortUriSerializer.instance().serialize(uUri);
        
        assertEquals(longUri, "//1G1YZ23J9P5800001.veh.gm.com/core.usubscription/3/SubscriptionChange#Update");
        assertEquals(shortUri, "//1G1YZ23J9P5800001/0/3/32768");
        assertEquals(Arrays.toString(microUri), "[1, 3, -128, 0, 0, 0, 3, 0, 17, 49, 71, 49, 89, 90, 50, 51, 74, 57, 80, 53, 56, 48, 48, 48, 48, 49]");
    }
}
