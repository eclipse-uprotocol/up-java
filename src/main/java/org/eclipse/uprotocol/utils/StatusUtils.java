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
package org.eclipse.uprotocol.utils;


import javax.annotation.Nullable;

import javax.annotation.Nonnull;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.rpc.Code;
import com.google.rpc.Status;

import java.util.Collection;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@SuppressWarnings({"java:S1200", "java:S6212"})
public final class StatusUtils {
    public static final Status STATUS_OK = buildStatus(Code.OK);

    private StatusUtils() {}

    public static boolean isOk(@Nullable Status status) {
        return status != null && status.getCode() == Code.OK_VALUE;
    }

    public static boolean hasCode(@Nullable Status status, int code) {
        return (status != null) && status.getCode() == code;
    }

    public static boolean hasCode(@Nullable Status status, @Nonnull Code code) {
        return (status != null) && status.getCode() == code.getNumber();
    }

    public static @Nonnull Code getCode(@Nullable Status status, @Nonnull Code defaultCode) {
        final Code code = (status != null) ? Code.forNumber(status.getCode()) : defaultCode;
        return (code != null) ? code : defaultCode;
    }

    public static @Nonnull Code getCode(@Nullable Status status) {
        return getCode(status, Code.UNKNOWN);
    }

    public static @Nonnull Code toCode(int value) {
        final Code code = Code.forNumber(value);
        return (code != null) ? code : Code.UNKNOWN;
    }

    public static Status.Builder newStatusBuilder(@Nonnull Code code) {
        return Status.newBuilder().setCode(code.getNumber());
    }

    public static Status.Builder newStatusBuilder(@Nonnull Code code, @Nullable String message) {
        return newStatusBuilder(code).setMessage(message != null ? message : "");
    }

    public static Status buildStatus(@Nonnull Code code) {
        return newStatusBuilder(code).build();
    }

    public static Status buildStatus(@Nonnull Code code, @Nullable String message) {
        return newStatusBuilder(code, message).build();
    }

    public static Status throwableToStatus(@Nonnull Throwable exception) {
        if (exception instanceof StatusException) {
            return ((StatusException) exception).getStatus();
        } else if (exception instanceof CompletionException || (exception instanceof ExecutionException)) {
            final Throwable cause = exception.getCause();
            if (cause instanceof StatusException) {
                return ((StatusException) cause).getStatus();
            } else if (cause != null) {
                return buildStatus(throwableToCode(cause), cause.getMessage());
            }
        }
        return buildStatus(throwableToCode(exception), exception.getMessage());
    }

    @SuppressWarnings({"java:S1541", "java:S3776"})
    private static @Nonnull Code throwableToCode(@Nonnull Throwable exception) {
        if (exception instanceof SecurityException) {
            return Code.PERMISSION_DENIED;
        } else if (exception instanceof InvalidProtocolBufferException) {
            return Code.INVALID_ARGUMENT;
        } else if (exception instanceof IllegalArgumentException) {
            return Code.INVALID_ARGUMENT;
        } else if (exception instanceof NullPointerException) {
            return Code.INVALID_ARGUMENT;
        } else if (exception instanceof CancellationException) {
            return Code.CANCELLED;
        } else if (exception instanceof IllegalStateException) {
            return Code.UNAVAILABLE;
        } else if (exception instanceof UnsupportedOperationException) {
            return Code.UNIMPLEMENTED;
        } else if (exception instanceof InterruptedException) {
            return Code.CANCELLED;
        } else if (exception instanceof TimeoutException) {
            return Code.DEADLINE_EXCEEDED;
        } else {
            return Code.UNKNOWN;
        }
    }

    public static void checkStatusOk(@Nonnull Status status) {
        if (!isOk(status)) {
            throw new StatusException(status);
        }
    }

    public static void checkArgument(boolean expression, @Nullable String errorMessage) {
        if (!expression) {
            throw new StatusException(Code.INVALID_ARGUMENT, errorMessage);
        }
    }

    public static void checkArgument(boolean expression, @Nonnull Code errorCode, @Nullable String errorMessage) {
        if (!expression) {
            throw new StatusException(errorCode, errorMessage);
        }
    }

    public static int checkArgumentPositive(int value, @Nullable String errorMessage) {
        if (value <= 0) {
            throw new StatusException(Code.INVALID_ARGUMENT, errorMessage);
        }
        return value;
    }

    public static int checkArgumentPositive(int value, @Nonnull Code errorCode, @Nullable String errorMessage) {
        if (value <= 0) {
            throw new StatusException(errorCode, errorMessage);
        }
        return value;
    }

    public static int checkArgumentNonNegative(int value, @Nullable String errorMessage) {
        if (value < 0) {
            throw new StatusException(Code.INVALID_ARGUMENT, errorMessage);
        }
        return value;
    }

    public static int checkArgumentNonNegative(int value, @Nonnull Code errorCode, @Nullable String errorMessage) {
        if (value < 0) {
            throw new StatusException(errorCode, errorMessage);
        }
        return value;
    }

    @SuppressWarnings("UnusedReturnValue")
    public static @Nonnull <T> T checkNotNull(@Nullable T reference, @Nullable String errorMessage) {
        if (reference == null) {
            throw new StatusException(Code.INVALID_ARGUMENT, errorMessage);
        }
        return reference;
    }

    public static @Nonnull <T> T checkNotNull(@Nullable T reference, @Nonnull Code errorCode,
            @Nullable String errorMessage) {
        if (reference == null) {
            throw new StatusException(errorCode, errorMessage);
        }
        return reference;
    }

    public static void checkState(boolean expression, @Nullable String errorMessage) {
        if (!expression) {
            throw new StatusException(Code.FAILED_PRECONDITION, errorMessage);
        }
    }

    public static void checkState(boolean expression, @Nonnull Code errorCode, @Nullable String errorMessage) {
        if (!expression) {
            throw new StatusException(errorCode, errorMessage);
        }
    }

    public static @Nonnull <C extends Collection<T>, T> T checkCollectionContains(
            @Nonnull C values, @Nonnull T value, @Nonnull String valueName) {
        checkNotNull(values, valueName + " '" + value + "' is not supported");
        checkNotNull(value, valueName + " is null");
        if (!values.contains(value)) {
            throw new StatusException(Code.INVALID_ARGUMENT, valueName + " '" + value + "' is not supported");
        }
        return value;
    }
}