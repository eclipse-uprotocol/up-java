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

import org.eclipse.uprotocol.uri.validator.UriValidator;
import org.eclipse.uprotocol.v1.UUri;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;


public class UriSerializerTest {


    @Test
    @DisplayName("Test using the serializers")
    public void test_using_the_serializers() {
        UUri uri = UUri.newBuilder()
                .setAuthorityName("myAuthority")
                .setUeId(1)
                .setUeVersionMajor(2)
                .setResourceId(3)
                .build();

        String serializedUri = UriSerializer.serialize(uri);
        assertEquals("//myAuthority/1/2/3", serializedUri);
    }

    @Test
    @DisplayName("Test deserializing a null UUri")
    public void test_deserializing_a_null_UUri() {
        UUri uri = UriSerializer.deserialize(null);
        assertTrue(UriValidator.isEmpty(uri));
    }

    @Test
    @DisplayName("Test deserializing an empty UUri")
    public void test_deserializing_an_empty_UUri() {
        UUri uri = UriSerializer.deserialize("");
        assertTrue(UriValidator.isEmpty(uri));
    }


    @Test
    @DisplayName("Test deserializing a blank UUri")
    public void test_deserializing_a_blank_UUri() {
        UUri uri = UriSerializer.deserialize("  ");
        assertTrue(UriValidator.isEmpty(uri));
    }

    @Test
    @DisplayName("Test deserializing with a valid URI that has scheme")
    public void test_deserializing_with_a_valid_URI_that_has_scheme() {
        UUri uri = UriSerializer.deserialize("up://myAuthority/1/2/3");
        assertEquals("myAuthority", uri.getAuthorityName());
        assertEquals(1, uri.getUeId());
        assertEquals(2, uri.getUeVersionMajor());
        assertEquals(3, uri.getResourceId());
    }

    @Test
    @DisplayName("Test deserializing with a valid URI that has scheme but nothing else")
    public void test_deserializing_with_a_valid_URI_that_has_scheme_but_nothing_else() {
        UUri uri = UriSerializer.deserialize("up://");
        assertTrue(UriValidator.isEmpty(uri));
    }

    @Test
    @DisplayName("Test deserializing a valid UUri with all fields")
    public void test_deserializing_a_valid_UUri_with_all_fields() {
        UUri uri = UriSerializer.deserialize("//myAuthority/1/2/3");
        assertEquals("myAuthority", uri.getAuthorityName());
        assertEquals(1, uri.getUeId());
        assertEquals(2, uri.getUeVersionMajor());
        assertEquals(3, uri.getResourceId());
    }

    @Test
    @DisplayName("Test deserializing a valid UUri with only authority")
    public void test_deserializing_a_valid_UUri_with_only_authority() {
        UUri uri = UriSerializer.deserialize("//myAuthority");
        assertEquals("myAuthority", uri.getAuthorityName());
        assertEquals(0, uri.getUeId());
        assertEquals(0, uri.getUeVersionMajor());
        assertEquals(0, uri.getResourceId());
    }

    @Test
    @DisplayName("Test deserializing a valid UUri with only authority and ueId")
    public void test_deserializing_a_valid_UUri_with_only_authority_and_ueId() {
        UUri uri = UriSerializer.deserialize("//myAuthority/1");
        assertEquals("myAuthority", uri.getAuthorityName());
        assertEquals(1, uri.getUeId());
        assertEquals(0, uri.getUeVersionMajor());
        assertEquals(0, uri.getResourceId());
    }

    @Test
    @DisplayName("Test deserializing a valid UUri with only authority, ueId and ueVersionMajor")
    public void test_deserializing_a_valid_UUri_with_only_authority_ueId_and_ueVersionMajor() {
        UUri uri = UriSerializer.deserialize("//myAuthority/1/2");
        assertEquals("myAuthority", uri.getAuthorityName());
        assertEquals(1, uri.getUeId());
        assertEquals(2, uri.getUeVersionMajor());
        assertEquals(0, uri.getResourceId());
    }

