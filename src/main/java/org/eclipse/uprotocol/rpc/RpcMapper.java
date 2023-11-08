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

package org.eclipse.uprotocol.rpc;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.rpc.Code;
import com.google.rpc.Status;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.eclipse.uprotocol.transport.datamodel.UPayload;

/**
 * RPC Wrapper is an interface that provides static methods to be able to wrap an RPC request with 
 * an RPC Response (uP-L2). APIs that return Message assumes that the payload is protobuf serialized 
 * com.google.protobuf.Any (UPayloadFormat.PROTOBUF) and will barf if anything else is passed
 */
public interface RpcMapper {

    /**
     * Map a response of CompletableFuture&lt;UPayload&gt; from Link into a CompletableFuture containing the declared expected return type of the RPC method or an exception.
     * @param responseFuture CompletableFuture&lt;UPayload&gt; response from uTransport.
     * @param expectedClazz The class name of the declared expected return type of the RPC method.
     * @return Returns a CompletableFuture containing the declared expected return type of the RPC method or an exception.
     * @param <T> The declared expected return type of the RPC method.
     */
    static <T extends Message> CompletableFuture<T> mapResponse(CompletableFuture<UPayload> responseFuture, Class<T> expectedClazz) {
        return responseFuture.handle((payload, exception) -> {
            // Unexpected exception
            if (exception != null) {
                throw new CompletionException(exception.getMessage(), exception);
            }
            if (payload == null) {
                throw new RuntimeException("Server returned a null payload. Expected " + expectedClazz.getName());
            }
            Any any;
            try {
                any = Any.parseFrom(payload.data());
            
                // Expected type
                if (any.is(expectedClazz)) {
                    return unpackPayload(any, expectedClazz);
                }
            } catch (InvalidProtocolBufferException e) {
                throw new RuntimeException(String.format("%s [%s]", e.getMessage(), Status.class.getName()), e);
            }
            // Some other type instead of the expected one
            throw new RuntimeException(String.format("Unknown payload type [%s]. Expected [%s]", any.getTypeUrl(), expectedClazz.getName()));
        });
    }

    /**
     * Map a response of CompletableFuture&lt;Any&gt; from Link into a CompletableFuture containing an RpcResult containing the declared expected return type T, or a Status containing any errors.
     * @param responseFuture CompletableFuture&lt;Any&gt; response from Link.
     * @param expectedClazz The class name of the declared expected return type of the RPC method.
     * @return Returns a CompletableFuture containing an RpcResult containing the declared expected return type T, or a Status containing any errors.
     * @param <T> The declared expected return type of the RPC method.
     */
    static <T extends Message> CompletableFuture<RpcResult<T>> mapResponseToResult(CompletableFuture<UPayload> responseFuture, Class<T> expectedClazz) {
        return responseFuture.handle((payload, exception) -> {
            // Unexpected exception
            if (exception != null) {
                throw new RuntimeException(exception.getMessage(), exception);
            }

            if (payload == null) {
                throw new RuntimeException("Server returned a null payload. Expected " + expectedClazz.getName());
            }
            Any any;
            try {
                any = Any.parseFrom(payload.data());
                
                // Expected type
                if (any.is(expectedClazz)) {
                    if (Status.class.equals(expectedClazz)) {
                        return calculateStatusResult(any);
                    } else {
                        return RpcResult.success(unpackPayload(any, expectedClazz));
                    }
                }
                // Status instead of the expected one
                if (any.is(Status.class)) {
                    return calculateStatusResult(any);
                }
            } catch (InvalidProtocolBufferException e) {
                throw new RuntimeException(String.format("%s [%s]", e.getMessage(), Status.class.getName()), e);
            }
            
            // Some other type instead of the expected one
            throw new RuntimeException(String.format("Unknown payload type [%s]. Expected [%s]",
                    any.getTypeUrl(), expectedClazz.getName()));
        });
    }

    @SuppressWarnings("unchecked")
    private static <T extends Message> RpcResult<T> calculateStatusResult(Any payload) {
            final Status status = unpackPayload(payload, Status.class);
            return status.getCode() == Code.OK_VALUE ?
                    RpcResult.success((T) status) : RpcResult.failure(status);
    }

    /**
     * Unpack a payload of type {@link Any} into an object of type T, which is what was packing into the {@link Any} object.
     * @param payload an {@link Any} message containing a type of expectedClazz.
     * @param expectedClazz The class name of the object packed into the {@link Any}
     * @return Returns an object of type T and of the class name specified, that was packed into the {@link Any} object.
     * @param <T> The message type of the object packed into the {@link Any}.
     */
    static <T extends Message> T unpackPayload(Any payload, Class<T> expectedClazz) {
        try {
            return payload.unpack(expectedClazz);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(String.format("%s [%s]", e.getMessage(), Status.class.getName()), e);
        }
    }

}
