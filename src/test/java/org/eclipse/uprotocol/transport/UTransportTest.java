/*
 * Copyright (c) 2024 General Motors GTO LLC
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 * SPDX-FileType: SOURCE
 * SPDX-FileCopyrightText: 2024 General Motors GTO LLC
 * SPDX-License-Identifier: Apache-2.0
*/

package org.eclipse.uprotocol.transport;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UMessage;
import org.eclipse.uprotocol.v1.UMessageType;
import org.eclipse.uprotocol.v1.UStatus;
import org.eclipse.uprotocol.v1.UUri;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test implementing and using uTransport API
 */
public class UTransportTest {
    @Test
    @DisplayName("Test happy path send message parts")
    public void test_happy_send_message_parts() {
        UTransport transport = new HappyUTransport();

        UStatus status = transport.send(UMessage.getDefaultInstance());

        assertEquals(status.getCode(), UCode.OK);
    }

    @Test
    @DisplayName("Test happy path send message")
    public void test_happy_send_message() {
        UTransport transport = new HappyUTransport();
        UStatus status = transport.send(UMessage.getDefaultInstance());
        assertEquals(status.getCode(), UCode.OK);
    }

    @Test
    @DisplayName("Test happy path register listener")
    public void test_happy_register_listener() {
        UTransport transport = new HappyUTransport();
        UStatus status = transport.registerListener(UUri.getDefaultInstance(), new MyListener(), UMessageType.UMESSAGE_TYPE_UNSPECIFIED);
        assertEquals(status.getCode(), UCode.OK);
    }

    @Test
    @DisplayName("Test happy path unregister listener")
    public void test_happy_register_unlistener() {
        UTransport transport = new HappyUTransport();
        UStatus status = transport.unregisterListener(UUri.getDefaultInstance(), new MyListener(), UMessageType.UMESSAGE_TYPE_UNSPECIFIED);
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

        UStatus status = transport.send(UMessage.getDefaultInstance());

        assertEquals(status.getCode(), UCode.INTERNAL);
    }

    @Test
    @DisplayName("Test unhappy path send message")
    public void test_unhappy_send_message() {
        UTransport transport = new SadUTransport();
        UStatus status = transport.send(UMessage.getDefaultInstance());
        assertEquals(status.getCode(), UCode.INTERNAL);
    }

    @Test
    @DisplayName("Test unhappy path register listener")
    public void test_unhappy_register_listener() {
        UTransport transport = new SadUTransport();
        UStatus status = transport.registerListener(UUri.getDefaultInstance(), new MyListener(), UMessageType.UMESSAGE_TYPE_UNSPECIFIED);
        assertEquals(status.getCode(), UCode.INTERNAL);
    }

    @Test
    @DisplayName("Test unhappy path unregister listener")
    public void test_unhappy_register_unlistener() {
        UTransport transport = new SadUTransport();
        UStatus status = transport.unregisterListener(UUri.getDefaultInstance(), new MyListener(), UMessageType.UMESSAGE_TYPE_UNSPECIFIED);
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
        public UStatus registerListener(UUri topic, UListener listener, UMessageType messageType) {
            listener.onReceive(UMessage.getDefaultInstance());
            return UStatus.newBuilder().setCode(UCode.OK).build();
        }

        @Override
        public UStatus unregisterListener(UUri topic, UListener listener, UMessageType messageType) {
            return UStatus.newBuilder().setCode(UCode.OK).build();
        }
    }

    private class SadUTransport implements UTransport {
        @Override
        public UStatus send(UMessage message) {
            return UStatus.newBuilder().setCode(UCode.INTERNAL).build();
        }

        @Override
        public UStatus registerListener(UUri topic, UListener listener, UMessageType messageType) {
            listener.onReceive(null);
            return UStatus.newBuilder().setCode(UCode.INTERNAL).build();
        }

        @Override
        public UStatus unregisterListener(UUri topic, UListener listener, UMessageType messageType) {
            return UStatus.newBuilder().setCode(UCode.INTERNAL).build();
        }

    }
}