    @Test
    @DisplayName("Test deserializing a string with invalid characters at the beginning")
    public void test_deserializing_a_string_with_invalid_characters() {
        UUri uri = UriSerializer.deserialize("$$");
        assertTrue(UriValidator.isEmpty(uri));
    }

    @Test
    @DisplayName("Test deserializing a string with names instead of ids for UeId")
    public void test_deserializing_a_string_with_names_instead_of_ids_for_UeId() {
        UUri uri = UriSerializer.deserialize("//myAuthority/myUeId/2/3");
        assertTrue(UriValidator.isEmpty(uri));
    }

    @Test
    @DisplayName("Test deserializing a string with names instead of ids for UeVersionMajor")
    public void test_deserializing_a_string_with_names_instead_of_ids_for_UeVersionMajor() {
        UUri uri = UriSerializer.deserialize("//myAuthority/1/myUeVersionMajor/3");
        assertTrue(UriValidator.isEmpty(uri));
    }

    @Test
    @DisplayName("Test deserializing a string with names instead of ids for ResourceId")
    public void test_deserializing_a_string_with_names_instead_of_ids_for_ResourceId() {
        UUri uri = UriSerializer.deserialize("//myAuthority/1/2/myResourceId");
        assertTrue(UriValidator.isEmpty(uri));
    }

    @Test
    @DisplayName("Test deserializing a string without authority")
    public void test_deserializing_a_string_without_authority() {
        UUri uri = UriSerializer.deserialize("/1/2/3");
        assertEquals(1, uri.getUeId());
        assertEquals(2, uri.getUeVersionMajor());
        assertEquals(3, uri.getResourceId());
        assertTrue(uri.getAuthorityName().isBlank());
    }

    @Test
    @DisplayName("Test deserializing a string without authority and ResourceId")
    public void test_deserializing_a_string_without_authority_and_ResourceId() {
        UUri uri = UriSerializer.deserialize("/1/2");
        assertEquals(1, uri.getUeId());
        assertEquals(2, uri.getUeVersionMajor());
        assertEquals(0, uri.getResourceId());
        assertTrue(uri.getAuthorityName().isBlank());
    }

    @Test
    @DisplayName("Test deserializing a string without authority, ResourceId and UeVersionMajor")
    public void test_deserializing_a_string_without_authority_ResourceId_and_UeVersionMajor() {
        UUri uri = UriSerializer.deserialize("/1");
        assertEquals(1, uri.getUeId());
        assertEquals(0, uri.getUeVersionMajor());
        assertEquals(0, uri.getResourceId());
        assertTrue(uri.getAuthorityName().isBlank());
    }

    @Test
    @DisplayName("Test deserializing a string with blank authority")
    public void test_deserializing_a_string_with_blank_authority() {
        UUri uri = UriSerializer.deserialize("///2");
        assertTrue(UriValidator.isEmpty(uri));
    }

    @Test
    @DisplayName("Test deserializing a string with all the items are the wildcard values")
    public void test_deserializing_a_string_with_all_the_items_are_the_wildcard_values() {
        UUri uri = UriSerializer.deserialize("//*/65535/255/65535");
        assertEquals("*", uri.getAuthorityName());
        assertEquals(0xFFFF, uri.getUeId());
        assertEquals(0xFF, uri.getUeVersionMajor());
        assertEquals(0xFFFF, uri.getResourceId());
    }

    @Test
    @DisplayName("Test deserializing a string with uEId() out of range")
    public void test_deserializing_a_string_with_uEId_out_of_range() {
        UUri uri = UriSerializer.deserialize("/68719476735/2/3");
        assertTrue(UriValidator.isEmpty(uri));
    }

    @Test
    @DisplayName("Test deserializing a string with uEVersionMajor out of range")
    public void test_deserializing_a_string_with_uEVersionMajor_out_of_range() {
        UUri uri = UriSerializer.deserialize("/1/256/3");
        assertTrue(UriValidator.isEmpty(uri));
    }

    @Test
    @DisplayName("Test deserializing a string with resourceId out of range")
    public void test_deserializing_a_string_with_resourceId_out_of_range() {
        UUri uri = UriSerializer.deserialize("/1/2/65536");
        assertTrue(UriValidator.isEmpty(uri));
    }

