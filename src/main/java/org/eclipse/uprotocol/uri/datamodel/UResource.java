/*
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
public class UResource {

    private static final UResource EMPTY = new UResource("", null,null);

    private static final UResource RESPONSE = new UResource("rpc", "response",null);

    private final String name;

    private final String instance;

    private final String message;

    //TODO when message is empty - try and infer it from the name "door" -> "Door"

    /**
     * Create an  Resource. The resource is something that is manipulated by a service such as a door.
     * @param name      The name of the resource as a noun such as door or window, or in the case a method that manipulates the resource, a verb.
     * @param instance  An instance of a resource such as front_left.
     * @param message   The Message type matches the protobuf service IDL message name that defines structured data types.
     *                  A message is a data structure type used to define data that is passed in  events and rpc methods.
     */
    public UResource(String name, String instance, String message) {
        Objects.requireNonNull(name, " Resource must have a name.");
        this.name = name;
        this.instance = instance;
        this.message = message;
    }

    /**
     * Static factory method for creating an  Resource using the resource name.
     * @param name The name of the resource as a noun such as door or window, or in the case a method that manipulates the resource, a verb.
     * @return Returns an UResource with the resource name where the instance and message are empty.
     *      If the instance does not exist, it is assumed that all the instances of the resource are wanted.
     */
    public static UResource fromName(String name) {
        return new UResource(name, null, null);
    }

    /**
     * Static factory method for creating an  Resource using the resource name and some resource instance.
     * @param name      The name of the resource as a noun such as door or window, or in the case a method that manipulates the resource, a verb.
     * @param instance  An instance of a resource such as front_left.
     * @return Returns an UResource with the resource name and a specific instance where the message is left empty.
     */
    public static UResource fromNameWithInstance(String name, String instance) {
        return new UResource(name, instance, null);
    }

    /**
     * Static factory method for creating an  Resource using a resource command name such as UpdateDoor.
     * @param commandName The RPC command name such as UpdateDoor.
     * @return returns an Ulitfi resource used for sending RPC commands.
     */
    public static UResource forRpc(String commandName) {
        Objects.requireNonNull(commandName, " Resource must have a command name.");
        return new UResource("rpc", commandName, null);
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
     * Static factory method for creating a response  resource that is returned from RPC calls<br>
     * @return Returns a response  resource used for response RPC calls.
     */
    public static UResource response() {
        return RESPONSE;
    }

    /**
     * Indicates that this resource is an empty container and has no valuable information in building uProtocol URI.
     * @return Returns true if this resource is an empty container and has no valuable information in building uProtocol URI.
     */
    public boolean isEmpty() {
        return name.isBlank() && instance().isEmpty() && message().isEmpty();
    }

    /**
     * @return Returns the name of the resource as a noun such as door or window, or in the case a method that manipulates the resource, a verb.
     */
    public String name() {
        return name;
    }

    /**
     * Support for building the name attribute in many protobuf Message objects.
     * Will build a string with the name and instance with a dot delimiter, only if the instance exists.
     * @return Returns a string used for building the name attribute in many protobuf Message objects.
     *      Will build a string with the name and instance with a dot delimiter, only if the instance exists.
     */
    public String nameWithInstance() {
        return instance().isPresent() ? String.format("%s.%s", name(), instance().get()) : name();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UResource that = (UResource) o;
        return Objects.equals(name, that.name) && Objects.equals(instance, that.instance) && Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, instance, message);
    }

    @Override
    public String toString() {
        return "UResource{" +
                "name='" + name + '\'' +
                ", instance='" + instance + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
