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
 * Data representation of uProtocol <b>URI</b>.
 * This class will be used to represent the source and sink (destination) parts of the Packet, for example in a CloudEvent Packet. <br>
 * UUri is used as a method to uniquely identify devices, services, and resources on the  network.<br>
 * Where software is deployed, what the service is called along with a version and the resources in the service.
 * Defining a common URI for the system allows applications and/or services to publish and discover each other
 * as well as maintain a database/repository of microservices in the various vehicles.<br>
 * Example for long format serialization:
 * <pre>
 *     //&lt;device&gt;.&lt;domain&gt;/&lt;service&gt;/&lt;version&gt;/&lt;resource&gt;#&lt;message&gt;
 * </pre>
 *
 */
public class UUri implements UriFormat {
    private static final UUri EMPTY = new UUri(UAuthority.empty(), UEntity.empty(), UResource.empty());

    private final UAuthority uAuthority;
    private final UEntity uEntity;
    private final UResource uResource;

    /**
     * Create a full  URI.
     * @param uAuthority The uAuthority represents the deployment location of a specific Software Entity .
     * @param uEntity The uEntity in the role of a service or in the role of an application is the software and version.
     * @param uResource The uResource is something that is manipulated by a service such as a Door.
     */
    public UUri(UAuthority uAuthority, UEntity uEntity, UResource uResource) {
        this.uAuthority = Objects.requireNonNullElse(uAuthority, UAuthority.empty());
        this.uEntity = Objects.requireNonNullElse(uEntity, UEntity.empty());
        this.uResource = Objects.requireNonNullElse(uResource, UResource.empty());
    }

    /**
     * Create a URI for a resource. This will match all the specific instances of the resource,
     *      for example all the instances of the vehicle doors.
     * @param uAuthority The  Authority represents the deployment location of a specific  Software Entity.
     * @param uEntity The USE in the role of a service or in the role of an application.
     * @param uResource The resource is something that is manipulated by a service such as a Door.
     */
    public UUri(UAuthority uAuthority, UEntity uEntity, String uResource) {
        this(uAuthority, uEntity, UResource.longFormat(uResource));
    }

    /**
     * Create an RPC Response UUri passing the Authority and Entity information.
     * @param uAuthority The uAuthority represents the deployment location of a specific Software Entity.
     * @param uEntity The SW entity information.
     * @return Returns a UUri of a constructed RPC Response.
     */
    public static UUri rpcResponse(UAuthority uAuthority, UEntity uEntity) {
        return new UUri(uAuthority, uEntity, UResource.forRpcResponse());
    }
    

    /**
     * Static factory method for creating an empty  uri, to avoid working with null<br>
     * @return Returns an empty uri to avoid working with null.
     */
    public static UUri empty() {
        return EMPTY;
    }

    /**
     * Indicates that this  URI is an empty container and has no valuable information in building uProtocol sinks or sources.
     * @return Returns true if this  URI is an empty container and has no valuable information in building uProtocol sinks or sources.
     */
    @Override
    public boolean isEmpty() {
        return uAuthority.isLocal() && uEntity().isEmpty()
                && uResource.isEmpty();
    }

    /**
     * Returns true if URI contains both names and numeric representations of the names inside its belly.
     * Meaning that this UUri can be serialized to long or micro formats.
     * @return Returns true if URI contains both names and numeric representations of the names inside its belly.
     *      Meaning that this UUri can be serialized to long or micro formats.
     */
    @Override
    public boolean isResolved() {
        return uAuthority.isResolved() && uEntity.isResolved() && uResource.isResolved();
    }

    /**
     * Determines if this UUri can be serialized into a long form UUri.
     * @return Returns true if this UUri can be serialized into a long form UUri.
     */
    @Override
    public boolean isLongForm() {
        return uAuthority.isLongForm() &&
                (uEntity.isLongForm() || uEntity.isEmpty()) &&
                (uResource.isLongForm() || uResource().isEmpty());
    }

    /**
     * Determines if this UUri can be serialized into a micro form UUri.
     * @return Returns true if this UUri can be serialized into a micro form UUri.
     */
    @Override
    public boolean isMicroForm() {
        return uAuthority.isMicroForm() && uEntity.isMicroForm() && uResource.isMicroForm();
    }

    /**
     * @return Returns the Authority represents the deployment location of a specific Software Entity.
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
        return "UriPart{" +
                "uAuthority=" + uAuthority +
                ", uEntity=" + uEntity +
                ", uResource=" + uResource +
                '}';
    }
}