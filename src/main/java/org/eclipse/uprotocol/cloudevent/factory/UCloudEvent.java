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

import org.eclipse.uprotocol.uri.serializer.LongUriSerializer;
import org.eclipse.uprotocol.uuid.factory.UuidUtils;
import org.eclipse.uprotocol.uuid.serializer.LongUuidSerializer;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventData;
import io.cloudevents.core.builder.CloudEventBuilder;
import org.eclipse.uprotocol.v1.*;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Class to extract  information from a CloudEvent.
 */
public interface UCloudEvent {

    /**
     * Extract the source from a cloud event. The source is a mandatory attribute.
     * The CloudEvent constructor does not allow creating a cloud event without a source.
     * @param cloudEvent CloudEvent with source to be extracted.
     * @return Returns the String value of a CloudEvent source attribute.
     */
    static String getSource(CloudEvent cloudEvent) {
        return cloudEvent.getSource().toString();
    }

    /**
     * Extract the sink from a cloud event. The sink attribute is optional.
     * @param cloudEvent CloudEvent with sink to be extracted.
     * @return Returns an Optional String value of a CloudEvent sink attribute if it exists,
     *      otherwise an Optional.empty() is returned.
     */
    static Optional<String> getSink(CloudEvent cloudEvent) {
        return extractStringValueFromExtension("sink", cloudEvent);
    }

    /**
     * Extract the request id from a cloud event that is a response RPC CloudEvent. The attribute is optional.
     * @param cloudEvent the response RPC CloudEvent with request id to be extracted.
     * @return Returns an Optional String value of a response RPC CloudEvent request id attribute if it exists,
     *      otherwise an Optional.empty() is returned.
     */
    static Optional<String> getRequestId(CloudEvent cloudEvent) {
        return extractStringValueFromExtension("reqid", cloudEvent);
    }

    /**
     * Extract the hash attribute from a cloud event. The hash attribute is optional.
     * @param cloudEvent CloudEvent with hash to be extracted.
     * @return Returns an Optional String value of a CloudEvent hash attribute if it exists,
     *      otherwise an Optional.empty() is returned.
     */
    static Optional<String> getHash(CloudEvent cloudEvent) {
        return extractStringValueFromExtension("hash", cloudEvent);
    }

    /**
     * Extract the string value of the priority attribute from a cloud event. The priority attribute is optional.
     * @param cloudEvent CloudEvent with priority to be extracted.
     * @return Returns an Optional String value of a CloudEvent priority attribute if it exists,
     *      otherwise an Optional.empty() is returned.
     */
    static Optional<String> getPriority(CloudEvent cloudEvent) {
        return extractStringValueFromExtension("priority", cloudEvent);
    }

    /**
     * Extract the integer value of the ttl attribute from a cloud event. The ttl attribute is optional.
     * @param cloudEvent CloudEvent with ttl to be extracted.
     * @return Returns an Optional String value of a CloudEvent ttl attribute if it exists,
     *      otherwise an Optional.empty() is returned.
     */
    static Optional<Integer> getTtl(CloudEvent cloudEvent) {
        return extractStringValueFromExtension("ttl", cloudEvent)
                .map(Integer::valueOf);
    }

    /**
     * Extract the string value of the token attribute from a cloud event. The token attribute is optional.
     * @param cloudEvent CloudEvent with token to be extracted.
     * @return Returns an Optional String value of a CloudEvent priority token if it exists,
     *      otherwise an Optional.empty() is returned.
     */
    static Optional<String> getToken(CloudEvent cloudEvent) {
        return extractStringValueFromExtension("token", cloudEvent);
    }

    /**
     * Extract the integer value of the communication status attribute from a cloud event. The communication status attribute is optional.
     * If there was a platform communication error that occurred while delivering this cloudEvent, it will be indicated in this attribute.
     * If the attribute does not exist, it is assumed that everything was UCode.OK_VALUE. <br>
     * If the attribute exists but is not a valid integer, we return UCode.OK_VALUE as we cannot determine that there was in fact a communication
     * status error or not
     * @param cloudEvent CloudEvent with the platformError to be extracted.
     * @return Returns a UCode value that indicates of a platform communication error while delivering this CloudEvent or UCode.OK_VALUE.
     */
    static Integer getCommunicationStatus(CloudEvent cloudEvent) {
        try {
            return extractIntegerValueFromExtension("commstatus", cloudEvent)
                    .orElse(UCode.OK_VALUE);
        } catch (Exception e) {
            return UCode.OK_VALUE;
        }
    }

