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
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.InvalidProtocolBufferException;

import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventData;
import io.cloudevents.core.builder.CloudEventBuilder;

import org.eclipse.uprotocol.Uoptions;
import org.eclipse.uprotocol.uri.serializer.UriSerializer;
import org.eclipse.uprotocol.uuid.factory.UuidUtils;
import org.eclipse.uprotocol.uuid.serializer.UuidSerializer;

import org.eclipse.uprotocol.v1.UAttributes;
import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UPayloadFormat;
import org.eclipse.uprotocol.v1.UPriority;
import org.eclipse.uprotocol.v1.UMessageType;
import org.eclipse.uprotocol.v1.UUID;
import org.eclipse.uprotocol.v1.UMessage;

/**
 * Class to extract information from a CloudEvent.
 */
public interface UCloudEvent {

    /**
     * Extract the source from a cloud event. The source is a mandatory attribute.
     * The CloudEvent constructor does not allow creating a cloud event without a
     * source.
     * 
     * @param cloudEvent CloudEvent with source to be extracted.
     * @return Returns the String value of a CloudEvent source attribute.
     */
    static String getSource(CloudEvent cloudEvent) {
        return cloudEvent.getSource().toString();
    }

    /**
     * Extract the sink from a cloud event. The sink attribute is optional.
     * 
     * @param cloudEvent CloudEvent with sink to be extracted.
     * @return Returns an Optional String value of a CloudEvent sink attribute if it
     *         exists,
     *         otherwise an Optional.empty() is returned.
     */
    static Optional<String> getSink(CloudEvent cloudEvent) {
        return extractStringValueFromExtension("sink", cloudEvent);
    }

    /**
     * Extract the request id from a cloud event that is a response RPC CloudEvent.
     * The attribute is optional.
     * 
     * @param cloudEvent the response RPC CloudEvent with request id to be
     *                   extracted.
     * @return Returns an Optional String value of a response RPC CloudEvent request
     *         id attribute if it exists,
     *         otherwise an Optional.empty() is returned.
     */
    static Optional<String> getRequestId(CloudEvent cloudEvent) {
        return extractStringValueFromExtension("reqid", cloudEvent);
    }

    /**
     * Extract the hash attribute from a cloud event. The hash attribute is
     * optional.
     * 
     * @param cloudEvent CloudEvent with hash to be extracted.
     * @return Returns an Optional String value of a CloudEvent hash attribute if it
     *         exists,
     *         otherwise an Optional.empty() is returned.
     */
    static Optional<String> getHash(CloudEvent cloudEvent) {
        return extractStringValueFromExtension("hash", cloudEvent);
    }

    /**
     * Extract the string value of the priority attribute from a cloud event. The
     * priority attribute is optional.
     * 
     * @param cloudEvent CloudEvent with priority to be extracted.
     * @return Returns an Optional String value of a CloudEvent priority attribute
     *         if it exists,
     *         otherwise an Optional.empty() is returned.
     */
    static Optional<String> getPriority(CloudEvent cloudEvent) {
        return extractStringValueFromExtension("priority", cloudEvent);
    }

    /**
     * Extract the integer value of the ttl attribute from a cloud event. The ttl
     * attribute is optional.
     * 
     * @param cloudEvent CloudEvent with ttl to be extracted.
     * @return Returns an Optional String value of a CloudEvent ttl attribute if it
     *         exists,
     *         otherwise an Optional.empty() is returned.
     */
    static Optional<Integer> getTtl(CloudEvent cloudEvent) {
        return extractStringValueFromExtension("ttl", cloudEvent)
                .map(Integer::valueOf);
    }

    /**
     * Extract the string value of the token attribute from a cloud event. The token
     * attribute is optional.
     * 
     * @param cloudEvent CloudEvent with token to be extracted.
     * @return Returns an Optional String value of a CloudEvent priority token if it
     *         exists,
     *         otherwise an Optional.empty() is returned.
     */
    static Optional<String> getToken(CloudEvent cloudEvent) {
        return extractStringValueFromExtension("token", cloudEvent);
    }

    /**
     * Extract the string value of the trafceparent attribute from a cloud event.
     * The traceparent attribute is optional.
     * 
     * @param cloudEvent CloudEvent with traceparent to be extracted.
     * @return Returns an Optional String value of a CloudEvent traceparent if it
     *         exists,
     *         otherwise an Optional.empty() is returned.
     */
    static Optional<String> getTraceparent(CloudEvent cloudEvent) {
        return extractStringValueFromExtension("traceparent", cloudEvent);
    }

