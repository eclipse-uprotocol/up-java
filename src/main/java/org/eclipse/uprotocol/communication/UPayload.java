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

import org.eclipse.uprotocol.v1.UPayloadFormat;

/**
 * Wrapper class that stores the payload as {@link UPayloadFormat}.
 */
public class UPayload {

    private ByteString data;
    private UPayloadFormat format;

    // Empty UPayload
    public static final UPayload EMPTY = new UPayload(null, null);
    
    /**
     * Private constructor for UPayload
     * @param data payload data
     * @param format payload format
     */
    private UPayload(ByteString data, UPayloadFormat format) {
        this.data = Optional.ofNullable(data).orElse(ByteString.EMPTY);
        this.format = Optional.ofNullable(format).orElse(UPayloadFormat.UPAYLOAD_FORMAT_UNSPECIFIED);
    }

    /**
     * Check if the payload is empty, returns true when what is passed is null or the data is empty.
     * 
     * @param payload the payload to check
     * @return true if the payload is empty
     */
    public static boolean isEmpty(UPayload payload) {
        return payload == null || 
            payload.getData().isEmpty() && payload.getFormat() == UPayloadFormat.UPAYLOAD_FORMAT_UNSPECIFIED;
    }


    /**
     * Get the payload data.
     * 
     * @return the payload data
     */
    public ByteString getData() {
        return data;
    }

    /**
     * Get the payload format.
     * 
     * @return the payload format
     */
    public UPayloadFormat getFormat() {
        return format;
    }
    

    /**
     * Build a uPayload from {@link google.protobuf.Message} by stuffing the message into an Any.
     * 
     * @param message the message to pack
     * @return the UPayload 
     */
    static UPayload packToAny(Message message) {
        return message == null ? new UPayload(null, null) :
            new UPayload(Any.pack(message).toByteString(), UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF_WRAPPED_IN_ANY);
    }

    /**
     * Build a uPayload from {@link google.protobuf.Message} using protobuf PayloadFormat.
     * 
     * @param message the message to pack
     * @return the UPayload
     */
    static UPayload pack(Message message) {
        return message == null ? new UPayload(null, null) : 
            new UPayload(message.toByteString(), UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF);
    }

    /**
     * Build a UPayload from specific data and passed format
     * @param data payload data.
     * @param format payload format.
     * @return the UPayload.
     */
    static UPayload pack(ByteString data, UPayloadFormat format) {
        return new UPayload(data, format);
    }

    /**
     * Unpack a uPayload into {@link google.protobuf.Message}.<br>
     * <br>
     * <b>IMPORTANT NOTE:</b> If {@link UPayloadFormat} is not {@link UPAYLOAD_FORMAT_PROTOBUF_WRAPPED_IN_ANY},
     * there is no guarantee that the parsing to T is correct as we do not have the data schema.
     * 
     * @param payload the payload to unpack
     * @param clazz the class of the message to unpack
     * @return the unpacked message
     */
    static <T extends Message> Optional<T> unpack(UPayload payload, Class<T> clazz) {
        if (payload == null) {
            return Optional.empty();
        }
        return unpack(payload.getData(), payload.getFormat(), clazz);
    }

    /**
     * Unpack a uPayload into a {@link google.protobuf.Message}.
     * <br>
     * <b>IMPORTANT NOTE:</b> If {@link UPayloadFormat} is not {@link UPAYLOAD_FORMAT_PROTOBUF_WRAPPED_IN_ANY},
     * there is no guarantee that the parsing to T is correct as we do not have the data schema.
     * 
     * @param data The serialized UPayload data
     * @param format The serialization format of the payload
     * @param clazz the class of the message to unpack
     * @return the unpacked message
     */
    @SuppressWarnings("unchecked")
    static <T extends Message> Optional<T> unpack(ByteString data, UPayloadFormat format, Class<T> clazz) {
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
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        UPayload uPayload = (UPayload) obj;
        return Objects.equals(data, uPayload.data) && format == uPayload.format;
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, format);
    }

    @Override
    public String toString() {
        return "UPayload{" + "data=" + data.toStringUtf8() + ", format=" + format + '}';
    }
}
