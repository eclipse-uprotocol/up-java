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

import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;

import org.eclipse.uprotocol.cloudevent.datamodel.UCloudEventAttributes;
import org.eclipse.uprotocol.cloudevent.factory.CloudEventFactory;
import org.eclipse.uprotocol.cloudevent.factory.UCloudEvent;

import org.eclipse.uprotocol.v1.UMessageType;
import org.eclipse.uprotocol.v1.UPriority;


class CloudEventToJsonSerializerTest {

    private final CloudEventSerializer serializer = new CloudEventToJsonSerializer();

    private final String protoContentType = CloudEventFactory.PROTOBUF_CONTENT_TYPE;

    @Test
    @DisplayName("Test serialize a CloudEvent to JSON")
    public void testSerializeCloudEventToJson() {

        // fake payload
        final Any protoPayload = buildProtoPayloadForTest();

        // cloudevent
        CloudEventBuilder cloudEventBuilder = CloudEventBuilder.v1()
                .withId("hello")
                .withType("pub.v1")
                .withSource(URI.create("/body.access/1/door.front_left"))
                .withDataContentType(protoContentType)
                .withDataSchema(URI.create(protoPayload.getTypeUrl()))
                .withData(protoPayload.toByteArray())
                .withExtension("ttl", 3)
                .withExtension("priority", "CS1");
        CloudEvent cloudEvent = cloudEventBuilder.build();

        final byte[] bytes = serializer.serialize(cloudEvent);
        final String jsonString = new String(bytes, StandardCharsets.UTF_8);
        final String expected = """
                {"specversion":"1.0",\
                "id":"hello",\
                "source":"/body.access/1/door.front_left",\
                "type":"pub.v1",\
                "datacontenttype":"application/x-protobuf",\
                "dataschema":"type.googleapis.com/io.cloudevents.v1.CloudEvent",\
                "priority":"CS1",\
                "ttl":3,\
                "data_base64":"CjB0eXBlLmdvb2dsZWFwaXMuY29tL2lvLmNsb3VkZXZlbnRzLn\
                YxLkNsb3VkRXZlbnQSPAoFaGVsbG8SEmh0dHA6Ly9leGFtcGxlLmNvbRoDMS4wIgx\
                leGFtcGxlLmRlbW8qCgoDdHRsEgMaATNCAA=="\
                }""";
        assertEquals(expected, jsonString);
    }

    @Test
    @DisplayName("Test serialize and deserialize a CloudEvent to JSON")
    public void testSerializeAndDeserializeCloudEventToJson() {

        // fake payload
        final Any protoPayload = buildProtoPayloadForTest();

        // cloudevent
        CloudEventBuilder cloudEventBuilder = CloudEventBuilder.v1()
                .withId("hello")
                .withType("pub.v1")
                .withSource(URI.create("/body.access/1/door.front_left"))
                .withDataContentType(protoContentType)
                .withDataSchema(URI.create(protoPayload.getTypeUrl()))
                .withData(protoPayload.toByteArray())
                .withExtension("ttl", 3)
                .withExtension("priority", "CS1");
        CloudEvent cloudEvent = cloudEventBuilder.build();

        final byte[] bytes = serializer.serialize(cloudEvent);
        final CloudEvent deserialize = serializer.deserialize(bytes);
        assertEquals(cloudEvent, deserialize);
    }

    @Test
    @DisplayName("Test serialize 2 different cloud events are not the same serialized elements")
    public void testSerializeTwoDifferentCloudEventAreNotTheSame() {

        // fake payload
        final Any protoPayload = buildProtoPayloadForTest();

        // cloudevent
        CloudEventBuilder cloudEventBuilder = CloudEventBuilder.v1()
                .withId("hello")
                .withType("pub.v1")
                .withSource(URI.create("/body.access/1/door.front_left"))
                .withDataContentType(protoContentType)
                .withDataSchema(URI.create(protoPayload.getTypeUrl()))
                .withData(protoPayload.toByteArray())
                .withExtension("ttl", 3)
                .withExtension("priority", "CS1");
        CloudEvent cloudEvent = cloudEventBuilder.build();

        // another cloudevent
        CloudEvent anotherCloudEvent = cloudEventBuilder
                .withType("file.v1")
                .build();

        final byte[] bytesCloudEvent = serializer.serialize(cloudEvent);
        final byte[] bytesAnotherCloudEvent = serializer.serialize(anotherCloudEvent);
        assertNotEquals(bytesCloudEvent, bytesAnotherCloudEvent);
    }

    @Test
    @DisplayName("Test serialize 2 equal cloud events are the same serialized elements")
    public void testSerializeTwoSameCloudEventAreTheSame() {

        // fake payload
        final Any protoPayload = buildProtoPayloadForTest();

        // cloudevent
        CloudEventBuilder cloudEventBuilder = CloudEventBuilder.v1()
                .withId("hello")
                .withType("pub.v1")
                .withSource(URI.create("/body.access/1/door.front_left"))
                .withDataContentType(protoContentType)
                .withDataSchema(URI.create(protoPayload.getTypeUrl()))
                .withData(protoPayload.toByteArray())
                .withExtension("ttl", 3)
                .withExtension("priority", "CS1");
        CloudEvent cloudEvent = cloudEventBuilder.build();

        // another cloudevent
        CloudEvent anotherCloudEvent = cloudEventBuilder.build();

        final byte[] bytesCloudEvent = serializer.serialize(cloudEvent);
        final byte[] bytesAnotherCloudEvent = serializer.serialize(anotherCloudEvent);
        assertArrayEquals(bytesCloudEvent, bytesAnotherCloudEvent);
    }

