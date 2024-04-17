/*
 * Copyright (c) 2024 General Motors GTO LLC
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
 * 
 * SPDX-FileType: SOURCE
 * SPDX-FileCopyrightText: 2024 General Motors GTO LLC
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.uprotocol.uri.serializer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.uprotocol.uri.factory.UResourceBuilder;
import org.eclipse.uprotocol.uri.validator.UriValidator;
import org.eclipse.uprotocol.v1.UAuthority;
import org.eclipse.uprotocol.v1.UEntity;
import org.eclipse.uprotocol.v1.UResource;
import org.eclipse.uprotocol.v1.UUri;

import com.google.protobuf.ByteString;

/**
 * UUri Serializer that serializes a UUri to a Short format string per
 * https://github.com/eclipse-uprotocol/uprotocol-spec/blob/main/basics/uri.adoc
 */
public class ShortUriSerializer implements UriSerializer<String> {

    private static final ShortUriSerializer INSTANCE = new ShortUriSerializer();

    private ShortUriSerializer() {
    }

    public static ShortUriSerializer instance() {
        return INSTANCE;
    }

    /**
     * Support for serializing {@link UUri} objects into their String format.
     * 
     * @param uri {@link UUri} object to be serialized to the String format.
     * @return Returns the String format of the supplied {@link UUri} that can be
     *         used as a sink or a source in a uProtocol publish communication.
     */
    @Override
    public String serialize(UUri uri) {
        if (uri == null || UriValidator.isEmpty(uri)) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        if (uri.hasAuthority()) {
            UAuthority authority = uri.getAuthority();

            if (authority.hasIp()) {
                try {
                    InetAddress ipAddress = InetAddress.getByAddress(authority.getIp().toByteArray());
                    sb.append("//");
                    sb.append(ipAddress.getHostAddress());
                } catch (UnknownHostException e) {
                    return "";
                }
            } else if (authority.hasId()) {
                sb.append("//");
                sb.append(authority.getId().toStringUtf8());
            }
            // Missing IP and ID, only has name
            else {
                return "";
            }
        }
        sb.append("/");

        sb.append(buildSoftwareEntityPartOfUri(uri.getEntity()));

        sb.append(buildResourcePartOfUri(uri));

        return sb.toString().replaceAll("/+$", "");
    }

    private static String buildResourcePartOfUri(UUri uri) {
        if (!uri.hasResource()) {
            return "";
        }
        final UResource uResource = uri.getResource();

        StringBuilder sb = new StringBuilder("/");
        sb.append(uResource.getId());

        return sb.toString();
    }

    /**
     * Create the service part of the uProtocol URI from an software entity object.
     * 
     * @param use Software Entity representing a service or an application.
     */
    private static String buildSoftwareEntityPartOfUri(UEntity use) {
        StringBuilder sb = new StringBuilder();
        sb.append(use.getId());
        sb.append("/");
        if (use.getVersionMajor() > 0) {
            sb.append(use.getVersionMajor());
        }

        return sb.toString();
    }

    /**
     * Deserialize a String into a UUri object.
     * 
     * @param uProtocolUri A short format uProtocol URI.
     * @return Returns an UUri data object.
     */
    @Override
    public UUri deserialize(String uProtocolUri) {
        if (uProtocolUri == null || uProtocolUri.isBlank()) {
            return UUri.getDefaultInstance();
        }

        String uri = uProtocolUri.contains(":") ? uProtocolUri.substring(uProtocolUri.indexOf(":") + 1)
                : uProtocolUri
                        .replace('\\', '/');

        boolean isLocal = !uri.startsWith("//");

        final String[] uriParts = uri.split("/");
        final int numberOfPartsInUri = uriParts.length;

        if (numberOfPartsInUri < 2) {
            return UUri.getDefaultInstance();
        }

        String uEId = "";
        String ueVersion = "";

        UResource uResource = null;

        UAuthority uAuthority = null;

        if (isLocal) {
            uEId = uriParts[1];
            if (numberOfPartsInUri > 2) {
                ueVersion = uriParts[2];

                if (numberOfPartsInUri > 3) {
                    uResource = parseFromString(uriParts[3]);
                }
                // Too many parts now
                if (numberOfPartsInUri > 4) {
                    return UUri.getDefaultInstance();
                }
            }
        } else {
            // If authority is blank, it is an error
            if (uriParts[2].isBlank()) {
                return UUri.getDefaultInstance();
            }

            // Try if it is an IP address, if not then it must be an ID
            if (IpAddress.isValid(uriParts[2])) {
                uAuthority = UAuthority.newBuilder().setIp(ByteString.copyFrom(IpAddress.toBytes(uriParts[2]))).build();
            } else {
                uAuthority = UAuthority.newBuilder().setId(ByteString.copyFromUtf8(uriParts[2])).build();
            }

            if (uriParts.length > 3) {
                uEId = uriParts[3];
                if (numberOfPartsInUri > 4) {
                    ueVersion = uriParts[4];

                    if (numberOfPartsInUri > 5) {
                        uResource = parseFromString(uriParts[5]);
                    }
                    // Way too many parts in the URI
                    if (numberOfPartsInUri > 6) {
                        return UUri.getDefaultInstance();
                    }
                }
            } else {
                return UUri.newBuilder()
                        .setAuthority(uAuthority)
                        .build();
            }
        }

        Integer useVersionInt = null;
        Integer ueIdInt = null;
        try {
            if (!ueVersion.isBlank()) {
                useVersionInt = Integer.valueOf(ueVersion);
            }

            if (!uEId.isBlank()) {
                ueIdInt = Integer.parseInt(uEId);
            }
        } catch (NumberFormatException ignored) {
            return UUri.getDefaultInstance();
        }

        UEntity.Builder uEntityBuilder = UEntity.newBuilder();

        if (ueIdInt != null) {
            uEntityBuilder.setId(ueIdInt);
        }
        if (useVersionInt != null) {
            uEntityBuilder.setVersionMajor(useVersionInt);
        }

        UUri.Builder uriBuilder = UUri.newBuilder().setEntity(uEntityBuilder);
        if (uAuthority != null) {
            uriBuilder.setAuthority(uAuthority);
        }
        if (uResource != null) {
            uriBuilder.setResource(uResource);
        }
        return uriBuilder.build();
    }

    /**
     * Static factory method for creating a UResource using a string value
     * 
     * @param resourceString String that contains the UResource id.
     * @return Returns a UResource object.
     */
    private static UResource parseFromString(String resourceString) {
        Objects.requireNonNull(resourceString, " Resource must have a command name.");
        Integer id = null;

        try {
            id = Integer.parseInt(resourceString);
        } catch (NumberFormatException ignored) {
            return UResource.getDefaultInstance();
        }

        return UResourceBuilder.fromId(id);
    }

}
