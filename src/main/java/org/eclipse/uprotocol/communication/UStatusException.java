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


import org.eclipse.uprotocol.v1.UStatus;
import org.eclipse.uprotocol.v1.UCode;


/**
 * The unchecked exception which carries uProtocol error model.
 */
public class UStatusException extends RuntimeException {
    private final UStatus mStatus;

    /**
     * Constructs an instance.
     *
     * @param code    An error {@link UCode}.
     * @param message An error message.
     */
    public UStatusException(UCode code, String message) {
        this(UStatus.newBuilder().setCode(code).setMessage(message).build(), null);
    }

    /**
     * Constructs an instance.
     *
     * @param code    An error {@link UCode}.
     * @param message An error message.
     * @param cause   An exception that caused this one.
     */
    public UStatusException(UCode code, String message, Throwable cause) {
        this(UStatus.newBuilder().setCode(code).setMessage(message).build(), cause);
    }

    /**
     * Constructs an instance.
     *
     * @param status An error {@link UStatus}.
     */
    public UStatusException(UStatus status) {
        this(status, null);
    }

    /**
     * Constructs an instance.
     *
     * @param status An error {@link UStatus}.
     * @param cause  An exception that caused this one.
     */
    public UStatusException(UStatus status, Throwable cause) {
        super((status != null) ? status.getMessage() : "", cause);
        mStatus = (status != null) ? status : UStatus.newBuilder().setCode(UCode.UNKNOWN).build();
    }

    /**
     * Get the error status.
     * @return The error {@link UStatus}.
     */
    public UStatus getStatus() {
        return mStatus;
    }

    /**
     * Get the error code.
     * @return The error {@link UCode}.
     */
    public UCode getCode() {
        return mStatus.getCode();
    }

    /**
     * Get the error message.
     * @return The error message.
     */
    @Override
    public String getMessage() {
        return mStatus.getMessage();
    }
}
