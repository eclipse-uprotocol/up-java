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

package org.eclipse.uprotocol.uri.serializer;

import org.eclipse.uprotocol.uri.datamodel.UAuthority;
import org.eclipse.uprotocol.uri.datamodel.UEntity;
import org.eclipse.uprotocol.uri.datamodel.UResource;
import org.eclipse.uprotocol.uri.datamodel.UUri;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * UUri Serializer that serializes a UUri to a short format per
 * https://github.com/eclipse-uprotocol/uprotocol-spec/blob/main/basics/uri.adoc
 */
public class ShortUriSerializer implements UriSerializer<String> {

    private static final String SCHEME = "s:"; // Required Scheme

    private static final ShortUriSerializer INSTANCE = new ShortUriSerializer();

    private ShortUriSerializer(){}

    public static ShortUriSerializer instance() {
        return INSTANCE;
    }

    /**
     * Support for serializing {@link UUri} objects into the short URI format.
     * @param Uri {@link UUri} object to be serialized to the short URI format.
     * @return Returns the short URI formatted string of the supplied {@link UUri} that can be used as a 
     * sink or a source in a uProtocol publish communication.
     */
    @Override
    public String serialize(UUri Uri) {
        if (Uri == null || Uri.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder(SCHEME);

        sb.append(buildAuthorityPartOfUri(Uri.uAuthority()));

        if (Uri.uAuthority().isMarkedRemote()) {
            sb.append("/");
        }

        if (Uri.uEntity().isEmpty()) {
            return sb.toString();
        }

        sb.append(buildSoftwareEntityPartOfUri(Uri.uEntity()));
        
        sb.append(buildResourcePartOfUri(Uri.uResource()));

        return sb.toString().replaceAll("/+$", "");
    }

    /**
     * Deserialize a Short formatted string into a UUri object.
     * @param uProtocolUri A short format uProtocol URI.
     * @return Returns an UUri data object.
     */
    @Override
    public UUri deserialize(String uProtocolUri) {
        if (uProtocolUri == null || uProtocolUri.isBlank() || !uProtocolUri.contains(SCHEME)) {
            return UUri.empty();
        }

        String uri = uProtocolUri.substring(uProtocolUri.indexOf(":")+1).replace('\\', '/');
        
        boolean isLocal = !uri.startsWith("//");

        final String[] uriParts = uri.split("/");
        final int numberOfPartsInUri = uriParts.length;

        if(numberOfPartsInUri == 0 || numberOfPartsInUri == 1) {
            return isLocal ? UUri.empty() :
                    new UUri(UAuthority.longRemote("", ""), UEntity.empty(), UResource.empty());
        }

        String useName = "";
        String useVersion = "";
        UResource uResource;
        UAuthority uAuthority;
        String[] authorityParts = uriParts[2].split("\\.");
        String device = authorityParts[0];
        String domain = "";

        if (authorityParts.length > 1) {
            domain = Arrays.stream(authorityParts)
                    .skip(1)
                    .collect(Collectors.joining("."));
        }
        uAuthority = UAuthority.longRemote(device, domain);

        if (uriParts.length > 3) {
            useName = uriParts[3];
            if (numberOfPartsInUri > 4) {
                useVersion = uriParts[4];

                if (numberOfPartsInUri > 5) {
                    try {
                        Short resourceId = Short.valueOf(uriParts[5]);
                        uResource = UResource.microFormat(resourceId);
                    } catch (NumberFormatException ignored) {
                        return UUri.empty();
                    }
                } else {
                    uResource = UResource.empty();
                }
            } else {
                uResource = UResource.empty();
            }

        } else {
            return new UUri(uAuthority, UEntity.empty(), UResource.empty());
        }

        Integer useVersionInt = null;
        try {
            if (!useVersion.isBlank()) {
                useVersionInt = Integer.valueOf(useVersion);
            }
        } catch (NumberFormatException ignored) {
            return UUri.empty();
        }

        Short useId = null;
        try {
            if (!useName.isBlank()) {
                useId = Short.valueOf(useName);
            }
        } catch (NumberFormatException ignored) {
            return UUri.empty();
        }
        
        return new UUri(uAuthority, UEntity.microFormat(useId, useVersionInt), uResource);
    }


    private static String buildResourcePartOfUri(UResource uResource) {
        if (uResource.isEmpty() || !uResource.isMicroForm()) {
            return "";
        }
        StringBuilder sb = new StringBuilder("/");
        uResource.id().ifPresent(id -> sb.append(id));

        return sb.toString();
    }

    
    private static String buildSoftwareEntityPartOfUri(UEntity use) {
        StringBuilder sb = new StringBuilder();
        use.id().ifPresent(sb::append);
        sb.append("/");
        use.version().ifPresent(sb::append);
        return sb.toString();
    }


    private static String buildAuthorityPartOfUri(UAuthority Authority) {
        if (Authority.isLocal()) {
            return "/";
        }
        StringBuilder partialURI = new StringBuilder("//");
        final Optional<String> maybeDevice = Authority.device();
        final Optional<String> maybeDomain = Authority.domain();

        if (maybeDevice.isPresent()) {
            partialURI.append(maybeDevice.get());
            maybeDomain.ifPresent(domain -> partialURI.append("."));
        }
        maybeDomain.ifPresent(partialURI::append);

        return partialURI.toString();
    }
}
