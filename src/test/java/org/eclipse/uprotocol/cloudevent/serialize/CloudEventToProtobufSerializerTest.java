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

package org.eclipse.uprotocol.cloudevent.serialize;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import org.eclipse.uprotocol.cloudevent.datamodel.UCloudEventAttributes;
import org.eclipse.uprotocol.cloudevent.datamodel.UCloudEventType;
import org.eclipse.uprotocol.cloudevent.factory.CloudEventFactory;
import org.eclipse.uprotocol.cloudevent.factory.UCloudEvent;
import org.eclipse.uprotocol.uri.datamodel.UAuthority;
import org.eclipse.uprotocol.uri.datamodel.UEntity;
import org.eclipse.uprotocol.uri.datamodel.UResource;
import org.eclipse.uprotocol.uri.datamodel.UUri;
import org.eclipse.uprotocol.uri.serializer.LongUriSerializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CloudEventToProtobufSerializerTest {

    private final CloudEventSerializer serializer = new CloudEventToProtobufSerializer();

    private final String protoContentType = CloudEventFactory.PROTOBUF_CONTENT_TYPE;

    @Test
    @DisplayName("Test serialize and deserialize a CloudEvent to protobuf")
    public void test_serialize_and_desirialize_cloud_event_to_protobuf() {

        // build the source
        UEntity use = UEntity.longFormat("body.access");
        UUri Uri = new UUri(UAuthority.local(), use, UResource.longFormat("Door", "front_left", null));
        String source = LongUriSerializer.instance().serialize(Uri);

        // fake payload
        final Any protoPayload = buildProtoPayloadForTest();

        // configure cloud event
        final UCloudEventAttributes uCloudEventAttributes = new UCloudEventAttributes.UCloudEventAttributesBuilder()
                .withHash("somehash")
                .withPriority(UCloudEventAttributes.Priority.LOW)
                .withTtl(3)
                .build();

        final CloudEventBuilder cloudEventBuilder = CloudEventFactory.buildBaseCloudEvent("hello", source,
                protoPayload.toByteArray(), protoPayload.getTypeUrl(),
                uCloudEventAttributes);
        cloudEventBuilder.withType("pub.v1");

        final CloudEvent cloudEvent = cloudEventBuilder.build();
        final byte[] bytes = serializer.serialize(cloudEvent);

        final CloudEvent deserialize = serializer.deserialize(bytes);

        // data is not the same type, does not work -> expected data=BytesCloudEventData actual data=io.cloudevents.protobuf.ProtoDataWrapper
        //assertEquals(cloudEvent, deserialize);

        assertCloudEventsAreTheSame(cloudEvent, deserialize);
    }

    @Test
    @DisplayName("Test serialize 2 different cloud events are not the same serialized elements")
    public void test_serialize_two_different_cloud_event_are_not_the_same() {

        // fake payload
        final Any protoPayload = buildProtoPayloadForTest();

        // cloudevent
        CloudEventBuilder cloudEventBuilder = CloudEventBuilder.v1()
                .withId("hello")
                .withType("pub.v1")
                .withSource(URI.create("/body.access/1/door.front_left"))
                .withDataContentType("application/protobuf")
                .withDataSchema(URI.create(protoPayload.getTypeUrl()))
                .withData(protoPayload.toByteArray());
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
    public void test_serialize_two_same_cloud_event_are_the_same() {

        // fake payload
        final Any protoPayload = buildProtoPayloadForTest();

        // cloudevent
        CloudEventBuilder cloudEventBuilder = CloudEventBuilder.v1()
                .withId("hello")
                .withType("pub.v1")
                .withSource(URI.create("/body.access/1/door.front_left"))
                .withDataContentType("application/protobuf")
                .withDataSchema(URI.create(protoPayload.getTypeUrl()))
                .withData(protoPayload.toByteArray());
        CloudEvent cloudEvent = cloudEventBuilder.build();

        // another cloudevent
        CloudEvent anotherCloudEvent = cloudEventBuilder.build();

        final byte[] bytesCloudEvent = serializer.serialize(cloudEvent);
        final byte[] bytesAnotherCloudEvent = serializer.serialize(anotherCloudEvent);
        assertArrayEquals(bytesCloudEvent, bytesAnotherCloudEvent);
    }

    @Test
    @DisplayName("test double serialization Protobuf when creating CloudEvent with factory methods")
    public void test_double_serialization_protobuf_when_creating_cloud_event_with_factory_methods() throws InvalidProtocolBufferException {

        final CloudEventSerializer serializer = CloudEventSerializers.PROTOBUF.serializer();

        // source
        UEntity use = UEntity.longFormat("body.access");
        UUri Uri = new UUri(UAuthority.local(), use,
                UResource.longFormat("door", "front_left", "Door"));
        String source = LongUriSerializer.instance().serialize(Uri);

        // fake payload
        final Any protoPayload = buildProtoPayloadForTest1();

        // additional attributes
        final UCloudEventAttributes uCloudEventAttributes = new UCloudEventAttributes.UCloudEventAttributesBuilder()
                .withHash("somehash")
                .withPriority(UCloudEventAttributes.Priority.STANDARD)
                .withTtl(3)
                .withToken("someOAuthToken")
                .build();

        // build the cloud event
        final CloudEventBuilder cloudEventBuilder = CloudEventFactory.buildBaseCloudEvent("testme", source,
                protoPayload.toByteArray(), protoPayload.getTypeUrl(),
                uCloudEventAttributes);
        cloudEventBuilder.withType(UCloudEventType.PUBLISH.type());

        final CloudEvent cloudEvent1 = cloudEventBuilder.build();

        final byte[] bytes1 = serializer.serialize(cloudEvent1);

        final CloudEvent cloudEvent2 = serializer.deserialize(bytes1);

        assertCloudEventsAreTheSame(cloudEvent2, cloudEvent1);

        final byte[] bytes2 = serializer.serialize(cloudEvent2);

        assertArrayEquals(bytes1, bytes2);

        final CloudEvent cloudEvent3 = serializer.deserialize(bytes2);
        final Any cloudEvent3Payload = UCloudEvent.getPayload(cloudEvent3);

        Class<? extends Message> clazz = io.cloudevents.v1.proto.CloudEvent.class;
        assertEquals(cloudEvent3Payload.unpack(clazz), protoPayload.unpack(clazz));

        assertEquals(cloudEvent2, cloudEvent3);
        assertCloudEventsAreTheSame(cloudEvent1, cloudEvent3);
    }

    @Test
    @DisplayName("test double serialization Protobuf")
    public void test_double_serialization_protobuf() throws InvalidProtocolBufferException {

        final CloudEventSerializer serializer = CloudEventSerializers.PROTOBUF.serializer();

        CloudEventBuilder builder = buildCloudEventForTest();
        Any cloudEventProto = buildProtoPayloadForTest1();

        builder.withDataContentType(protoContentType);
        builder.withData(cloudEventProto.toByteArray());
        builder.withDataSchema(URI.create(cloudEventProto.getTypeUrl()));

        CloudEvent cloudEvent1 = builder.build();

        final byte[] bytes1 = serializer.serialize(cloudEvent1);

        final CloudEvent cloudEvent2 = serializer.deserialize(bytes1);

        assertCloudEventsAreTheSame(cloudEvent2, cloudEvent1);

        final byte[] bytes2 = serializer.serialize(cloudEvent2);

        assertArrayEquals(bytes1, bytes2);

        final CloudEvent cloudEvent3 = serializer.deserialize(bytes2);
        final Any cloudEvent3Payload = UCloudEvent.getPayload(cloudEvent3);

        Class<? extends Message> clazz = io.cloudevents.v1.proto.CloudEvent.class;
        assertEquals(cloudEvent3Payload.unpack(clazz), cloudEventProto.unpack(clazz));

        assertEquals(cloudEvent2, cloudEvent3);
        assertCloudEventsAreTheSame(cloudEvent1, cloudEvent3);
    }

    @Test
    @DisplayName("test double serialization proto to Json")
    public void test_double_serialization_proto_to_json() {

        final CloudEventSerializer protoSerializer = CloudEventSerializers.PROTOBUF.serializer();
        final CloudEventSerializer jsonSerializer = CloudEventSerializers.JSON.serializer();


        CloudEventBuilder builder = buildCloudEventForTest();
        Any cloudEventProto = buildProtoPayloadForTest1();

        builder.withDataContentType(protoContentType);
        builder.withData(cloudEventProto.toByteArray());
        builder.withDataSchema(URI.create(cloudEventProto.getTypeUrl()));

        CloudEvent cloudEvent1 = builder.build();

        final byte[] bytes1 = protoSerializer.serialize(cloudEvent1);

        final CloudEvent cloudEvent2 = protoSerializer.deserialize(bytes1);

        assertCloudEventsAreTheSame(cloudEvent2, cloudEvent1);

        final byte[] bytes2 = protoSerializer.serialize(cloudEvent2);

        assertArrayEquals(bytes1, bytes2);

        final byte[] bytes3 = jsonSerializer.serialize(cloudEvent2);
        final CloudEvent cloudEvent3 = jsonSerializer.deserialize(bytes3);

        assertCloudEventsAreTheSame(cloudEvent2, cloudEvent3);
        assertEquals(cloudEvent1, cloudEvent3);
    }

    @Test
    @DisplayName("test double serialization json to proto")
    public void test_double_serialization_json_to_proto() {

        final CloudEventSerializer protoSerializer = CloudEventSerializers.PROTOBUF.serializer();
        final CloudEventSerializer jsonSerializer = CloudEventSerializers.JSON.serializer();

        CloudEventBuilder builder = buildCloudEventForTest();
        Any cloudEventProto = buildProtoPayloadForTest1();

        builder.withDataContentType(protoContentType);
        builder.withData(cloudEventProto.toByteArray());
        builder.withDataSchema(URI.create(cloudEventProto.getTypeUrl()));

        CloudEvent cloudEvent1 = builder.build();

        final byte[] bytes1 = jsonSerializer.serialize(cloudEvent1);

        final CloudEvent cloudEvent2 = jsonSerializer.deserialize(bytes1);

        assertEquals(cloudEvent2, cloudEvent1);

        final byte[] bytes2 = jsonSerializer.serialize(cloudEvent2);

        assertArrayEquals(bytes1, bytes2);

        final byte[] bytes3 = protoSerializer.serialize(cloudEvent2);
        final CloudEvent cloudEvent3 = protoSerializer.deserialize(bytes3);

        assertCloudEventsAreTheSame(cloudEvent2, cloudEvent3);
        assertCloudEventsAreTheSame(cloudEvent1, cloudEvent3);
    }

    private void assertCloudEventsAreTheSame(CloudEvent cloudEvent1, CloudEvent cloudEvent2) {
        assertNotNull(cloudEvent1);
        assertNotNull(cloudEvent2);

        assertEquals(cloudEvent1.getSpecVersion().toString(), cloudEvent2.getSpecVersion().toString());
        assertEquals(cloudEvent1.getId(), cloudEvent2.getId());
        assertEquals(cloudEvent1.getSource(), cloudEvent2.getSource());
        assertEquals(cloudEvent1.getType(), cloudEvent2.getType());
        assertEquals(cloudEvent1.getDataContentType(), cloudEvent2.getDataContentType());
        assertEquals(cloudEvent1.getDataSchema(), cloudEvent2.getDataSchema());

        final Set<String> ce1ExtensionNames = cloudEvent1.getExtensionNames();
        final Set<String> ce2ExtensionNames = cloudEvent2.getExtensionNames();

        assertEquals(String.join(",", ce1ExtensionNames), String.join(",", ce2ExtensionNames));

        assertArrayEquals(Objects.requireNonNull(cloudEvent1.getData()).toBytes(),
                Objects.requireNonNull(cloudEvent2.getData()).toBytes());

        assertEquals(cloudEvent1, cloudEvent2);
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