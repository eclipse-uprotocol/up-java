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
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.rpc.Code;
import com.google.rpc.Status;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class RpcTest {

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

    private static <T extends Message> CompletableFuture<T> mapResponse(CompletableFuture<Any> responseFuture,
                                                                        Class<T> expectedClazz) {
        return responseFuture.handle((payload, exception) -> {
            // Unexpected exception
            if (exception != null) {
                throw new RuntimeException(exception.getMessage(), exception);
            }
            if (payload == null) {
                throw new RuntimeException("Server returned a null payload. Expected " + expectedClazz.getName());
            }
            // Expected type
            if (payload.is(expectedClazz)) {
                try {
                    return payload.unpack(expectedClazz);
                } catch (InvalidProtocolBufferException e) {
                    throw new RuntimeException(String.format("%s [%s]", e.getMessage(), expectedClazz.getName()), e);
                }
            }
            // Status instead of the expected one
            if (payload.is(Status.class)) {
                try {
                    Status status = payload.unpack(Status.class);
                    throw new RuntimeException(String.format("Error returned, status code: [%s], message: [%s]",
                            Code.forNumber(status.getCode()), status.getMessage()));
                } catch (InvalidProtocolBufferException e) {
                    throw new RuntimeException(String.format("%s [%s]", e.getMessage(), Status.class.getName()), e);
                }
            }
            // Some other type instead of the expected one
            throw new RuntimeException(String.format("Unknown payload type [%s]. Expected [%s]",
                    payload.getTypeUrl(), expectedClazz.getName()));
        });
    }

    private static <T extends Message> CompletableFuture<RpcResult<T>> mapResponseToRpcResponse(CompletableFuture<Any> responseFuture,
                                                                                                            Class<T> expectedClazz) {
        return responseFuture.handle((payload, exception) -> {
            // Unexpected exception
            if (exception != null) {
                throw new RuntimeException(exception.getMessage(), exception);
            }
            if (payload == null) {
                throw new RuntimeException("Server returned a null payload. Expected " + expectedClazz.getName());
            }
            // Expected type
            if (payload.is(expectedClazz)) {
                try {
                    return RpcResult.success(payload.unpack(expectedClazz));
                } catch (InvalidProtocolBufferException e) {
                    throw new RuntimeException(String.format("%s [%s]", e.getMessage(), expectedClazz.getName()), e);
                }
            }
            // Status instead of the expected one
            if (payload.is(Status.class)) {
                try {
                    return RpcResult.failure(payload.unpack(Status.class));
                } catch (InvalidProtocolBufferException e) {
                    throw new RuntimeException(String.format("%s [%s]", e.getMessage(), Status.class.getName()), e);
                }
            }
            // Some other type instead of the expected one
            throw new RuntimeException(String.format("Unknown payload type [%s]. Expected [%s]",
                    payload.getTypeUrl(), expectedClazz.getName()));
        });
    }

    @Test
    void test_compose_happy_path() {
        CloudEvent request = buildCloudEventForTest().build();
        final CompletableFuture<RpcResult<Int32Value>> rpcResponse =
                mapResponseToRpcResponse(uLinkReturnsNumber3.invokeMethod(request), Int32Value.class)
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
                mapResponseToRpcResponse(uLinkWithStatusCodeInsteadOfHappyPath.invokeMethod(request), Int32Value.class)
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
        assertFalse(test.isCompletedExceptionally());
        assertEquals(rpcResponse.get().failureValue().getCode(), Code.INVALID_ARGUMENT_VALUE);
        assertFalse(test.isCompletedExceptionally());
    }

    @Test
    void test_compose_with_failure() {
        CloudEvent request = buildCloudEventForTest().build();
        final CompletableFuture<RpcResult<Int32Value>> rpcResponse =
                mapResponseToRpcResponse(uLinkThatCompletesWithAnException.invokeMethod(request), Int32Value.class)
                        .thenApply(ur -> ur.map(i -> Int32Value.of(i.getValue()+5)));
        assertTrue(rpcResponse.isCompletedExceptionally());
        Exception exception = assertThrows(java.util.concurrent.ExecutionException.class, rpcResponse::get);
        assertEquals(exception.getMessage(), "java.lang.RuntimeException: Boom");
    }

    @Test
    void test_compose_with_failure_transform_Exception() {
        CloudEvent request = buildCloudEventForTest().build();
        final CompletableFuture<RpcResult<Int32Value>> rpcResponse =
                mapResponseToRpcResponse(uLinkThatCompletesWithAnException.invokeMethod(request), Int32Value.class)
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
    void test_success_invoke_method_happy_flow_using_mapResponseToRpcResponse() {
        CloudEvent request = buildCloudEventForTest().build();
        final CompletableFuture<RpcResult<io.cloudevents.v1.proto.CloudEvent>> rpcResponse =
                mapResponseToRpcResponse(uLinkHappyPath.invokeMethod(request), io.cloudevents.v1.proto.CloudEvent.class);

        assertFalse(rpcResponse.isCompletedExceptionally());
        final CompletableFuture<Void> test = rpcResponse.thenAccept(RpcResult -> {
            assertTrue(RpcResult.isSuccess());
            assertEquals(buildProtoPayloadForTest(), RpcResult.successValue());
        });
        assertFalse(test.isCompletedExceptionally());
    }

    @Test
    void test_fail_invoke_method_when_invoke_method_returns_a_status_using_mapResponseToRpcResponse() {
        CloudEvent request = buildCloudEventForTest().build();
        final CompletableFuture<RpcResult<io.cloudevents.v1.proto.CloudEvent>> rpcResponse =
                mapResponseToRpcResponse(uLinkWithStatusCodeInsteadOfHappyPath.invokeMethod(request), io.cloudevents.v1.proto.CloudEvent.class);

        assertFalse(rpcResponse.isCompletedExceptionally());
        final CompletableFuture<Void> test = rpcResponse.thenAccept(RpcResult -> {
            assertTrue(RpcResult.isFailure());
            assertEquals(Code.INVALID_ARGUMENT.getNumber(), RpcResult.failureValue().getCode());
            assertEquals("boom", RpcResult.failureValue().getMessage());
        });
        assertFalse(test.isCompletedExceptionally());
    }

    @Test
    void test_fail_invoke_method_when_invoke_method_threw_an_exception_using_mapResponseToRpcResponse() {
        CloudEvent request = buildCloudEventForTest().build();
        final CompletableFuture<RpcResult<io.cloudevents.v1.proto.CloudEvent>> rpcResponse =
                mapResponseToRpcResponse(uLinkThatCompletesWithAnException.invokeMethod(request), io.cloudevents.v1.proto.CloudEvent.class);

        assertTrue(rpcResponse.isCompletedExceptionally());
        Exception exception = assertThrows(java.util.concurrent.ExecutionException.class, rpcResponse::get);
        assertEquals(exception.getMessage(), "java.lang.RuntimeException: Boom");
    }

    @Test
    void test_fail_invoke_method_when_invoke_method_returns_a_bad_proto_using_mapResponseToRpcResponse() {
        CloudEvent request = buildCloudEventForTest().build();
        final CompletableFuture<RpcResult<io.cloudevents.v1.proto.CloudEvent>> rpcResponse =
                mapResponseToRpcResponse(uLinkThatReturnsTheWrongProto.invokeMethod(request), io.cloudevents.v1.proto.CloudEvent.class);

        assertTrue(rpcResponse.isCompletedExceptionally());
        Exception exception = assertThrows(java.util.concurrent.ExecutionException.class, rpcResponse::get);
        assertEquals(exception.getMessage(),
                "java.lang.RuntimeException: Unknown payload type [type.googleapis.com/google.protobuf.Int32Value]. Expected [io.cloudevents.v1.proto.CloudEvent]");
    }

    // ---

    @Test
    void test_success_invoke_method_happy_flow_using_mapResponse() {
        CloudEvent request = buildCloudEventForTest().build();
        final CompletableFuture<io.cloudevents.v1.proto.CloudEvent> rpcResponse =
                mapResponse(uLinkHappyPath.invokeMethod(request), io.cloudevents.v1.proto.CloudEvent.class);

        assertFalse(rpcResponse.isCompletedExceptionally());
        final CompletableFuture<Void> test = rpcResponse.thenAccept(cloudEvent -> assertEquals(buildProtoPayloadForTest(), cloudEvent));
        assertFalse(test.isCompletedExceptionally());
    }

    @Test
    void test_fail_invoke_method_when_invoke_method_returns_a_status_using_mapResponse() {
        CloudEvent request = buildCloudEventForTest().build();
        final CompletableFuture<io.cloudevents.v1.proto.CloudEvent> rpcResponse =
                mapResponse(uLinkWithStatusCodeInsteadOfHappyPath.invokeMethod(request), io.cloudevents.v1.proto.CloudEvent.class);

        assertTrue(rpcResponse.isCompletedExceptionally());

        Exception exception = assertThrows(java.util.concurrent.ExecutionException.class, rpcResponse::get);
        assertEquals(exception.getMessage(), "java.lang.RuntimeException: Error returned, status code: [INVALID_ARGUMENT], message: [boom]");
    }

    @Test
    void test_fail_invoke_method_when_invoke_method_threw_an_exception_using_mapResponse() {
        CloudEvent request = buildCloudEventForTest().build();
        final CompletableFuture<io.cloudevents.v1.proto.CloudEvent> rpcResponse =
                mapResponse(uLinkThatCompletesWithAnException.invokeMethod(request), io.cloudevents.v1.proto.CloudEvent.class);

        assertTrue(rpcResponse.isCompletedExceptionally());
        Exception exception = assertThrows(java.util.concurrent.ExecutionException.class, rpcResponse::get);
        assertEquals(exception.getMessage(), "java.lang.RuntimeException: Boom");
    }

    @Test
    void test_fail_invoke_method_when_invoke_method_returns_a_bad_proto_using_mapResponse() {
        CloudEvent request = buildCloudEventForTest().build();
        final CompletableFuture<io.cloudevents.v1.proto.CloudEvent> rpcResponse =
                mapResponse(uLinkThatReturnsTheWrongProto.invokeMethod(request), io.cloudevents.v1.proto.CloudEvent.class);

        assertTrue(rpcResponse.isCompletedExceptionally());
        Exception exception = assertThrows(java.util.concurrent.ExecutionException.class, rpcResponse::get);
        assertEquals(exception.getMessage(),
                "java.lang.RuntimeException: Unknown payload type [type.googleapis.com/google.protobuf.Int32Value]. Expected [io.cloudevents.v1.proto.CloudEvent]");
    }

    // ---

    @Test
    void test_success_invoke_method_happy_flow() {
        //Stub code
        CloudEvent request = buildCloudEventForTest().build();
        final CompletableFuture<Any> rpcResponse = uLinkHappyPath.invokeMethod(request);

        final CompletableFuture<io.cloudevents.v1.proto.CloudEvent> stubReturnValue = rpcResponse.handle((payload, exception) -> {
            // happy flow, no exception
            assertNull(exception);

            // check the payload is not google.rpc.Status
            assertFalse(payload.is(Status.class));

            // check the payload is the cloud event we build
            assertTrue(payload.is(io.cloudevents.v1.proto.CloudEvent.class));

            try {
                return payload.unpack(io.cloudevents.v1.proto.CloudEvent.class);
            } catch (InvalidProtocolBufferException e) {
                throw new RuntimeException(e);
            }
        });

        stubReturnValue.thenAccept(cloudEvent -> assertEquals(buildProtoPayloadForTest(), cloudEvent));

    }

    @Test
    void test_fail_invoke_method_when_invoke_method_returns_a_status() {
        //Stub code
        CloudEvent request = buildCloudEventForTest().build();
        final CompletableFuture<Any> rpcResponse = uLinkWithStatusCodeInsteadOfHappyPath.invokeMethod(request);

        final CompletableFuture<io.cloudevents.v1.proto.CloudEvent> stubReturnValue = rpcResponse.handle((payload, exception) -> {
            // happy flow, no exception
            assertNull(exception);

            // check the payload not google.rpc.Status
            assertTrue(payload.is(Status.class));

            // check the payload is not the type we expected
            assertFalse(payload.is(io.cloudevents.v1.proto.CloudEvent.class));

            // we know it is a Status - so let's unpack it
            try {
                Status status = payload.unpack(Status.class);
                throw new RuntimeException(String.format("Error returned, status code: [%s], message: [%s]",
                        Code.forNumber(status.getCode()), status.getMessage()));
            } catch (InvalidProtocolBufferException e) {
                throw new RuntimeException(e);
            }

        });

        assertTrue(stubReturnValue.isCompletedExceptionally());

        Exception exception = assertThrows(java.util.concurrent.ExecutionException.class, stubReturnValue::get);
        assertEquals(exception.getMessage(), "java.lang.RuntimeException: Error returned, status code: [INVALID_ARGUMENT], message: [boom]");

    }

    @Test
    void test_fail_invoke_method_when_invoke_method_threw_an_exception() {
        //Stub code
        CloudEvent request = buildCloudEventForTest().build();
        final CompletableFuture<Any> rpcResponse = uLinkThatCompletesWithAnException.invokeMethod(request);

        final CompletableFuture<io.cloudevents.v1.proto.CloudEvent> stubReturnValue = rpcResponse.handle((payload, exception) -> {
            // exception was thrown
            assertNotNull(exception);

            assertNull(payload);

            throw new RuntimeException(exception.getMessage(), exception);

        });

        assertTrue(stubReturnValue.isCompletedExceptionally());

        Exception exception = assertThrows(java.util.concurrent.ExecutionException.class, stubReturnValue::get);
        assertEquals(exception.getMessage(), "java.lang.RuntimeException: Boom");

    }

    @Test
    void test_fail_invoke_method_when_invoke_method_returns_a_bad_proto() {
        //Stub code
        CloudEvent request = buildCloudEventForTest().build();
        final CompletableFuture<Any> rpcResponse = uLinkThatReturnsTheWrongProto.invokeMethod(request);

        final CompletableFuture<io.cloudevents.v1.proto.CloudEvent> stubReturnValue = rpcResponse.handle((payload, exception) -> {
            // happy flow, no exception
            assertNull(exception);

            // check the payload is not google.rpc.Status
            assertFalse(payload.is(Status.class));

            // check the payload is the cloud event we build
            assertFalse(payload.is(io.cloudevents.v1.proto.CloudEvent.class));

            try {
                return payload.unpack(io.cloudevents.v1.proto.CloudEvent.class);
            } catch (InvalidProtocolBufferException e) {
                throw new RuntimeException(String.format("%s [%s]", e.getMessage(), "io.cloudevents.v1.proto.CloudEvent.class"), e);
            }
        });

        assertTrue(stubReturnValue.isCompletedExceptionally());

        Exception exception = assertThrows(java.util.concurrent.ExecutionException.class, stubReturnValue::get);
        assertEquals(exception.getMessage(), "java.lang.RuntimeException: Type of the Any message does not match the given class. [io.cloudevents.v1.proto.CloudEvent.class]");

    }

    @Test
    void what_the_stub_looks_like() throws InterruptedException {

        Rpc uLink = new Rpc() {
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

        //Stub code

        CloudEvent request = buildCloudEventForTest().build();
        final CompletableFuture<Any> invokeMethodResponse = uLink.invokeMethod(request);

        CompletableFuture<io.cloudevents.v1.proto.CloudEvent> stubReturnValue = rpcResponse(invokeMethodResponse);
        assertFalse(stubReturnValue.isCancelled());

    }

    private static CompletableFuture<io.cloudevents.v1.proto.CloudEvent> rpcResponse(CompletableFuture<Any> invokeMethodResponse) {

        final CompletableFuture<io.cloudevents.v1.proto.CloudEvent> stubReturnValue = invokeMethodResponse.handle((payload, exception) -> {
            // invoke method had some unexpected problem.
            if (exception != null) {
                throw new RuntimeException(exception.getMessage(), exception);
            }
            if (payload == null) {
                throw new RuntimeException("Server returned a null payload. Expected a io.cloudevents.v1.proto.CloudEvent");
            }

            // test to see if we have expected type
            if (payload.is(io.cloudevents.v1.proto.CloudEvent.class)) {
                try {
                    return payload.unpack(io.cloudevents.v1.proto.CloudEvent.class);
                } catch (InvalidProtocolBufferException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }

            // this will be called only if expected return type is not status, but status was returned to indicate a problem.
            if (payload.is(Status.class)) {
                try {
                    Status status = payload.unpack(Status.class);
                    throw new RuntimeException(String.format("Error returned, status code: [%s], message: [%s]",
                            Code.forNumber(status.getCode()), status.getMessage()));
                } catch (InvalidProtocolBufferException e) {
                    throw new RuntimeException(String.format("%s [%s]", e.getMessage(), "com.google.grpc.Status.class"), e);
                }
            }

            throw new RuntimeException(String.format("Unknown payload type [%s]", payload.getTypeUrl()));

        });

        return stubReturnValue;
    }



    private io.cloudevents.v1.proto.CloudEvent buildProtoPayloadForTest() {
        io.cloudevents.v1.proto.CloudEvent cloudEventProto = io.cloudevents.v1.proto.CloudEvent.newBuilder()
                .setSpecVersion("1.0")
                .setId("hello")
                .setSource("http://example.com")
                .setType("example.demo")
                .setProtoData(Any.newBuilder().build())
                .putAttributes("ttl", io.cloudevents.v1.proto.CloudEvent.CloudEventAttributeValue.newBuilder()
                        .setCeString("3").build())
                .build();
        return cloudEventProto;
    }

    private CloudEventBuilder buildCloudEventForTest() {
        return CloudEventBuilder.v1()
                .withId("hello")
                .withType("req.v1")
                .withSource(URI.create("//VCU.VIN/body.access"));
    }

}