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
 * 
 * SPDX-FileType: SOURCE
 * SPDX-FileCopyrightText: 2023 General Motors GTO LLC
 * SPDX-License-Identifier: Apache-2.0
 */
package org.eclipse.uprotocol.transport.validate;


import org.eclipse.uprotocol.uri.validator.UriValidator;
import org.eclipse.uprotocol.uuid.factory.UuidUtils;
import org.eclipse.uprotocol.v1.*;
import org.eclipse.uprotocol.validation.ValidationResult;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * {@link UAttributes} is the class that defines the Payload. It is the place for configuring time to live, priority,
 * security tokens and more.
 * Each UAttributes class defines a different type of message payload. The payload can represent a simple published
 * payload with some state change,
 * Payload representing an RPC request or Payload representing an RPC response.
 * UAttributesValidator is a base class for all UAttribute validators, that can help validate that the
 * {@link UAttributes} object is correctly defined
 * to define the Payload correctly.
 */
public abstract class UAttributesValidator {

    /**
     * Static factory method for getting a validator according to the {@link UMessageType} defined in the
     * {@link UAttributes}.
     *
     * @param attribute UAttributes containing the UMessageType.
     * @return returns a UAttributesValidator according to the {@link UMessageType} defined in the {@link UAttributes}.
     */
    public static UAttributesValidator getValidator(UAttributes attribute) {

        switch (attribute.getType()) {
            case UMESSAGE_TYPE_RESPONSE:
                return Validators.RESPONSE.validator();
            case UMESSAGE_TYPE_REQUEST:
                return Validators.REQUEST.validator();
            default:
                return Validators.PUBLISH.validator();
        }
    }

    /**
     * Take a {@link UAttributes} object and run validations.
     *
     * @param attributes The UAttriubes to validate.
     * @return Returns a {@link ValidationResult} that is success or failed with a message containing all validation
     * errors for
     * invalid configurations.
     */
    public ValidationResult validate(UAttributes attributes) {
        final String errorMessage = Stream.of(validateType(attributes),
                         validateTtl(attributes), validateSink(attributes),
                         validatePermissionLevel(attributes), validateReqId(attributes))
                .filter(ValidationResult::isFailure).map(ValidationResult::getMessage).collect(Collectors.joining(","));
        return errorMessage.isBlank() ? ValidationResult.success() : ValidationResult.failure(errorMessage);
    }

    /**
     * Check the time-to-live attribute to see if it has expired. <br>
     * The message has expired when the current time is greater than the original UUID time
     * plus the ttl attribute.
     *
     * @param uAttributes UAttributes with time to live value.
     * @return Returns a true if the original time plus the ttl is less than the current time
     */
    public boolean isExpired(UAttributes uAttributes) {
        final int ttl = uAttributes.getTtl();
        final Optional<Long> maybeTime = UuidUtils.getTime(uAttributes.getId());
        
        // if the message does not have a ttl or the original time is not present or the ttl is less than 0
        if (!uAttributes.hasTtl() || maybeTime.isEmpty() || ttl <= 0) {
            return false;
        }

        // the original time plus the ttl is less than the current time, the message has expired 
        return (maybeTime.get() + ttl) < System.currentTimeMillis();
    }


    /**
     * Validate the time to live configuration. If the UAttributes does not contain a time to live then the
     * ValidationResult is ok.
     *
     * @param attributes UAttributes object containing the message time to live configuration to validate.
     * @return Returns a {@link ValidationResult} that is success or failed with a failure message.
     */
    public ValidationResult validateTtl(UAttributes attributes) {
        int ttl = attributes.getTtl();
        if (attributes.hasTtl() && ttl <= 0) {
            return ValidationResult.failure(String.format("Invalid TTL [%s]", ttl));
        } else {
            return ValidationResult.success();
        }

    }

    /**
     * Validate the sink UriPart for the default case. If the UAttributes does not contain a sink then the
     * ValidationResult is ok.
     *
     * @param attributes UAttributes object containing the sink to validate.
     * @return Returns a {@link ValidationResult} that is success or failed with a failure message.
     */
    public ValidationResult validateSink(UAttributes attributes) {
        if (attributes.hasSink()) {
            return UriValidator.validate(attributes.getSink());
        } else {
            return ValidationResult.success();
        }
    }

    /**
     * Validate the permissionLevel for the default case. If the UAttributes does not contain a permission level then
     * the ValidationResult is ok.
     *
     * @param attributes UAttributes object containing the permission level to validate.
     * @return Returns a ValidationResult indicating if the permissionLevel is valid or not.
     */
    public ValidationResult validatePermissionLevel(UAttributes attributes) {
        if (!attributes.hasPermissionLevel() || attributes.getPermissionLevel() > 0) {
            return ValidationResult.success();
        } else {
            return ValidationResult.failure("Invalid Permission Level");
        }
    }

    
    /**
     * Validate the correlationId for the default case. If the UAttributes does not contain a request id then the
     * ValidationResult is ok.
     *
     * @param attributes Attributes object containing the request id to validate.
     * @return Returns a {@link ValidationResult} that is success or failed with a failure message.
     */
    public ValidationResult validateReqId(UAttributes attributes) {
        if (attributes.hasReqid() && !UuidUtils.isUuid(attributes.getReqid())) {
            return ValidationResult.failure("Invalid UUID");
        } else {
            return ValidationResult.success();
        }
    }

