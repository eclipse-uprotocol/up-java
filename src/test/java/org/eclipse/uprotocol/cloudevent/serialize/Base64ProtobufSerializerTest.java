/*
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
    public void test_deserialize_bytes_to_string() {

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
    public void test_deserialize_bytes_to_string_when_bytes_is_null() {

        String payload = Base64ProtobufSerializer.deserialize(null);
        assertEquals("", payload);
    }

    @Test
    @DisplayName("Test deserialize a byte[] to a String when byte[] is empty")
    public void test_deserialize_bytes_to_string_when_bytes_is_empty() {

        String payload = Base64ProtobufSerializer.deserialize(new byte[0]);
        assertEquals("", payload);
    }

    @Test
    @DisplayName("Test serialize a base64 String to bytes")
    public void test_serialize_string_into_bytes() {

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
    public void test_serialize_string_into_bytes_when_string_is_null() {

        final byte[] bytes = Base64ProtobufSerializer.serialize(null);
        byte[] ceBytes = new byte[0];

        assertArrayEquals(ceBytes, bytes);
    }

    @Test
    @DisplayName("Test serialize a base64 String to bytes when string is empty")
    public void test_serialize_string_into_bytes_when_string_is_empty() {

        final byte[] bytes = Base64ProtobufSerializer.serialize("");
        byte[] ceBytes = new byte[0];

        assertArrayEquals(ceBytes, bytes);
    }

}