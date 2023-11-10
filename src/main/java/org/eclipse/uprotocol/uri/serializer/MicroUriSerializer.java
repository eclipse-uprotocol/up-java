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


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import org.eclipse.uprotocol.uri.builder.UResourceBuilder;
import org.eclipse.uprotocol.uri.validator.UriValidator;
import org.eclipse.uprotocol.v1.UUri;
import org.eclipse.uprotocol.v1.UAuthority;
import org.eclipse.uprotocol.v1.UEntity;
import com.google.protobuf.ByteString;

/**
 * UUri Serializer that serializes a UUri to a byte[] (micro format) per
 * <a href="https://github.com/eclipse-uprotocol/uprotocol-spec/blob/main/basics/uri.adoc">...</a>
 */
public class MicroUriSerializer implements UriSerializer<byte[]> {

    static final int LOCAL_MICRO_URI_LENGTH = 8; // local micro URI length

    static final int IPV4_MICRO_URI_LENGTH = 12; // IPv4 micro URI length 

    static final int IPV6_MICRO_URI_LENGTH = 24; // IPv6 micro UriPart length

    static final byte UP_VERSION = 0x1; // UP version

    private static final MicroUriSerializer INSTANCE = new MicroUriSerializer();

    private MicroUriSerializer(){}

    public static MicroUriSerializer instance() {
        return INSTANCE;
    }

    /**
     * The type of address used for Micro URI.
     */
    private enum AddressType {
        LOCAL(0),
        IPv4(1),
        IPv6(2),
        ID(3);

        private final int value;

        AddressType(int value) {
            this.value = value;
        }

        public byte getValue() {
            return (byte)value;
        }

        public static Optional<AddressType> from(int value) {
            return Arrays.stream(AddressType.values())
                    .filter(p -> p.getValue() == value)
                    .findAny();
        }
    }

    /**
     * Serialize a UUri into a byte[] following the Micro-URI specifications.
     * @param Uri The {@link UUri} data object.
     * @return Returns a byte[] representing the serialized {@link UUri}.
     */
    @Override
    public byte[] serialize(UUri Uri) {
        AddressType type = AddressType.LOCAL;

        if (Uri == null || UriValidator.isEmpty(Uri) || !UriValidator.isMicroForm(Uri)) {
            return new byte[0];
        }

        Optional<Integer> maybeUeId = Optional.ofNullable(Uri.getEntity().getId());
        Optional<Integer> maybeUResourceId = Optional.ofNullable(Uri.getResource().getId());

 
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        // UP_VERSION
        os.write(UP_VERSION);

        // Determine the uAuthority type to be written
        switch (Uri.getAuthority().getRemoteCase()) {
            case REMOTE_NOT_SET: 
                type = AddressType.LOCAL;
                break;
            case IP:
                final Integer length = Uri.getAuthority().getIp().size();
                if (length == 4) {
                    type = AddressType.IPv4;
                } else if (length == 16) {
                    type = AddressType.IPv6;
                } else {
                    return new byte[0];
                }
                break;

            case ID:
                type = AddressType.ID;
                break;
            
            default:
                return new byte[0];
        }

        os.write(type.getValue());

        // URESOURCE_ID
        os.write(maybeUResourceId.get()>>8);
        os.write(maybeUResourceId.get());

        // UENTITY_ID
        os.write(maybeUeId.get()>>8);
        os.write(maybeUeId.get());

        // UE_VERSION
        os.write(Uri.getEntity().getVersionMajor() == 0 ? (byte)0 : Uri.getEntity().getVersionMajor());

        // UNUSED
        os.write((byte)0);


        // Populating the UAuthority
        if (type != AddressType.LOCAL) {

            // Write the ID length if the type is ID
            if (type == AddressType.ID) {
                os.write(Uri.getAuthority().getId().size());
            }
                
            try {
                switch(Uri.getAuthority().getRemoteCase()) {
                    case IP:
                        os.write(Uri.getAuthority().getIp().toByteArray());
                        break;
                    case ID:
                        os.write(Uri.getAuthority().getId().toByteArray());
                        break;
                    default:
                        break;
                }
                
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return os.toByteArray();

    }


    /**
     * Deserialize a byte[] into a {@link UUri} object.
     * @param microUri A byte[] uProtocol micro URI.
     * @return Returns an {@link UUri} data object from the serialized format of a microUri.
     */
    @Override
    public UUri deserialize(byte[] microUri) {
        if (microUri == null || microUri.length < LOCAL_MICRO_URI_LENGTH ) {
            return UUri.getDefaultInstance();
        }

        // Need to be version 1
        if (microUri[0] != 0x1) {
            return UUri.getDefaultInstance();
        }

        int uResourceId = ((microUri[2] & 0xFF) << 8) | (microUri[3] & 0xFF);
        
        Optional<AddressType> type = AddressType.from(microUri[1]);
 
        // Validate Type is found
        if (type.isEmpty()) {
            return UUri.getDefaultInstance();
        }

        // Validate that the microUri is the correct length for the type
        final AddressType addressType = type.get();
        if (addressType == AddressType.LOCAL && microUri.length != LOCAL_MICRO_URI_LENGTH) {
            return UUri.getDefaultInstance();
        }
        else if (addressType == AddressType.IPv4 && microUri.length != IPV4_MICRO_URI_LENGTH) {
            return UUri.getDefaultInstance();
        }
        else if (addressType == AddressType.IPv6 && microUri.length != IPV6_MICRO_URI_LENGTH) {
            return UUri.getDefaultInstance();
        }

        // UENTITY_ID
        int ueId = ((microUri[4] & 0xFF) << 8) | (microUri[5] & 0xFF);

        // UE_VERSION
        int uiVersion = Byte.toUnsignedInt(microUri[6]);

        // Calculate uAuthority
        UAuthority uAuthority = null;
        switch (addressType) {
            case IPv4:
            case IPv6:
                uAuthority = UAuthority.newBuilder().setIp(ByteString.copyFrom(microUri, 8, 
                    addressType == AddressType.IPv4 ? 4 : 16)).build();
                break;
            case ID:
                int length = Byte.toUnsignedInt(microUri[8]);
                uAuthority = UAuthority.newBuilder().setId(ByteString.copyFrom(microUri, 9, 
                    length)).build();
                break;
            default:
                break;
        }

        UUri.Builder uriBuilder = UUri.newBuilder()
                .setEntity(UEntity.newBuilder()
                    .setId(ueId)
                    .setVersionMajor(uiVersion))
                .setResource(UResourceBuilder.fromId(uResourceId));

        if (uAuthority != null) {
            uriBuilder.setAuthority(uAuthority);
        }

        return uriBuilder.build();
    }

}
