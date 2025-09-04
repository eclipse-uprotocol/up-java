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
package org.eclipse.uprotocol.uri.factory;

import org.eclipse.uprotocol.Uoptions;
import org.eclipse.uprotocol.v1.UUri;

import com.google.protobuf.DescriptorProtos.ServiceOptions;
import com.google.protobuf.Descriptors.ServiceDescriptor;

/**
 * A factory for uProtocol URIs.
 */
public final class UriFactory {

    public static final String WILDCARD_AUTHORITY = "*";
    public static final int WILDCARD_ENTITY_ID = 0xFFFF;
    public static final int WILDCARD_ENTITY_VERSION = 0xFF;
    public static final int WILDCARD_RESOURCE_ID = 0xFFFF;

    /**
     * A uProtocol pattern URI that matches all UUris.
     */
    public static final UUri ANY = UUri.newBuilder()
            .setAuthorityName(WILDCARD_AUTHORITY)
            .setUeId(WILDCARD_ENTITY_ID)
            .setUeVersionMajor(WILDCARD_ENTITY_VERSION)
            .setResourceId(WILDCARD_RESOURCE_ID).build();

    private UriFactory() {
        // Prevent instantiation
    }

    /**
     * Builds a UEntity for an protobuf generated code Service Descriptor.
     * 
     * @param descriptor The protobuf generated code Service Descriptor.
     * @param resourceId The resource id.
     * @return Returns a UEntity for an protobuf generated code Service Descriptor.
     */
    public static UUri fromProto(ServiceDescriptor descriptor, int resourceId) {
        return fromProto(descriptor, resourceId, null);
    }

    /**
     * Builds a UEntity for an protobuf generated code Service Descriptor.
     * 
     * @param descriptor    The protobuf generated code Service Descriptor.
     * @param resourceId    The resource id.
     * @param authorityName The authority name.
     * @return Returns a UEntity for an protobuf generated code Service Descriptor.
     */
    public static UUri fromProto(ServiceDescriptor descriptor, int resourceId, String authorityName) {
        if (descriptor == null) {
            return UUri.getDefaultInstance();
        }

        final ServiceOptions options = descriptor.getOptions();

        UUri.Builder builder = UUri.newBuilder()
                .setUeId(options.<Integer>getExtension(Uoptions.serviceId))
                .setUeVersionMajor(options.<Integer>getExtension(Uoptions.serviceVersionMajor))
                .setResourceId(resourceId);

        if (authorityName != null && !authorityName.isEmpty()) {
            builder.setAuthorityName(authorityName);
        }
        return builder.build();
    }
}
