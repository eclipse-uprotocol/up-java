/*
 * Copyright (c) 2023 General Motors GTO LLC
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
 * SPDX-FileType: SOURCE
 * SPDX-FileCopyrightText: 2023 General Motors GTO LLC
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.uprotocol.transport.builder;


import org.eclipse.uprotocol.v1.*;
import org.eclipse.uprotocol.uuid.factory.UuidFactory;

import java.util.Objects;

/**
 * Builder for easy construction of the UAttributes object.
 */
public class UAttributesBuilder {

    private final UUri source;
    private final UUID id;
    private final UMessageType type;
    private final UPriority priority;
    private Integer ttl;
    private String token;
    private UUri sink;
    private Integer plevel;
    private Integer commstatus;
    private UUID reqid;


    /**
     * Construct a UAttributesBuilder for a publish message.
     * @param source   Source address of the message.
     * @param priority The priority of the message.
     * @return Returns the UAttributesBuilder with the configured priority.
     */
    public static UAttributesBuilder publish(UUri source, UPriority priority) {
        Objects.requireNonNull(source, "source cannot be null.");
        Objects.requireNonNull(priority, "UPriority cannot be null.");
        return new UAttributesBuilder(source, UuidFactory.Factories.UPROTOCOL.factory().create(), 
        UMessageType.UMESSAGE_TYPE_PUBLISH, priority);
    }


    /**
     * Construct a UAttributesBuilder for a notification message.
     * @param source   Source address of the message.
     * @param priority The priority of the message.
     * @param sink The destination URI.
     * @return Returns the UAttributesBuilder with the configured priority and sink.
     */
    public static UAttributesBuilder notification(UUri source, UPriority priority, UUri sink) {
        Objects.requireNonNull(source, "source cannot be null.");
        Objects.requireNonNull(priority, "UPriority cannot be null.");
        Objects.requireNonNull(sink, "sink cannot be null.");

        return new UAttributesBuilder(source, UuidFactory.Factories.UPROTOCOL.factory().create(), 
        UMessageType.UMESSAGE_TYPE_PUBLISH, priority).withSink(sink);
    }
    

    /**
     * Construct a UAttributesBuilder for a request message.
     * @param source   Source address of the message.
     * @param priority The priority of the message.
     * @param sink The destination URI.
     * @param ttl The time to live in milliseconds.
     * @return Returns the UAttributesBuilder with the configured priority, sink and ttl.
     */
    public static UAttributesBuilder request(UUri source, UPriority priority, UUri sink, Integer ttl) {
        Objects.requireNonNull(source, "source cannot be null.");
        Objects.requireNonNull(priority, "UPriority cannot be null.");
        Objects.requireNonNull(ttl, "ttl cannot be null.");
        Objects.requireNonNull(sink, "sink cannot be null.");
        
        return new UAttributesBuilder(source, UuidFactory.Factories.UPROTOCOL.factory().create(), 
        UMessageType.UMESSAGE_TYPE_REQUEST, priority).withTtl(ttl).withSink(sink);
    }


    /**
     * Construct a UAttributesBuilder for a response message.
     * @param source   Source address of the message.
     * @param priority The priority of the message.
     * @param sink The destination URI.
     * @param reqid The original request UUID used to correlate the response to the request.
     * @return Returns the UAttributesBuilder with the configured priority, sink and reqid.
     */
    public static UAttributesBuilder response(UUri source, UPriority priority, UUri sink, UUID reqid) {
        Objects.requireNonNull(source, "source cannot be null.");
        Objects.requireNonNull(priority, "UPriority cannot be null.");
        Objects.requireNonNull(sink, "sink cannot be null.");
        Objects.requireNonNull(reqid, "reqid cannot be null.");
        
        return new UAttributesBuilder(source, UuidFactory.Factories.UPROTOCOL.factory().create(), 
        UMessageType.UMESSAGE_TYPE_RESPONSE, priority).withSink(sink).withReqId(reqid);
    }


    /**
     * Construct the UAttributesBuilder with the configurations that are required for every payload transport.
     *
     * @param source   Source address of the message.
     * @param id       Unique identifier for the message.
     * @param type     Message type such as Publish a state change, RPC request or RPC response.
     * @param priority uProtocol Prioritization classifications.
     */
    private UAttributesBuilder(UUri source, UUID id, UMessageType type, UPriority priority) {
        this.source = source;
        this.id = id;
        this.type = type;
        this.priority = priority;
    }

    /**
     * Add the time to live in milliseconds.
     *
     * @param ttl the time to live in milliseconds.
     * @return Returns the UAttributesBuilder with the configured ttl.
     */
    public UAttributesBuilder withTtl(Integer ttl) {
        this.ttl = ttl;
        return this;
    }


    /**
     * Add the authorization token used for TAP.
     *
     * @param token the authorization token used for TAP.
     * @return Returns the UAttributesBuilder with the configured token.
     */
    public UAttributesBuilder withToken(String token) {
        this.token = token;
        return this;
    }


    /**
     * Add the explicit destination URI.
     *
     * @param sink the explicit destination URI.
     * @return Returns the UAttributesBuilder with the configured sink.
     */
    public UAttributesBuilder withSink(UUri sink) {
        this.sink = sink;
        return this;
    }


    /**
     * Add the permission level of the message.
     *
     * @param plevel the permission level of the message.
     * @return Returns the UAttributesBuilder with the configured plevel.
     */
    public UAttributesBuilder withPermissionLevel(Integer plevel) {
        this.plevel = plevel;
        return this;
    }


    /**
     * Add the communication status of the message.
     *
     * @param commstatus the communication status of the message.
     * @return Returns the UAttributesBuilder with the configured commstatus.
     */
    public UAttributesBuilder withCommStatus(Integer commstatus) {
        this.commstatus = commstatus;
        return this;
    }


    /**
     * Add the request ID.
     *
     * @param reqid the request ID.
     * @return Returns the UAttributesBuilder with the configured reqid.
     */
    public UAttributesBuilder withReqId(UUID reqid) {
        this.reqid = reqid;
        return this;
    }

    
    /**
     * Construct the UAttributes from the builder.
     *
     * @return Returns a constructed
     */
    public UAttributes build() {
        UAttributes.Builder attributesBuilder = UAttributes.newBuilder()
                .setSource(source)
                .setId(id)
                .setType(type)
                .setPriority(priority);

        if(sink!=null){
            attributesBuilder.setSink(sink);
        }

        if(ttl!=null){
            attributesBuilder.setTtl(ttl);
        }
        if(plevel!=null){
            attributesBuilder.setPermissionLevel(plevel);
        }
        if(commstatus!=null){
            attributesBuilder.setCommstatus(commstatus);
        }
        if(reqid!=null){
            attributesBuilder.setReqid(reqid);
        }
        if(token!=null){
            attributesBuilder.setToken(token);
        }
        return attributesBuilder.build();
    }
}
