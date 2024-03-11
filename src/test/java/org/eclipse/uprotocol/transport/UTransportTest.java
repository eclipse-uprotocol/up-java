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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.uprotocol.transport.builder.UAttributesBuilder;
import org.eclipse.uprotocol.uri.factory.UResourceBuilder;
import org.eclipse.uprotocol.v1.UAttributes;
import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UEntity;
import org.eclipse.uprotocol.v1.UMessage;
import org.eclipse.uprotocol.v1.UMessageType;
import org.eclipse.uprotocol.v1.UPriority;
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

    @Test
    @DisplayName("Test passing different type of UListeners")
    public void test_passing_different_type_of_UListeners() {
        UTransport transport = new HappyUTransport();
        UStatus status = transport.registerListener(UUri.getDefaultInstance(), new MyPublishListener());
        assertEquals(status.getCode(), UCode.OK);
    }

    final class MyListener implements UListener {
        @Override
        public void onReceive(UMessage message) {}
    }


    final class MyPublishListener implements PublishUListener {
        @Override
        public void onReceive(UMessage message) {
            assertEquals(message.getAttributes().getType(), UMessageType.UMESSAGE_TYPE_PUBLISH);
            assertFalse(message.getAttributes().hasSink());
        }
    }

    final class MyNotificationListener implements NotificationUListener {
        @Override
        public void onReceive(UMessage message) {
            assertEquals(message.getAttributes().getType(), UMessageType.UMESSAGE_TYPE_PUBLISH);
            assertTrue(message.getAttributes().hasSink());
        }
    }

    final class MyRequestListener implements RequestUListener {
        @Override
        public void onReceive(UMessage message) {
            assertEquals(message.getAttributes().getType(), UMessageType.UMESSAGE_TYPE_REQUEST);
            assertTrue(message.getAttributes().hasSink());
        }
    }
    final class MyResponseListener implements ResponseUListener {
        @Override
        public void onReceive(UMessage message) {
            assertEquals(message.getAttributes().getType(), UMessageType.UMESSAGE_TYPE_RESPONSE);
            assertTrue(message.getAttributes().hasSink());
        }
    }

    private class HappyUTransport implements UTransport {
        @Override
        public UStatus send(UMessage message) {
            return UStatus.newBuilder().setCode((message == null) ? UCode.INVALID_ARGUMENT : UCode.OK).build();
        }


        @Override
        public UStatus registerListener(UUri topic, UListener listener) {

            if (listener instanceof PublishUListener) {
                listener.onReceive(publishMessage());
                return UStatus.newBuilder().setCode(UCode.OK).build();
            }
            else if (listener instanceof NotificationUListener) {
                listener.onReceive(notificationMessage());
                return UStatus.newBuilder().setCode(UCode.OK).build();
            }
            else if (listener instanceof RequestUListener) {
                listener.onReceive(requestMessage());
                return UStatus.newBuilder().setCode(UCode.OK).build();
            }
            else if (listener instanceof ResponseUListener) {
                listener.onReceive(responseMessage());
                return UStatus.newBuilder().setCode(UCode.OK).build();
            }
            listener.onReceive(UMessage.getDefaultInstance());
            return UStatus.newBuilder().setCode(UCode.OK).build();
        }

        @Override
        public UStatus unregisterListener(UUri topic, UListener listener) {
            return UStatus.newBuilder().setCode(UCode.OK).build();
        }
    }

    private class SadUTransport implements UTransport {
        @Override
        public UStatus send(UMessage message) {
            return UStatus.newBuilder().setCode(UCode.INTERNAL).build();
        }

        @Override
        public UStatus registerListener(UUri topic, UListener listener) {
            listener.onReceive(null);
            return UStatus.newBuilder().setCode(UCode.INTERNAL).build();
        }

        @Override
        public UStatus unregisterListener(UUri topic, UListener listener) {
            return UStatus.newBuilder().setCode(UCode.INTERNAL).build();
        }

    }

    private UMessage publishMessage() {
        UUri source = UUri.newBuilder()
                .setEntity(UEntity.newBuilder().setName("hartley_app").setVersionMajor(1))
                .setResource(UResourceBuilder.fromId(8000)).build();
        UAttributes attributes = UAttributesBuilder.publish(source, UPriority.UPRIORITY_CS1).build();
        return UMessage.newBuilder().setAttributes(attributes).build();
    }

    private UMessage notificationMessage() {
        UUri source = UUri.newBuilder()
                .setEntity(UEntity.newBuilder().setName("hartley_service").setVersionMajor(1))
                .setResource(UResourceBuilder.fromId(8001)).build();
        UUri sink = UUri.newBuilder()
                .setEntity(UEntity.newBuilder().setName("hartley_app").setVersionMajor(1))
                .setResource(UResourceBuilder.fromId(8000)).build();
        UAttributes attributes = UAttributesBuilder.notification(source, sink, UPriority.UPRIORITY_CS1).build();
        return UMessage.newBuilder().setAttributes(attributes).build();
    }

    private UMessage requestMessage() {
        UUri source = UUri.newBuilder()
                .setEntity(UEntity.newBuilder().setName("hartley_app").setVersionMajor(1))
                .setResource(UResourceBuilder.forRpcResponse()).build();
        UUri sink = UUri.newBuilder()
                .setEntity(UEntity.newBuilder().setName("hartley_service").setVersionMajor(1))
                .setResource(UResourceBuilder.forRpcRequest(1)).build();
        UAttributes attributes = UAttributesBuilder.request(source, sink, UPriority.UPRIORITY_CS1, 1000).build();
        return UMessage.newBuilder().setAttributes(attributes).build();
    }

    private UMessage responseMessage() {
        UAttributes attributes = UAttributesBuilder.response(requestMessage().getAttributes()).build();
        return UMessage.newBuilder().setAttributes(attributes).build();
    }
}
