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
package org.eclipse.uprotocol.uri.serializer;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import org.eclipse.uprotocol.uri.validator.UriValidator;
import org.eclipse.uprotocol.v1.UUri;

/**
 * Provides functionality for serializing and deserializing {@link UUri}s to/from their
 * corresponding URI representation as defined by the uProtocol specification.
 *
 * @see <a href="https://github.com/eclipse-uprotocol/uprotocol-spec/blob/v1.6.0-alpha.6/basics/uri.adoc">
 * uProtocol URI Specification</a>
 */
public final class UriSerializer {

    /**
     * The scheme used for uProtocol URIs.
     */
    public static final String SCHEME_UP = "up";

    private static final Pattern AUTHORITY_PATTERN = Pattern.compile("^[a-z0-9-._~]{0,128}$");

    private UriSerializer() {
        // prevent instantiation
    }

    /**
     * Serializes a {@link UUri} into its URI representation.
     * <p>
     * The returned URI does not include the "up:" scheme. If the scheme needs to be included,
     * the overloaded {@link #serialize(UUri, boolean)} method with the {@code includeScheme}
     * parameter set to {@code true} can be used.
     *
     * @param uuri The UUri to be serialized.
     * @return The URI.
     * @throws NullPointerException if the UUri is null.
     * @throws IllegalArgumentException if the UUri does not comply with the UUri specification.
     */
    // [impl->dsn~uri-authority-mapping~1]
    // [impl->dsn~uri-path-mapping~1]
    // [impl->req~uri-serialization~1]
    public static String serialize(UUri uuri) {
        return serialize(uuri, false);
    }

    /**
     * Serializes a {@link UUri} into its URI representation.
     * 
     * @param uuri The UUri to be serialized.
     * @param includeScheme Whether to include the "up:" scheme in the serialized URI.
     *                     If false, the scheme and the colon will be omitted.
     *                     This can be useful when embedding the URI in contexts where
     *                     the scheme is implied or not allowed.
     * @return The URI.
     * @throws NullPointerException if the UUri is null.
     * @throws IllegalArgumentException if the UUri does not comply with the UUri specification.
     */
    // [impl->dsn~uri-authority-mapping~1]
    // [impl->dsn~uri-path-mapping~1]
    // [impl->req~uri-serialization~1]
    public static String serialize(UUri uuri, boolean includeScheme) {
        Objects.requireNonNull(uuri);
        UriValidator.validate(uuri);
        StringBuilder sb = new StringBuilder();

        if (includeScheme) {
            sb.append(SCHEME_UP).append(":");
        }
        if (!uuri.getAuthorityName().isBlank()) {
            sb.append("//");
            sb.append(uuri.getAuthorityName());
        }

        sb.append("/");
        final var pathSegments = String.format("%X/%X/%X",
                uuri.getUeId(),
                uuri.getUeVersionMajor(),
                uuri.getResourceId());
        sb.append(pathSegments);
        return sb.toString();
    }

    /**
     * Deserializes a URI into a UUri.
     * 
     * @param uProtocolUri The URI to deserialize.
     * @return The UUri.
     * @throws NullPointerException if the URI is null.
     * @throws IllegalArgumentException if the URI is invalid.
     */
    // [impl->dsn~uri-authority-name-length~1]
    // [impl->dsn~uri-scheme~1]
    // [impl->dsn~uri-host-only~2]
    // [impl->dsn~uri-authority-mapping~1]
    // [impl->dsn~uri-path-mapping~1]
    // [impl->req~uri-serialization~1]
    public static UUri deserialize(String uProtocolUri) {
        Objects.requireNonNull(uProtocolUri);
        final var parsedUri = URI.create(uProtocolUri);
        return deserialize(parsedUri);
    }

    /**
     * Deserializes a URI into a UUri.
     * 
     * @param uProtocolUri The URI to deserialize.
     * @return The UUri.
     * @throws NullPointerException if the URI is null.
     * @throws IllegalArgumentException if the URI is invalid.
     */
    // [impl->dsn~uri-authority-name-length~1]
    // [impl->dsn~uri-scheme~1]
    // [impl->dsn~uri-host-only~2]
    // [impl->dsn~uri-authority-mapping~1]
    // [impl->dsn~uri-path-mapping~1]
    // [impl->req~uri-serialization~1]
    public static UUri deserialize(URI uProtocolUri) {
        Objects.requireNonNull(uProtocolUri);

        if (uProtocolUri.getScheme() != null && !SCHEME_UP.equals(uProtocolUri.getScheme())) {
            throw new IllegalArgumentException("uProtocol URI must use '%s' scheme".formatted(SCHEME_UP));
        }
        if (uProtocolUri.getQuery() != null) {
            throw new IllegalArgumentException("uProtocol URI must not contain query");
        }
        if (uProtocolUri.getFragment() != null) {
            throw new IllegalArgumentException("uProtocol URI must not contain fragment");
        }

        String authority;
        try {
            // this should work if authority is server-based (i.e. contains a host)
            var uriWithServerAuthority = uProtocolUri.parseServerAuthority();
            // we can then verify that the authority does neither contain user info nor port
            UriValidator.validateParsedAuthority(uriWithServerAuthority);
            authority = uriWithServerAuthority.getHost();
        } catch (URISyntaxException e) {
            // the authority is not server-based but might still be valid according to the UUri spec,
            // we just need to make sure that it either is the wildcard authority or contains allowed
            // characters only
            authority = uProtocolUri.getAuthority();
            if (authority != null && !"*".equals(authority) && !AUTHORITY_PATTERN.matcher(authority).matches()) {
                throw new IllegalArgumentException(
                    "uProtocol URI authority contains invalid characters",
                    e);
            }
        }

        final var pathSegments = uProtocolUri.getPath().split("/");
        if (pathSegments.length != 4) {
            throw new IllegalArgumentException("uProtocol URI must have exactly 3 path segments");
        }

        final var builder = UUri.newBuilder();
        Optional.ofNullable(authority).ifPresent(builder::setAuthorityName);

        if (pathSegments[1].isEmpty()) {
            throw new IllegalArgumentException("URI must contain non-empty entity ID");
        }
        try {
            builder.setUeId(Integer.parseUnsignedInt(pathSegments[1], 16));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("URI must contain 32 bit hex-encoded entity ID", e);
        }

        if (pathSegments[2].isEmpty()) {
            throw new IllegalArgumentException("URI must contain non-empty entity version");
        }
        try {
            int versionMajor = Integer.parseUnsignedInt(pathSegments[2], 16);
            UriValidator.validateVersionMajor(versionMajor);
            builder.setUeVersionMajor(versionMajor);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("URI must contain 8 bit hex-encoded entity version", e);
        }

        // the fourth path segment can not be empty because the String.split() method excludes
        // trailing empty strings from the resulting array
        // it is therefore safe to simply parse it as an unsigned integer
        try {
            int resourceId = Integer.parseUnsignedInt(pathSegments[3], 16);
            UriValidator.validateResourceId(resourceId);
            builder.setResourceId(resourceId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("URI must contain 16 bit hex-encoded resource ID", e);
        }
        return builder.build();
    }
}
