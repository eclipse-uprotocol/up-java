/**
 * SPDX-FileCopyrightText: 2024 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.eclipse.uprotocol.communication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.uprotocol.v1.UMessage;
import org.eclipse.uprotocol.v1.UPayloadFormat;
import org.eclipse.uprotocol.v1.UUri;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


public class UPayloadTest {
    
    @Test
    @DisplayName("Test isEmpty() with null UPayload")
    public void test_building_a_UPayload_calling_pack_passing_null() {
        assertTrue(UPayload.isEmpty(UPayload.pack(null)));
        assertTrue(UPayload.isEmpty(UPayload.packToAny(null)));
        assertTrue(UPayload.isEmpty(UPayload.pack(null, null)));
    }

    @Test
    @DisplayName("Test iEmpty() when we build a valid UPayload that data is empty but format is not")
    public void test_building_a_UPayload_calling_pack() {
        UPayload payload = UPayload.pack(UUri.newBuilder().build());
        assertFalse(UPayload.isEmpty(payload));
    }

    @Test
    @DisplayName("Test iEmpty() when we build a valid UPayload where both data and format are not empty")
    public void test_building_a_UPayload_calling_packToAny() {
        UPayload payload = UPayload.packToAny(UUri.newBuilder().setAuthorityName("Hartley").build());
        assertFalse(UPayload.isEmpty(payload));
    }

    @Test
    @DisplayName("Test iEmpty() when we pass null")
    public void test_building_a_UPayload_calling_pack_with_null() {
        assertTrue(UPayload.isEmpty(null));
    }

    @Test
    @DisplayName("Test unpack() passing null")
    public void test_unpacking_a_UPayload_calling_unpack_with_null() {
        assertFalse(UPayload.unpack(null, UUri.class).isPresent());
        assertFalse(UPayload.unpack(UPayload.pack(null), UUri.class).isPresent());
    }


    @Test
    @DisplayName("Test unpack() passing null ByteString")
    public void test_unpack_passing_a_null_bytestring() {
        assertFalse(UPayload.unpack(null, UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF, UUri.class).isPresent());
    }

    @Test
    @DisplayName("Test unpack() passing google.protobuf.Any packed UPayload")
    public void test_unpack_passing_a_google_protobuf_any_packed_UPayload() {
        UUri uri = UUri.newBuilder().setAuthorityName("Hartley").build();
        UPayload payload = UPayload.packToAny(uri);
        Optional<UUri> unpacked = UPayload.unpack(payload, UUri.class);
        assertTrue(unpacked.isPresent());
        assertEquals(uri, unpacked.get());
    }

    @Test
    @DisplayName("Test unpack() passing an unsupported format in UPayload")
    public void test_unpack_passing_an_unsupported_format_in_UPayload() {
        UUri uri = UUri.newBuilder().setAuthorityName("Hartley").build();
        UPayload payload = UPayload.pack(uri.toByteString(), UPayloadFormat.UPAYLOAD_FORMAT_JSON);
        Optional<UUri> unpacked = UPayload.unpack(payload, UUri.class);
        assertFalse(unpacked.isPresent());
        assertEquals(unpacked, Optional.empty());
    }

    @Test
    @DisplayName("Test unpack() to unpack a message of the wrong type")
    public void test_unpack_to_unpack_a_message_of_the_wrong_type() {
        UUri uri = UUri.newBuilder().setAuthorityName("Hartley").build();
        Optional<UMessage> unpacked = UPayload.unpack(
            uri.toByteString(), UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF, UMessage.class);
        assertFalse(unpacked.isPresent());
        assertEquals(unpacked, Optional.empty());
    }

    @Test
    @DisplayName("Test equals when they are equal")
    public void test_equals_when_they_are_equal() {
        UUri uri = UUri.newBuilder().setAuthorityName("Hartley").build();
        UPayload payload1 = UPayload.packToAny(uri);
        UPayload payload2 = UPayload.packToAny(uri);
        assertEquals(payload1, payload2);
    }

    @Test
    @DisplayName("Test equals when they are not equal")
    public void test_equals_when_they_are_not_equal() {
        UUri uri1 = UUri.newBuilder().setAuthorityName("Hartley").build();
        UUri uri2 = UUri.newBuilder().setAuthorityName("Hartley").build();
        UPayload payload1 = UPayload.packToAny(uri1);
        UPayload payload2 = UPayload.pack(uri2);
        assertFalse(payload1.equals(payload2));
    }

    @Test
    @DisplayName("Test equals when object is null")
    public void test_equals_when_object_is_null() {
        UUri uri = UUri.newBuilder().setAuthorityName("Hartley").build();
        UPayload payload = UPayload.packToAny(uri);
        assertFalse(payload.equals(null));
    }

    @Test
    @DisplayName("Test equals when object is not an instance of UPayload")
    public void test_equals_when_object_is_not_an_instance_of_UPayload() {
        UUri uri = UUri.newBuilder().setAuthorityName("Hartley").build();
        UPayload payload = UPayload.packToAny(uri);
        assertFalse(payload.equals(uri));
    }

    @Test
    @DisplayName("Test equals when it is the same object")
    public void test_equals_when_it_is_the_same_object() {
        UUri uri = UUri.newBuilder().setAuthorityName("Hartley").build();
        UPayload payload = UPayload.packToAny(uri);
        assertTrue(payload.equals(payload));
    }

    @Test
    @DisplayName("Test equals when the data is the same but the format is not")
    public void test_equals_when_the_data_is_the_same_but_the_format_is_not() {
        UUri uri = UUri.newBuilder().setAuthorityName("Hartley").build();
        UPayload payload1 = UPayload.pack(uri.toByteString(), UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF);
        UPayload payload2 = UPayload.pack(uri.toByteString(), UPayloadFormat.UPAYLOAD_FORMAT_JSON);
        assertFalse(payload1.equals(payload2));
    }
    
    @Test
    @DisplayName("Test hashCode")
    public void test_hashCode() {
        UUri uri = UUri.newBuilder().setAuthorityName("Hartley").build();
        UPayload payload = UPayload.packToAny(uri);
        assertEquals(payload.hashCode(), payload.hashCode());
    }

    @Test
    @DisplayName("Test hashCode when they are not the same objects")
    public void test_hashCode_when_they_are_not_the_same_objects() {
        UUri uri1 = UUri.newBuilder().setAuthorityName("Hartley").build();
        UPayload payload1 = UPayload.packToAny(uri1);
        assertFalse(payload1.hashCode() == uri1.hashCode());
    }

    @Test
    @DisplayName("Test toString for an empty payload")
    public void test_toString_for_an_empty_payload() {
        UPayload payload = UPayload.EMPTY;
        assertEquals("UPayload{data=, format=UPAYLOAD_FORMAT_UNSPECIFIED}",
            payload.toString());
    }

}
