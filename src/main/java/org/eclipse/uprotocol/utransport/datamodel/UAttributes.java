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
 */

package org.eclipse.uprotocol.utransport.datamodel;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.uprotocol.uri.datamodel.UAuthority;
import org.eclipse.uprotocol.uri.datamodel.UEntity;
import org.eclipse.uprotocol.uri.datamodel.UResource;
import org.eclipse.uprotocol.uri.datamodel.UUri;

/**
 * When sending data over uTransport the basic API for send uses a source topic and the UPayload as the data.
 * Any other information about the message is placed in the UAttributes class.
 * The UAttributes class holds the additional information along with business methods for understanding more about the actual message sent.
 * {@link UAttributes} is the class that defines the Payload. It is the place for configuring time to live, priority, security tokens and more.
 * Each UAttributes class defines a different type of message payload. The payload can represent a simple published payload with some state change,
 * Payload representing an RPC request or Payload representing an RPC response.
 */
public class UAttributes {

    private static final UAttributes EMPTY = new UAttributes(null, null, null, null, null, null, null, null, null);

    // Required Attributes
    private final UUID id;                  // Unique identifier for the message
    private final UMessageType type;        // Message type
    private final UPriority priority;       // Message priority

    // Optional Attributes
    private final Integer ttl;              // Time to live in milliseconds
    private final String token;             // Authorization token used for TAP
    private final UUri sink;                // Explicit destination URI
    private final Integer plevel;           // Permission Level
    private final Integer commstatus;       // Communication Status
    private final UUID reqid;               // Request ID

    
    /**
     * Construct the transport UAttributes object.
     *
     * @param id                Unique identifier for the message. Required.
     * @param type              Message type such as Publish a state change, RPC request or RPC response. Required.
     * @param priority          Message priority. Required.
     * @param ttl               Time to live in milliseconds.
     * @param token             Authorization token used for TAP.
     * @param sink              Explicit destination URI, used in notifications and RPC messages.
     * @param plevel            Permission Level.
     * @param commstatus        Communication Status, used to indicate platform communication errors that occurred during delivery.
     * @param reqid             Request ID, used to indicate the id of the RPC request that matches this RPC response.
     */
    private UAttributes(UUID id, UMessageType type, UPriority priority, Integer ttl, String token,
                        UUri sink, Integer plevel, Integer commstatus, UUID reqid) {
        this.id = id;
        this.type = type;
        this.priority = priority;
        this.ttl = ttl;
        this.token = token;
        this.sink = sink;
        this.plevel = plevel;
        this.commstatus = commstatus;
        this.reqid = reqid;
    }

    private UAttributes(UAttributesBuilder builder) {
        this(builder.id, builder.type, builder.priority, builder.ttl, builder.token, builder.sink,
                builder.plevel, builder.commstatus, builder.reqid);
    }


    /**
     * Static factory method for creating an empty attributes object, to avoid working with null.
     * @return Returns an empty attributes that indicates that there are no added additional attributes to configure.
     * An empty UAttributes is not valid, in the same way null is not valid, this is because UAttributes has 3 required values - id, type and priority.
     */
    public static UAttributes empty() {
        return EMPTY;
    }

    /**
     * Static factory method for creating a base UAttributes for an RPC request.
     * @param id id Unique identifier for the RPC request message.
     * @param sink UUri describing the exact RPC command.
     * @return Returns a base UAttributes that can be used to build an RPC request.
     */
    public static UAttributesBuilder forRpcRequest(UUID id, UUri sink) {
        return new UAttributesBuilder(id, UMessageType.REQUEST, UPriority.REALTIME_INTERACTIVE)
                .withSink(sink);
    }

