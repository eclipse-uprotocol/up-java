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

import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UMessage;
import org.eclipse.uprotocol.v1.UPayloadFormat;

/**
 * Wrapper class that stores the payload as {@link UPayloadFormat}.
 */
// [impl->dsn~communication-layer-api-declaration~1]
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
     * Unpacks a protobuf from a message payload into a Java type.
     * <p>
     * <em>IMPORTANT NOTE:</em> If {@link UPayloadFormat} is not
     * {@link UPayloadFormat#UPAYLOAD_FORMAT_PROTOBUF_WRAPPED_IN_ANY},
     * there is no guarantee that the parsing to T is correct as we do not have the data schema.
     * 
     * @param payload The payload to unpack.
     * @param clazz The Java type to unpack the payload to.
     * @return The unpacked type instance.
     * @deprecated Use {@link #unpackOrDefaultInstance(UPayload, Class)} instead.
     */
    @Deprecated(forRemoval = true)
    public static <T extends Message> Optional<T> unpack(UPayload payload, Class<T> clazz) {
        if (payload == null) {
            return Optional.empty();
        }
        return unpack(payload.data(), payload.format(), clazz);
    }

    /**
     * Unpacks a protobuf from a message payload into a Java type.
     * 
     * @param payload The payload to unpack.
     * @param expectedType The Java type to unpack the protobuf to.
     * @return An instance of the expected type. The instance will be the default instance if the
     * given protobuf is empty and the payload format is {@link UPayloadFormat#UPAYLOAD_FORMAT_PROTOBUF}.
     * <p>
     * <em>IMPORTANT NOTE:</em> If <em>format</em> is not
     * {@link UPayloadFormat#UPAYLOAD_FORMAT_PROTOBUF_WRAPPED_IN_ANY} then there is no guarantee
     * that the returned instance's fields contain proper values because in the absence of a data schema
     * it is unclear, if the protobuf actually represents an instance of the expected type.
     * @throws NullPointerException if any of the arguments are {@code null}.
     * @throws UStatusException if the protobuf cannot be unpacked to the expected type.
     */
    public static <T extends Message> T unpackOrDefaultInstance(UPayload payload, Class<T> expectedType) {
        return unpackOrDefaultInstance(payload.data(), payload.format(), expectedType);
    }

    /**
     * Unpacks a protobuf into a Java type.
     * <br>
     * <b>IMPORTANT NOTE:</b> If the format is not {@link UPayloadFormat#UPAYLOAD_FORMAT_PROTOBUF_WRAPPED_IN_ANY},
     * there is no guarantee that the parsing to T is correct as we do not have the data schema.
     * 
     * @param data The protobuf to unpack.
     * @param format The serialization format of the protobuf.
     * @param clazz The Java type to unpack the protobuf to.
     * @return The unpacked type instance.
     * @throws NullPointerException if clazz is {@code null}.
     * @deprecated Use {@link #unpackOrDefaultInstance(ByteString, UPayloadFormat, Class)} instead.
     */
    @Deprecated(forRemoval = true)
    @SuppressWarnings("unchecked")
    public static <T extends Message> Optional<T> unpack(ByteString data, UPayloadFormat format, Class<T> clazz) {
        Objects.requireNonNull(clazz, "clazz must not be null");
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

    /**
     * Unpacks a protobuf to a Java type.
     * 
     * @param protobuf The protobuf to unpack.
     * @param format The serialization format of the protobuf.
     * @param expectedType The Java type to unpack the protobuf to.
     * @return An instance of the expected type. The instance will be the default instance if the
     * given protobuf is empty and the payload format is {@link UPayloadFormat#UPAYLOAD_FORMAT_PROTOBUF}.
     * <p>
     * <em>IMPORTANT NOTE:</em> If <em>format</em> is not
     * {@link UPayloadFormat#UPAYLOAD_FORMAT_PROTOBUF_WRAPPED_IN_ANY} then there is no guarantee
     * that the returned instance's fields contain proper values because in the absence of a data schema
     * it is unclear, if the protobuf actually represents an instance of the expected type.
     * @throws NullPointerException if any of the arguments are {@code null}.
     * @throws UStatusException if the protobuf cannot be unpacked to the expected type.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Message> T unpackOrDefaultInstance(
            ByteString protobuf,
            UPayloadFormat format,
            Class<T> expectedType) {
        Objects.requireNonNull(protobuf, "data must not be null");
        Objects.requireNonNull(format, "format must not be null");
        Objects.requireNonNull(expectedType, "expectedType must not be null");
        switch (format) {
            case UPAYLOAD_FORMAT_UNSPECIFIED: // Default is WRAPPED_IN_ANY
            case UPAYLOAD_FORMAT_PROTOBUF_WRAPPED_IN_ANY :
                try {
                    return Any.parseFrom(protobuf).unpack(expectedType);
                } catch (InvalidProtocolBufferException e) {
                    throw new UStatusException(UCode.INVALID_ARGUMENT, "Failed to unpack Any", e);
                }

            case UPAYLOAD_FORMAT_PROTOBUF:
                T defaultInstance = com.google.protobuf.Internal.getDefaultInstance(expectedType);
                if (protobuf.isEmpty()) {
                    // this can happen when trying to unpack a proto message that has no fields
                    // and is therefore encoded as an empty byte array
                    return defaultInstance;
                } else {
                    try {
                        return (T) defaultInstance.getParserForType().parseFrom(protobuf);
                    } catch (InvalidProtocolBufferException e) {
                        throw new UStatusException(UCode.INVALID_ARGUMENT, "Failed to unpack protobuf", e);
                    }
                }

            default:
                throw new UStatusException(
                    UCode.INVALID_ARGUMENT, "Unsupported payload format");
        }
    }
}
