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
 * Data representation of an <b> Software Entity - uE</b><br>
 * An Software Entity is a piece of software deployed somewhere on a uDevice.<br>
 * The Software Entity is used in the source and sink parts of communicating software.<br>
 * A uE that publishes events is a <b>Service</b> role.<br>
 * A uE that consumes events is an <b>Application</b> role.
 */
public class UEntity implements UriPart {
    private static final UEntity EMPTY = new UEntity("", null, null, false);

    private final String name;      // uE Name
    private final Integer version;   // uE Major Version
    private final Short id;         // uE ID
    private final boolean markedResolved; // Indicates that this UAuthority has already been resolved.

    /**
     * Build a Software Entity that represents a communicating piece of software.
     * @param name The name of the software such as petapp or body.access.
     * @param version The software version. If not supplied, the latest version of the service will be used.
     * @param id A numeric identifier for the software entity.
     * @param markedResolved Indicates that this uResource was populated with intent of having all data.
     */
    private UEntity(String name, Integer version, Short id, boolean markedResolved) {
        Objects.requireNonNull(name, " Software Entity must have a name");
        this.name = name;
        this.id = id;
        this.version = version;
        this.markedResolved = markedResolved;
    }

    public static UEntity resolvedFormat(String name, Integer version, Short id) {
        boolean resolved = !name.isEmpty() && version != null && id != null;
        return new UEntity(name, version, id, resolved);
    }

    /**
     * Static factory method for creating a uE using the software entity name, that can be used to serialize long UUris.
     * @param name The software entity name, such as petapp or body.access.
     * @return Returns an UEntity with the name where the version is the latest version of the service and can only be serialized
     *      to long UUri format.
     */
    public static UEntity longFormat(String name) {
        return new UEntity(name, null, null, false);
    }

    /**
     * Static factory method for creating a uE using the software entity name, that can be used to serialize long UUris.
     * @param name The software entity name, such as petapp or body.access.
     * @param version The software entity version.
     * @return Returns an UEntity with the name and the version of the service and can only be serialized
     *      to long UUri format.
     */
    public static UEntity longFormat(String name, Integer version) {
        return new UEntity(name, version, null, false);
    }

    /**
     * Static factory method for creating a uE using the software entity identification number, that can be used to serialize micro UUris.
     * @param id The software entity name, such as petapp or body.access.
     * @return Returns an UEntity with the name where the version is the latest version of the service and can only be serialized
     *      to long UUri format.
     */
    public static UEntity microFormat(Short id) {
        return new UEntity("", null, id, false);
    }

    /**
     * Static factory method for creating a uE using the software entity identification number, that can be used to serialize micro UUris.
     * @param id The software entity name, such as petapp or body.access.
     * @param version The software entity version.
     * @return Returns an UEntity with the name and the version of the service and can only be serialized
     *      to long UUri format.
     */
    public static UEntity microFormat(Short id, Integer version) {
        return new UEntity("", version, id, false);
    }

    /**
     * Static factory method for creating an empty  software entity, to avoid working with null<br>
     * @return Returns an empty  software entity that has a blank name and no version information.
     */
    public static UEntity empty() {
        return EMPTY;
    }

    /**
     * Indicates that this USE is an empty container and has no valuable information in building uProtocol sinks or sources.
     * @return Returns true if this USE is an empty container and has no valuable information in building uProtocol sinks or sources.
     */
    @Override
    public boolean isEmpty() {
        return name.isBlank() && version().isEmpty() && id().isEmpty();
    }

    /**
     * Return true if the UEntity contains both the name and IDs meaning it contains all the information to be serialized
     *      into a long UUri or a micro form UUri.
     * @return  Returns true of this resource contains resolved information meaning it contains all the information to be serialized
     *      into a long UUri or a micro form UUri.
     */
    public boolean isResolved() {
        return markedResolved;
    }

    /**
     * Determine if this software entity can be serialised into a long UUri form.
     * @return Returns true if this software entity can be serialised into a long UUri form, meaning it has at least a name.
     */
    public boolean isLongForm() {
        return !name().isEmpty();
    }

    /**
     * Returns true if the Uri part contains the id's which will allow the Uri part to be serialized into micro form.
     * @return Returns true if the Uri part can be serialized into micro form.
     */
    @Override
    public boolean isMicroForm() {
        return id().isPresent();
    }

    /**
     * @return Returns the name of the software such as petpp or body.access.
     */
    public String name() {
        return name;
    }

    /**
     * @return Returns the software version if it exists.
     * If the version does not exist, the latest version of the service will be used.
     */
    public Optional<Integer> version() {
        return Optional.ofNullable(version);
    }

    /**
     * @return Returns the software id if it exists. The software id represents the numeric identifier of the uE.
     */
    public Optional<Short> id() {
        return Optional.ofNullable(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UEntity uEntity = (UEntity) o;
        return markedResolved == uEntity.markedResolved && Objects.equals(name, uEntity.name)
                && Objects.equals(version, uEntity.version) && Objects.equals(id, uEntity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version, id, markedResolved);
    }


    @Override
    public String toString() {
        return "UEntity{" +
                "name='" + name + '\'' +
                ", version=" + version +
                ", id=" + id +
                ", markedResolved=" + markedResolved +
                '}';
    }
}
