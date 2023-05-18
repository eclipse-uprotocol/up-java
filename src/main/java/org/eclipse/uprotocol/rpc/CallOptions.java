/*
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

package org.eclipse.uprotocol.rpc;

import java.util.Objects;
import java.util.Optional;

/**
 * This class is used when making uRPC calls to pass additional options. Copied from Misha's class.
 */
public class CallOptions {

    /**
     * Default timeout of a call in milliseconds.
     */
    public static final int TIMEOUT_DEFAULT = 10000;

    /**
     * Default instance.
     */
    public static final CallOptions DEFAULT = new CallOptions(TIMEOUT_DEFAULT, "");

    private final int mTimeout;
    private final String mToken;

    private CallOptions (Builder builder) {
        this(builder.mTimeout, builder.mToken);
    }

    private CallOptions(int timeout,  String token) {
        mTimeout = timeout;
        mToken = (token != null) ? token : "";
    }

    /**
     * Constructs a new builder.
     *
     * @return A builder.
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Get a timeout.
     *
     * @return A timeout in milliseconds.
     */
    public int timeout() {
        return mTimeout;
    }

    /**
     * Get an OAuth2 access token.
     *
     * @return An Optional OAuth2 access token.
     */
    public Optional<String> token() {
        return mToken.isBlank() ? Optional.empty() : Optional.of(mToken);
    }

    /**
     * Builder for constructing <code>CallOptions</code>.
     */
    public static final class Builder {
        private int mTimeout = TIMEOUT_DEFAULT;
        private String mToken = "";

        private Builder() {}

        /**
         * Add a timeout.
         *
         * @param timeout A timeout in milliseconds.
         * @return This builder.
         */
        public  Builder withTimeout(int timeout) {
            mTimeout = (timeout <= 0) ? TIMEOUT_DEFAULT : timeout;
            return this;
        }

        /**
         * Add an OAuth2 access token.
         *
         * @param token An OAuth2 access token.
         * @return This builder.
         */
        public Builder withToken(String token) {
            mToken = token;
            return this;
        }

        /**
         * Construct a <code>CallOptions</code> from this builder.
         *
         * @return A constructed <code>CallOptions</code>.
         */
        public CallOptions build() {
            return new CallOptions(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CallOptions that = (CallOptions) o;
        return mTimeout == that.mTimeout && Objects.equals(mToken, that.mToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mTimeout, mToken);
    }

    @Override
    public String toString() {
        return "CallOptions{" +
                "mTimeout=" + mTimeout +
                ", mToken='" + mToken + '\'' +
                '}';
    }
}
