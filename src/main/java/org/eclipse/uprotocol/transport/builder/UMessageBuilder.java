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
import org.eclipse.uprotocol.v1.UMessage;
import org.eclipse.uprotocol.v1.UAttributes;
import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UPriority;
import org.eclipse.uprotocol.v1.UMessageType;
import org.eclipse.uprotocol.v1.UPayloadFormat;
import org.eclipse.uprotocol.v1.UUID;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;

import org.eclipse.uprotocol.uuid.factory.UuidFactory;

import java.util.Objects;
import java.util.Optional;

/**
 * Builder for easy construction of the UAttributes object.
 */
public class UMessageBuilder {

    private final UUri source;
    private final UUID id;
    private final UMessageType type;
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
     * Construct a UMessageBuilder for a publish message.
     * 
     * @param source The topic the message is published to (a.k.a Source address).
     * @return Returns the UMessageBuilder with the configured priority.
     */
    public static UMessageBuilder publish(UUri source) {
        Objects.requireNonNull(source, "source cannot be null.");
        return new UMessageBuilder(source, UuidFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.UMESSAGE_TYPE_PUBLISH);
    }

    /**
     * Construct a UMessageBuilder for a notification message.
     * 
     * @param source The topic the message is published to (a.k.a Source address).
     * @param sink   The destination address for the notification (who will receive
     *               the notification).
     * @return Returns the UMessageBuilder with the configured priority and sink.
     */
    public static UMessageBuilder notification(UUri source, UUri sink) {
        Objects.requireNonNull(source, "source cannot be null.");
        Objects.requireNonNull(sink, "sink cannot be null.");

        return new UMessageBuilder(source, UuidFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.UMESSAGE_TYPE_NOTIFICATION).withSink(sink);
    }

    /**
     * Construct a UMessageBuilder for a request message.
     * 
     * @param source Source address for the message (address of the client sending
     *               the request message).
     * @param sink   The method that is being requested (a.k.a. destination
     *               address).
     * @param ttl    The time to live in milliseconds.
     * @return Returns the UMessageBuilder with the configured priority, sink and
     *         ttl.
     */
    public static UMessageBuilder request(UUri source, UUri sink, Integer ttl) {
        Objects.requireNonNull(source, "source cannot be null.");
        Objects.requireNonNull(ttl, "ttl cannot be null.");
        Objects.requireNonNull(sink, "sink cannot be null.");

        return new UMessageBuilder(source, UuidFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.UMESSAGE_TYPE_REQUEST).withTtl(ttl).withSink(sink);
    }

    /**
     * Construct a UMessageBuilder for a response message.
     * 
     * @param source The source address of the method that was requested
     * @param sink   The destination of the client thatsend the request.
     * @param reqid  The original request UUID used to correlate the response to the
     *               request.
     * @return Returns the UMessageBuilder with the configured priority, sink and
     *         reqid.
     */
    public static UMessageBuilder response(UUri source, UUri sink, UUID reqid) {
        Objects.requireNonNull(source, "source cannot be null.");
        Objects.requireNonNull(sink, "sink cannot be null.");
        Objects.requireNonNull(reqid, "reqid cannot be null.");

        return new UMessageBuilder(source, UuidFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.UMESSAGE_TYPE_RESPONSE).withSink(sink).withReqId(reqid);
    }

    /**
     * Construct a UMessageBuilder for a response message using an existing request.
     * 
     * @param request The original request {@code UAttributes} used to correlate the
     *                response to the request.
     * @return Returns the UMessageBuilder with the configured source, sink,
     *         priority, and reqid.
     */
    public static UMessageBuilder response(UAttributes request) {
        Objects.requireNonNull(request, "request cannot be null.");
        return new UMessageBuilder(
                request.getSink(),
                UuidFactory.Factories.UPROTOCOL.factory().create(),
                UMessageType.UMESSAGE_TYPE_RESPONSE)
                .withPriority(request.getPriority())
                .withSink(request.getSource())
                .withReqId(request.getId());
    }

    /**
     * Construct the UMessageBuilder with the configurations that are required for
     * every payload transport.
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
     * Add the time to live in milliseconds.
     *
     * @param ttl the time to live in milliseconds.
     * @return Returns the UMessageBuilder with the configured ttl.
     */
    public UMessageBuilder withTtl(Integer ttl) {
        this.ttl = ttl;
        return this;
    }

    /**
     * Add the authorization token used for TAP.
     *
     * @param token the authorization token used for TAP.
     * @return Returns the UMessageBuilder with the configured token.
     */
    public UMessageBuilder withToken(String token) {
        this.token = token;
        return this;
    }

