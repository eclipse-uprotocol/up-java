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
package org.eclipse.uprotocol.cloudevent.factory;

import java.net.URI;

import com.google.protobuf.Any;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;

import org.eclipse.uprotocol.cloudevent.datamodel.UCloudEventAttributes;
import org.eclipse.uprotocol.uuid.factory.UuidFactory;
import org.eclipse.uprotocol.uuid.serializer.UuidSerializer;
import org.eclipse.uprotocol.v1.UMessageType;
import org.eclipse.uprotocol.v1.UUID;
import org.eclipse.uprotocol.v1.UUri;

/**
 * A factory is a part of the software has methods to generate concrete objects,
 * usually of the same type or interface.<br>
 * CloudEvents is a specification for describing events in a common way. We will
 * use CloudEvents
 * to formulate all kinds of events (messages) that will be sent to and from
 * devices.<br>
 * The CloudEvent factory knows how to generate CloudEvents of the 4 core types:
 * req.v1, res.v1, pub.v1, and file.v1<br>
 */
public interface CloudEventFactory {

    String PROTOBUF_CONTENT_TYPE = "application/x-protobuf";

    /**
     * Create a CloudEvent for an event for the use case of: RPC Request message.
     *
     * @param applicationUriForRPC The uri for the application requesting the RPC.
     * @param serviceMethodUri     The uri for the method to be called on the
     *                             service Ex.: :/body.access/1/rpc.UpdateDoor
     * @param protoPayload         Protobuf Any object with the Message command to
     *                             be executed on the sink service.
     * @param attributes           Additional attributes such as ttl, hash, priority
     *                             and token.
     * @return Returns an request CloudEvent.
     */
    static CloudEvent request(String applicationUriForRPC,
            String serviceMethodUri,
            Any protoPayload,
            UCloudEventAttributes attributes) {
        String id = generateCloudEventId();
        return buildBaseCloudEvent(id, applicationUriForRPC, protoPayload, attributes)
                .withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_REQUEST))
                .withExtension("sink", URI.create(serviceMethodUri))
                .build();
    }

    /**
     * Create a CloudEvent for an event for the use case of: RPC Response message.
     *
     * @param applicationUriForRPC The destination of the response. The uri for the
     *                             original application that requested the RPC and
     *                             this response is for.
     * @param serviceMethodUri     The uri for the method that was called on the
     *                             service Ex.: :/body.access/1/rpc.UpdateDoor
     * @param requestId            The cloud event id from the original request
     *                             cloud event that this response if for.
     * @param protoPayload         The protobuf serialized response message as
     *                             defined by the application interface or the
     *                             google.rpc.Status message containing the details
     *                             of an error.
     * @param attributes           Additional attributes such as ttl, hash and
     *                             priority.
     * @return Returns an response CloudEvent.
     */
    static CloudEvent response(String applicationUriForRPC,
            String serviceMethodUri,
            String requestId,
            Any protoPayload,
            UCloudEventAttributes attributes) {
        String id = generateCloudEventId();
        return buildBaseCloudEvent(id, serviceMethodUri, protoPayload, attributes)
                .withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_RESPONSE))
                .withExtension("sink", URI.create(applicationUriForRPC))
                .withExtension("reqid", requestId)
                .build();
    }

    /**
     * Create a CloudEvent for an event for the use case of: RPC Response message
     * that failed.
     *
     * @param applicationUriForRPC The destination of the response. The uri for the
     *                             original application that requested the RPC and
     *                             this response is for.
     * @param serviceMethodUri     The uri for the method that was called on the
     *                             service Ex.: :/body.access/1/rpc.UpdateDoor
     * @param requestId            The cloud event id from the original request
     *                             cloud event that this response if for.
     * @param communicationStatus  A {@link Code} value that indicates of a platform
     *                             communication error while delivering this
     *                             CloudEvent.
     * @param attributes           Additional attributes such as ttl, hash and
     *                             priority.
     * @return Returns an response CloudEvent Response for the use case of RPC
     *         Response message that failed.
     */
    static CloudEvent failedResponse(String applicationUriForRPC,
            String serviceMethodUri,
            String requestId,
            Integer communicationStatus,
            UCloudEventAttributes attributes) {
        String id = generateCloudEventId();

        return buildBaseCloudEvent(id, serviceMethodUri, null, attributes)
                .withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_RESPONSE))
                .withExtension("sink", URI.create(applicationUriForRPC))
                .withExtension("reqid", requestId)
                .withExtension("commstatus", communicationStatus)
                .build();
    }

    /**
     * Create a CloudEvent for an event for the use case of: Publish generic
     * message.
     *
     * @param source       The uri of the topic being published.
     * @param protoPayload protobuf Any object with the Message to be published.
     * @param attributes   Additional attributes such as ttl, hash and priority.
     * @return Returns a publish CloudEvent.
     */
    static CloudEvent publish(String source, Any protoPayload, UCloudEventAttributes attributes) {
        String id = generateCloudEventId();
        return buildBaseCloudEvent(id, source, protoPayload, attributes)
                .withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_PUBLISH))
                .build();
    }

    /**
     * Create a CloudEvent for an event for the use case of: Publish a notification
     * message.<br>
     * A published event containing the sink (destination) is often referred to as a
     * notification, it is an event sent to a specific consumer.
     *
     * @param source       The uri of the topic being published.
     * @param sink         The uri of the destination of this notification.
     * @param protoPayload protobuf Any object with the Message to be published.
     * @param attributes   Additional attributes such as ttl, hash and priority.
     * @return Returns a publish CloudEvent.
     */
    static CloudEvent notification(String source, String sink, Any protoPayload, UCloudEventAttributes attributes) {
        String id = generateCloudEventId();
        return buildBaseCloudEvent(id, source, protoPayload, attributes)
                .withType(UCloudEvent.getEventType(UMessageType.UMESSAGE_TYPE_NOTIFICATION))
                .withExtension("sink", URI.create(sink))
                .build();
    }

    /**
     * @return Returns a UUIDv8 id.
     */
    static String generateCloudEventId() {
        UUID uuid = UuidFactory.Factories.UPROTOCOL.factory().create();
        return UuidSerializer.serialize(uuid);
    }

    /**
     * Base CloudEvent builder that is the same for all CloudEvent types.
     *
     * @param id           Event unique identifier.
     * @param source       Identifies who is sending this event in the format of a
     *                     uProtocol URI that
     *                     can be built from a {@link UUri} object.
     * @param protoPayload Optional payload for the message
     * @param attributes   Additional cloud event attributes that can be passed in.
     *                     All attributes are optional and will be added only if
     *                     they
     *                     were configured.
     * @return Returns a CloudEventBuilder that can be additionally configured and
     *         then by calling .build() construct a CloudEvent
     *         ready to be serialized and sent to the transport layer.
     */
    static CloudEventBuilder buildBaseCloudEvent(String id, String source,
            Any protoPayload,
            UCloudEventAttributes attributes) {
        CloudEventBuilder cloudEventBuilder = CloudEventBuilder.v1()
                .withId(id)
                .withSource(URI.create(source));

        if (protoPayload != null) {
            cloudEventBuilder.withData(protoPayload.toByteArray());
        }

        attributes.ttl().ifPresent(ttl -> cloudEventBuilder.withExtension("ttl", ttl));
        attributes.priority().ifPresent(priority -> cloudEventBuilder.withExtension("priority",
                UCloudEvent.getCeName(priority.getValueDescriptor())));
        attributes.hash().ifPresent(hash -> cloudEventBuilder.withExtension("hash", hash));
        attributes.token().ifPresent(token -> cloudEventBuilder.withExtension("token", token));
        attributes.traceparent().ifPresent(traceparent -> cloudEventBuilder.withExtension("traceparent", traceparent));

        return cloudEventBuilder;
    }
}
