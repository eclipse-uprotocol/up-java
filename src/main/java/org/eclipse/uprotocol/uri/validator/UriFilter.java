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
package org.eclipse.uprotocol.uri.validator;

import org.eclipse.uprotocol.v1.UAttributes;
import org.eclipse.uprotocol.v1.UUri;

import java.util.Objects;

/**
 * URI Filter matches URIs based on source and sink URIs.
 */
public record UriFilter(UUri source, UUri sink) {

    /**
     * Constructs a new URI filter with the given source and sink URIs.
     * 
     * @param source The source URI.
     * @param sink The sink URI.
     */
    public UriFilter {
        Objects.requireNonNull(source);
        Objects.requireNonNull(sink);
    }
    

    /**
     * Matches the given attributes with the source and sink URIs.
     * 
     * @param attributes The attributes to match.
     * @return Returns true if the attributes match the source and sink URIs.
     */
    public boolean matches(UAttributes attributes) {
        if (attributes == null) {
            return false;
        }
        return UriValidator.matches(source, attributes.getSource()) && UriValidator.matches(sink, attributes.getSink());
    }
}
