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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import org.eclipse.uprotocol.communication.UStatusException;
import org.eclipse.uprotocol.transport.UTransport;
import org.eclipse.uprotocol.uri.factory.UriFactory;
import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UUri;

/**
 * A helper for validating uProtocol URIs.
 */
public final class UriValidator {

    /**
     * The maximum length of an authority name.
     */
    public static final int AUTHORITY_NAME_MAX_LENGTH = 128;

    private static final Pattern AUTHORITY_PATTERN = Pattern.compile("^[a-z0-9-._~]*$");

    private UriValidator() {
        // prevent instantiation
    }

    /**
     * The minimum publish/notification topic id for a URI.
     */
    public static final int MIN_TOPIC_ID = 0x8000;

    /**
     * The Default resource id.
     */
    public static final int DEFAULT_RESOURCE_ID = 0x0000;

    /**
     * Validates a UUri against the uProtocol specification.
     *
     * @param uuri The UUri to validate.
     * @throws NullPointerException if uuri is {@code null}.
     * @throws IllegalArgumentException if uuri does not comply with the UUri specification.
     */
    public static void validate(UUri uuri) {
        Objects.requireNonNull(uuri, "URI must not be null");

        Optional.ofNullable(uuri.getAuthorityName())
            .filter(s -> !s.isEmpty())
            .ifPresent(name -> {
                try {
                    var uri = new URI(null, name, null, null, null);
                    validateAuthority(uri);
                } catch (URISyntaxException e) {
                    throw new IllegalArgumentException("Invalid authority name", e);
                }
            });

        // no need to check uEntity ID which is of Java primitive type (signed) int but actually represents
        // an unsigned 32 bit integer, thus any value is valid

        validateVersionMajor(uuri.getUeVersionMajor());
        validateResourceId(uuri.getResourceId());
    }

    public static void validateVersionMajor(int versionMajor) {
        if ((versionMajor & 0xFFFF_FF00) != 0) {
            throw new IllegalArgumentException("uEntity version major must be in range [0x00, 0x%X]"
                .formatted(UriFactory.WILDCARD_ENTITY_VERSION));
        }
    }

    public static void validateResourceId(int resourceId) {
        if ((resourceId & 0xFFFF_0000) != 0) {
            throw new IllegalArgumentException("uEntity resource ID must be in range [0x0000, 0x%X]"
                .formatted(UriFactory.WILDCARD_RESOURCE_ID));
        }
    }

    /**
     * Verifies that the authority part of a uProtocol URI complies with the uProtocol specification.
     *
     * @param uri The URI to validate.
     * @throws NullPointerException if uri is {@code null}.
     * @throws IllegalArgumentException if the authority part of the URI does not comply with the
     *         uProtocol specification.
     * @return The validated authority part of the URI.
     */
    // [impl->dsn~uri-authority-name-length~1]
    // [impl->dsn~uri-host-only~2]
    public static String validateAuthority(URI uri) {
        Objects.requireNonNull(uri, "URI must not be null");

        String authority;
        try {
            // this should work if authority is server-based, i.e. contains a host, literal IP or IPv4 address
            var uriWithServerAuthority = uri.parseServerAuthority();
            if (uriWithServerAuthority.getPort() != -1) {
                throw new IllegalArgumentException("uProtocol URI must not contain port");
            }
            if (uriWithServerAuthority.getUserInfo() != null) {
                throw new IllegalArgumentException("uProtocol URI must not contain user info");
            }
            authority = uriWithServerAuthority.getHost();
            if (authority != null && authority.startsWith("[") && authority.endsWith("]")) {
                // this is an IPv6 literal address
                return authority;
            }
        } catch (URISyntaxException e) {
            // the authority is not server-based but might still be valid according to the UUri spec,
            authority = uri.getAuthority();
        }
        // make sure that authority name either is the wildcard authority or contains allowed characters only
        if (authority != null && !"*".equals(authority) && !AUTHORITY_PATTERN.matcher(authority).matches()) {
            throw new IllegalArgumentException("uProtocol URI authority contains invalid characters");
        }
        // and does not exceed maximum length
        if (authority != null && authority.length() > AUTHORITY_NAME_MAX_LENGTH) {
            throw new IllegalArgumentException("uProtocol URI authority must not exceed %d characters"
                .formatted(AUTHORITY_NAME_MAX_LENGTH));
        }

        return authority;
    }

    /**
     * Checks if a uProtocol URI represents an RPC method address.
     *
     * @param uri The URI to check.
     * @return {@code true} if the URI's resource ID is &gt; {@value #DEFAULT_RESOURCE_ID}
     *         and &lt; {@value #MIN_TOPIC_ID}.
     * @throws NullPointerException if uri is {@code null}.
     */
    public static boolean isRpcMethod(UUri uri) {
        Objects.requireNonNull(uri, "URI must not be null");
        return uri.getResourceId() > DEFAULT_RESOURCE_ID &&
                uri.getResourceId() < MIN_TOPIC_ID;
    }

