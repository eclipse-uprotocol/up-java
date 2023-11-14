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

package org.eclipse.uprotocol.cloudevent.factory;

import com.google.protobuf.Any;
import com.google.rpc.Code;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import org.eclipse.uprotocol.cloudevent.datamodel.UCloudEventAttributes;
import org.eclipse.uprotocol.uri.serializer.LongUriSerializer;
import org.eclipse.uprotocol.v1.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Objects;
import static org.junit.jupiter.api.Assertions.*;

class CloudEventFactoryTest {


    private static final String DATA_CONTENT_TYPE = CloudEventFactory.PROTOBUF_CONTENT_TYPE;

    @Test
    @DisplayName("Test create base CloudEvent")
    public void test_create_base_cloud_event()  {

        String source = buildUriForTest();

        // fake payload
        final Any protoPayload = buildProtoPayloadForTest();

        // additional attributes
        final UCloudEventAttributes uCloudEventAttributes = new UCloudEventAttributes.UCloudEventAttributesBuilder()
                .withHash("somehash")
                .withPriority(UPriority.UPRIORITY_CS1)
                .withTtl(3)
                .withToken("someOAuthToken")
                .build();

        // build the cloud event
        final CloudEventBuilder cloudEventBuilder = CloudEventFactory.buildBaseCloudEvent("testme", source,
                protoPayload.toByteArray(), protoPayload.getTypeUrl(),
                uCloudEventAttributes);
        cloudEventBuilder.withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH));

        final CloudEvent cloudEvent = cloudEventBuilder.build();

        assertEquals("1.0", cloudEvent.getSpecVersion().toString());
        assertEquals("testme", cloudEvent.getId());
        assertEquals(source, cloudEvent.getSource().toString());
        assertEquals(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH), cloudEvent.getType());
        assertFalse(cloudEvent.getExtensionNames().contains("sink"));
        assertEquals("somehash", cloudEvent.getExtension("hash"));
        assertEquals(UPriority.UPRIORITY_CS1.name(), cloudEvent.getExtension("priority"));
        assertEquals(3, cloudEvent.getExtension("ttl"));
        assertEquals("someOAuthToken", cloudEvent.getExtension("token"));

        assertArrayEquals(protoPayload.toByteArray(), Objects.requireNonNull(cloudEvent.getData()).toBytes());
    }

     @Test
    @DisplayName("Test create base CloudEvent with datacontenttype and dataschema")
    public void test_create_base_cloud_event_with_datacontenttype_and_schema()  {

        String source = buildUriForTest();

        // fake payload
        final Any protoPayload = buildProtoPayloadForTest();

        // additional attributes
        final UCloudEventAttributes uCloudEventAttributes = new UCloudEventAttributes.UCloudEventAttributesBuilder()
                .withHash("somehash")
                .withPriority(UPriority.UPRIORITY_CS1)
                .withTtl(3)
                .withToken("someOAuthToken")
                .build();

        // build the cloud event
        final CloudEventBuilder cloudEventBuilder = CloudEventFactory.buildBaseCloudEvent("testme", source,
                protoPayload.toByteArray(), protoPayload.getTypeUrl(),
                uCloudEventAttributes);
        cloudEventBuilder.withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH))
                .withDataContentType(DATA_CONTENT_TYPE)
                .withDataSchema(URI.create(protoPayload.getTypeUrl()));

        final CloudEvent cloudEvent = cloudEventBuilder.build();

        // test all attributes
        assertEquals("1.0", cloudEvent.getSpecVersion().toString());
        assertEquals("testme", cloudEvent.getId());
        assertEquals(source, cloudEvent.getSource().toString());
        assertEquals(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH), cloudEvent.getType());
        assertEquals(DATA_CONTENT_TYPE, cloudEvent.getDataContentType());
        assertEquals("type.googleapis.com/io.cloudevents.v1.CloudEvent",
                Objects.requireNonNull(cloudEvent.getDataSchema()).toString());
        assertFalse(cloudEvent.getExtensionNames().contains("sink"));
        assertEquals("somehash", cloudEvent.getExtension("hash"));
        assertEquals(UPriority.UPRIORITY_CS1.name(), cloudEvent.getExtension("priority"));
        assertEquals(3, cloudEvent.getExtension("ttl"));
        assertEquals("someOAuthToken", cloudEvent.getExtension("token"));

        assertArrayEquals(protoPayload.toByteArray(), Objects.requireNonNull(cloudEvent.getData()).toBytes());
    }

    @Test
    @DisplayName("Test create base CloudEvent without attributes")
    public void test_create_base_cloud_event_without_attributes()  {

        String source = buildUriForTest();

        // fake payload
        final Any protoPayload = buildProtoPayloadForTest();

        // no additional attributes
        final UCloudEventAttributes uCloudEventAttributes = UCloudEventAttributes.empty();

        // build the cloud event
        final CloudEventBuilder cloudEventBuilder = CloudEventFactory.buildBaseCloudEvent("testme", source,
                protoPayload.toByteArray(), protoPayload.getTypeUrl(),
                uCloudEventAttributes);
        cloudEventBuilder.withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH));

        final CloudEvent cloudEvent = cloudEventBuilder.build();

        assertEquals("1.0", cloudEvent.getSpecVersion().toString());
        assertEquals("testme", cloudEvent.getId());
        assertEquals(source, cloudEvent.getSource().toString());
        assertEquals(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH), cloudEvent.getType());
        assertFalse(cloudEvent.getExtensionNames().contains("sink"));
        assertFalse(cloudEvent.getExtensionNames().contains("hash"));
        assertFalse(cloudEvent.getExtensionNames().contains("priority"));
        assertFalse(cloudEvent.getExtensionNames().contains("ttl"));

        assertArrayEquals(protoPayload.toByteArray(), Objects.requireNonNull(cloudEvent.getData()).toBytes());

    }

    @Test
    @DisplayName("Test create publish CloudEvent")
    public void test_create_publish_cloud_event() {

        // source
        String source = buildUriForTest();

        // fake payload
        final Any protoPayload = buildProtoPayloadForTest();

        // additional attributes
        final UCloudEventAttributes uCloudEventAttributes = new UCloudEventAttributes.UCloudEventAttributesBuilder()
                .withHash("somehash")
                .withPriority(UPriority.UPRIORITY_CS1)
                .withTtl(3)
                .build();

        final CloudEvent cloudEvent = CloudEventFactory.publish(source, protoPayload, uCloudEventAttributes);

        assertEquals("1.0", cloudEvent.getSpecVersion().toString());
        assertNotNull(cloudEvent.getId());
        assertEquals(source, cloudEvent.getSource().toString());
        assertEquals(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH), cloudEvent.getType());
        assertFalse(cloudEvent.getExtensionNames().contains("sink"));
        assertEquals("somehash", cloudEvent.getExtension("hash"));
        assertEquals(UPriority.UPRIORITY_CS1.name(), cloudEvent.getExtension("priority"));
        assertEquals(3, cloudEvent.getExtension("ttl"));

        assertArrayEquals(protoPayload.toByteArray(), Objects.requireNonNull(cloudEvent.getData()).toBytes());
    }

    @Test
    @DisplayName("Test create notification CloudEvent")
    public void test_create_notification_cloud_event() {

        // source
        String source = buildUriForTest();

        // sink
        String sink = buildUriForTest();

        // fake payload
        final Any protoPayload = buildProtoPayloadForTest();

        // additional attributes
        final UCloudEventAttributes uCloudEventAttributes = new UCloudEventAttributes.UCloudEventAttributesBuilder()
                .withHash("somehash")
                .withPriority(UPriority.UPRIORITY_CS2)
                .withTtl(3)
                .build();

        // build the cloud event of type publish with destination - a notification
        final CloudEvent cloudEvent = CloudEventFactory.notification(source, sink, protoPayload, uCloudEventAttributes);

        assertEquals("1.0", cloudEvent.getSpecVersion().toString());
        assertNotNull(cloudEvent.getId());
        assertEquals(source, cloudEvent.getSource().toString());

        assertTrue(cloudEvent.getExtensionNames().contains("sink"));
        assertEquals(sink, Objects.requireNonNull(cloudEvent.getExtension("sink")).toString());

        assertEquals(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH), cloudEvent.getType());
        assertEquals("somehash", cloudEvent.getExtension("hash"));
        assertEquals(UPriority.UPRIORITY_CS2.name(), cloudEvent.getExtension("priority"));
        assertEquals(3, cloudEvent.getExtension("ttl"));

        assertArrayEquals(protoPayload.toByteArray(), Objects.requireNonNull(cloudEvent.getData()).toBytes());

    }

    @Test
    @DisplayName("Test create request RPC CloudEvent coming from a local USE")
    public void test_create_request_cloud_event_from_local_use() {

        // UriPart for the application requesting the RPC
        String applicationUriForRPC = buildUriForTest();

        // service Method UriPart
        String serviceMethodUri = buildUriForTest();

        // fake payload
        final Any protoPayload = buildProtoPayloadForTest();

        // additional attributes
        final UCloudEventAttributes uCloudEventAttributes = new UCloudEventAttributes.UCloudEventAttributesBuilder()
                .withHash("somehash")
                .withPriority(UPriority.UPRIORITY_CS2)
                .withTtl(3)
                .withToken("someOAuthToken")
                .build();

        final CloudEvent cloudEvent = CloudEventFactory.request(applicationUriForRPC, serviceMethodUri,
                protoPayload, uCloudEventAttributes);

        assertEquals("1.0", cloudEvent.getSpecVersion().toString());
        assertNotNull(cloudEvent.getId());
        assertEquals(applicationUriForRPC, cloudEvent.getSource().toString());

        assertTrue(cloudEvent.getExtensionNames().contains("sink"));
        assertEquals(serviceMethodUri, Objects.requireNonNull(cloudEvent.getExtension("sink")).toString());

        assertEquals("req.v1", cloudEvent.getType());
        assertEquals("somehash", cloudEvent.getExtension("hash"));
        assertEquals(UPriority.UPRIORITY_CS2.name(), cloudEvent.getExtension("priority"));
        assertEquals(3, cloudEvent.getExtension("ttl"));
        assertEquals("someOAuthToken", cloudEvent.getExtension("token"));

        assertArrayEquals(protoPayload.toByteArray(), Objects.requireNonNull(cloudEvent.getData()).toBytes());

    }


    @Test
    @DisplayName("Test create response RPC CloudEvent originating from a local USE")
    public void test_create_response_cloud_event_originating_from_local_use() {

        // UriPart for the application requesting the RPC
        String applicationUriForRPC = buildUriForTest();

        // service Method UriPart
        String serviceMethodUri = buildUriForTest();

        // fake payload
        final Any protoPayload = buildProtoPayloadForTest();

        // additional attributes
        final UCloudEventAttributes uCloudEventAttributes = new UCloudEventAttributes.UCloudEventAttributesBuilder()
                .withHash("somehash")
                .withPriority(UPriority.UPRIORITY_CS2)
                .withTtl(3)
                .build();

        final CloudEvent cloudEvent = CloudEventFactory.response(applicationUriForRPC, serviceMethodUri,
                "requestIdFromRequestCloudEvent", protoPayload, uCloudEventAttributes);

        assertEquals("1.0", cloudEvent.getSpecVersion().toString());
        assertNotNull(cloudEvent.getId());
        assertEquals(serviceMethodUri, cloudEvent.getSource().toString());

        assertTrue(cloudEvent.getExtensionNames().contains("sink"));
        assertEquals(applicationUriForRPC, Objects.requireNonNull(cloudEvent.getExtension("sink")).toString());

        assertEquals("res.v1", cloudEvent.getType());
        assertEquals("somehash", cloudEvent.getExtension("hash"));
        assertEquals(UPriority.UPRIORITY_CS2.name(), cloudEvent.getExtension("priority"));
        assertEquals(3, cloudEvent.getExtension("ttl"));

        assertEquals("requestIdFromRequestCloudEvent", cloudEvent.getExtension("reqid"));

        assertArrayEquals(protoPayload.toByteArray(), Objects.requireNonNull(cloudEvent.getData()).toBytes());

    }


    @Test
    @DisplayName("Test create a failed response RPC CloudEvent originating from a local USE")
    public void test_create_a_failed_response_cloud_event_originating_from_local_use() {

        // UriPart for the application requesting the RPC
        String applicationUriForRPC = buildUriForTest();

        // service Method UriPart
        String serviceMethodUri = buildUriForTest();

        // additional attributes
        final UCloudEventAttributes uCloudEventAttributes = new UCloudEventAttributes.UCloudEventAttributesBuilder()
                .withHash("somehash")
                .withPriority(UPriority.UPRIORITY_CS2)
                .withTtl(3)
                .build();

        final CloudEvent cloudEvent = CloudEventFactory.failedResponse(applicationUriForRPC, serviceMethodUri,
                "requestIdFromRequestCloudEvent",
                Code.INVALID_ARGUMENT_VALUE,
                uCloudEventAttributes);

        assertEquals("1.0", cloudEvent.getSpecVersion().toString());
        assertNotNull(cloudEvent.getId());
        assertEquals(serviceMethodUri, cloudEvent.getSource().toString());

        assertTrue(cloudEvent.getExtensionNames().contains("sink"));
        assertEquals(applicationUriForRPC, Objects.requireNonNull(cloudEvent.getExtension("sink")).toString());

        assertEquals("res.v1", cloudEvent.getType());
        assertEquals("somehash", cloudEvent.getExtension("hash"));
        assertEquals(UPriority.UPRIORITY_CS2.name(), cloudEvent.getExtension("priority"));
        assertEquals(3, cloudEvent.getExtension("ttl"));
        assertEquals(Code.INVALID_ARGUMENT_VALUE, cloudEvent.getExtension("commstatus"));

        assertEquals("requestIdFromRequestCloudEvent", cloudEvent.getExtension("reqid"));

    }

    @Test
    @DisplayName("Test create a failed response RPC CloudEvent originating from a microRemote USE")
    public void test_create_a_failed_response_cloud_event_originating_from_remote_use() {

        // UriPart for the application requesting the RPC
        String applicationUriForRPC = buildUriForTest();

        // service Method UriPart
        String serviceMethodUri = buildUriForTest();


        // additional attributes
        final UCloudEventAttributes uCloudEventAttributes = new UCloudEventAttributes.UCloudEventAttributesBuilder()
                .withHash("somehash")
                .withPriority(UPriority.UPRIORITY_CS2)
                .withTtl(3)
                .build();

        final CloudEvent cloudEvent = CloudEventFactory.failedResponse(applicationUriForRPC, serviceMethodUri,
                "requestIdFromRequestCloudEvent",
                Code.INVALID_ARGUMENT_VALUE,
                uCloudEventAttributes);

        assertEquals("1.0", cloudEvent.getSpecVersion().toString());
        assertNotNull(cloudEvent.getId());
        assertEquals(serviceMethodUri, cloudEvent.getSource().toString());

        assertTrue(cloudEvent.getExtensionNames().contains("sink"));
        assertEquals(applicationUriForRPC, Objects.requireNonNull(cloudEvent.getExtension("sink")).toString());

        assertEquals("res.v1", cloudEvent.getType());
        assertEquals("somehash", cloudEvent.getExtension("hash"));
        assertEquals(UPriority.UPRIORITY_CS2.name(), cloudEvent.getExtension("priority"));
        assertEquals(3, cloudEvent.getExtension("ttl"));
        assertEquals(Code.INVALID_ARGUMENT_VALUE, cloudEvent.getExtension("commstatus"));

        assertEquals("requestIdFromRequestCloudEvent", cloudEvent.getExtension("reqid"));

    }

    private String buildUriForTest() {

        UUri Uri = UUri.newBuilder()
            .setEntity(UEntity.newBuilder().setName("body.access"))
            .setResource(UResource.newBuilder()
                .setName("door")
                .setInstance("front_left")
                .setMessage("Door"))
            .build();
        
        return LongUriSerializer.instance().serialize(Uri);
    }

    private Any buildProtoPayloadForTest() {
        io.cloudevents.v1.proto.CloudEvent cloudEventProto = io.cloudevents.v1.proto.CloudEvent.newBuilder()
                .setSpecVersion("1.0")
                .setId("hello")
                .setSource("https://example.com")
                .setType("example.demo")
                .setProtoData(Any.newBuilder().build())
                .build();
        return Any.pack(cloudEventProto);
    }


}