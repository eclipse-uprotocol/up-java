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

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.eclipse.uprotocol.transport.UListener;
import org.eclipse.uprotocol.transport.builder.UMessageBuilder;
import org.eclipse.uprotocol.uri.factory.UriFactory;
import org.eclipse.uprotocol.v1.UAttributes;
import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UMessage;
import org.eclipse.uprotocol.v1.UMessageType;
import org.eclipse.uprotocol.v1.UPayloadFormat;
import org.eclipse.uprotocol.v1.UStatus;
import org.eclipse.uprotocol.v1.UUri;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.protobuf.ByteString;

@ExtendWith(MockitoExtension.class)
// [utest->dsn~communication-layer-impl-default~1]
class InMemoryRpcServerTest extends CommunicationLayerClientTestBase {

    @Mock
    private RequestHandler handler;

    private static Stream<Arguments> invalidArgsForRegisterRequestHandler() {
        return Stream.of(
            Arguments.of(UriFactory.ANY, 0, null, NullPointerException.class),
            Arguments.of(null, 0, mock(RequestHandler.class), NullPointerException.class),
            Arguments.of(UriFactory.ANY, 0x0000, mock(RequestHandler.class), CompletionException.class),
            Arguments.of(UriFactory.ANY, 0x8000, mock(RequestHandler.class), CompletionException.class)
        );
    }

    @ParameterizedTest(name = "Test registerRequestHandler fails for invalid arguments: {index} => {arguments}")
    @MethodSource("invalidArgsForRegisterRequestHandler")
    void testRegisterRequestHandlerFailsForNullParameters(
        UUri uri,
        int methodId,
        RequestHandler handler,
        Class<? extends Throwable> expectedException) {
        RpcServer server = new InMemoryRpcServer(transport, uriProvider);
        assertThrows(expectedException, () -> server.registerRequestHandler(uri, methodId, handler)
            .toCompletableFuture().join());
    }

    @ParameterizedTest(name = "Test unregisterRequestHandler fails for invalid arguments: {index} => {arguments}")
    @MethodSource("invalidArgsForRegisterRequestHandler")
    void testUnregisterRequestHandlerFailsForNullParameters(
        UUri uri,
        int methodId,
        RequestHandler handler,
        Class<? extends Throwable> expectedException) {
        RpcServer server = new InMemoryRpcServer(transport, uriProvider);
        assertThrows(expectedException, () -> server.unregisterRequestHandler(uri, methodId, handler)
            .toCompletableFuture().join());
    }

    @Test
    @DisplayName("Test registering and unregistering a request listener")
    void testRegisterRequestListenerSucceeds() {
        final RpcServer server = new InMemoryRpcServer(transport, uriProvider);
        final var originFilter = UriFactory.ANY;
        server.registerRequestHandler(
                originFilter,
                METHOD_URI.getResourceId(),
                handler)
            .toCompletableFuture()
            .join();
        verify(transport).registerListener(
            eq(originFilter),
            eq(METHOD_URI),
            any(UListener.class));

        server.unregisterRequestHandler(
                originFilter,
                METHOD_URI.getResourceId(),
                handler)
            .toCompletableFuture()
            .join();
        verify(transport).unregisterListener(
            eq(originFilter),
            eq(METHOD_URI),
            any(UListener.class));
    }

    @Test
    @DisplayName("Test registering the same request handler twice for the same endpoint")
    void testRegisteringTwiceTheSameRequestHandler() {
        final RpcServer server = new InMemoryRpcServer(transport, uriProvider);

        final var originFilter = UriFactory.ANY;
        server.registerRequestHandler(
                originFilter,
                METHOD_URI.getResourceId(),
                handler)
            .toCompletableFuture()
            .join();
        verify(transport, times(1)).registerListener(
            eq(originFilter),
            eq(METHOD_URI),
            any(UListener.class));

        var exception = assertThrows(CompletionException.class, () -> server.registerRequestHandler(
                originFilter,
                METHOD_URI.getResourceId(),
                handler)
            .toCompletableFuture()
            .join());
        assertEquals(UCode.ALREADY_EXISTS, ((UStatusException) exception.getCause()).getCode());
    }

    @Test
    @DisplayName("Test unregistering a request handler that wasn't registered already")
    void testUnregisterRequestHandlerFailsForUnknownHandler() {
        final RpcServer server = new InMemoryRpcServer(transport, uriProvider);

        var exception = assertThrows(
            CompletionException.class,
            () -> server.unregisterRequestHandler(UriFactory.ANY, 1, handler)
                .toCompletableFuture().join());
        assertEquals(UCode.NOT_FOUND, ((UStatusException) exception.getCause()).getCode());
        verify(transport, never()).unregisterListener(
            any(UUri.class),
            any(UUri.class),
            any(UListener.class));
    }

