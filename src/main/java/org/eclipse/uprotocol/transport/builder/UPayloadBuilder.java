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

package org.eclipse.uprotocol.transport.builder;


import java.util.Optional;

import org.eclipse.uprotocol.v1.*;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;


public interface UPayloadBuilder {

    /**
     * Build a uPayload from google.protobuf.Message by stuffing the message into an Any.
     * 
     * @param message the message to pack
     * @return the UPayload 
     */
    static UPayload packToAny(Message message) {
        return UPayload.newBuilder()
            .setFormat(UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF_WRAPPED_IN_ANY)
            .setValue(Any.pack(message).toByteString())
            .build();
    }

    /**
     * Build a uPayload from google.protobuf.Message using protobuf PayloadFormat.
     * 
     * @param message the message to pack
     * @return the UPayload
     */
    static UPayload pack(Message message) {
        return UPayload.newBuilder()
            .setFormat(UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF)
            .setValue(message.toByteString())
            .build();
    }


    /**
     * Unpack a uPayload into a google.protobuf.Message.
     * 
     * @param payload the payload to unpack
     * @param clazz the class of the message to unpack
     * @return the unpacked message
     */
    @SuppressWarnings("unchecked")
    static <T extends Message> Optional<T> unpack(UPayload payload, Class<T> clazz) {
        if (payload == null || payload.getValue() == null) {
            return Optional.empty();
        }
        try {
            switch (payload.getFormat()) {
                case UPAYLOAD_FORMAT_UNSPECIFIED: // Default is WRAPPED_IN_ANY
                case UPAYLOAD_FORMAT_PROTOBUF_WRAPPED_IN_ANY :
                    return Optional.of(Any.parseFrom(payload.getValue()).unpack(clazz));
            
                case UPAYLOAD_FORMAT_PROTOBUF: 
                    T defaultInstance = com.google.protobuf.Internal.getDefaultInstance(clazz);
                    return Optional.of((T)defaultInstance.getParserForType().parseFrom(payload.getValue()));
                
                default:
                    return Optional.empty();
            }
        }
        catch (InvalidProtocolBufferException e) {
            return Optional.empty();
        }
    }
}
