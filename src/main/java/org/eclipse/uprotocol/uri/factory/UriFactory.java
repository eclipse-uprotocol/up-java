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


import java.util.Objects;
import java.util.Optional;

import org.eclipse.uprotocol.Uoptions;
import org.eclipse.uprotocol.uri.validator.UriValidator;
import org.eclipse.uprotocol.v1.UUri;

import com.google.protobuf.DescriptorProtos.ServiceOptions;
import com.google.protobuf.Descriptors.ServiceDescriptor;

/**
 * A factory for uProtocol URIs.
 */
public final class UriFactory {

    public static final String WILDCARD_AUTHORITY = "*";
    public static final int WILDCARD_ENTITY_TYPE_ID = 0x0000_FFFF;
    public static final int WILDCARD_ENTITY_INSTANCE_ID = 0xFFFF_0000;
    public static final int WILDCARD_ENTITY_ID = WILDCARD_ENTITY_TYPE_ID | WILDCARD_ENTITY_INSTANCE_ID;
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
     * Creates a uProtocol URI for a resource defined by a protobuf service descriptor.
     * <p>
     * The descriptor is expected to contain {@link Uoptions#serviceId service ID}
     * and {@link Uoptions#serviceVersionMajor major version} options.
     *
     * @param descriptor The service descriptor to create the URI for.
     * @param resourceId The resource ID to create the URI for.
     * @return The URI.
     * @throws NullPointerException if the descriptor is {@code null}.
     * @throws IllegalArgumentException if the descriptor does not contain the required options
     * or if the options can not be used to create a valid uProtocol URI.
     */
    public static UUri fromProto(ServiceDescriptor descriptor, int resourceId) {
        return fromProto(descriptor, resourceId, null);
    }

    /**
     * Creates a uProtocol URI for a resource defined by a protobuf service descriptor.
     * <p>
     * The descriptor is expected to contain {@link Uoptions#serviceId service ID}
     * and {@link Uoptions#serviceVersionMajor major version} options.
     * 
     * @param descriptor The service descriptor to create the URI for.
     * @param resourceId The resource ID to create the URI for.
     * @param authorityName The URI's authority name or {@code null} to create a local URI.
     * @return The URI.
     * @throws NullPointerException if the descriptor is {@code null}.
     * @throws IllegalArgumentException if the descriptor does not contain the required options
     * or if the options can not be used to create a valid uProtocol URI.
     */
    public static UUri fromProto(ServiceDescriptor descriptor, int resourceId, String authorityName) {
        Objects.requireNonNull(descriptor);
        final ServiceOptions options = descriptor.getOptions();
        if (!options.hasExtension(Uoptions.serviceId) || !options.hasExtension(Uoptions.serviceVersionMajor)) {
            throw new IllegalArgumentException(
                    "The provided descriptor does not contain the required uProtocol options.");
        }

        UUri.Builder builder = UUri.newBuilder()
                .setUeId(options.<Integer>getExtension(Uoptions.serviceId))
                .setUeVersionMajor(options.<Integer>getExtension(Uoptions.serviceVersionMajor))
                .setResourceId(resourceId);

        Optional.ofNullable(authorityName).ifPresent(builder::setAuthorityName);
        final var uuri = builder.build();
        UriValidator.validate(uuri);
        return uuri;
    }
}
