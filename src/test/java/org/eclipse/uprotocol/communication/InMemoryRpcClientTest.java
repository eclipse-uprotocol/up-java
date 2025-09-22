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

import static org.junit.Assert.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.concurrent.CompletableFuture;

import org.eclipse.uprotocol.transport.builder.UMessageBuilder;
import org.eclipse.uprotocol.uri.factory.UriFactory;
import org.eclipse.uprotocol.uuid.factory.UuidFactory;
import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UMessage;
import org.eclipse.uprotocol.v1.UPayloadFormat;
import org.eclipse.uprotocol.v1.UPriority;
import org.eclipse.uprotocol.v1.UUri;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.common.truth.Truth;
import com.google.protobuf.ByteString;

// [utest->dsn~communication-layer-impl-default~1]
class InMemoryRpcClientTest extends CommunicationLayerClientTestBase {

    private static void assertMessageHasOptions(CallOptions options, UMessage message) {
        Optional.ofNullable(options.timeout())
            .ifPresent(timeout -> assertEquals(timeout, message.getAttributes().getTtl()));
        Optional.ofNullable(options.priority())
            .ifPresent(priority -> assertEquals(priority, message.getAttributes().getPriority()));
        Optional.ofNullable(options.token())
            .ifPresent(token -> assertEquals(token, message.getAttributes().getToken()));
    }

    private static Stream<Arguments> callOptionsAndPayloadProvider() {
        return Stream.of(
            Arguments.of(CallOptions.DEFAULT, UPayload.EMPTY, UCode.OK),
            Arguments.of(
                new CallOptions(3000, UPriority.UPRIORITY_CS4, null),
                UPayload.packToAny(UUri.newBuilder().build()),
                UCode.OK),
            Arguments.of(
                new CallOptions(4000, UPriority.UPRIORITY_CS5, ""),
                UPayload.pack(UUri.newBuilder().build()),
                UCode.OK),
            Arguments.of(
                new CallOptions(5000, UPriority.UPRIORITY_CS6, "my-token"),
                UPayload.pack(ByteString.copyFromUtf8("hello"), UPayloadFormat.UPAYLOAD_FORMAT_TEXT),
                (UCode) null)
        );
    }

    @ParameterizedTest(name = "Test successful RPC, sending and receiving a payload: {index} - {arguments}")
    @MethodSource("callOptionsAndPayloadProvider")
    @SuppressWarnings("unchecked")
    void testInvokeMethodWithPayloadSucceeds(CallOptions options, UPayload payload, UCode responseStatus) {
        RpcClient rpcClient = new InMemoryRpcClient(transport, uriProvider);

        var response = rpcClient.invokeMethod(METHOD_URI, payload, options);
        verify(transport).registerListener(any(UUri.class), any(Optional.class), responseListener.capture());
        verify(transport).send(requestMessage.capture());
        assertEquals(payload.data(), requestMessage.getValue().getPayload());
        assertMessageHasOptions(options, requestMessage.getValue());

        var requestAttributes = requestMessage.getValue().getAttributes();
        var responseMessageBuilder = UMessageBuilder.response(requestAttributes);
        Optional.ofNullable(responseStatus)
            .ifPresent(status -> responseMessageBuilder.withCommStatus(status));
        var responseMessage = responseMessageBuilder.build(payload);
        responseListener.getValue().onReceive(responseMessage);

        var receivedPayload = assertDoesNotThrow(() -> response.toCompletableFuture().get());
        assertEquals(payload, receivedPayload);
    }
 
    @Test
    @DisplayName("Test running into timeout when invoking method")
    void testInvokeMethodFailsForTimeout() {
        final UPayload payload = UPayload.packToAny(UUri.newBuilder().build());
        final CallOptions options = new CallOptions(100, UPriority.UPRIORITY_CS5, "token");

        RpcClient rpcClient = new InMemoryRpcClient(transport, uriProvider);
        var exception = assertThrows(ExecutionException.class, () -> {
            rpcClient.invokeMethod(METHOD_URI, payload, options).toCompletableFuture().get();
        });
        Truth.assertThat(exception).hasCauseThat().isInstanceOf(UStatusException.class);
        assertEquals(
            UCode.DEADLINE_EXCEEDED,
            ((UStatusException) exception.getCause()).getStatus().getCode()
        );
    }

