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

import org.eclipse.uprotocol.uri.factory.UriFactory;

import java.util.Objects;

/**
 * Data representation of an <b> URI</b>.<br>
 * Matches the uProtocol URI in SDV-202 uProtocol Format<br>
 * This class will be used to represent the source and sink (destination) parts of the  Packet CloudEvent. <br>
 * URI is used as a method to uniquely identify devices, services, and resources on the  network.<br>
 * Defining a common URI for the system allows applications and/or services to publish and discover each other
 * as well as maintain a database/repository of microservices in the various vehicles.<br>
 * Example:
 * <pre>
 *     up://&lt;device&gt;.&lt;domain&gt;/&lt;service&gt;/&lt;version&gt;/&lt;resource&gt;#&lt;message&gt;
 * </pre>
 *
 */
public class UUri {
    private static final UUri EMPTY = new UUri(UAuthority.empty(), UEntity.empty(), UResource.empty());

    public static final String SCHEME = "up:";
    private final UAuthority uAuthority;
    private final UEntity uEntity;
    private final UResource uResource;

    private transient String uProtocolUri;

    /**
     * Create a full  URI.
     * @param uAuthority The  Authority represents the deployment location of a specific  Software Entity in the Ultiverse.
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
     * @param uAuthority The  Authority represents the deployment location of a specific  Software Entity in the Ultiverse.
     * @param uEntity The USE in the role of a service or in the role of an application.
     * @param uResource The resource is something that is manipulated by a service such as a Door.
     */
    public UUri(UAuthority uAuthority, UEntity uEntity, String uResource) {
        this(uAuthority, uEntity, UResource.fromName(uResource));
    }

    /**
     * Static factory method for creating an empty  uri, to avoid working with null<br>
     * @return Returns an empty altifi  uri to avoid working with null.
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
     * @return Returns the  Authority represents the deployment location of a specific  Software Entity in the Ultiverse.
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

    /**
     * Support for a lazy generation of the uProtocol Uri string that is used in CloudEvent routing for
     * sources and sinks.
     * The function used to generate the string is the buildUProtocolUri method in the {@link UriFactory}.
     * @return Returns the String that can be used as Source and Sink values of CloudEvents. The value is cached and only calculated on the first call.
     */
    public String uProtocolUri() {
        if (this.uProtocolUri == null) {
            uProtocolUri = UriFactory.buildUProtocolUri(uAuthority(), uEntity(), uResource());
        }
        return uProtocolUri;
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
}
