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

package org.eclipse.uprotocol.uri.datamodel;

import java.util.Objects;
import java.util.Optional;

/**
 * An  service API - defined in the {@link UEntity} - has Resources and Methods. Both of these are represented by the UResource class.<br>
 * An  Resource represents a resource from a Service such as "door" and an optional specific instance such as "front_left". In addition, it can optionally contain
 * the name of the resource Message type, such as "Door". The Message type matches the protobuf service IDL that defines structured data types. <br>
 * An UResource is something that can be manipulated/controlled/exposed by a service. Resources are unique when prepended with UAuthority that represents the device and
 * UEntity that represents the service.
 */
public class UResource implements UriFormat {

    private static final UResource EMPTY = new UResource("", null,null, null, false);

    private static final UResource RESPONSE = new UResource("rpc", "response",null, (short) 0, true);

    private final String name;

    private final String instance;

    private final String message;

    private final Short id;

    private final boolean markedResolved; // Indicates that this UAuthority has already been resolved.

    /**
     * Create a uResource. The resource is something that is manipulated by a service such as a door.
     * @param name      The name of the resource as a noun such as door or window, or in the case a method that manipulates the resource, a verb.
     * @param instance  An instance of a resource such as front_left.
     * @param message   The Message type matches the protobuf service IDL message name that defines structured data types.
     *                  A message is a data structure type used to define data that is passed in  events and rpc methods.
     * @param id        The numeric representation of this uResource.
     * @param markedResolved Indicates that this uResource was populated with intent of having all data.
     */
    private UResource(String name, String instance, String message, Short id, boolean markedResolved) {
        this.name = Objects.requireNonNullElse(name, "");
        this.instance = instance;
        this.message = message;
        this.id = id;
        this.markedResolved = markedResolved;
    }

    /**
     * Build a UResource that has serialization information.
     * @param name The name of the resource as a noun such as door or window, or in the case a method that manipulates the resource, a verb.
     * @param instance An instance of a resource such as front_left.
     * @param message The Message type matches the protobuf service IDL message name that defines structured data types.
     *                A message is a data structure type used to define data that is passed in  events and rpc methods.
     * @param id The numeric representation of this uResource.
     * @return Returns a UResource that has all the information that is needed to serialize into a long UUri or a micro UUri.
     */
    public static UResource resolvedFormat(String name, String instance, String message, Short id) {
        boolean resolved = name != null && !name.isEmpty() && instance != null && !instance.isEmpty() && id != null;
        return new UResource(name, instance, message, id, resolved);
    }

    /**
     * Build a UResource that can be serialized into a long UUri. Mostly used for publishing messages.
     * @param name The name of the resource as a noun such as door or window, or in the case a method that manipulates the resource, a verb.
     * @return Returns a UResource that can be serialized into a long UUri.
     */
    public static UResource longFormat(String name) {
        return new UResource(name, null, null, null, false);
    }

    /**
     * Build a UResource that can be serialized into a long UUri. Mostly used for publishing messages.
     * @param name The name of the resource as a noun such as door or window, or in the case a method that manipulates the resource, a verb.
     * @param instance An instance of a resource such as front_left.
     * @param message The Message type matches the protobuf service IDL message name that defines structured data types.
     *                A message is a data structure type used to define data that is passed in  events and rpc methods.
     * @return Returns a UResource that can be serialised into a long UUri.
     */
    public static UResource longFormat(String name, String instance, String message) {
        return new UResource(name, instance, message, null, false);
    }

    /**
     * Build a UResource that can be serialised into a micro UUri. Mostly used for publishing messages.
     * @param id The numeric representation of this uResource.
     * @return Returns a UResource that can be serialised into a micro UUri.
     */
    public static UResource microFormat(Short id) {
        return new UResource("", null, null, id, false);
    }

    /**
     * Build a UResource for rpc request, using only the long format.
     * @param methodName The RPC method name.
     * @return Returns a UResource used for an RPC request that could be serialised in long format.
     */
    public static UResource forRpcRequest(String methodName) {
        return new UResource("rpc", methodName, null, null, false);
    }

