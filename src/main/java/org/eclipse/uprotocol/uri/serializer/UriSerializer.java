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
 * SPDX-FileType: SOURCE
 * SPDX-FileCopyrightText: 2023 General Motors GTO LLC
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.uprotocol.uri.serializer;


import java.util.Objects;
import org.eclipse.uprotocol.uri.validator.UriValidator;
import org.eclipse.uprotocol.v1.UUri;

/**
 * UUri Serializer that serializes a UUri to a long format string per
 * https://github.com/eclipse-uprotocol/uprotocol-spec/blob/main/basics/uri.adoc
 */
public interface UriSerializer {

    /**
     * Support for serializing {@link UUri} objects into their String format.
     * @param Uri {@link UUri} object to be serialized to the String format.
     * @return Returns the String format of the supplied {@link UUri} that can be used as a sink or a source in a uProtocol publish communication.
     */
    static String serialize(UUri Uri) {
        if (Uri == null || UriValidator.isEmpty(Uri)) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        if (!Uri.getAuthorityName().isBlank()) {
            sb.append("//");
            sb.append(Uri.getAuthorityName());
        }

        sb.append("/");
        sb.append( Uri.getUeId());
        sb.append("/");
        sb.append(Uri.getUeVersionMajor());
        sb.append("/");
        sb.append(Uri.getResourceId());
        return sb.toString().replaceAll("/+$", "");
    }

    /**
     * Deserialize a String into a UUri object.
     * @param uProtocolUri A long format uProtocol URI.
     * @return Returns an UUri data object.
     */
    static UUri deserialize(String uProtocolUri) {
        if (uProtocolUri == null) {
            return UUri.getDefaultInstance();
        }

        String uri = uProtocolUri.contains(":") ? uProtocolUri.substring(uProtocolUri.indexOf(":")+1) : uProtocolUri 
                .replace('\\', '/');
        
        boolean isLocal = !uri.startsWith("//");

        final String[] uriParts = uri.split("/");
        final int numberOfPartsInUri = uriParts.length;

        if(numberOfPartsInUri == 0 || numberOfPartsInUri == 1) {
            return UUri.getDefaultInstance();
        }

        UUri.Builder builder = UUri.newBuilder();
        try {
            if(isLocal) {
                builder.setUeId(Integer.parseUnsignedInt(uriParts[1]));
                if (numberOfPartsInUri > 2) {
                    builder.setUeVersionMajor(Integer.parseUnsignedInt(uriParts[2]));

                    if (numberOfPartsInUri > 3) {
                        builder.setResourceId(Integer.parseUnsignedInt(uriParts[3]));
                    }
                } 
            } else {
                // If authority is blank, it is an error
                if (uriParts[2].isBlank()) {
                    return UUri.getDefaultInstance();
                }
                builder.setAuthorityName(uriParts[2]);

                if (uriParts.length > 3) {
                    builder.setUeId(Integer.parseUnsignedInt(uriParts[3]));
                    if (numberOfPartsInUri > 4) {
                        builder.setUeVersionMajor(Integer.parseUnsignedInt(uriParts[4]));

                        if (numberOfPartsInUri > 5) { 
                            builder.setResourceId(Integer.parseUnsignedInt(uriParts[5]));
                        }

                    } 
                }
            }
        } catch (NumberFormatException e) {
            return UUri.getDefaultInstance();
        }

        // Ensure the major version is less than the wildcard
        if (builder.getUeVersionMajor() > UriValidator.MAJOR_VERSION_WILDCARD) {
            return UUri.getDefaultInstance();
        }

        // Ensure the resource id is less than the wildcard
        if (builder.getResourceId() > UriValidator.WILDCARD_ID) {
            return UUri.getDefaultInstance();
        }

        return builder.build();
    }
}