    @Test
    @DisplayName("Test deserializing a string with negative uEId")
    public void test_deserializing_a_string_with_negative_uEId() {
        UUri uri = UriSerializer.deserialize("/-1/2/3");
        assertTrue(UriValidator.isEmpty(uri));
    }

    @Test
    @DisplayName("Test deserializing a string with negative uEVersionMajor")
    public void test_deserializing_a_string_with_negative_uEVersionMajor() {
        UUri uri = UriSerializer.deserialize("/1/-2/3");
        assertTrue(UriValidator.isEmpty(uri));
    }

    @Test
    @DisplayName("Test deserializing a string with negative resourceId")
    public void test_deserializing_a_string_with_negative_resourceId() {
        UUri uri = UriSerializer.deserialize("/1/2/-3");
        assertTrue(UriValidator.isEmpty(uri));
    }

    @Test
    @DisplayName("Test deserializing a string with wildcard ResourceId")
    public void test_deserializing_a_string_with_wildcard_resourceId() {
        UUri uri = UriSerializer.deserialize("/1/2/65535");
        assertEquals(1, uri.getUeId());
        assertEquals(2, uri.getUeVersionMajor());
        assertEquals(0xFFFF, uri.getResourceId());
    }

    @Test
    @DisplayName("Test serializing an Empty UUri")
    public void test_serializing_an_empty_UUri() {
        UUri uri = UUri.getDefaultInstance();
        String serializedUri = UriSerializer.serialize(uri);
        assertTrue(serializedUri.isBlank());
    }

    @Test
    @DisplayName("Test serializing a null UUri")
    public void test_serializing_a_null_UUri() {
        String serializedUri = UriSerializer.serialize(null);
        assertTrue(serializedUri.isBlank());
    }

    @Test
    @DisplayName("Test serializing a full UUri")
    public void test_serializing_a_full_UUri() {
        UUri uri = UUri.newBuilder()
                .setAuthorityName("myAuthority")
                .setUeId(1)
                .setUeVersionMajor(2)
                .setResourceId(3)
                .build();
        String serializedUri = UriSerializer.serialize(uri);
        assertEquals("//myAuthority/1/2/3", serializedUri);
    }

    @Test
    @DisplayName("Test serializing a UUri with only authority")
    public void test_serializing_a_UUri_with_only_authority() {
        UUri uri = UUri.newBuilder()
                .setAuthorityName("myAuthority")
                .build();
        String serializedUri = UriSerializer.serialize(uri);
        assertEquals("//myAuthority/0/0/0", serializedUri);
    }

    @Test
    @DisplayName("Test serializing a UUri with only authority and ueId")
    public void test_serializing_a_UUri_with_only_authority_and_ueId() {
        UUri uri = UUri.newBuilder()
                .setAuthorityName("myAuthority")
                .setUeId(1)
                .build();
        String serializedUri = UriSerializer.serialize(uri);
        assertEquals("//myAuthority/1/0/0", serializedUri);
    }

    @Test
    @DisplayName("Test serializing a UUri with only authority, ueId and ueVersionMajor")
    public void test_serializing_a_UUri_with_only_authority_ueId_and_ueVersionMajor() {
        UUri uri = UUri.newBuilder()
                .setAuthorityName("myAuthority")
                .setUeId(1)
                .setUeVersionMajor(2)
                .build();
        String serializedUri = UriSerializer.serialize(uri);
        assertEquals("//myAuthority/1/2/0", serializedUri);
    }

    @Test
    @DisplayName("Test serializing a UUri with only authority, ueId, ueVersionMajor and resourceId")
    public void test_serializing_a_UUri_with_only_authority_ueId_ueVersionMajor_and_resourceId() {
        UUri uri = UUri.newBuilder()
                .setAuthorityName("myAuthority")
                .setUeId(1)
                .setUeVersionMajor(2)
                .setResourceId(3)
                .build();
        String serializedUri = UriSerializer.serialize(uri);
        assertEquals("//myAuthority/1/2/3", serializedUri);
    }
}