    /**
     * Build a UResource for rpc request, using only the micro format.
     * @param methodId The numeric representation method name for the RPC.
     * @return Returns a UResource used for an RPC request that could be serialised in micro format.
     */
    public static UResource forRpcRequest(Short methodId) {
        return new UResource("rpc", null, null, methodId, false);
    }

    /**
     * Build a UResource for rpc request, using both the long and micro format information.
     * @param methodName The RPC method name.
     * @param methodId The numeric representation method name for the RPC.
     * @return Returns a UResource used for an RPC request that could be serialised in long and micro format.
     */
    public static UResource forRpcRequest(String methodName, Short methodId) {
        boolean resolved = methodName != null && !methodName.isEmpty() && methodId != null;
        return new UResource("rpc", methodName, null, methodId, resolved);
    }

    /**
     * Static factory method for creating a response resource that is returned from RPC calls<br>
     * @return Returns a response  resource used for response RPC calls.
     */
    public static UResource forRpcResponse() {
        return RESPONSE;
    }

    /**
     * @return Returns true if this resource specifies an RPC method call.
     */
    public boolean isRPCMethod() {
        return name.equals("rpc");
    }

    /**
     * Static factory method for creating an empty  resource, to avoid working with null<br>
     * @return Returns an empty  resource that has a blank name and no message instance information.
     */
    public static UResource empty() {
        return EMPTY;
    }

    /**
     * Indicates that this resource is an empty container and has no valuable information in building uProtocol URI.
     * @return Returns true if this resource is an empty container and has no valuable information in building uProtocol URI.
     */
    public boolean isEmpty() {
        return name.isBlank() && instance().isEmpty() && message().isEmpty() && id().isEmpty();
    }

    /**
     * @return Returns the name of the resource as a noun such as door or window, or in the case a method that manipulates the resource, a verb.
     */
    public String name() {
        return name;
    }

    /**
     * @return Returns the resource id if it exists.
     */
    public Optional<Short> id() {
        return Optional.ofNullable(id);
    }

    /**
     * An instance of a resource such as front_left
     * or in the case of RPC a method name that manipulates the resource such as UpdateDoor.
     * @return Returns the resource instance of the resource if it exists.
     * If the instance does not exist, it is assumed that all the instances of the resource are wanted.
     */
    public Optional<String> instance() {
        return instance == null || instance.isBlank() ? Optional.empty() : Optional.of(instance);
    }

    /**
     * The Message type matches the protobuf service IDL that defines structured data types.
     * A message is a data structure type used to define data that is passed in  events and rpc methods.
     * @return Returns the Message type matches the protobuf service IDL that defines structured data types.
     */
    public Optional<String> message() {
        return message == null || message.isBlank() ? Optional.empty() : Optional.of(message);
    }

    /**
     * Return true if this resource contains both ID and names.
     * Method type of UResource requires name, instance, and ID where a topic
     * type of UResource also requires message to not be null 
     * @return  Returns true of this resource contains resolved information
     */
    public boolean isResolved() {
        return markedResolved;
    }

    /**
     * Returns true if the uResource contains names so that it can be serialized to long format.
     * @return Returns true if the uResource contains names so that it can be serialized to long format.
     */
    @Override
    public boolean isLongForm() {
        return !name().isEmpty() && instance().isPresent();
    }

    /**
     * Returns true if the uResource contains the id's which will allow the Uri part to be serialized into micro form.
     * @return Returns true if the uResource can be serialized into micro form.
     */
    @Override
    public boolean isMicroForm() {
        return id().isPresent();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UResource uResource = (UResource) o;
        return markedResolved == uResource.markedResolved && Objects.equals(name, uResource.name)
                && Objects.equals(instance, uResource.instance) && Objects.equals(message, uResource.message)
                && Objects.equals(id, uResource.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, instance, message, id, markedResolved);
    }

    @Override
    public String toString() {
        return "UResource{" +
                "name='" + name + '\'' +
                ", instance='" + instance + '\'' +
                ", message='" + message + '\'' +
                ", id=" + id +
                ", markedResolved=" + markedResolved +
                '}';
    }
}
