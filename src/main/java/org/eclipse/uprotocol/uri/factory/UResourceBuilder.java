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
package org.eclipse.uprotocol.uri.factory;

import java.util.Objects;
import org.eclipse.uprotocol.UServiceTopic;
import org.eclipse.uprotocol.UprotocolOptions;
import org.eclipse.uprotocol.v1.UResource;

import com.google.protobuf.DescriptorProtos.ServiceOptions;
import com.google.protobuf.Descriptors.ServiceDescriptor;

public interface UResourceBuilder {

    /**
     * The minimum topic ID, below this value are methods.
     */
    static final int MIN_TOPIC_ID = 0x8000;

    /**
     * Builds a UResource for an RPC response.
     * @return Returns a UResource for an RPC response.
     */
    static UResource forRpcResponse() {
        return UResource.newBuilder()
            .setName("rpc")
            .setInstance("response")
            .setId(0)
            .build();
    }


    /**
     * Builds a UResource for an RPC request.
     * @param method The method to be invoked.
     * @return Returns a UResource for an RPC request.
     */
    static UResource forRpcRequest(String method) {
        return forRpcRequest(method, null);
    }

    /**
     * Builds a UResource for an RPC request with an ID and method name
     * @param method The method to be invoked.
     * @param id The ID of the request.
     * @return Returns a UResource for an RPC request.
     */
    static UResource forRpcRequest(String method, Integer id) {
        UResource.Builder builder = UResource.newBuilder().setName("rpc");
        
        if (method != null) {
            builder.setInstance(method);
        }
        if (id != null) {
            builder.setId(id);
        }

        return builder.build();
    }

    /**
     * Builds a UResource for an RPC request with an ID
     * @param id The ID of the request.
     * @return Returns a UResource for an RPC request.
     */
    static UResource forRpcRequest(Integer id) {
        return forRpcRequest(null, id);

    }

    /**
     * Build a UResource from an ID. This method will determine if
     * the id is a RPC or topic ID based on the range
     * @param id The ID of the request.
     * @return Returns a UResource for an RPC request.
     */
    static UResource fromId(Integer id) {
        Objects.requireNonNull(id, "id cannot be null");
        
        return (id == 0) ? forRpcResponse() : (id < MIN_TOPIC_ID) ? forRpcRequest(id) : UResource.newBuilder().setId(id).build();
    }


    /**
     * Build a UResource from a UServiceTopic that is defined in protos and 
     * available from generated stubs.
     * @param topic The UServiceTopic to build the UResource from.
     * @return Returns a UResource for an RPC request.
     */
    static UResource fromUServiceTopic(UServiceTopic topic) {
        Objects.requireNonNull(topic, "topic cannot be null");
        String[] nameAndInstanceParts = topic.getName().split("\\.");
        String resourceName = nameAndInstanceParts[0];
        String resourceInstance = nameAndInstanceParts.length > 1 ? nameAndInstanceParts[1] : null;
        
        UResource.Builder builder = UResource.newBuilder()
            .setName(resourceName)
            .setId(topic.getId())
            .setMessage(topic.getMessage());
        
            if (resourceInstance != null) {
            builder.setInstance(resourceInstance);
        }

        return builder.build();
    }


    /**
     * Build a UResource manually from a protobuf message. This method will determine if
     * the message is a RPC or topic message based on the message type
     * @param message The protobuf message.
     * @return Returns a UResource for an RPC request.
     */
    static UResource fromProto(ServiceDescriptor descriptor, String topicName) {
        ServiceOptions options = descriptor.getOptions();

        return options.getExtension(UprotocolOptions.notificationTopic)
                .stream()
                .filter(p -> p.getName().equals(topicName))
                .findFirst()
                .map(p -> fromUServiceTopic(p))
                .orElse(UResource.newBuilder().build());
        
    }

}
