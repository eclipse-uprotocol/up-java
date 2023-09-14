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
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * UUri Serializer that serializes a UUri to a string (long or long form) per
 *  https://github.com/eclipse-uprotocol/uprotocol-spec/blob/main/basics/uri.adoc
 */
public class StringUriSerializer implements UriSerializer<String> {

    /**
     * Serialize the UUri object Into a string in Long Form.
     * 
     * @param Uri The  URI data object.
     * @return Returns the uProtocol URI string from an  URI data object
     *      that can be used as a sink or a source in a uProtocol publish communication.
     */
    @Override
    public String serialize(UUri Uri) {
        if (Uri == null || Uri.isEmpty()) {
            return new String();
        }

        StringBuilder sb = new StringBuilder();

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

    private static String buildResourcePartOfUri(UResource uResource) {
        if (uResource.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder("/");
        sb.append(uResource.name());
        uResource.instance().ifPresent(instance -> sb.append(".").append(instance));
        uResource.message().ifPresent(message -> sb.append("#").append(message));

        return sb.toString();
    }

    /**
     * Create the service part of the uProtocol URI from an  software entity object.
     * @param use  Software Entity representing a service or an application.
     */
    private static String buildSoftwareEntityPartOfUri(UEntity use) {
        StringBuilder sb = new StringBuilder(use.name().trim());
        sb.append("/");
        use.version().ifPresent(sb::append);

        return sb.toString();
    }


    /**
     * Create the authority part of the uProtocol URI from an  authority object.
     * @param Authority represents the deployment location of a specific  Software Entity in the Ultiverse.
     * @return Returns the String representation of the  Authority in the uProtocol URI.
     */
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

    /**
     * Deserialize a String into a UUri object.
     * @param uProtocolUri A long format uProtocol URI.
     * @return Returns an UUri data object.
     */
    @Override
    public UUri deserialize(String uProtocolUri) {
        if (uProtocolUri == null || uProtocolUri.isBlank()) {
            return UUri.empty();
        }

        String uri = uProtocolUri.contains(":") ? uProtocolUri.substring(uProtocolUri.indexOf(":")+1) : uProtocolUri 
                .replace('\\', '/');
        
        boolean isLocal = !uri.startsWith("//");

        final String[] uriParts = uri.split("/");
        final int numberOfPartsInUri = uriParts.length;

        if(numberOfPartsInUri == 0 || numberOfPartsInUri == 1) {
            return isLocal ? UUri.empty() :
                    new UUri(UAuthority.remote("", ""), UEntity.empty(), UResource.empty());
        }

        String useName;
        String useVersion = "";

        UResource uResource;

        UAuthority uAuthority;
        if(isLocal) {
            uAuthority = UAuthority.local();
            useName = uriParts[1];
            if (numberOfPartsInUri > 2) {
                useVersion = uriParts[2];

                uResource = numberOfPartsInUri > 3 ? UResource.parseFromString(uriParts[3]) : UResource.empty();

            } else {
                uResource = UResource.empty();
            }
        } else {
            String[] authorityParts = uriParts[2].split("\\.");
            String device = authorityParts[0];
            String domain = "";
            if (authorityParts.length > 1) {
                domain = Arrays.stream(authorityParts)
                        .skip(1)
                        .collect(Collectors.joining("."));
            }
            uAuthority = UAuthority.remote(device, domain);

            if (uriParts.length > 3) {
                useName = uriParts[3];
                if (numberOfPartsInUri > 4) {
                    useVersion = uriParts[4];

                    uResource = numberOfPartsInUri > 5 ? UResource.parseFromString(uriParts[5]) : UResource.empty();

                } else {
                    uResource = UResource.empty();
                }
            } else {
                return new UUri(uAuthority, UEntity.empty(), UResource.empty());
            }

        }

        Integer useVersionInt = null;
        try {
            if (!useVersion.isBlank()) {
                useVersionInt = Integer.valueOf(useVersion);
            }
        } catch (NumberFormatException e) {
            useVersionInt = null;
        }

        return new UUri(uAuthority, new UEntity(useName, useVersionInt), uResource);
    }

}
