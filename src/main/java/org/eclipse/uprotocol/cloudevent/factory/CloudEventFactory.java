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
import org.eclipse.uprotocol.uri.datamodel.UUri;
import org.eclipse.uprotocol.uri.factory.UriFactory;
import com.google.protobuf.Any;
import com.google.protobuf.Empty;
import com.google.rpc.Code;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import org.eclipse.uprotocol.uuid.factory.UUIDFactory;
import java.net.URI;
import java.util.UUID;

/**
 * A factory is a part of the software has methods to generate concrete objects, usually of the same type or interface.<br>
 * CloudEvents is a specification for describing events in a common way. We will use CloudEvents
 * to formulate all kinds of  events (messages) that will be sent to and from devices.<br>
 * The CloudEvent factory knows how to generate CloudEvents of the 4 core types: req.v1, res.v1, pub.v1, and file.v1<br>
 */
public interface CloudEventFactory {

    String PROTOBUF_CONTENT_TYPE = "application/x-protobuf";

    /**
     * Create a CloudEvent for an event for the use case of: RPC Request message.
     *
     * @param applicationUriForRPC   The uri for the application requesting the RPC.
     * @param serviceMethodUri       The uri for the method to be called on the service Ex.: :/body.access/1/rpc.UpdateDoor
     * @param protoPayload           Protobuf Any object with the Message command to be executed on the sink service.
     * @param attributes             Additional attributes such as ttl, hash, priority and token.
     * @return Returns an  request CloudEvent.
     */
    static CloudEvent request(String applicationUriForRPC,
                              String serviceMethodUri,
                              Any protoPayload,
                              UCloudEventAttributes attributes) {
        String id = generateCloudEventId();
        return buildBaseCloudEvent(id, applicationUriForRPC, protoPayload.toByteArray(), protoPayload.getTypeUrl(), attributes )
                .withType(UCloudEventType.REQUEST.type())
                .withExtension("sink", URI.create(serviceMethodUri))
                .build();
    }

    /**
     * Create a CloudEvent for an event for the use case of: RPC Response message.
     *
     * @param applicationUriForRPC  The destination of the response. The uri for the original application that requested the RPC and this response is for.
     * @param serviceMethodUri      The uri for the method that was called on the service Ex.: :/body.access/1/rpc.UpdateDoor
     * @param requestId             The cloud event id from the original request cloud event that this response if for.
     * @param protoPayload          The protobuf serialized response message as defined by the application interface or the
     *                              google.rpc.Status message containing the details of an error.
     * @param attributes            Additional attributes such as ttl, hash and priority.
     * @return Returns an  response CloudEvent.
     */
    static CloudEvent response(String applicationUriForRPC,
                               String serviceMethodUri,
                               String requestId,
                               Any protoPayload,
                               UCloudEventAttributes attributes) {
        String id = generateCloudEventId();
        return buildBaseCloudEvent(id, serviceMethodUri, protoPayload.toByteArray(), protoPayload.getTypeUrl(), attributes)
                .withType(UCloudEventType.RESPONSE.type())
                .withExtension("sink", URI.create(applicationUriForRPC))
                .withExtension("reqid", requestId)
                .build();
    }

    /**
     * Create a CloudEvent for an event for the use case of: RPC Response message that failed.
     *
     * @param applicationUriForRPC  The destination of the response. The uri for the original application that requested the RPC and this response is for.
     * @param serviceMethodUri      The uri for the method that was called on the service Ex.: :/body.access/1/rpc.UpdateDoor
     * @param requestId             The cloud event id from the original request cloud event that this response if for.
     * @param communicationStatus   A {@link Code} value that indicates of a platform communication error while delivering this CloudEvent.
     * @param attributes            Additional attributes such as ttl, hash and priority.
     * @return Returns an  response CloudEvent Response for the use case of RPC Response message that failed.
     */
    static CloudEvent failedResponse(String applicationUriForRPC,
                                     String serviceMethodUri,
                                     String requestId,
                                     Integer communicationStatus,
                                     UCloudEventAttributes attributes) {
        String id = generateCloudEventId();
        final Any protoPayload = Any.pack(Empty.getDefaultInstance());
        return buildBaseCloudEvent(id, serviceMethodUri, protoPayload.toByteArray(), protoPayload.getTypeUrl(), attributes)
                .withType(UCloudEventType.RESPONSE.type())
                .withExtension("sink", URI.create(applicationUriForRPC))
                .withExtension("reqid", requestId)
                .withExtension("commstatus", communicationStatus)
                .build();
    }