    /**
     * Checks if a uProtocol URI represents an RPC response address.
     *
     * @param uri The URI to check.
     * @return {@code true} if the URI's resource ID is {@value #DEFAULT_RESOURCE_ID}.
     * @throws NullPointerException if uri is {@code null}.
     */
    public static boolean isRpcResponse(UUri uri) {
        Objects.requireNonNull(uri, "URI must not be null");
        return isNotificationDestination(uri);
    }

    /**
     * Checks if a uProtocol URI represents a destination for a notification.
     *
     * @param uri The URI to check.
     * @return {@code true} if the URI's resource ID is {@value #DEFAULT_RESOURCE_ID}.
     * @throws NullPointerException if uri is {@code null}.
     */
    public static boolean isNotificationDestination(UUri uri) {
        Objects.requireNonNull(uri, "URI must not be null");
        return uri.getResourceId() == DEFAULT_RESOURCE_ID;
    }

    /**
     * Checks if a uProtocol URI can be used as the source of an event or notification.
     *
     * @param uri The URI to check.
     * @return {@code true} if the URI's resource ID is &gt;= {@value #MIN_TOPIC_ID}
     *         and &lt; {@value UriFactory#WILDCARD_RESOURCE_ID}.
     * @throws NullPointerException if uri is {@code null}.
     */
    public static boolean isTopic(UUri uri) {
        Objects.requireNonNull(uri, "URI must not be null");
        return uri.getResourceId() >= MIN_TOPIC_ID &&
        uri.getResourceId() < UriFactory.WILDCARD_RESOURCE_ID;
    }

    static boolean matchesAuthority(UUri pattern, UUri candidateUri) {
        return hasWildcardAuthority(pattern) ||
                pattern.getAuthorityName().equals(candidateUri.getAuthorityName());
    }

    static boolean matchesEntityTypeId(UUri pattern, UUri candidateUri) {
        final var entityTypeIdToMatch = pattern.getUeId() & UriFactory.WILDCARD_ENTITY_TYPE_ID;
        return entityTypeIdToMatch == UriFactory.WILDCARD_ENTITY_TYPE_ID ||
                entityTypeIdToMatch == (candidateUri.getUeId() & UriFactory.WILDCARD_ENTITY_TYPE_ID);
    }

    static boolean matchesEntityInstance(UUri pattern, UUri candidateUri) {
        final var instanceIdToMatch = pattern.getUeId() & UriFactory.WILDCARD_ENTITY_INSTANCE_ID;
        return instanceIdToMatch == UriFactory.WILDCARD_ENTITY_INSTANCE_ID ||
                instanceIdToMatch == (candidateUri.getUeId() & UriFactory.WILDCARD_ENTITY_INSTANCE_ID);
    }

    static boolean matchesEntityVersion(UUri pattern, UUri candidateUri) {
        return hasWildcardEntityVersion(pattern) ||
                pattern.getUeVersionMajor() == candidateUri.getUeVersionMajor();
    }

    static boolean matchesEntity(UUri pattern, UUri candidateUri) {
        return matchesEntityTypeId(pattern, candidateUri) &&
                matchesEntityInstance(pattern, candidateUri) &&
                matchesEntityVersion(pattern, candidateUri);
    }

    static boolean matchesResource(UUri pattern, UUri candidateUri) {
        return hasWildcardResourceId(pattern) ||
                pattern.getResourceId() == candidateUri.getResourceId();
    }

    /**
     * Checks if a given candidate URI matches a pattern.
     *
     * @param pattern The pattern to match.
     * @param candidateUri The candidate URI to match against the pattern.
     * @return {@code true} if the candidate matches the pattern.
     * @throws NullPointerException if any of the arguments are {@code null}.
     */
    // [impl->dsn~uri-pattern-matching~2]
    public static boolean matches(UUri pattern, UUri candidateUri) {
        Objects.requireNonNull(pattern, "Pattern must not be null");
        Objects.requireNonNull(candidateUri, "Candidate URI must not be null");
        return matchesAuthority(pattern, candidateUri) &&
                matchesEntity(pattern, candidateUri) &&
                matchesResource(pattern, candidateUri);
    }

    /**
     * Checks if a uProtocol URI contains the wildcard authority.
     *
     * @param uri The URI to check.
     * @return {@code true} if the URI's authority is the wildcard authority.
     * @throws NullPointerException if uri is {@code null}.
     */
    public static boolean hasWildcardAuthority(UUri uri) {
        return UriFactory.WILDCARD_AUTHORITY.equals(uri.getAuthorityName());
    }

