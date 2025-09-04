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
import java.util.stream.Stream;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.uprotocol.v1.UMessage;
import org.eclipse.uprotocol.v1.UPayloadFormat;
import org.eclipse.uprotocol.v1.UUri;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import com.google.protobuf.StringValue;


public class UPayloadTest {
    
    @Test
    @DisplayName("Test isEmpty() with null UPayload")
    public void testBuildingAUpayloadCallingPackPassingNull() {
        assertTrue(UPayload.isEmpty(UPayload.pack(null)));
        assertTrue(UPayload.isEmpty(UPayload.packToAny(null)));
    }


    @Test
    @DisplayName("Test iEmpty() when we build a valid UPayload that data is empty but format is not")
    public void testBuildingAUpayloadCallingPack() {
        UPayload payload = UPayload.pack(UUri.newBuilder().build());
        assertFalse(UPayload.isEmpty(payload));
    }


    @Test
    @DisplayName("Test iEmpty() when we build a valid UPayload where both data and format are not empty")
    public void testBuildingAUpayloadCallingPacktoany() {
        UPayload payload = UPayload.packToAny(UUri.newBuilder().setAuthorityName("Hartley").build());
        assertFalse(UPayload.isEmpty(payload));
    }


    @Test
    @DisplayName("Test iEmpty() when we pass null")
    public void testBuildingAUpayloadCallingPackWithNull() {
        assertTrue(UPayload.isEmpty(null));
    }


    @Test
    @DisplayName("Test unpack() passing null")
    public void testUnpackingAUpayloadCallingUnpackWithNull() {
        assertFalse(UPayload.unpack((UMessage) null, UUri.class).isPresent());
        assertFalse(UPayload.unpack((UPayload) null, UUri.class).isPresent());
        assertFalse(UPayload.unpack(UPayload.pack(null), UUri.class).isPresent());
    }


    @Test
    @DisplayName("Test unpack() passing null ByteString")
    public void testUnpackPassingANullBytestring() {
        assertFalse(UPayload.unpack(null, UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF, UUri.class).isPresent());
    }


    @Test
    @DisplayName("Test unpack() passing google.protobuf.Any packed UPayload")
    public void testUnpackPassingAGoogleProtobufAnyPackedUpayload() {
        UUri uri = UUri.newBuilder().setAuthorityName("Hartley").build();
        UPayload payload = UPayload.packToAny(uri);
        Optional<UUri> unpacked = UPayload.unpack(payload, UUri.class);
        assertTrue(unpacked.isPresent());
        assertEquals(uri, unpacked.get());
    }


    @Test
    @DisplayName("Test unpack() passing an unsupported format in UPayload")
    public void testUnpackPassingAnUnsupportedFormatInUpayload() {
        UUri uri = UUri.newBuilder().setAuthorityName("Hartley").build();
        UPayload payload = UPayload.pack(uri.toByteString(), UPayloadFormat.UPAYLOAD_FORMAT_JSON);
        Optional<UUri> unpacked = UPayload.unpack(payload, UUri.class);
        assertFalse(unpacked.isPresent());
        assertEquals(unpacked, Optional.empty());
    }


    @Test
    @DisplayName("Test unpack() to unpack a message of the wrong type")
    public void testUnpackToUnpackAMessageOfTheWrongType() {
        UUri uri = UUri.newBuilder().setAuthorityName("Hartley").build();
        Optional<UMessage> unpacked = UPayload.unpack(
            uri.toByteString(), UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF, UMessage.class);
        assertFalse(unpacked.isPresent());
        assertEquals(unpacked, Optional.empty());
    }

    static Stream<Arguments> unpackOrDefaultInstanceArgsProvider() {
        var stringValue = StringValue.of("hello");
        var protobuf = stringValue.toByteString();

        // Claims to have a string of length 20 but only provides 4 bytes
        var invalidStringValueProtobuf = ByteString.fromHex("0A1441424344");

        return Stream.of(
            // ByteString, UPayloadFormat, Class<? extends Message>, Exception
            Arguments.of(null, null, null, NullPointerException.class),
            Arguments.of(null, UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF, StringValue.class, NullPointerException.class),
            Arguments.of(protobuf, null, StringValue.class, NullPointerException.class),
            Arguments.of(protobuf, UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF, null, NullPointerException.class),
            Arguments.of(protobuf, UPayloadFormat.UPAYLOAD_FORMAT_JSON, StringValue.class, UStatusException.class),
            Arguments.of(protobuf, UPayloadFormat.UPAYLOAD_FORMAT_RAW, StringValue.class, UStatusException.class),
            Arguments.of(protobuf, UPayloadFormat.UPAYLOAD_FORMAT_SHM, StringValue.class, UStatusException.class),
            Arguments.of(protobuf, UPayloadFormat.UPAYLOAD_FORMAT_SOMEIP, StringValue.class, UStatusException.class),
            Arguments.of(
                protobuf,
                UPayloadFormat.UPAYLOAD_FORMAT_SOMEIP_TLV,
                StringValue.class,
                UStatusException.class),
            Arguments.of(ByteString.EMPTY, UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF, StringValue.class, null),
            Arguments.of(protobuf, UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF, StringValue.class, null),
            // unpacking a protobuf that does not match the expected type fails
            Arguments.of(
                invalidStringValueProtobuf,
                UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF,
                StringValue.class,
                UStatusException.class),
            // unpacking a protobuf to a different type succeeds because of missing type information
            // from the protobuf
            Arguments.of(protobuf, UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF, UUri.class, null),
            // unpacking a protobuf to a different type fails because of the type information
            // contained in the Any wrapper
            Arguments.of(
                Any.pack(stringValue).toByteString(),
                UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF_WRAPPED_IN_ANY,
                UUri.class,
                UStatusException.class),
            Arguments.of(
                Any.pack(stringValue).toByteString(),
                UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF_WRAPPED_IN_ANY,
                StringValue.class,
                null)
        );
    }

