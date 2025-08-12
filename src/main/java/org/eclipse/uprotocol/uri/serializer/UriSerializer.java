/**
 * SPDX-FileCopyrightText: 2024 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.eclipse.uprotocol.uri.serializer;

import org.eclipse.uprotocol.uri.factory.UriFactory;
import org.eclipse.uprotocol.uri.validator.UriValidator;
import org.eclipse.uprotocol.v1.UUri;

/**
 * UUri Serializer that serializes a UUri to a long format string per
 * https://github.com/eclipse-uprotocol/uprotocol-spec/blob/main/basics/uri.adoc.
 */
public interface UriSerializer {



    /**
     * Support for serializing {@link UUri} objects into their String format.
     * 
     * @param uri {@link UUri} object to be serialized to the String format.
     * @return Returns the String format of the supplied {@link UUri} that can be
     *         used as a sink or a source in a uProtocol publish communication.
     */
    static String serialize(UUri uri) {
        if (uri == null || UriValidator.isEmpty(uri)) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        if (!uri.getAuthorityName().isBlank()) {
            sb.append("//");
            sb.append(uri.getAuthorityName());
        }

        sb.append("/");
        sb.append(Integer.toHexString(uri.getUeId()));
        sb.append("/");
        sb.append(Integer.toHexString(uri.getUeVersionMajor()));
        sb.append("/");
        sb.append(Integer.toHexString(uri.getResourceId()));
        return sb.toString().replaceAll("/+$", "");
    }

    /**
     * Deserialize a String into a UUri object.
     * 
     * @param uProtocolUri A long format uProtocol URI.
     * @return Returns an UUri data object.
     */
    static UUri deserialize(String uProtocolUri) {
        if (uProtocolUri == null) {
            return UUri.getDefaultInstance();
        }

        String uri = uProtocolUri.contains(":") ? uProtocolUri.substring(uProtocolUri.indexOf(":") + 1)
                : uProtocolUri
                        .replace('\\', '/');

        boolean isLocal = !uri.startsWith("//");

        final String[] uriParts = uri.split("/");
        final int numberOfPartsInUri = uriParts.length;

        if (numberOfPartsInUri == 0 || numberOfPartsInUri == 1) {
            return UUri.getDefaultInstance();
        }

        UUri.Builder builder = UUri.newBuilder();
        try {
            if (isLocal) {
                builder.setUeId(Integer.parseUnsignedInt(uriParts[1], 16));
                if (numberOfPartsInUri > 2) {
                    builder.setUeVersionMajor(Integer.parseUnsignedInt(uriParts[2], 16));

                    if (numberOfPartsInUri > 3) {
                        builder.setResourceId(Integer.parseUnsignedInt(uriParts[3], 16));
                    }
                }
            } else {
                // If authority is blank, it is an error
                if (uriParts[2].isBlank()) {
                    return UUri.getDefaultInstance();
                }
                builder.setAuthorityName(uriParts[2]);

                if (uriParts.length > 3) {
                    builder.setUeId(Integer.parseUnsignedInt(uriParts[3], 16));
                    if (numberOfPartsInUri > 4) {
                        builder.setUeVersionMajor(Integer.parseUnsignedInt(uriParts[4], 16));

                        if (numberOfPartsInUri > 5) {
                            builder.setResourceId(Integer.parseUnsignedInt(uriParts[5], 16));
                        }

                    }
                }
            }
        } catch (NumberFormatException e) {
            return UUri.getDefaultInstance();
        }

        // Ensure the major version is less than the wildcard
        if (builder.getUeVersionMajor() > UriFactory.WILDCARD_ENTITY_VERSION) {
            return UUri.getDefaultInstance();
        }

        // Ensure the resource id is less than the wildcard
        if (builder.getResourceId() > UriFactory.WILDCARD_ENTITY_ID) {
            return UUri.getDefaultInstance();
        }

        return builder.build();
    }
}
