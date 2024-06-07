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

import java.util.Optional;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeoutException;

import com.google.protobuf.Message;

import org.eclipse.uprotocol.v1.UCode;

/**
 * RPC Wrapper is an interface that provides static methods to be able to wrap an RPC request with 
 * an RPC Response (uP-L2). APIs that return Message assumes that the payload is either protobuf serialized 
 * UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF_WRAPPED_IN_ANY) or UPAYLOAD_FORMAT_PROTOBUF.
 */
public interface RpcMapper {

    /**
     * Map a response from invoking a method on a uTransport service into a CompletionStage 
     * containing the declared expected return type of the RPC method.
     * 
     * NOTE: If the payload is not wrapped in Any, there is no guarantee that the parsing
     * is correct as we do not have the data schema.
     * 
     * @param responseFuture CompletionStage&lt;UPayload&gt; response from uTransport.
     * @param expectedClazz The class name of the declared expected return type of the RPC method.
     * @return Returns a CompletionStage containing the declared expected return type 
     *         of the RPC method or an exception.
     * @param <T> The declared expected return type of the RPC method.
     */
    static <T extends Message> CompletionStage<T> mapResponse(
            CompletionStage<UPayload> responseFuture, Class<T> expectedClazz) {
        return responseFuture.handle((payload, exception) -> {
            // Unexpected exception
            if (exception != null) {
                throw new CompletionException(exception.getMessage(), exception);
            }

            if (payload != null) {
                if (payload.getData().isEmpty()) {
                    return com.google.protobuf.Internal.getDefaultInstance(expectedClazz);
                } else {
                    Optional<T> result = UPayload.unpack(payload, expectedClazz);
                    return result.get();
                }
            }

            // Some other type instead of the expected one
            throw new RuntimeException(String.format("Unknown payload. Expected [%s]", expectedClazz.getName()));
        });
    }

    /**
     * Map a response from method invocation to a RpcResult containing the declared expected
     * return type of the RPC method.
     * 
     * @param responseFuture CompletionStage&lt;UPayload&gt; response from uTransport.
     * @param expectedClazz The class name of the declared expected return type of the RPC method.
     * @return Returns a CompletionStage containing an RpcResult containing the declared expected
     *         return type T, or a Status containing any errors.
     * @param <T> The declared expected return type of the RPC method.
     */
    static <T extends Message> CompletionStage<RpcResult<T>> mapResponseToResult(
            CompletionStage<UPayload> responseFuture, Class<T> expectedClazz) {

        return responseFuture.handle((payload, exception) -> {
            // Handling exceptions
            if (exception != null) {
                if (exception instanceof CompletionException) {
                    exception = exception.getCause();
                }
                if (exception instanceof UStatusException) {
                    return RpcResult.failure(((UStatusException) exception).getStatus());
                } else if (exception instanceof TimeoutException) {
                    return RpcResult.failure(UCode.DEADLINE_EXCEEDED, "Request timed out");
                } else {
                    return RpcResult.failure(UCode.INVALID_ARGUMENT, exception.getMessage());
                }
            }

            if (payload != null) { 
                if (payload.getData().isEmpty()) {
                    return RpcResult.success(com.google.protobuf.Internal.getDefaultInstance(expectedClazz));
                } else {
                    Optional<T> result = UPayload.unpack(payload, expectedClazz);
                    return RpcResult.success(result.get());
                }
            }

            // Some other type instead of the expected one
            exception = new RuntimeException(String.format(
                "Unknown or null payload type. Expected [%s]", expectedClazz.getName()));
            return RpcResult.failure(exception.getMessage(), exception);
        });
    }


}