    @ParameterizedTest(name = "Test unpackOrDefaultInstance: {index} => {arguments}")
    @MethodSource("unpackOrDefaultInstanceArgsProvider")
    void testUnpackOrDefaultInstanceSucceedsForSimpleProtobuf(
            ByteString protobuf,
            UPayloadFormat format,
            Class<? extends Message> expectedClass,
            Class<? extends Exception> expectedException) {
        if (expectedException != null) {
            assertThrows(expectedException, () -> {
                UPayload.unpackOrDefaultInstance(protobuf, format, expectedClass);
            });
        } else {
            var unpacked = UPayload.unpackOrDefaultInstance(protobuf, format, expectedClass);
            assertInstanceOf(expectedClass, unpacked);
        }
    }

    @Test
    @DisplayName("Test equals when they are equal")
    public void testEqualsWhenTheyAreEqual() {
        UUri uri = UUri.newBuilder().setAuthorityName("Hartley").build();
        UPayload payload1 = UPayload.packToAny(uri);
        UPayload payload2 = UPayload.packToAny(uri);
        assertEquals(payload1, payload2);
    }


    @Test
    @DisplayName("Test equals when they are not equal")
    public void testEqualsWhenTheyAreNotEqual() {
        UUri uri1 = UUri.newBuilder().setAuthorityName("Hartley").build();
        UUri uri2 = UUri.newBuilder().setAuthorityName("Hartley").build();
        UPayload payload1 = UPayload.packToAny(uri1);
        UPayload payload2 = UPayload.pack(uri2);
        assertFalse(payload1.equals(payload2));
    }


    @Test
    @DisplayName("Test equals when object is null")
    public void testEqualsWhenObjectIsNull() {
        UUri uri = UUri.newBuilder().setAuthorityName("Hartley").build();
        UPayload payload = UPayload.packToAny(uri);
        assertNotNull(payload);
    }


    @SuppressWarnings("unlikely-arg-type")
    @Test
    @DisplayName("Test equals when object is not an instance of UPayload")
    public void testEqualsWhenObjectIsNotAnInstanceOfUpayload() {
        UUri uri = UUri.newBuilder().setAuthorityName("Hartley").build();
        UPayload payload = UPayload.packToAny(uri);
        assertFalse(payload.equals(uri));
    }


    @Test
    @DisplayName("Test equals when it is the same object")
    public void testEqualsWhenItIsTheSameObject() {
        UUri uri = UUri.newBuilder().setAuthorityName("Hartley").build();
        UPayload payload = UPayload.packToAny(uri);
        assertTrue(payload.equals(payload));
    }

    @Test
    @DisplayName("Test equals when the data is the same but the format is not")
    public void testEqualsWhenTheDataIsTheSameButTheFormatIsNot() {
        UUri uri = UUri.newBuilder().setAuthorityName("Hartley").build();
        UPayload payload1 = UPayload.pack(uri.toByteString(), UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF);
        UPayload payload2 = UPayload.pack(uri.toByteString(), UPayloadFormat.UPAYLOAD_FORMAT_JSON);
        assertFalse(payload1.equals(payload2));
    }
    

    @Test
    @DisplayName("Test hashCode")
    public void testHashcode() {
        UUri uri = UUri.newBuilder().setAuthorityName("Hartley").build();
        UPayload payload = UPayload.packToAny(uri);
        assertEquals(payload.hashCode(), payload.hashCode());
    }


    @Test
    @DisplayName("Test hashCode when they are not the same objects")
    public void testHashcodeWhenTheyAreNotTheSameObjects() {
        UUri uri1 = UUri.newBuilder().setAuthorityName("Hartley").build();
        UPayload payload1 = UPayload.packToAny(uri1);
        assertFalse(payload1.hashCode() == uri1.hashCode());
    }


    @Test
    @DisplayName("Test unpack passing a valid UMessage")
    public void testUnpackPassingAValidUmessage() {
        UUri uri = UUri.newBuilder().setAuthorityName("Hartley").build();
        UPayload payload = UPayload.packToAny(uri);
        UMessage message = UMessage.newBuilder().setPayload(payload.data()).build();
        Optional<UUri> unpacked = UPayload.unpack(message, UUri.class);
        assertTrue(unpacked.isPresent());
        assertEquals(uri, unpacked.get());
    }
}