    /**
     * Fetch the UCode from the CloudEvent commstatus integer value. The
     * communication status attribute is optional.
     * If there was a platform communication error that occurred while delivering
     * this cloudEvent, it will be indicated in this attribute.
     * If the attribute does not exist, it is assumed that everything was
     * UCode.OK_VALUE. <br>
     * If the attribute exists but is not a valid integer, we return UCode.OK_VALUE
     * as we cannot determine that there was in fact a communication
     * status error or not
     * 
     * @param cloudEvent CloudEvent with the platformError to be extracted.
     * @return Returns a UCode that indicates of a platform communication error
     *         while delivering this CloudEvent or UCode.OK.
     */
    static UCode getCommunicationStatus(CloudEvent cloudEvent) {
        try {
            return UCode.forNumber(extractIntegerValueFromExtension("commstatus", cloudEvent).orElse(UCode.OK_VALUE));
        } catch (Exception e) {
            return UCode.OK;
        }
    }

    /**
     * Indication of a platform communication error that occurred while trying to
     * deliver the CloudEvent.
     * 
     * @param cloudEvent CloudEvent to be queried for a platform delivery error.
     * @return returns true if the provided CloudEvent is marked with having a
     *         platform delivery problem.
     */
    static boolean hasCommunicationStatusProblem(CloudEvent cloudEvent) {
        return getCommunicationStatus(cloudEvent) != UCode.OK;
    }

    /**
     * Returns a new CloudEvent from the supplied CloudEvent, with the platform
     * communication added.
     * 
     * @param cloudEvent          CloudEvent that the platform delivery error will
     *                            be added.
     * @param communicationStatus the platform delivery error Code to add to the
     *                            CloudEvent.
     * @return Returns a new CloudEvent from the supplied CloudEvent, with the
     *         platform communication added.
     */
    static CloudEvent addCommunicationStatus(CloudEvent cloudEvent, Integer communicationStatus) {
        if (communicationStatus == null) {
            return cloudEvent;
        }
        CloudEventBuilder builder = CloudEventBuilder.v1(cloudEvent);
        builder.withExtension("commstatus", communicationStatus);
        return builder.build();
    }

    /**
     * Extract the timestamp from the UUIDV8 CloudEvent Id, with Unix epoch as the
     * 
     * @param cloudEvent The CloudEvent with the timestamp to extract.
     * @return Return the timestamp from the UUIDV8 CloudEvent Id or an empty
     *         Optional if timestamp can't be extracted.
     */
    static Optional<Long> getCreationTimestamp(CloudEvent cloudEvent) {
        final String cloudEventId = cloudEvent.getId();
        final UUID uuid = UuidSerializer.deserialize(cloudEventId);

        return UuidUtils.getTime(uuid);
    }

    /**
     * Calculate if a CloudEvent configured with a creation time and a ttl attribute
     * is expired.<br>
     * The ttl attribute is a configuration of how long this event should live for
     * after it was generated (in milliseconds)
     * 
     * @param cloudEvent The CloudEvent to inspect for being expired.
     * @return Returns true if the CloudEvent was configured with a ttl &gt; 0 and a
     *         creation time to compare for expiration.
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
        if (cloudEventCreationTime == null) {
            return false;
        }
        final OffsetDateTime now = OffsetDateTime.now();
        final OffsetDateTime creationTimePlusTtl = cloudEventCreationTime.plus(ttl, ChronoUnit.MILLIS);

        return now.isAfter(creationTimePlusTtl);
    }

    /**
     * Calculate if a CloudEvent configured with UUIDv8 id and a ttl attribute is
     * expired.<br>
     * The ttl attribute is a configuration of how long this event should live for
     * after it was generated (in milliseconds)
     * 
     * @param cloudEvent The CloudEvent to inspect for being expired.
     * @return Returns true if the CloudEvent was configured with a ttl &gt; 0 and
     *         UUIDv8 id to compare for expiration.
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
        final UUID uuid = UuidSerializer.deserialize(cloudEventId);

        if (uuid.equals(UUID.getDefaultInstance())) {
            return false;
        }

        long delta = System.currentTimeMillis() - UuidUtils.getTime(uuid).orElse(0L);

        return delta >= ttl;
    }

    /**
     * Check if a CloudEvent is a valid UUIDv6 or v8 .
     * 
     * @param cloudEvent The CloudEvent with the id to inspect.
     * @return Returns true if the CloudEvent is valid.
     */
    static boolean isCloudEventId(CloudEvent cloudEvent) {
        final String cloudEventId = cloudEvent.getId();
        final UUID uuid = UuidSerializer.deserialize(cloudEventId);

        return UuidUtils.isUuid(uuid);
    }