    /**
     * Create a CloudEvent for an event for the use case of: Publish generic message.
     *
     * @param source The  uri of the topic being published.
     * @param protoPayload protobuf Any object with the Message to be published.
     * @param attributes Additional attributes such as ttl, hash and priority.
     * @return Returns a publish CloudEvent.
     */
    static CloudEvent publish(String source, Any protoPayload, UCloudEventAttributes attributes) {
        String id = generateCloudEventId();
        return buildBaseCloudEvent(id, source, protoPayload.toByteArray(), protoPayload.getTypeUrl(), attributes )
                .withType(UCloudEventType.PUBLISH.type())
                .build();
    }

    /**
     * Create a CloudEvent for an event for the use case of: Publish a notification message.<br>
     * A published event containing the sink (destination) is often referred to as a notification, it is an event sent to a specific consumer.
     *
     * @param source        The  uri of the topic being published.
     * @param sink          The  uri of the destination of this notification.
     * @param protoPayload  protobuf Any object with the Message to be published.
     * @param attributes    Additional attributes such as ttl, hash and priority.
     * @return Returns a publish CloudEvent.
     */
    static CloudEvent notification(String source, String sink, Any protoPayload, UCloudEventAttributes attributes) {
        String id = generateCloudEventId();
        return buildBaseCloudEvent(id, source, protoPayload.toByteArray(), protoPayload.getTypeUrl(), attributes )
                .withType(UCloudEventType.PUBLISH.type())
                .withExtension("sink", URI.create(sink))
                .build();
    }

    /**
     * @return Returns a UUIDv8 id.
     */
    static String generateCloudEventId() {
        UUID uuid = UUIDFactory.Factories.UPROTOCOL.factory().create();
        return uuid.toString();
    }

    /**
     * Base CloudEvent builder that is the same for all CloudEvent types.
     *
     * @param id                 Event unique identifier.
     * @param source             Identifies who is sending this event in the format of a uProtocol URI that
     *                           can be built from a {@link UUri} object using
     *                           the {@link UriFactory}
     * @param protoPayloadBytes  The serialized Event data with the content type of "application/x-protobuf".
     * @param protoPayloadSchema The schema of the proto payload bytes, for example you can use <code>protoPayload.getTypeUrl()</code> on your service/app object.
     * @param attributes        Additional cloud event attributes that can be passed in. All attributes are optional and will be added only if they
     *                           were configured.
     * @return Returns a CloudEventBuilder that can be additionally configured and then by calling .build() construct a CloudEvent
     * ready to be serialized and sent to the transport layer.
     */
    static CloudEventBuilder buildBaseCloudEvent(String id, String source,
                                                 byte[] protoPayloadBytes,
                                                 String protoPayloadSchema,
                                                 UCloudEventAttributes attributes) {
        final CloudEventBuilder cloudEventBuilder = CloudEventBuilder.v1()
                .withId(id)
                .withSource(URI.create(source))
                /* Not needed:
                .withDataContentType(PROTOBUF_CONTENT_TYPE)
                .withDataSchema(URI.create(protoPayloadSchema))
                */
                .withData(protoPayloadBytes);

        attributes.ttl().ifPresent(ttl -> cloudEventBuilder.withExtension("ttl", ttl));
        attributes.priority().ifPresent(priority -> cloudEventBuilder.withExtension("priority", priority.qosString()));
        attributes.hash().ifPresent(hash -> cloudEventBuilder.withExtension("hash", hash));
        attributes.token().ifPresent(token -> cloudEventBuilder.withExtension("token", token));

        return cloudEventBuilder;
    }
}