    /**
     * Indication of a platform communication error that occurred while trying to deliver the CloudEvent.
     * @param cloudEvent CloudEvent to be queried for a platform delivery error.
     * @return returns true if the provided CloudEvent is marked with having a platform delivery problem.
     */
    static boolean hasCommunicationStatusProblem(CloudEvent cloudEvent) {
        return getCommunicationStatus(cloudEvent) != UCode.OK_VALUE;
    }

    /**
     * Returns a new CloudEvent from the supplied CloudEvent, with the platform communication added.
     * @param cloudEvent CloudEvent that the platform delivery error will be added.
     * @param communicationStatus the platform delivery error Code to add to the CloudEvent.
     * @return Returns a new CloudEvent from the supplied CloudEvent, with the platform communication added.
     */
    static CloudEvent addCommunicationStatus(CloudEvent cloudEvent, Integer communicationStatus) {
        if(communicationStatus == null) {
            return cloudEvent;
        }
        CloudEventBuilder builder = CloudEventBuilder.v1(cloudEvent);
        builder.withExtension("commstatus", communicationStatus);
        return builder.build();
    }

    /**
     * Extract the timestamp from the UUIDV8 CloudEvent Id, with Unix epoch as the
     * @param cloudEvent The CloudEvent with the timestamp to extract.
     * @return Return the timestamp from the UUIDV8 CloudEvent Id or an empty Optional if timestamp can't be extracted.
     */
    static Optional<Long> getCreationTimestamp(CloudEvent cloudEvent) {
        final String cloudEventId = cloudEvent.getId();
        final UUID uuid = LongUuidSerializer.instance().deserialize(cloudEventId);

        return UuidUtils.getTime(uuid);
    }

    /**
     * Calculate if a CloudEvent configured with a creation time and a ttl attribute is expired.<br>
     * The ttl attribute is a configuration of how long this event should live for after it was generated (in milliseconds)
     * @param cloudEvent The CloudEvent to inspect for being expired.
     * @return Returns true if the CloudEvent was configured with a ttl &gt; 0 and a creation time to compare for expiration.
     */
    static boolean isExpiredByCloudEventCreationDate(CloudEvent cloudEvent) {
        final Optional<Integer> maybeTtl = getTtl(cloudEvent);
        if (maybeTtl.isEmpty()) {
            return false;
        }
        int ttl = maybeTtl.get();
        if (ttl <= 0) {
            return false;
        }
        final OffsetDateTime cloudEventCreationTime = cloudEvent.getTime();
        if (cloudEventCreationTime == null){
            return false;
        }
        final OffsetDateTime now = OffsetDateTime.now();
        final OffsetDateTime creationTimePlusTtl = cloudEventCreationTime.plus(ttl, ChronoUnit.MILLIS);

        return now.isAfter(creationTimePlusTtl);
    }

    /**
     * Calculate if a CloudEvent configured with UUIDv8 id and a ttl attribute is expired.<br>
     * The ttl attribute is a configuration of how long this event should live for after it was generated (in milliseconds)
     * @param cloudEvent The CloudEvent to inspect for being expired.
     * @return Returns true if the CloudEvent was configured with a ttl &gt; 0 and UUIDv8 id to compare for expiration.
     */
    static boolean isExpired(CloudEvent cloudEvent) {
        final Optional<Integer> maybeTtl = getTtl(cloudEvent);
        if (maybeTtl.isEmpty()) {
            return false;
        }
        int ttl = maybeTtl.get();
        if (ttl <= 0) {
            return false;
        }
        final String cloudEventId = cloudEvent.getId();
        final UUID uuid = LongUuidSerializer.instance().deserialize(cloudEventId);

        if (uuid.equals(UUID.getDefaultInstance())) {
            return false;
        }

        long delta = System.currentTimeMillis() - UuidUtils.getTime(uuid).orElse(0L);

        return delta >= ttl;
    }

