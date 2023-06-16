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
 */

package org.eclipse.uprotocol.cloudevent.datamodel;

import java.util.Objects;
import java.util.Optional;

/**
 * Specifies the properties that can configure the UCloudEvent.
 */
public class UCloudEventAttributes {

    private static final UCloudEventAttributes EMPTY = new UCloudEventAttributes(null, null, null, null);

    private final String hash;
    private final Priority priority;
    private final Integer ttl;
    private final String token;

    /**
     * Construct the properties object.
     *
     * @param hash     an HMAC generated on the data portion of the CloudEvent message using the device key.
     * @param priority uProtocol Prioritization classifications defined at QoS in SDV-202.
     * @param ttl      How long this event should live for after it was generated (in milliseconds).
     *                 Events without this attribute (or value is 0) MUST NOT timeout.
     * @param token    Oauth2 access token to perform the access request defined in the request message.
     */
    private UCloudEventAttributes(String hash, Priority priority, Integer ttl, String token) {
        this.hash = hash;
        this.priority = priority;
        this.ttl = ttl;
        this.token = token;
    }

    private UCloudEventAttributes(UCloudEventAttributesBuilder builder) {
        this.hash = builder.hash;
        this.priority = builder.priority;
        this.ttl = builder.ttl;
        this.token = builder.token;
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
        return hash().isEmpty() && priority().isEmpty() && ttl().isEmpty() && token().isEmpty();
    }

    /**
     * An HMAC generated on the data portion of the CloudEvent message using the device key.
     * @return Returns an Optional hash attribute.
     */
    public Optional<String> hash() {
        return hash == null || hash.isBlank() ? Optional.empty() : Optional.of(hash);
    }

    /**
     * uProtocol Prioritization classifications defined at QoS in SDV-202.
     * @return Returns an Optional priority attribute.
     */
    public Optional<Priority> priority() {
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
     * Builder for constructing the UCloudEventAttributes.
     */
    public static class UCloudEventAttributesBuilder {
        private String hash;
        private Priority priority;
        private Integer ttl;
        private String token;

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
         * add a uProtocol Prioritization classifications defined at QoS in SDV-202.
         * @param priority uProtocol Prioritization classifications defined at QoS in SDV-202.
         * @return Returns the UCloudEventAttributesBuilder with the configured priority.
         */
        public UCloudEventAttributesBuilder withPriority(Priority priority) {
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
         * Construct the UCloudEventAttributes from the builder.
         * @return Returns a constructed UProperty.
         */
        public UCloudEventAttributes build() {
            // validation if needed
            return new UCloudEventAttributes(this);
        }
    }

    /**
     * Priority according to SDV 202 Quality of Service (QoS) and Prioritization.
     */
    public enum Priority {
        // Low Priority. No bandwidth assurance such as File Transfer.
        LOW ("CS0"),
        // Standard, undifferentiated application such as General (unclassified).
        STANDARD ("CS1"),
        // Operations, Administration, and Management such as Streamer messages (sub, connect, etc…)
        OPERATIONS ("CS2"),
        // Multimedia streaming such as Video Streaming
        MULTIMEDIA_STREAMING ("CS3"),
        // Real-time interactive such as High priority (rpc events)
        REALTIME_INTERACTIVE ("CS4"),
        // Signaling such as Important
        SIGNALING("CS5"),
        // Network control such as Safety Critical
        NETWORK_CONTROL ("CS6");

        private final String qosString;
        public String qosString() {
            return qosString;
        }

        Priority(String qosString) {
            this.qosString = qosString;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UCloudEventAttributes that = (UCloudEventAttributes) o;
        return Objects.equals(hash, that.hash) && priority == that.priority
                && Objects.equals(ttl, that.ttl) && Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash, priority, ttl, token);
    }

    @Override
    public String toString() {
        return "UCloudEventAttributes{" +
                "hash='" + hash + '\'' +
                ", priority=" + priority +
                ", ttl=" + ttl +
                ", token='" + token + '\'' +
                '}';
    }
}
