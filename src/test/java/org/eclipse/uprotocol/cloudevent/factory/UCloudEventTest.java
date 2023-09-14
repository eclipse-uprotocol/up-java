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

package org.eclipse.uprotocol.cloudevent.factory;

import org.eclipse.uprotocol.cloudevent.datamodel.UCloudEventAttributes;
import org.eclipse.uprotocol.cloudevent.datamodel.UCloudEventType;
import org.eclipse.uprotocol.uri.datamodel.UAuthority;
import org.eclipse.uprotocol.uri.datamodel.UEntity;
import org.eclipse.uprotocol.uri.datamodel.UResource;
import org.eclipse.uprotocol.uri.datamodel.UUri;
import org.eclipse.uprotocol.uri.serializer.UriSerializer;
import org.eclipse.uprotocol.uuid.factory.UUIDFactory;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.rpc.Code;
import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventData;
import io.cloudevents.core.builder.CloudEventBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class UCloudEventTest {

    private static final String DATA_CONTENT_TYPE = CloudEventFactory.PROTOBUF_CONTENT_TYPE;

    @Test
    @DisplayName("Test extracting the source from a CloudEvent.")
    public void test_extract_source_from_cloudevent() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest();
        CloudEvent cloudEvent = builder.build();

        String source = UCloudEvent.getSource(cloudEvent);
        assertEquals("/body.access//door.front_left#Door", source);
    }

    @Test
    @DisplayName("Test extracting the sink from a CloudEvent when the sink exists.")
    public void test_extract_sink_from_cloudevent_when_sink_exists() {
        String sinkForTest = "//bo.cloud/petapp/1/rpc.response";

        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withExtension("sink", URI.create(sinkForTest));

        CloudEvent cloudEvent = builder.build();

        final Optional<String> sink = UCloudEvent.getSink(cloudEvent);
        assertTrue(sink.isPresent());
        assertEquals(sinkForTest, sink.get());
    }

    @Test
    @DisplayName("Test extracting the sink from a CloudEvent when the sink does not exist.")
    public void test_extract_sink_from_cloudevent_when_sink_does_not_exist() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest();
        CloudEvent cloudEvent = builder.build();

        final Optional<String> sink = UCloudEvent.getSink(cloudEvent);
        assertTrue(sink.isEmpty());
    }

    @Test
    @DisplayName("Test extracting the request id from a CloudEvent when the request id exists.")
    public void test_extract_requestId_from_cloudevent_when_requestId_exists() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withExtension("reqid", "someRequestId");

        CloudEvent cloudEvent = builder.build();

        final Optional<String> requestId = UCloudEvent.getRequestId(cloudEvent);
        assertTrue(requestId.isPresent());
        assertEquals("someRequestId", requestId.get());
    }

    @Test
    @DisplayName("Test extracting the request id from a CloudEvent when the request id does not exist.")
    public void test_extract_requestId_from_cloudevent_when_requestId_does_not_exist() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest();
        CloudEvent cloudEvent = builder.build();

        final Optional<String> requestId = UCloudEvent.getRequestId(cloudEvent);
        assertTrue(requestId.isEmpty());
    }

    @Test
    @DisplayName("Test extracting the request id from a CloudEvent when the request id is null.")
    public void test_extract_requestId_from_cloudevent_when_requestId_value_is_null() {
        String reqid = null;
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withExtension("reqid", reqid);
        CloudEvent cloudEvent = builder.build();

        final Optional<String> requestId = UCloudEvent.getRequestId(cloudEvent);
        assertTrue(requestId.isEmpty());
    }

    @Test
    @DisplayName("Test extracting the hash from a CloudEvent when the hash exists.")
    public void test_extract_hash_from_cloudevent_when_hash_exists() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest();

        CloudEvent cloudEvent = builder.build();

        final Optional<String> hash = UCloudEvent.getHash(cloudEvent);
        assertTrue(hash.isPresent());
        assertEquals("somehash", hash.get());
    }

    @Test
    @DisplayName("Test extracting the hash from a CloudEvent when the hash does not exist.")
    public void test_extract_hash_from_cloudevent_when_hash_does_not_exist() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withoutExtension("hash");
        CloudEvent cloudEvent = builder.build();

        final Optional<String> hash = UCloudEvent.getHash(cloudEvent);
        assertTrue(hash.isEmpty());
    }

    @Test
    @DisplayName("Test extracting the priority from a CloudEvent when the priority exists.")
    public void test_extract_priority_from_cloudevent_when_priority_exists() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest();

        CloudEvent cloudEvent = builder.build();

        final Optional<String> priority = UCloudEvent.getPriority(cloudEvent);
        assertTrue(priority.isPresent());
        Assertions.assertEquals(UCloudEventAttributes.Priority.STANDARD.qosString(), priority.get());
    }

    @Test
    @DisplayName("Test extracting the priority from a CloudEvent when the priority does not exist.")
    public void test_extract_priority_from_cloudevent_when_priority_does_not_exist() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withoutExtension("priority");
        CloudEvent cloudEvent = builder.build();

        final Optional<String> priority = UCloudEvent.getPriority(cloudEvent);
        assertTrue(priority.isEmpty());
    }

    @Test
    @DisplayName("Test extracting the ttl from a CloudEvent when the ttl exists.")
    public void test_extract_ttl_from_cloudevent_when_ttl_exists() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest();

        CloudEvent cloudEvent = builder.build();

        final Optional<Integer> ttl = UCloudEvent.getTtl(cloudEvent);
        assertTrue(ttl.isPresent());
        assertEquals(3, ttl.get());
    }

    @Test
    @DisplayName("Test extracting the ttl from a CloudEvent when the ttl does not exist.")
    public void test_extract_ttl_from_cloudevent_when_ttl_does_not_exist() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withoutExtension("ttl");
        CloudEvent cloudEvent = builder.build();

        final Optional<Integer> ttl = UCloudEvent.getTtl(cloudEvent);
        assertTrue(ttl.isEmpty());
    }

    @Test
    @DisplayName("Test extracting the token from a CloudEvent when the token exists.")
    public void test_extract_token_from_cloudevent_when_token_exists() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest();

        CloudEvent cloudEvent = builder.build();

        final Optional<String> token = UCloudEvent.getToken(cloudEvent);
        assertTrue(token.isPresent());
        assertEquals("someOAuthToken", token.get());
    }

    @Test
    @DisplayName("Test extracting the token from a CloudEvent when the token does not exist.")
    public void test_extract_token_from_cloudevent_when_token_does_not_exist() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withoutExtension("token");
        CloudEvent cloudEvent = builder.build();

        final Optional<String> token = UCloudEvent.getToken(cloudEvent);
        assertTrue(token.isEmpty());
    }

    @Test
    @DisplayName("Test a CloudEvent has a platform communication error when the platform communication error exists.")
    public void test_cloudevent_has_platform_error_when_platform_error_exists() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withExtension("commstatus", Code.ABORTED_VALUE);

        CloudEvent cloudEvent = builder.build();

        assertTrue(UCloudEvent.hasCommunicationStatusProblem(cloudEvent));
        assertEquals(10, UCloudEvent.getCommunicationStatus(cloudEvent));
    }

    @Test
    @DisplayName("Test a CloudEvent has a platform communication error when the platform communication error does not exist.")
    public void test_cloudevent_has_platform_error_when_platform_error_does_not_exist() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest();

        CloudEvent cloudEvent = builder.build();

        assertFalse(UCloudEvent.hasCommunicationStatusProblem(cloudEvent));
        assertEquals(Code.OK_VALUE, UCloudEvent.getCommunicationStatus(cloudEvent));
    }

    @Test
    @DisplayName("Test extracting the platform communication error from a CloudEvent when the platform communication error exists but in the wrong format.")
    public void test_extract_platform_error_from_cloudevent_when_platform_error_exists_in_wrong_format() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withExtension("commstatus", "boom");

        CloudEvent cloudEvent = builder.build();

        assertFalse(UCloudEvent.hasCommunicationStatusProblem(cloudEvent));
        assertEquals(Code.OK_VALUE, UCloudEvent.getCommunicationStatus(cloudEvent));
    }

    @Test
    @DisplayName("Test extracting the platform communication error from a CloudEvent when the platform communication error exists.")
    public void test_extract_platform_error_from_cloudevent_when_platform_error_exists() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withExtension("commstatus", Code.INVALID_ARGUMENT_VALUE);

        CloudEvent cloudEvent = builder.build();

        final Integer communicationStatus = UCloudEvent.getCommunicationStatus(cloudEvent);
        assertEquals(3, communicationStatus);
    }

    @Test
    @DisplayName("Test extracting the platform communication error from a CloudEvent when the platform communication error does not exist.")
    public void test_extract_platform_error_from_cloudevent_when_platform_error_does_not_exist() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest();
        CloudEvent cloudEvent = builder.build();

        final Integer communicationStatus = UCloudEvent.getCommunicationStatus(cloudEvent);
        assertEquals(Code.OK_VALUE, communicationStatus);
    }

    @Test
    @DisplayName("Test adding a platform communication error to an existing CloudEvent.")
    public void test_adding_platform_error_to_existing_cloudevent() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest();
        CloudEvent cloudEvent = builder.build();

        assertEquals(Code.OK_VALUE, UCloudEvent.getCommunicationStatus(cloudEvent));

        CloudEvent cloudEvent1 = UCloudEvent.addCommunicationStatus(cloudEvent, Code.DEADLINE_EXCEEDED_VALUE);

        assertEquals(4, UCloudEvent.getCommunicationStatus(cloudEvent1));
        assertEquals(Code.OK_VALUE, UCloudEvent.getCommunicationStatus(cloudEvent));
    }

    @Test
    @DisplayName("Test adding an empty platform communication error to an existing CloudEvent, does nothing.")
    public void test_adding_empty_platform_error_to_existing_cloudevent() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest();
        CloudEvent cloudEvent = builder.build();

        assertEquals(Code.OK_VALUE, UCloudEvent.getCommunicationStatus(cloudEvent));

        CloudEvent cloudEvent1 = UCloudEvent.addCommunicationStatus(cloudEvent, null);

        assertEquals(Code.OK_VALUE, UCloudEvent.getCommunicationStatus(cloudEvent));

        assertEquals(cloudEvent, cloudEvent1);
    }

    @Test
    @DisplayName("Test extracting creation timestamp from the CloudEvent UUID id when the id is not a UUIDV8.")
    public void test_extract_creation_timestamp_from_cloudevent_UUID_Id_when_not_a_UUIDV8_id() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest();
        CloudEvent cloudEvent = builder.build();
        final Optional<Long> creationTimestamp = UCloudEvent.getCreationTimestamp(cloudEvent);
        assertTrue(creationTimestamp.isEmpty());
    }

    @Test
    @DisplayName("Test extracting creation timestamp from the CloudEvent UUIDV8 id when the id is valid.")
    public void test_extract_creation_timestamp_from_cloudevent_UUIDV8_Id_when_UUIDV8_id_is_valid() {
        UUID uuid = UUIDFactory.Factories.UPROTOCOL.factory().create();
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withId(uuid.toString());
        CloudEvent cloudEvent = builder.build();

        final Optional<Long> maybeCreationTimestamp = UCloudEvent.getCreationTimestamp(cloudEvent);
        assertTrue(maybeCreationTimestamp.isPresent());

        final long creationTimestamp = maybeCreationTimestamp.get();

        final OffsetDateTime now = OffsetDateTime.now();

        final Instant creationTimestampInstant = Instant.ofEpochMilli(creationTimestamp);
        final long creationTimestampInstantEpochSecond = creationTimestampInstant.getEpochSecond();
        final long nowTimeStampEpochSecond = now.toEpochSecond();

        assertEquals(creationTimestampInstantEpochSecond, nowTimeStampEpochSecond);
    }

    @Test
    @DisplayName("Test if the CloudEvent is not expired using creation date when no ttl is configured.")
    public void test_cloudevent_is_not_expired_cd_when_no_ttl_configured() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withoutExtension("ttl");
        CloudEvent cloudEvent = builder.build();
        assertFalse(UCloudEvent.isExpiredByCloudEventCreationDate(cloudEvent));
    }

    @Test
    @DisplayName("Test if the CloudEvent is not expired using creation date when configured ttl is zero.")
    public void test_cloudevent_is_not_expired_cd_when_ttl_is_zero() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withExtension("ttl", 0);
        CloudEvent cloudEvent = builder.build();
        assertFalse(UCloudEvent.isExpiredByCloudEventCreationDate(cloudEvent));
    }

    @Test
    @DisplayName("Test if the CloudEvent is not expired using creation date when configured ttl is minus one.")
    public void test_cloudevent_is_not_expired_cd_when_ttl_is_minus_one() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withExtension("ttl", -1);
        CloudEvent cloudEvent = builder.build();
        assertFalse(UCloudEvent.isExpiredByCloudEventCreationDate(cloudEvent));
    }

    @Test
    @DisplayName("Test if the CloudEvent is not expired using creation date when configured ttl is 500 milliseconds but no creation date.")
    public void test_cloudevent_is_not_expired_cd_when_ttl_3_mili_no_creation_date() {
        final Any protoPayload = buildProtoPayloadForTest();
        final CloudEventBuilder builder = CloudEventBuilder.v1()
                .withId("id")
                .withType("pub.v1")
                .withSource(URI.create("/body.accss//door.front_left#Door"))
                .withDataContentType(DATA_CONTENT_TYPE)
                .withDataSchema(URI.create(protoPayload.getTypeUrl()))
                .withData(protoPayload.toByteArray())
                .withExtension("ttl", 500);
        CloudEvent cloudEvent = builder.build();
        assertFalse(UCloudEvent.isExpiredByCloudEventCreationDate(cloudEvent));
    }

    @Test
    @DisplayName("Test if the CloudEvent is not expired using creation date when configured ttl is 500 milliseconds with creation date of now.")
    public void test_cloudevent_is_not_expired_cd_when_ttl_500_mili_with_creation_date_of_now() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withTime(OffsetDateTime.now())
                .withExtension("ttl", 500);
        CloudEvent cloudEvent = builder.build();
        assertFalse(UCloudEvent.isExpiredByCloudEventCreationDate(cloudEvent));
    }

    @Test
    @DisplayName("Test if the CloudEvent is expired using creation date when configured ttl is 500 milliseconds with creation date of yesterday.")
    public void test_cloudevent_is_expired_cd_when_ttl_500_mili_with_creation_date_of_yesterday() {
        OffsetDateTime yesterday = OffsetDateTime.now().minus(1, ChronoUnit.DAYS);
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withTime(yesterday)
                .withExtension("ttl", 500);
        CloudEvent cloudEvent = builder.build();
        assertTrue(UCloudEvent.isExpiredByCloudEventCreationDate(cloudEvent));
    }

    @Test
    @DisplayName("Test if the CloudEvent is not expired using creation date when configured ttl is 500 milliseconds with creation date of tomorrow.")
    public void test_cloudevent_is_not_expired_cd_when_ttl_500_mili_with_creation_date_of_tomorrow() {
        OffsetDateTime tomorrow = OffsetDateTime.now().plus(1, ChronoUnit.DAYS);
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withTime(tomorrow)
                .withExtension("ttl", 500);
        CloudEvent cloudEvent = builder.build();
        assertFalse(UCloudEvent.isExpiredByCloudEventCreationDate(cloudEvent));
    }

    @Test
    @DisplayName("Test if the CloudEvent is not expired when no ttl is configured.")
    public void test_cloudevent_is_not_expired_when_no_ttl_configured() {
        UUID uuid = UUIDFactory.Factories.UPROTOCOL.factory().create();
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withoutExtension("ttl")
                .withId(uuid.toString());
        CloudEvent cloudEvent = builder.build();
        assertFalse(UCloudEvent.isExpired(cloudEvent));
    }

    @Test
    @DisplayName("Test if the CloudEvent is not expired when configured ttl is zero.")
    public void test_cloudevent_is_not_expired_when_ttl_is_zero() {
        UUID uuid = UUIDFactory.Factories.UPROTOCOL.factory().create();
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withExtension("ttl", 0)
                .withId(uuid.toString());
        CloudEvent cloudEvent = builder.build();
        assertFalse(UCloudEvent.isExpired(cloudEvent));
    }

    @Test
    @DisplayName("Test if the CloudEvent is not expired when configured ttl is minus one.")
    public void test_cloudevent_is_not_expired_when_ttl_is_minus_one() {
        UUID uuid = UUIDFactory.Factories.UPROTOCOL.factory().create();
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withExtension("ttl", -1)
                .withId(uuid.toString());
        CloudEvent cloudEvent = builder.build();
        assertFalse(UCloudEvent.isExpired(cloudEvent));
    }

    @Test
    @DisplayName("Test if the CloudEvent is not expired when configured ttl is large number.")
    public void test_cloudevent_is_not_expired_when_ttl_is_large_number_mili() {
        UUID uuid = UUIDFactory.Factories.UPROTOCOL.factory().create();
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withExtension("ttl", Integer.MAX_VALUE)
                .withId(uuid.toString());
        CloudEvent cloudEvent = builder.build();
        assertFalse(UCloudEvent.isExpired(cloudEvent));
    }

    @Test
    @DisplayName("Test if the CloudEvent is expired when configured ttl is 1 milliseconds.")
    public void test_cloudevent_is_expired_when_ttl_1_mili() throws InterruptedException {
        UUID uuid = UUIDFactory.Factories.UPROTOCOL.factory().create();
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withExtension("ttl", 1)
                .withId(uuid.toString());
        CloudEvent cloudEvent = builder.build();
        Thread.sleep(800);
        assertTrue(UCloudEvent.isExpired(cloudEvent));
    }

    @Test
    @DisplayName("Test if the CloudEvent has a UUIDV8 id.")
    public void test_cloudevent_has_a_UUIDV8_id() {
        UUID uuid = UUIDFactory.Factories.UPROTOCOL.factory().create();
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withId(uuid.toString());
        CloudEvent cloudEvent = builder.build();
        assertTrue(UCloudEvent.isCloudEventId(cloudEvent));
    }

    @Test
    @DisplayName("Test if the CloudEvent does not have a UUIDV8 id.")
    public void test_cloudevent_does_not_have_a_UUIDV8_id() {
        UUID uuid = UUID.randomUUID();
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withExtension("ttl", 3)
                .withId(uuid.toString());
        CloudEvent cloudEvent = builder.build();
        assertFalse(UCloudEvent.isCloudEventId(cloudEvent));
    }

    @Test
    @DisplayName("Test if the CloudEvent does not have a valid UUID id but some string")
    public void test_cloudevent_does_not_have_a_UUID_id_just_some_string() {
        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withExtension("ttl", 3);
        CloudEvent cloudEvent = builder.build();
        assertFalse(UCloudEvent.isCloudEventId(cloudEvent));
    }

    @Test
    @DisplayName("Test extract payload from cloud event as Any protobuf object")
    public void test_extractPayload_from_cloud_event_as_any_proto_object() {
        Any payloadForCloudEvent = buildProtoPayloadForTest();
        byte[] cloudEventData = payloadForCloudEvent.toByteArray();

        final CloudEventBuilder cloudEventBuilder = CloudEventBuilder.v1()
                .withId("someId")
                .withType("pub.v1")
                .withSource(URI.create("/body.access/1/door.front_left#Door"))
                .withDataContentType(DATA_CONTENT_TYPE)
                .withDataSchema(URI.create(payloadForCloudEvent.getTypeUrl()))
                .withData(cloudEventData);
        CloudEvent cloudEvent = cloudEventBuilder.build();

        final Any extracted = UCloudEvent.getPayload(cloudEvent);

        assertEquals(payloadForCloudEvent, extracted);
    }

    @Test
    @DisplayName("Test extract payload from cloud event when payload is not an Any protobuf object")
    public void test_extractPayload_from_cloud_event_when_payload_is_not_an_any_proto_object() throws InvalidProtocolBufferException {
        io.cloudevents.v1.proto.CloudEvent payloadForCloudEvent = buildProtoPayloadForTest1();
        byte[] cloudEventData = payloadForCloudEvent.toByteArray();

        final CloudEventBuilder cloudEventBuilder = CloudEventBuilder.v1()
                .withId("someId")
                .withType("pub.v1")
                .withSource(URI.create("/body.access/1/door.front_left#Door"))
                .withDataContentType(DATA_CONTENT_TYPE)
                .withDataSchema(URI.create("type.googleapis.com/io.cloudevents.v1.CloudEvent"))
                .withData(cloudEventData);
        CloudEvent cloudEvent = cloudEventBuilder.build();

        final CloudEventData data = cloudEvent.getData();
        final Any dataAsAny = Any.parseFrom(data.toBytes());

        final Any extracted = UCloudEvent.getPayload(cloudEvent);

        assertEquals(dataAsAny, extracted);
    }

    @Test
    @DisplayName("Test extract payload from cloud event when payload is a bad protobuf object")
    public void test_extractPayload_from_cloud_event_when_payload_is_bad_proto_object()  {
        final CloudEventBuilder cloudEventBuilder = CloudEventBuilder.v1()
                .withId("someId")
                .withType("pub.v1")
                .withSource(URI.create("/body.access/1/door.front_left#Door"))
                .withDataContentType(DATA_CONTENT_TYPE)
                .withDataSchema(URI.create("type.googleapis.com/io.cloudevents.v1.CloudEvent"))
                .withData("<html><head></head><body><p>Hello</p></body></html>".getBytes());
        CloudEvent cloudEvent = cloudEventBuilder.build();

        final Any extracted = UCloudEvent.getPayload(cloudEvent);

        assertEquals(Any.getDefaultInstance(), extracted);
    }

    @Test
    @DisplayName("Test extract payload from cloud event as Any protobuf object when there is no data schema")
    public void test_extractPayload_from_cloud_event_as_any_proto_object_when_no_schema() {
        Any payloadForCloudEvent = buildProtoPayloadForTest();
        byte[] cloudEventData = payloadForCloudEvent.toByteArray();

        final CloudEventBuilder cloudEventBuilder = CloudEventBuilder.v1()
                .withId("someId")
                .withType("pub.v1")
                .withSource(URI.create("/body.access/1/door.front_left#Door"))
                .withDataContentType(DATA_CONTENT_TYPE)
                .withData(cloudEventData);
        CloudEvent cloudEvent = cloudEventBuilder.build();

        final Any extracted = UCloudEvent.getPayload(cloudEvent);

        assertEquals(payloadForCloudEvent, extracted);
    }

    @Test
    @DisplayName("Test extract payload from cloud event as Any protobuf object when there is no data")
    public void test_extractPayload_from_cloud_event_as_any_proto_object_when_no_data() {
        Any payloadForCloudEvent = buildProtoPayloadForTest();

        final CloudEventBuilder cloudEventBuilder = CloudEventBuilder.v1()
                .withId("someId")
                .withType("pub.v1")
                .withSource(URI.create("/body.access/1/door.front_left#Door"))
                .withDataContentType(DATA_CONTENT_TYPE)
                .withDataSchema(URI.create(payloadForCloudEvent.getTypeUrl()));
        CloudEvent cloudEvent = cloudEventBuilder.build();

        final Any extracted = UCloudEvent.getPayload(cloudEvent);

        assertEquals(Any.getDefaultInstance(), extracted);
    }

    @Test
    @DisplayName("Test unpack payload by class from cloud event as protobuf Message object")
    public void test_unpack_payload_by_class_from_cloud_event_proto_message_object() {
        Any payloadForCloudEvent = Any.pack(io.cloudevents.v1.proto.CloudEvent.newBuilder()
                .setSpecVersion("1.0")
                .setId("hello")
                .setSource("//VCU.MY_CAR_VIN/someService")
                .setType("example.demo")
                .setProtoData(Any.newBuilder().build())
                .build());
        byte[] cloudEventData = payloadForCloudEvent.toByteArray();

        final CloudEventBuilder cloudEventBuilder = CloudEventBuilder.v1()
                .withId("someId")
                .withType("pub.v1")
                .withSource(URI.create("/body.access/1/door.front_left#Door"))
                .withDataContentType(DATA_CONTENT_TYPE)
                .withDataSchema(URI.create(payloadForCloudEvent.getTypeUrl()))
                .withData(cloudEventData);
        CloudEvent cloudEvent = cloudEventBuilder.build();

        final Optional<io.cloudevents.v1.proto.CloudEvent> extracted =
                UCloudEvent.unpack(cloudEvent, io.cloudevents.v1.proto.CloudEvent.class);

        assertTrue(extracted.isPresent());
        final io.cloudevents.v1.proto.CloudEvent unpackedCE = extracted.get();
        assertEquals("1.0", unpackedCE.getSpecVersion());
        assertEquals("hello", unpackedCE.getId());
        assertEquals("example.demo", unpackedCE.getType());
        assertEquals("//VCU.MY_CAR_VIN/someService", unpackedCE.getSource());

    }

    @Test
    @DisplayName("Test unpack payload by class from cloud event when protobuf Message is not unpack-able")
    public void test_unpack_payload_by_class_from_cloud_event_proto_message_object_when_not_valid_message() {
        final CloudEventBuilder cloudEventBuilder = CloudEventBuilder.v1()
                .withId("someId")
                .withType("pub.v1")
                .withSource(URI.create("/body.access/1/door.front_left#Door"))
                .withDataContentType(DATA_CONTENT_TYPE)
                .withDataSchema(URI.create("type.googleapis.com/io.cloudevents.v1.CloudEvent"))
                .withData("<html><head></head><body><p>Hello</p></body></html>".getBytes());
        CloudEvent cloudEvent = cloudEventBuilder.build();

        final Optional<io.cloudevents.v1.proto.CloudEvent> extracted =
                UCloudEvent.unpack(cloudEvent, io.cloudevents.v1.proto.CloudEvent.class);

        assertTrue(extracted.isEmpty());

    }

    @Test
    @DisplayName("Test pretty printing a cloud event with a sink")
    public void test_pretty_printing_a_cloudevent_with_a_sink() {

        String sinkForTest = "//bo.cloud/petapp/1/rpc.response";

        CloudEventBuilder builder = buildBaseCloudEventBuilderForTest()
                .withExtension("sink", URI.create(sinkForTest));
        CloudEvent cloudEvent = builder.build();

        final String prettyPrint = UCloudEvent.toString(cloudEvent);
        final String expected = "CloudEvent{id='testme', source='/body.access//door.front_left#Door', " +
                "sink='//bo.cloud/petapp/1/rpc.response', type='pub.v1'}";

        assertEquals(expected, prettyPrint);
    }

    @Test
    @DisplayName("Test pretty printing a cloud event that is null")
    public void test_pretty_printing_a_cloudevent_that_is_null() {

        final String prettyPrint = UCloudEvent.toString(null);
        final String expected = "null";

        assertEquals(expected, prettyPrint);
    }

    @Test
    @DisplayName("Test pretty printing a cloud event without a sink")
    public void test_pretty_printing_a_cloudevent_without_a_sink() {
        CloudEvent cloudEvent = buildBaseCloudEventBuilderForTest().build();

        final String prettyPrint = UCloudEvent.toString(cloudEvent);
        final String expected = "CloudEvent{id='testme', source='/body.access//door.front_left#Door', type='pub.v1'}";

        assertEquals(expected, prettyPrint);
    }

    private CloudEventBuilder buildBaseCloudEventBuilderForTest() {
        // source
        UEntity use = UEntity.fromName("body.access");
        UUri Uri = new UUri(UAuthority.local(), use,
                new UResource("door", "front_left", "Door"));
        String source = UriSerializer.STRING.serialize(Uri);

        // fake payload
        final Any protoPayload = buildProtoPayloadForTest();

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

        return cloudEventBuilder;
    }

    private Any buildProtoPayloadForTest() {
        return Any.pack(buildProtoPayloadForTest1());
    }

    private io.cloudevents.v1.proto.CloudEvent buildProtoPayloadForTest1() {
        return io.cloudevents.v1.proto.CloudEvent.newBuilder()
                .setSpecVersion("1.0")
                .setId("hello")
                .setSource("//VCU.MY_CAR_VIN/body.access//door.front_left#Door")
                .setType("example.demo")
                .setProtoData(Any.newBuilder().build())
                .build();
    }
}