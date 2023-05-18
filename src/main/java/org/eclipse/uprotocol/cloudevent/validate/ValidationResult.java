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

package org.eclipse.uprotocol.cloudevent.validate;

import com.google.rpc.Code;
import com.google.rpc.Status;

import java.util.Objects;

/**
 * Class wrapping a ValidationResult of success or failure wrapping the value of a google.rpc.Status.
 */
public abstract class ValidationResult {

    public static final Status STATUS_SUCCESS = Status.newBuilder().setCode(Code.OK_VALUE).setMessage("OK").build();

    private static final ValidationResult SUCCESS = new Success();

    private ValidationResult(){}

    public abstract Status toStatus();

    public abstract boolean isSuccess();

    public boolean isFailure() {
        return !isSuccess();
    }

    public abstract String getMessage();

    /**
     * Implementation for failure, wrapping the message.
     */
    private static class Failure extends ValidationResult {
        private final String message;

        private Failure(String message) {
            this.message = Objects.requireNonNullElse(message, "Validation Failed.");
        }

        @Override
        public Status toStatus() {
            return Status.newBuilder().setCode(3).setMessage(message).build();
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return "ValidationResult.Failure(" + "message='" + message + '\'' + ')';
        }
    }

    /**
     * Implementation for success, wrapping a google.rpc.Status with Code 0 for success.
     */
    private static class Success extends ValidationResult {

        @Override
        public Status toStatus() {
            return STATUS_SUCCESS;
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public String getMessage() {
            return "";
        }

        @Override
        public String toString() {
            return "ValidationResult.Success()";
        }
    }

    public static ValidationResult success() {
        return SUCCESS;
    }

    public static ValidationResult failure(String message) {
        return new Failure(message);
    }

}