    /**
     * Static factory method for creating a base UAttributes for an RPC request.
     * @param id Unique identifier for the RPC request message.
     * @param uAuthority Indicates where the software for RPC request is installed.
     * @param serviceUEntity Indicates what service we want to RPC request to change.
     * @param commandName String command name the RPC is executing.
     * @return Returns a base UAttributes that can be used to build an RPC request.
     */
    public static UAttributesBuilder forRpcRequest(UUID id, UAuthority uAuthority, UEntity serviceUEntity, String commandName) {
        return new UAttributesBuilder(id, UMessageType.REQUEST, UPriority.REALTIME_INTERACTIVE)
                .withSink(new UUri(uAuthority, serviceUEntity, UResource.forRpc(commandName)));
    }

    /**
     * Static factory method for creating a base UAttributes for an RPC response.
     * @param id Unique identifier for the RPC response message.
     * @param sink UUri describing where the response needs to go.
     * @param requestId The UUID of the message that this response is responding to.
     * @return Returns a base UAttributes that can be used to build an RPC response.
     */
    public static UAttributesBuilder forRpcResponse(UUID id, UUri sink, UUID requestId) {
        return new UAttributesBuilder(id, UMessageType.RESPONSE, UPriority.REALTIME_INTERACTIVE)
                .withSink(sink)
                .withReqId(requestId);
    }

    /**
     * Static factory method for creating a base UAttributes for an RPC response.
     * @param id Unique identifier for the RPC response message.
     * @param uAuthority Indicates where the software for RPC response is for is installed.
     * @param callerUEntity Indicates what service called the RPC request and this response is for.
     * @param requestId The id of the RPC request that this message is responding to.
     * @return Returns a base UAttributes that can be used to build an RPC response.
     */
    public static UAttributesBuilder forRpcResponse(UUID id, UAuthority uAuthority, UEntity callerUEntity, UUID requestId) {
        return new UAttributesBuilder(id, UMessageType.RESPONSE, UPriority.REALTIME_INTERACTIVE)
                .withSink(new UUri(uAuthority, callerUEntity, UResource.fromNameWithInstance("rpc", "response")))
                .withReqId(requestId);
    }

    /**
     * Unique identifier for the message.
     * @return Returns the unique identifier for the message.
     */
    public UUID id() {
        return id;
    }

    /**
     * Message type such as Publish a state change, RPC request or RPC response.
     * @return Returns the message type such as Publish a state change, RPC request or RPC response.
     */
    public UMessageType type() {
        return type;
    }

    /**
     * uProtocol Prioritization classifications. 
     * @return Returns the configured uProtocol Prioritization classifications.
     */
    public UPriority priority() {
        return priority;
    }

    /**
     * A time to live which is how long this event should live for after it was generated (in milliseconds).
     * Events without this attribute (or value is 0) MUST NOT timeout.
     * @return An Optional time to live which is how long this event should live for after it was generated (in milliseconds).
     */
    public Optional<Integer> ttl() {
        return ttl == null ? Optional.empty() : Optional.of(this.ttl);
    }

    /**
     * Oauth2 access token to perform the access request defined in the request message.
     * @return Returns an Optional token attribute.
     */
    public Optional<String> token() {
        return token == null || token.isBlank() ? Optional.empty() : Optional.of(token);
    }


    /**
     * An explicit destination URI, used in notifications and RPC messages.
     * @return Returns an Optional destination URI attribute, used in notifications and RPC messages.
     */
    public Optional<UUri> sink() {
        return sink == null ? Optional.empty() : Optional.of(sink);
    }

    /**
     * The reqid is used to indicate a response for a specific request.
     * @return Returns an Optional requestId that indicates that this is a response for a specific request.
     */
    public Optional<UUID> reqid() {
        return reqid == null ? Optional.empty() : Optional.of(reqid);
    }

    /**
     * The permission level of the message.
     * @return Returns an Optional permission level attribute.
     */
    public Optional<Integer> plevel() {
        return plevel == null ? Optional.empty() : Optional.of(plevel);
    }

    /**
     * The communication status of the message.
     * @return Returns an Optional communication status attribute that indicates an error from the platform.
     */
    public Optional<Integer> commstatus() {
        return commstatus == null ? Optional.empty() : Optional.of(commstatus);
    }

