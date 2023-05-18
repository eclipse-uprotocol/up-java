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

import com.google.rpc.Code;
import com.google.rpc.Status;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Wrapper class for RPC Stub calls. It contains a Success with the type of the RPC call, or a failure with the Status returned by the failed call.
 * @param <T> The type of the successful RPC call.
 */
public abstract class RpcResult<T> {

    private RpcResult() {}

    public abstract boolean isSuccess();
    public abstract boolean isFailure();
    public abstract T getOrElse(final T defaultValue);
    public abstract T getOrElse(final Supplier<T> defaultValue);

    public abstract <U> RpcResult<U> map(Function<T, U> f);

    public abstract <U> RpcResult<U> flatMap(Function<T, RpcResult<U>> f);

    public abstract RpcResult<T> filter(Function<T, Boolean> f);

    public abstract Status failureValue();

    public abstract T successValue();

    private static class Success<T> extends RpcResult<T> {

        private final T value;

        private Success(T value) {
            this.value = value;
        }


        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public boolean isFailure() {
            return false;
        }

        @Override
        public T getOrElse(T defaultValue) {
            return successValue();
        }

        @Override
        public T getOrElse(Supplier<T> defaultValue) {
            return successValue();
        }

        @Override
        public <U> RpcResult<U> map(Function<T, U> f) {
            try {
                return success(f.apply(successValue()));
            } catch (Exception e) {
                return failure(e.getMessage(), e);
            }
        }

        @Override
        public <U> RpcResult<U> flatMap(Function<T, RpcResult<U>> f) {
            try {
                return f.apply(successValue());
            } catch (Exception e) {
                return failure(e.getMessage(), e);
            }
        }

        @Override
        public RpcResult<T> filter(Function<T, Boolean> f) {
            try {
                return f.apply(successValue())
                        ? this
                        : failure(Code.FAILED_PRECONDITION, "filtered out");
            } catch (Exception e) {
                return failure(e.getMessage(), e);
            }
        }

        @Override
        public Status failureValue() {
            throw new IllegalStateException("Method failureValue() called on a Success instance");
        }

        @Override
        public T successValue() {
            return value;
        }

        @Override
        public String toString() {
            return String.format("Success(%s)", successValue().toString());
        }
    }

    private static class Failure<T> extends RpcResult<T> {

        private final Status value;

        private Failure(Status value) {
            this.value = value;
        }

        private Failure(Code code, String message) {
            this.value = Status.newBuilder()
                    .setCode(code.getNumber())
                    .setMessage(message).build();
        }

        private Failure(Exception e) {
            this.value = Status.newBuilder()
                    .setCode(Code.UNKNOWN_VALUE)
                    .setMessage(e.getMessage()).build();
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public boolean isFailure() {
            return true;
        }

        @Override
        public T getOrElse(T defaultValue) {
            return defaultValue;
        }

        @Override
        public T getOrElse(Supplier<T> defaultValue) {
            return defaultValue.get();
        }

        @Override
        public <U> RpcResult<U> map(Function<T, U> f) {
            return failure(this);
        }

        @Override
        public <U> RpcResult<U> flatMap(Function<T, RpcResult<U>> f) {
            return failure(failureValue());
        }

        @Override
        public RpcResult<T> filter(Function<T, Boolean> f) {
            return failure(this);
        }

        @Override
        public Status failureValue() {
            return value;
        }

        @Override
        public T successValue() {
            throw new IllegalStateException("Method successValue() called on a Failure instance");
        }

        @Override
        public String toString() {
            return String.format("Failure(%s)", value);
        }
    }

    public static <T> RpcResult<T> success(T value) {
        return new Success<>(value);
    }

    public static <T> RpcResult<T> failure(Status value) {
        return new Failure<>(value);
    }

    public static <T, U> RpcResult<T> failure(Failure<U> failure) {
        return new Failure<>(failure.value);
    }

    public static <T> RpcResult<T> failure(String message, Throwable e) {
        return new Failure<>(new IllegalStateException(message, e));
    }

    public static <T> RpcResult<T> failure(Code code, String message) {
        return new Failure<>(code, message);
    }

    public static <T> RpcResult<T> flatten(RpcResult<RpcResult<T>> result) {
        return result.flatMap(x -> x);
    }

}
