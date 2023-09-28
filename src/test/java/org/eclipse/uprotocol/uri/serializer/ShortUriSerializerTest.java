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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.uprotocol.uri.datamodel.UAuthority;
import org.eclipse.uprotocol.uri.datamodel.UEntity;
import org.eclipse.uprotocol.uri.datamodel.UResource;
import org.eclipse.uprotocol.uri.datamodel.UUri;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ShortUriSerializerTest {
    @Test
    @DisplayName("Test serialize empty UUri")
    public void test_serialize_empty_uuri() {
        final String strUri = ShortUriSerializer.instance().serialize(UUri.empty());
        assertEquals("", strUri);
    }

    @Test
    @DisplayName("Test serialize null UUri")
    public void test_serialize_null_uuri() {
        final String strUri = ShortUriSerializer.instance().serialize(null);
        assertEquals("", strUri);
    }

    @Test
    @DisplayName("Test serialize Resolved local UUri")
    public void test_serialize_resolved_local_uuri() {
        final UEntity uEntity = UEntity.resolvedFormat("hartley", 1, (short)2);
        final UResource uResource = UResource.resolvedFormat("salary", "raise", "Salary", (short)4);
        final UUri uri = new UUri(UAuthority.local(), uEntity, uResource);
        final String strUri = ShortUriSerializer.instance().serialize(uri);
        assertFalse(strUri.isEmpty());
        assertEquals("s:/2/1/4", strUri);
    }

    @Test
    @DisplayName("Test serialize Resolved remote UUri")
    public void test_serialize_resolved_remote_uuri() {
        final UAuthority uAuthority = UAuthority.longRemote("vcu", "vin");
        final UEntity uEntity = UEntity.resolvedFormat("hartley", 1, (short)2);
        final UResource uResource = UResource.resolvedFormat("salary", "raise", "Salary", (short)4);
        final UUri uri = new UUri(uAuthority, uEntity, uResource);
        final String strUri = ShortUriSerializer.instance().serialize(uri);
        assertFalse(strUri.isEmpty());
        assertEquals("s://vcu.vin/2/1/4", strUri);
    }

    @Test
    @DisplayName("Test serialize with empty authority names")
    public void test_serialize_missing_authority_names() {
        final UAuthority uAuthority = UAuthority.longRemote("", "");
        final UEntity uEntity = UEntity.microFormat((short)2, 1);
        final UResource uResource = UResource.microFormat((short)4);
        final UUri uri = new UUri(uAuthority, uEntity, uResource);
        final String strUri = ShortUriSerializer.instance().serialize(uri);
        assertEquals("s:///2/1/4", strUri);
    }

    @Test
    @DisplayName("Test serialize with empty UEntity")
    public void test_serialize_empty_uentity() {
        final UAuthority uAuthority = UAuthority.local();
        final UEntity uEntity = UEntity.empty();
        final UResource uResource = UResource.microFormat((short)4);
        final UUri uri = new UUri(uAuthority, uEntity, uResource);
        final String strUri = ShortUriSerializer.instance().serialize(uri);
        assertEquals("s:/", strUri);
    }

    @Test
    @DisplayName("Test serialize with empty UResource")
    public void test_serialize_empty_uresource() {
        final UAuthority uAuthority = UAuthority.local();
        final UEntity uEntity = UEntity.microFormat((short)2, 1);
        final UResource uResource = UResource.empty();
        final UUri uri = new UUri(uAuthority, uEntity, uResource);
        final String strUri = ShortUriSerializer.instance().serialize(uri);
        assertEquals("s:/2/1", strUri);
    }

    @Test
    @DisplayName("Test serialize with UResource that is missing uresource id")
    public void test_serialize_missing_uresource_id() {
        final UAuthority uAuthority = UAuthority.local();
        final UEntity uEntity = UEntity.microFormat((short)2, 1);
        final UResource uResource = UResource.longFormat("raise");
        final UUri uri = new UUri(uAuthority, uEntity, uResource);
        final String strUri = ShortUriSerializer.instance().serialize(uri);
        assertEquals("s:/2/1", strUri);
    }

    @Test
    @DisplayName("Test deserialize null string")
    public void test_deserialize_null_string() {
        final UUri uri = ShortUriSerializer.instance().deserialize(null);
        assertEquals(UUri.empty(), uri);
    }

    @Test
    @DisplayName("Test deserialize empty string")
    public void test_deserialize_empty_string() {
        final UUri uri = ShortUriSerializer.instance().deserialize("");
        assertEquals(UUri.empty(), uri);
    }

    @Test
    @DisplayName("Test deserialize string with no slashes")
    public void test_deserialize_string_with_no_slashes() {
        final UUri uri = ShortUriSerializer.instance().deserialize("abc");
        assertEquals(UUri.empty(), uri);
    }

    @Test
    @DisplayName("Test deserialize string with one slash")
    public void test_deserialize_string_with_one_slash() {
        final UUri uri = ShortUriSerializer.instance().deserialize("s:/");
        assertEquals(UUri.empty(), uri);
    }

    @Test
    @DisplayName("Test deserialize string with two slashes")
    public void test_deserialize_string_with_two_slashes() {
        final UUri uri = ShortUriSerializer.instance().deserialize("s://");
        assertTrue(uri.isEmpty());
        assertTrue(uri.uAuthority().isMarkedRemote());
    }

    @Test
    @DisplayName("Test deserialize string with three slashes")
    public void test_deserialize_string_with_three_slashes() {
        final UUri uri = ShortUriSerializer.instance().deserialize("s:///");
        assertTrue(uri.isEmpty());
        assertTrue(uri.uAuthority().isMarkedRemote());
    }

    @Test
    @DisplayName("Test deserialize string with four slashes")
    public void test_deserialize_string_with_four_slashes() {
        final UUri uri = ShortUriSerializer.instance().deserialize("s:////");
        assertTrue(uri.isEmpty());
        assertTrue(uri.uAuthority().isMarkedRemote());
    }


    @Test
    @DisplayName("Test deserialize string with without any parts")
    public void test_deserialize_string_without_any_parts() {
        final UUri uri = ShortUriSerializer.instance().deserialize("s:");
        assertTrue(uri.isEmpty());
    }
    

    @Test
    @DisplayName("Test deserialize string with only device part of authority and no entity or resource")
    public void test_deserialize_string_with_only_device_part_ofauthority_and_no_entity_or_resource() {
        final UUri uri = ShortUriSerializer.instance().deserialize("s://vcu");
        assertFalse(uri.isEmpty());
        assertTrue(uri.uAuthority().isMarkedRemote());
        assertEquals("vcu", uri.uAuthority().device().orElse(""));
        assertFalse(uri.uAuthority().domain().isPresent());
        assertTrue(uri.uEntity().isEmpty());
        assertTrue(uri.uResource().isEmpty());
    }
    @Test
    @DisplayName("Test deserialize string with authority and no entity or resource")
    public void test_deserialize_string_with_authority_and_no_entity_or_resource() {
        final UUri uri = ShortUriSerializer.instance().deserialize("s://vcu.vin");
        assertFalse(uri.isEmpty());
        assertTrue(uri.uAuthority().isMarkedRemote());
        assertEquals("vcu", uri.uAuthority().device().orElse(""));
        assertEquals("vin", uri.uAuthority().domain().orElse(""));
        assertTrue(uri.uEntity().isEmpty());
        assertTrue(uri.uResource().isEmpty());
    }

    @Test
    @DisplayName("Test deserialize string with authority and entity and no resource")
    public void test_deserialize_string_with_authority_and_entity_and_no_resource() {
        final UUri uri = ShortUriSerializer.instance().deserialize("s://vcu.vin/2/1");
        assertFalse(uri.isEmpty());
        assertTrue(uri.uAuthority().isMarkedRemote());
        assertEquals("vcu", uri.uAuthority().device().orElse(""));
        assertEquals("vin", uri.uAuthority().domain().orElse(""));
        assertFalse(uri.uEntity().isEmpty());
        assertEquals((short)2, uri.uEntity().id().orElse((short)0));
        assertEquals(1, uri.uEntity().version().orElse(0));
        assertTrue(uri.uResource().isEmpty());
    }

    @Test
    @DisplayName("Test deserialize string with authority and missing UEntity but with uResource")
    public void test_deserialize_string_with_authority_and_missing_entity_with_resource() {
        final UUri uri = ShortUriSerializer.instance().deserialize("s://vcu.vin///2");
        assertFalse(uri.isEmpty());
        assertTrue(uri.uAuthority().isMarkedRemote());
        assertEquals("vcu", uri.uAuthority().device().orElse(""));
        assertEquals("vin", uri.uAuthority().domain().orElse(""));
        assertTrue(uri.uEntity().isEmpty());
        assertFalse(uri.uResource().isEmpty());
        assertTrue(uri.uResource().id().isPresent());
        assertEquals(uri.uResource().id().get(), (short)2);
    }

    @Test
    @DisplayName("Test deserialize remote string without scheme")
    public void test_deserialize_remote_string_without_scheme() {
        final UUri uri = ShortUriSerializer.instance().deserialize("//vcu.vin/2/1/2");
        assertTrue(uri.isEmpty());
    }

    @Test
    @DisplayName("Test deserialize local string without scheme")
    public void test_deserialize_local_string_without_scheme() {
        final UUri uri = ShortUriSerializer.instance().deserialize("/2/1/2");
        assertTrue(uri.isEmpty());
    }

    @Test
    @DisplayName("Test deserialize remote string with UEntity id but missing UEntity version")
    public void test_deserialize_remote_string_with_uentityid_without_version() {
        final UUri uri = ShortUriSerializer.instance().deserialize("s://vcu.vin/2");
        assertFalse(uri.isEmpty());
        assertTrue(uri.uAuthority().isMarkedRemote());
        assertEquals("vcu", uri.uAuthority().device().orElse(""));
        assertEquals("vin", uri.uAuthority().domain().orElse(""));
        assertFalse(uri.uEntity().isEmpty());
        assertEquals((short)2, uri.uEntity().id().orElse((short)0));
        assertTrue(uri.uResource().isEmpty());
    }

    @Test
    @DisplayName("Test deserialize string with invalid uEntity_id")
    public void test_deserialize_string_with_invalid_uentity_id() {
        final UUri uri = ShortUriSerializer.instance().deserialize("s://vcu.vin/abc/1/2");
        assertTrue(uri.isEmpty());
    }

    @Test
    @DisplayName("Test deserialize string with invalid uEntity_version")
    public void test_deserialize_string_with_invalid_uentity_version() {
        final UUri uri = ShortUriSerializer.instance().deserialize("s://vcu.vin/2/abc/2");
        assertTrue(uri.isEmpty());
    }

    @Test
    @DisplayName("Test deserialize string with invalid uResource_id")
    public void test_deserialize_string_with_invalid_uresource_id() {
        final UUri uri = ShortUriSerializer.instance().deserialize("s://vcu.vin/2/1/abc");
        assertTrue(uri.isEmpty());
    }

}
