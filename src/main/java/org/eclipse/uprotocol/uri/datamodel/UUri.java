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

/**
 * Data representation of an <b> URI</b>.
 * This class will be used to represent the source and sink (destination) parts of the  Packet CloudEvent. <br>
 * URI is used as a method to uniquely identify devices, services, and resources on the  network.<br>
 * Defining a common URI for the system allows applications and/or services to publish and discover each other
 * as well as maintain a database/repository of microservices in the various vehicles.<br>
 * Example:
 * <pre>
 *     //&lt;device&gt;.&lt;domain&gt;/&lt;service&gt;/&lt;version&gt;/&lt;resource&gt;#&lt;message&gt;
 * </pre>
 *
 */
public class UUri implements Uri {
    private static final UUri EMPTY = new UUri(UAuthority.empty(), UEntity.empty(), UResource.empty());

    private final UAuthority uAuthority;
    private final UEntity uEntity;
    private final UResource uResource;


    /**
     * Create a full  URI.
     * @param uAuthority The  Authority represents the deployment location of a specific  Software Entity .
     * @param uEntity The USE in the role of a service or in the role of an application.
     * @param uResource The resource is something that is manipulated by a service such as a Door.
     */
    public UUri(UAuthority uAuthority, UEntity uEntity, UResource uResource) {
        this.uAuthority = Objects.requireNonNullElse(uAuthority, UAuthority.empty());
        this.uEntity = Objects.requireNonNullElse(uEntity, UEntity.empty());
        this.uResource = Objects.requireNonNullElse(uResource, UResource.empty());
    }

    /**
     * Create an  URI for a resource. This will match all the specific instances of the resource,
     *      for example all the instances of the vehicle doors.
     * @param uAuthority The  Authority represents the deployment location of a specific  Software Entity.
     * @param uEntity The USE in the role of a service or in the role of an application.
     * @param uResource The resource is something that is manipulated by a service such as a Door.
     */
    public UUri(UAuthority uAuthority, UEntity uEntity, String uResource) {
        this(uAuthority, uEntity, UResource.fromName(uResource));
    }

    /**
     * Create a RPC Response UUri passing the Authority and Entity information
     * @param uAuthority The  Authority represents the deployment location of a specific  Software Entity.
     * @param uEntity The SW entity information
     * @return Returns a UUri of a constructed RPC Response
     */
    public static UUri rpcResponse(UAuthority uAuthority, UEntity uEntity) {
        return new UUri(uAuthority, uEntity, UResource.response());
    }
    

    /**
     * Static factory method for creating an empty  uri, to avoid working with null<br>
     * @return Returns an empty ultify  uri to avoid working with null.
     */
    public static UUri empty() {
        return EMPTY;
    }

    /**
     * Indicates that this  URI is an empty container and has no valuable information in building uProtocol sinks or sources.
     * @return Returns true if this  URI is an empty container and has no valuable information in building uProtocol sinks or sources.
     */
    public boolean isEmpty() {
        return uAuthority.isLocal() && uEntity().isEmpty()
                && uResource.isEmpty();
    }

    /**
     * @return Returns the  Authority represents the deployment location of a specific  Software Entity.
     */
    public UAuthority uAuthority() {
        return uAuthority;
    }

    /**
     * @return Returns the USE in the role of a service or in the role of an application.
     */
    public UEntity uEntity() {
        return uEntity;
    }

    /**
     * @return Returns the  resource, something that is manipulated by a service such as a Door.
     */
    public UResource uResource() {
        return this.uResource;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UUri uri = (UUri) o;
        return Objects.equals(uAuthority, uri.uAuthority) && Objects.equals(uEntity, uri.uEntity)
                && Objects.equals(uResource, uri.uResource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uAuthority, uEntity, uResource);
    }

    @Override
    public String toString() {
        return "Uri{" +
                "uAuthority=" + uAuthority +
                ", uEntity=" + uEntity +
                ", uResource=" + uResource +
                '}';
    }

    /**
     * Returns true if URI contains both names and numeric representations of the names inside
     * its belly.
     * @return Returns true if URI contains both names and numeric representations of the names inside
     */
    public boolean isResolved() {
        return uAuthority.isResolved() && uEntity.isResolved() && uResource.isResolved();
    }

    /**
     * Check if the UEntity and UResource contains Long form URI information (names)
     * @return Returns true if the UEntity and UResource contains Long form URI information (names)
     */
    public boolean isLongForm() {
        return  isResolved() || uEntity.isLongForm() && uResource.isLongForm();
    }
}