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
import org.eclipse.uprotocol.uri.datamodel.UAuthority.AddressType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Optional;

/**
 * UUri Factory used to build different types of UUri (long, short, micro), and UUri objects themselves
 * for the various use cases found in uProtocol specifications.
 * For more information, please refer to https://github.com/eclipse-uprotocol/uprotocol-spec/blob/main/basics/uri.adoc
 */
public class MicroUriSerializer implements UriSerializer<byte[]> {

    static final int LOCAL_MICRO_URI_LENGTH = 8; // local micro URI length

    static final int IPV4_MICRO_URI_LENGTH = 12; // IPv4 micro URI length 

    static final int IPV6_MICRO_URI_LENGTH = 24; // IPv6 micro Uri length


    /**
     * Serialize a UUri into a byte[] following the Micro-URI specifications
     * 
     * @param Uri The  URI data object.
     * @return Returns the serialized URI into a byte[] 
     */
    @Override
    public byte[] serialize(UUri Uri) {
        if (Uri == null || Uri.isEmpty()) {
            return new byte[0];
        }

        Optional<InetAddress> maybeAddress = Uri.uAuthority().address();
        Optional<Short> maybeUeId = Uri.uEntity().id();
        Optional<Short> maybeUResourceId = Uri.uResource().id();
        if (!maybeUeId.isPresent() || !maybeUResourceId.isPresent()) {
            return new byte[0];
        }

        // Remote Uri but the address is missing
        if (!maybeAddress.isPresent() && Uri.uAuthority().isRemote()) {
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
            os.write((byte)0);
            os.write((byte)0);
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
     * Deserialize a byte[] into a UUri object
     * @param microUri A byte[] uProtocol micro URI.
     * @return Returns an  URI data object.
     */
    @Override
    public UUri deserialize(byte[] microUri) {
        if (microUri == null || microUri.length < LOCAL_MICRO_URI_LENGTH ) {
            return UUri.empty();
        }

        // Need to be version 1
        if (microUri[0] != 0x1) {
            return UUri.empty();
        }

        int uResourceId = ((microUri[2] & 0xFF) << 8) | (microUri[3] & 0xFF);

        Optional<InetAddress> maybeAddress = Optional.empty();
        
        Optional<AddressType> type = AddressType.from(microUri[1]);
 
        // Validate Type is found
        if (!type.isPresent()) {
            return UUri.empty();
        }

        // Validate that the microUri is the correct length for the type
        if (type.get() == AddressType.LOCAL && microUri.length != LOCAL_MICRO_URI_LENGTH) {
            return UUri.empty();
        }
        else if (type.get() == AddressType.IPv4 && microUri.length != IPV4_MICRO_URI_LENGTH) {
            return UUri.empty();
        }
        else if (type.get() == AddressType.IPv6 && microUri.length != IPV6_MICRO_URI_LENGTH) {
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
        
        if (ueVersion == 0) {
            ueVersionString = null; // no version provided
        }
        else if ((ueVersion & 0x7FF) != 0) {
            ueVersionString += "." + (ueVersion & 0x7FF);
        }

        return new UUri((type.get() == AddressType.LOCAL) ? UAuthority.local() : UAuthority.remote(maybeAddress.get()),
                UEntity.fromId(ueVersionString, (short)ueId),
                UResource.fromId((short)uResourceId));
    }    

}