    @Test
    @DisplayName("Test registering a request handler with unavailable transport fails")
    void testRegisteringRequestListenerFailsIfTransportIsUnavailable() {
        when(transport.registerListener(any(UUri.class), any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.failedFuture(new UStatusException(UCode.UNAVAILABLE, "unavailable")));
        RpcServer server = new InMemoryRpcServer(transport, uriProvider);

        var exception = assertThrows(
            CompletionException.class,
            () -> server.registerRequestHandler(UriFactory.ANY, METHOD_URI.getResourceId(), handler)
                .toCompletableFuture().join());
        assertEquals(UCode.UNAVAILABLE, ((UStatusException) exception.getCause()).getCode());
        verify(transport, times(1)).registerListener(
            eq(UriFactory.ANY),
            eq(METHOD_URI),
            any(UListener.class));
    }

    private static Stream<Arguments> requestHandlerExceptionProvider() {
        return Stream.of(
            Arguments.of(
                new UStatusException(UStatus.newBuilder()
                    .setCode(UCode.PERMISSION_DENIED)
                    .setMessage("Client is not authorized to invoke this operation")
                    .build()),
                UCode.PERMISSION_DENIED,
                "Client is not authorized to invoke this operation"),
            Arguments.of(
                new IllegalStateException("service not ready (yet)"),
                UCode.INTERNAL,
                InMemoryRpcServer.REQUEST_HANDLER_ERROR_MESSAGE)
        );
    }

    @ParameterizedTest(name = "Test request handler throws exception: {index} => {arguments}")
    @MethodSource("requestHandlerExceptionProvider")
    void testHandleRequestHandlesException(Exception thrownException, UCode expectedCode, String expectedMessage) {
        when(handler.handleRequest(any(UMessage.class))).thenThrow(thrownException);

        RpcServer server = new InMemoryRpcServer(transport, uriProvider);
        server.registerRequestHandler(UriFactory.ANY, METHOD_URI.getResourceId(), handler)
            .toCompletableFuture().join();
        final ArgumentCaptor<UListener> requestListener = ArgumentCaptor.forClass(UListener.class);
        verify(transport).registerListener(eq(UriFactory.ANY), eq(METHOD_URI), requestListener.capture());

        final var request = UMessageBuilder.request(uriProvider.getSource(), METHOD_URI, 5000).build();
        requestListener.getValue().onReceive(request);
        verify(handler).handleRequest(request);

        final ArgumentCaptor<UMessage> responseMessage = ArgumentCaptor.forClass(UMessage.class);
        verify(transport).send(responseMessage.capture());

        assertEquals(expectedCode, responseMessage.getValue().getAttributes().getCommstatus());
        final var status = UPayload.unpack(responseMessage.getValue(), UStatus.class);
        assertEquals(expectedCode, status.get().getCode());
        assertEquals(expectedMessage, status.get().getMessage());
    }

    private static Stream<Arguments> errorHandlersProvider() {
        return Stream.of(
            Arguments.of(
                mock(Consumer.class),
                mock(Consumer.class),
                new UStatusException(UCode.UNAVAILABLE, "unavailable")
            ),
            Arguments.of(
                null,
                null,
                new UStatusException(UCode.DEADLINE_EXCEEDED, "message expired")
            ),
            Arguments.of(
                null,
                null,
                new IllegalStateException("not ready")
            )
        );
    }

    @ParameterizedTest(name = "Test request handler reports send error: {index} => {arguments}")
    @MethodSource("errorHandlersProvider")
    void testRequestHandlerReportsSendError(
            Consumer<Throwable> sendResponseErrorHandler,
            Consumer<UMessage> unexpectedMessageHandler,
            Exception sendError) {

        final var request = UMessageBuilder.request(uriProvider.getSource(), METHOD_URI, 5000)
            .build(UPayload.pack(ByteString.copyFromUtf8("Hello"), UPayloadFormat.UPAYLOAD_FORMAT_TEXT));
        final var responsePayload = UPayload.pack(
            ByteString.copyFromUtf8("Hello again"),
            UPayloadFormat.UPAYLOAD_FORMAT_TEXT);
        when(handler.handleRequest(any(UMessage.class))).thenReturn(responsePayload);
        when(transport.send(any(UMessage.class)))
            .thenReturn(CompletableFuture.failedFuture(sendError));

        var server = new InMemoryRpcServer(transport, uriProvider);
        Optional.ofNullable(sendResponseErrorHandler).ifPresent(server::setSendErrorHandler);
        Optional.ofNullable(unexpectedMessageHandler).ifPresent(server::setUnexpectedMessageHandler);
        server.registerRequestHandler(UriFactory.ANY, METHOD_URI.getResourceId(), handler)
            .toCompletableFuture().join();
        final ArgumentCaptor<UListener> requestListener = ArgumentCaptor.forClass(UListener.class);
        verify(transport).registerListener(eq(UriFactory.ANY), eq(METHOD_URI), requestListener.capture());

        requestListener.getValue().onReceive(request);
        verify(handler).handleRequest(request);

        final ArgumentCaptor<UMessage> responseMessage = ArgumentCaptor.forClass(UMessage.class);
        verify(transport).send(responseMessage.capture());
        if (sendResponseErrorHandler != null) {
            verify(sendResponseErrorHandler).accept(sendError);
        }
        // Default handler just logs the error, so nothing to verify
    }

    static Stream<Arguments> unexpectedMessageHandlerProvider() {
        return Stream.of(
            Arguments.of((Consumer<UMessage>) null),
            Arguments.of(mock(Consumer.class))
        );
    }

    @ParameterizedTest
    @MethodSource("unexpectedMessageHandlerProvider")
    void testRequestHandlerIgnoresRequestsWithoutHandler(Consumer<UMessage> unexpectedMessageHandler) {
        final var unsolicitedRequest = UMessageBuilder.request(uriProvider.getSource(), METHOD_URI, 5000).build();

        var server = new InMemoryRpcServer(transport, uriProvider);
        Optional.ofNullable(unexpectedMessageHandler).ifPresent(server::setUnexpectedMessageHandler);
        server.registerRequestHandler(UriFactory.ANY, METHOD_URI.getResourceId(), handler)
            .toCompletableFuture().join();
        final ArgumentCaptor<UListener> requestListener = ArgumentCaptor.forClass(UListener.class);
        verify(transport).registerListener(eq(UriFactory.ANY), eq(METHOD_URI), requestListener.capture());

        server.unregisterRequestHandler(UriFactory.ANY, METHOD_URI.getResourceId(), handler)
            .toCompletableFuture().join();

        requestListener.getValue().onReceive(unsolicitedRequest);
        verify(handler, never()).handleRequest(any(UMessage.class));
        verify(transport, never()).send(any(UMessage.class));
        if (unexpectedMessageHandler != null) {
            verify(unexpectedMessageHandler).accept(unsolicitedRequest);
        }
    }

    @Test
    void testHandleRequestIgnoresNonRequestMessages() {
        final var invalidNotification = UMessage.newBuilder()
            .setAttributes(UAttributes.newBuilder()
                .setType(UMessageType.UMESSAGE_TYPE_NOTIFICATION)
                .setSource(TRANSPORT_SOURCE)
                .setSink(METHOD_URI)
                .build())
            .build();

        @SuppressWarnings("unchecked")
        final Consumer<Throwable> sendResponseErrorHandler = mock(Consumer.class);
        @SuppressWarnings("unchecked")
        final Consumer<UMessage> unexpectedMessageHandler = mock(Consumer.class);

        var server = new InMemoryRpcServer(
            transport,
            uriProvider);
        server.setSendErrorHandler(sendResponseErrorHandler);
        server.setUnexpectedMessageHandler(unexpectedMessageHandler);
        server.registerRequestHandler(UriFactory.ANY, METHOD_URI.getResourceId(), handler)
            .toCompletableFuture().join();
        final ArgumentCaptor<UListener> requestListener = ArgumentCaptor.forClass(UListener.class);
        verify(transport).registerListener(eq(UriFactory.ANY), eq(METHOD_URI), requestListener.capture());

        requestListener.getValue().onReceive(invalidNotification);
        verify(handler, never()).handleRequest(any(UMessage.class));
        verify(sendResponseErrorHandler, never()).accept(any(Throwable.class));
        verify(transport, never()).send(any(UMessage.class));
        verify(unexpectedMessageHandler).accept(invalidNotification);
    }

    @Test
    @DisplayName("Test handling a request where the handler returns a payload and completes successfully")
    void testHandleRequestSucceedsWithPayload() {
        final var request = UMessageBuilder.request(uriProvider.getSource(), METHOD_URI, 5000)
            .build(UPayload.pack(ByteString.copyFromUtf8("Hello"), UPayloadFormat.UPAYLOAD_FORMAT_TEXT));
        final var responsePayload = UPayload.pack(
            ByteString.copyFromUtf8("Hello again"),
            UPayloadFormat.UPAYLOAD_FORMAT_TEXT);
        when(handler.handleRequest(any(UMessage.class))).thenReturn(responsePayload);

        RpcServer server = new InMemoryRpcServer(transport, uriProvider);
        server.registerRequestHandler(UriFactory.ANY, METHOD_URI.getResourceId(), handler)
            .toCompletableFuture().join();
        final ArgumentCaptor<UListener> requestListener = ArgumentCaptor.forClass(UListener.class);
        verify(transport).registerListener(eq(UriFactory.ANY), eq(METHOD_URI), requestListener.capture());

        requestListener.getValue().onReceive(request);
        verify(handler).handleRequest(request);

        final ArgumentCaptor<UMessage> responseMessage = ArgumentCaptor.forClass(UMessage.class);
        verify(transport).send(responseMessage.capture());

        assertEquals(UCode.OK, responseMessage.getValue().getAttributes().getCommstatus());
        assertEquals(responsePayload.data(), responseMessage.getValue().getPayload());
    }
}
