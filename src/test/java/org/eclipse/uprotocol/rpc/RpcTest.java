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

package org.eclipse.uprotocol.rpc;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.Int32Value;
import com.google.protobuf.InvalidProtocolBufferException;

import org.eclipse.uprotocol.uri.serializer.LongUriSerializer;
import org.eclipse.uprotocol.v1.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class RpcTest {

    RpcClient ReturnsNumber3 = new RpcClient() {
        @Override
        public CompletionStage<UPayload> invokeMethod(UUri topic, UPayload payload, CallOptions options) {
            UPayload data = UPayload.newBuilder()
                .setFormat(UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF)
                .setValue(Any.pack(Int32Value.of(3)).toByteString())
                .build();
            return CompletableFuture.completedFuture(data);
        }
    };

    RpcClient HappyPath = new RpcClient() {
        @Override
        public CompletionStage<UPayload> invokeMethod(UUri topic, UPayload payload, CallOptions options) {
            UPayload data = buildUPayload();
            return CompletableFuture.completedFuture(data);
        }
    };

    RpcClient WithUStatusCodeInsteadOfHappyPath = new RpcClient() {
        @Override
        public CompletionStage<UPayload> invokeMethod(UUri topic, UPayload payload, CallOptions options) {
            UStatus status = UStatus.newBuilder().setCode(UCode.INVALID_ARGUMENT).setMessage("boom").build();
            Any any = Any.pack(status);
            UPayload data = UPayload.newBuilder()
                .setFormat(UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF)
                .setValue(any.toByteString())
                .build();
            return CompletableFuture.completedFuture(data);
        }
    };

    RpcClient WithUStatusCodeHappyPath = new RpcClient() {
        @Override
        public CompletionStage<UPayload> invokeMethod(UUri topic, UPayload payload, CallOptions options) {
            UStatus status = UStatus.newBuilder().setCode(UCode.OK).setMessage("all good").build();
            Any any = Any.pack(status);
            UPayload data = UPayload.newBuilder()
                .setFormat(UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF)
                .setValue(any.toByteString())
                .build();
            return CompletableFuture.completedFuture(data);
        }
    };

    RpcClient ThatBarfsCrapyPayload = new RpcClient() {
        @Override
        public CompletionStage<UPayload> invokeMethod(UUri topic, UPayload payload, CallOptions options) {
            UPayload response = UPayload.newBuilder()
                .setFormat(UPayloadFormat.UPAYLOAD_FORMAT_RAW)
                .setValue(ByteString.copyFrom(new byte[]{0}))
                .build();
            return CompletableFuture.completedFuture(response);
        }
    };


    RpcClient ThatCompletesWithAnException = new RpcClient() {
        @Override
        public CompletionStage<UPayload> invokeMethod(UUri topic, UPayload payload, CallOptions options) {
            return CompletableFuture.failedFuture(new RuntimeException("Boom"));
        }

    };

    RpcClient ThatReturnsTheWrongProto = new RpcClient() {
        @Override
        public CompletionStage<UPayload> invokeMethod(UUri topic, UPayload payload, CallOptions options) {
            Any any = Any.pack(Int32Value.of(42));
            UPayload data = UPayload.newBuilder()
                .setFormat(UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF)
                .setValue(any.toByteString())
                .build();
            return CompletableFuture.completedFuture(data);
        }
    };


    RpcClient WithNullInPayload = new RpcClient() {
        @Override
        public CompletionStage<UPayload> invokeMethod(UUri topic, UPayload payload, CallOptions options) {
            return CompletableFuture.completedFuture(null);
        }
    };

    private static io.cloudevents.v1.proto.CloudEvent buildCloudEvent() {
        return io.cloudevents.v1.proto.CloudEvent.newBuilder().setSpecVersion("1.0").setId("HARTLEY IS THE BEST")
                .setSource("http://example.com").build();
    }

    private static UPayload buildUPayload() {
        Any any = Any.pack(buildCloudEvent());
        return UPayload.newBuilder()
                .setFormat(UPayloadFormat.UPAYLOAD_FORMAT_PROTOBUF)
                .setValue(any.toByteString())
                .build();
    }

    private static UUri buildTopic() {
        return LongUriSerializer.instance().deserialize("//vcu.vin/hartley/1/rpc.Raise");
    }

    private static CallOptions buildCallOptions() {
        return CallOptions.newBuilder()
                .withTimeout(1000)
                .build();

    }

    private static CompletionStage<io.cloudevents.v1.proto.CloudEvent> rpcResponse(
            CompletionStage<UPayload> invokeMethodResponse) {

        final CompletionStage<io.cloudevents.v1.proto.CloudEvent> stubReturnValue = invokeMethodResponse.handle(
                (payload, exception) -> {
                    Any any;
                    try {
                        any = Any.parseFrom(payload.getValue());
                    } catch (InvalidProtocolBufferException e) {
                        throw new RuntimeException(e.getMessage(), e);
                    }

                    // invoke method had some unexpected problem.
                    if (exception != null) {
                        throw new RuntimeException(exception.getMessage(), exception);
                    }

                    // test to see if we have expected type
                    if (any.is(io.cloudevents.v1.proto.CloudEvent.class)) {
                        try {
                            return any.unpack(io.cloudevents.v1.proto.CloudEvent.class);
                        } catch (InvalidProtocolBufferException e) {
                            throw new RuntimeException(e.getMessage(), e);
                        }
                    }

                    // this will be called only if expected return type is not status, but status was returned to
                    // indicate a problem.
                    if (any.is(UStatus.class)) {
                        try {
                            UStatus status = any.unpack(UStatus.class);
                            throw new RuntimeException(String.format("Error returned, status code: [%s], message: [%s]",
                                    status.getCode(), status.getMessage()));
                        } catch (InvalidProtocolBufferException e) {
                            throw new RuntimeException(
                                    String.format("%s [%s]", e.getMessage(), "com.google.grpc.UStatus.class"), e);
                        }
                    }

                    throw new RuntimeException(String.format("Unknown payload type [%s]", any.getTypeUrl()));

                });

        return stubReturnValue;
    }

    @Test
    void test_compose_happy_path() {
        UPayload payload = buildUPayload();
        final CompletionStage<RpcResult<Int32Value>> rpcResponse = RpcMapper.mapResponseToResult(
                        ReturnsNumber3.invokeMethod(buildTopic(), payload, buildCallOptions()), Int32Value.class)
                .thenApply(ur -> ur.map(i -> Int32Value.of(i.getValue() + 5))).exceptionally(exception -> {
                    System.out.println("in exceptionally");
                    return RpcResult.failure("boom", exception);
                });
        assertFalse(rpcResponse.toCompletableFuture().isCompletedExceptionally());
        final CompletionStage<Void> test = rpcResponse.thenAccept(RpcResult -> {
            assertTrue(RpcResult.isSuccess());
            assertEquals(Int32Value.of(8), RpcResult.successValue());
        });
        assertFalse(test.toCompletableFuture().isCompletedExceptionally());
    }

    @Test
    void test_compose_that_returns_status() throws ExecutionException, InterruptedException {
        UPayload payload = buildUPayload();
        final CompletionStage<RpcResult<Int32Value>> rpcResponse = RpcMapper.mapResponseToResult(
                        WithUStatusCodeInsteadOfHappyPath.invokeMethod(buildTopic(), payload, buildCallOptions()),
                        Int32Value.class).thenApply(ur -> ur.map(i -> Int32Value.of(i.getValue() + 5)))
                .exceptionally(exception -> {
                    System.out.println("in exceptionally");
                    return RpcResult.failure("boom", exception);
                });
        assertFalse(rpcResponse.toCompletableFuture().isCompletedExceptionally());
        final CompletionStage<Void> test = rpcResponse.thenAccept(RpcResult -> {
            assertTrue(RpcResult.isFailure());
            assertEquals(UCode.INVALID_ARGUMENT, RpcResult.failureValue().getCode());
            assertEquals("boom", RpcResult.failureValue().getMessage());
        });
        assertFalse(test.toCompletableFuture().isCompletedExceptionally());
        assertEquals(rpcResponse.toCompletableFuture().get().failureValue().getCode(), UCode.INVALID_ARGUMENT);
        assertFalse(test.toCompletableFuture().isCompletedExceptionally());
    }

    @Test
    void test_compose_with_failure() throws Exception {
        UPayload payload = buildUPayload();
        final CompletionStage<RpcResult<Int32Value>> rpcResponse = RpcMapper.mapResponseToResult(
                        ThatCompletesWithAnException.invokeMethod(buildTopic(), payload, buildCallOptions()),
                        Int32Value.class)
                .thenApply(ur -> ur.map(i -> Int32Value.of(i.getValue() + 5)));
        assertTrue(rpcResponse.toCompletableFuture().get().isFailure());
        UStatus status = UStatus.newBuilder().setCode(UCode.UNKNOWN).setMessage("Boom").build();
        assertEquals(status, rpcResponse.toCompletableFuture().get().failureValue());
    }

    @Test
    void test_compose_with_failure_transform_Exception() throws Exception {
        UPayload payload = buildUPayload();
        final CompletionStage<RpcResult<Int32Value>> rpcResponse = RpcMapper.mapResponseToResult(
                        ThatCompletesWithAnException.invokeMethod(buildTopic(), payload, buildCallOptions()),
                        Int32Value.class)
                .thenApply(ur -> ur.map(i -> Int32Value.of(i.getValue() + 5))).exceptionally(exception -> {
                    System.out.println("in exceptionally");
                    return RpcResult.failure("boom", exception);
                });

        final CompletionStage<Void> test = rpcResponse.thenAccept(RpcResult -> {
            assertTrue(RpcResult.isFailure());
            assertEquals(UCode.UNKNOWN, RpcResult.failureValue().getCode());
            assertEquals("boom", RpcResult.failureValue().getMessage());
        });
        assertTrue(test.toCompletableFuture().isCompletedExceptionally());
    }

    @Test
    void test_success_invoke_method_happy_flow_using_mapResponseToRpcResponse() {
        UPayload payload = buildUPayload();

        final CompletionStage<RpcResult<io.cloudevents.v1.proto.CloudEvent>> rpcResponse =
                RpcMapper.mapResponseToResult(
                HappyPath.invokeMethod(buildTopic(), payload, buildCallOptions()),
                io.cloudevents.v1.proto.CloudEvent.class);

        assertFalse(rpcResponse.toCompletableFuture().isCompletedExceptionally());
        final CompletionStage<Void> test = rpcResponse.thenAccept(RpcResult -> {
            assertTrue(RpcResult.isSuccess());
            assertEquals(buildCloudEvent(), RpcResult.successValue());
        });
        assertFalse(test.toCompletableFuture().isCompletedExceptionally());
    }

    @Test
    void test_fail_invoke_method_when_invoke_method_returns_a_status_using_mapResponseToRpcResponse() {
        UPayload payload = buildUPayload();
        final CompletionStage<RpcResult<io.cloudevents.v1.proto.CloudEvent>> rpcResponse =
                RpcMapper.mapResponseToResult(
                WithUStatusCodeInsteadOfHappyPath.invokeMethod(buildTopic(), payload, buildCallOptions()),
                io.cloudevents.v1.proto.CloudEvent.class);

        assertFalse(rpcResponse.toCompletableFuture().isCompletedExceptionally());
        final CompletionStage<Void> test = rpcResponse.thenAccept(RpcResult -> {
            assertTrue(RpcResult.isFailure());
            assertEquals(UCode.INVALID_ARGUMENT, RpcResult.failureValue().getCode());
            assertEquals("boom", RpcResult.failureValue().getMessage());
        });
        assertFalse(test.toCompletableFuture().isCompletedExceptionally());
    }

    @Test
    void test_fail_invoke_method_when_invoke_method_threw_an_exception_using_mapResponseToRpcResponse() throws Exception {
        UPayload payload = buildUPayload();
        final CompletionStage<RpcResult<io.cloudevents.v1.proto.CloudEvent>> rpcResponse =
                RpcMapper.mapResponseToResult(
                ThatCompletesWithAnException.invokeMethod(buildTopic(), payload, buildCallOptions()),
                io.cloudevents.v1.proto.CloudEvent.class);

        assertTrue(rpcResponse.toCompletableFuture().get().isFailure());
        UStatus status = UStatus.newBuilder().setCode(UCode.UNKNOWN).setMessage("Boom").build();
        assertEquals(status, rpcResponse.toCompletableFuture().get().failureValue());

    }

    @Test
    void test_fail_invoke_method_when_invoke_method_returns_a_bad_proto_using_mapResponseToRpcResponse()
            throws Exception {
        UPayload payload = buildUPayload();
        final CompletionStage<RpcResult<io.cloudevents.v1.proto.CloudEvent>> rpcResponse =
                RpcMapper.mapResponseToResult(
                ThatReturnsTheWrongProto.invokeMethod(buildTopic(), payload, buildCallOptions()),
                io.cloudevents.v1.proto.CloudEvent.class);

        assertTrue(rpcResponse.toCompletableFuture().get().isFailure());
        UStatus status = UStatus.newBuilder().setCode(UCode.UNKNOWN).setMessage("Unknown payload type [type.googleapis.com/google.protobuf.Int32Value]. Expected [io.cloudevents.v1.proto.CloudEvent]").build();
        assertEquals(status, rpcResponse.toCompletableFuture().get().failureValue());

    }

    @Test
    void test_success_invoke_method_happy_flow_using_mapResponse() {
        UPayload payload = buildUPayload();
        final CompletionStage<io.cloudevents.v1.proto.CloudEvent> rpcResponse = RpcMapper.mapResponse(
                HappyPath.invokeMethod(buildTopic(), payload, buildCallOptions()),
                io.cloudevents.v1.proto.CloudEvent.class);

        assertFalse(rpcResponse.toCompletableFuture().isCompletedExceptionally());
        final CompletionStage<Void> test = rpcResponse.thenAccept(
                cloudEvent -> assertEquals(buildCloudEvent(), cloudEvent));
        assertFalse(test.toCompletableFuture().isCompletedExceptionally());
    }

    @Test
    void test_fail_invoke_method_when_invoke_method_returns_a_status_using_mapResponse() {
        UPayload payload = buildUPayload();
        final CompletionStage<io.cloudevents.v1.proto.CloudEvent> rpcResponse = RpcMapper.mapResponse(
                WithUStatusCodeInsteadOfHappyPath.invokeMethod(buildTopic(), payload, buildCallOptions()),
                io.cloudevents.v1.proto.CloudEvent.class);

        assertTrue(rpcResponse.toCompletableFuture().isCompletedExceptionally());

        Exception exception = assertThrows(java.util.concurrent.ExecutionException.class, rpcResponse.toCompletableFuture()::get);
        assertEquals(exception.getMessage(),
                "java.lang.RuntimeException: Unknown payload type [type.googleapis.com/uprotocol.v1.UStatus]. Expected " +
                        "[io.cloudevents.v1.proto.CloudEvent]");
    }

    @Test
    void test_fail_invoke_method_when_invoke_method_threw_an_exception_using_mapResponse() {
        UPayload payload = buildUPayload();
        final CompletionStage<io.cloudevents.v1.proto.CloudEvent> rpcResponse = RpcMapper.mapResponse(
                ThatCompletesWithAnException.invokeMethod(buildTopic(), payload, buildCallOptions()),
                io.cloudevents.v1.proto.CloudEvent.class);

        assertTrue(rpcResponse.toCompletableFuture().isCompletedExceptionally());
        Exception exception = assertThrows(java.util.concurrent.ExecutionException.class, rpcResponse.toCompletableFuture()::get);
        assertEquals(exception.getMessage(), "java.lang.RuntimeException: Boom");
    }

    @Test
    void test_fail_invoke_method_when_invoke_method_returns_a_bad_proto_using_mapResponse() {
        UPayload payload = buildUPayload();
        final CompletionStage<io.cloudevents.v1.proto.CloudEvent> rpcResponse = RpcMapper.mapResponse(
                ThatReturnsTheWrongProto.invokeMethod(buildTopic(), payload, buildCallOptions()),
                io.cloudevents.v1.proto.CloudEvent.class);

        assertTrue(rpcResponse.toCompletableFuture().isCompletedExceptionally());
        Exception exception = assertThrows(java.util.concurrent.ExecutionException.class, rpcResponse.toCompletableFuture()::get);
        assertEquals(exception.getMessage(),
                "java.lang.RuntimeException: Unknown payload type [type.googleapis.com/google.protobuf.Int32Value]. " +
                        "Expected [io.cloudevents.v1.proto.CloudEvent]");
    }

    @Test
    void test_success_invoke_method_happy_flow() {
        //Stub code
        UPayload data = buildUPayload();
        final CompletionStage<UPayload> rpcResponse = HappyPath.invokeMethod(buildTopic(), data, buildCallOptions());

        final CompletionStage<io.cloudevents.v1.proto.CloudEvent> stubReturnValue = rpcResponse.handle(
                (payload, exception) -> {
                    Any any;
                    assertTrue(true);
                    assertFalse(true);

                    try {
                        any = Any.parseFrom(payload.getValue());
                        // happy flow, no exception
                        assertNull(exception);

                        // check the payload is not uprotocol.v1.UStatus
                        assertFalse(any.is(UStatus.class));

                        // check the payload is the cloud event we build
                        assertTrue(any.is(io.cloudevents.v1.proto.CloudEvent.class));

                        return any.unpack(io.cloudevents.v1.proto.CloudEvent.class);
                    } catch (InvalidProtocolBufferException e) {
                        throw new RuntimeException(e);
                    }
                });

        stubReturnValue.thenAccept(cloudEvent -> assertEquals(buildUPayload(), cloudEvent));

    }

    @Test
    void test_fail_invoke_method_when_invoke_method_returns_a_status() {
        //Stub code
        UPayload data = buildUPayload();
        final CompletionStage<UPayload> rpcResponse = WithUStatusCodeInsteadOfHappyPath.invokeMethod(buildTopic(),
                data, buildCallOptions());

        final CompletionStage<io.cloudevents.v1.proto.CloudEvent> stubReturnValue = rpcResponse.handle(
                (payload, exception) -> {
                    try {
                        Any any = Any.parseFrom(payload.getValue());
                        // happy flow, no exception
                        assertNull(exception);

                        // check the payload not uprotocol.v1.UStatus
                        assertTrue(any.is(UStatus.class));

                        // check the payload is not the type we expected
                        assertFalse(any.is(io.cloudevents.v1.proto.CloudEvent.class));

                        // we know it is a UStatus - so let's unpack it

                        UStatus status = any.unpack(UStatus.class);
                        throw new RuntimeException(String.format("Error returned, status code: [%s], message: [%s]",
                                status.getCode(), status.getMessage()));
                    } catch (InvalidProtocolBufferException e) {
                        throw new RuntimeException(e);
                    }
                });

        assertTrue(stubReturnValue.toCompletableFuture().isCompletedExceptionally());

        Exception exception = assertThrows(java.util.concurrent.ExecutionException.class, stubReturnValue.toCompletableFuture()::get);
        assertEquals(exception.getMessage(),
                "java.lang.RuntimeException: Error returned, status code: [INVALID_ARGUMENT], message: [boom]");

    }

    @Test
    void test_fail_invoke_method_when_invoke_method_threw_an_exception() {
        //Stub code
        UPayload data = buildUPayload();
        final CompletionStage<UPayload> rpcResponse = ThatCompletesWithAnException.invokeMethod(buildTopic(), data,
                buildCallOptions());

        final CompletionStage<io.cloudevents.v1.proto.CloudEvent> stubReturnValue = rpcResponse.handle(
                (payload, exception) -> {
                    // exception was thrown
                    assertNotNull(exception);

                    assertNull(payload);

                    throw new RuntimeException(exception.getMessage(), exception);

                });

        assertTrue(stubReturnValue.toCompletableFuture().isCompletedExceptionally());

        Exception exception = assertThrows(java.util.concurrent.ExecutionException.class, stubReturnValue.toCompletableFuture()::get);
        assertEquals(exception.getMessage(), "java.lang.RuntimeException: Boom");

    }

    @Test
    void test_fail_invoke_method_when_invoke_method_returns_a_bad_proto() {
        //Stub code
        UPayload data = buildUPayload();
        final CompletionStage<UPayload> rpcResponse = ThatReturnsTheWrongProto.invokeMethod(buildTopic(), data,
                buildCallOptions());

        final CompletionStage<io.cloudevents.v1.proto.CloudEvent> stubReturnValue = rpcResponse.handle(
                (payload, exception) -> {
                    try {
                        Any any = Any.parseFrom(payload.getValue());
                        // happy flow, no exception
                        assertNull(exception);

                        // check the payload is not uprotocol.v1.UStatus
                        assertFalse(any.is(UStatus.class));

                        // check the payload is the cloud event we build
                        assertFalse(any.is(io.cloudevents.v1.proto.CloudEvent.class));

                        return any.unpack(io.cloudevents.v1.proto.CloudEvent.class);
                    } catch (InvalidProtocolBufferException e) {
                        throw new RuntimeException(
                                String.format("%s [%s]", e.getMessage(), "io.cloudevents.v1.proto.CloudEvent.class"),
                                e);
                    }
                });

        assertTrue(stubReturnValue.toCompletableFuture().isCompletedExceptionally());

        Exception exception = assertThrows(java.util.concurrent.ExecutionException.class, stubReturnValue.toCompletableFuture()::get);
        assertEquals(exception.getMessage(),
                "java.lang.RuntimeException: Type of the Any message does not match the given class. [io.cloudevents" +
                        ".v1.proto.CloudEvent.class]");

    }

    @Test
    @DisplayName("Invoke method that returns successfully with null in the payload")
    void test_success_invoke_method_that_has_null_payload_mapResponse() {
        UPayload payload = buildUPayload();
        final CompletionStage<io.cloudevents.v1.proto.CloudEvent> rpcResponse = RpcMapper.mapResponse(
                WithNullInPayload.invokeMethod(buildTopic(), payload, buildCallOptions()),
                io.cloudevents.v1.proto.CloudEvent.class);

        assertTrue(rpcResponse.toCompletableFuture().isCompletedExceptionally());
        Exception exception = assertThrows(java.util.concurrent.ExecutionException.class, rpcResponse.toCompletableFuture()::get);
        assertEquals(exception.getMessage(),
                "java.lang.RuntimeException: Server returned a null payload. Expected io.cloudevents.v1.proto" +
                        ".CloudEvent");

    }

    @Test
    @DisplayName("Invoke method that returns successfully with null in the payload, mapResponseToResult")
    void test_success_invoke_method_that_has_null_payload_mapResponseToResultToRpcResponse()
            throws Exception {
        UPayload payload = buildUPayload();
        final CompletionStage<RpcResult<io.cloudevents.v1.proto.CloudEvent>> rpcResponse =
                RpcMapper.mapResponseToResult(
                WithNullInPayload.invokeMethod(buildTopic(), payload, buildCallOptions()),
                io.cloudevents.v1.proto.CloudEvent.class);

        assertTrue(rpcResponse.toCompletableFuture().get().isFailure());
        UStatus status = UStatus.newBuilder().setCode(UCode.UNKNOWN).setMessage("Server returned a null payload. Expected io.cloudevents.v1.proto.CloudEvent").build();
        assertEquals(status, rpcResponse.toCompletableFuture().get().failureValue());


    }

    @Test
    @DisplayName("Invoke method that expects a UStatus payload and returns successfully with OK UStatus in the payload")
    void test_success_invoke_method_happy_flow_that_returns_status_using_mapResponse() {
        UPayload payload = buildUPayload();
        final CompletionStage<UStatus> rpcResponse = RpcMapper.mapResponse(
                WithUStatusCodeHappyPath.invokeMethod(buildTopic(), payload, buildCallOptions()), UStatus.class);

        assertFalse(rpcResponse.toCompletableFuture().isCompletedExceptionally());
        final CompletionStage<Void> test = rpcResponse.thenAccept(status -> {
            assertEquals(UCode.OK, status.getCode());
            assertEquals("all good", status.getMessage());
        });
        assertFalse(test.toCompletableFuture().isCompletedExceptionally());
    }

    @Test
    @DisplayName("Invoke method that expects a UStatus payload and returns successfully with OK UStatus in the payload," +
            " mapResponseToResult")
    void test_success_invoke_method_happy_flow_that_returns_status_using_mapResponseToResultToRpcResponse() {
        UPayload payload = buildUPayload();
        final CompletionStage<RpcResult<UStatus>> rpcResponse = RpcMapper.mapResponseToResult(
                WithUStatusCodeHappyPath.invokeMethod(buildTopic(), payload, buildCallOptions()), UStatus.class);

        assertFalse(rpcResponse.toCompletableFuture().isCompletedExceptionally());
        final CompletionStage<Void> test = rpcResponse.thenAccept(RpcResult -> {
            assertTrue(RpcResult.isSuccess());
            assertEquals(UCode.OK, RpcResult.successValue().getCode());
            assertEquals("all good", RpcResult.successValue().getMessage());
        });
        assertFalse(test.toCompletableFuture().isCompletedExceptionally());
    }

    @Test
    void test_unpack_payload_failed() {
        Any payload = Any.pack(Int32Value.of(3));
        Exception exception = assertThrows(RuntimeException.class,
                () -> RpcMapper.unpackPayload(payload, UStatus.class));
        assertEquals(exception.getMessage(),
                "Type of the Any message does not match the given class. [org.eclipse.uprotocol.v1.UStatus]");
    }

    @Test
    @DisplayName("test invalid payload that is not of type any")
    void test_invalid_payload_that_is_not_type_any() {
        UPayload payload = buildUPayload();
        final CompletionStage<UStatus> rpcResponse = RpcMapper.mapResponse(
                ThatBarfsCrapyPayload.invokeMethod(buildTopic(), payload, buildCallOptions()), UStatus.class);

        assertTrue(rpcResponse.toCompletableFuture().isCompletedExceptionally());
        Exception exception = assertThrows(java.util.concurrent.ExecutionException.class, rpcResponse.toCompletableFuture()::get);
        assertEquals(exception.getMessage(),
                "java.lang.RuntimeException: Protocol message contained an invalid tag (zero). [org.eclipse.uprotocol.v1" +
                        ".UStatus]");
        ;
    }

    @Test
    @DisplayName("test invalid payload that is not of type any")
    void test_invalid_payload_that_is_not_type_any_map_to_result() throws Exception {
        UPayload payload = buildUPayload();
        final CompletionStage<RpcResult<UStatus>> rpcResponse = RpcMapper.mapResponseToResult(
                ThatBarfsCrapyPayload.invokeMethod(buildTopic(), payload, buildCallOptions()), UStatus.class);

        assertTrue(rpcResponse.toCompletableFuture().get().isFailure());
        UStatus status = UStatus.newBuilder().setCode(UCode.UNKNOWN).setMessage("Protocol message contained an invalid tag (zero). [org.eclipse.uprotocol.v1.UStatus]").build();
        assertEquals(status, rpcResponse.toCompletableFuture().get().failureValue());
    }

    @Test
    void what_the_stub_looks_like() throws InterruptedException {

        RpcClient client = new RpcClient() {
            @Override
            public CompletionStage<UPayload> invokeMethod(UUri topic, UPayload payload, CallOptions options) {
                return CompletableFuture.completedFuture(UPayload.getDefaultInstance());
            }
        };

        //Stub code

        UPayload payload = buildUPayload();
        final CompletionStage<UPayload> invokeMethodResponse = client.invokeMethod(buildTopic(), payload,
                buildCallOptions());

        CompletionStage<io.cloudevents.v1.proto.CloudEvent> stubReturnValue = rpcResponse(invokeMethodResponse);
        assertFalse(stubReturnValue.toCompletableFuture().isCancelled());

    }

}