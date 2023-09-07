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

package org.eclipse.uprotocol.uri.factory;

import org.eclipse.uprotocol.uri.datamodel.UAuthority;
import org.eclipse.uprotocol.uri.datamodel.UEntity;
import org.eclipse.uprotocol.uri.datamodel.UResource;
import org.eclipse.uprotocol.uri.datamodel.UUri;
import org.eclipse.uprotocol.uri.datamodel.UAuthority.AddressType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * UUri Factory used to build different types of UUri (long, short, micro), and UUri objects themselves
 * for the various use cases found in uProtocol specifications.
 * For more information, please refer to https://github.com/eclipse-uprotocol/uprotocol-spec/blob/main/basics/uri.adoc
 */
public interface UriFactory {

    static final int LOCAL_MICRO_URI_LENGTH = 8; // local micro URI length

    static final int IPV4_MICRO_URI_LENGTH = 12; // IPv4 micro URI length 

    static final int IPV6_MICRO_URI_LENGTH = 24; // IPv6 micro Uri length


    /**
     * Build a Long-URI string from the separate parts of an  URI.
     * 
     * @param Uri The  URI data object.
     * @return Returns the uProtocol URI string from an  URI data object
     *      that can be used as a sink or a source in a uProtocol publish communication.
     */
    static String buildUProtocolUri(UUri Uri) {
        if (Uri == null || Uri.isEmpty()) {
            return new String();
        }

        StringBuilder sb = new StringBuilder();

        sb.append(buildAuthorityPartOfUri(Uri.uAuthority(), false));

        if (Uri.uAuthority().isMarkedRemote()) {
            sb.append("/");
        }

        if (Uri.uEntity().isEmpty()) {
            return sb.toString();
        }

        sb.append(buildSoftwareEntityPartOfUri(Uri.uEntity(), false));
        
        sb.append(buildResourcePartOfUri(Uri.uResource(), false));

        return sb.toString().replaceAll("/+$", "");
    }

    /**
     * Build a Long-Uri string using the separate parts of an URI.
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
     * Build a Short-Uri string from a UUri object
     * 
     * @param Uri The  URI data object.
     * @return Returns the short form uProtocol URI string from an  URI data object 
     */
    static String buildUProtocolShortUri(UUri Uri) {
        if (Uri == null || Uri.isEmpty()) {
            return new String();
        }

        StringBuilder sb = new StringBuilder();

        sb.append(buildAuthorityPartOfUri(Uri.uAuthority(), true));

        if (Uri.uAuthority().isMarkedRemote()) {
            sb.append("/");
        }

        if (Uri.uEntity().id().isEmpty()) {
            return sb.toString();
        }

        sb.append(buildSoftwareEntityPartOfUri(Uri.uEntity(), true));
        
        sb.append(buildResourcePartOfUri(Uri.uResource(), true));

        return sb.toString().replaceAll("/+$", "");
    }

    /**
     * Build a Short-Uri string using the separate parts of an URI.
     * of an  URI.
     *
     * @param uAuthority The  Authority represents the deployment location of a specific  Software Entity in the Ultiverse.
     * @param uEntity The  Software Entity in the role of a service or in the role of an application.
     * @param uResource The resource is something that is manipulated by a service such as a Door.
     *
     * @return Returns the uProtocol URI string from an  URI data object
     *      that can be used as a sink or a source in a uProtocol publish communication.
     */
    static String buildUProtocolShortUri(UAuthority uAuthority, UEntity uEntity, UResource uResource) {
        return buildUProtocolShortUri(new UUri(uAuthority, uEntity, uResource));
    }

