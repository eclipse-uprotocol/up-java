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

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.CyclicBarrier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import org.mockito.junit.jupiter.MockitoExtension;

import org.eclipse.uprotocol.core.usubscription.v3.SubscriptionResponse;
import org.eclipse.uprotocol.core.usubscription.v3.SubscriptionStatus;
import org.eclipse.uprotocol.core.usubscription.v3.UnsubscribeResponse;
import org.eclipse.uprotocol.core.usubscription.v3.Update;
import org.eclipse.uprotocol.transport.UListener;
import org.eclipse.uprotocol.transport.UTransport;
import org.eclipse.uprotocol.transport.builder.UMessageBuilder;
import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UMessage;
import org.eclipse.uprotocol.v1.UStatus;
import org.eclipse.uprotocol.v1.UUri;

@ExtendWith(MockitoExtension.class)
public class InMemorySubscriberTest {

    @Mock
    private UTransport transport;

    @Mock
    private InMemoryRpcClient rpcClient;

    @Mock
    private SimpleNotifier notifier;

    private final UUri topic = UUri.newBuilder().setAuthorityName("hartley").setUeId(3)
        .setUeVersionMajor(1).setResourceId(0x8000).build();

    private final UUri source = UUri.newBuilder().setAuthorityName("Hartley").setUeId(4)
        .setUeVersionMajor(1).build();
    private final UListener listener = new UListener() {
        @Override
        public void onReceive(UMessage message) {
            // Do nothing
        }
    };


    @BeforeEach
    public void setup() {
        rpcClient = mock(InMemoryRpcClient.class);
        notifier = mock(SimpleNotifier.class);
        transport = mock(UTransport.class);
    }


    @Test
    @DisplayName("Testing simple mock of RpcClient and notifier happy path")
    public void test_simple_mock_of_rpcClient_and_notifier() {
        
        final SubscriptionResponse response = SubscriptionResponse.newBuilder()
        .setTopic(topic)
        .setStatus(SubscriptionStatus.newBuilder().setState(SubscriptionStatus.State.SUBSCRIBED).build())
        .build();

        when(transport.registerListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));
        
        when(transport.getSource()).thenReturn(source);

        when(rpcClient.invokeMethod(any(UUri.class), any(UPayload.class), any(CallOptions.class)))
            .thenReturn(CompletableFuture.completedFuture(UPayload.pack(response)));

