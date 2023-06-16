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
 * Data representation of an <b> Authority</b>.<br> An  Authority consists of a device and a domain.<br>
 * Device and domain names are used as part of the URI for device and service discovery.<br>
 * Devices will be grouped together into realms of Zone of Authority.<br>
 * An  Authority represents the deployment location of a specific  Software Entity in the Ultiverse.
 */
public class UAuthority {
    private final static UAuthority EMPTY = new UAuthority(null, null, false);

    /**
     * A device is a logical independent representation of a service bus in different execution environments.<br>
     *  Devices will be grouped together into realms of Zone of Authority.
     */
    private final String device;

    /**
     * The domain an  software entity is deployed on, such as vehicle or backoffice.<br>
     * Vehicle Domain name <b>MUST</b> be that of the vehicle VIN.<br>
     * A domain name is an identification string that defines a realm of administrative autonomy, authority or control within the Internet.
     */
    private final String domain;

    /**
     * An  Uri starting with up:// is a remote configuration of a URI, and we mark the uAuthority implicitly as remote.
     */
    private final boolean markedRemote;

    // TODO add user information - what is this used for? make sure it is part of the domain.

    /**
     * Constructor.
     *
     * @param device        The device an  software entity is deployed on, such as the VCU, CCU or Cloud (PaaS).
     * @param domain        The domain an  software entity is deployed on, such as vehicle or backoffice.
     * @param markedRemote  Indicates if this UAuthority was implicitly marked as remote. Used for validation.
     */
    private UAuthority(String device, String domain, boolean markedRemote) {
        this.device = device == null ? null : device.toLowerCase();
        this.domain = domain == null ? null : domain.toLowerCase();
        this.markedRemote = markedRemote;
    }

    /**
     * Static factory method for creating a local  authority.<br>
     * A local uri does not contain an authority and looks like this:
     * <pre> :&lt;service&gt;/&lt;version&gt;/&lt;resource&gt;#&lt;Message&gt; </pre>
     * @return Returns a local altifi authority that has no domain or device information.
     */
    public static UAuthority local() {
        return EMPTY;
    }

    /**
     * Static factory method for creating a remote  authority.<br>
     * A remote uri contains an authority and looks like this:
     * <pre> up://&lt;device&gt;.&lt;domain&gt;/&lt;service&gt;/&lt;version&gt;/&lt;resource&gt;#&lt;Message&gt; </pre>
     * @param device The device an  software entity is deployed on, such as the VCU, CCU or Cloud (PaaS).
     * @param domain The domain an  software entity is deployed on, such as vehicle or backoffice. Vehicle Domain name <b>MUST</b> be that of the vehicle VIN.
     * @return returns a remote  authority that contains the device and the domain.
     */
    public static UAuthority remote(String device, String domain) {
        return new UAuthority(device, domain, true);
    }

    /**
     * Static factory method for creating an empty  authority, to avoid working with null<br>
     * @return Returns an empty altifi authority that has no domain or device information.
     */
    public static UAuthority empty() {
        return EMPTY;
    }

    /**
     * @return returns true if this  authority is remote, meaning it contains a device or a domain.
     */
    public boolean isRemote() {
        return domain().isPresent() || device().isPresent();
    }

    /**
     * @return returns true if this  authority is local, meaning does not contain a device or a domain.
     */
    public boolean isLocal() {
        return domain().isEmpty() && device().isEmpty();
    }

    /**
     * @return Returns the device an  software entity is deployed on, such as the VCU, CCU or Cloud (PaaS).
     */
    public Optional<String> device() {
        return device == null || device.isBlank() ? Optional.empty() : Optional.of(device);
    }

    /**
     * @return Returns the domain an  software entity is deployed on, such as vehicle or backoffice.
     */
    public Optional<String> domain() {
        return domain == null || domain.isBlank() ? Optional.empty() : Optional.of(domain);
    }

    /**
     * @return Returns the explicitly configured remote deployment.
     */
    public boolean isMarkedRemote() {
        return markedRemote;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UAuthority that = (UAuthority) o;
        return markedRemote == that.markedRemote && Objects.equals(device, that.device)
                && Objects.equals(domain, that.domain);
    }

    @Override
    public int hashCode() {
        return Objects.hash(device, domain, markedRemote);
    }

    @Override
    public String toString() {
        return "UAuthority{" +
                "device='" + device + '\'' +
                ", domain='" + domain + '\'' +
                ", markedRemote=" + markedRemote +
                '}';
    }
}
