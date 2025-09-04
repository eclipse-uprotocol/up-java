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
package org.eclipse.uprotocol.communication;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UStatus;

/**
 * Wrapper class for RPC Stub calls. It contains a Success with the type of the RPC call, 
 * or a failure with the UStatus returned by the failed call.
 * @param <T> The type of the successful RPC call.
 */
@Deprecated(forRemoval = true)
public abstract class RpcResult<T> {

    private RpcResult() {
    }

    public abstract boolean isSuccess();
    public abstract boolean isFailure();
    public abstract T getOrElse(T defaultValue);
    public abstract T getOrElse(Supplier<T> defaultValue);

    public abstract <U> RpcResult<U> map(Function<T, U> f);

    public abstract <U> RpcResult<U> flatMap(Function<T, RpcResult<U>> f);

    public abstract RpcResult<T> filter(Function<T, Boolean> f);

    public abstract UStatus failureValue();

    public abstract T successValue();

    private static final class Success<T> extends RpcResult<T> {

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
                        : failure(UCode.FAILED_PRECONDITION, "filtered out");
            } catch (Exception e) {
                return failure(e.getMessage(), e);
            }
        }

        @Override
        public UStatus failureValue() {
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

    private static final class Failure<T> extends RpcResult<T> {

        private final UStatus value;

        private Failure(UStatus value) {
            this.value = value;
        }

        private Failure(UCode code, String message) {
            this.value = UStatus.newBuilder()
                    .setCode(code)
                    .setMessage(message).build();
        }

        private Failure(Exception e) {
            this.value = UStatus.newBuilder()
                    .setCode(UCode.UNKNOWN)
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
        public UStatus failureValue() {
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

    public static <T> RpcResult<T> failure(UStatus value) {
        return new Failure<>(value);
    }

    public static <T, U> RpcResult<T> failure(Failure<U> failure) {
        return new Failure<>(failure.value);
    }

    public static <T> RpcResult<T> failure(String message, Throwable e) {
        return new Failure<>(new IllegalStateException(message, e));
    }

    public static <T> RpcResult<T> failure(UCode code, String message) {
        return new Failure<>(code, Objects.requireNonNullElse(message, "No message provided"));
    }

    public static <T> RpcResult<T> flatten(RpcResult<RpcResult<T>> result) {
        return result.flatMap(x -> x);
    }

}
