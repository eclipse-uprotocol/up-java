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

import java.net.InetAddress;
import java.util.Objects;
import java.util.Optional;

/**
 * An Authority represents the deployment location of a specific Software Entity.
 * Data representation of an <b>Authority</b>.<br> An Authority consists of a device, a domain and a micro version in the form of an IP Address.<br>
 * Device and domain names are used as part of the URI for device and service discovery. Optimized micro versions of the UUri will use the IP Address.<br>
 * Devices will be grouped together into realms of Zone of Authority.<br>
 */
public class UAuthority implements UriFormat {
    private final static UAuthority EMPTY = new UAuthority(null, null, null, false, true);

    /**
     * A device is a logical independent representation of a service bus in different execution environments.<br>
     * Devices will be grouped together into realms of Zone of Authority.
     */
    private final String device;

    /**
     * The domain a software entity is deployed on, such as vehicle or backoffice.<br>
     * Vehicle Domain name <b>MUST</b> be that of the vehicle VIN.<br>
     * A domain name is an identification string that defines a realm of administrative autonomy, authority or control within the Internet.
     */
    private final String domain;

    /**
     * An UAuthority starting with // is a remote configuration of a URI, and we mark the uAuthority implicitly as remote.
     * This is never exposed externally and is used internally to indicate remote or local deployments.
     */
    private final boolean markedRemote;

    /**
     * The device IP address. Represents the micro version of a UAuthority.
     */
    private final InetAddress address;

    /**
     * Indicates that this UAuthority has already been resolved.
     * A resolved UAuthority means that it has all the information needed to be serialised in the long format or the micro format of a UUri.
     */
    private final boolean markedResolved;

    /**
     * Constructor for building a UAuthority.
     * @param device        The device a software entity is deployed on, such as the VCU, CCU or cloud provider.
     * @param domain        The domain a software entity is deployed on, such as vehicle or Cloud (PaaS).
     * @param address      The device IP address of the device.
     * @param markedRemote  Indicates if this UAuthority was implicitly marked as remote.
     * @param markedResolved Indicates that this uAuthority has all the information needed to be serialised
     *                          in the long format or the micro format of a UUri.
     */
    private UAuthority(String device, String domain, InetAddress address, boolean markedRemote, boolean markedResolved) {
        this.device = device == null ? null : device.toLowerCase();
        this.domain = domain == null ? null : domain.toLowerCase();
        this.address = address;
        this.markedRemote = markedRemote;
        this.markedResolved = markedResolved;
    }

    /**
     * Static factory method for creating a local uAuthority.<br>
     * A local uri does not contain an authority and looks like this:
     * <pre> :&lt;service&gt;/&lt;version&gt;/&lt;resource&gt;#&lt;Message&gt; </pre>
     * @return Returns a local uAuthority that has no domain, device, or ip address information, indicating to uProtocol
     *      that the uAuthority part in the UUri is relative to the sender/receiver deployment environment.
     */
    public static UAuthority local() {
        return EMPTY;
    }

    /**
     * Static factory method for creating a remote authority supporting the long serialization information representation of a UUri.<br>
     * Building a UAuthority with this method will create an unresolved uAuthority that can only be serialised in long UUri format.
     * An uri with a long representation of uAUthority can be serialised as follows:
     * <pre> //&lt;device&gt;.&lt;domain&gt;/&lt;service&gt;/&lt;version&gt;/&lt;resource&gt;#&lt;Message&gt; </pre>
     * @param device The device a software entity is deployed on, such as the VCU, CCU or cloud provider.
     * @param domain The domain a software entity is deployed on, such as vehicle or Cloud (PaaS). Vehicle Domain name <b>MUST</b> be that of the vehicle VIN.
     * @return Returns a uAuthority that contains the device and the domain and can only be serialized in long UUri format.
     */
    public static UAuthority longRemote(String device, String domain) {
        return new UAuthority(device, domain, null, true, false);
    }

    /**
     * Static factory method for creating a remote authority supporting the micro serialization information representation of a UUri.<br>
     * Building a UAuthority with this method will create an unresolved uAuthority that can only be serialised in micro UUri format.
     * @param address The ip address of the device a software entity is deployed on.
     * @return Returns a uAuthority that contains only the internet address of the device, and can only be serialized in micro UUri format.
     */
    public static UAuthority microRemote(InetAddress address) {
        return new UAuthority(null, null, address, true, false);
    }

