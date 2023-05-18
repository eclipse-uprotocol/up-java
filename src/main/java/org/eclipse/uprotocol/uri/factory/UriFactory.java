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

package org.eclipse.uprotocol.uri.factory;

import org.eclipse.uprotocol.uri.datamodel.UAuthority;
import org.eclipse.uprotocol.uri.datamodel.UEntity;
import org.eclipse.uprotocol.uri.datamodel.UResource;
import org.eclipse.uprotocol.uri.datamodel.UUri;

import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A factory is a part of the software has methods to generate concrete objects, usually of the same type or interface.<br>
 * The  URI Factory generates an  URI.
 */
public interface UriFactory {

    Pattern schemaPattern = Pattern.compile("(?i)up:");

    /**
     * Create the uProtocol URI string for source sink and topics from an  URI.
     * 
     * @param Uri The  URI data object.
     * @return Returns the uProtocol URI string from an  URI data object
     *      that can be used as a sink or a source in a uProtocol publish communication.
     */
    static String buildUProtocolUri(UUri Uri) {

        if (Uri == null || Uri.isEmpty()){
            return Uri.SCHEME;
        }

        StringBuilder sb = new StringBuilder(Uri.SCHEME);

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
     * Create the uProtocol URI string for source sink and topics from the separate parts
     * of an  URI.
     *
     * @param uAuthority The  Authority represents the deployment location of a specific  Software Entity in the Ultiverse.
     * @param uEntity The  Software Entity in the role of a service or in the role of an application.
     * @param uResource The resource is something that is manipulated by a service such as a Door.
     *
     * @return Returns the uProtocol URI string from an  URI data object
     *      that can be used as a sink or a source in a uProtocol publish communication.
     */
    static String buildUProtocolUri(UAuthority uAuthority, UEntity uEntity, UResource uResource) {
        return buildUProtocolUri(new UUri(uAuthority, uEntity, uResource));
    }

    /**
     * Create the uProtocol URI string for the source or sink of the CloudEvent that represents an RPC request.
     * Use this to generate the URI for the  software entity who originated the RPC call.
     * As specified in SDV-202 Request for the source and SDV-202 Response for the sink.
     * 
     * @param uAuthority       The uAuthority of the  software entity requesting the RPC.
     * @param uEntitySource    The  software entity requesting the RPC.
     * @return Returns the uProtocol URI string that can be used in a CloudEvent representing the application making an RPC call to a service.
     */
    static String buildUriForRpc(UAuthority uAuthority,
                                 UEntity uEntitySource) {
        StringBuilder sb = new StringBuilder(UUri.SCHEME);

        sb.append(buildAuthorityPartOfUri(uAuthority));
        if (uAuthority.isMarkedRemote()) {
            sb.append("/");
        }
        sb.append(buildSoftwareEntityPartOfUri(uEntitySource));
        sb.append("/rpc.response");

        return sb.toString();
    }

    /**
     * Create the uProtocol URI string for the sink of the CloudEvent that represents an RPC request
     * or the source of the CloudEvent that represents an RPC response.
     * @param uAuthority        The uAuthority of the  software entity service accepting the RPC.
     * @param uEntity           The  software entity service accepting the RPC.
     * @param methodName        The name of the RPC method on the service such as UpdateDoor.
     * @return Returns          Returns the uProtocol URI string for the sink of the CloudEvent that represents
     * an RPC request or the source of the CloudEvent that represents an RPC response for RPC scenarios.
     */
    static String buildMethodUri(UAuthority uAuthority, UEntity uEntity, String methodName) {
        StringBuilder sb = new StringBuilder(UUri.SCHEME);

        sb.append(buildAuthorityPartOfUri(uAuthority));
        if (uAuthority.isMarkedRemote()) {
            sb.append("/");
        }
        sb.append(buildSoftwareEntityPartOfUri(uEntity));

        sb.append(buildResourcePartOfUri(UResource.forRpc(methodName)));

        return sb.toString();
    }

    private static String buildResourcePartOfUri(UResource uResource) {
        if (uResource.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder("/").append(uResource.name());
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
     * Create an  URI data object from a uProtocol string.
     * @param uProtocolUri A String uProtocol URI.
     * @return Returns an  URI data object.
     */
    static UUri parseFromUri(String uProtocolUri) {
        if (uProtocolUri == null || uProtocolUri.isBlank()) {
            return UUri.empty();
        }

        String uri = schemaPattern.matcher(uProtocolUri)
                .replaceFirst("")
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

                uResource = numberOfPartsInUri > 3 ? buildResource(uriParts[3]) : UResource.empty();

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

                    uResource = numberOfPartsInUri > 5 ? buildResource(uriParts[5]) : UResource.empty();

                } else {
                    uResource = UResource.empty();
                }
            } else {
                return new UUri(uAuthority, UEntity.empty(), UResource.empty());
            }

        }

        return new UUri(uAuthority, new UEntity(useName, useVersion), uResource);
    }

    private static UResource buildResource(String resourceString) {
        String[] parts = resourceString.split("#");
        String nameAndInstance = parts[0];
        String[] nameAndInstanceParts = nameAndInstance.split("\\.");
        String resourceName = nameAndInstanceParts[0];
        String resourceInstance = nameAndInstanceParts.length > 1 ? nameAndInstanceParts[1] : null;
        String resourceMessage = parts.length > 1 ? parts[1] : null;
        return new UResource(resourceName, resourceInstance, resourceMessage);
    }

}
