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
package org.eclipse.uprotocol.transport.builder;

import org.eclipse.uprotocol.v1.UUri;
import org.eclipse.uprotocol.validation.ValidationException;
import org.eclipse.uprotocol.v1.UMessage;
import org.eclipse.uprotocol.v1.UAttributes;
import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UPriority;
import org.eclipse.uprotocol.v1.UMessageType;
import org.eclipse.uprotocol.v1.UPayloadFormat;
import org.eclipse.uprotocol.v1.UUID;

import com.google.protobuf.ByteString;

import org.eclipse.uprotocol.communication.UPayload;
import org.eclipse.uprotocol.transport.validator.UAttributesValidator;
import org.eclipse.uprotocol.uuid.factory.UuidFactory;
import org.eclipse.uprotocol.uuid.factory.UuidUtils;

import java.util.Objects;
import java.util.Optional;

/**
 * Builder for easy construction of the UAttributes object.
 */
public final class UMessageBuilder {

    private final UUri source;
    private final UMessageType type;

    private UUID id;
    private UPriority priority;
    private Integer ttl;
    private String token;
    private UUri sink;
    private Integer plevel;
    private UCode commstatus;
    private UUID reqid;
    private String traceparent;

    private UPayloadFormat format;
    private ByteString payload;

    /**
     * Gets a builder for a publish message.
     * <p>
     * A publish message is used to notify all interested consumers of an event that has occurred.
     * Consumers usually indicate their interest by <em>subscribing</em> to a particular topic.
     *
     * @param source The topic to publish the message to.
     * @return The builder.
     * @throws NullPointerException if the source is {@code null}.
     */
    public static UMessageBuilder publish(UUri source) {
        Objects.requireNonNull(source, "source cannot be null.");

        return new UMessageBuilder(
            source,
            UuidFactory.create(), // [impl->dsn~up-attributes-id~1]
            UMessageType.UMESSAGE_TYPE_PUBLISH); // [impl->dsn~up-attributes-publish-type~1]

    }

    /**
     * Gets a builder for a notification message.
     * <p>
     * A notification is used to inform a specific consumer about an event that has occurred.
     *
     * @param source The component that the notification originates from.
     * @param sink   The URI identifying the destination to send the notification to.
     * @return The builder.
     * @throws NullPointerException if the source or sink is {@code null}.
     */
    public static UMessageBuilder notification(UUri source, UUri sink) {
        Objects.requireNonNull(source, "source cannot be null.");
        Objects.requireNonNull(sink, "sink cannot be null.");

        return new UMessageBuilder(
                source,
                UuidFactory.create(), // [impl->dsn~up-attributes-id~1]
                UMessageType.UMESSAGE_TYPE_NOTIFICATION) // [impl->dsn~up-attributes-notification-type~1]
            .withSink(sink);
    }

    /**
     * Gets a builder for an RPC request message.
     * <p>
     * A request message is used to invoke a service's method with some input data, expecting
     * the service to reply with a response message which is correlated by means of its
     * {@link UAttributes#getReqid() request ID}.
     * <p>
     * The builder will be initialized with {@link UPriority#UPRIORITY_CS4}.
     *
     * @param source The URI that the sender of the request expects the response message at.
     * @param sink   The URI identifying the method to invoke.
     * @param ttl    The number of milliseconds after which the request should no longer be processed
     * by the target service. The given value is interpreted as an <em>unsigned</em> integer.
     * @return The builder.
     * @throws NullPointerException if the source or sink is {@code null}.
     * @throws IllegalArgumentException if the ttl is 0.
     */
    public static UMessageBuilder request(UUri source, UUri sink, int ttl) {
        Objects.requireNonNull(source, "source cannot be null.");
        Objects.requireNonNull(sink, "sink cannot be null.");

        if (ttl == 0) {
            // [impl->dsn~up-attributes-request-ttl~1]
            throw new IllegalArgumentException("ttl must be greater than 0.");
        }
        return new UMessageBuilder(
                source,
                UuidFactory.create(), // [impl->dsn~up-attributes-id~1]
                UMessageType.UMESSAGE_TYPE_REQUEST) // [impl->dsn~up-attributes-request-type~1]
            .withSink(sink)
            .withTtl(ttl)
            .withPriority(UPriority.UPRIORITY_CS4);
    }

