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
package org.eclipse.uprotocol.transport;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.eclipse.uprotocol.communication.UStatusException;
import org.eclipse.uprotocol.transport.builder.UMessageBuilder;
import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UMessage;
import org.eclipse.uprotocol.v1.UUri;

/**
 * Test implementing and using uTransport API
 */
public class UTransportTest {
    @Test
    @DisplayName("Test happy path send message")
    public void test_happy_send_message() {
        UTransport transport = new HappyUTransport();
        UUri uri = UUri.newBuilder().setUeId(1).setUeVersionMajor(1).setResourceId(0x8000).build();

        CompletionStage<Void> result = transport.send(UMessageBuilder.publish(uri).build());
        assertFalse(result.toCompletableFuture().isCompletedExceptionally());
    }

    @Test
    @DisplayName("Test happy path register listener")
    public void test_happy_register_listener() {
        UTransport transport = new HappyUTransport();
        assertFalse(transport.registerListener(UUri.getDefaultInstance(), 
        new MyListener()).toCompletableFuture().isCompletedExceptionally());
    }

    @Test
    @DisplayName("Test happy path unregister listener")
    public void test_happy_register_unlistener() {
        UTransport transport = new HappyUTransport();
        assertFalse(transport.unregisterListener(UUri.getDefaultInstance(), 
        new MyListener()).toCompletableFuture().isCompletedExceptionally());
    }

    @Test
    @DisplayName("Test unhappy path send message")
    public void test_unhappy_send_message() {
        UTransport transport = new SadUTransport();
        
        CompletionStage<Void> result = transport.send(null); 

        assertTrue(result.toCompletableFuture().isCompletedExceptionally());
        result.exceptionally(e -> {
            assertTrue(e instanceof UStatusException);
            assertEquals(((UStatusException) e).getCode(), UCode.INTERNAL);
            return null;
        });
    }

    @Test
    @DisplayName("Test unhappy path register listener")
    public void test_unhappy_register_listener() {
        UTransport transport = new SadUTransport();
        CompletionStage<Void> result = transport.registerListener(UUri.getDefaultInstance(), 
            new MyListener());

        assertTrue(result.toCompletableFuture().isCompletedExceptionally());
        result.exceptionally(e -> {
            assertTrue(e instanceof UStatusException);
            assertEquals(((UStatusException) e).getCode(), UCode.INTERNAL);
            return null;
        });
    }

    @Test
    @DisplayName("Test unhappy path unregister listener")
    public void test_unhappy_register_unlistener() {
        UTransport transport = new SadUTransport();
        
        CompletionStage<Void> result = transport.unregisterListener(UUri.getDefaultInstance(), 
            new MyListener());
        assertTrue(result.toCompletableFuture().isCompletedExceptionally());
        result.exceptionally(e -> {
            assertTrue(e instanceof UStatusException);
            assertEquals(((UStatusException) e).getCode(), UCode.INTERNAL);
            return null;
        });
    }

    final class MyListener implements UListener {
        @Override
        public void onReceive(UMessage message) {}
    }

    private class HappyUTransport implements UTransport {
        @Override
        public CompletionStage<Void> send(UMessage message) {
            return CompletableFuture.completedFuture(null);
        }


        @Override
        public CompletionStage<Void> registerListener(UUri source, UUri sink, UListener listener) {
            listener.onReceive(null);
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletionStage<Void> unregisterListener(UUri source, UUri sink, UListener listener) {
            return CompletableFuture.completedStage(null);
        }


        @Override
        public UUri getSource() {
            return UUri.getDefaultInstance();
        }

        @Override
        public void close() {
        }
    }

    private class SadUTransport implements UTransport {
        @Override
        public CompletionStage<Void> send(UMessage message) {
            return CompletableFuture.failedFuture(new UStatusException(UCode.INTERNAL, ""));
        }

        @Override
        public CompletionStage<Void> registerListener(UUri source, UUri sink, UListener listener) {
            listener.onReceive(null);
            return CompletableFuture.failedFuture(new UStatusException(UCode.INTERNAL, ""));
        }

        @Override
        public CompletionStage<Void> unregisterListener(UUri source, UUri sink, UListener listener) {
            return CompletableFuture.failedFuture(new UStatusException(UCode.INTERNAL, ""));
        }

        @Override
        public UUri getSource() {
            return UUri.getDefaultInstance();
        }
        
        @Override
        public void close() {
        }
    }

    @Test
    @DisplayName("Test happy path registerlistener with source filter only")
    public void test_happy_register_listener_source_filter() {
        UTransport transport = new HappyUTransport();
        transport.registerListener(UUri.getDefaultInstance(), 
            new MyListener()).toCompletableFuture().join();
    }

    @Test
    @DisplayName("Test happy path unregisterlistener with source filter only")
    public void test_happy_unregister_listener_source_filter() {
        UTransport transport = new HappyUTransport();
        transport.unregisterListener(UUri.getDefaultInstance(), 
            new MyListener()).toCompletableFuture().join();
    }
}
