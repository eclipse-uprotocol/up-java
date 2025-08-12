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
 * URI Factory that builds URIs from protos.
 */
public interface UriFactory {
    String WILDCARD_AUTHORITY = "*";
    int WILDCARD_ENTITY_ID = 0xFFFF;
    int WILDCARD_ENTITY_VERSION = 0xFF;
    int WILDCARD_RESOURCE_ID = 0xFFFF;

    /**
     * Builds a UEntity for an protobuf generated code Service Descriptor.
     * 
     * @param descriptor The protobuf generated code Service Descriptor.
     * @param resourceId The resource id.
     * @return Returns a UEntity for an protobuf generated code Service Descriptor.
     */
    static UUri fromProto(ServiceDescriptor descriptor, int resourceId) {
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
    static UUri fromProto(ServiceDescriptor descriptor, int resourceId, String authorityName) {
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


    /**
     * A uProtocol pattern URI that matches all UUris.
     */
    UUri ANY = UUri.newBuilder()
            .setAuthorityName(WILDCARD_AUTHORITY)
            .setUeId(WILDCARD_ENTITY_ID)
            .setUeVersionMajor(WILDCARD_ENTITY_VERSION)
            .setResourceId(WILDCARD_RESOURCE_ID).build();
}
