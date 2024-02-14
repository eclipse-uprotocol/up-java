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

import org.eclipse.uprotocol.transport.UListener;
import org.eclipse.uprotocol.v1.UEntity;
import org.eclipse.uprotocol.v1.UMessage;
import org.eclipse.uprotocol.v1.UUri;


public class UStreamer {
    private Route ingress;
    private Route[] egress;

    UStreamer(Route ingress, Route[] egress) {
        this.ingress = ingress;
        this.egress = egress;

    }

    public void start() {
        if (ingress == null || egress == null || egress.length == 0) {
            return;
        }

        UUri uri = UUri.newBuilder()
        .setAuthority(ingress.getAuthority())
        .setEntity(UEntity.newBuilder().setName("*"))
        .build();
        ingress.getTransport().registerListener(uri, new IngressListener());

        for (Route route : egress) {
            UUri egressUri = UUri.newBuilder()
                    .setAuthority(route.getAuthority())
                    .setEntity(UEntity.newBuilder().setName("*"))
                    .build();
            route.getTransport().registerListener(egressUri, new EgressListener());
        }
    }

    
    public void stop() {
    }


    /**
     * Listener for ingress messages. This listener will receive egress
     * messages and forward to the appropriate egress transports
     */
    private class IngressListener implements UListener {

        @Override
        public void onReceive(UMessage message) {
            System.out.println("IngressListener received message" + message.toString());
            for (Route route : egress) {
                if (route.getAuthority().equals(message.getAttributes().getSink().getAuthority())) {
                    route.getTransport().send(message);
                }
            }
            
        }
    }

    /**
     * Listener for ingress messages from egress transport.
     */
    private class EgressListener implements UListener {

        @Override
        public void onReceive(UMessage message) {
            System.out.println("EgressListener received message" + message.toString());
            ingress.getTransport().send(message);
        }
    }

}
