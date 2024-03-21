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


public interface IpAddress {
    
    static byte[] toBytes(String ipAddress) {
        if (ipAddress == null || ipAddress.isEmpty()) {
            return new byte[0];
        }

        if (isValidIPv4Address(ipAddress)) {
            return convertIPv4ToByteArray(ipAddress);
        } else if (isValidIPv6Address(ipAddress)) {
            return convertIPv6ToByteArray(ipAddress);
        } else {
            return new byte[0];
        }
    }

    static boolean isValid(String ipAddress) {
        return (ipAddress != null) &&
            !ipAddress.isEmpty() &&
            (isValidIPv4Address(ipAddress) || isValidIPv6Address(ipAddress));
    }

    private static boolean isValidIPv4Address(String ipAddress) {
        String[] octets = ipAddress.split("\\.");

        if (octets.length != 4) {
            return false;
        }

        for (String octet : octets) {
            try {
                int value = Integer.parseInt(octet);
                if (value < 0 || value > 255) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }

        return true;
    }

    private static byte[] convertIPv4ToByteArray(String ipAddress) {
        String[] octets = ipAddress.split("\\.");
        byte[] ipAddressBytes = new byte[4];

        for (int i = 0; i < 4; i++) {
            ipAddressBytes[i] = (byte) Integer.parseInt(octets[i]);
        }

        return ipAddressBytes;
    }

    private static boolean isValidIPv6Address(String ipAddress) {
   
        // Split the address into groups using the colon separator
        String[] groups = ipAddress.split(":");
    
        // Check the number of groups
        if (groups.length > 8) {
            return false; // Too many groups
        }
    
        boolean hasDoubleColon = false;
        int emptyGroups = 0;
    
        for (int i = 0; i < groups.length; i++) {
            String group = groups[i];
    
            // Check for an empty group
            if (group.isEmpty()) {
                emptyGroups++;
    
                // Double colon can only appear once
                if (emptyGroups > 1) {
                    return false;
                }
    
                hasDoubleColon = true;
                continue;
            }
    
            // Check each character in the group
            for (int j = 0; j < group.length(); j++) {
                char c = group.charAt(j);
    
                // Check if the character is a valid hexadecimal digit
                if (!isValidHexDigit(c)) {
                    return false;
                }
            }
        }
    
        // Check if the address ends with a double colon
        if (ipAddress.endsWith(":")) {
            // We already had an empty group so crap out
            if (emptyGroups > 0) {
                return false;
            }
            hasDoubleColon = true;
        }

        // Check the final number of groups
        if (!hasDoubleColon && 
             groups.length != 8) {
            return false; // Not enough groups
        }
    
        return true;
    }
    
    private static boolean isValidHexDigit(char c) {
        return (c >= 'a' && c <= 'f') ||
               (c >= '0' && c <= '9') || 
               (c >= 'A' && c <= 'F');
    }

    private static byte[] convertIPv6ToByteArray(String ipAddress) {
        // Split the address into groups using the colon separator
        String[] groups = ipAddress.split(":");

        // Initialize the byte array
        byte[] ipAddressBytes = new byte[16];

        // Index to keep track of the current position in the byte array
        int index = 0;

        for (int i = 0; i < groups.length; i++) {
            String group = groups[i];

            // Check for an empty group
            if (group.isEmpty()) {
                // Calculate the number of empty groups needed
                int emptyGroups = 8 - (groups.length - 1);

                // Fill the empty groups with zeros
                for (int j = 0; j < emptyGroups; j++) {
                    ipAddressBytes[index++] = 0;
                    ipAddressBytes[index++] = 0;
                }
                continue;
            }

            // Convert the group to a 16-bit integer
            int value = Integer.parseInt(group, 16);

            // Split the value into two bytes
            ipAddressBytes[index++] = (byte) ((value >> 8) & 0xFF);
            ipAddressBytes[index++] = (byte) (value & 0xFF);
        }

        return ipAddressBytes;
    }
}