    /**
     * Checks if a uProtocol URI contains the wildcard uEntity type.
     *
     * @param uri The URI to check.
     * @return {@code true} if the URI's uEntity type identifier is the wildcard identifier.
     * @throws NullPointerException if uri is {@code null}.
     */
    public static boolean hasWildcardEntityTypeId(UUri uri) {
        return (uri.getUeId() & UriFactory.WILDCARD_ENTITY_TYPE_ID) == UriFactory.WILDCARD_ENTITY_TYPE_ID;
    }

    /**
     * Checks if a uProtocol URI contains the wildcard uEntity instance identifier.
     *
     * @param uri The URI to check.
     * @return {@code true} if the URI's uEntity instance identifier is the wildcard identifier.
     * @throws NullPointerException if uri is {@code null}.
     */
    public static boolean hasWildcardEntityInstanceId(UUri uri) {
        return (uri.getUeId() & UriFactory.WILDCARD_ENTITY_INSTANCE_ID) == UriFactory.WILDCARD_ENTITY_INSTANCE_ID;
    }

    /**
     * Checks if a uProtocol URI contains the wildcard uEntity major version.
     *
     * @param uri The URI to check.
     * @return {@code true} if the URI's uEntity major version is the wildcard version.
     * @throws NullPointerException if uri is {@code null}.
     */
    public static boolean hasWildcardEntityVersion(UUri uri) {
        return uri.getUeVersionMajor() == UriFactory.WILDCARD_ENTITY_VERSION;
    }

    /**
     * Checks if a uProtocol URI contains the wildcard uEntity resource identifier.
     *
     * @param uri The URI to check.
     * @return {@code true} if the URI's uEntity resource identifier is the wildcard identifier.
     * @throws NullPointerException if uri is {@code null}.
     */
    public static boolean hasWildcardResourceId(UUri uri) {
        return uri.getResourceId() == UriFactory.WILDCARD_RESOURCE_ID;
    }

    /**
     * Checks if a uProtocol URI contains any wildcard values.
     *
     * @param uri The URI to check.
     * @return {@code true} if at least one of the URI's fields contains a wildcard value.
     * @throws NullPointerException if uri is {@code null}.
     */
    public static boolean hasWildcard(UUri uri) {
        Objects.requireNonNull(uri, "URI must not be null");
        return hasWildcardAuthority(uri) ||
                hasWildcardEntityTypeId(uri) ||
                hasWildcardEntityInstanceId(uri) ||
                hasWildcardEntityVersion(uri) ||
                hasWildcardResourceId(uri);
    }

    /**
     * Verifies that given uProtocol URIs can be used as source and sink filter URIs
     * for registering listeners.
     * <p>
     * This function is helpful for implementing {@link UTransport} in accordance with the
     * uProtocol Transport Layer specification.
     *
     * @param sourceFilter The source filter URI to verify.
     * @param sinkFilter The optional sink filter URI to verify.
     * @throws NullPointerException if any of the arguments are {@code null}.
     * @throws UStatusException if the given URIs cannot be used as filter criteria.
     */
    public static void verifyFilterCriteria(UUri sourceFilter, Optional<UUri> sinkFilter) {
        Objects.requireNonNull(sourceFilter);
        Objects.requireNonNull(sinkFilter);
        try {
            validate(sourceFilter);
            sinkFilter.ifPresent(UriValidator::validate);
        } catch (IllegalArgumentException e) {
            throw new UStatusException(
                UCode.INVALID_ARGUMENT,
                "source and sink filters must be valid uProtocol URIs",
                e);
        }
        sinkFilter.ifPresentOrElse(
            filter -> {
                if (isNotificationDestination(filter) && isNotificationDestination(sourceFilter)) {
                    throw new UStatusException(
                        UCode.INVALID_ARGUMENT,
                        "source and sink filters must not both have resource ID 0");
                }
                if (isRpcMethod(filter)
                    && !hasWildcardResourceId(sourceFilter)
                    && !isRpcResponse(sourceFilter)) {
                    throw new UStatusException(
                        UCode.INVALID_ARGUMENT,
                        """
                        source filter must either have the wildcard resource ID or resource ID 0, \
                        if sink filter matches RPC method resource ID
                        """);
                }
            },
            () -> {
                if (!hasWildcardResourceId(sourceFilter) && !isTopic(sourceFilter)) {
                    throw new UStatusException(
                        UCode.INVALID_ARGUMENT,
                        """
                        source filter must either have the wildcard resource ID or a resource ID from topic range, \
                        if sink filter is empty
                        """);
                } // no sink filter provided
            }
        );
        // everything else might match valid messages
    }
}
