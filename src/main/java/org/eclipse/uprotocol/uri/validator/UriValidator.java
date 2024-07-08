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

import org.eclipse.uprotocol.uri.factory.UriFactory;
import org.eclipse.uprotocol.v1.UUri;

/**
 * class for validating Uris.
 */
public interface UriValidator {

    /**
     * The minimum publish/notification topic id for a URI.
     */
    int MIN_TOPIC_ID = 0x8000;

    /**
     * The Default resource id.
     */
    int DEFAULT_RESOURCE_ID = 0;


    /**
     * Indicates that this URI is an empty as it does not contain authority, entity,
     * and resource.
     *
     * @param uri {@link UUri} to check if it is empty
     * @return Returns true if this URI is an empty container and has no valuable
     *         information in building uProtocol sinks or sources.
     */
    static boolean isEmpty(UUri uri) {
        return uri == null || uri.equals(UUri.getDefaultInstance());
    }

    /**
     * Returns true if URI is of type RPC. A UUri is of type RPC if its
     * resource ID is less than MIN_TOPIC_ID and greater than RESOURCE_ID_RESPONSE.
     *
     * @param uri {@link UUri} to check if it is of type RPC method
     * @return Returns true if URI is of type RPC.
     */
    static boolean isRpcMethod(UUri uri) {
        return !isEmpty(uri) &&
                uri.getResourceId() > DEFAULT_RESOURCE_ID &&
                uri.getResourceId() < MIN_TOPIC_ID;
    }

    /**
     * Returns true if URI is of type RPC response.
     *
     * @param uri {@link UUri} to check response
     * @return Returns true if URI is of type RPC response.
     */
    static boolean isRpcResponse(UUri uri) {
        return isDefaultResourceId(uri);
    }

    /**
     * Returns true if URI has the resource id of 0.
     *
     * @param uri {@link UUri} to check request
     * @return Returns true if URI has a resource id of 0.
     */
    static boolean isDefaultResourceId(UUri uri) {
        return !isEmpty(uri) && uri.getResourceId() == DEFAULT_RESOURCE_ID;
    }

    /**
     * Returns true if URI is of type Topic used for publish and notifications.
     *
     * @param uri {@link UUri} to check if it is of type Topic
     * @return Returns true if URI is of type Topic.
     */
    static boolean isTopic(UUri uri) {
        return !isEmpty(uri) && uri.getResourceId() >= MIN_TOPIC_ID;
    }

    /**
     * Checks if the authority of the uriToMatch matches the candidateUri.
     * A match occurs if the authority name in uriToMatch is a wildcard
     * or if both URIs have the same authority name.
     *
     * @param uriToMatch The URI to match.
     * @param candidateUri The candidate URI to match against.
     * @return True if the authority names match, False otherwise.
     */
    static boolean matchesAuthority(UUri uriToMatch, UUri candidateUri) {
        return UriFactory.WILDCARD_AUTHORITY.equals(uriToMatch.getAuthorityName()) ||
                uriToMatch.getAuthorityName().equals(candidateUri.getAuthorityName());
    }

    /**
     * Checks if the entity ID of the uriToMatch matches the candidateUri.
     * A match occurs if the entity ID in uriToMatch is a wildcard (0xFFFF)
     * or if the masked entity IDs of both URIs are equal.
     * The entity ID masking is performed using a bitwise AND operation with
     * 0xFFFF. If the result of the bitwise AND operation between the
     * uriToMatch's entity ID and 0xFFFF is 0xFFFF, it indicates that the
     * uriToMatch's entity ID is a wildcard and can match any entity ID.
     * Otherwise, the function checks if the masked entity IDs of both URIs
     * are equal, meaning that the relevant parts of their entity IDs match.
     *
     * @param uriToMatch The URI to match.
     * @param candidateUri The candidate URI to match against.
     * @return True if the entity IDs match, False otherwise.
     */
    static boolean matchesEntityId(UUri uriToMatch, UUri candidateUri) {
        return (uriToMatch.getUeId() & UriFactory.WILDCARD_ENTITY_ID) == UriFactory.WILDCARD_ENTITY_ID ||
                (uriToMatch.getUeId() & UriFactory.WILDCARD_ENTITY_ID) ==
                        (candidateUri.getUeId() & UriFactory.WILDCARD_ENTITY_ID);
    }

    /**
     * Checks if the entity instance of the uriToMatch matches the candidateUri.
     * A match occurs if the upper 16 bits of the entity ID in uriToMatch are zero
     * or if the upper 16 bits of the entity IDs of both URIs are equal.
     *
     * @param uriToMatch The URI to match.
     * @param candidateUri The candidate URI to match against.
     * @return True if the entity instances match, False otherwise.
     */
    static boolean matchesEntityInstance(UUri uriToMatch, UUri candidateUri) {
        return (uriToMatch.getUeId() & 0xFFFF0000) == 0x00000000 ||
                (uriToMatch.getUeId() & 0xFFFF0000) == (candidateUri.getUeId() & 0xFFFF0000);
    }

    /**
     * Checks if the entity version of the uriToMatch matches the candidateUri.
     * A match occurs if the entity version in uriToMatch is a wildcard
     * or if both URIs have the same entity version.
     *
     * @param uriToMatch The URI to match.
     * @param candidateUri The candidate URI to match against.
     * @return True if the entity versions match, False otherwise.
     */
    static boolean matchesEntityVersion(UUri uriToMatch, UUri candidateUri) {
        return UriFactory.WILDCARD_ENTITY_VERSION == uriToMatch.getUeVersionMajor() ||
                uriToMatch.getUeVersionMajor() == candidateUri.getUeVersionMajor();
    }

    /**
     * Checks if the entity of the uriToMatch matches the candidateUri.
     * A match occurs if the entity ID, entity instance, and entity version
     * of both URIs match according to their respective rules.
     *
     * @param uriToMatch The URI to match.
     * @param candidateUri The candidate URI to match against.
     * @return True if the entities match, False otherwise.
     */
    static boolean matchesEntity(UUri uriToMatch, UUri candidateUri) {
        return matchesEntityId(uriToMatch, candidateUri) &&
                matchesEntityInstance(uriToMatch, candidateUri) &&
                matchesEntityVersion(uriToMatch, candidateUri);
    }

    /**
     * Checks if the resource of the uriToMatch matches the candidateUri.
     * A match occurs if the resource ID in uriToMatch is a wildcard
     * or if both URIs have the same resource ID.
     *
     * @param uriToMatch The URI to match.
     * @param candidateUri The candidate URI to match against.
     * @return True if the resource IDs match, False otherwise.
     */
    static boolean matchesResource(UUri uriToMatch, UUri candidateUri) {
        return UriFactory.WILDCARD_RESOURCE_ID == uriToMatch.getResourceId() ||
                uriToMatch.getResourceId() == candidateUri.getResourceId();
    }

    /**
     * Checks if the entire URI (authority, entity, and resource) of the uriToMatch
     * matches the candidateUri. A match occurs if the authority, entity, and resource
     * of both URIs match according to their respective rules.
     *
     * @param uriToMatch The URI to match.
     * @param candidateUri The candidate URI to match against.
     * @return True if the entire URIs match, False otherwise.
     */
    static boolean matches(UUri uriToMatch, UUri candidateUri) {
        return matchesAuthority(uriToMatch, candidateUri) &&
                matchesEntity(uriToMatch, candidateUri) &&
                matchesResource(uriToMatch, candidateUri);
    }
}


