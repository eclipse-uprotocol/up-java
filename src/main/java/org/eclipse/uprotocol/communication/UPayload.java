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
package org.eclipse.uprotocol.communication;


import java.util.Objects;
import java.util.Optional;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

import org.eclipse.uprotocol.v1.UMessage;
import org.eclipse.uprotocol.v1.UPayloadFormat;

/**
 * Wrapper class that stores the payload as {@link UPayloadFormat}.
 */
public record UPayload (ByteString data, UPayloadFormat format) {

    // Empty UPayload
    public static final UPayload EMPTY = new UPayload();


    public UPayload {
        Objects.requireNonNull(data);
        Objects.requireNonNull(format);
    }


    public UPayload() {
        this(ByteString.EMPTY, UPayloadFormat.UPAYLOAD_FORMAT_UNSPECIFIED);
    }
    

    /**
     * Check if the payload is empty, returns true when what is passed is null or the data is empty.
     * 
     * @param payload the payload to check
     * @return true if the payload is empty
     */
    public static boolean isEmpty(UPayload payload) {
        return payload == null || 
            payload.data().isEmpty() && payload.format() == UPayloadFormat.UPAYLOAD_FORMAT_UNSPECIFIED;
    }


    /**
     * Build a uPayload from {@link Message} by stuffing the message into an Any.
     * 
     * @param message the message to pack
     * @return the UPayload 
     */
    public static UPayload packToAny(Message message) {
        return message == null ? EMPTY :
            new UPayload(Any.pack(message).toByteString(), UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF_WRAPPED_IN_ANY);
    }


    /**
     * Build a uPayload from {@link Message} using protobuf PayloadFormat.
     * 
     * @param message the message to pack
     * @return the UPayload
     */
    public static UPayload pack(Message message) {
        return message == null ? EMPTY : 
            new UPayload(message.toByteString(), UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF);
    }


    /**
     * Build a UPayload from specific data and passed format.
     *
     * @param data payload data.
     * @param format payload format.
     * @return the UPayload.
     */
    public static UPayload pack(ByteString data, UPayloadFormat format) {
        return new UPayload(data, format);
    }


    /**
     * Unpack a uMessage into {@link Message}.
     * 
     * @param message the message to unpack
     * @param clazz the class of the message to unpack
     * @return the unpacked message
     */
    public static <T extends Message> Optional<T> unpack(UMessage message, Class<T> clazz) {
        if (message == null) {
            return Optional.empty();
        }
        return unpack(message.getPayload(), message.getAttributes().getPayloadFormat(), clazz);
    }


    /**
     * Unpack a uPayload into {@link Message}.
     * <p>
     * <em>IMPORTANT NOTE:</em> If {@link UPayloadFormat} is not
     * {@link UPayloadFormat#UPAYLOAD_FORMAT_PROTOBUF_WRAPPED_IN_ANY},
     * there is no guarantee that the parsing to T is correct as we do not have the data schema.
     * 
     * @param payload the payload to unpack
     * @param clazz the class of the message to unpack
     * @return the unpacked message
     */
    public static <T extends Message> Optional<T> unpack(UPayload payload, Class<T> clazz) {
        if (payload == null) {
            return Optional.empty();
        }
        return unpack(payload.data(), payload.format(), clazz);
    }


    /**
     * Unpack a uPayload into a {@link Message}.
     * <br>
     * <b>IMPORTANT NOTE:</b> If the format is not {@link UPayloadFormat#UPAYLOAD_FORMAT_PROTOBUF_WRAPPED_IN_ANY},
     * there is no guarantee that the parsing to T is correct as we do not have the data schema.
     * 
     * @param data The serialized UPayload data
     * @param format The serialization format of the payload
     * @param clazz the class of the message to unpack
     * @return the unpacked message
     */
    @SuppressWarnings("unchecked")
    public static <T extends Message> Optional<T> unpack(ByteString data, UPayloadFormat format, Class<T> clazz) {
        format = Objects.requireNonNullElse(format, UPayloadFormat.UPAYLOAD_FORMAT_UNSPECIFIED);
        if (data == null || data.isEmpty()) {
            return Optional.empty();
        }
        try {
            switch (format) {
                case UPAYLOAD_FORMAT_UNSPECIFIED: // Default is WRAPPED_IN_ANY
                case UPAYLOAD_FORMAT_PROTOBUF_WRAPPED_IN_ANY :
                    return Optional.of(Any.parseFrom(data).unpack(clazz));
            
                case UPAYLOAD_FORMAT_PROTOBUF: 
                    T defaultInstance = com.google.protobuf.Internal.getDefaultInstance(clazz);
                    return Optional.of((T) defaultInstance.getParserForType().parseFrom(data));
                
                default:
                    return Optional.empty();
            }
        } catch (InvalidProtocolBufferException e) {
            return Optional.empty();
        }
    }
}