    /**
     * Look at the configured UAttributes and determine if the payload could be an RPC Request.
     * @return Returns true if the attributes configured indicate that the payload is an RPC Request.
     */
    public boolean isRpcRequest() {
        return UMessageType.REQUEST.equals(type()) && sink().isPresent();
    }

    /**
     * Look at the configured UAttributes and determine if the payload could be an RPC Response.
     * @return Returns true if the attributes configured indicate that the payload is an RPC Response.
     */
    public boolean isRpcResponse() {
        return UMessageType.RESPONSE.equals(type()) && sink().isPresent() && reqid().isPresent();
    }

    /**
     * Look at the configured UAttributes and determine of the platform indicated problems.
     * @return Returns true if there were platform errors during the operation of uTransport.
     */
    public boolean isPlatformTransportSuccess() {
        return commstatus().orElse(0) == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UAttributes that = (UAttributes) o;
        return Objects.equals(id, that.id) && type == that.type
                && priority == that.priority && Objects.equals(ttl, that.ttl)
                && Objects.equals(token, that.token) && Objects.equals(sink, that.sink)
                && Objects.equals(plevel, that.plevel) && Objects.equals(commstatus, that.commstatus)
                && Objects.equals(reqid, that.reqid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, priority, ttl, token, sink, plevel, commstatus, reqid);
    }

    @Override
    public String toString() {
        return "UAttributes{" +
                "id=" + id +
                ", type=" + type +
                ", priority=" + priority +
                ", ttl=" + ttl +
                ", token='" + token + '\'' +
                ", sink=" + sink +
                ", plevel=" + plevel +
                ", commstatus=" + commstatus +
                ", reqid=" + reqid +
                '}';
    }

    /**
     * Builder for easy construction of the UAttributes object.
     */
    public static class UAttributesBuilder {

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
         * Construct the UAttributesBuilder with the configurations that are required for every payload transport.
         * @param id Unique identifier for the message.
         * @param type Message type such as Publish a state change, RPC request or RPC response.
         * @param priority uProtocol Prioritization classifications.
         */
        public UAttributesBuilder(UUID id, UMessageType type, UPriority priority) {
            this.id = id;
            this. type = type;
            this.priority = priority;
        }

        /**
         * Add the time to live in milliseconds.
         * @param ttl the time to live in milliseconds.
         * @return Returns the UAttributesBuilder with the configured ttl.
         */
        public UAttributesBuilder withTtl(Integer ttl) {
            this.ttl = ttl;
            return this;
        }

        /**
         * Add the authorization token used for TAP.
         * @param token the authorization token used for TAP.
         * @return Returns the UAttributesBuilder with the configured token.
         */
        public UAttributesBuilder withToken(String token) {
            this.token = token;
            return this;
        }

        /**
         * Add the explicit destination URI.
         * @param sink the explicit destination URI.
         * @return Returns the UAttributesBuilder with the configured sink.
         */
        public UAttributesBuilder withSink(UUri sink) {
            this.sink = sink;
            return this;
        }

        /**
         * Add the permission level of the message.
         * @param plevel the permission level of the message.
         * @return Returns the UAttributesBuilder with the configured plevel.
         */
        public UAttributesBuilder withPermissionLevel(Integer plevel) {
            this.plevel = plevel;
            return this;
        }

        /**
         * Add the communication status of the message.
         * @param commstatus the communication status of the message.
         * @return Returns the UAttributesBuilder with the configured commstatus.
         */
        public UAttributesBuilder withCommStatus(Integer commstatus) {
            this.commstatus = commstatus;
            return this;
        }

        /**
         * Add the request ID.
         * @param reqid the request ID.
         * @return Returns the UAttributesBuilder with the configured reqid.
         */
        public UAttributesBuilder withReqId(UUID reqid) {
            this.reqid = reqid;
            return this;
        }

        /**
         * Construct the UAttributes from the builder.
         * @return Returns a constructed UAttributes.
         */
        public UAttributes build() {
            return new UAttributes(this);
        }
    }
}