    /**
     * Add the priority of the message.
     * 
     * @param priority the priority of the message.
     * @return Returns the UMessageBuilder with the configured priority.
     */
    public UMessageBuilder withPriority(UPriority priority) {
        this.priority = priority;
        return this;
    }

    /**
     * Add the permission level of the message.
     *
     * @param plevel the permission level of the message.
     * @return Returns the UMessageBuilder with the configured plevel.
     */
    public UMessageBuilder withPermissionLevel(Integer plevel) {
        this.plevel = plevel;
        return this;
    }

    /**
     * Add the traceprent.
     *
     * @param traceparent the trace parent.
     * @return Returns the UMessageBuilder with the configured traceparent.
     */
    public UMessageBuilder withTraceparent(String traceparent) {
        this.traceparent = traceparent;
        return this;
    }

    /**
     * Add the communication status of the message.
     *
     * @param commstatus the communication status of the message.
     * @return Returns the UMessageBuilder with the configured commstatus.
     */
    public UMessageBuilder withCommStatus(UCode commstatus) {
        this.commstatus = commstatus;
        return this;
    }

    /**
     * Add the request ID.
     *
     * @param reqid the request ID.
     * @return Returns the UMessageBuilder with the configured reqid.
     */
    private UMessageBuilder withReqId(UUID reqid) {
        this.reqid = reqid;
        return this;
    }

    /**
     * Add the explicit destination URI.
     *
     * @param sink the explicit destination URI.
     * @return Returns the UMessageBuilder with the configured sink.
     */
    private UMessageBuilder withSink(UUri sink) {
        this.sink = sink;
        return this;
    }

    /**
     * Build a message with the passed google protobuf message object
     * 
     * @param message Google protobuf message to be packed into the payload
     * @return Returns the UMessage with the configured payload.
     */
    public UMessage build(Message message) {
        Objects.requireNonNull(message, "Protobuf Message cannot be null.");
        this.format = UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF;
        this.payload = message.toByteString();
        return build();
    }

    /**
     * Build a message with the message already packed into google.protobuf.Any
     * 
     * @param any Google protobuf Any object to be packed into the payload
     * @return Returns the UMessage with the configured payload.
     */
    public UMessage build(Any any) {
        Objects.requireNonNull(any, "any cannot be null.");
        this.format = UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF_WRAPPED_IN_ANY;
        this.payload = any.toByteString();
        return build();
    }

    public UMessage build(UPayloadFormat format, ByteString payload) {
        Objects.requireNonNull(format, "format cannot be null.");
        Objects.requireNonNull(payload, "payload cannot be null.");
        this.format = format;
        this.payload = payload;
        return build();
    }

    /**
     * Construct the UMessage from the builder.
     *
     * @return Returns a constructed
     */
    public UMessage build() {
        UMessage.Builder messageBuilder = UMessage.newBuilder();

        UAttributes.Builder attributesBuilder = UAttributes.newBuilder()
                .setSource(source)
                .setId(id)
                .setType(type);

        Optional<UPriority> priority = Optional.ofNullable(this.priority);
        switch (type) {
            case UMESSAGE_TYPE_REQUEST:
            case UMESSAGE_TYPE_RESPONSE:
                attributesBuilder.setPriority(
                        priority
                                .filter(v -> v.getNumber() >= UPriority.UPRIORITY_CS4.getNumber())
                                .orElse(UPriority.UPRIORITY_CS4));
                break;
            default:
                attributesBuilder.setPriority(
                        priority
                                .filter(v -> v.getNumber() >= UPriority.UPRIORITY_CS1.getNumber())
                                .orElse(UPriority.UPRIORITY_CS1));
                break;
        }

        Optional.ofNullable(sink).ifPresent(attributesBuilder::setSink);
        Optional.ofNullable(ttl).ifPresent(attributesBuilder::setTtl);
        Optional.ofNullable(plevel).ifPresent(attributesBuilder::setPermissionLevel);
        Optional.ofNullable(commstatus).ifPresent(attributesBuilder::setCommstatus);
        Optional.ofNullable(reqid).ifPresent(attributesBuilder::setReqid);
        Optional.ofNullable(token).ifPresent(attributesBuilder::setToken);
        Optional.ofNullable(traceparent).ifPresent(attributesBuilder::setTraceparent);
        Optional.ofNullable(payload).ifPresent(messageBuilder::setPayload);
        Optional.ofNullable(format).ifPresent(attributesBuilder::setPayloadFormat);

        return messageBuilder.setAttributes(attributesBuilder).build();
    }
}
