/**
 * SPDX-FileCopyrightText: 2025 Contributors to the Eclipse Foundation
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
package org.eclipse.uprotocol.transport;

import java.util.Objects;

import org.eclipse.uprotocol.uri.validator.UriValidator;
import org.eclipse.uprotocol.v1.UUri;

/**
 * A URI provider that is statically configured with the uEntity's authority, entity ID and version.
 */
public final class StaticUriProvider implements LocalUriProvider {

    private final UUri localUri;

    private StaticUriProvider(UUri uuri) {
        this.localUri = uuri;
    }

    /**
     * Creates a new provider for a uEntity with a known local URI.
     *
     * @param uuri The local URI of the uEntity.
     * @return The provider.
     * @throws NullPointerException if uuri is {@code null}.
     */
    public static StaticUriProvider of(UUri uuri) {
        return of(uuri.getAuthorityName(), uuri.getUeId(), uuri.getUeVersionMajor());
    }

    /**
     * Creates a new provider for a uEntity with a known authority, entity ID and version.
     *
     * @param authority The authority name of the uEntity.
     * @param entityId The entity ID of the uEntity.
     * @param majorVersion The major version of the uEntity.
     * @return The provider.
     * @throws IllegalArgumentException if the provided parameters are invalid.
     * @throws NullPointerException if authority is {@code null}.
     */
    public static StaticUriProvider of(String authority, int entityId, int majorVersion) {
        Objects.requireNonNull(authority);
        UUri localUri = UUri.newBuilder()
            .setAuthorityName(authority)
            .setUeId(entityId)
            .setUeVersionMajor(majorVersion)
            .build();
        UriValidator.validate(localUri);
        return new StaticUriProvider(localUri);
    }

    @Override
    public String getAuthority() {
        return localUri.getAuthorityName();
    }

    @Override
    public UUri getSource() {
        return localUri;
    }

    @Override
    public UUri getResource(int id) {
        return UUri.newBuilder(localUri)
            .setResourceId(id)
            .build();
    }
}