    /**
     * Gets a builder for an RPC response message.
     * <p>
     * A response message is used to send the outcome of processing a request message
     * to the original sender of the request message.
     * <p>
     * The builder will be initialized with {@link UPriority#UPRIORITY_CS4}.
     *
     * @param source The URI identifying the method that has been invoked and which the created message is
     *   the outcome of.
     * @param sink   The URI that the sender of the request expects to receive the response message at.
     * @param reqid  The identifier of the request that this is the response to.
     * @return The builder.
     * @throws NullPointerException if any of the parameters are {@code null}.
     */
    public static UMessageBuilder response(UUri source, UUri sink, UUID reqid) {
        Objects.requireNonNull(source, "source cannot be null.");
        Objects.requireNonNull(sink, "sink cannot be null for Response.");
        Objects.requireNonNull(reqid, "reqid cannot be null.");

        return new UMessageBuilder(
                source,
                UuidFactory.create(), // [impl->dsn~up-attributes-id~1]
                UMessageType.UMESSAGE_TYPE_RESPONSE) // [impl->dsn~up-attributes-response-type~1]

            .withSink(sink)
            .withReqId(reqid)
            .withPriority(UPriority.UPRIORITY_CS4);
    }

    /**
     * Gets a builder for creating an RPC response message in reply to a request.
     * <p>
     * A response message is used to send the outcome of processing a request message
     * to the original sender of the request message.
     *
     * @param request The attributes from the request message. The response message
     * builder will be initialized with the corresponding attribute values.
     * @return The builder.
     * @throws NullPointerException if request is {@code null}.
     * @throws IllegalArgumentException if the request does not contain valid request attributes.
     */
    public static UMessageBuilder response(UAttributes request) {
        Objects.requireNonNull(request, "request cannot be null.");

        // Validate the request
        try {
            UAttributesValidator.Validators.REQUEST.validator().validate(request);
        } catch (ValidationException e) {
            throw new IllegalArgumentException("request is not a valid request attributes.", e);
        }

        return new UMessageBuilder(
                request.getSink(),
                UuidFactory.create(), // [impl->dsn~up-attributes-id~1]
                UMessageType.UMESSAGE_TYPE_RESPONSE) // [impl->dsn~up-attributes-response-type~1]
            .withPriority(request.getPriority())
            .withSink(request.getSource())
            .withReqId(request.getId())
            .withTtl(request.getTtl());
    }

    /**
     * Creates a builder for attribute values required for all types of messages.
     *
     * @param source Source address of the message.
     * @param id     Unique identifier for the message.
     * @param type   Message type such as Publish a state change, RPC request or RPC
     *               response.
     */
    private UMessageBuilder(UUri source, UUID id, UMessageType type) {
        this.source = source;
        this.id = id;
        this.type = type;
    }

    /**
     * Sets the message's identifier.
     * <p>
     * Every message must have an identifier. If this method is not used, an identifier will be
     * generated and set on the message when one of the <em>build</em> methods is invoked.
     *
     * @param id The custom message ID.
     * @return The builder with the custom message ID.
     * @throws NullPointerException if the id is {@code null}.
     * @throws IllegalArgumentException if the id is not a {@link UuidUtils#isUProtocol(UUID) valid uProtocol UUID}.
     */
    // [impl->dsn~up-attributes-id~1]
    public UMessageBuilder withMessageId(UUID id) {
        Objects.requireNonNull(id, "id cannot be null.");
        if (!UuidUtils.isUProtocol(id)) {
            throw new IllegalArgumentException("id must be a valid uProtocol UUID.");
        }
        this.id = id;
        return this;
    }

    /**
     * Sets the message's time-to-live.
     *
     * @param ttl The time-to-live in milliseconds. Note that the value is interpreted as an
     * <em>unsigned</em> integer. A value of 0 indicates that the message never expires.
     * @return The builder with the configured ttl.
     * @throws IllegalArgumentException if the builder is used for creating an RPC message and the TTL is 0.
     */
    public UMessageBuilder withTtl(int ttl) {
        if ((this.type == UMessageType.UMESSAGE_TYPE_REQUEST
            || this.type == UMessageType.UMESSAGE_TYPE_RESPONSE)
            && ttl == 0) {
            // [impl->dsn~up-attributes-request-ttl~1]
            throw new IllegalArgumentException("TTL of RPC messages must be greater than 0.");
        }
        this.ttl = ttl;
        return this;
    }

    /**
     * Sets the message's authorization token used for TAP.
     *
     * @param token The token.
     * @return The builder with the configured token.
     * @throws NullPointerException if the token is {@code null}.
     * @throws IllegalStateException if the message is not an RPC request message.
     */
    public UMessageBuilder withToken(String token) {
        // [impl->dsn~up-attributes-request-token~1]
        Objects.requireNonNull(token, "token cannot be null.");
        if (this.type != UMessageType.UMESSAGE_TYPE_REQUEST) {
            throw new IllegalStateException("Token can only be set for RPC request messages.");
        }
        this.token = token;
        return this;
    }

