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
package org.eclipse.uprotocol.uri.builder;

import java.util.Objects;

import org.eclipse.uprotocol.v1.UResource;

import com.google.protobuf.ProtocolMessageEnum;

public interface UResourceBuilder {

    static final int MAX_RPC_ID = 1000;

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
        
        return (id < MAX_RPC_ID) ? forRpcRequest(id) : UResource.newBuilder().setId(id).build();
    }


    /**
     * Build a UResource from a protobuf message. This method will determine if
     * the message is a RPC or topic message based on the message type
     * @param message The protobuf message.
     * @return Returns a UResource for an RPC request.
     */
    static UResource fromProto(ProtocolMessageEnum instance) {
        UResource resource = UResource.newBuilder()
            .setName(instance.getDescriptorForType().getContainingType().getName())
            .setInstance(instance.getValueDescriptor().getName())
            .setId(instance.getNumber())
            .build();
            return resource;
    }

}