    /**
     * Check if a CloudEvent is a valid UUIDv6 or v8 .
     * @param cloudEvent The CloudEvent with the id to inspect.
     * @return Returns true if the CloudEvent is valid.
     */
    static boolean isCloudEventId(CloudEvent cloudEvent) {
        final String cloudEventId = cloudEvent.getId();
        final UUID uuid = LongUuidSerializer.instance().deserialize(cloudEventId);

        return UuidUtils.isUuid(uuid);
    }

    /**
     * Extract the payload from the CloudEvent as a protobuf Any object. <br>
     * An all or nothing error handling strategy is implemented. If anything goes wrong, an Any.getDefaultInstance() will be returned.
     * @param cloudEvent CloudEvent containing the payload to extract.
     * @return Extracts the payload from a CloudEvent as a Protobuf Any object.
     */
    static Any getPayload(CloudEvent cloudEvent) {
        final CloudEventData data = cloudEvent.getData();
        if (data == null) {
            return Any.getDefaultInstance();
        }
        try {
            return Any.parseFrom(data.toBytes());
        } catch (InvalidProtocolBufferException e) {
            return Any.getDefaultInstance();
        }
    }

    /**
     * Extract the payload from the CloudEvent as a protobuf Message of the provided class. The protobuf of this message
     * class must be loaded on the client for this to work. <br>
     * An all or nothing error handling strategy is implemented. If anything goes wrong, an empty optional will be returned. <br>
     * Example: <br>
     * <pre>Optional&lt;SomeMessage&gt; unpacked = UCloudEvent.unpack(cloudEvent, SomeMessage.class);</pre>
     * @param cloudEvent CloudEvent containing the payload to extract.
     * @param clazz The class that extends {@link Message} that the payload is extracted into.
     * @return Returns a {@link Message} payload of the class type that is provided.
     * @param <T> The class type of the Message to be unpacked.
     */
    static <T extends Message> Optional<T> unpack(CloudEvent cloudEvent, Class<T> clazz) {
        try {
            return Optional.of(getPayload(cloudEvent).unpack(clazz));
        } catch (InvalidProtocolBufferException e) {
            // All or nothing error handling strategy. If something goes wrong, you just get an empty.
            return Optional.empty();
        }
    }

    /**
     * Function used to pretty print a CloudEvent containing only the id, source, type and maybe a sink. Used mainly for logging.
     * @param cloudEvent The CloudEvent we want to pretty print.
     * @return returns the String representation of the CloudEvent containing only the id, source, type and maybe a sink.
     */
    static String toString(CloudEvent cloudEvent) {
        return (cloudEvent != null) ? "CloudEvent{id='" + cloudEvent.getId() +
                "', source='" + cloudEvent.getSource() + "'" +
                getSink(cloudEvent).map(sink -> String.format(", sink='%s'", sink)).orElse("") +
                ", type='" + cloudEvent.getType() + "'}" : "null";
    }

    /**
     * Utility for extracting the String value of an extension.
     * @param extensionName The name of the CloudEvent extension.
     * @param cloudEvent The CloudEvent containing the data.
     * @return returns the Optional String value of the extension matching the extension name,
     *      or an Optional.empty() is the value does not exist.
     */
    private static Optional<String> extractStringValueFromExtension(String extensionName, CloudEvent cloudEvent) {
        final Set<String> extensionNames = cloudEvent.getExtensionNames();
        if (extensionNames.contains(extensionName)) {
            Object extension = cloudEvent.getExtension(extensionName);
            return extension == null ? Optional.empty() : Optional.of(String.valueOf(extension));
        }
        return Optional.empty();
    }

    /**
     * Utility for extracting the Integer value of an extension.
     * @param extensionName The name of the CloudEvent extension.
     * @param cloudEvent The CloudEvent containing the data.
     * @return returns the Optional Integer value of the extension matching the extension name,
     *      or an Optional.empty() is the value does not exist.
     */
    private static Optional<Integer> extractIntegerValueFromExtension(String extensionName, CloudEvent cloudEvent) {
        return extractStringValueFromExtension(extensionName, cloudEvent)
                .map(Integer::valueOf);
    }


