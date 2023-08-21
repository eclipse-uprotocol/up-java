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

package org.eclipse.uprotocol.utransport;

import java.util.Objects;
import java.util.Optional;

public class UAttributes {

    private static final UAttributes EMPTY = new UAttributes(null, null, null, null, null, null, null);

    private final String hash;
    private final Priority priority;
    private final Integer ttl;
    private final String token;
    private final Integer serializationHint;

    private final String sink;
    private final String requestId;

    /**
     * Construct the transport properties object.
     *
     * @param hash              an HMAC generated on the data portion of the payload using the device key.
     * @param priority          uProtocol Prioritization classifications.
     * @param ttl               How long this event should live for after it was generated (in milliseconds).
     *                          Events without this attribute (or value is 0) MUST NOT timeout.
     * @param token             Oauth2 access token to perform the access request defined in the request message.
     * @param serializationHint hint regarding the bytes contained within the UPayload.
     * @param sink              an explicit destination URI.
     * @param requestId         The requestId is used to return a response for a specific request.
     */
    private UAttributes(String hash, Priority priority, Integer ttl,
                                 String token, Integer serializationHint, String sink,
                                 String requestId) {
        this.hash = hash;
        this.priority = priority;
        this.ttl = ttl;
        this.token = token;
        this.serializationHint = serializationHint;
        this.sink = sink;
        this.requestId = requestId;
    }

    private UAttributes(UTransportAttributesBuilder builder) {
        this(builder.hash, builder.priority, builder.ttl, builder.token,
                builder.serializationHint, builder.sink, builder.requestId);
    }

    /**
     * Static factory method for creating an empty ultifi cloud event attributes object, to avoid working with null.
     * @return Returns an empty transport attributes that indicates that there are no added additional attributes to configure.
     */
    public static UAttributes empty() {
        return EMPTY;
    }

    public boolean isEmpty() {
        return hash().isEmpty() && priority().isEmpty() && ttl().isEmpty()
                && token().isEmpty() && serializationHint().isEmpty()
                && requestId().isEmpty();
    }

    /**
     * an HMAC generated on the data portion of the payload using the device key.
     * @return Returns an Optional hash attribute.
     */
    public Optional<String> hash() {
        return hash == null || hash.isBlank() ? Optional.empty() : Optional.of(hash);
    }

    /**
     * uProtocol Prioritization classifications. Default if not provided is LOW.
     * @return Returns the configured uProtocol Prioritization classifications.
     */
    public Optional<Priority> priority() {
        return priority == null ? Optional.empty() : Optional.of(priority);
    }

    /**
     * hint regarding the bytes contained within the UPayload.
     * @return Returns an Optional hint regarding the bytes contained within the UPayload.
     */
    public Optional<Integer> ttl() {
        return ttl == null ? Optional.empty() : Optional.of(this.ttl);
    }

    /**
     * Oauth2 access token to perform the access request defined in the request message.
     * @return Returns an Optional token attribute.
     */
    public Optional<String> token() {
        return token == null || token.isBlank() ? Optional.empty() : Optional.of(token);
    }

    /**
     * How long this event should live for after it was generated (in milliseconds).
     * Events without this attribute (or value is 0) MUST NOT timeout.
     * @return Returns an Optional time to live attribute.
     */
    public Optional<Integer> serializationHint() {
        return serializationHint == null ? Optional.empty() : Optional.of(this.serializationHint);
    }

    /**
     * an explicit destination URI.
     * @return Returns an Optional destination URI attribute.
     */
    public Optional<String> sink() {
        return sink == null || sink.isBlank() ? Optional.empty() : Optional.of(sink);
    }

    /**
     * The requestId is used to return a response for a specific request.
     * @return Returns an Optional requestId attribute.
     */
    public Optional<String> requestId() {
        return requestId == null || requestId.isBlank() ? Optional.empty() : Optional.of(requestId);
    }

    public static class UTransportAttributesBuilder {
        private String hash;
        private Priority priority;
        private Integer ttl;
        private String token;
        private Integer serializationHint;
        private String sink;
        private String requestId;

        public UTransportAttributesBuilder() {}

        /**
         * Add an HMAC generated on the data portion of the payload using the device key.
         * @param hash an HMAC generated on the data portion of the CloudEvent message using the device key.
         * @return Returns the UTransportAttributesBuilder with the configured hash.
         */
        public UTransportAttributesBuilder withHash(String hash) {
            this.hash = hash;
            return this;
        }

        /**
         * Add uProtocol Prioritization classifications.
         * @param priority the uProtocol Prioritization classifications.
         * @return Returns the UTransportAttributesBuilder with the configured Priority.
         */
        public UTransportAttributesBuilder withPriority(Priority priority) {
            this.priority = priority;
            return this;
        }

        /**
         * Add a time to live configuration. How long this event should live after it was generated.
         * @param ttl How long this event should live for after it was generated (in milliseconds).
         *            Events without this attribute (or value is 0) MUST NOT timeout.
         * @return Returns the UTransportAttributesBuilder with the configured time to live.
         */
        public UTransportAttributesBuilder withTtl(Integer ttl) {
            this.ttl = ttl;
            return this;
        }

        /**
         * Add an Oauth2 access token to perform the access request defined in the request message.
         * @param token an Oauth2 access token to perform the access request defined in the request message.
         * @return Returns the UTransportAttributesBuilder with the configured token.
         */
        public UTransportAttributesBuilder withToken(String token) {
            this.token = token;
            return this;
        }

        /**
         * Add a hint regarding the bytes contained within the UPayload.
         * @param serializationHint hint regarding the bytes contained within the UPayload.
         * @return Returns the UTransportAttributesBuilder with the configured serialization hint.
         */
        public UTransportAttributesBuilder withSerializationHint(Integer serializationHint) {
            this.serializationHint = serializationHint;
            return this;
        }

        /**
         * Add an explicit destination URI.
         * @param sink an explicit destination URI.
         * @return Returns the UTransportAttributesBuilder with the configured destination URI.
         */
        public UTransportAttributesBuilder withSink(String sink) {
            this.sink = sink;
            return this;
        }

        /**
         * The requestId is used to return a response for a specific request.
         * @param requestId a requestId that is used to return a response for a specific request.
         * @return Returns the UTransportAttributesBuilder with the configured requestId.
         */
        public UTransportAttributesBuilder withRequestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        /**
         * Construct the UTransportAttributes from the builder.
         * @return Returns a constructed UTransportAttributes.
         */
        public UAttributes build() {
            // validation if needed
            return new UAttributes(this);
        }
    }

    

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UAttributes that = (UAttributes) o;
        return Objects.equals(hash, that.hash) && priority == that.priority && Objects.equals(ttl, that.ttl)
                && Objects.equals(token, that.token) && Objects.equals(serializationHint, that.serializationHint)
                && Objects.equals(sink, that.sink) && Objects.equals(requestId, that.requestId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash, priority, ttl, token, serializationHint, sink, requestId);
    }

    @Override
    public String toString() {
        return "UTransportAttributes{" +
                "hash='" + hash + '\'' +
                ", priority=" + priority +
                ", ttl=" + ttl +
                ", token='" + token + '\'' +
                ", serializationHint=" + serializationHint +
                ", sink='" + sink + '\'' +
                ", requestId='" + requestId + '\'' +
                '}';
    }
}