    /**
     * Validate the {@link UMessageType} attribute, it is required.
     *
     * @param attributes UAttributes object containing the message type to validate.
     * @return Returns a {@link ValidationResult} that is success or failed with a failure message.
     */
    public abstract ValidationResult validateType(UAttributes attributes);

    /**
     * Validators Factory. Example:
     * UAttributesValidator validateForPublishMessageType = UAttributesValidator.Validators.PUBLISH.validator()
     */
    public enum Validators {
        PUBLISH(new Publish()), REQUEST(new Request()), RESPONSE(new Response());

        private final UAttributesValidator uattributesValidator;

        Validators(UAttributesValidator uattributesValidator) {
            this.uattributesValidator = uattributesValidator;
        }

        public UAttributesValidator validator() {
            return uattributesValidator;
        }
    }

    /**
     * Implements validations for UAttributes that define a message that is meant for publishing state changes.
     */
    private static class Publish extends UAttributesValidator {

        /**
         * Validates that attributes for a message meant to publish state changes has the correct type.
         *
         * @param attributes UAttributes object containing the message type to validate.
         * @return Returns a {@link ValidationResult} that is success or failed with a failure message.
         */
        @Override
        public ValidationResult validateType(UAttributes attributes) {
            return UMessageType.UMESSAGE_TYPE_PUBLISH == attributes.getType() ? ValidationResult.success() : ValidationResult.failure(
                    String.format("Wrong Attribute Type [%s]", attributes.getType()));
        }

        @Override
        public String toString() {
            return "UAttributesValidator.Publish";
        }
    }

    /**
     * Implements validations for UAttributes that define a message that is meant for an RPC request.
     */
    private static class Request extends UAttributesValidator {

        /**
         * Validates that attributes for a message meant for an RPC request has the correct type.
         *
         * @param attributes UAttributes object containing the message type to validate.
         * @return Returns a {@link ValidationResult} that is success or failed with a failure message.
         */
        @Override
        public ValidationResult validateType(UAttributes attributes) {
            return UMessageType.UMESSAGE_TYPE_REQUEST == attributes.getType() ? ValidationResult.success() : ValidationResult.failure(
                    String.format("Wrong Attribute Type [%s]", attributes.getType()));
        }

        /**
         * Validates that attributes for a message meant for an RPC request has a destination sink.
         * In the case of an RPC request, the sink is required.
         *
         * @param attributes UAttributes object containing the sink to validate.
         * @return Returns a {@link ValidationResult} that is success or failed with a failure message.
         */
        @Override
        public ValidationResult validateSink(UAttributes attributes) {
            if (!attributes.hasSink()) {
                return ValidationResult.failure("Missing Sink");
            }
            return UriValidator.validateRpcMethod(attributes.getSink());

        }

        /**
         * Validate the time to live configuration.
         * In the case of an RPC request, the time to live is required.
         *
         * @param attributes UAttributes object containing the time to live to validate.
         * @return Returns a {@link ValidationResult} that is success or failed with a failure message.
         */
        @Override
        public ValidationResult validateTtl(UAttributes attributes) {
            if (!attributes.hasTtl()) {
                return ValidationResult.failure("Missing TTL");
            }
            int ttl = attributes.getTtl();
            if (ttl <= 0) {
                return ValidationResult.failure(String.format("Invalid TTL [%s]", ttl));
            } else {
                return ValidationResult.success();
            }
        }

        @Override
        public String toString() {
            return "UAttributesValidator.Request";
        }
    }

    /**
     * Implements validations for UAttributes that define a message that is meant for an RPC response.
     */
    private static class Response extends UAttributesValidator {

        /**
         * Validates that attributes for a message meant for an RPC response has the correct type.
         *
         * @param attributes UAttributes object containing the message type to validate.
         * @return Returns a {@link ValidationResult} that is success or failed with a failure message.
         */
        @Override
        public ValidationResult validateType(UAttributes attributes) {
            return UMessageType.UMESSAGE_TYPE_RESPONSE == attributes.getType() ? ValidationResult.success() :
                    ValidationResult.failure(
                    String.format("Wrong Attribute Type [%s]", attributes.getType()));
        }

        /**
         * Validates that attributes for a message meant for an RPC response has a destination sink.
         * In the case of an RPC response, the sink is required.
         *
         * @param attributes UAttributes object containing the sink to validate.
         * @return Returns a {@link ValidationResult} that is success or failed with a failure message.
         */
        @Override
        public ValidationResult validateSink(UAttributes attributes) {
            Objects.requireNonNull(attributes, "UAttributes cannot be null.");
            if (!attributes.hasSink()|| attributes.getSink() == UUri.getDefaultInstance()) {
                return ValidationResult.failure("Missing Sink");
            }
            ValidationResult result = UriValidator.validateRpcResponse(attributes.getSink());
            return result;

        }


        /**
         * Validate the correlationId. n the case of an RPC response, the correlation id is required.
         *
         * @param attributes UAttributes object containing the correlation id to validate.
         * @return Returns a {@link ValidationResult} that is success or failed with a failure message.
         */
        @Override
        public ValidationResult validateReqId(UAttributes attributes) {
            if (!attributes.hasReqid()||attributes.getReqid()== UUID.getDefaultInstance()) {
                return ValidationResult.failure("Missing correlationId");
            }
            if (!UuidUtils.isUuid(attributes.getReqid())) {
                return ValidationResult.failure(String.format("Invalid correlationId [%s]", attributes.getReqid()));
            } else {
                return ValidationResult.success();
            }

        }

        @Override
        public String toString() {
            return "UAttributesValidator.Response";
        }
    }

}
