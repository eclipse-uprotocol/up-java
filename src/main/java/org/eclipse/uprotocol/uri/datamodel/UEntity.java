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
 * An  Software Entity is a piece of software deployed somewhere on a uDevice.<br>
 * The  Software Entity is used in the source and sink parts of communicating software.<br>
 * A uE that publishes events is a <b>Service</b> role.<br>
 * A uE that consumes events is an <b>Application</b> role.
 */
public class UEntity implements Uri {
    private static final UEntity EMPTY = new UEntity("", null);

    private final String name;      // uE Name
    private final Integer version;   // uE Major Version
    private final Short id;         // uE ID

    /**
     * Build an  Software Entity that represents a communicating piece of software.
     * @param name The name of the software such as petpp or body.access.
     * @param version The software version. If not supplied, the latest version of the service will be used.
     */
    public UEntity(String name, Integer version, Short id) {
        Objects.requireNonNull(name, " Software Entity must have a name");
        this.name = name;
        this.id = id;
        this.version = version;
    }

    /**
     * Build an Software Entity that represents a communicating piece of software.
     * @param name The name of the software such as petpp or body.access.
     * @param version The software version. If not supplied, the latest version of the service will be used.
     */
    public UEntity(String name, Integer version) {
        Objects.requireNonNull(name, " Software Entity must have a name");
        this.name = name;
        this.id = null;
        this.version = version;
    }

    /**
     * Static factory method for creating a uE using the application or service name.
     * @param name The application or service name, such as petapp or body.access.
     * @return Returns an UEntity with the name where the version is the latest version of the service.
     */
    public static UEntity fromName(String name) {
        return new UEntity(name, null);
    }


    /**
     * Static factory method for creating a uE using id and version.
     * @param version The software version. If not supplied, the latest version of the service will be used.
     * @param id The software id.
     * @return Returns a UEntity with id but unknown name.
     */
    public static UEntity fromId(Integer version, Short id) {
        Objects.requireNonNull(id, "ID must be supplied");
        return new UEntity(String.valueOf(id), version, id);
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
    public boolean isEmpty() {
        return name.isBlank() && version().isEmpty();
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
     * @return Returns the software id if it exists.
     * 
     */
    public Optional<Short> id() {
        return Optional.ofNullable(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UEntity that = (UEntity) o;
        return Objects.equals(name, that.name) && Objects.equals(version, that.version) && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version, id);
    }

    @Override
    public String toString() {
        return "UEntity{" + "name='" + name + '\''
                + ", version='" + (version == null ? "latest" : version) + '\'' +
                ", id='" + (id == null ? "null" : id) + '\'' + '}';
    }


    /**
     * Return true if the UEntity contains both the name and IDs meaning it is resolved
     * UEntity. Resolved UEntity contains name and id when the name and ID are not the same
     * @return  Returns true of this resource contains resolved information
     */
    public boolean isResolved() {
        boolean isResolved = !name.isBlank() && (id != null);
        
        try {
            isResolved =  (id != Short.valueOf(name));
        } catch (NumberFormatException e) {
            return isResolved;
        }
        return isResolved;
    }

    /**
     * Check if the UEntity contains Long form URI information (uE name)
     * @return Returns true if the UEntity contains Long form URI information (names)
     */
    public boolean isLongForm() {
        return isResolved() || !name.isBlank();
    }
}
