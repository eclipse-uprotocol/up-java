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
     * major version wildcard
     */
    int MAJOR_VERSION_WILDCARD = 0xFF;


    /**
     * Indicates that this  URI is an empty as it does not contain authority, entity, and resource.
     *
     * @param uri {@link UUri} to check if it is empty
     * @return Returns true if this  URI is an empty container and has no valuable information in building uProtocol sinks or sources.
     */
    static boolean isEmpty(UUri uri) {
        return (uri == null || uri.equals(UUri.getDefaultInstance()));
    }


    /**
     * Returns true if URI is of type RPC. A UUri is of type RPC if it contains the word rpc in the resource name 
     * and has an instance name and/or the id is less than MIN_TOPIC_ID.
     *
     * @param uri {@link UUri} to check if it is of type RPC method
     * @return Returns true if URI is of type RPC.
     */
    static boolean isRpcMethod(UUri uri) {
        return !isEmpty(uri) && 
            uri.getResourceId() != DEFAULT_RESOURCE_ID &&
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
}