    /**
     * Sets the priority of the message.
     * <p>
     * If not set explicitly, the priority will be {@link UPriority#UPRIORITY_UNSPECIFIED}.
     *
     * @param priority The priority to be used for sending the message.
     * @return The builder with the configured priority.
     * @throws NullPointerException if the priority is {@code null}.
     * @throws IllegalArgumentException if the builder is used for creating an RPC message
     * but the priority is less than {@link UPriority#UPRIORITY_CS4}.
     */
    public UMessageBuilder withPriority(UPriority priority) {
        // [impl->dsn~up-attributes-request-priority~1]
        Objects.requireNonNull(priority, "priority cannot be null.");
        if (priority.getNumber() < UPriority.UPRIORITY_CS4_VALUE &&
            (this.type == UMessageType.UMESSAGE_TYPE_REQUEST || this.type == UMessageType.UMESSAGE_TYPE_RESPONSE)) {
            throw new IllegalArgumentException("priority must be at least CS4 for RPC messages");
        }
        this.priority = priority;
        return this;
    }

    /**
     * Sets the message's permission level.
     *
     * @param plevel The level. Note that the value is interpreted as an <em>unsigned</em> integer.
     * @return The builder with the configured permission level.
     * @throws IllegalStateException if the message is not an RPC request message.
     */
    public UMessageBuilder withPermissionLevel(int plevel) {
        // [impl->dsn~up-attributes-permission-level~1]
        if (this.type != UMessageType.UMESSAGE_TYPE_REQUEST) {
            throw new IllegalStateException("Permission level can only be set for RPC request messages.");
        }
        this.plevel = plevel;
        return this;
    }

    /**
     * Sets the identifier of the W3C Trace Context to convey in the message.
     *
     * @param traceparent The identifier.
     * @return The builder with the configured traceparent.
     * @throws NullPointerException if the traceparent is {@code null}.
     */
    public UMessageBuilder withTraceparent(String traceparent) {
        // [impl->dsn~up-attributes-traceparent~1]
        Objects.requireNonNull(traceparent, "traceparent cannot be null.");
        this.traceparent = traceparent;
        return this;
    }

    /**
     * Sets the message's communication status.
     *
     * @param commstatus The status.
     * @return The builder with the configured commstatus.
     * @throws IllegalStateException if the message is not an RPC response message.
     */
    public UMessageBuilder withCommStatus(UCode commstatus) {
        Objects.requireNonNull(commstatus, "commstatus cannot be null.");
        if (this.type != UMessageType.UMESSAGE_TYPE_RESPONSE) {
            throw new IllegalStateException("Communication status can only be set for RPC response messages.");
        }
        this.commstatus = commstatus;
        return this;
    }

    private UMessageBuilder withReqId(UUID reqid) {
        this.reqid = reqid;
        return this;
    }

    private UMessageBuilder withSink(UUri sink) {
        this.sink = sink;
        return this;
    }

    /**
     * Build a message with the passed {@link UPayload}.
     * 
     * @param payload The payload to be packed into the message.
     * @return Returns the UMessage with the configured payload.
     * @throws NullPointerException if the payload is {@code null}.
     */
    public UMessage build(UPayload payload) {
        Objects.requireNonNull(payload, "payload cannot be null.");
        // [impl->dsn~up-attributes-payload-format~1]
        this.format = payload.format();
        this.payload = payload.data();
        return build();
    }

    
    /**
     * Creates the message based on the builder's state.
     *
     * @return A message ready to be sent using a transport implementation.
     * @throws ValidationException if the properties set on the builder do not represent a
     * consistent set of attributes as determined by {@link UAttributesValidator#validate(UAttributes)}.
     */
    public UMessage build() {
        UAttributes.Builder attributesBuilder = UAttributes.newBuilder()
                .setSource(source)
                .setId(id)
                .setType(type);

        Optional.ofNullable(sink).ifPresent(attributesBuilder::setSink);
        Optional.ofNullable(priority).ifPresent(attributesBuilder::setPriority);
        Optional.ofNullable(ttl).ifPresent(attributesBuilder::setTtl);
        Optional.ofNullable(plevel).ifPresent(attributesBuilder::setPermissionLevel);
        Optional.ofNullable(commstatus).ifPresent(attributesBuilder::setCommstatus);
        Optional.ofNullable(reqid).ifPresent(attributesBuilder::setReqid);
        Optional.ofNullable(token).ifPresent(attributesBuilder::setToken);
        Optional.ofNullable(traceparent).ifPresent(attributesBuilder::setTraceparent);
        Optional.ofNullable(format).ifPresent(attributesBuilder::setPayloadFormat);

        final var attributes = attributesBuilder.build();
        UAttributesValidator.getValidator(attributes).validate(attributes);

        UMessage.Builder messageBuilder = UMessage.newBuilder();
        Optional.ofNullable(payload).ifPresent(messageBuilder::setPayload);
        return messageBuilder.setAttributes(attributes).build();
    }
}
