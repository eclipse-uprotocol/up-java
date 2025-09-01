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
package org.eclipse.uprotocol.client.usubscription.v3;

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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import org.mockito.junit.jupiter.MockitoExtension;
import org.eclipse.uprotocol.communication.CallOptions;
import org.eclipse.uprotocol.communication.InMemoryRpcClient;
import org.eclipse.uprotocol.communication.SimpleNotifier;
import org.eclipse.uprotocol.communication.UPayload;
import org.eclipse.uprotocol.communication.UStatusException;
import org.eclipse.uprotocol.core.usubscription.v3.FetchSubscribersResponse;
import org.eclipse.uprotocol.core.usubscription.v3.FetchSubscriptionsRequest;
import org.eclipse.uprotocol.core.usubscription.v3.FetchSubscriptionsResponse;
import org.eclipse.uprotocol.core.usubscription.v3.NotificationsResponse;
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
public class InMemoryUSubscriptionClientTest {

    @Mock
    private UTransport transport;

    @Mock
    private InMemoryRpcClient rpcClient;

    @Mock
    private SimpleNotifier notifier;

    @Mock
    private SubscriptionChangeHandler subscriptionChangeHandler;

    private final UUri topic = UUri.newBuilder()
        .setAuthorityName("hartley")
        .setUeId(3)
        .setUeVersionMajor(1)
        .setResourceId(0x8000)
        .build();

    private final UUri source = UUri.newBuilder()
        .setAuthorityName("Hartley")
        .setUeId(4)
        .setUeVersionMajor(1)
        .build();
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
    @DisplayName("Testing creation of InMemoryUSubscriptionClient passing only the transport")
    public void testCreationOfInMemoryUSubscriptionClientPassingOnlyTheTransport() {
        when(transport.getSource()).thenReturn(source);

        when(transport.registerListener(any(UUri.class), any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));

        assertDoesNotThrow(() -> {
            new InMemoryUSubscriptionClient(transport);
        });

        verify(transport, times(2)).getSource();
        verify(transport, times(2)).registerListener(any(), any(), any());
    }


    @Test
    @DisplayName("Testing creation of InMemoryUSubscriptionClient passing null for the transport")
    public void testCreationOfInMemoryUSubscriptionClientPassingNullForTheTransport() {
        assertThrows(NullPointerException.class, () -> {
            new InMemoryUSubscriptionClient(null);
        });
    }


    @Test
    @DisplayName("Testing simple mock of RpcClient and notifier happy path")
    public void testSimpleMockOfRpcClientAndNotifier() {
        
        final SubscriptionResponse response = SubscriptionResponse.newBuilder()
            .setTopic(topic)
            .setStatus(SubscriptionStatus.newBuilder().setState(SubscriptionStatus.State.SUBSCRIBED).build())
            .build();

        when(transport.registerListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));
        
        when(rpcClient.invokeMethod(any(UUri.class), any(UPayload.class), any(CallOptions.class)))
            .thenReturn(CompletableFuture.completedFuture(UPayload.pack(response)));