        when(notifier.registerNotificationListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));


        InMemorySubscriber subscriber = new InMemorySubscriber(transport, rpcClient, notifier);
        assertNotNull(subscriber);
        
        assertDoesNotThrow(() -> {
            assertEquals(subscriber.subscribe(topic, listener).toCompletableFuture().get().getStatus().getState(),
                SubscriptionStatus.State.SUBSCRIBED);
        });

        verify(rpcClient, times(1)).invokeMethod(any(), any(), any());
        verify(notifier, times(1)).registerNotificationListener(any(), any());
        verify(transport, times(1)).registerListener(any(), any());
        verify(transport, times(1)).getSource();
    } 
    

    @Test
    @DisplayName("Testing simple mock of RpcClient when usubscription returned SUBSCRIBE_PENDING")
    public void test_simple_mock_of_rpcClient_and_notifier_returned_subscribe_pending() {
        
        final SubscriptionResponse response = SubscriptionResponse.newBuilder()
        .setTopic(topic)
        .setStatus(SubscriptionStatus.newBuilder().setState(SubscriptionStatus.State.SUBSCRIBE_PENDING).build())
        .build();

        when(transport.registerListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));
        
        when(transport.getSource()).thenReturn(source);

        when(rpcClient.invokeMethod(any(UUri.class), any(UPayload.class), any(CallOptions.class)))
            .thenReturn(CompletableFuture.completedFuture(UPayload.pack(response)));

        when(notifier.registerNotificationListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));

        InMemorySubscriber subscriber = new InMemorySubscriber(transport, rpcClient, notifier);
        assertNotNull(subscriber);
        
        assertDoesNotThrow(() -> {
            assertEquals(subscriber.subscribe(topic, listener).toCompletableFuture().get().getStatus().getState(),
                SubscriptionStatus.State.SUBSCRIBE_PENDING);
        });

        verify(rpcClient, times(1)).invokeMethod(any(), any(), any());
        verify(notifier, times(1)).registerNotificationListener(any(), any());
        verify(transport, times(1)).registerListener(any(), any());
        verify(transport, times(1)).getSource();
    }


    @Test
    @DisplayName("Testing simple mock of RpcClient and notifier when usubscription returned UNSUBSCRIBED")
    public void test_simple_mock_when_subscription_service_returns_unsubscribed() {
        
        final SubscriptionResponse response = SubscriptionResponse.newBuilder()
        .setTopic(topic)
        .setStatus(SubscriptionStatus.newBuilder().setState(SubscriptionStatus.State.UNSUBSCRIBED).build())
        .build();

        when(transport.getSource()).thenReturn(source);

        when(rpcClient.invokeMethod(any(UUri.class), any(UPayload.class), any(CallOptions.class)))
            .thenReturn(CompletableFuture.completedFuture(UPayload.pack(response)));

        when(notifier.registerNotificationListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));


        InMemorySubscriber subscriber = new InMemorySubscriber(transport, rpcClient, notifier);
        assertNotNull(subscriber);
        
        assertDoesNotThrow(() -> {
            assertEquals(subscriber.subscribe(topic, listener).toCompletableFuture().get().getStatus().getState(),
                SubscriptionStatus.State.UNSUBSCRIBED);
        });

        verify(rpcClient, times(1)).invokeMethod(any(), any(), any());
        verify(notifier, times(1)).registerNotificationListener(any(), any());
        verify(transport, times(0)).registerListener(any(), any());
        verify(transport, times(1)).getSource();
    }



    @Test
    @DisplayName("Test subscribe using mock RpcClient and SimplerNotifier when invokemethod return an exception")
    void test_subscribe_using_mock_RpcClient_and_SimplerNotifier_when_invokemethod_return_an_exception() {
        when(transport.getSource()).thenReturn(source);
        when(rpcClient.invokeMethod(any(UUri.class), any(UPayload.class), any(CallOptions.class)))
            .thenReturn(CompletableFuture.failedFuture(
                    new UStatusException(UCode.PERMISSION_DENIED, "Not permitted")));

        when(notifier.registerNotificationListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));

        InMemorySubscriber subscriber = new InMemorySubscriber(transport, rpcClient, notifier);
        assertNotNull(subscriber);

        assertThrows(CompletionException.class, () -> {
            CompletionStage<SubscriptionResponse> response = subscriber.subscribe(topic, listener);
            assertTrue(response.toCompletableFuture().isCompletedExceptionally());
            response.handle((r, e) -> {
                e = e.getCause();
                assertTrue(e instanceof UStatusException);
                assertEquals(((UStatusException) e).getCode(), UCode.PERMISSION_DENIED);
                return null;
            });
            response.toCompletableFuture().join();
        });

        verify(rpcClient, times(1)).invokeMethod(any(), any(), any());
        verify(notifier, times(1)).registerNotificationListener(any(), any());
        verify(transport, times(0)).registerListener(any(), any());
        verify(transport, times(1)).getSource();
    }


    @Test
    @DisplayName("Test subscribe using mock RpcClient and SimplerNotifier when" + 
                 "we pass a subscription change notification handler")
    void test_subscribe_when_we_pass_a_subscription_change_notification_handler() {
        when(transport.registerListener(any(UUri.class), any(UListener.class)))
        .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));
    
        when(transport.getSource()).thenReturn(source);

        when(rpcClient.invokeMethod(any(UUri.class), any(UPayload.class), any(CallOptions.class)))
            .thenReturn(CompletableFuture.completedFuture(UPayload.pack(SubscriptionResponse.newBuilder()
                .setTopic(topic)
                .setStatus(SubscriptionStatus.newBuilder().setState(SubscriptionStatus.State.SUBSCRIBED).build())
                .build())));

        when(notifier.registerNotificationListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));

        InMemorySubscriber subscriber = new InMemorySubscriber(transport, rpcClient, notifier);
        assertNotNull(subscriber);

        SubscriptionChangeHandler handler = new SubscriptionChangeHandler() {
            @Override
            public void handleSubscriptionChange(UUri topic, SubscriptionStatus status) {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'handleSubscriptionChange'");
            }
        };
        assertDoesNotThrow(() -> {
            assertEquals(subscriber.subscribe(topic, listener, CallOptions.DEFAULT, handler)
                .toCompletableFuture().get().getStatus().getState(), SubscriptionStatus.State.SUBSCRIBED);
        });

        verify(rpcClient, times(1)).invokeMethod(any(), any(), any());
        verify(notifier, times(1)).registerNotificationListener(any(), any());
        verify(transport, times(1)).registerListener(any(), any());
        verify(transport, times(1)).getSource();
    }


    @Test
    @DisplayName("Test subscribe to the same topic twice passing the same parameters")
    void test_subscribe_when_we_try_to_subscribe_to_the_same_topic_twice() {
        when(transport.registerListener(any(UUri.class), any(UListener.class)))
        .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));
    
        when(transport.getSource()).thenReturn(source);

        when(rpcClient.invokeMethod(any(UUri.class), any(UPayload.class), any(CallOptions.class)))
            .thenReturn(CompletableFuture.completedFuture(UPayload.pack(SubscriptionResponse.newBuilder()
                .setTopic(topic)
                .setStatus(SubscriptionStatus.newBuilder().setState(SubscriptionStatus.State.SUBSCRIBED).build())
                .build())));

        when(notifier.registerNotificationListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));

        InMemorySubscriber subscriber = new InMemorySubscriber(transport, rpcClient, notifier);
        assertNotNull(subscriber);

        SubscriptionChangeHandler handler = new SubscriptionChangeHandler() {
            @Override
            public void handleSubscriptionChange(UUri topic, SubscriptionStatus status) {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'handleSubscriptionChange'");
            }
        };
        assertDoesNotThrow(() -> {
            assertEquals(subscriber.subscribe(topic, listener, CallOptions.DEFAULT, handler)
                .toCompletableFuture().get().getStatus().getState(), SubscriptionStatus.State.SUBSCRIBED);
        });

        assertDoesNotThrow(() -> {
            assertEquals(subscriber.subscribe(topic, listener, CallOptions.DEFAULT, handler)
                .toCompletableFuture().get().getStatus().getState(), SubscriptionStatus.State.SUBSCRIBED);
        });

        verify(rpcClient, times(2)).invokeMethod(any(), any(), any());
        verify(notifier, times(1)).registerNotificationListener(any(), any());
        verify(transport, times(2)).registerListener(any(), any());
        verify(transport, times(2)).getSource();
    }


    @Test
    @DisplayName("Test subscribe to the same topic twice passing different SubscriptionChangeHandlers")
    void test_subscribe_to_the_same_topic_twice_passing_different_subscription_change_handlers() {
        when(transport.registerListener(any(UUri.class), any(UListener.class)))
        .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));
    
        when(transport.getSource()).thenReturn(source);

        when(rpcClient.invokeMethod(any(UUri.class), any(UPayload.class), any(CallOptions.class)))
            .thenReturn(CompletableFuture.completedFuture(UPayload.pack(SubscriptionResponse.newBuilder()
                .setTopic(topic)
                .setStatus(SubscriptionStatus.newBuilder().setState(SubscriptionStatus.State.SUBSCRIBE_PENDING).build())
                .build())));

        when(notifier.registerNotificationListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));

        InMemorySubscriber subscriber = new InMemorySubscriber(transport, rpcClient, notifier);
        assertNotNull(subscriber);

        SubscriptionChangeHandler handler1 = new SubscriptionChangeHandler() {
            @Override
            public void handleSubscriptionChange(UUri topic, SubscriptionStatus status) {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'handleSubscriptionChange'");
            }
        };
        SubscriptionChangeHandler handler2 = new SubscriptionChangeHandler() {
            @Override
            public void handleSubscriptionChange(UUri topic, SubscriptionStatus status) {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'handleSubscriptionChange'");
            }
        };
        assertDoesNotThrow(() -> {
            assertEquals(subscriber.subscribe(topic, listener, CallOptions.DEFAULT, handler1)
                .toCompletableFuture().get().getStatus().getState(), SubscriptionStatus.State.SUBSCRIBE_PENDING);
        });

        assertThrows( CompletionException.class, () -> {
            CompletionStage<SubscriptionResponse> response = subscriber.subscribe(topic, listener, 
                CallOptions.DEFAULT, handler2);
            
            assertTrue(response.toCompletableFuture().isCompletedExceptionally());
            response.handle((r, e) -> {
                e = e.getCause();
                assertTrue(e instanceof UStatusException);
                assertEquals(((UStatusException) e).getCode(), UCode.ALREADY_EXISTS);
                return null;
            });
            response.toCompletableFuture().join();
        });

        verify(rpcClient, times(2)).invokeMethod(any(), any(), any());
        verify(notifier, times(1)).registerNotificationListener(any(), any());
        verify(transport, times(2)).registerListener(any(), any());
        verify(transport, times(2)).getSource();
    }


    @Test
    @DisplayName("Test unsubscribe using mock RpcClient and SimplerNotifier")
    void test_unsubscribe_using_mock_RpcClient_and_SimplerNotifier() {
        when(notifier.registerNotificationListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));

        when(transport.unregisterListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));

        when(rpcClient.invokeMethod(any(UUri.class), any(UPayload.class), any(CallOptions.class)))
            .thenReturn(CompletableFuture.completedFuture(UPayload.pack(UnsubscribeResponse.getDefaultInstance())));

        when(notifier.unregisterNotificationListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));

        InMemorySubscriber subscriber = new InMemorySubscriber(transport, rpcClient, notifier);
        assertNotNull(subscriber);

        assertDoesNotThrow(() -> {
            assertEquals(subscriber.unsubscribe(topic, listener).toCompletableFuture().get().getCode(), UCode.OK);
        });

        subscriber.close();

        verify(rpcClient, times(1)).invokeMethod(any(), any(), any());
        verify(notifier, times(1)).unregisterNotificationListener(any(), any());
        verify(notifier, times(1)).registerNotificationListener(any(), any());
        verify(transport, times(1)).unregisterListener(any(), any());
    }


    @Test
    @DisplayName("Test unsubscribe using when invokemethod return an exception")
    void test_unsubscribe_when_invokemethod_return_an_exception() {
        when(notifier.registerNotificationListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));

        when(rpcClient.invokeMethod(any(UUri.class), any(UPayload.class), any(CallOptions.class)))
            .thenReturn(CompletableFuture.completedFuture(UPayload.pack(UnsubscribeResponse.getDefaultInstance())));

        when(notifier.unregisterNotificationListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));
        
        when(transport.unregisterListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.ABORTED).build()));    

        InMemorySubscriber subscriber = new InMemorySubscriber(transport, rpcClient, notifier);
        assertNotNull(subscriber);

        assertDoesNotThrow(() -> {
            CompletionStage<UStatus> response = subscriber.unsubscribe(topic, listener);
            assertNotNull(response);
            assertFalse(response.toCompletableFuture().isCompletedExceptionally());
            assertEquals(response.toCompletableFuture().get().getCode(), UCode.ABORTED);
        });

        subscriber.close();

        verify(rpcClient, times(1)).invokeMethod(any(), any(), any());
        verify(notifier, times(1)).unregisterNotificationListener(any(), any());
        verify(notifier, times(1)).registerNotificationListener(any(), any());
        verify(transport, times(1)).unregisterListener(any(), any());
    }


    @Test
    @DisplayName("Test unsubscribe when invokemethod returned OK but we failed to unregister the listener")
    void test_unsubscribe_when_invokemethod_returned_OK_but_we_failed_to_unregister_the_listener() {
        when(notifier.registerNotificationListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));

        when(rpcClient.invokeMethod(any(UUri.class), any(UPayload.class), any(CallOptions.class)))
            .thenReturn(CompletableFuture.completedFuture(UPayload.pack(SubscriptionResponse.newBuilder()
                .setTopic(topic)
                .setStatus(SubscriptionStatus.newBuilder().setState(SubscriptionStatus.State.SUBSCRIBE_PENDING).build())
                .build())));

        when(transport.unregisterListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.ABORTED).build()));

        when(notifier.unregisterNotificationListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));

        InMemorySubscriber subscriber = new InMemorySubscriber(transport, rpcClient, notifier);
        assertNotNull(subscriber);

        assertDoesNotThrow(() -> {
            CompletionStage<UStatus> response = subscriber.unsubscribe(topic, listener);
            assertNotNull(response);
            assertFalse(response.toCompletableFuture().isCompletedExceptionally());
            assertEquals(response.toCompletableFuture().get().getCode(), UCode.ABORTED);
        });

        subscriber.close();

        verify(rpcClient, times(1)).invokeMethod(any(), any(), any());
        verify(notifier, times(1)).unregisterNotificationListener(any(), any());
        verify(notifier, times(1)).registerNotificationListener(any(), any());
        verify(transport, times(1)).unregisterListener(any(), any());
    }


    @Test
    @DisplayName("Test handling going from SUBSCRIBE_PENDING to SUBSCRIBED state")
    void test_handling_going_from_subscribe_pending_to_subscribed_state() 
        throws InterruptedException, BrokenBarrierException {

        // Create a CyclicBarrier with a count of 2 so we synchronize the subscription with the
        // changes in states for the usubscription service
        CyclicBarrier barrier = new CyclicBarrier(2);

        when(transport.registerListener(any(UUri.class), any(UListener.class)))
        .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));
    
        when(transport.getSource()).thenReturn(source);

        when(rpcClient.invokeMethod(any(UUri.class), any(UPayload.class), any(CallOptions.class)))
            .thenReturn(CompletableFuture.completedFuture(UPayload.pack(SubscriptionResponse.newBuilder()
                .setTopic(topic)
                .setStatus(SubscriptionStatus.newBuilder().setState(SubscriptionStatus.State.SUBSCRIBE_PENDING).build())
                .build())));

        // Fake sending the subscription change notification
        when(notifier.registerNotificationListener(any(UUri.class), any(UListener.class)))
            .thenAnswer(invocation -> {
                Executors.newSingleThreadExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            barrier.await();
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        Update update = Update.newBuilder().setTopic(topic).setStatus(SubscriptionStatus.newBuilder()
                            .setState(SubscriptionStatus.State.SUBSCRIBED).build()).build();
                        UMessage message = UMessageBuilder.notification(topic, source).build(UPayload.pack(update));
                        invocation.getArgument(1, UListener.class).onReceive(message);
                    }
                });
                return CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build());
            });
            

        InMemorySubscriber subscriber = new InMemorySubscriber(transport, rpcClient, notifier);
        assertNotNull(subscriber);

        SubscriptionChangeHandler handler = new SubscriptionChangeHandler() {
            @Override
            public void handleSubscriptionChange(UUri topic, SubscriptionStatus status) {
                assertEquals(status.getState(), SubscriptionStatus.State.SUBSCRIBED);
            }
        };
        assertDoesNotThrow(() -> {
            assertEquals(subscriber.subscribe(topic, listener, CallOptions.DEFAULT, handler)
                .toCompletableFuture().get().getStatus().getState(), SubscriptionStatus.State.SUBSCRIBE_PENDING);
        });

        // Wait for a specific time (500ms)
        Thread.sleep(100);
        
        // Release the barrier by calling await() again
        barrier.await();

        verify(rpcClient, times(1)).invokeMethod(any(), any(), any());
        verify(notifier, times(1)).registerNotificationListener(any(), any());
        verify(transport, times(1)).registerListener(any(), any());
        verify(transport, times(1)).getSource();
    }

    @Test
    @DisplayName("Test handling going from SUBSCRIBE_PENDING to SUBSCRIBED state but handler throws exception")
    void test_handling_going_from_subscribe_pending_to_subscribed_state_but_handler_throws_exception()
        throws InterruptedException, BrokenBarrierException {

        // Create a CyclicBarrier with a count of 2 so we synchronize the subscription with the
        // changes in states for the usubscription service
        CyclicBarrier barrier = new CyclicBarrier(2);

        when(transport.registerListener(any(UUri.class), any(UListener.class)))
        .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));
    
        when(transport.getSource()).thenReturn(source);

        when(rpcClient.invokeMethod(any(UUri.class), any(UPayload.class), any(CallOptions.class)))
            .thenReturn(CompletableFuture.completedFuture(UPayload.pack(SubscriptionResponse.newBuilder()
                .setTopic(topic)
                .setStatus(SubscriptionStatus.newBuilder().setState(SubscriptionStatus.State.SUBSCRIBE_PENDING).build())
                .build())));

        // Fake sending the subscription change notification
        when(notifier.registerNotificationListener(any(UUri.class), any(UListener.class)))
            .thenAnswer(invocation -> {
                Executors.newSingleThreadExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            barrier.await();
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        Update update = Update.newBuilder().setTopic(topic).setStatus(SubscriptionStatus.newBuilder()
                            .setState(SubscriptionStatus.State.SUBSCRIBED).build()).build();
                        UMessage message = UMessageBuilder.notification(topic, source).build(UPayload.pack(update));
                        invocation.getArgument(1, UListener.class).onReceive(message);
                    }
                });
                return CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build());
            });
            

        InMemorySubscriber subscriber = new InMemorySubscriber(transport, rpcClient, notifier);
        assertNotNull(subscriber);

        SubscriptionChangeHandler handler = new SubscriptionChangeHandler() {
            @Override
            public void handleSubscriptionChange(UUri topic, SubscriptionStatus status) {
                throw new UnsupportedOperationException("Throwing exception in the handler");
            }
        };
        assertDoesNotThrow(() -> {
            assertEquals(subscriber.subscribe(topic, listener, CallOptions.DEFAULT, handler)
                .toCompletableFuture().get().getStatus().getState(), SubscriptionStatus.State.SUBSCRIBE_PENDING);
        });

        // Wait for a specific time (500ms)
        Thread.sleep(100);
        
        // Release the barrier by calling await() again
        barrier.await();

        verify(rpcClient, times(1)).invokeMethod(any(), any(), any());
        verify(notifier, times(1)).registerNotificationListener(any(), any());
        verify(transport, times(1)).registerListener(any(), any());
        verify(transport, times(1)).getSource();
    }


    @Test
    @DisplayName("Test notification handling when we pass the wrong message type to the handler")
    void test_notification_handling_when_we_pass_the_wrong_message_type_to_the_handler() 
        throws InterruptedException, BrokenBarrierException {

        // Create a CyclicBarrier with a count of 2 so we synchronize the subscription with the
        // changes in states for the usubscription service
        CyclicBarrier barrier = new CyclicBarrier(2);

        when(transport.registerListener(any(UUri.class), any(UListener.class)))
        .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));
    
        when(transport.getSource()).thenReturn(source);

        when(rpcClient.invokeMethod(any(UUri.class), any(UPayload.class), any(CallOptions.class)))
            .thenReturn(CompletableFuture.completedFuture(UPayload.packToAny(SubscriptionResponse.newBuilder()
                .setTopic(topic)
                .setStatus(SubscriptionStatus.newBuilder().setState(SubscriptionStatus.State.SUBSCRIBE_PENDING).build())
                .build())));

        // Fake sending the subscription change notification
        when(notifier.registerNotificationListener(any(UUri.class), any(UListener.class)))
            .thenAnswer(invocation -> {
                Executors.newSingleThreadExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            barrier.await();
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        Update update = Update.newBuilder().setTopic(topic).setStatus(SubscriptionStatus.newBuilder()
                            .setState(SubscriptionStatus.State.SUBSCRIBED).build()).build();
                        UMessage message = UMessageBuilder.publish(topic).build(UPayload.pack(update));
                        invocation.getArgument(1, UListener.class).onReceive(message);
                    }
                });
                return CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build());
            });
            

        InMemorySubscriber subscriber = new InMemorySubscriber(transport, rpcClient, notifier);
        assertNotNull(subscriber);

        SubscriptionChangeHandler handler = new SubscriptionChangeHandler() {
            @Override
            public void handleSubscriptionChange(UUri topic, SubscriptionStatus status) {
                throw new UnsupportedOperationException("This handler should not be called");
            }
        };
        assertDoesNotThrow(() -> {
            assertEquals(subscriber.subscribe(topic, listener, CallOptions.DEFAULT, handler)
                .toCompletableFuture().get().getStatus().getState(), SubscriptionStatus.State.SUBSCRIBE_PENDING);
        });

        // Wait for a specific time (500ms)
        Thread.sleep(100);
        
        // Release the barrier by calling await() again
        barrier.await();

        verify(rpcClient, times(1)).invokeMethod(any(), any(), any());
        verify(notifier, times(1)).registerNotificationListener(any(), any());
        verify(transport, times(1)).registerListener(any(), any());
        verify(transport, times(1)).getSource();
    }


    @Test
    @DisplayName("Test notification handling when we get something other than the Update message")
    void test_notification_handling_when_we_get_something_other_than_the_update_message() throws InterruptedException, BrokenBarrierException {

        // Create a CyclicBarrier with a count of 2 so we synchronize the subscription with the
        // changes in states for the usubscription service
        CyclicBarrier barrier = new CyclicBarrier(2);

        when(transport.registerListener(any(UUri.class), any(UListener.class)))
        .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));
    
        when(transport.getSource()).thenReturn(source);

        when(rpcClient.invokeMethod(any(UUri.class), any(UPayload.class), any(CallOptions.class)))
            .thenReturn(CompletableFuture.completedFuture(UPayload.pack(SubscriptionResponse.newBuilder()
                .setTopic(topic)
                .setStatus(SubscriptionStatus.newBuilder().setState(SubscriptionStatus.State.SUBSCRIBE_PENDING).build())
                .build())));

        // Fake sending the subscription change notification
        when(notifier.registerNotificationListener(any(UUri.class), any(UListener.class)))
            .thenAnswer(invocation -> {
                Executors.newSingleThreadExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            barrier.await();
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        UMessage message = UMessageBuilder.notification(topic, source)
                            .build(UPayload.packToAny(source));
                        invocation.getArgument(1, UListener.class).onReceive(message);
                    }
                });
                return CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build());
            });
            

        InMemorySubscriber subscriber = new InMemorySubscriber(transport, rpcClient, notifier);
        assertNotNull(subscriber);

        SubscriptionChangeHandler handler = new SubscriptionChangeHandler() {
            @Override
            public void handleSubscriptionChange(UUri topic, SubscriptionStatus status) {
                throw new UnsupportedOperationException("This handler should not be called");
            }
        };
        assertDoesNotThrow(() -> {
            assertEquals(subscriber.subscribe(topic, listener, CallOptions.DEFAULT, handler)
                .toCompletableFuture().get().getStatus().getState(), SubscriptionStatus.State.SUBSCRIBE_PENDING);
        });

        // Wait for a specific time (500ms)
        Thread.sleep(100);
        
        // Release the barrier by calling await() again
        barrier.await();

        verify(rpcClient, times(1)).invokeMethod(any(), any(), any());
        verify(notifier, times(1)).registerNotificationListener(any(), any());
        verify(transport, times(1)).registerListener(any(), any());
        verify(transport, times(1)).getSource();
    }


    @Test
    @DisplayName("Test notification handling when we didn't register a notification handler")
    void test_notification_handling_when_we_didnt_register_a_notification_handler() 
        throws InterruptedException, BrokenBarrierException {

        // Create a CyclicBarrier with a count of 2 so we synchronize the subscription with the
        // changes in states for the usubscription service
        CyclicBarrier barrier = new CyclicBarrier(2);

        when(transport.registerListener(any(UUri.class), any(UListener.class)))
        .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));
    
        when(transport.getSource()).thenReturn(source);

        when(rpcClient.invokeMethod(any(UUri.class), any(UPayload.class), any(CallOptions.class)))
            .thenReturn(CompletableFuture.completedFuture(UPayload.pack(SubscriptionResponse.newBuilder()
                .setTopic(topic)
                .setStatus(SubscriptionStatus.newBuilder().setState(SubscriptionStatus.State.SUBSCRIBE_PENDING).build())
                .build())));

        // Fake sending the subscription change notification
        when(notifier.registerNotificationListener(any(UUri.class), any(UListener.class)))
            .thenAnswer(invocation -> {
                Executors.newSingleThreadExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            barrier.await();
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        Update update = Update.newBuilder().setTopic(topic).setStatus(SubscriptionStatus.newBuilder()
                            .setState(SubscriptionStatus.State.SUBSCRIBED).build()).build();
                        UMessage message = UMessageBuilder.notification(topic, source).build(UPayload.pack(update));
                        invocation.getArgument(1, UListener.class).onReceive(message);
                    }
                });
                return CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build());
            });
            

        InMemorySubscriber subscriber = new InMemorySubscriber(transport, rpcClient, notifier);
        assertNotNull(subscriber);

        assertDoesNotThrow(() -> {
            assertEquals(subscriber.subscribe(topic, listener)
                .toCompletableFuture().get().getStatus().getState(), SubscriptionStatus.State.SUBSCRIBE_PENDING);
        });

        // Wait for a specific time (100ms)
        Thread.sleep(100);
        
        // Release the barrier by calling await() again
        barrier.await();

        verify(rpcClient, times(1)).invokeMethod(any(), any(), any());
        verify(notifier, times(1)).registerNotificationListener(any(), any());
        verify(transport, times(1)).registerListener(any(), any());
        verify(transport, times(1)).getSource();
    }
}
