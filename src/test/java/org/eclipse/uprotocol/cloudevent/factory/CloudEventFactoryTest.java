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
import io.cloudevents.v1.proto.CloudEvent;
import io.cloudevents.v1.proto.CloudEvent.CloudEventAttributeValue;

import org.eclipse.uprotocol.UprotocolOptions;
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
        final CloudEvent.Builder cloudEventBuilder = CloudEventFactory.buildBaseCloudEvent("testme", source,
                protoPayload, uCloudEventAttributes);
        cloudEventBuilder.setType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH));

        final CloudEvent cloudEvent = cloudEventBuilder.build();

        assertEquals("1.0", cloudEvent.getSpecVersion());
        assertEquals("testme", cloudEvent.getId());
        assertEquals(source, cloudEvent.getSource());
        assertEquals(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH), cloudEvent.getType());
        assertFalse(cloudEvent.getAttributesMap().containsKey("sink"));
        assertEquals("somehash", cloudEvent.getAttributesMap().get("hash").getCeString());
        assertEquals(
                UPriority.UPRIORITY_CS1.getValueDescriptor().getOptions().getExtension(UprotocolOptions.ceName), 
                cloudEvent.getAttributesMap().get("priority").getCeString());
        assertEquals(3, cloudEvent.getAttributesMap().get("ttl").getCeInteger());
        assertEquals("someOAuthToken", cloudEvent.getAttributesMap().get("token").getCeString());

        assertEquals(protoPayload, Objects.requireNonNull(cloudEvent.getProtoData()));
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
        final CloudEvent.Builder cloudEventBuilder = CloudEventFactory.buildBaseCloudEvent("testme", source,
                protoPayload,
                uCloudEventAttributes);
        cloudEventBuilder.setType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH))
                .putAttributes("datacontenttype", CloudEventAttributeValue.newBuilder().setCeString(DATA_CONTENT_TYPE).build())
                .putAttributes("dataschema", CloudEventAttributeValue.newBuilder().setCeString(protoPayload.getTypeUrl()).build());

        final CloudEvent cloudEvent = cloudEventBuilder.build();

        // test all attributes
        assertEquals("1.0", cloudEvent.getSpecVersion());
        assertEquals("testme", cloudEvent.getId());
        assertEquals(source, cloudEvent.getSource());
        assertEquals(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH), cloudEvent.getType());
        assertEquals(DATA_CONTENT_TYPE, cloudEvent.getAttributesMap().get("datacontenttype").getCeString());
        assertEquals("type.googleapis.com/io.cloudevents.v1.CloudEvent",
                Objects.requireNonNull(cloudEvent.getAttributesMap().get("dataschema").getCeString()));
        assertFalse(cloudEvent.getAttributesMap().containsKey("sink"));
        assertEquals("somehash", cloudEvent.getAttributesMap().get("hash").getCeString());
        assertEquals(
                UPriority.UPRIORITY_CS1.getValueDescriptor().getOptions().getExtension(UprotocolOptions.ceName), 
                cloudEvent.getAttributesMap().get("priority").getCeString());
        assertEquals(3, cloudEvent.getAttributesMap().get("ttl").getCeInteger());
        assertEquals("someOAuthToken", cloudEvent.getAttributesMap().get("token").getCeString());

        assertEquals(protoPayload, Objects.requireNonNull(cloudEvent.getProtoData()));
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
        final CloudEvent.Builder cloudEventBuilder = CloudEventFactory.buildBaseCloudEvent("testme", source,
                protoPayload,
                uCloudEventAttributes);
        cloudEventBuilder.setType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH));

        final CloudEvent cloudEvent = cloudEventBuilder.build();

        assertEquals("1.0", cloudEvent.getSpecVersion());
        assertEquals("testme", cloudEvent.getId());
        assertEquals(source, cloudEvent.getSource());
        assertEquals(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH), cloudEvent.getType());
        assertFalse(cloudEvent.getAttributesMap().containsKey("sink"));
        assertFalse(cloudEvent.getAttributesMap().containsKey("hash"));
        assertFalse(cloudEvent.getAttributesMap().containsKey("priority"));
        assertFalse(cloudEvent.getAttributesMap().containsKey("ttl"));

        assertArrayEquals(protoPayload.toByteArray(), Objects.requireNonNull(cloudEvent.getProtoData().toByteArray()));

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

        assertEquals("1.0", cloudEvent.getSpecVersion());
        assertNotNull(cloudEvent.getId());
        assertEquals(source, cloudEvent.getSource());
        assertEquals(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH), cloudEvent.getType());
        assertFalse(cloudEvent.getAttributesMap().containsKey("sink"));
        assertEquals("somehash", cloudEvent.getAttributesMap().get("hash").getCeString());
        assertEquals(
                UPriority.UPRIORITY_CS1.getValueDescriptor().getOptions().getExtension(UprotocolOptions.ceName), 
                cloudEvent.getAttributesMap().get("priority").getCeString());
        assertEquals(3, cloudEvent.getAttributesMap().get("ttl").getCeInteger());
        assertEquals(protoPayload, Objects.requireNonNull(cloudEvent.getProtoData()));
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

        assertEquals("1.0", cloudEvent.getSpecVersion());
        assertNotNull(cloudEvent.getId());
        assertEquals(source, cloudEvent.getSource());

        assertTrue(cloudEvent.getAttributesMap().containsKey("sink"));

        assertEquals("somehash", cloudEvent.getAttributesMap().get("hash").getCeString());
        assertEquals(
                UPriority.UPRIORITY_CS2.getValueDescriptor().getOptions().getExtension(UprotocolOptions.ceName), 
                cloudEvent.getAttributesMap().get("priority").getCeString());
        assertEquals(3, cloudEvent.getAttributesMap().get("ttl").getCeInteger());

        assertEquals(protoPayload, Objects.requireNonNull(cloudEvent.getProtoData()));

        assertEquals(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH), cloudEvent.getType());
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

        assertEquals("1.0", cloudEvent.getSpecVersion());
        assertNotNull(cloudEvent.getId());
        assertEquals(applicationUriForRPC, cloudEvent.getSource());

        assertTrue(cloudEvent.getAttributesMap().containsKey("sink"));

        assertEquals(
                UMessageType.UMESSAGE_TYPE_REQUEST.getValueDescriptor().getOptions().getExtension(UprotocolOptions.ceName), 
                cloudEvent.getType());
        

        assertEquals(serviceMethodUri, cloudEvent.getAttributesMap().get("sink").getCeString());
        assertEquals("somehash", cloudEvent.getAttributesMap().get("hash").getCeString());
        assertEquals(
                UPriority.UPRIORITY_CS2.getValueDescriptor().getOptions().getExtension(UprotocolOptions.ceName), 
                cloudEvent.getAttributesMap().get("priority").getCeString());
        assertEquals(3, cloudEvent.getAttributesMap().get("ttl").getCeInteger());
        assertEquals("someOAuthToken", cloudEvent.getAttributesMap().get("token").getCeString());

        assertEquals(protoPayload, Objects.requireNonNull(cloudEvent.getProtoData()));
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

        assertEquals("1.0", cloudEvent.getSpecVersion());
        assertNotNull(cloudEvent.getId());
        assertEquals(serviceMethodUri, cloudEvent.getSource());

        assertEquals("requestIdFromRequestCloudEvent", cloudEvent.getAttributesMap().get("reqid").getCeString());
        
        assertTrue(cloudEvent.getAttributesMap().containsKey("sink"));
        assertEquals(
                UMessageType.UMESSAGE_TYPE_RESPONSE.getValueDescriptor().getOptions().getExtension(UprotocolOptions.ceName), 
                cloudEvent.getType());
        

        assertEquals(applicationUriForRPC, cloudEvent.getAttributesMap().get("sink").getCeString());
        assertEquals("somehash", cloudEvent.getAttributesMap().get("hash").getCeString());
        assertEquals(
                UPriority.UPRIORITY_CS2.getValueDescriptor().getOptions().getExtension(UprotocolOptions.ceName), 
                cloudEvent.getAttributesMap().get("priority").getCeString());
        assertEquals(3, cloudEvent.getAttributesMap().get("ttl").getCeInteger());
        assertEquals(protoPayload, Objects.requireNonNull(cloudEvent.getProtoData()));
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
                UCode.INVALID_ARGUMENT_VALUE,
                uCloudEventAttributes);

        assertEquals("1.0", cloudEvent.getSpecVersion());
        assertNotNull(cloudEvent.getId());
        assertEquals(serviceMethodUri, cloudEvent.getSource());

        assertEquals(UCode.INVALID_ARGUMENT_VALUE, cloudEvent.getAttributesMap().get("commstatus").getCeInteger());

        assertEquals("requestIdFromRequestCloudEvent", cloudEvent.getAttributesMap().get("reqid").getCeString());
        
        assertTrue(cloudEvent.getAttributesMap().containsKey("sink"));
        assertEquals(
                UMessageType.UMESSAGE_TYPE_RESPONSE.getValueDescriptor().getOptions().getExtension(UprotocolOptions.ceName), 
                cloudEvent.getType());
        

        assertEquals(applicationUriForRPC, cloudEvent.getAttributesMap().get("sink").getCeString());
        assertEquals("somehash", cloudEvent.getAttributesMap().get("hash").getCeString());
        assertEquals(
                UPriority.UPRIORITY_CS2.getValueDescriptor().getOptions().getExtension(UprotocolOptions.ceName), 
                cloudEvent.getAttributesMap().get("priority").getCeString());
        assertEquals(3, cloudEvent.getAttributesMap().get("ttl").getCeInteger());
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
                UCode.INVALID_ARGUMENT_VALUE,
                uCloudEventAttributes);

        assertEquals("1.0", cloudEvent.getSpecVersion());
        assertNotNull(cloudEvent.getId());
        assertEquals(serviceMethodUri, cloudEvent.getSource());

        assertEquals(UCode.INVALID_ARGUMENT_VALUE, cloudEvent.getAttributesMap().get("commstatus").getCeInteger());


        assertEquals("requestIdFromRequestCloudEvent", cloudEvent.getAttributesMap().get("reqid").getCeString());
        
        assertTrue(cloudEvent.getAttributesMap().containsKey("sink"));
        assertEquals(
                UMessageType.UMESSAGE_TYPE_RESPONSE.getValueDescriptor().getOptions().getExtension(UprotocolOptions.ceName), 
                cloudEvent.getType());
        

        assertEquals(applicationUriForRPC, cloudEvent.getAttributesMap().get("sink").getCeString());
        assertEquals("somehash", cloudEvent.getAttributesMap().get("hash").getCeString());
        assertEquals(
                UPriority.UPRIORITY_CS2.getValueDescriptor().getOptions().getExtension(UprotocolOptions.ceName), 
                cloudEvent.getAttributesMap().get("priority").getCeString());
        assertEquals(3, cloudEvent.getAttributesMap().get("ttl").getCeInteger());
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