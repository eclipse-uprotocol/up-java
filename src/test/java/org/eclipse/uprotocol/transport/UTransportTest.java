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

import static org.junit.jupiter.api.Assertions.assertEquals;
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

        UStatus status = transport.send(UMessageBuilder.publish(uri).build());
        assertEquals(status.getCode(), UCode.OK);
    }

    @Test
    @DisplayName("Test happy path register listener")
    public void test_happy_register_listener() {
        UTransport transport = new HappyUTransport();
        UStatus status = transport.registerListener(UUri.getDefaultInstance(), new MyListener());
        assertEquals(status.getCode(), UCode.OK);
    }

    @Test
    @DisplayName("Test happy path unregister listener")
    public void test_happy_register_unlistener() {
        UTransport transport = new HappyUTransport();
        UStatus status = transport.unregisterListener(UUri.getDefaultInstance(), new MyListener());
        assertEquals(status.getCode(), UCode.OK);
    }

    @Test
    @DisplayName("Test sending null message")
    public void test_sending_null_message() {
        UTransport transport = new HappyUTransport();
        UStatus status = transport.send(null);
        assertEquals(status.getCode(), UCode.INVALID_ARGUMENT);
    }


    @Test
    @DisplayName("Test unhappy path send message parts")
    public void test_unhappy_send_message_parts() {
        UTransport transport = new SadUTransport();

        UStatus status = transport.send(null);

        assertEquals(status.getCode(), UCode.INTERNAL);
    }

    @Test
    @DisplayName("Test unhappy path send message")
    public void test_unhappy_send_message() {
        UTransport transport = new SadUTransport();
        UStatus status = transport.send(null);
        assertEquals(status.getCode(), UCode.INTERNAL);
    }

    @Test
    @DisplayName("Test unhappy path register listener")
    public void test_unhappy_register_listener() {
        UTransport transport = new SadUTransport();
        UStatus status = transport.registerListener(UUri.getDefaultInstance(), new MyListener());
        assertEquals(status.getCode(), UCode.INTERNAL);
    }

    @Test
    @DisplayName("Test unhappy path unregister listener")
    public void test_unhappy_register_unlistener() {
        UTransport transport = new SadUTransport();
        UStatus status = transport.unregisterListener(UUri.getDefaultInstance(), new MyListener());
        assertEquals(status.getCode(), UCode.INTERNAL);
    }

    final class MyListener implements UListener {
        @Override
        public void onReceive(UMessage message) {}
    }

    private class HappyUTransport implements UTransport {
        @Override
        public UStatus send(UMessage message) {
            return UStatus.newBuilder().setCode((message == null) ? UCode.INVALID_ARGUMENT : UCode.OK).build();
        }


        @Override
        public UStatus registerListener(UUri source, UUri sink, UListener listener) {
            listener.onReceive(null);
            return UStatus.newBuilder().setCode(UCode.OK).build();
        }

        @Override
        public UStatus unregisterListener(UUri source, UUri sink, UListener listener) {
            return UStatus.newBuilder().setCode(UCode.OK).build();
        }


        @Override
        public UUri getSource() {
            return UUri.getDefaultInstance();
        }
    }

    private class SadUTransport implements UTransport {
        @Override
        public UStatus send(UMessage message) {
            return UStatus.newBuilder().setCode(UCode.INTERNAL).build();
        }

        @Override
        public UStatus registerListener(UUri source, UUri sink, UListener listener) {
            listener.onReceive(null);
            return UStatus.newBuilder().setCode(UCode.INTERNAL).build();
        }

        @Override
        public UStatus unregisterListener(UUri source, UUri sink, UListener listener) {
            return UStatus.newBuilder().setCode(UCode.INTERNAL).build();
        }

        @Override
        public UUri getSource() {
            return UUri.getDefaultInstance();
        }
    }

    @Test
    @DisplayName("Test happy path registerlistener with source filter only")
    public void test_happy_register_listener_source_filter() {
        UTransport transport = new HappyUTransport();
        UStatus status = transport.registerListener(UUri.getDefaultInstance(), new MyListener());
        assertEquals(status.getCode(), UCode.OK);
    }

    @Test
    @DisplayName("Test happy path unregisterlistener with source filter only")
    public void test_happy_unregister_listener_source_filter() {
        UTransport transport = new HappyUTransport();
        UStatus status = transport.unregisterListener(UUri.getDefaultInstance(), new MyListener());
        assertEquals(status.getCode(), UCode.OK);
    }
}