    /**
     * Build a Micro-URI byte[] using a passed UUri object
     * 
     * @param Uri The  URI data object.
     * @return Returns the short form uProtocol URI string from an  URI data object 
     */
    static byte[] buildUProtocolMicroUri(UUri Uri) {
        if (Uri == null || Uri.isEmpty()) {
            return new byte[0];
        }

        Optional<InetAddress> maybeAddress = Uri.uAuthority().address();
        Optional<Short> maybeUeId = Uri.uEntity().id();
        Optional<Short> maybeUResourceId = Uri.uResource().id();
        if (!maybeUeId.isPresent() || !maybeUResourceId.isPresent()) {
            return new byte[0];
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        // UP_VERSION
        os.write(0x1);

        // TYPE
        if (Uri.uAuthority().isLocal()) {
            os.write(0x0);
        } else {
            os.write(maybeAddress.get() instanceof Inet4Address ? 1 : 2);
        }

        // URESOURCE_ID
        os.write(maybeUResourceId.get()>>8);
        os.write(maybeUResourceId.get());

            // UAUTHORITY_ADDRESS
        if (!Uri.uAuthority().isLocal()) {
            try {
                os.write(maybeAddress.get().getAddress());
            } catch (IOException e) {
                return new byte[0];
            }
        }

        // UENTITY_ID
        os.write(maybeUeId.get()>>8);
        os.write(maybeUeId.get());
        
        // UENTITY_VERSION
        String version = Uri.uEntity().version().orElse("");
        if (version.isEmpty()) {
            os.write((byte)Short.MAX_VALUE>>8);
            os.write((byte)Short.MAX_VALUE);
        } else {
            String[] parts = version.split("\\.");
            if (parts.length > 1) {
                int major = (Integer.parseInt(parts[0]) << 3) + (Integer.parseInt(parts[1]) >> 8);
                os.write((byte)major);
                os.write((byte)Integer.parseInt(parts[1]));
            } else {
                os.write(Integer.parseInt(parts[0])<<3);
                os.write(0);
            }
        }
        return os.toByteArray();
        
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
        StringBuilder sb = new StringBuilder();

        sb.append(buildAuthorityPartOfUri(uAuthority, false));
        if (uAuthority.isMarkedRemote()) {
            sb.append("/");
        }
        sb.append(buildSoftwareEntityPartOfUri(uEntitySource, false));
        sb.append("/");
        sb.append(UResource.response().nameWithInstance());

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
        StringBuilder sb = new StringBuilder();

        sb.append(buildAuthorityPartOfUri(uAuthority, false));
        if (uAuthority.isMarkedRemote()) {
            sb.append("/");
        }
        sb.append(buildSoftwareEntityPartOfUri(uEntity, false));

        sb.append(buildResourcePartOfUri(UResource.forRpc(methodName), false));

        return sb.toString();
    }

    private static String buildResourcePartOfUri(UResource uResource, boolean shortUri) {
        if (uResource.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder("/");
        if (shortUri) {
            uResource.id().ifPresent(sb::append);
        } else {
            sb.append(uResource.name());
            uResource.instance().ifPresent(instance -> sb.append(".").append(instance));
            uResource.message().ifPresent(message -> sb.append("#").append(message));
        }

        return sb.toString();
    }

    /**
     * Create the service part of the uProtocol URI from an  software entity object.
     * @param use  Software Entity representing a service or an application.
     */
    private static String buildSoftwareEntityPartOfUri(UEntity use, boolean shortUri) {
        StringBuilder sb = new StringBuilder(shortUri? use.id().get().toString() : use.name().trim());
        sb.append("/");
        use.version().ifPresent(sb::append);

        return sb.toString();
    }


    /**
     * Create the authority part of the uProtocol URI from an  authority object.
     * @param Authority represents the deployment location of a specific  Software Entity in the Ultiverse.
     * @return Returns the String representation of the  Authority in the uProtocol URI.
     */
    private static String buildAuthorityPartOfUri(UAuthority Authority, boolean shortUri) {
        if (Authority.isLocal()) {
            return "/";
        }
        StringBuilder partialURI = new StringBuilder("//");
        if (shortUri) {
            final Optional<InetAddress> maybeAddress = Authority.address();
            if (maybeAddress.isPresent()) {
                partialURI.append(maybeAddress.get().getHostAddress());
            }
            return partialURI.toString();
        }
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
     * Create an  URI data object from a uProtocol string (long or short).
     * @param uProtocolUri A String uProtocol URI.
     * @return Returns an  URI data object.
     */
    static UUri parseFromUri(String uProtocolUri) {
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

        Short maybeUeId = null;
        if (!useName.isEmpty()) {
            try {
                maybeUeId = Short.parseShort(useName);
            } catch (NumberFormatException e) {
                maybeUeId = null;
            }
        }
        return new UUri(uAuthority, new UEntity(useName, useVersion, maybeUeId), uResource);
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


    /**
     * Create an  URI data object from a uProtocol micro URI.
     * @param microUri A byte[] uProtocol micro URI.
     * @return Returns an  URI data object.
     */
    static UUri parseFromMicroUri(byte[] microUri) {
        if (microUri == null || microUri.length < UriFactory.LOCAL_MICRO_URI_LENGTH ) {
            return UUri.empty();
        }

        // Need to be version 1
        if (microUri[0] != 0x1) {
            return UUri.empty();
        }

        int uResourceId = ((microUri[2] & 0xFF) << 8) | (microUri[3] & 0xFF);

        Optional<InetAddress> maybeAddress = Optional.empty();
        
        Optional<AddressType> type = AddressType.from(microUri[1]);

        if (!type.isPresent()) {
            return UUri.empty();
        }

        int index = 4;
        if (!(type.get() == AddressType.LOCAL)) {
            try {
                maybeAddress = Optional.of(InetAddress.getByAddress(
                    Arrays.copyOfRange(microUri, index, (type.get() == AddressType.IPv4) ? 8 : 20)));
            } catch (Exception e) {
                maybeAddress = Optional.empty();
            }
            index += type.get() == AddressType.IPv4 ? 4 : 16;
        }
        
        int ueId = ((microUri[index++] & 0xFF) << 8) | (microUri[index++] & 0xFF);

        int ueVersion = ((microUri[index++] & 0xFF) << 8) | (microUri[index++] & 0xFF);

        String ueVersionString = String.valueOf(ueVersion >> 11);
        if ((ueVersion & 0x7FF) != 0) {
            ueVersionString += "." + (ueVersion & 0x7FF);
        }

        return new UUri((type.get() == AddressType.LOCAL) ? UAuthority.local() : UAuthority.remote(maybeAddress.get()),
                new UEntity("", ueVersionString, (short)ueId),
                UResource.fromId((short)uResourceId));
    }
        

}
