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
package org.eclipse.uprotocol.cloudevent.serialize;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

class Base64ProtobufSerializerTest {

    @Test
    @DisplayName("Test deserialize a byte[] to a String")
    public void testDeserializeBytesToString() {

        // build the payload as just another cloud event packed into an Any
        CloudEvent datapayload = CloudEventBuilder.v1()
                .withId("hello")
                .withType("example.vertx")
                .withSource(URI.create("http://localhost"))
                .build();

        final byte[] bytes = CloudEventSerializers.PROTOBUF.serializer().serialize(datapayload);

        String payload = Base64ProtobufSerializer.deserialize(bytes);
        assertEquals("CgVoZWxsbxIQaHR0cDovL2xvY2FsaG9zdBoDMS4wIg1leGFtcGxlLnZlcnR4", payload);
    }

    @Test
    @DisplayName("Test deserialize a byte[] to a String when byte[] is null")
    public void testDeserializeBytesToStringWhenBytesIsNull() {

        String payload = Base64ProtobufSerializer.deserialize(null);
        assertEquals("", payload);
    }

    @Test
    @DisplayName("Test deserialize a byte[] to a String when byte[] is empty")
    public void testDeserializeBytesToStringWhenBytesIsEmpty() {

        String payload = Base64ProtobufSerializer.deserialize(new byte[0]);
        assertEquals("", payload);
    }

    @Test
    @DisplayName("Test serialize a base64 String to bytes")
    public void testSerializeStringIntoBytes() {

        String base64String = "CgVoZWxsbxIQaHR0cDovL2xvY2FsaG9zdBoDMS4wIg1leGFtcGxlLnZlcnR4";
        final byte[] bytes = Base64ProtobufSerializer.serialize(base64String);

        CloudEvent datapayload = CloudEventBuilder.v1()
                .withId("hello")
                .withType("example.vertx")
                .withSource(URI.create("http://localhost"))
                .build();

        final byte[] ceBytes = CloudEventSerializers.PROTOBUF.serializer().serialize(datapayload);

        assertArrayEquals(ceBytes, bytes);
    }

    @Test
    @DisplayName("Test serialize a base64 String to bytes when string is null")
    public void testSerializeStringIntoBytesWhenStringIsNull() {

        final byte[] bytes = Base64ProtobufSerializer.serialize(null);
        byte[] ceBytes = new byte[0];

        assertArrayEquals(ceBytes, bytes);
    }

    @Test
    @DisplayName("Test serialize a base64 String to bytes when string is empty")
    public void testSerializeStringIntoBytesWhenStringIsEmpty() {

        final byte[] bytes = Base64ProtobufSerializer.serialize("");
        byte[] ceBytes = new byte[0];

        assertArrayEquals(ceBytes, bytes);
    }
}