    /**
     * Extract the payload from the CloudEvent as a protobuf Any object. <br>
     * An all or nothing error handling strategy is implemented. If anything goes
     * wrong, an Any.getDefaultInstance() will be returned.
     * 
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
     * Function used to pretty print a CloudEvent containing only the id, source,
     * type and maybe a sink. Used mainly for logging.
     * 
     * @param cloudEvent The CloudEvent we want to pretty print.
     * @return returns the String representation of the CloudEvent containing only
     *         the id, source, type and maybe a sink.
     */
    static String toString(CloudEvent cloudEvent) {
        return (cloudEvent != null) ? "CloudEvent{id='" + cloudEvent.getId() +
                "', source='" + cloudEvent.getSource() + "'" +
                getSink(cloudEvent).map(sink -> String.format(", sink='%s'", sink)).orElse("") +
                ", type='" + cloudEvent.getType() + "'}" : "null";
    }

    /**
     * Utility for extracting the String value of an extension.
     * 
     * @param extensionName The name of the CloudEvent extension.
     * @param cloudEvent    The CloudEvent containing the data.
     * @return returns the Optional String value of the extension matching the
     *         extension name,
     *         or an Optional.empty() is the value does not exist.
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
     * 
     * @param extensionName The name of the CloudEvent extension.
     * @param cloudEvent    The CloudEvent containing the data.
     * @return returns the Optional Integer value of the extension matching the
     *         extension name,
     *         or an Optional.empty() is the value does not exist.
     */
    private static Optional<Integer> extractIntegerValueFromExtension(String extensionName, CloudEvent cloudEvent) {
        return extractStringValueFromExtension(extensionName, cloudEvent)
                .map(Integer::valueOf);
    }

    /**
     * Get the string representation of the UMessageType.
     * 
     * Note: The UMessageType is determined by the type of the CloudEvent. If
     * the UMessageType is UMESSAGE_TYPE_NOTIFICATION, we assume the CloudEvent type
     * is "pub.v1" and the sink is present.
     * 
     * @param type The UMessageType
     * @return returns the string representation of the UMessageType
     * 
     */
    static String getEventType(UMessageType type) {
        return getCeName(type.getValueDescriptor());
    }

    /**
     * Get the string representation of the UPriority
     * 
     * @param priority
     * @return returns the string representation of the UPriority
     */
    static String getCePriority(UPriority priority) {
        return getCeName(priority.getValueDescriptor());
    }

    /**
     * Get the UMessageType from the string representation.
     * 
     * Note: The UMessageType is determined by the type of the CloudEvent.
     * If the CloudEvent type is "pub.v1" and the sink is present, the UMessageType
     * is assumed to be
     * UMESSAGE_TYPE_NOTIFICATION, this is because uProtocol CloudEvent definition
     * did not have an explicit
     * notification type.
     * 
     * @param cloudEvent The CloudEvent containing the data.
     * 
     * @return returns the UMessageType
     */
    static UMessageType getMessageType(String ceType) {
        return UMessageType.getDescriptor().getValues().stream()
                .filter(v -> v.getOptions().hasExtension(Uoptions.ceName) &&
                        v.getOptions().getExtension(Uoptions.ceName).equals(ceType))
                .map(v -> UMessageType.forNumber(v.getNumber()))
                .findFirst()
                .orElse(UMessageType.UNRECOGNIZED);
    }

    /**
     * Get the UMessage from the cloud event
     * 
     * @param event The CloudEvent containing the data.
     * @return returns the UMessage
     */
    static UMessage toMessage(CloudEvent event) {
        Objects.requireNonNull(event);

        UAttributes.Builder builder = UAttributes.newBuilder()
                .setSource(UriSerializer.deserialize(getSource(event)))
                .setId(UuidSerializer.deserialize(event.getId()))
                .setType(getMessageType(event.getType()));

        if (event.getData() != null) {
            builder.setPayloadFormat(getUPayloadFormatFromContentType(event.getDataContentType()));
        }

        if (hasCommunicationStatusProblem(event)) {
            builder.setCommstatus(getCommunicationStatus(event));
        }
        getPriority(event).map(p -> UPriority.getDescriptor().getValues().stream()
                .filter(v -> v.getOptions().hasExtension(Uoptions.ceName) &&
                        v.getOptions().getExtension(Uoptions.ceName).equals(p))
                .map(v -> UPriority.forNumber(v.getNumber()))
                .findFirst()
                .orElse(UPriority.UPRIORITY_UNSPECIFIED))
                .ifPresent(builder::setPriority);

        getSink(event).map(UriSerializer::deserialize).ifPresent(builder::setSink);

        getRequestId(event).map(UuidSerializer::deserialize).ifPresent(builder::setReqid);

        getTtl(event).ifPresent(builder::setTtl);

        getToken(event).ifPresent(builder::setToken);

        getTraceparent(event).ifPresent(builder::setTraceparent);

        Optional<Integer> permissionLevel = extractIntegerValueFromExtension("plevel", event);
        permissionLevel.ifPresent(builder::setPermissionLevel);

        UMessage.Builder messageBuilder = UMessage.newBuilder().setAttributes(builder);

        // Set the data payload if it is present
        Optional.ofNullable(event.getData())
                .stream()
                .map(CloudEventData::toBytes)
                .map(ByteString::copyFrom)
                .findFirst()
                .ifPresent(messageBuilder::setPayload);

        return messageBuilder.build();
    }

