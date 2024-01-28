/*
 * Copyright (c) 2023 General Motors GTO LLC
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * SPDX-FileType: SOURCE
 * SPDX-FileCopyrightText: 2023 General Motors GTO LLC
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.uprotocol.cloudevent.datamodel;

import org.eclipse.uprotocol.v1.UPriority;

import java.util.Objects;
import java.util.Optional;

/**
 * Specifies the properties that can configure the UCloudEvent.
 */
public class UCloudEventAttributes {

    private static final UCloudEventAttributes EMPTY = new UCloudEventAttributes(null, null, null, null, null);

    private final String hash;
    private final UPriority priority;
    private final Integer ttl;
    private final String token;
    private final String traceparent;

    /**
     * Construct the properties object.
     *
     * @param hash     An HMAC generated on the data portion of the CloudEvent message using the device key.
     * @param priority uProtocol Prioritization classifications.
     * @param ttl      How long this event should live for after it was generated (in milliseconds).
     *                 Events without this attribute (or value is 0) MUST NOT timeout.
     * @param token    Oauth2 access token to perform the access request defined in the request message.
     */
    private UCloudEventAttributes(String hash, UPriority priority, Integer ttl, String token) {
        this(hash, priority, ttl, token, null);
    }

    /**
     * Construct the properties object.
     *
     * @param hash     An HMAC generated on the data portion of the CloudEvent message using the device key.
     * @param priority uProtocol Prioritization classifications.
     * @param ttl      How long this event should live for after it was generated (in milliseconds).
     *                 Events without this attribute (or value is 0) MUST NOT timeout.
     * @param token    Oauth2 access token to perform the access request defined in the request message.
     * @param traceparent    Optional identifier used to correlate observability across related events
     */
    private UCloudEventAttributes(String hash, UPriority priority, Integer ttl, String token, String traceparent) {
        this.hash = hash;
        this.priority = priority;
        this.ttl = ttl;
        this.token = token;
        this.traceparent = traceparent;
    }

    private UCloudEventAttributes(UCloudEventAttributesBuilder builder) {
        this.hash = builder.hash;
        this.priority = builder.priority;
        this.ttl = builder.ttl;
        this.token = builder.token;
        this.traceparent = builder.traceparent;
    }

    /**
     * Static factory method for creating an empty  cloud event attributes object, to avoid working with null<br>
     * @return Returns an empty  cloud event attributes that indicates
     * that there are no added additional attributes to configure.
     */
    public static UCloudEventAttributes empty() {
        return EMPTY;
    }

    /**
     * Indicates that there are no added additional attributes to configure when building a CloudEvent.
     * @return Returns true if this attributes container is an empty container and has no valuable information in building a CloudEvent.
     */
    public boolean isEmpty() {
        return hash().isEmpty() && priority().isEmpty() && ttl().isEmpty() && token().isEmpty() && traceparent().isEmpty();
    }

    /**
     * An HMAC generated on the data portion of the CloudEvent message using the device key.
     * @return Returns an Optional hash attribute.
     */
    public Optional<String> hash() {
        return hash == null || hash.isBlank() ? Optional.empty() : Optional.of(hash);
    }

    /**
     * uProtocol Prioritization classifications.
     * @return Returns an Optional priority attribute.
     */
    public Optional<UPriority> priority() {
        return priority == null ? Optional.empty() : Optional.of(priority);
    }

    /**
     * How long this event should live for after it was generated (in milliseconds).
     * @return Returns an Optional time to live attribute.
     */
    public Optional<Integer> ttl() {
        return ttl == null ? Optional.empty() : Optional.of(ttl);
    }
    
    /**
     * Oauth2 access token to perform the access request defined in the request message.
     * @return Returns an Optional OAuth token attribute.
     */
    public Optional<String> token() {
        return token == null || token.isBlank() ? Optional.empty() : Optional.of(token);
    }
    
    /**
     * An identifier used to correlate observability across related events.
     * @return Returns an Optional traceparent attribute.
     */
    public Optional<String> traceparent() {
        return traceparent == null || traceparent.isBlank() ? Optional.empty() : Optional.of(traceparent);
    }
    
    /**
     * Builder for constructing the UCloudEventAttributes.
     */
    public static class UCloudEventAttributesBuilder {
        private String hash;
        private UPriority priority;
        private Integer ttl;
        private String token;
        private String traceparent;

        public UCloudEventAttributesBuilder() {}

        /**
         * add an HMAC generated on the data portion of the CloudEvent message using the device key.
         * @param hash an HMAC generated on the data portion of the CloudEvent message using the device key.
         * @return Returns the UCloudEventAttributesBuilder with the configured hash.
         */
        public UCloudEventAttributesBuilder withHash(String hash) {
            this.hash = hash;
            return this;
        }

        /**
         * add a uProtocol Prioritization classifications.
         * @param priority uProtocol Prioritization classifications.
         * @return Returns the UCloudEventAttributesBuilder with the configured priority.
         */
        public UCloudEventAttributesBuilder withPriority(UPriority priority) {
            this.priority = priority;
            return this;
        }

        /**
         * add a time to live which is how long this event should live for after it was generated (in milliseconds).
         * Events without this attribute (or value is 0) MUST NOT timeout.
         * @param ttl How long this event should live for after it was generated (in milliseconds).
         *            Events without this attribute (or value is 0) MUST NOT timeout.
         * @return Returns the UCloudEventAttributesBuilder with the configured time to live.
         */
        public UCloudEventAttributesBuilder withTtl(Integer ttl) {
            this.ttl = ttl;
            return this;
        }

        /**
         * Add an Oauth2 access token to perform the access request defined in the request message.
         * @param token An Oauth2 access token to perform the access request defined in the request message.
         * @return Returns the UCloudEventAttributesBuilder with the configured OAuth token.
         */
        public UCloudEventAttributesBuilder withToken(String token) {
            this.token = token;
            return this;
        }

        /**
         * Add an identifier used to correlate observability across related events.
         * @param traceparent An identifier used to correlate observability across related events.
         * @return Returns the UCloudEventAttributesBuilder with the configured traceparent.
         */
        public UCloudEventAttributesBuilder withTraceparent(String traceparent) {
            this.traceparent = traceparent;
            return this;
        }
        
        /**
         * Construct the UCloudEventAttributes from the builder.
         * @return Returns a constructed UProperty.
         */
        public UCloudEventAttributes build() {
            // validation if needed
            return new UCloudEventAttributes(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UCloudEventAttributes that = (UCloudEventAttributes) o;
        return Objects.equals(hash, that.hash) && priority == that.priority
                && Objects.equals(ttl, that.ttl) && Objects.equals(token, that.token)
                && Objects.equals(traceparent, that.traceparent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash, priority, ttl, token, traceparent);
    }

    @Override
    public String toString() {
        String traceParentString = "";
        if (traceparent != null) {
            traceParentString = ", traceparent='" + traceparent + '\'';
        }
        return "UCloudEventAttributes{" +
                "hash='" + hash + '\'' +
                ", priority=" + priority +
                ", ttl=" + ttl +
                ", token='" + token + '\'' +
                traceParentString +
                '}';
    }
}
