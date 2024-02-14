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
package org.eclipse.uprotocol.streamer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.uprotocol.transport.UListener;
import org.eclipse.uprotocol.transport.UTransport;
import org.eclipse.uprotocol.transport.builder.UAttributesBuilder;
import org.eclipse.uprotocol.uri.factory.UResourceBuilder;
import org.eclipse.uprotocol.v1.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class UStreamerTest {
    @Test
    @DisplayName("Initializing the Streamer")
    public void test_streamer_initialization() {
        UTransport ingressTransport = new IngressTransport();
        
        Route ingress = new Route(
            UAuthority.newBuilder().setName("Ingress").build(),
            ingressTransport);
        
        Route[] egress = {
            new Route (
                UAuthority.newBuilder().setName("Egress").build(),
                new EgressTransport()
        )};

        UStreamer streamer = new UStreamer(ingress, egress);
        System.out.println("Streamer initialized");
        streamer.start();

        System.out.println("Sending a message");
        ingressTransport.send(createMessage());
        /* Send a message and see if flow through */
        System.out.println("Message sent");
        streamer.stop();
    }
    

    private UMessage createMessage() {
        final UUri source = UUri.newBuilder()
                .setAuthority(UAuthority.newBuilder().setName("Ingress"))
                .setEntity(UEntity.newBuilder().setName("hartley_app").setVersionMajor(1))
                .setResource(UResourceBuilder.forRpcResponse()).build();
        
        final UUri sink = UUri.newBuilder().setAuthority(UAuthority.newBuilder().setName("Egress"))
            .setEntity(UEntity.newBuilder().setName("hr_service").setVersionMajor(1))
            .setResource(UResourceBuilder.forRpcRequest("Raise")).build();
 
        final UAttributes attributes = UAttributesBuilder.request(source, sink, UPriority.UPRIORITY_CS4, 1000).build();
        return UMessage.newBuilder()
            .setAttributes(attributes)
            .setPayload(UPayload.getDefaultInstance())
            .build();
    }

    
    private class IngressTransport implements UTransport {
        private UListener listener;

        @Override
        public UStatus send(UMessage message) {
            if (listener != null) {
                listener.onReceive(message);
            }
            return UStatus.newBuilder().setCode(UCode.OK).build();
        }

        @Override
        public UStatus registerListener(UUri topic, UListener listener) {
            this.listener = listener;
            return UStatus.newBuilder().setCode(UCode.OK).build();
        }

        @Override
        public UStatus unregisterListener(UUri topic, UListener listener) {
            return UStatus.newBuilder().setCode(UCode.OK).build();
        }
    }


    private class EgressTransport implements UTransport {

        @Override
        public UStatus send(UMessage message) {
            return UStatus.newBuilder().setCode(UCode.OK).build();
        }

        @Override
        public UStatus registerListener(UUri topic, UListener listener) {
            return UStatus.newBuilder().setCode(UCode.OK).build();
        }

        @Override
        public UStatus unregisterListener(UUri topic, UListener listener) {
            return UStatus.newBuilder().setCode(UCode.OK).build();
        }
    }

}
