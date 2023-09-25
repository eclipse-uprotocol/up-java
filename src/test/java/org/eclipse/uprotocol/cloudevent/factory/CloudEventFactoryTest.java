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
import org.eclipse.uprotocol.uri.factory.UriFactory;
import com.google.protobuf.Any;
import com.google.rpc.Code;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
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

        // source
        UEntity use = UEntity.fromName("body.access");
        UUri Uri = new UUri(UAuthority.local(), use,
                new UResource("door", "front_left", "Door"));
        String source = UriFactory.buildUProtocolUri(Uri);

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

        final CloudEvent cloudEvent = cloudEventBuilder.build();

        assertEquals("1.0", cloudEvent.getSpecVersion().toString());
        assertEquals("testme", cloudEvent.getId());
        assertEquals(source, cloudEvent.getSource().toString());
        assertEquals(UCloudEventType.PUBLISH.type(), cloudEvent.getType());
        assertFalse(cloudEvent.getExtensionNames().contains("sink"));
        assertEquals("somehash", cloudEvent.getExtension("hash"));
        assertEquals(UCloudEventAttributes.Priority.STANDARD.qosString(), cloudEvent.getExtension("priority"));
        assertEquals(3, cloudEvent.getExtension("ttl"));
        assertEquals("someOAuthToken", cloudEvent.getExtension("token"));

        assertArrayEquals(protoPayload.toByteArray(), Objects.requireNonNull(cloudEvent.getData()).toBytes());
    }

     @Test
    @DisplayName("Test create base CloudEvent with datacontenttype and dataschema")
    public void test_create_base_cloud_event_with_datacontenttype_and_schema()  {

        // source
        UEntity use = UEntity.fromName("body.access");
        UUri ultifiUri = new UUri(UAuthority.local(), use,
                new UResource("door", "front_left", "Door"));
        String source = UriFactory.buildUProtocolUri(ultifiUri);

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
        cloudEventBuilder.withType(UCloudEventType.PUBLISH.type())
                .withDataContentType(DATA_CONTENT_TYPE)
                .withDataSchema(URI.create(protoPayload.getTypeUrl()));

        final CloudEvent cloudEvent = cloudEventBuilder.build();

        // test all attributes match the table in SDV-202
        assertEquals("1.0", cloudEvent.getSpecVersion().toString());
        assertEquals("testme", cloudEvent.getId());
        assertEquals(source, cloudEvent.getSource().toString());
        assertEquals(UCloudEventType.PUBLISH.type(), cloudEvent.getType());
        assertEquals(DATA_CONTENT_TYPE, cloudEvent.getDataContentType());
        assertEquals("type.googleapis.com/io.cloudevents.v1.CloudEvent",
                Objects.requireNonNull(cloudEvent.getDataSchema()).toString());
        assertFalse(cloudEvent.getExtensionNames().contains("sink"));
        assertEquals("somehash", cloudEvent.getExtension("hash"));
        assertEquals(UCloudEventAttributes.Priority.STANDARD.qosString(), cloudEvent.getExtension("priority"));
        assertEquals(3, cloudEvent.getExtension("ttl"));
        assertEquals("someOAuthToken", cloudEvent.getExtension("token"));

        assertArrayEquals(protoPayload.toByteArray(), Objects.requireNonNull(cloudEvent.getData()).toBytes());
    }

    @Test
    @DisplayName("Test create base CloudEvent without attributes")
    public void test_create_base_cloud_event_without_attributes()  {

        // source
        UEntity use = UEntity.fromName("body.access");
        UUri Uri = new UUri(UAuthority.local(), use,
                new UResource("door", "front_left", "Door"));
        String source = UriFactory.buildUProtocolUri(Uri);

        // fake payload
        final Any protoPayload = buildProtoPayloadForTest();

        // no additional attributes
        final UCloudEventAttributes uCloudEventAttributes = UCloudEventAttributes.empty();

        // build the cloud event
        final CloudEventBuilder cloudEventBuilder = CloudEventFactory.buildBaseCloudEvent("testme", source,
                protoPayload.toByteArray(), protoPayload.getTypeUrl(),
                uCloudEventAttributes);
        cloudEventBuilder.withType(UCloudEventType.PUBLISH.type());

        final CloudEvent cloudEvent = cloudEventBuilder.build();

        assertEquals("1.0", cloudEvent.getSpecVersion().toString());
        assertEquals("testme", cloudEvent.getId());
        assertEquals(source, cloudEvent.getSource().toString());
        assertEquals(UCloudEventType.PUBLISH.type(), cloudEvent.getType());
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
        UEntity use = UEntity.fromName("body.access");
        UUri Uri = new UUri(UAuthority.local(), use,
                new UResource("door", "front_left", "Door"));
        String source = UriFactory.buildUProtocolUri(Uri);

        // fake payload
        final Any protoPayload = buildProtoPayloadForTest();

        // additional attributes
        final UCloudEventAttributes uCloudEventAttributes = new UCloudEventAttributes.UCloudEventAttributesBuilder()
                .withHash("somehash")
                .withPriority(UCloudEventAttributes.Priority.STANDARD)
                .withTtl(3)
                .build();

        final CloudEvent cloudEvent = CloudEventFactory.publish(source, protoPayload, uCloudEventAttributes);

        assertEquals("1.0", cloudEvent.getSpecVersion().toString());
        assertNotNull(cloudEvent.getId());
        assertEquals(source, cloudEvent.getSource().toString());
        assertEquals(UCloudEventType.PUBLISH.type(), cloudEvent.getType());
        assertFalse(cloudEvent.getExtensionNames().contains("sink"));
        assertEquals("somehash", cloudEvent.getExtension("hash"));
        assertEquals(UCloudEventAttributes.Priority.STANDARD.qosString(), cloudEvent.getExtension("priority"));
        assertEquals(3, cloudEvent.getExtension("ttl"));

        assertArrayEquals(protoPayload.toByteArray(), Objects.requireNonNull(cloudEvent.getData()).toBytes());
    }

    @Test
    @DisplayName("Test create notification CloudEvent")
    public void test_create_notification_cloud_event() {

        // source
        UEntity use = UEntity.fromName("body.access");
        UUri Uri = new UUri(UAuthority.local(), use,
                new UResource("door", "front_left", "Door"));
        String source = UriFactory.buildUProtocolUri(Uri);

        // sink
        UEntity sinkUse = UEntity.fromName("petapp");
        UUri sinkUri = new UUri(UAuthority.remote("com.gm.bo", "bo"), sinkUse, "OK");
        String sink = UriFactory.buildUProtocolUri(sinkUri);

        // fake payload
        final Any protoPayload = buildProtoPayloadForTest();

        // additional attributes
        final UCloudEventAttributes uCloudEventAttributes = new UCloudEventAttributes.UCloudEventAttributesBuilder()
                .withHash("somehash")
                .withPriority(UCloudEventAttributes.Priority.OPERATIONS)
                .withTtl(3)
                .build();

        // build the cloud event of type publish with destination - a notification
        final CloudEvent cloudEvent = CloudEventFactory.notification(source, sink, protoPayload, uCloudEventAttributes);

        assertEquals("1.0", cloudEvent.getSpecVersion().toString());
        assertNotNull(cloudEvent.getId());
        assertEquals(source, cloudEvent.getSource().toString());

        assertTrue(cloudEvent.getExtensionNames().contains("sink"));
        assertEquals(sink, Objects.requireNonNull(cloudEvent.getExtension("sink")).toString());

        assertEquals(UCloudEventType.PUBLISH.type(), cloudEvent.getType());
        assertEquals("somehash", cloudEvent.getExtension("hash"));
        assertEquals(UCloudEventAttributes.Priority.OPERATIONS.qosString(), cloudEvent.getExtension("priority"));
        assertEquals(3, cloudEvent.getExtension("ttl"));

        assertArrayEquals(protoPayload.toByteArray(), Objects.requireNonNull(cloudEvent.getData()).toBytes());

    }

    @Test
    @DisplayName("Test create request RPC CloudEvent coming from a local USE")
    public void test_create_request_cloud_event_from_local_use() {

        // Uri for the application requesting the RPC
        UEntity sourceUse = UEntity.fromName("petapp");
        String applicationUriForRPC = UriFactory.buildUriForRpc(UAuthority.local(), sourceUse);

        // service Method Uri
        UEntity methodSoftwareEntityService = new UEntity("body.access", "1");
        UUri methodUri = new UUri(UAuthority.local(), methodSoftwareEntityService,
                UResource.forRpc("UpdateDoor"));
        String serviceMethodUri = UriFactory.buildUProtocolUri(methodUri);

        // fake payload
        final Any protoPayload = buildProtoPayloadForTest();

        // additional attributes
        final UCloudEventAttributes uCloudEventAttributes = new UCloudEventAttributes.UCloudEventAttributesBuilder()
                .withHash("somehash")
                .withPriority(UCloudEventAttributes.Priority.OPERATIONS)
                .withTtl(3)
                .withToken("someOAuthToken")
                .build();

        final CloudEvent cloudEvent = CloudEventFactory.request(applicationUriForRPC, serviceMethodUri,
                protoPayload, uCloudEventAttributes);

        assertEquals("1.0", cloudEvent.getSpecVersion().toString());
        assertNotNull(cloudEvent.getId());
        assertEquals("/petapp//rpc.response", cloudEvent.getSource().toString());

        assertTrue(cloudEvent.getExtensionNames().contains("sink"));
        assertEquals("/body.access/1/rpc.UpdateDoor", Objects.requireNonNull(cloudEvent.getExtension("sink")).toString());

        assertEquals("req.v1", cloudEvent.getType());
        assertEquals("somehash", cloudEvent.getExtension("hash"));
        assertEquals(UCloudEventAttributes.Priority.OPERATIONS.qosString(), cloudEvent.getExtension("priority"));
        assertEquals(3, cloudEvent.getExtension("ttl"));
        assertEquals("someOAuthToken", cloudEvent.getExtension("token"));

        assertArrayEquals(protoPayload.toByteArray(), Objects.requireNonNull(cloudEvent.getData()).toBytes());

    }

    @Test
    @DisplayName("Test create request RPC CloudEvent coming from a remote USE")
    public void test_create_request_cloud_event_from_remote_use() {

        // Uri for the application requesting the RPC
        UAuthority sourceUseAuthority = UAuthority.remote("bo", "cloud");
        UEntity sourceUse = new UEntity("petapp", "1");
        String applicationUriForRPC = UriFactory.buildUriForRpc(sourceUseAuthority, sourceUse);

        // service Method Uri
        UEntity methodSoftwareEntityService = new UEntity("body.access", "1");
        UUri methodUri = new UUri(UAuthority.remote("VCU", "MY_CAR_VIN"),
                methodSoftwareEntityService,
                UResource.forRpc("UpdateDoor"));
        String serviceMethodUri = UriFactory.buildUProtocolUri(methodUri);

        // fake payload
        final Any protoPayload = buildProtoPayloadForTest();

        // additional attributes
        final UCloudEventAttributes uCloudEventAttributes = new UCloudEventAttributes.UCloudEventAttributesBuilder()
                .withHash("somehash")
                .withPriority(UCloudEventAttributes.Priority.OPERATIONS)
                .withTtl(3)
                .withToken("someOAuthToken")
                .build();

        final CloudEvent cloudEvent = CloudEventFactory.request(applicationUriForRPC, serviceMethodUri,
                protoPayload, uCloudEventAttributes);

        assertEquals("1.0", cloudEvent.getSpecVersion().toString());
        assertNotNull(cloudEvent.getId());
        assertEquals("//bo.cloud/petapp/1/rpc.response", cloudEvent.getSource().toString());

        assertTrue(cloudEvent.getExtensionNames().contains("sink"));
        assertEquals("//vcu.my_car_vin/body.access/1/rpc.UpdateDoor", Objects.requireNonNull(cloudEvent.getExtension("sink")).toString());

        assertEquals("req.v1", cloudEvent.getType());
        assertEquals("somehash", cloudEvent.getExtension("hash"));
        assertEquals(UCloudEventAttributes.Priority.OPERATIONS.qosString(), cloudEvent.getExtension("priority"));
        assertEquals(3, cloudEvent.getExtension("ttl"));
        assertEquals("someOAuthToken", cloudEvent.getExtension("token"));

        assertArrayEquals(protoPayload.toByteArray(), Objects.requireNonNull(cloudEvent.getData()).toBytes());

    }

    @Test
    @DisplayName("Test create response RPC CloudEvent originating from a local USE")
    public void test_create_response_cloud_event_originating_from_local_use() {

        // Uri for the application requesting the RPC
        UEntity sourceUse = new UEntity("petapp", "1");
        String applicationUriForRPC = UriFactory.buildUriForRpc(UAuthority.local(), sourceUse);

        // service Method Uri
        UEntity methodSoftwareEntityService = new UEntity("body.access", "1");
        UUri methodUri = new UUri(UAuthority.local(), methodSoftwareEntityService,
                UResource.forRpc("UpdateDoor"));
        String serviceMethodUri = UriFactory.buildUProtocolUri(methodUri);

        // fake payload
        final Any protoPayload = buildProtoPayloadForTest();

        // additional attributes
        final UCloudEventAttributes uCloudEventAttributes = new UCloudEventAttributes.UCloudEventAttributesBuilder()
                .withHash("somehash")
                .withPriority(UCloudEventAttributes.Priority.OPERATIONS)
                .withTtl(3)
                .build();

        final CloudEvent cloudEvent = CloudEventFactory.response(applicationUriForRPC, serviceMethodUri,
                "requestIdFromRequestCloudEvent", protoPayload, uCloudEventAttributes);

        assertEquals("1.0", cloudEvent.getSpecVersion().toString());
        assertNotNull(cloudEvent.getId());
        assertEquals("/body.access/1/rpc.UpdateDoor", cloudEvent.getSource().toString());

        assertTrue(cloudEvent.getExtensionNames().contains("sink"));
        assertEquals("/petapp/1/rpc.response", Objects.requireNonNull(cloudEvent.getExtension("sink")).toString());

        assertEquals("res.v1", cloudEvent.getType());
        assertEquals("somehash", cloudEvent.getExtension("hash"));
        assertEquals(UCloudEventAttributes.Priority.OPERATIONS.qosString(), cloudEvent.getExtension("priority"));
        assertEquals(3, cloudEvent.getExtension("ttl"));

        assertEquals("requestIdFromRequestCloudEvent", cloudEvent.getExtension("reqid"));

        assertArrayEquals(protoPayload.toByteArray(), Objects.requireNonNull(cloudEvent.getData()).toBytes());

    }

    @Test
    @DisplayName("Test create response RPC CloudEvent originating from a remote USE")
    public void test_create_response_cloud_event_originating_from_remote_use() {

        // Uri for the application requesting the RPC
        UAuthority sourceUseAuthority = UAuthority.remote("bo", "cloud");
        UEntity sourceUse = UEntity.fromName("petapp");
        String applicationUriForRPC = UriFactory.buildUriForRpc(sourceUseAuthority, sourceUse);

        // service Method Uri
        UEntity methodSoftwareEntityService = new UEntity("body.access", "1");
        UUri methodUri = new UUri(UAuthority.remote("VCU", "MY_CAR_VIN"),
                methodSoftwareEntityService,
                UResource.forRpc("UpdateDoor"));
        String serviceMethodUri = UriFactory.buildUProtocolUri(methodUri);

        // fake payload
        final Any protoPayload = buildProtoPayloadForTest();

        // additional attributes
        final UCloudEventAttributes uCloudEventAttributes = new UCloudEventAttributes.UCloudEventAttributesBuilder()
                .withHash("somehash")
                .withPriority(UCloudEventAttributes.Priority.OPERATIONS)
                .withTtl(3)
                .build();

        final CloudEvent cloudEvent = CloudEventFactory.response(applicationUriForRPC, serviceMethodUri,
                "requestIdFromRequestCloudEvent", protoPayload, uCloudEventAttributes);


        assertEquals("1.0", cloudEvent.getSpecVersion().toString());
        assertNotNull(cloudEvent.getId());
        assertEquals("//vcu.my_car_vin/body.access/1/rpc.UpdateDoor", cloudEvent.getSource().toString());

        assertTrue(cloudEvent.getExtensionNames().contains("sink"));
        assertEquals("//bo.cloud/petapp//rpc.response", Objects.requireNonNull(cloudEvent.getExtension("sink")).toString());

        assertEquals("res.v1", cloudEvent.getType());
        assertEquals("somehash", cloudEvent.getExtension("hash"));
        assertEquals(UCloudEventAttributes.Priority.OPERATIONS.qosString(), cloudEvent.getExtension("priority"));
        assertEquals(3, cloudEvent.getExtension("ttl"));

        assertEquals("requestIdFromRequestCloudEvent", cloudEvent.getExtension("reqid"));

        assertArrayEquals(protoPayload.toByteArray(), Objects.requireNonNull(cloudEvent.getData()).toBytes());

    }

    @Test
    @DisplayName("Test create a failed response RPC CloudEvent originating from a local USE")
    public void test_create_a_failed_response_cloud_event_originating_from_local_use() {

        // Uri for the application requesting the RPC
        UEntity sourceUse = new UEntity("petapp", "1");
        String applicationUriForRPC = UriFactory.buildUriForRpc(UAuthority.local(), sourceUse);

        // service Method Uri
        UEntity methodSoftwareEntityService = new UEntity("body.access", "1");
        UUri methodUri = new UUri(UAuthority.local(), methodSoftwareEntityService,
                UResource.forRpc("UpdateDoor"));
        String serviceMethodUri = UriFactory.buildUProtocolUri(methodUri);

        // fake payload
        final Any protoPayload = buildProtoPayloadForTest();

        // additional attributes
        final UCloudEventAttributes uCloudEventAttributes = new UCloudEventAttributes.UCloudEventAttributesBuilder()
                .withHash("somehash")
                .withPriority(UCloudEventAttributes.Priority.OPERATIONS)
                .withTtl(3)
                .build();

        final CloudEvent cloudEvent = CloudEventFactory.failedResponse(applicationUriForRPC, serviceMethodUri,
                "requestIdFromRequestCloudEvent",
                Code.INVALID_ARGUMENT_VALUE,
                uCloudEventAttributes);

        assertEquals("1.0", cloudEvent.getSpecVersion().toString());
        assertNotNull(cloudEvent.getId());
        assertEquals("/body.access/1/rpc.UpdateDoor", cloudEvent.getSource().toString());

        assertTrue(cloudEvent.getExtensionNames().contains("sink"));
        assertEquals("/petapp/1/rpc.response", Objects.requireNonNull(cloudEvent.getExtension("sink")).toString());

        assertEquals("res.v1", cloudEvent.getType());
        assertEquals("somehash", cloudEvent.getExtension("hash"));
        assertEquals(UCloudEventAttributes.Priority.OPERATIONS.qosString(), cloudEvent.getExtension("priority"));
        assertEquals(3, cloudEvent.getExtension("ttl"));
        assertEquals(Code.INVALID_ARGUMENT_VALUE, cloudEvent.getExtension("commstatus"));

        assertEquals("requestIdFromRequestCloudEvent", cloudEvent.getExtension("reqid"));

    }

    @Test
    @DisplayName("Test create a failed response RPC CloudEvent originating from a remote USE")
    public void test_create_a_failed_response_cloud_event_originating_from_remote_use() {

        // Uri for the application requesting the RPC
        UAuthority sourceUseAuthority = UAuthority.remote("bo", "cloud");
        UEntity sourceUse = UEntity.fromName("petapp");
        String applicationUriForRPC = UriFactory.buildUriForRpc(sourceUseAuthority, sourceUse);

        // service Method Uri
        UEntity methodSoftwareEntityService = new UEntity("body.access", "1");
        UUri methodUri = new UUri(UAuthority.remote("VCU", "MY_CAR_VIN"),
                methodSoftwareEntityService,
                UResource.forRpc("UpdateDoor"));
        String serviceMethodUri = UriFactory.buildUProtocolUri(methodUri);

        // fake payload
        final Any protoPayload = buildProtoPayloadForTest();

        // additional attributes
        final UCloudEventAttributes uCloudEventAttributes = new UCloudEventAttributes.UCloudEventAttributesBuilder()
                .withHash("somehash")
                .withPriority(UCloudEventAttributes.Priority.OPERATIONS)
                .withTtl(3)
                .build();

        final CloudEvent cloudEvent = CloudEventFactory.failedResponse(applicationUriForRPC, serviceMethodUri,
                "requestIdFromRequestCloudEvent",
                Code.INVALID_ARGUMENT_VALUE,
                uCloudEventAttributes);

        assertEquals("1.0", cloudEvent.getSpecVersion().toString());
        assertNotNull(cloudEvent.getId());
        assertEquals("//vcu.my_car_vin/body.access/1/rpc.UpdateDoor", cloudEvent.getSource().toString());

        assertTrue(cloudEvent.getExtensionNames().contains("sink"));
        assertEquals("//bo.cloud/petapp//rpc.response", Objects.requireNonNull(cloudEvent.getExtension("sink")).toString());

        assertEquals("res.v1", cloudEvent.getType());
        assertEquals("somehash", cloudEvent.getExtension("hash"));
        assertEquals(UCloudEventAttributes.Priority.OPERATIONS.qosString(), cloudEvent.getExtension("priority"));
        assertEquals(3, cloudEvent.getExtension("ttl"));
        assertEquals(Code.INVALID_ARGUMENT_VALUE, cloudEvent.getExtension("commstatus"));

        assertEquals("requestIdFromRequestCloudEvent", cloudEvent.getExtension("reqid"));

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