    /**
     * Static factory method for creating a remote authority that is completely resolved with name, device and ip address of the device.<br>
     * Building a UAuthority with this method will enable serialisation in both UUri formats, long UUri format and micro UUri format.
     * Note that in the case of missing data, this will not fail, but simply create a UAuthority that is not resolved.
     * @param device The device name for long serialization of UUri.
     * @param domain The domain name for long serialization of UUri.
     * @param address the IP address for the device, for micro serialization of UUri.
     * @return Returns a uAuthority that contains all resolved data and so can be serialized into a long UUri or a micro UUri.
     */
    public static UAuthority resolvedRemote(String device, String domain, InetAddress address) {
        boolean resolved = device != null && !device.isBlank() && address != null;
        return new UAuthority(device, domain, address, true, resolved);
    }

    /**
     * Static factory method for creating an empty uAuthority, to avoid working with null<br>
     * Empty uAuthority is still serializable in both long and micro UUri formats, and will be as local to the current deployment environment.
     * @return Returns an empty authority that has no domain, device, or device ip address information.
     */
    public static UAuthority empty() {
        return EMPTY;
    }

    /**
     * Accessing an optional device of the uAuthority.
     * @return Returns the device a software entity is deployed on, such as the VCU, CCU or cloud provider.
     */
    public Optional<String> device() {
        return device == null || device.isBlank() ? Optional.empty() : Optional.of(device);
    }

    /**
     * Accessing an optional domain of the uAuthority.
     * @return Returns the domain a software entity is deployed on, such as vehicle or backoffice.
     */
    public Optional<String> domain() {
        return domain == null || domain.isBlank() ? Optional.empty() : Optional.of(domain);
    }

    /**
     * Accessing an optional IP address configuration of the uAuthority.
     * @return Returns the device IP address.
     */
    public Optional<InetAddress> address() {
        return Optional.ofNullable(address);
    }

    /**
     * Support for determining if this uAuthority is defined for a local deployment.
     * @return returns true if this uAuthority is local, meaning does not contain a device/domain for long UUri or information for micro UUri.
     */
    public boolean isLocal() {
        return isEmpty() && !isMarkedRemote();
    }

    /**
     * Support for determining if this uAuthority defines a deployment that is defined as remote.
     * @return Returns true if this uAuthority is remote, meaning it contains information for serialising a long UUri or a micro UUri.
     */
    public boolean isRemote() {
        return isMarkedRemote();
    }

    /**
     * Support for determining if this uAuthority was configured to be remote.
     * @return Returns true if this uAuthority is explicitly configured remote deployment.
     */
    public boolean isMarkedRemote() {
        return markedRemote;
    }

    /**
     * Indicates that this UAuthority has already been resolved.
     * A resolved UAuthority means that it has all the information needed to be serialised in the long format or the micro format of a UUri.
     * @return Returns true if this UAuthority is resolved with all the information needed to be serialised in the long format or the micro format of a UUri.
     */
    @Override
    public boolean isResolved() {
        return markedResolved;
    }

    /**
     * Determine if the UAuthority can be used to serialize a UUri in long format.
     * @return Returns true if the UAuthority can be used to serialize a UUri in long format.
     */
    @Override
    public boolean isLongForm() {
        return isLocal() || device().isPresent();
    }

    /**
     * Determine if the UAuthority can be used to serialize a UUri in micro format.
     * @return Returns true if the uAuthority can be serialized a UUri in micro format.
     */
    @Override
    public boolean isMicroForm() {
        return isLocal() || address().isPresent();
    }

    @Override
    public boolean isEmpty() {
        return domain().isEmpty() && device().isEmpty() && address().isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UAuthority that = (UAuthority) o;
        return markedRemote == that.markedRemote && markedResolved == that.markedResolved && Objects.equals(device, that.device)
                && Objects.equals(domain, that.domain) && Objects.equals(address, that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(device, domain, markedRemote, address, markedResolved);
    }

    @Override
    public String toString() {
        return "UAuthority{" +
                "device='" + device + '\'' +
                ", domain='" + domain + '\'' +
                ", markedRemote=" + markedRemote +
                ", address=" + address +
                ", markedResolved=" + markedResolved +
                '}';
    }
}
