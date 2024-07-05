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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.eclipse.uprotocol.transport.builder.UMessageBuilder;
import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UMessage;
import org.eclipse.uprotocol.v1.UStatus;
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

        CompletionStage<UStatus> result = transport.send(UMessageBuilder.publish(uri).build());
        assertFalse(result.toCompletableFuture().isCompletedExceptionally());
        assertEquals(result.toCompletableFuture().join().getCode(), UCode.OK);
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
        assertEquals(transport.send(null).toCompletableFuture().join().getCode(), UCode.INTERNAL);
    }

    @Test
    @DisplayName("Test unhappy path register listener")
    public void test_unhappy_register_listener() {
        UTransport transport = new SadUTransport();
        CompletionStage<UStatus> result = transport.registerListener(UUri.getDefaultInstance(), 
            new MyListener());

        assertEquals(result.toCompletableFuture().join().getCode(), UCode.INTERNAL);
    }

    @Test
    @DisplayName("Test unhappy path unregister listener")
    public void test_unhappy_register_unlistener() {
        UTransport transport = new SadUTransport();
        
        CompletionStage<UStatus> result = transport.unregisterListener(UUri.getDefaultInstance(), 
            new MyListener());
        
        assertEquals(result.toCompletableFuture().join().getCode(), UCode.INTERNAL);
    }

    class MyListener implements UListener {
        @Override
        public void onReceive(UMessage message) {}
    }

    private class HappyUTransport implements UTransport {
        @Override
        public CompletionStage<UStatus> send(UMessage message) {
            return CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build());
        }


        @Override
        public CompletionStage<UStatus> registerListener(UUri source, UUri sink, UListener listener) {
            listener.onReceive(null);
            return CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build());
        }

        @Override
        public CompletionStage<UStatus> unregisterListener(UUri source, UUri sink, UListener listener) {
            return CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.OK).build());
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
        public CompletionStage<UStatus> send(UMessage message) {
            return CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.INTERNAL).build());
        }

        @Override
        public CompletionStage<UStatus> registerListener(UUri source, UUri sink, UListener listener) {
            listener.onReceive(null);
            return CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.INTERNAL).build());
        }

        @Override
        public CompletionStage<UStatus> unregisterListener(UUri source, UUri sink, UListener listener) {
            return CompletableFuture.completedFuture(UStatus.newBuilder().setCode(UCode.INTERNAL).build());
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
        CompletionStage<UStatus> result = transport.registerListener(UUri.getDefaultInstance(), new MyListener());
        assertEquals(result.toCompletableFuture().join().getCode(), UCode.OK);
    }

    @Test
    @DisplayName("Test happy path unregisterlistener with source filter only")
    public void test_happy_unregister_listener_source_filter() {
        UTransport transport = new HappyUTransport();
        CompletionStage<UStatus> result = transport.unregisterListener(UUri.getDefaultInstance(), new MyListener());
        assertEquals(result.toCompletableFuture().join().getCode(), UCode.OK);
    }
}