        when(notifier.registerNotificationListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));


        InMemoryUSubscriptionClient subscriber = new InMemoryUSubscriptionClient(transport, rpcClient, notifier);
        assertNotNull(subscriber);
        
        assertDoesNotThrow(() -> {
            assertEquals(subscriber.subscribe(topic, listener).toCompletableFuture().get().getStatus().getState(),
                SubscriptionStatus.State.SUBSCRIBED);
        });

        verify(rpcClient, times(1)).invokeMethod(any(), any(), any());
        verify(notifier, times(1)).registerNotificationListener(any(), any());
        verify(transport, times(1)).registerListener(any(), any());
    } 
    

    @Test
    @DisplayName("Testing simple mock of RpcClient when usubscription returned SUBSCRIBE_PENDING")
    public void testSimpleMockOfRpcClientAndNotifierReturnedSubscribePending() {
        
        final SubscriptionResponse response = SubscriptionResponse.newBuilder()
            .setTopic(topic)
            .setStatus(SubscriptionStatus.newBuilder().setState(SubscriptionStatus.State.SUBSCRIBE_PENDING).build())
            .build();

        when(transport.registerListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));
        
        when(rpcClient.invokeMethod(any(UUri.class), any(UPayload.class), any(CallOptions.class)))
            .thenReturn(CompletableFuture.completedFuture(UPayload.pack(response)));

        when(notifier.registerNotificationListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));

        InMemoryUSubscriptionClient subscriber = new InMemoryUSubscriptionClient(transport, rpcClient, notifier);
        assertNotNull(subscriber);
        
        assertDoesNotThrow(() -> {
            assertEquals(subscriber.subscribe(topic, listener).toCompletableFuture().get().getStatus().getState(),
                SubscriptionStatus.State.SUBSCRIBE_PENDING);
        });

        verify(rpcClient, times(1)).invokeMethod(any(), any(), any());
        verify(notifier, times(1)).registerNotificationListener(any(), any());
        verify(transport, times(1)).registerListener(any(), any());
    }


    @Test
    @DisplayName("Testing simple mock of RpcClient and notifier when usubscription returned UNSUBSCRIBED")
    public void testSimpleMockWhenSubscriptionServiceReturnsUnsubscribed() {
        
        final SubscriptionResponse response = SubscriptionResponse.newBuilder()
            .setTopic(topic)
            .setStatus(SubscriptionStatus.newBuilder().setState(SubscriptionStatus.State.UNSUBSCRIBED).build())
            .build();

        when(rpcClient.invokeMethod(any(UUri.class), any(UPayload.class), any(CallOptions.class)))
            .thenReturn(CompletableFuture.completedFuture(UPayload.pack(response)));

        when(notifier.registerNotificationListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));


        InMemoryUSubscriptionClient subscriber = new InMemoryUSubscriptionClient(transport, rpcClient, notifier);
        assertNotNull(subscriber);
        
        assertDoesNotThrow(() -> {
            assertEquals(subscriber.subscribe(topic, listener).toCompletableFuture().get().getStatus().getState(),
                SubscriptionStatus.State.UNSUBSCRIBED);
        });

        verify(rpcClient, times(1)).invokeMethod(any(), any(), any());
        verify(notifier, times(1)).registerNotificationListener(any(), any());
        verify(transport, never()).registerListener(any(), any());
    }



    @Test
    @DisplayName("Test subscribe using mock RpcClient and SimplerNotifier when invokemethod return an exception")
    void testSubscribeUsingMockRpcClientAndSimplerNotifierWhenInvokemethodReturnAnException() {
        when(rpcClient.invokeMethod(any(UUri.class), any(UPayload.class), any(CallOptions.class)))
            .thenReturn(CompletableFuture.failedFuture(
                    new UStatusException(UCode.PERMISSION_DENIED, "Not permitted")));

        when(notifier.registerNotificationListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));

        InMemoryUSubscriptionClient subscriber = new InMemoryUSubscriptionClient(transport, rpcClient, notifier);
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
    }


    @Test
    @DisplayName("Test subscribe using mock RpcClient and SimplerNotifier when" + 
                 "we pass a subscription change notification handler")
    void testSubscribeWhenWePassASubscriptionChangeNotificationHandler() {
        when(transport.registerListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));
    
        when(rpcClient.invokeMethod(any(UUri.class), any(UPayload.class), any(CallOptions.class)))
            .thenReturn(CompletableFuture.completedFuture(UPayload.pack(SubscriptionResponse.newBuilder()
                .setTopic(topic)
                .setStatus(SubscriptionStatus.newBuilder().setState(SubscriptionStatus.State.SUBSCRIBED).build())
                .build())));

        when(notifier.registerNotificationListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));

        InMemoryUSubscriptionClient subscriber = new InMemoryUSubscriptionClient(transport, rpcClient, notifier);
        assertNotNull(subscriber);

        assertDoesNotThrow(() -> {
            assertEquals(subscriber.subscribe(topic, listener, CallOptions.DEFAULT, subscriptionChangeHandler)
                .toCompletableFuture().get().getStatus().getState(), SubscriptionStatus.State.SUBSCRIBED);
        });

        verify(rpcClient, times(1)).invokeMethod(any(), any(), any());
        verify(notifier, times(1)).registerNotificationListener(any(), any());
        verify(transport, times(1)).registerListener(any(), any());
    }


    @Test
    @DisplayName("Test subscribe to the same topic twice passing the same parameters")
    void testSubscribeWhenWeTryToSubscribeToTheSameTopicTwice() {
        when(transport.registerListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));
    
        when(rpcClient.invokeMethod(any(UUri.class), any(UPayload.class), any(CallOptions.class)))
            .thenReturn(CompletableFuture.completedFuture(UPayload.pack(SubscriptionResponse.newBuilder()
                .setTopic(topic)
                .setStatus(SubscriptionStatus.newBuilder().setState(SubscriptionStatus.State.SUBSCRIBED).build())
                .build())));

        when(notifier.registerNotificationListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));

        InMemoryUSubscriptionClient subscriber = new InMemoryUSubscriptionClient(transport, rpcClient, notifier);
        assertNotNull(subscriber);

        assertDoesNotThrow(() -> {
            assertEquals(subscriber.subscribe(topic, listener, CallOptions.DEFAULT, subscriptionChangeHandler)
                .toCompletableFuture().get().getStatus().getState(), SubscriptionStatus.State.SUBSCRIBED);
        });

        assertDoesNotThrow(() -> {
            assertEquals(subscriber.subscribe(topic, listener, CallOptions.DEFAULT, subscriptionChangeHandler)
                .toCompletableFuture().get().getStatus().getState(), SubscriptionStatus.State.SUBSCRIBED);
        });

        verify(rpcClient, times(2)).invokeMethod(any(), any(), any());
        verify(notifier, times(1)).registerNotificationListener(any(), any());
        verify(transport, times(2)).registerListener(any(), any());
    }


    @Test
    @DisplayName("Test subscribe to the same topic twice passing different SubscriptionChangeHandlers")
    void testSubscribeToTheSameTopicTwicePassingDifferentSubscriptionChangeHandlers() {
        when(transport.registerListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));
    
        when(rpcClient.invokeMethod(any(UUri.class), any(UPayload.class), any(CallOptions.class)))
            .thenReturn(CompletableFuture.completedFuture(UPayload.pack(SubscriptionResponse.newBuilder()
                .setTopic(topic)
                .setStatus(SubscriptionStatus.newBuilder().setState(SubscriptionStatus.State.SUBSCRIBE_PENDING).build())
                .build())));

        when(notifier.registerNotificationListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));

        InMemoryUSubscriptionClient subscriber = new InMemoryUSubscriptionClient(transport, rpcClient, notifier);
        assertNotNull(subscriber);

        assertDoesNotThrow(() -> {
            assertEquals(subscriber.subscribe(topic, listener, CallOptions.DEFAULT, subscriptionChangeHandler)
                .toCompletableFuture().get().getStatus().getState(), SubscriptionStatus.State.SUBSCRIBE_PENDING);
        });

        assertThrows( CompletionException.class, () -> {
            CompletionStage<SubscriptionResponse> response = subscriber.subscribe(
                topic,
                listener,
                CallOptions.DEFAULT,
                mock(SubscriptionChangeHandler.class));
            
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
    }


    @Test
    @DisplayName("Test unsubscribe using mock RpcClient and SimplerNotifier")
    void testUnsubscribeUsingMockRpcClientAndSimplerNotifier() {
        when(notifier.registerNotificationListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));

        when(transport.unregisterListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));

        when(rpcClient.invokeMethod(any(UUri.class), any(UPayload.class), any(CallOptions.class)))
            .thenReturn(CompletableFuture.completedFuture(UPayload.pack(UnsubscribeResponse.getDefaultInstance())));

        when(notifier.unregisterNotificationListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));

        InMemoryUSubscriptionClient subscriber = new InMemoryUSubscriptionClient(transport, rpcClient, notifier);
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
    void testUnsubscribeWhenInvokemethodReturnAnException() {
        when(notifier.registerNotificationListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));

        when(rpcClient.invokeMethod(any(UUri.class), any(UPayload.class), any(CallOptions.class)))
            .thenReturn(CompletableFuture.failedStage(new UStatusException(UCode.CANCELLED, "Operation cancelled")));

        when(notifier.unregisterNotificationListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));
        
        InMemoryUSubscriptionClient subscriber = new InMemoryUSubscriptionClient(transport, rpcClient, notifier);
        assertNotNull(subscriber);

        assertDoesNotThrow(() -> {
            CompletionStage<UStatus> response = subscriber.unsubscribe(topic, listener);
            assertNotNull(response);
            assertFalse(response.toCompletableFuture().isCompletedExceptionally());
            assertEquals(response.toCompletableFuture().get().getCode(), UCode.CANCELLED);
        });

        subscriber.close();

        verify(rpcClient, times(1)).invokeMethod(any(), any(), any());
        verify(notifier, times(1)).unregisterNotificationListener(any(), any());
        verify(notifier, times(1)).registerNotificationListener(any(), any());
        verify(transport, times(0)).unregisterListener(any(), any());
    }


    @Test
    @DisplayName("Test unsubscribe when invokemethod returned OK but we failed to unregister the listener")
    void testUnsubscribeWhenInvokemethodReturnedOkButWeFailedToUnregisterTheListener() {
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

        InMemoryUSubscriptionClient subscriber = new InMemoryUSubscriptionClient(transport, rpcClient, notifier);
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
    void testHandlingGoingFromSubscribePendingToSubscribedState() 
        throws InterruptedException, BrokenBarrierException {

        // Create a CyclicBarrier with a count of 2 so we synchronize the subscription with the
        // changes in states for the usubscription service
        CyclicBarrier barrier = new CyclicBarrier(2);

        when(transport.registerListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));
    
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
            

        InMemoryUSubscriptionClient subscriber = new InMemoryUSubscriptionClient(transport, rpcClient, notifier);
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
    }

    @Test
    @DisplayName("Test handling going from SUBSCRIBE_PENDING to SUBSCRIBED state but handler throws exception")
    void testHandlingGoingFromSubscribePendingToSubscribedStateButHandlerThrowsException()
        throws InterruptedException, BrokenBarrierException {

        // Create a CyclicBarrier with a count of 2 so we synchronize the subscription with the
        // changes in states for the usubscription service
        CyclicBarrier barrier = new CyclicBarrier(2);

        when(transport.registerListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));
    
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
            

        InMemoryUSubscriptionClient subscriber = new InMemoryUSubscriptionClient(transport, rpcClient, notifier);
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
    }


    @Test
    @DisplayName("Test notification handling when we pass the wrong message type to the handler")
    void testNotificationHandlingWhenWePassTheWrongMessageTypeToTheHandler() 
        throws InterruptedException, BrokenBarrierException {

        // Create a CyclicBarrier with a count of 2 so we synchronize the subscription with the
        // changes in states for the usubscription service
        CyclicBarrier barrier = new CyclicBarrier(2);

        when(transport.registerListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));
    
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
            

        InMemoryUSubscriptionClient subscriber = new InMemoryUSubscriptionClient(transport, rpcClient, notifier);
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
    }


    @Test
    @DisplayName("Test notification handling when we get something other than the Update message")
    void testNotificationHandlingWhenWeGetSomethingOtherThanTheUpdateMessage() 
            throws InterruptedException, BrokenBarrierException {

        // Create a CyclicBarrier with a count of 2 so we synchronize the subscription with the
        // changes in states for the usubscription service
        CyclicBarrier barrier = new CyclicBarrier(2);

        when(transport.registerListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));
    
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
            

        InMemoryUSubscriptionClient subscriber = new InMemoryUSubscriptionClient(transport, rpcClient, notifier);
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
    }


    @Test
    @DisplayName("Test notification handling when we didn't register a notification handler")
    void testNotificationHandlingWhenWeDidntRegisterANotificationHandler() 
        throws InterruptedException, BrokenBarrierException {

        // Create a CyclicBarrier with a count of 2 so we synchronize the subscription with the
        // changes in states for the usubscription service
        CyclicBarrier barrier = new CyclicBarrier(2);

        when(transport.registerListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));
    
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
            

        InMemoryUSubscriptionClient subscriber = new InMemoryUSubscriptionClient(transport, rpcClient, notifier);
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
    }


    @Test
    @DisplayName("Test registerNotification() api when passed a null topic")
    void testRegisterNotificationApiWhenPassedANullTopic() {
        when(notifier.registerNotificationListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));

        InMemoryUSubscriptionClient subscriber = new InMemoryUSubscriptionClient(transport, rpcClient, notifier);
        assertNotNull(subscriber);

        assertThrows(NullPointerException.class, () -> {
            subscriber.registerForNotifications(null, subscriptionChangeHandler);
        });

        verify(notifier, times(1)).registerNotificationListener(any(), any());
    }


    @Test
    @DisplayName("Test registerNotification() api when passed a null handler")
    void testRegisterNotificationApiWhenPassedANullHandler() {
        when(notifier.registerNotificationListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));

        InMemoryUSubscriptionClient subscriber = new InMemoryUSubscriptionClient(transport, rpcClient, notifier);
        assertNotNull(subscriber);

        assertThrows(NullPointerException.class, () -> {
            subscriber.registerForNotifications(topic, null);
        });

        verify(notifier, times(1)).registerNotificationListener(any(), any());
    }

    @Test
    @DisplayName("Test unregisterListener() api for the happy path")
    void testUnregisterListenerApiForTheHappyPath() {
        when(notifier.registerNotificationListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));

        when(transport.registerListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));

        when(transport.unregisterListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));

        when(rpcClient.invokeMethod(any(UUri.class), any(UPayload.class), any(CallOptions.class)))
            .thenReturn(CompletableFuture.completedFuture(UPayload.pack(SubscriptionResponse.newBuilder()
                .setTopic(topic)
                .setStatus(SubscriptionStatus.newBuilder().setState(SubscriptionStatus.State.SUBSCRIBED).build())
                .build())));

        InMemoryUSubscriptionClient subscriber = new InMemoryUSubscriptionClient(transport, rpcClient, notifier);
        assertNotNull(subscriber);

        assertDoesNotThrow(() -> {
            assertEquals(subscriber.subscribe(topic, listener).toCompletableFuture().get().getStatus().getState(),
                SubscriptionStatus.State.SUBSCRIBED);
            assertEquals(subscriber.unregisterListener(topic, listener).toCompletableFuture().get().getCode(),
                UCode.OK);
        });

        verify(transport, times(1)).unregisterListener(any(), any());
    }


    @Test
    @DisplayName("Test registerNotification() api when passed a valid topic and handler")
    void testRegisterNotificationApiWhenPassedAValidTopicAndHandler() {
        when(notifier.registerNotificationListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));
        
        when(transport.getSource()).thenReturn(source);

        when(rpcClient.invokeMethod(any(UUri.class), any(UPayload.class), any(CallOptions.class)))
            .thenReturn(CompletableFuture.completedFuture(UPayload.pack(NotificationsResponse.getDefaultInstance())));

        InMemoryUSubscriptionClient subscriber = new InMemoryUSubscriptionClient(transport, rpcClient, notifier);
        assertNotNull(subscriber);

        UUri topic = UUri.newBuilder(transport.getSource()).setResourceId(0x8000).build();

        assertDoesNotThrow(() -> subscriber.registerForNotifications(topic, subscriptionChangeHandler)
            .toCompletableFuture().get());
        verify(notifier, times(1)).registerNotificationListener(any(), any());
    }


    @Test
    @DisplayName("Test registerNotification() api when invokeMethod() throws an exception")
    void testRegisterNotificationApiWhenInvokeMethodThrowsAnException() {
        when(notifier.registerNotificationListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));

        when(transport.getSource()).thenReturn(source);

        when(rpcClient.invokeMethod(any(UUri.class), any(UPayload.class), any(CallOptions.class)))
            .thenReturn(CompletableFuture.failedFuture(
                new UStatusException(UCode.PERMISSION_DENIED, "Not permitted")));

        InMemoryUSubscriptionClient subscriber = new InMemoryUSubscriptionClient(transport, rpcClient, notifier);
        assertNotNull(subscriber);

        UUri topic = UUri.newBuilder(transport.getSource()).setResourceId(0x8000).build();

        assertDoesNotThrow(() -> {
            var response = subscriber.registerForNotifications(topic, subscriptionChangeHandler);
            assertTrue(response.toCompletableFuture().isCompletedExceptionally());
            response.handle((r, e) -> {
                e = e.getCause();
                assertTrue(e instanceof UStatusException);
                assertEquals(((UStatusException) e).getCode(), UCode.PERMISSION_DENIED);
                return null;
            }).toCompletableFuture().get();
        });
    }


    @Test
    @DisplayName("Test registerNotification() calling the API twice passing the same topic and handler")
    void testRegisterNotificationApiCallingTheApiTwicePassingTheSameTopicAndHandler() {
        when(notifier.registerNotificationListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));

        when(transport.getSource()).thenReturn(source);

        when(rpcClient.invokeMethod(any(UUri.class), any(UPayload.class), any(CallOptions.class)))
            .thenReturn(CompletableFuture.completedFuture(UPayload.pack(NotificationsResponse.getDefaultInstance())));

        InMemoryUSubscriptionClient subscriber = new InMemoryUSubscriptionClient(transport, rpcClient, notifier);
        assertNotNull(subscriber);

        UUri topic = UUri.newBuilder(transport.getSource()).setResourceId(0x8000).build();

        assertDoesNotThrow(() -> {
            assertTrue(NotificationsResponse.getDefaultInstance().equals(
                subscriber.registerForNotifications(topic, subscriptionChangeHandler).toCompletableFuture().get()));
        });

        assertDoesNotThrow(() -> {
            assertTrue(NotificationsResponse.getDefaultInstance().equals(
                subscriber.registerForNotifications(topic, subscriptionChangeHandler).toCompletableFuture().get()));
        });

        verify(notifier, times(1)).registerNotificationListener(any(), any());
    }


    @Test
    @DisplayName("Test registerNotification() calling the API twice passing the same topic but different handlers")
    void testRegisterNotificationApiCallingTheApiTwicePassingTheSameTopicButDifferentHandlers() {
        when(notifier.registerNotificationListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));

        when(transport.getSource()).thenReturn(source);

        when(rpcClient.invokeMethod(any(UUri.class), any(UPayload.class), any(CallOptions.class)))
            .thenReturn(CompletableFuture.completedFuture(UPayload.pack(NotificationsResponse.getDefaultInstance())));

        InMemoryUSubscriptionClient subscriber = new InMemoryUSubscriptionClient(transport, rpcClient, notifier);
        assertNotNull(subscriber);

        UUri topic = UUri.newBuilder(transport.getSource()).setResourceId(0x8000).build();

        assertDoesNotThrow(() -> {
            assertTrue(NotificationsResponse.getDefaultInstance().equals(
                subscriber.registerForNotifications(topic, subscriptionChangeHandler).toCompletableFuture().get()));
        });

        assertDoesNotThrow(() -> {
            CompletionStage<NotificationsResponse> response = subscriber.registerForNotifications(
                topic,
                mock(SubscriptionChangeHandler.class));
            assertTrue(response.toCompletableFuture().isCompletedExceptionally());
            
            response.handle((r, e) -> {
                assertNotNull(e);
                e = e.getCause();
                assertTrue(e instanceof UStatusException);
                assertEquals(((UStatusException) e).getCode(), UCode.ALREADY_EXISTS);
                return null;
            }).toCompletableFuture().join();
        });

        verify(notifier, times(1)).registerNotificationListener(any(), any());
    }


    @Test
    @DisplayName("Test unregisterNotification() api for the happy path")
    void testUnregisterNotificationApiForTheHappyPath() {
        when(notifier.registerNotificationListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));

        when(transport.getSource()).thenReturn(source);

        when(rpcClient.invokeMethod(any(UUri.class), any(UPayload.class), any(CallOptions.class)))
            .thenReturn(CompletableFuture.completedFuture(UPayload.pack(NotificationsResponse.getDefaultInstance())));

        InMemoryUSubscriptionClient subscriber = new InMemoryUSubscriptionClient(transport, rpcClient, notifier);
        assertNotNull(subscriber);

        UUri topic = UUri.newBuilder(transport.getSource()).setResourceId(0x8000).build();

        assertDoesNotThrow(() -> {
            subscriber.registerForNotifications(topic, subscriptionChangeHandler).toCompletableFuture().get();
            subscriber.unregisterForNotifications(topic).toCompletableFuture().get();
        });

        verify(notifier, times(1)).registerNotificationListener(any(), any());
    }


    @Test
    @DisplayName("Test unregisterNotification() api when passed a null topic")
    void testUnregisterNotificationApiWhenPassedANullTopic() {
        when(notifier.registerNotificationListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));

        InMemoryUSubscriptionClient subscriber = new InMemoryUSubscriptionClient(transport, rpcClient, notifier);
        assertNotNull(subscriber);

        assertThrows(NullPointerException.class, () -> {
            subscriber.unregisterForNotifications(null);
        });

        verify(notifier, times(1)).registerNotificationListener(any(), any());
    }


    @Test
    @DisplayName("Test unregisterNotification() api when passed a null handler")
    void testUnregisterNotificationApiWhenPassedANullHandler() {
        when(notifier.registerNotificationListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));

        InMemoryUSubscriptionClient subscriber = new InMemoryUSubscriptionClient(transport, rpcClient, notifier);
        assertNotNull(subscriber);

        assertThrows(NullPointerException.class, () -> {
            subscriber.unregisterForNotifications(topic, null);
        });

        verify(notifier, times(1)).registerNotificationListener(any(), any());
    }


    @Test
    @DisplayName("Test calling unregisterNotification() api when we never registered the notification below")
    void testCallingUnregisterNotificationApiWhenWeNeverRegisteredTheNotificationBelow() {
        when(notifier.registerNotificationListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));

        when(rpcClient.invokeMethod(any(UUri.class), any(UPayload.class), any(CallOptions.class)))
            .thenReturn(CompletableFuture.failedFuture(new UStatusException(UCode.NOT_FOUND, "Not found")));

        InMemoryUSubscriptionClient subscriber = new InMemoryUSubscriptionClient(transport, rpcClient, notifier);
        assertNotNull(subscriber);

        UUri topic = UUri.newBuilder(source).setResourceId(0x8000).build();

        assertDoesNotThrow(() -> {
            CompletionStage<NotificationsResponse> response = subscriber.unregisterForNotifications(topic);
            assertTrue(response.toCompletableFuture().isCompletedExceptionally());

            response.handle((r, e) -> {
                assertNotNull(e);
                e = e.getCause();
                assertTrue(e instanceof UStatusException);
                assertEquals(((UStatusException) e).getCode(), UCode.NOT_FOUND);
                return null;
            }).toCompletableFuture().join();
        });

        verify(transport, never()).getSource();
        verify(notifier, times(1)).registerNotificationListener(any(), any());
    }


    @Test
    @DisplayName("Test fetchSubscribers() when pssing null topic")
    void testFetchSubscribersWhenPassingNullTopic() {
        when(notifier.registerNotificationListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));

        InMemoryUSubscriptionClient subscriber = new InMemoryUSubscriptionClient(transport, rpcClient, notifier);
        assertNotNull(subscriber);

        assertThrows(NullPointerException.class, () -> {
            subscriber.fetchSubscribers(null).toCompletableFuture().get();
        });
    }


    @Test
    @DisplayName("Test fetchSubscribers() when passing a valid topic")
    void testFetchSubscribersWhenPassingAValidTopic() {
        when(notifier.registerNotificationListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));

        when(rpcClient.invokeMethod(any(UUri.class), any(UPayload.class), any(CallOptions.class)))
            .thenReturn(CompletableFuture.completedFuture(
                UPayload.pack(FetchSubscribersResponse.getDefaultInstance())));

        InMemoryUSubscriptionClient subscriber = new InMemoryUSubscriptionClient(transport, rpcClient, notifier);
        assertNotNull(subscriber);

        assertDoesNotThrow(() -> {
            assertTrue(FetchSubscribersResponse.getDefaultInstance().equals(
                subscriber.fetchSubscribers(topic).toCompletableFuture().get()));
        });

        verify(notifier, times(1)).registerNotificationListener(any(), any());
    }


    @Test
    @DisplayName("Test fetchSubscribers() when passing when invokeMethod returns NOT_PERMITTED")
    void testFetchSubscribersWhenPassingWhenInvokeMethodReturnsNotPermitted() {
        when(notifier.registerNotificationListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));

        when(rpcClient.invokeMethod(any(UUri.class), any(UPayload.class), any(CallOptions.class)))
            .thenReturn(CompletableFuture.failedFuture(
                new UStatusException(UCode.PERMISSION_DENIED, "Not permitted")));

        InMemoryUSubscriptionClient subscriber = new InMemoryUSubscriptionClient(transport, rpcClient, notifier);
        assertNotNull(subscriber);

        assertDoesNotThrow(() -> {
            CompletionStage<FetchSubscribersResponse> response = subscriber.fetchSubscribers(topic);
            assertTrue(response.toCompletableFuture().isCompletedExceptionally());
            response.handle((r, e) -> {
                e = e.getCause();
                assertTrue(e instanceof UStatusException);
                assertEquals(((UStatusException) e).getCode(), UCode.PERMISSION_DENIED);
                return null;
            }).toCompletableFuture().join();
        });

        verify(notifier, times(1)).registerNotificationListener(any(), any());
    }


    @Test
    @DisplayName("Test fetchSubscriptions() passing null FetchSubscriptionRequest")
    void testFetchSubscriptionsPassingNullFetchSubscriptionRequest() {
        when(notifier.registerNotificationListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));

        InMemoryUSubscriptionClient subscriber = new InMemoryUSubscriptionClient(transport, rpcClient, notifier);
        assertNotNull(subscriber);

        assertThrows(NullPointerException.class, () -> {
            subscriber.fetchSubscriptions(null).toCompletableFuture().get();
        });

        verify(notifier, times(1)).registerNotificationListener(any(), any());
    }


    @Test
    @DisplayName("Test fetchSubscriptions() passing a valid FetchSubscriptionRequest")
    void testFetchSubscriptionsPassingAValidFetchSubscriptionRequest() {
        when(notifier.registerNotificationListener(any(UUri.class), any(UListener.class)))
            .thenReturn(CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build()));

        when(rpcClient.invokeMethod(any(UUri.class), any(UPayload.class), any(CallOptions.class)))
            .thenReturn(CompletableFuture.completedFuture(
                UPayload.pack(FetchSubscriptionsResponse.getDefaultInstance())));

        InMemoryUSubscriptionClient subscriber = new InMemoryUSubscriptionClient(transport, rpcClient, notifier);
        assertNotNull(subscriber);

        FetchSubscriptionsRequest request = FetchSubscriptionsRequest.newBuilder().setTopic(topic).build();

        assertDoesNotThrow(() -> {
            assertTrue(FetchSubscriptionsResponse.getDefaultInstance().equals(
                subscriber.fetchSubscriptions(request).toCompletableFuture().get()));
        });

        verify(notifier, times(1)).registerNotificationListener(any(), any());
    }
}