    /**
     * Get the string representation of the UMessageType
     * @param type The UMessageType
     * @return returns the string representation of the UMessageType
     */
    static String getEventType(UMessageType type){
        switch (type){
            case UMESSAGE_TYPE_PUBLISH:
                return "pub.v1";
            case UMESSAGE_TYPE_REQUEST:
                return "req.v1";
            case UMESSAGE_TYPE_RESPONSE:
                return "res.v1";
            default:
                return "";
        }
    }

    /**
     * Get the UMessageType from the string representation
     * @param ce_type The string representation of the UMessageType
     * @return returns the UMessageType
     */
    static UMessageType getMessageType(String ce_type){
        switch (ce_type){
            case "pub.v1":
                return UMessageType.UMESSAGE_TYPE_PUBLISH;
            case "req.v1":
                return UMessageType.UMESSAGE_TYPE_REQUEST;
            case "res.v1":
                return UMessageType.UMESSAGE_TYPE_RESPONSE;
            default:
                return UMessageType.UMESSAGE_TYPE_UNSPECIFIED;
        }
    }
    /**
     * Get the UMessage from the cloud event
     * @param event The CloudEvent containing the data.
     * @return returns the UMessage
     */
    static UMessage toMessage(CloudEvent event) {
        Objects.requireNonNull(event);
        UUri source = LongUriSerializer.instance().deserialize(getSource(event));

        UPayload payload = UPayload.newBuilder().setFormat(UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF)
                .setValue(getPayload(event).toByteString()).build();

        UAttributes.Builder builder =
                UAttributes.newBuilder().setId(LongUuidSerializer.instance().deserialize(event.getId()))
                        .setType(getMessageType(event.getType()));

        if (hasCommunicationStatusProblem(event)) {
            builder.setCommstatus(getCommunicationStatus(event));
        }
        getPriority(event).map(UPriority::valueOf).ifPresent(builder::setPriority);

        getSink(event).map(LongUriSerializer.instance()::deserialize).ifPresent(builder::setSink);

        getRequestId(event).map(LongUuidSerializer.instance()::deserialize).ifPresent(builder::setReqid);

        getTtl(event).ifPresent(builder::setTtl);

        getToken(event).ifPresent(builder::setToken);

        Optional<Integer> permission_level = extractIntegerValueFromExtension("plevel", event);
        permission_level.ifPresent(builder::setPermissionLevel);

        UAttributes attributes = builder.build();

        return UMessage.newBuilder().setAttributes(attributes).setPayload(payload).setSource(source).build();

    }

    /**
     * Get the Cloudevent from the UMessage<br>
     * <b>Note: For now, only the value format of UPayload is supported in the SDK.If the UPayload has a reference, it
     * needs to be copied to CloudEvent.</b>
     * @param message The UMessage protobuf containing the data
     * @return returns the cloud event
     */
    static CloudEvent fromMessage(UMessage message) {
        UAttributes attributes = message.getAttributes();

        CloudEventBuilder cloudEventBuilder =
                CloudEventBuilder.v1().withId(LongUuidSerializer.instance().serialize(attributes.getId()));

        cloudEventBuilder.withType(getEventType(attributes.getType()));

        cloudEventBuilder.withSource(URI.create(LongUriSerializer.instance().serialize(message.getSource())));

        // IMPORTANT: Currently, ONLY the VALUE format is supported in the SDK!
        if (message.getPayload().hasValue())
            cloudEventBuilder.withData(message.getPayload().getValue().toByteArray());

        if (attributes.hasTtl())
            cloudEventBuilder.withExtension("ttl",attributes.getTtl());

        if (attributes.hasToken())
            cloudEventBuilder.withExtension("token",attributes.getToken());

        if(attributes.getPriorityValue()>0)
            cloudEventBuilder.withExtension("priority",String.valueOf(attributes.getPriority()));

        if(attributes.hasSink())
            cloudEventBuilder.withExtension("sink",
                     URI.create(LongUriSerializer.instance().serialize(attributes.getSink())));

        if(attributes.hasCommstatus())
            cloudEventBuilder.withExtension("commstatus",attributes.getCommstatus());

        if(attributes.hasReqid())
            cloudEventBuilder.withExtension("reqid",LongUuidSerializer.instance().serialize(attributes.getReqid()));

        if(attributes.hasPermissionLevel())
            cloudEventBuilder.withExtension("plevel",attributes.getPermissionLevel());
       return cloudEventBuilder.build();

    }



}
