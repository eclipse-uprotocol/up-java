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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import java.util.concurrent.CompletionStage;
import org.eclipse.uprotocol.transport.UTransport;
import org.eclipse.uprotocol.transport.builder.UMessageBuilder;
import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UMessage;
import org.eclipse.uprotocol.v1.UStatus;
import org.eclipse.uprotocol.v1.UUri;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class InMemoryRpcServerTest {
    @Test
    @DisplayName("Test registering and unregister a request listener")
    public void testRegisteringRequestListener() {
        RequestHandler handler = new RequestHandler() {
            @Override
            public UPayload handleRequest(UMessage request) {
                return UPayload.EMPTY;
            }
        };
        UUri method = createMethodUri();
        RpcServer server = new InMemoryRpcServer(new TestUTransport());
        final CompletionStage<UStatus> result = server.registerRequestHandler(method, handler);
        assertFalse(result.toCompletableFuture().isCompletedExceptionally());
        assertDoesNotThrow(() -> assertEquals(result.toCompletableFuture().get().getCode(), UCode.OK));
        
        // second time should return an error
        final CompletionStage<UStatus> result2 = server.unregisterRequestHandler(method, handler);
        assertFalse(result2.toCompletableFuture().isCompletedExceptionally());
        assertDoesNotThrow(() -> assertEquals(result2.toCompletableFuture().get().getCode(), UCode.OK));
    }

    @Test
    @DisplayName("Test registering twice the same request handler for the same method")
    public void testRegisteringTwiceTheSameRequestHandler() {
        RequestHandler handler = new RequestHandler() {
            @Override
            public UPayload handleRequest(UMessage request) {
                return UPayload.EMPTY;
            }
        };
        RpcServer server = new InMemoryRpcServer(new TestUTransport());
        assertFalse(server.registerRequestHandler(createMethodUri(), handler)
            .toCompletableFuture().isCompletedExceptionally());

        CompletionStage<UStatus> result = server.registerRequestHandler(createMethodUri(), handler);
        assertFalse(result.toCompletableFuture().isCompletedExceptionally());
        assertDoesNotThrow(() -> {
            assertEquals(result.toCompletableFuture().get().getCode(), UCode.ALREADY_EXISTS);
        });
    }

    @Test
    @DisplayName("Test unregistering a request handler that wasn't registered already")
    public void testUnregisteringNonRegisteredRequestHandler() {
        RequestHandler handler = new RequestHandler() {
            @Override
            public UPayload handleRequest(UMessage request) {
                throw new UnsupportedOperationException("Unimplemented method 'handleRequest'");
            }
        };
        RpcServer server = new InMemoryRpcServer(new TestUTransport());
        CompletionStage<UStatus> result = server.unregisterRequestHandler(createMethodUri(), handler);
        assertFalse(result.toCompletableFuture().isCompletedExceptionally());
        assertDoesNotThrow(() -> {
            assertEquals(result.toCompletableFuture().get().getCode(), UCode.NOT_FOUND);
        });
    }

    @Test
    @DisplayName("Test register a request handler where authority does not match the transport source authority")
    public void testRegisteringRequestListenerWithWrongAuthority() {
        RequestHandler handler = new RequestHandler() {
            @Override
            public UPayload handleRequest(UMessage request) {
                return UPayload.EMPTY;
            }
        };
        RpcServer server = new InMemoryRpcServer(new TestUTransport());
        UUri method = UUri.newBuilder()
            .setAuthorityName("Steven")
            .setUeId(4)
            .setUeVersionMajor(1)
            .setResourceId(3).build();
        CompletionStage<UStatus> status = server.registerRequestHandler(method, handler);
        assertFalse(status.toCompletableFuture().isCompletedExceptionally());
        assertDoesNotThrow(() -> {
            UStatus result = status.toCompletableFuture().get();
            assertEquals(result.getCode(), UCode.INVALID_ARGUMENT);
            assertEquals(result.getMessage(), "Method URI does not match the transport source URI");
        });
    }

    @Test
    @DisplayName("Test register a request handler where ue_id does not match the transport source ue)_id")
    public void testRegisteringRequestListenerWithWrongUeId() {
        RequestHandler handler = new RequestHandler() {
            @Override
            public UPayload handleRequest(UMessage request) {
                return UPayload.EMPTY;
            }
        };
        RpcServer server = new InMemoryRpcServer(new TestUTransport());
        UUri method = UUri.newBuilder()
            .setAuthorityName("Hartley")
            .setUeId(5)
            .setUeVersionMajor(1)
            .setResourceId(3).build();
        
        CompletionStage<UStatus> status = server.registerRequestHandler(method, handler);
        assertFalse(status.toCompletableFuture().isCompletedExceptionally());
        assertDoesNotThrow(() -> {
            UStatus result = status.toCompletableFuture().get();
            assertEquals(result.getCode(), UCode.INVALID_ARGUMENT);
            assertEquals(result.getMessage(), "Method URI does not match the transport source URI");
        });
    }

    @Test
    @DisplayName("Test register request handler where ue_version_major does not " + 
        "match the transport source ue_version_major")
    public void testRegisteringRequestListenerWithWrongUeVersionMajor() {
        RequestHandler handler = new RequestHandler() {
            @Override
            public UPayload handleRequest(UMessage request) {
                return UPayload.EMPTY;
            }
        };
        RpcServer server = new InMemoryRpcServer(new TestUTransport());
        UUri method = UUri.newBuilder()
            .setAuthorityName("Hartley")
            .setUeId(4)
            .setUeVersionMajor(2)
            .setResourceId(3).build();
        
        CompletionStage<UStatus> status = server.registerRequestHandler(method, handler);
        assertFalse(status.toCompletableFuture().isCompletedExceptionally());
        assertDoesNotThrow(() -> {
            UStatus result = status.toCompletableFuture().get();
            assertEquals(result.getCode(), UCode.INVALID_ARGUMENT);
            assertEquals(result.getMessage(), "Method URI does not match the transport source URI");
        });

    }

    @Test
    @DisplayName("Test unregister requesthandler where authority not match the transport source URI")
    public void testUnregisteringRequestHandlerWithWrongAuthority() {
        RequestHandler handler = new RequestHandler() {
            @Override
            public UPayload handleRequest(UMessage request) {
                return UPayload.EMPTY;
            }
        };
        RpcServer server = new InMemoryRpcServer(new TestUTransport());
        UUri method = UUri.newBuilder()
            .setAuthorityName("Steven")
            .setUeId(4)
            .setUeVersionMajor(1)
            .setResourceId(3).build();
        
        CompletionStage<UStatus> status = server.unregisterRequestHandler(method, handler);

        assertFalse(status.toCompletableFuture().isCompletedExceptionally());
        assertDoesNotThrow(() -> {
            UStatus result = status.toCompletableFuture().get();
            assertEquals(result.getCode(), UCode.INVALID_ARGUMENT);
            assertEquals(result.getMessage(), "Method URI does not match the transport source URI");
        });
    }

    @Test
    @DisplayName("Test unregister request handler where ue_id does not match the transport source URI")
    public void testUnregisteringRequestHandlerWithWrongUeId() {
        RequestHandler handler = new RequestHandler() {
            @Override
            public UPayload handleRequest(UMessage request) {
                return UPayload.EMPTY;
            }
        };
        RpcServer server = new InMemoryRpcServer(new TestUTransport());
        UUri method = UUri.newBuilder()
            .setAuthorityName("Hartley")
            .setUeId(5)
            .setUeVersionMajor(1)
            .setResourceId(3).build();
        
        CompletionStage<UStatus> result = server.unregisterRequestHandler(method, handler);
        assertFalse(result.toCompletableFuture().isCompletedExceptionally());
        assertDoesNotThrow(() -> {
            UStatus status = result.toCompletableFuture().get();
            assertEquals(status.getCode(), UCode.INVALID_ARGUMENT);
            assertEquals(status.getMessage(), "Method URI does not match the transport source URI");
        });
    }

    @Test
    @DisplayName("Test unregister request handler where ue_version_major does not match the transport source URI")
    public void testUnregisteringRequestHandlerWithWrongUeVersionMajor() {
        RequestHandler handler = new RequestHandler() {
            @Override
            public UPayload handleRequest(UMessage request) {
                return UPayload.EMPTY;
            }
        };
        RpcServer server = new InMemoryRpcServer(new TestUTransport());
        UUri method = UUri.newBuilder()
            .setAuthorityName("Hartley")
            .setUeId(4)
            .setUeVersionMajor(2)
            .setResourceId(3).build();
        
        CompletionStage<UStatus> result = server.unregisterRequestHandler(method, handler);
        assertFalse(result.toCompletableFuture().isCompletedExceptionally());
        assertDoesNotThrow(() -> {
            UStatus status = result.toCompletableFuture().get();
            assertEquals(status.getCode(), UCode.INVALID_ARGUMENT);
            assertEquals(status.getMessage(), "Method URI does not match the transport source URI");
        });
    }


    @Test
    @DisplayName("Test register a request handler when we use the ErrorUTransport that returns an error")
    public void testRegisteringRequestListenerWithErrorTransport() {
        RequestHandler handler = new RequestHandler() {
            @Override
            public UPayload handleRequest(UMessage request) {
                return UPayload.EMPTY;
            }
        };
        RpcServer server = new InMemoryRpcServer(new ErrorUTransport());
        CompletionStage<UStatus> result = server.registerRequestHandler(createMethodUri(), handler);
        assertFalse(result.toCompletableFuture().isCompletedExceptionally());
        assertDoesNotThrow(() -> {
            UStatus status = result.toCompletableFuture().get();
            assertEquals(status.getCode(), UCode.FAILED_PRECONDITION);
        });
    }

    @Test
    @DisplayName("Test handleRequests when we have 2 RpcServers and the request is not for the second instance" +
        "this is to test that we pull from mRequestHandlers and remove returns nothing")
    public void testHandlerequests() {
        // test transport that will trigger the handleRequest()
        UTransport transport = new EchoUTransport();
        RequestHandler handler = new RequestHandler() {
            @Override
            public UPayload handleRequest(UMessage request) {
                throw new UnsupportedOperationException("this should not be called");
            }
        };

        RpcServer server = new InMemoryRpcServer(transport);

        UUri method = createMethodUri();
        UUri method2 = UUri.newBuilder(method).setResourceId(69).build();

        server.registerRequestHandler(method, handler)
            .thenApplyAsync(status -> {
                UMessage request = UMessageBuilder.request(transport.getSource(), method, 1000).build();
                assertDoesNotThrow(() -> {
                    return transport.send(request).toCompletableFuture().get();
                });
                return status;
            })
            .thenApplyAsync(status -> {
                assertDoesNotThrow(() -> {
                    return server.registerRequestHandler(method2, handler).toCompletableFuture().get();
                });
                return status;
            });
    }

    @Test
    @DisplayName("Test handleRequests the handler triggered an exception")
    public void testHandlerequestsException() {
        // test transport that will trigger the handleRequest()
        UTransport transport = new EchoUTransport();
        RequestHandler handler = new RequestHandler() {
            @Override
            public UPayload handleRequest(UMessage request) {
                throw new UStatusException(UStatus.newBuilder().setCode(UCode.FAILED_PRECONDITION)
                    .setMessage("Steven it failed!").build());
            }
        };

        RpcServer server = new InMemoryRpcServer(transport);

        UUri method = createMethodUri();

        server.registerRequestHandler(method, handler).thenApply(status -> {
            UMessage request = UMessageBuilder.request(transport.getSource(), method, 1000).build();
            CompletionStage<UStatus> result = transport.send(request);
            assertFalse(result.toCompletableFuture().isCompletedExceptionally());
            assertDoesNotThrow(() -> {
                assertEquals(result.toCompletableFuture().get().getCode(), UCode.FAILED_PRECONDITION);
            });
            return status;
        });
    }

    @Test
    @DisplayName("Test handleRequests the handler triggered an unknown exception")
    public void testHandlerequestsUnknownException() {
        // test transport that will trigger the handleRequest()
        UTransport transport = new EchoUTransport();
        RequestHandler handler = new RequestHandler() {
            @Override
            public UPayload handleRequest(UMessage request) {
                throw new UnsupportedOperationException("Steven it failed!");
            }
        };

        RpcServer server = new InMemoryRpcServer(transport);

        UUri method = createMethodUri();

        server.registerRequestHandler(method, handler).thenAcceptAsync(status -> {
            // fake sending a request message that will trigger the handler to be called but since it is 
            // not for the same method as the one registered, it should be ignored and the handler not called
            UMessage request = UMessageBuilder.request(transport.getSource(), method, 1000).build();

            CompletionStage<UStatus> result = transport.send(request);
            assertFalse(result.toCompletableFuture().isCompletedExceptionally());
            assertDoesNotThrow(() -> {
                assertEquals(result.toCompletableFuture().get().getCode(), UCode.INTERNAL);
                assertEquals(result.toCompletableFuture().get().getMessage(), "Steven it failed!");
            });
            
        });
    }

    @Test
    @DisplayName("Test handleRequests when we receive a request for a method that we do not have a registered handler")
    public void testHandlerequestsNoHandler() {
        // test transport that will trigger the handleRequest()
        UTransport transport = new EchoUTransport();
        RequestHandler handler = new RequestHandler() {
            @Override
            public UPayload handleRequest(UMessage request) {
                throw new UnsupportedOperationException("this should not be called");
            }
        };

        RpcServer server = new InMemoryRpcServer(transport);
        UUri method = createMethodUri();
        UUri method2 = UUri.newBuilder(method).setResourceId(69).build();

        assertDoesNotThrow(() -> {
            server.registerRequestHandler(method, handler).thenApplyAsync(result -> {
                assertEquals(result.getCode(), UCode.OK);
                UMessage request = UMessageBuilder.request(transport.getSource(), method2, 1000).build();
                assertDoesNotThrow(() -> {
                    UStatus status = transport.send(request).toCompletableFuture().get();
                    assertEquals(status.getCode(), UCode.NOT_FOUND);
                });
                return result;
            });
        });
    }

    @Test
    @DisplayName("Test handling a request where the handler returns a payload and completes successfully")
    public void testHandlerequestsWithPayload() {
        // test transport that will trigger the handleRequest()
        UTransport transport = new EchoUTransport();

        RequestHandler handler = new RequestHandler() {
            @Override
            public UPayload handleRequest(UMessage request) {
                return UPayload.EMPTY;
            }
        };

        RpcServer server = new InMemoryRpcServer(transport);

        UUri method = createMethodUri();

        server.registerRequestHandler(method, handler).thenAccept(status -> {
            UMessage request = UMessageBuilder.request(transport.getSource(), method, 1000).build();
            CompletionStage<UStatus> result = transport.send(request);
            assertFalse(result.toCompletableFuture().isCompletedExceptionally());
            assertDoesNotThrow(() -> {
                assertEquals(result.toCompletableFuture().get().getCode(), UCode.OK);
            });
        });
    }

    @Test
    @DisplayName("Test registerRequestHandler when passed parameters are null")
    public void testRegisterrequesthandlerNullParameters() {
        RequestHandler handler = new RequestHandler() {
            @Override
            public UPayload handleRequest(UMessage request) {
                return UPayload.EMPTY;
            }
        };

        RpcServer server = new InMemoryRpcServer(new TestUTransport());
        assertDoesNotThrow(() -> {
            UStatus status = server.registerRequestHandler(null, null).toCompletableFuture().get();
            assertEquals(status.getCode(), UCode.INVALID_ARGUMENT);
            assertEquals(status.getMessage(), "Method URI or handler missing");
        });

        assertDoesNotThrow(() -> {
            UStatus status = server.registerRequestHandler(createMethodUri(), null).toCompletableFuture().get();
            assertEquals(status.getCode(), UCode.INVALID_ARGUMENT);
            assertEquals(status.getMessage(), "Method URI or handler missing");
        });
        
        assertDoesNotThrow(() -> {
            UStatus status = server.registerRequestHandler(null, handler).toCompletableFuture().get();
            assertEquals(status.getCode(), UCode.INVALID_ARGUMENT);
            assertEquals(status.getMessage(), "Method URI or handler missing");
        });
    }

    @Test
    @DisplayName("Test unregisterRequestHandler when passed parameters are null")
    public void testUnregisterrequesthandlerNullParameters() {
        RequestHandler handler = new RequestHandler() {
            @Override
            public UPayload handleRequest(UMessage request) {
                return UPayload.EMPTY;
            }
        };

        RpcServer server = new InMemoryRpcServer(new TestUTransport());
        assertDoesNotThrow(() -> {
            UStatus status = server.unregisterRequestHandler(null, null).toCompletableFuture().get();
            assertEquals(status.getCode(), UCode.INVALID_ARGUMENT);
            assertEquals(status.getMessage(), "Method URI or handler missing");
        });

        assertDoesNotThrow(() -> {
            UStatus status = server.unregisterRequestHandler(createMethodUri(), null).toCompletableFuture().get();
            assertEquals(status.getCode(), UCode.INVALID_ARGUMENT);
            assertEquals(status.getMessage(), "Method URI or handler missing");
        });
        
        assertDoesNotThrow(() -> {
            UStatus status = server.unregisterRequestHandler(null, handler).toCompletableFuture().get();
            assertEquals(status.getCode(), UCode.INVALID_ARGUMENT);
            assertEquals(status.getMessage(), "Method URI or handler missing");
        });
    }

    
    // Helper method to create a UUri that matches that of the default TestUTransport
    private UUri createMethodUri() {
        return UUri.newBuilder()
            .setAuthorityName("Hartley")
            .setUeId(4)
            .setUeVersionMajor(1)
            .setResourceId(3).build();
    }

}
