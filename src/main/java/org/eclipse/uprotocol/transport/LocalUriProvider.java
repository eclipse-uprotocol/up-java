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

import org.eclipse.uprotocol.v1.UUri;

/**
 * A factory for URIs representing this uEntity's resources.
 * <p>
 * Implementations may use arbitrary mechanisms to determine the information that
 * is necessary for creating URIs, e.g. environment variables, configuration files etc.
 */
public interface LocalUriProvider {
    /**
     * Gets the <em>authority</em> used for URIs representing this uEntity's resources.
     *
     * @return The authority name.
     */
    String getAuthority();

    /**
     * Gets the URI that represents the resource that this uEntity expects
     * RPC responses and notifications to be sent to.
     *
     * @return The source URI.
     */
    UUri getSource();

    /**
     * Gets a URI that represents a given resource of this uEntity.
     *
     * @param id The ID of the resource.
     * @return The resource URI.
     */
    UUri getResource(int id);
}
