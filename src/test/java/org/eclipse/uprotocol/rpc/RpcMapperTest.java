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
import com.google.protobuf.Int32Value;
import com.google.rpc.Code;
import com.google.rpc.Status;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class RpcMapperTest {

    Rpc uLinkReturnsNumber3 = new Rpc() {
        @Override
        public CompletableFuture<Any> invokeMethod(CloudEvent requestEvent) {
            Any payload = Any.pack(Int32Value.of(3));
            return CompletableFuture.completedFuture(payload);
        }
        @Override
        public String getResponseUri() {
            return "";
        }
    };

    Rpc uLinkHappyPath = new Rpc() {
        @Override
        public CompletableFuture<Any> invokeMethod(CloudEvent requestEvent) {
            Any payload = Any.pack(buildProtoPayloadForTest());
            return CompletableFuture.completedFuture(payload);
        }

        @Override
        public String getResponseUri() {
            return "";
        }
    };

    Rpc uLinkWithStatusCodeInsteadOfHappyPath = new Rpc() {
        @Override
        public CompletableFuture<Any> invokeMethod(CloudEvent requestEvent) {
            Any payload = Any.pack(Status.newBuilder()
                    .setCode(Code.INVALID_ARGUMENT_VALUE)
                    .setMessage("boom")
                    .build());
            return CompletableFuture.completedFuture(payload);
        }

        @Override
        public String getResponseUri() {
            return "";
        }
    };

    Rpc uLinkWithStatusCodeHappyPath = new Rpc() {
        @Override
        public CompletableFuture<Any> invokeMethod(CloudEvent requestEvent) {
            Any payload = Any.pack(Status.newBuilder()
                    .setCode(Code.OK_VALUE)
                    .setMessage("all good")
                    .build());
            return CompletableFuture.completedFuture(payload);
        }

        @Override
        public String getResponseUri() {
            return "";
        }
    };

    Rpc uLinkWithStatusCodeThatFailedHappyPath = new Rpc() {
        @Override
        public CompletableFuture<Any> invokeMethod(CloudEvent requestEvent) {
            Any payload = Any.pack(Status.newBuilder()
                    .setCode(Code.INVALID_ARGUMENT_VALUE)
                    .setMessage("boom")
                    .build());
            return CompletableFuture.completedFuture(payload);
        }

        @Override
        public String getResponseUri() {
            return "";
        }
    };

    Rpc uLinkWithNullInPayload = new Rpc() {
        @Override
        public CompletableFuture<Any> invokeMethod(CloudEvent requestEvent) {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public String getResponseUri() {
            return "";
        }
    };

    Rpc uLinkThatCompletesWithAnException = new Rpc() {
        @Override
        public CompletableFuture<Any> invokeMethod(CloudEvent requestEvent) {
            return CompletableFuture.failedFuture(new RuntimeException("Boom"));
        }

        @Override
        public String getResponseUri() {
            return "";
        }
    };

    Rpc uLinkThatReturnsTheWrongProto = new Rpc() {
        @Override
        public CompletableFuture<Any> invokeMethod(CloudEvent requestEvent) {
            Any payload = Any.pack(Int32Value.of(42));
            return CompletableFuture.completedFuture(payload);
        }

        @Override
        public String getResponseUri() {
            return "";
        }
    };

    @Test
    void test_compose_happy_path() {
        CloudEvent request = buildCloudEventForTest().build();
        final CompletableFuture<RpcResult<Int32Value>> rpcResponse =
                RpcMapper.mapResponseToResult(uLinkReturnsNumber3.invokeMethod(request), Int32Value.class)
                        .thenApply(ur -> ur.map(i -> Int32Value.of(i.getValue()+5)))
                        .exceptionally(exception -> {
                            System.out.println("in exceptionally");
                            return RpcResult.failure("boom", exception);
                        });
        assertFalse(rpcResponse.isCompletedExceptionally());
        final CompletableFuture<Void> test = rpcResponse.thenAccept(RpcResult -> {
            assertTrue(RpcResult.isSuccess());
            assertEquals(Int32Value.of(8), RpcResult.successValue());
        });
        assertFalse(test.isCompletedExceptionally());
    }

    @Test
    void test_compose_that_returns_status() throws ExecutionException, InterruptedException {
        CloudEvent request = buildCloudEventForTest().build();
        final CompletableFuture<RpcResult<Int32Value>> rpcResponse =
                RpcMapper.mapResponseToResult(uLinkWithStatusCodeInsteadOfHappyPath.invokeMethod(request), Int32Value.class)
                        .thenApply(ur -> ur.map(i -> Int32Value.of(i.getValue()+5)))
                        .exceptionally(exception -> {
                            System.out.println("in exceptionally");
                            return RpcResult.failure("boom", exception);
                        });
        assertFalse(rpcResponse.isCompletedExceptionally());
        final CompletableFuture<Void> test = rpcResponse.thenAccept(RpcResult -> {
            assertTrue(RpcResult.isFailure());
            assertEquals(Code.INVALID_ARGUMENT_VALUE, RpcResult.failureValue().getCode());
            assertEquals("boom", RpcResult.failureValue().getMessage());
        });
        assertEquals(rpcResponse.get().failureValue().getCode(), Code.INVALID_ARGUMENT_VALUE);
        assertFalse(test.isCompletedExceptionally());
    }

    @Test
    void test_compose_with_failure() {
        CloudEvent request = buildCloudEventForTest().build();
        final CompletableFuture<RpcResult<Int32Value>> rpcResponse =
                RpcMapper.mapResponseToResult(uLinkThatCompletesWithAnException.invokeMethod(request), Int32Value.class)
                        .thenApply(ur -> ur.map(i -> Int32Value.of(i.getValue()+5)));
        assertTrue(rpcResponse.isCompletedExceptionally());
        Exception exception = assertThrows(java.util.concurrent.ExecutionException.class, rpcResponse::get);
        assertEquals(exception.getMessage(), "java.lang.RuntimeException: Boom");
    }

    @Test
    void test_compose_with_failure_transform_Exception() {
        CloudEvent request = buildCloudEventForTest().build();
        final CompletableFuture<RpcResult<Int32Value>> rpcResponse =
                RpcMapper.mapResponseToResult(uLinkThatCompletesWithAnException.invokeMethod(request), Int32Value.class)
                        .thenApply(ur -> ur.map(i -> Int32Value.of(i.getValue()+5)))
                        .exceptionally(exception -> {
                            System.out.println("in exceptionally");
                            return RpcResult.failure("boom", exception);
                        });
        assertFalse(rpcResponse.isCompletedExceptionally());
        final CompletableFuture<Void> test = rpcResponse.thenAccept(RpcResult -> {
            assertTrue(RpcResult.isFailure());
            assertEquals(Code.UNKNOWN_VALUE, RpcResult.failureValue().getCode());
            assertEquals("boom", RpcResult.failureValue().getMessage());
        });
        assertFalse(test.isCompletedExceptionally());
    }

    @Test
    @DisplayName("Invoke method that returns the expected class successfully")
    void test_success_invoke_method_happy_flow_using_mapResponse() {
        CloudEvent request = buildCloudEventForTest().build();
        final CompletableFuture<io.cloudevents.v1.proto.CloudEvent> rpcResponse =
                RpcMapper.mapResponse(uLinkHappyPath.invokeMethod(request), io.cloudevents.v1.proto.CloudEvent.class);

        assertFalse(rpcResponse.isCompletedExceptionally());
        final CompletableFuture<Void> test = rpcResponse.thenAccept(cloudEvent -> assertEquals(buildProtoPayloadForTest(), cloudEvent));
        assertFalse(test.isCompletedExceptionally());
    }

    @Test
    @DisplayName("Invoke method that returns successfully with null in the payload")
    void test_success_invoke_method_that_has_null_payload_mapResponse() {
        CloudEvent request = buildCloudEventForTest().build();
        final CompletableFuture<io.cloudevents.v1.proto.CloudEvent> rpcResponse =
                RpcMapper.mapResponse(uLinkWithNullInPayload.invokeMethod(request), io.cloudevents.v1.proto.CloudEvent.class);

        assertTrue(rpcResponse.isCompletedExceptionally());
        Exception exception = assertThrows(java.util.concurrent.ExecutionException.class, rpcResponse::get);
        assertEquals(exception.getMessage(), "java.lang.RuntimeException: Server returned a null payload. Expected io.cloudevents.v1.proto.CloudEvent");

    }

    @Test
    @DisplayName("Invoke method that expects a Status payload and returns successfully with OK Status in the payload")
    void test_success_invoke_method_happy_flow_that_returns_status_using_mapResponse() {
        CloudEvent request = buildCloudEventForTest().build();
        final CompletableFuture<Status> rpcResponse =
                RpcMapper.mapResponse(uLinkWithStatusCodeHappyPath.invokeMethod(request), Status.class);

        assertFalse(rpcResponse.isCompletedExceptionally());
        final CompletableFuture<Void> test = rpcResponse.thenAccept(status -> {
            assertEquals(Code.OK.getNumber(), status.getCode());
            assertEquals("all good", status.getMessage());
        });
        assertFalse(test.isCompletedExceptionally());
    }

    @Test
    @DisplayName("Invoke method that expects a Status payload and returns successfully with a not OK Status in the payload")
    void test_success_invoke_method_happy_flow_that_returns_failed_status_using_mapResponse() {
        CloudEvent request = buildCloudEventForTest().build();
        final CompletableFuture<Status> rpcResponse =
                RpcMapper.mapResponse(uLinkWithStatusCodeThatFailedHappyPath.invokeMethod(request), Status.class);

        assertFalse(rpcResponse.isCompletedExceptionally());
        final CompletableFuture<Void> test = rpcResponse.thenAccept(status -> {
            assertEquals(Code.INVALID_ARGUMENT_VALUE, status.getCode());
            assertEquals("boom", status.getMessage());
        });
        assertFalse(test.isCompletedExceptionally());
    }

    @Test
    @DisplayName("Invoke method that expects a CloudEvent payload and returns successfully with a Status in the payload")
    void test_fail_invoke_method_when_invoke_method_returns_a_status_using_mapResponse() {
        CloudEvent request = buildCloudEventForTest().build();
        final CompletableFuture<io.cloudevents.v1.proto.CloudEvent> rpcResponse =
                RpcMapper.mapResponse(uLinkWithStatusCodeInsteadOfHappyPath.invokeMethod(request), io.cloudevents.v1.proto.CloudEvent.class);

        assertTrue(rpcResponse.isCompletedExceptionally());
        Exception exception = assertThrows(java.util.concurrent.ExecutionException.class, rpcResponse::get);
        assertEquals(exception.getMessage(), "java.lang.RuntimeException: Unknown payload type [type.googleapis.com/google.rpc.Status]. " +
                "Expected [io.cloudevents.v1.proto.CloudEvent]");
    }

    @Test
    @DisplayName("Invoke method that throws an exception")
    void test_fail_invoke_method_when_invoke_method_threw_an_exception_using_mapResponse() {
        CloudEvent request = buildCloudEventForTest().build();
        final CompletableFuture<io.cloudevents.v1.proto.CloudEvent> rpcResponse =
                RpcMapper.mapResponse(uLinkThatCompletesWithAnException.invokeMethod(request), io.cloudevents.v1.proto.CloudEvent.class);

        assertTrue(rpcResponse.isCompletedExceptionally());
        Exception exception = assertThrows(java.util.concurrent.ExecutionException.class, rpcResponse::get);
        assertEquals(exception.getMessage(), "java.lang.RuntimeException: Boom");
    }

    @Test
    @DisplayName("Invoke method that expects a CloudEvent in the payload but gets an Int32Value")
    void test_fail_invoke_method_when_invoke_method_returns_a_bad_proto_using_mapResponse() {
        CloudEvent request = buildCloudEventForTest().build();
        final CompletableFuture<io.cloudevents.v1.proto.CloudEvent> rpcResponse =
                RpcMapper.mapResponse(uLinkThatReturnsTheWrongProto.invokeMethod(request), io.cloudevents.v1.proto.CloudEvent.class);

        assertTrue(rpcResponse.isCompletedExceptionally());
        Exception exception = assertThrows(java.util.concurrent.ExecutionException.class, rpcResponse::get);
        assertEquals(exception.getMessage(),
                "java.lang.RuntimeException: Unknown payload type [type.googleapis.com/google.protobuf.Int32Value]. Expected [io.cloudevents.v1.proto.CloudEvent]");
    }

    @Test
    @DisplayName("Invoke method that returns the expected class successfully, mapResponseToResult")
    void test_success_invoke_method_happy_flow_using_mapResponseToResultToRpcResponse() {
        CloudEvent request = buildCloudEventForTest().build();
        final CompletableFuture<RpcResult<io.cloudevents.v1.proto.CloudEvent>> rpcResponse =
                RpcMapper.mapResponseToResult(uLinkHappyPath.invokeMethod(request), io.cloudevents.v1.proto.CloudEvent.class);

        assertFalse(rpcResponse.isCompletedExceptionally());
        final CompletableFuture<Void> test = rpcResponse.thenAccept(RpcResult -> {
            assertTrue(RpcResult.isSuccess());
            assertEquals(buildProtoPayloadForTest(), RpcResult.successValue());
        });
        assertFalse(test.isCompletedExceptionally());
    }

    @Test
    @DisplayName("Invoke method that returns successfully with null in the payload, mapResponseToResult")
    void test_success_invoke_method_that_has_null_payload_mapResponseToResultToRpcResponse() {
        CloudEvent request = buildCloudEventForTest().build();
        final CompletableFuture<RpcResult<io.cloudevents.v1.proto.CloudEvent>> rpcResponse =
                RpcMapper.mapResponseToResult(uLinkWithNullInPayload.invokeMethod(request), io.cloudevents.v1.proto.CloudEvent.class);

        assertTrue(rpcResponse.isCompletedExceptionally());
        Exception exception = assertThrows(java.util.concurrent.ExecutionException.class, rpcResponse::get);
        assertEquals(exception.getMessage(), "java.lang.RuntimeException: Server returned a null payload. Expected io.cloudevents.v1.proto.CloudEvent");

    }

    @Test
    @DisplayName("Invoke method that expects a Status payload and returns successfully with OK Status in the payload, mapResponseToResult")
    void test_success_invoke_method_happy_flow_that_returns_status_using_mapResponseToResultToRpcResponse() {
        CloudEvent request = buildCloudEventForTest().build();
        final CompletableFuture<RpcResult<Status>> rpcResponse =
                RpcMapper.mapResponseToResult(uLinkWithStatusCodeHappyPath.invokeMethod(request), Status.class);

        assertFalse(rpcResponse.isCompletedExceptionally());
        final CompletableFuture<Void> test = rpcResponse.thenAccept(RpcResult -> {
            assertTrue(RpcResult.isSuccess());
            assertEquals(Code.OK.getNumber(), RpcResult.successValue().getCode());
            assertEquals("all good", RpcResult.successValue().getMessage());
        });
        assertFalse(test.isCompletedExceptionally());
    }

    @Test
    @DisplayName("Invoke method that expects a Status payload and returns successfully with a not OK Status in the payload, mapResponseToResult")
    void test_success_invoke_method_happy_flow_that_returns_failed_status_using_mapResponseToResultToRpcResponse() {
        CloudEvent request = buildCloudEventForTest().build();
        final CompletableFuture<RpcResult<Status>> rpcResponse =
                RpcMapper.mapResponseToResult(uLinkWithStatusCodeThatFailedHappyPath.invokeMethod(request), Status.class);

        assertFalse(rpcResponse.isCompletedExceptionally());
        final CompletableFuture<Void> test = rpcResponse.thenAccept(RpcResult -> {
            assertTrue(RpcResult.isFailure());
            assertEquals(Code.INVALID_ARGUMENT_VALUE, RpcResult.failureValue().getCode());
            assertEquals("boom", RpcResult.failureValue().getMessage());
        });
        assertFalse(test.isCompletedExceptionally());
    }

    @Test
    @DisplayName("Invoke method that expects a CloudEvent payload and returns successfully with a Status in the payload, mapResponseToResult")
    void test_fail_invoke_method_when_invoke_method_returns_a_status_using_mapResponseToResultToRpcResponse() {
        CloudEvent request = buildCloudEventForTest().build();
        final CompletableFuture<RpcResult<io.cloudevents.v1.proto.CloudEvent>> rpcResponse =
                RpcMapper.mapResponseToResult(uLinkWithStatusCodeInsteadOfHappyPath.invokeMethod(request), io.cloudevents.v1.proto.CloudEvent.class);

        assertFalse(rpcResponse.isCompletedExceptionally());
        final CompletableFuture<Void> test = rpcResponse.thenAccept(RpcResult -> {
            assertTrue(RpcResult.isFailure());
            assertEquals(Code.INVALID_ARGUMENT.getNumber(), RpcResult.failureValue().getCode());
            assertEquals("boom", RpcResult.failureValue().getMessage());
        });
        assertFalse(test.isCompletedExceptionally());
    }

    @Test
    @DisplayName("Invoke method that throws an exception, mapResponseToResult")
    void test_fail_invoke_method_when_invoke_method_threw_an_exception_using_mapResponseToResultToRpcResponse() {
        CloudEvent request = buildCloudEventForTest().build();
        final CompletableFuture<RpcResult<io.cloudevents.v1.proto.CloudEvent>> rpcResponse =
                RpcMapper.mapResponseToResult(uLinkThatCompletesWithAnException.invokeMethod(request), io.cloudevents.v1.proto.CloudEvent.class);

        assertTrue(rpcResponse.isCompletedExceptionally());
        Exception exception = assertThrows(java.util.concurrent.ExecutionException.class, rpcResponse::get);
        assertEquals(exception.getMessage(), "java.lang.RuntimeException: Boom");
    }

    @Test
    @DisplayName("Invoke method that expects a CloudEvent in the payload but gets an Int32Value, mapResponseToResult")
    void test_fail_invoke_method_when_invoke_method_returns_a_bad_proto_using_mapResponseToResultToRpcResponse() {
        CloudEvent request = buildCloudEventForTest().build();
        final CompletableFuture<RpcResult<io.cloudevents.v1.proto.CloudEvent>> rpcResponse =
                RpcMapper.mapResponseToResult(uLinkThatReturnsTheWrongProto.invokeMethod(request), io.cloudevents.v1.proto.CloudEvent.class);

        assertTrue(rpcResponse.isCompletedExceptionally());
        Exception exception = assertThrows(java.util.concurrent.ExecutionException.class, rpcResponse::get);
        assertEquals(exception.getMessage(),
                "java.lang.RuntimeException: Unknown payload type [type.googleapis.com/google.protobuf.Int32Value]. Expected [io.cloudevents.v1.proto.CloudEvent]");
    }

    @Test
    void test_unpack_payload_failed() {
        Any payload = Any.pack(Int32Value.of(3));
        Exception exception = assertThrows(RuntimeException.class, () -> RpcMapper.unpackPayload(payload, Status.class));
        assertEquals(exception.getMessage(),
                "Type of the Any message does not match the given class. [com.google.rpc.Status]");
    }

    private io.cloudevents.v1.proto.CloudEvent buildProtoPayloadForTest() {
        return io.cloudevents.v1.proto.CloudEvent.newBuilder()
                .setSpecVersion("1.0")
                .setId("hello")
                .setSource("http://example.com")
                .setType("example.demo")
                .setProtoData(Any.newBuilder().build())
                .putAttributes("ttl", io.cloudevents.v1.proto.CloudEvent.CloudEventAttributeValue.newBuilder()
                        .setCeString("3").build())
                .build();
    }

    private CloudEventBuilder buildCloudEventForTest() {
        return CloudEventBuilder.v1()
                .withId("hello")
                .withType("req.v1")
                .withSource(URI.create("//VCU.VIN/body.access"));
    }

}