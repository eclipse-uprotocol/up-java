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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.concurrent.CompletionStage;
import org.eclipse.uprotocol.core.usubscription.v3.SubscriptionResponse;
import org.eclipse.uprotocol.core.usubscription.v3.UnsubscribeResponse;
import org.eclipse.uprotocol.transport.UListener;
import org.eclipse.uprotocol.transport.UTransport;
import org.eclipse.uprotocol.transport.builder.UMessageBuilder;
import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UMessage;
import org.eclipse.uprotocol.v1.UPriority;
import org.eclipse.uprotocol.v1.UStatus;
import org.eclipse.uprotocol.v1.UUri;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class UPClientTest {
    
    @Test
    @DisplayName("Test creating UPClient and passing a null UTransport")
    public void testCreateUPClientWithNullTransport() {
        assertThrows(NullPointerException.class, () -> UPClient.create(null));
    }

    @Test
    @DisplayName("Test creating UPClient and passing ErrorUTransport")
    public void testCreateUPClientWithErrorTransport() {
        assertThrows(UStatusException.class, () -> UPClient.create(new ErrorUTransport()));
    }

    @Test
    @DisplayName("Test sending a simple notification")
    public void testSendNotification() {
        
        UStatus status = UPClient.create(new TestUTransport()).notify(createTopic(), createDestinationUri(), null);
        assertEquals(status.getCode(), UCode.OK);
    }


    @Test
    @DisplayName("Test sending a simple notification passing a google.protobuf.Message payload")
    public void testSendNotificationWithPayload() {
        UUri uri = UUri.newBuilder().setAuthorityName("Hartley").build();
        UStatus status = UPClient.create(new TestUTransport()).notify(createTopic(), createDestinationUri(), UPayload.pack(uri));
        assertEquals(status.getCode(), UCode.OK);
    }


    @Test
    @DisplayName("Test registering a listener and comparing it with the saved listener from UTransport")
    public void testRegisterListener() {
        UListener listener = new UListener() {
            @Override
            public void onReceive(UMessage message) {
                assertNotNull(message);
            }
        };

        UStatus status = UPClient.create(new TestUTransport()).registerNotificationListener(createTopic(), listener);
        assertEquals(status.getCode(), UCode.OK);
    }


    @Test
    @DisplayName("Test unregister a saved notification listener")
    public void test_unregister_notification_listener() {
        UListener listener = new UListener() {
            @Override
            public void onReceive(UMessage message) {
                assertNotNull(message);
            }
        };

        Notifier notifier = UPClient.create(new TestUTransport());
        UStatus status = notifier.registerNotificationListener(createTopic(), listener);
        assertEquals(status.getCode(), UCode.OK);

        status = notifier.unregisterNotificationListener(createTopic(), listener);
        assertEquals(status.getCode(), UCode.OK);
    }


    @Test
    @DisplayName("Test unregistering a listener that was not registered")
    public void testUnregisterListenerNotRegistered() {
        UListener listener = new UListener() {
            @Override
            public void onReceive(UMessage message) {
                assertNotNull(message);
            }
        };

        UStatus status = UPClient.create(new TestUTransport()).unregisterNotificationListener(createTopic(), listener);
        assertEquals(status.getCode(), UCode.INVALID_ARGUMENT);
    }


    @Test
    @DisplayName("Test sending a simple publish message without a payload")
    public void testSendPublish() {
        UStatus status = UPClient.create(new TestUTransport()).publish(createTopic(), null);
        assertEquals(status.getCode(), UCode.OK);
    }

    
    @Test
    @DisplayName("Test sending a simple publish message with a stuffed UPayload that was build with packToAny()")
    public void testSendPublishWithStuffedPayload() {
        UUri uri = UUri.newBuilder().setAuthorityName("Hartley").build();
        UStatus status = UPClient.create(new TestUTransport()).publish(createTopic(), UPayload.packToAny(uri));
        assertEquals(status.getCode(), UCode.OK);
    }


    @Test
    @DisplayName("Test calling invokeMethod passing UPayload")
    public void testInvokeMethodWithPayload() {
        UPayload payload = UPayload.packToAny(UUri.newBuilder().build());
        CompletionStage<UPayload> response = UPClient.create(new TestUTransport()).invokeMethod(createMethodUri(), payload, null);
        assertNotNull(response);
        response.toCompletableFuture().join();
        assertFalse(response.toCompletableFuture().isCompletedExceptionally());
    }

    @Test
    @DisplayName("Test calling invokeMethod passing a UPaylod and calloptions")
    public void testInvokeMethodWithPayloadAndCallOptions() {
        UPayload payload = UPayload.packToAny(UUri.newBuilder().build());
        CallOptions options = new CallOptions(1000, UPriority.UPRIORITY_CS5);
        CompletionStage<UPayload> response = UPClient.create(new TestUTransport()).invokeMethod(createMethodUri(), payload, options);
        assertNotNull(response);
        response.toCompletableFuture().join();
        assertFalse(response.toCompletableFuture().isCompletedExceptionally());
    }

    @Test
    @DisplayName("Test calling invokeMethod passing a Null UPayload")
    public void testInvokeMethodWithNullPayload() {
        CompletionStage<UPayload> response = UPClient.create(new TestUTransport()).invokeMethod(createMethodUri(), null, CallOptions.DEFAULT);
        assertNotNull(response);
        response.toCompletableFuture().join();
        assertFalse(response.toCompletableFuture().isCompletedExceptionally());
    }
 
    @Test
    @DisplayName("Test calling invokeMethod with TimeoutUTransport that will timeout the request")
    public void testInvokeMethodWithTimeoutTransport() {
        final UPayload payload = UPayload.packToAny(UUri.newBuilder().build());
        final CallOptions options = new CallOptions(100, UPriority.UPRIORITY_CS5, "token");
        final CompletionStage<UPayload> response = UPClient.create(
            new TimeoutUTransport()).invokeMethod(createMethodUri(), payload, options);
        Exception exception = assertThrows(java.util.concurrent.ExecutionException.class, 
            response.toCompletableFuture()::get);
        assertEquals(exception.getMessage(),
                "java.util.concurrent.TimeoutException");
    }


    @Test
    @DisplayName("Test calling invokeMethod with MultiInvokeUTransport that will invoke multiple listeners")
    public void testInvokeMethodWithMultiInvokeTransport() {
        RpcClient rpcClient = UPClient.create(new TestUTransport());
        UPayload payload = UPayload.packToAny(UUri.newBuilder().build());

        CompletionStage<UPayload> response = rpcClient.invokeMethod(createMethodUri(), payload, null);
        assertNotNull(response);
        CompletionStage<UPayload> response2 = rpcClient.invokeMethod(createMethodUri(), payload, null);
        assertNotNull(response2);
        response.toCompletableFuture().join();
        response2.toCompletableFuture().join();
        assertFalse(response.toCompletableFuture().isCompletedExceptionally());
    }


    @Test
    @DisplayName("Test subscribe happy path")
    public void test_subscribe_happy_path() {
        UUri topic = createTopic();

        CompletionStage<SubscriptionResponse> response = UPClient.create(
                new TestUTransport()).subscribe(topic, new UListener() {
            @Override
            public void onReceive(UMessage message) {
                // Do nothing
            }
        }, null);
        response.toCompletableFuture().join();
        assertFalse(response.toCompletableFuture().isCompletedExceptionally());
    }

    @Test
    @DisplayName("Test unsubscribe happy path")
    public void test_unsubscribe_happy_path() {
        UUri topic = createTopic();
        UStatus response = UPClient.create(new HappyUnSubscribeUTransport()).unsubscribe(topic, new UListener() {
            @Override
            public void onReceive(UMessage message) {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'onReceive'");
            }
        }, null);
        assertEquals(response.getMessage(), "");
        assertEquals(response.getCode(), UCode.OK );
    }

    @Test
    @DisplayName("Test unregisterListener after we successfully subscribed to a topic")
    public void testUnregisterListener() {
        UUri topic = createTopic();
        UListener myListener = new UListener() {
            @Override
            public void onReceive(UMessage message) {
                // Do nothing
            }
        };

        Subscriber subscriber = UPClient.create(new HappySubscribeUTransport());
        CompletionStage<SubscriptionResponse> response = subscriber.subscribe(topic, myListener, new CallOptions(100));
        response.toCompletableFuture().join();
        assertFalse(response.toCompletableFuture().isCompletedExceptionally());
        UStatus status = subscriber.unregisterListener(topic, myListener);
        assertEquals(status.getCode(), UCode.OK);
    }


    @Test
    @DisplayName("Test unsubscribe with commstatus error using the UnHappyUnSubscribeUTransport")
    public void testUnsubscribeWithCommStatusError() {
        UUri topic = createTopic();
        
        UStatus response = UPClient.create(new CommStatusTransport()).unsubscribe(topic, new UListener() {
            @Override
            public void onReceive(UMessage message) {
                return;
            }
        }, null);
        assertEquals(response.getMessage(), "Communication error [FAILED_PRECONDITION]");
        assertEquals(response.getCode(), UCode.FAILED_PRECONDITION);
    
    }

    @Test
    @DisplayName("Test unsubscribe where the invokemethod throws an exception")
    public void testUnsubscribeWithException() {
        UUri topic = createTopic();
        UStatus response = UPClient.create(new TimeoutUTransport()).unsubscribe(topic, new UListener() {
            @Override
            public void onReceive(UMessage message) {
                return;
            }
        }, new CallOptions(1));
        assertEquals(response.getMessage(), "Request timed out");
        assertEquals(response.getCode(), UCode.DEADLINE_EXCEEDED);
    }

    @Test
    @DisplayName("Test unsubscribe where the invokemethod throws an exception")
    public void testUnsubscribeWithException2() {
        UUri topic = createTopic();
        UStatus response = UPClient.create(new TimeoutUTransport()).unsubscribe(topic, new UListener() {
            @Override
            public void onReceive(UMessage message) {
                return;
            }
        }, new CallOptions(1));
        assertEquals(response.getMessage(), "Request timed out");
        assertEquals(response.getCode(), UCode.DEADLINE_EXCEEDED);
    }


    @Test
    @DisplayName("Test registering and unregister a request listener")
    public void test_registering_request_listener() {
        RequestHandler handler = new RequestHandler() {
            @Override
            public UPayload handleRequest(UMessage request) {
                return UPayload.EMPTY;
            }
        };
        RpcServer server = UPClient.create(new TestUTransport());
        UStatus status = server.registerRequestHandler(createMethodUri(), handler);
        assertEquals(status.getCode(), UCode.OK);
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
        RpcServer server = UPClient.create(new TestUTransport());
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
        RpcServer server = UPClient.create(new TestUTransport());
        UStatus status = server.unregisterRequestHandler(createMethodUri(), handler);
        assertEquals(status.getCode(), UCode.NOT_FOUND);
    }


    @Test
    @DisplayName("Test response handler being called for a different type of message than RESPONSE")
    public void testResponseHandlerForDifferentTypeOfMessage() {
        UTransport transport = new EchoUTransport();
        UPClient client = UPClient.create(transport);

        assertEquals(client.notify(createTopic(), transport.getSource(), null), 
            UStatus.newBuilder().setCode(UCode.OK).build());
    }
 
    @Test
    @DisplayName("Test response handler receiving a response when it doesn't have a pending request")
    public void testResponseHandlerWithoutPendingRequest() {
        UTransport transport = new TestUTransport();
        UPClient client1 = UPClient.create(transport);
        UPClient client2 = UPClient.create(transport);

        CompletionStage<UPayload> response1 = client1.invokeMethod(createMethodUri(), null, null);
        CompletionStage<UPayload> response2 = client2.invokeMethod(createMethodUri(), null, null);
        response2.toCompletableFuture().join();
        response1.toCompletableFuture().join();
        assertFalse(response1.toCompletableFuture().isCompletedExceptionally());
        assertFalse(response2.toCompletableFuture().isCompletedExceptionally());
    }


    @Test
    @DisplayName("Test request handler being called when someone sent a notification")
    public void testRequestHandlerForNotification() {
        UTransport transport = new EchoUTransport();
        UPClient client = UPClient.create(transport);

        client.registerRequestHandler(createMethodUri(), new RequestHandler() {
            @Override
            public UPayload handleRequest(UMessage request) {
                return UPayload.EMPTY;
            }
        });
        assertEquals(client.notify(createTopic(), transport.getSource(), null), 
            UStatus.newBuilder().setCode(UCode.OK).build());
    }
/*
    @Test
    @DisplayName("Test request handler being called when someone re send a request (to ourself)")
    public void testRequestHandlerForRequest() {
        UTransport transport = new EchoUTransport();
        UPClient client = UPClient.create(transport);
        UUri uri = UUri.newBuilder().setAuthorityName("Hartley").build();
        UPayload payload = UPayload.pack(uri);

        client.registerRequestHandler(createMethodUri(), new RequestHandler() {
            @Override
            public UPayload handleRequest(UMessage request) {
                return UPayload.pack(request.getPayload(), request.getAttributes().getPayloadFormat());
            }
        });
        UPayload response = client.invokeMethod(createMethodUri(), payload, null).toCompletableFuture().join();
        assertEquals(UPayload.unpack(response, UUri.class).get(), uri);
    }
*/
    @Test
    @DisplayName("Test request handler being called when someone re send a request (to ourself)")
    public void testRequestHandlerForRequest() {
        UTransport transport = new EchoUTransport();
        UPClient client = UPClient.create(transport);

        client.registerRequestHandler(createMethodUri(), new RequestHandler() {
            @Override
            public UPayload handleRequest(UMessage request) {
                return UPayload.EMPTY;
            }
        });
        UPayload response = client.invokeMethod(createMethodUri(), null, null).toCompletableFuture().join();
        assertEquals(response.toString(), UPayload.EMPTY.toString());
    }
   
    private UUri createTopic() {
        return UUri.newBuilder()
            .setAuthorityName("Hartley")
            .setUeId(4)
            .setUeVersionMajor(1)
            .setResourceId(0x8000)
            .build();
    }


    private UUri createDestinationUri() {
        return UUri.newBuilder()
            .setUeId(4)
            .setUeVersionMajor(1)
            .build();
    }


    private UUri createMethodUri() {
        return UUri.newBuilder()
            .setAuthorityName("Hartley")
            .setUeId(4)
            .setUeVersionMajor(1)
            .setResourceId(3).build();
    }


    /**
     * Test UTransport that will return SubscribeResponse
     */
    private class HappySubscribeUTransport extends TestUTransport {
        @Override
        public UMessage buildResponse(UMessage request) {
            return UMessageBuilder.response(request.getAttributes()).build(UPayload.pack(
                SubscriptionResponse.newBuilder().setTopic(createTopic()).build()));
        }
    };


    /**
     * Test UTransport that will return SubscribeResponse
     */
    private class HappyUnSubscribeUTransport extends TestUTransport {
        @Override
        public UMessage buildResponse(UMessage request) {
            return UMessageBuilder.response(request.getAttributes()).build(
                UPayload.pack(UnsubscribeResponse.newBuilder().build()));
        }
    };

    /**
     * Test UTransport that will set the commstatus for an error
     */
    private class CommStatusTransport extends TestUTransport {
        @Override
        public UMessage buildResponse(UMessage request) {
            UStatus status = UStatus.newBuilder()
                .setCode(UCode.FAILED_PRECONDITION)
                .setMessage("CommStatus Error")
                .build();
            return UMessageBuilder.response(request.getAttributes())
                .withCommStatus(status.getCode())
                .build(UPayload.pack(status));
        }
    };

}
