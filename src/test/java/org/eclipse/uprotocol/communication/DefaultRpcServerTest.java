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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Iterator;
import java.util.concurrent.Executors;

import org.eclipse.uprotocol.transport.UListener;
import org.eclipse.uprotocol.transport.UTransport;
import org.eclipse.uprotocol.transport.builder.UMessageBuilder;
import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UMessage;
import org.eclipse.uprotocol.v1.UStatus;
import org.eclipse.uprotocol.v1.UUri;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class DefaultRpcServerTest {
    @Test
    @DisplayName("Test registering and unregister a request listener")
    public void test_registering_request_listener() {
        RequestHandler handler = new RequestHandler() {
            @Override
            public UPayload handleRequest(UMessage request) {
                return UPayload.EMPTY;
            }
        };
        UUri method = createMethodUri();
        RpcServer server = new DefaultRpcServer(new TestUTransport());
        assertEquals(server.registerRequestHandler(method, handler).getCode(), UCode.OK);
        assertEquals(server.unregisterRequestHandler(method, handler).getCode(), UCode.OK);
    }

    @Test
    @DisplayName("Test registering twice the same request handler for the same method")
    public void test_registering_twice_the_same_request_handler() {
        RequestHandler handler = new RequestHandler() {
            @Override
            public UPayload handleRequest(UMessage request) {
                return UPayload.EMPTY;
            }
        };
        RpcServer server = new DefaultRpcServer(new TestUTransport());
        UStatus status = server.registerRequestHandler(createMethodUri(), handler);
        assertEquals(status.getCode(), UCode.OK);
        status = server.registerRequestHandler(createMethodUri(), handler);
        assertEquals(status.getCode(), UCode.ALREADY_EXISTS);
    }

    @Test
    @DisplayName("Test unregistering a request handler that wasn't registered already")
    public void test_unregistering_non_registered_request_handler() {
        RequestHandler handler = new RequestHandler() {
            @Override
            public UPayload handleRequest(UMessage request) {
                throw new UnsupportedOperationException("Unimplemented method 'handleRequest'");
            }
        };
        RpcServer server = new DefaultRpcServer(new TestUTransport());
        UStatus status = server.unregisterRequestHandler(createMethodUri(), handler);
        assertEquals(status.getCode(), UCode.NOT_FOUND);
    }

    @Test
    @DisplayName("Test register a request handler where authority does not match the transport source authority")
    public void test_registering_request_listener_with_wrong_authority() {
        RequestHandler handler = new RequestHandler() {
            @Override
            public UPayload handleRequest(UMessage request) {
                return UPayload.EMPTY;
            }
        };
        RpcServer server = new DefaultRpcServer(new TestUTransport());
        UUri method = UUri.newBuilder()
            .setAuthorityName("Steven")
            .setUeId(4)
            .setUeVersionMajor(1)
            .setResourceId(3).build();
        
        Exception exception = assertThrows(UStatusException.class, 
            () -> server.registerRequestHandler(method, handler));
        assertEquals(exception.getMessage(),
                "Method URI does not match the transport source URI");
    }

    @Test
    @DisplayName("Test register a request handler where ue_id does not match the transport source ue)_id")
    public void test_registering_request_listener_with_wrong_ue_id() {
        RequestHandler handler = new RequestHandler() {
            @Override
            public UPayload handleRequest(UMessage request) {
                return UPayload.EMPTY;
            }
        };
        RpcServer server = new DefaultRpcServer(new TestUTransport());
        UUri method = UUri.newBuilder()
            .setAuthorityName("Hartley")
            .setUeId(5)
            .setUeVersionMajor(1)
            .setResourceId(3).build();
        
        Exception exception = assertThrows(UStatusException.class, 
            () -> server.registerRequestHandler(method, handler));
        assertEquals(exception.getMessage(),
                "Method URI does not match the transport source URI");
    }

    @Test
    @DisplayName("Test register request handler where ue_version_major does not " + 
        "match the transport source ue_version_major")
    public void test_registering_request_listener_with_wrong_ue_version_major() {
        RequestHandler handler = new RequestHandler() {
            @Override
            public UPayload handleRequest(UMessage request) {
                return UPayload.EMPTY;
            }
        };
        RpcServer server = new DefaultRpcServer(new TestUTransport());
        UUri method = UUri.newBuilder()
            .setAuthorityName("Hartley")
            .setUeId(4)
            .setUeVersionMajor(2)
            .setResourceId(3).build();
        
        Exception exception = assertThrows(UStatusException.class, 
            () -> server.registerRequestHandler(method, handler));
        assertEquals(exception.getMessage(),
                "Method URI does not match the transport source URI");
    }

    @Test
    @DisplayName("Test unregister requesthandler where authority not match the transport source URI")
    public void test_unregistering_request_handler_with_wrong_authority() {
        RequestHandler handler = new RequestHandler() {
            @Override
            public UPayload handleRequest(UMessage request) {
                return UPayload.EMPTY;
            }
        };
        RpcServer server = new DefaultRpcServer(new TestUTransport());
        UUri method = UUri.newBuilder()
            .setAuthorityName("Steven")
            .setUeId(4)
            .setUeVersionMajor(1)
            .setResourceId(3).build();
        
        Exception exception = assertThrows(UStatusException.class, 
            () -> server.unregisterRequestHandler(method, handler));
        assertEquals(exception.getMessage(),
                "Method URI does not match the transport source URI");
    }

    @Test
    @DisplayName("Test unregister request handler where ue_id does not match the transport source URI")
    public void test_unregistering_request_handler_with_wrong_ue_id() {
        RequestHandler handler = new RequestHandler() {
            @Override
            public UPayload handleRequest(UMessage request) {
                return UPayload.EMPTY;
            }
        };
        RpcServer server = new DefaultRpcServer(new TestUTransport());
        UUri method = UUri.newBuilder()
            .setAuthorityName("Hartley")
            .setUeId(5)
            .setUeVersionMajor(1)
            .setResourceId(3).build();
        
        Exception exception = assertThrows(UStatusException.class, 
            () -> server.unregisterRequestHandler(method, handler));
        assertEquals(exception.getMessage(),
                "Method URI does not match the transport source URI");
    }

    @Test
    @DisplayName("Test unregister request handler where ue_version_major does not match the transport source URI")
    public void test_unregistering_request_handler_with_wrong_ue_version_major() {
        RequestHandler handler = new RequestHandler() {
            @Override
            public UPayload handleRequest(UMessage request) {
                return UPayload.EMPTY;
            }
        };
        RpcServer server = new DefaultRpcServer(new TestUTransport());
        UUri method = UUri.newBuilder()
            .setAuthorityName("Hartley")
            .setUeId(4)
            .setUeVersionMajor(2)
            .setResourceId(3).build();
        
        Exception exception = assertThrows(UStatusException.class, 
            () -> server.unregisterRequestHandler(method, handler));
        assertEquals(exception.getMessage(),
                "Method URI does not match the transport source URI");
    }


    @Test
    @DisplayName("Test register a request handler when we use the ErrorUTransport that returns an error")
    public void test_registering_request_listener_with_error_transport() {
        RequestHandler handler = new RequestHandler() {
            @Override
            public UPayload handleRequest(UMessage request) {
                return UPayload.EMPTY;
            }
        };
        RpcServer server = new DefaultRpcServer(new ErrorUTransport());
        UStatus status = server.registerRequestHandler(createMethodUri(), handler);
        assertEquals(status.getCode(), UCode.FAILED_PRECONDITION);
    }

    @Test
    @DisplayName("Test handleRequests when we have 2 RpcServers and the request is not for the second instance" +
        "this is to test that we pull from mRequestHandlers and remove returns nothing")
    public void test_handleRequests() {
        // test transport that will trigger the handleRequest()
        UTransport transport = new TestUTransport() {
            @Override
            public UStatus send(UMessage message) {
                for (Iterator<UListener> it = listeners.iterator(); it.hasNext();) {
                    UListener listener = it.next();
                    listener.onReceive(message);
                }
                return UStatus.newBuilder().setCode(UCode.OK).build();
            }
        };

        RequestHandler handler = new RequestHandler() {
            @Override
            public UPayload handleRequest(UMessage request) {
                throw new UnsupportedOperationException("this should not be called");
            }
        };

        RpcServer server = new DefaultRpcServer(transport);

        UUri method = createMethodUri();
        UUri method2 = UUri.newBuilder(method).setResourceId(69).build();

        assertEquals(server.registerRequestHandler(method, handler).getCode(), UCode.OK);

        // fake sending a request message that will trigger the handler to be called but since it is 
        // not for the same method as the one registered, it should be ignored and the handler not called
        UMessage request = UMessageBuilder.request(transport.getSource(), method2, 1000).build();
        assertEquals(transport.send(request).getCode(), UCode.OK);
    }

    @Test
    @DisplayName("Test handleRequests the handler triggered an exception")
    public void test_handleRequests_exception() {
        // test transport that will trigger the handleRequest()
        UTransport transport = new TestUTransport() {
            @Override
            public UStatus send(UMessage message) {
                for (Iterator<UListener> it = listeners.iterator(); it.hasNext();) {
                    UListener listener = it.next();
                    listener.onReceive(message);
                }
                return UStatus.newBuilder().setCode(UCode.OK).build();
            }
        };

        RequestHandler handler = new RequestHandler() {
            @Override
            public UPayload handleRequest(UMessage request) {
                throw new UStatusException(UStatus.newBuilder().setCode(UCode.FAILED_PRECONDITION)
                    .setMessage("Steven it failed!").build());
            }
        };

        RpcServer server = new DefaultRpcServer(transport);

        UUri method = createMethodUri();

        assertEquals(server.registerRequestHandler(method, handler).getCode(), UCode.OK);

        // fake sending a request message that will trigger the handler to be called but since it is 
        // not for the same method as the one registered, it should be ignored and the handler not called
        UMessage request = UMessageBuilder.request(transport.getSource(), method, 1000).build();
        assertEquals(transport.send(request).getCode(), UCode.OK);
    }

    @Test
    @DisplayName("Test handleRequests the handler triggered an unknown exception")
    public void test_handleRequests_unknown_exception() {
        // test transport that will trigger the handleRequest()
        UTransport transport = new TestUTransport() {
            @Override
            public UStatus send(UMessage message) {
                for (Iterator<UListener> it = listeners.iterator(); it.hasNext();) {
                    UListener listener = it.next();
                    listener.onReceive(message);
                }
                return UStatus.newBuilder().setCode(UCode.OK).build();
            }
        };

        RequestHandler handler = new RequestHandler() {
            @Override
            public UPayload handleRequest(UMessage request) {
                throw new UnsupportedOperationException("Steven it failed!");
            }
        };

        RpcServer server = new DefaultRpcServer(transport);

        UUri method = createMethodUri();

        assertEquals(server.registerRequestHandler(method, handler).getCode(), UCode.OK);

        // fake sending a request message that will trigger the handler to be called but since it is 
        // not for the same method as the one registered, it should be ignored and the handler not called
        UMessage request = UMessageBuilder.request(transport.getSource(), method, 1000).build();
        assertEquals(transport.send(request).getCode(), UCode.OK);
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