    @Test
    @DisplayName("Test invoking method fails for transport error")
    void testInvokeMethodFailsForTransportError() {
        when(transport.send(any(UMessage.class)))
            .thenReturn(CompletableFuture.failedFuture(
                new UStatusException(UCode.UNAVAILABLE, "transport not ready")));
        RpcClient rpcClient = new InMemoryRpcClient(transport, uriProvider);
        var exception = assertThrows(ExecutionException.class, () -> {
            rpcClient.invokeMethod(METHOD_URI, UPayload.EMPTY, CallOptions.DEFAULT).toCompletableFuture().get();
        });
        verify(transport).send(requestMessage.capture());
        assertEquals(UPayload.EMPTY.data(), requestMessage.getValue().getPayload());
        assertMessageHasOptions(CallOptions.DEFAULT, requestMessage.getValue());

        Truth.assertThat(exception).hasCauseThat().isInstanceOf(UStatusException.class);
        assertEquals(
            UCode.UNAVAILABLE,
            ((UStatusException) exception.getCause()).getStatus().getCode()
        );
    }

    @Test
    @DisplayName("Test unsuccessful RPC, with service returning error")
    @SuppressWarnings("unchecked")
    void testInvokeMethodFailsForErroneousServiceInvocation() {
        RpcClient rpcClient = new InMemoryRpcClient(transport, uriProvider);
        var response = rpcClient.invokeMethod(METHOD_URI, UPayload.EMPTY, CallOptions.DEFAULT);
        verify(transport).registerListener(any(UUri.class), any(Optional.class), responseListener.capture());
        verify(transport).send(requestMessage.capture());
        assertEquals(UPayload.EMPTY.data(), requestMessage.getValue().getPayload());
        assertMessageHasOptions(CallOptions.DEFAULT, requestMessage.getValue());

        var requestAttributes = requestMessage.getValue().getAttributes();
        var responseMessage = UMessageBuilder.response(requestAttributes)
            .withCommStatus(UCode.RESOURCE_EXHAUSTED)
            .build();
        responseListener.getValue().onReceive(responseMessage);

        var exception = assertThrows(ExecutionException.class, () -> response.toCompletableFuture().get());
        Truth.assertThat(exception).hasCauseThat().isInstanceOf(UStatusException.class);
        assertEquals(
            UCode.RESOURCE_EXHAUSTED,
            ((UStatusException) exception.getCause()).getStatus().getCode()
        );
    }

    static Stream<Arguments> unexpectedMessageHandlerProvider() {
        return Stream.of(
            Arguments.of((Consumer<UMessage>) null),
            Arguments.of(mock(Consumer.class))
        );
    }

    @ParameterizedTest(name = "Test client handles unexpected incoming messages: {index} - {arguments}")
    @MethodSource("unexpectedMessageHandlerProvider")
    @SuppressWarnings("unchecked")
    void testHandleUnexpectedResponse(Consumer<UMessage> unexpectedMessageHandler) {
        var rpcClient = new InMemoryRpcClient(transport, uriProvider);
        Optional.ofNullable(unexpectedMessageHandler).ifPresent(rpcClient::setUnexpectedMessageHandler);
        verify(transport).registerListener(any(UUri.class), any(Optional.class), responseListener.capture());

        // send an arbitrary request
        rpcClient.invokeMethod(METHOD_URI, UPayload.EMPTY, CallOptions.DEFAULT);
        verify(transport).send(requestMessage.capture());

        // create unsolicited response message
        final var reqId = UuidFactory.create();
        assertNotEquals(reqId, requestMessage.getValue().getAttributes().getId());
        var responseMessage = UMessageBuilder.response(
                METHOD_URI,
                TRANSPORT_SOURCE,
                reqId)
            .build();
        responseListener.getValue().onReceive(responseMessage);

        if (unexpectedMessageHandler != null) {
            // assert that the unexpected response is handled correctly
            verify(unexpectedMessageHandler).accept(responseMessage);
        }

        // create unsolicited notification message
        var notificationMessage = UMessageBuilder.notification(
                UUri.newBuilder()
                    .setAuthorityName("hartley")
                    .setUeId(10)
                    .setUeVersionMajor(1)
                    .setResourceId(0x9100)
                    .build(),
                TRANSPORT_SOURCE)
            .build();
        responseListener.getValue().onReceive(notificationMessage);

        if (unexpectedMessageHandler != null) {
            // assert that the unexpected notification is handled correctly
            verify(unexpectedMessageHandler).accept(notificationMessage);
        }
    }

    @Test
    @DisplayName("Test close() unregisters the response listener from the transport")
    void testCloseUnregistersResponseListenerFromTransport() {
        InMemoryRpcClient rpcClient = new InMemoryRpcClient(transport, uriProvider);
        verify(transport).registerListener(
            eq(UriFactory.ANY),
            eq(Optional.of(TRANSPORT_SOURCE)),
            responseListener.capture());
        rpcClient.close();
        verify(transport).unregisterListener(
            eq(UriFactory.ANY),
            eq(Optional.of(TRANSPORT_SOURCE)),
            eq(responseListener.getValue()));
    }
}