    /**
     * Get the Cloudevent from the UMessage<br>
     * <b>Note: For now, only the value format of UPayload is supported in the
     * SDK.If the UPayload has a reference, it
     * needs to be copied to CloudEvent.</b>
     * 
     * @param message The UMessage protobuf containing the data
     * @return returns the cloud event
     */
    static CloudEvent fromMessage(UMessage message) {
        Objects.requireNonNull(message, "message cannot be null.");

        UAttributes attributes = Objects.requireNonNullElse(message.getAttributes(), UAttributes.getDefaultInstance());

        CloudEventBuilder cloudEventBuilder = CloudEventBuilder.v1()
                .withId(UuidSerializer.serialize(attributes.getId()));

        cloudEventBuilder.withType(getEventType(attributes.getType()));

        cloudEventBuilder.withSource(URI.create(UriSerializer.serialize(attributes.getSource())));

        if (!message.getPayload().isEmpty()) {
            cloudEventBuilder.withData(message.getPayload().toByteArray());
        }

        final String contentType = getContentTypeFromUPayloadFormat(message.getAttributes().getPayloadFormat());
        if (!contentType.isEmpty()) {
            cloudEventBuilder.withDataContentType(contentType);
        }

        if (attributes.hasTtl())
            cloudEventBuilder.withExtension("ttl", attributes.getTtl());

        if (attributes.hasToken())
            cloudEventBuilder.withExtension("token", attributes.getToken());

        if (attributes.getPriorityValue() > 0)
            cloudEventBuilder.withExtension("priority", UCloudEvent.getCePriority(attributes.getPriority()));

        if (attributes.hasSink())
            cloudEventBuilder.withExtension("sink",
                    URI.create(UriSerializer.serialize(attributes.getSink())));

        if (attributes.hasCommstatus())
            cloudEventBuilder.withExtension("commstatus", attributes.getCommstatus().getNumber());

        if (attributes.hasReqid())
            cloudEventBuilder.withExtension("reqid", UuidSerializer.serialize(attributes.getReqid()));

        if (attributes.hasPermissionLevel())
            cloudEventBuilder.withExtension("plevel", attributes.getPermissionLevel());

        if (attributes.hasTraceparent())
            cloudEventBuilder.withExtension("traceparent", attributes.getTraceparent());

        return cloudEventBuilder.build();

    }

    /**
     * Retrieves the payload format enumeration based on the provided string
     * representation of the data content type <br>
     * This method uses the uProtocol mimeType custom options declared in
     * upayload.proto.
     *
     * @param contentType The content type string representing the format of the
     *                    payload.
     * @return The corresponding UPayloadFormat enumeration based on the content
     *         type.
     */
    static UPayloadFormat getUPayloadFormatFromContentType(String contentType) {
        return UPayloadFormat.getDescriptor().getValues().stream()
                .filter(v -> v.getOptions().hasExtension(Uoptions.mimeType) &&
                        v.getOptions().getExtension(Uoptions.mimeType).equals(contentType))
                .map(v -> UPayloadFormat.forNumber(v.getNumber()))
                .findFirst()
                .orElse(UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF_WRAPPED_IN_ANY);
    }

    /**
     * Retrieves the string representation of the data content type based on the
     * provided UPayloadFormat. <BR>
     * This method uses the uProtocol mimeType custom options declared in
     * upayload.proto.
     *
     * @param format The UPayloadFormat enumeration representing the payload format.
     * @return The corresponding content type string based on the payload format.
     */
    static String getContentTypeFromUPayloadFormat(UPayloadFormat format) {
        // Since the default value is UPAYLOAD_FORMAT_PROTOBUF_WRAPPED_IN_ANY, we return
        // an empty string.
        if (format == UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF_WRAPPED_IN_ANY) {
            return "";
        }
        return format.getValueDescriptor().getOptions().<String>getExtension(Uoptions.mimeType);
    }

    /**
     * Retrieves the string representation of the data content type based on the
     * provided Enum value descriptor. <BR>
     *
     * @param descriptor The EnumDescriptor enumeration representing the payload
     *                   format.
     * @return The corresponding string name for the value.
     */
    static String getCeName(EnumValueDescriptor descriptor) {
        return descriptor.getOptions().<String>getExtension(Uoptions.ceName);
    }

}