    @Test
    @DisplayName("test double serialization Protobuf when creating CloudEvent with factory methods")
    public void testDoubleSerializationProtobufWhenCreatingCloudEventWithFactoryMethods()
            throws InvalidProtocolBufferException {

        final CloudEventSerializer serializer = CloudEventSerializers.JSON.serializer();

        String source = "/body.access//door.front_left#Door";

        // fake payload
        final Any protoPayload = buildProtoPayloadForTest1();

        // additional attributes
        final UCloudEventAttributes uCloudEventAttributes = new UCloudEventAttributes.UCloudEventAttributesBuilder()
                .withHash("somehash")
                .withPriority(UPriority.UPRIORITY_CS1)
                .withTtl(3)
                .withToken("someOAuthToken")
                .build();

        // build the cloud event
        final CloudEventBuilder cloudEventBuilder = CloudEventFactory.buildBaseCloudEvent("testme", source,
                protoPayload, uCloudEventAttributes);
        cloudEventBuilder.withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH));

        final CloudEvent cloudEvent1 = cloudEventBuilder.build();

        final byte[] bytes1 = serializer.serialize(cloudEvent1);

        final CloudEvent cloudEvent2 = serializer.deserialize(bytes1);

        assertEquals(cloudEvent2, cloudEvent1);

        final byte[] bytes2 = serializer.serialize(cloudEvent2);

        assertArrayEquals(bytes1, bytes2);

        final CloudEvent cloudEvent3 = serializer.deserialize(bytes2);
        final Any cloudEvent3Payload = UCloudEvent.getPayload(cloudEvent3);

        Class<? extends Message> clazz = io.cloudevents.v1.proto.CloudEvent.class;
        assertEquals(cloudEvent3Payload.unpack(clazz), protoPayload.unpack(clazz));

        assertEquals(cloudEvent2, cloudEvent3);
        assertEquals(cloudEvent1, cloudEvent3);
    }

    @Test
    @DisplayName("test double serialization Json")
    public void testDoubleSerializationJson() throws InvalidProtocolBufferException {

        final CloudEventSerializer serializer = CloudEventSerializers.JSON.serializer();

        CloudEventBuilder builder = buildCloudEventForTest();
        Any cloudEventProto = buildProtoPayloadForTest1();

        builder.withDataContentType(protoContentType);
        builder.withData(cloudEventProto.toByteArray());
        builder.withDataSchema(URI.create(cloudEventProto.getTypeUrl()));

        CloudEvent cloudEvent1 = builder.build();

        final byte[] bytes1 = serializer.serialize(cloudEvent1);

        final CloudEvent cloudEvent2 = serializer.deserialize(bytes1);

        assertEquals(cloudEvent2, cloudEvent1);

        final byte[] bytes2 = serializer.serialize(cloudEvent2);

        assertArrayEquals(bytes1, bytes2);

        final CloudEvent cloudEvent3 = serializer.deserialize(bytes2);
        final Any cloudEvent3Payload = UCloudEvent.getPayload(cloudEvent3);

        Class<? extends Message> clazz = io.cloudevents.v1.proto.CloudEvent.class;
        assertEquals(cloudEvent3Payload.unpack(clazz), cloudEventProto.unpack(clazz));

        assertEquals(cloudEvent2, cloudEvent3);
        assertEquals(cloudEvent1, cloudEvent3);
    }

    private CloudEventBuilder buildCloudEventForTest() {
        return CloudEventBuilder.v1()
                .withId("hello")
                .withType("pub.v1")
                .withSource(URI.create("//VCU.VIN/body.access"));
    }

    private Any buildProtoPayloadForTest1() {
        io.cloudevents.v1.proto.CloudEvent cloudEventProto = io.cloudevents.v1.proto.CloudEvent.newBuilder()
                .setSpecVersion("1.0")
                .setId("hello")
                .setSource("//VCU.VIN/body.access")
                .setType("pub.v1")
                .setProtoData(Any.newBuilder().build())
                .build();
        return Any.pack(cloudEventProto);
    }

    private Any buildProtoPayloadForTest() {
        io.cloudevents.v1.proto.CloudEvent cloudEventProto = io.cloudevents.v1.proto.CloudEvent.newBuilder()
                .setSpecVersion("1.0")
                .setId("hello")
                .setSource("http://example.com")
                .setType("example.demo")
                .setProtoData(Any.newBuilder().build())
                .putAttributes("ttl", io.cloudevents.v1.proto.CloudEvent.CloudEventAttributeValue.newBuilder()
                        .setCeString("3").build())
                .build();
        return Any.pack(cloudEventProto);
    }
}
