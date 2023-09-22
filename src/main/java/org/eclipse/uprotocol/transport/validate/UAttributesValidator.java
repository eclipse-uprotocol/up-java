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
package org.eclipse.uprotocol.transport.validate;

import org.eclipse.uprotocol.transport.datamodel.UAttributes;
import org.eclipse.uprotocol.transport.datamodel.UMessageType;
import org.eclipse.uprotocol.transport.datamodel.UPriority;
import org.eclipse.uprotocol.transport.datamodel.UStatus;
import org.eclipse.uprotocol.transport.datamodel.UStatus.Code;
import org.eclipse.uprotocol.uri.validator.UriValidator;
import org.eclipse.uprotocol.uuid.factory.UUIDUtils;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * {@link UAttributes} is the class that defines the Payload. It is the place for configuring time to live, priority, security tokens and more.
 * Each UAttributes class defines a different type of message payload. The payload can represent a simple published payload with some state change,
 * Payload representing an RPC request or Payload representing an RPC response.
 * UAttributesValidator is a base class for all UAttribute validators, that can help validate that the {@link UAttributes} object is correctly defined
 * to define the Payload correctly.
 */
public abstract class UAttributesValidator {

    /**
     * Static factory method for getting a validator according to the {@link UMessageType} defined in the {@link UAttributes}.
     * @param attribute UAttributes containing the UMessageType.
     * @return returns a UAttributesValidator according to the {@link UMessageType} defined in the {@link UAttributes}.
     */
    public static UAttributesValidator getValidator(UAttributes attribute) {
        if (attribute.type() == null) {
            return Validators.PUBLISH.validator();
        }
        switch (attribute.type()){
            case RESPONSE:
                return Validators.RESPONSE.validator();
            case REQUEST:
                return Validators.REQUEST.validator();
            default:
                return Validators.PUBLISH.validator();
        }
    }


    /**
     * Validators Factory. Example:
     * UAttributesValidator validateForPublishMessageType = UAttributesValidator.Validators.PUBLISH.validator()
     */
    public enum Validators {
        PUBLISH (new Publish()),
        REQUEST (new Request()),
        RESPONSE (new Response());

        private final UAttributesValidator uattributesValidator;

        public UAttributesValidator validator() {
            return uattributesValidator;
        }

        Validators(UAttributesValidator uattributesValidator) {
            this.uattributesValidator = uattributesValidator;
        }
    }


    /**
     * Take a {@link UAttributes} object and run validations.
     * @param attributes The UAttriubes to validate.
     * @return Returns a {@link UStatus} that is success or failed with a message containing all validation errors for
     *      invalid configurations.
     */
    public UStatus validate(UAttributes attributes) {
        final String errorMessage = Stream.of(
                    validateId(attributes),
                    validateType(attributes),
                    validatePriority(attributes),
                    validateTtl(attributes),
                    validateSink(attributes),
                    validateCommStatus(attributes),
                    validatePermissionLevel(attributes),
                    validateReqId(attributes))
                .filter(UStatus::isFailed)
                .map(UStatus::msg)
                .collect(Collectors.joining(","));
        return errorMessage.isBlank() ? UStatus.ok() :
                UStatus.failed(errorMessage, Code.INVALID_ARGUMENT);
    }

    /**
     * Indication if the Payload with these UAttributes is expired.
     * @param uAttributes UAttributes with time to live value.
     * @return Returns a {@link UStatus} that is success meaning not expired or failed with a validation message or expiration.
     */
    public UStatus isExpired(UAttributes uAttributes) {
        try {
            final Optional<Integer> maybeTtl = uAttributes.ttl();
            if (maybeTtl.isEmpty()) {
                return UStatus.ok("Not Expired");
            }
            int ttl = maybeTtl.get();
            if (ttl <= 0) {
                return UStatus.ok("Not Expired");
            }

            long delta = System.currentTimeMillis() - UUIDUtils.getTime(uAttributes.id());

            return delta >= ttl ? UStatus.failed("Payload is expired", Code.DEADLINE_EXCEEDED) : UStatus.ok("Not Expired");
        } catch (Exception e) {
            return UStatus.ok("Not Expired");
        }
    }

    /**
     * Validate the id attribute, it is required.
     * @param attributes UAttributes object containing the id to validate.
     * @return Returns a {@link UStatus} that is success or failed with a failure message.
     */
    public UStatus validateId(UAttributes attributes) {
        final UUID id = attributes.id();
        try {
            return UUIDUtils.isUuid(id) ? UStatus.ok() :
                    UStatus.failed(String.format("Invalid UUID [%s]", id), Code.INVALID_ARGUMENT.value());
        } catch (Exception e) {
            return UStatus.failed(String.format("Invalid UUID [%s] [%s]", id, e.getMessage()), Code.INVALID_ARGUMENT.value());
        }
    }

    /**
     * Validate the {@link UPriority} since it is required.
     * @param attributes UAttributes object containing the message priority to validate.
     * @return Returns a {@link UStatus} that is success or failed with a failure message.
     */
    public UStatus validatePriority(UAttributes attributes) {
        return attributes.priority() == null ?
                UStatus.failed("Priority is missing", Code.INVALID_ARGUMENT.value()) : UStatus.ok();
    }

    /**
     * Validate the time to live configuration. If the UAttributes does not contain a time to live then the UStatus is ok.
     * @param attributes UAttributes object containing the message time to live configuration to validate.
     * @return Returns a {@link UStatus} that is success or failed with a failure message.
     */
    public UStatus validateTtl(UAttributes attributes) {
        return attributes.ttl()
                .filter(ttl -> ttl <= 0)
                .map(ttl -> UStatus.failed(String.format("Invalid TTL [%s]", ttl), Code.INVALID_ARGUMENT.value()))
                .orElse(UStatus.ok());
    }

    /**
     * Validate the sink UriPart for the default case. If the UAttributes does not contain a sink then the UStatus is ok.
     * @param attributes UAttributes object containing the sink to validate.
     * @return Returns a {@link UStatus} that is success or failed with a failure message.
     */
    public UStatus validateSink(UAttributes attributes) {
        return attributes.sink()
                .map(UriValidator::validate)
                .orElse(UStatus.ok());
    }

    /**
     * Validate the permissionLevel for the default case. If the UAttributes does not contain a permission level then the UStatus is ok.
     * @param attributes UAttributes object containing the permission level to validate.
     * @return Returns a UStatus indicating if the permissionLevel is valid or not.
     */
    public UStatus validatePermissionLevel(UAttributes attributes) {
        return attributes.plevel()
                .map(permissionLevel -> permissionLevel > 0 ? UStatus.ok() :  UStatus.failed("Invalid Permission Level", Code.INVALID_ARGUMENT.value()))
                .orElse(UStatus.ok());
    }

    /**
     * Validate the commStatus for the default case. If the UAttributes does not contain a comm status then the UStatus is ok.
     * @param attributes UAttributes object containing the comm status to validate.
     * @return Returns a {@link UStatus} that is success or failed with a failure message.
     */
    public UStatus validateCommStatus(UAttributes attributes) {
        return attributes.commstatus().or(() -> Optional.of(Code.OK.value()))
                .flatMap(Code::from)
                .map(code -> UStatus.ok())
                .orElse(UStatus.failed("Invalid Communication Status Code", Code.INVALID_ARGUMENT.value()));
    }

    /**
     * Validate the correlationId for the default case. If the UAttributes does not contain a request id then the UStatus is ok.
     * @param attributes Attributes object containing the request id to validate.
     * @return Returns a {@link UStatus} that is success or failed with a failure message.
     */
    public UStatus validateReqId(UAttributes attributes) {
        return attributes.reqid()
                .filter(correlationId -> !UUIDUtils.isUuid(correlationId))
                .map(correlationId -> UStatus.failed("Invalid UUID", Code.INVALID_ARGUMENT.value()))
                .orElse(UStatus.ok());
    }

    /**
     * Validate the {@link UMessageType} attribute, it is required.
     * @param attributes UAttributes object containing the message type to validate.
     * @return Returns a {@link UStatus} that is success or failed with a failure message.
     */
    public abstract UStatus validateType(UAttributes attributes);

    /**
     * Implements validations for UAttributes that define a message that is meant for publishing state changes.
     */
    private static class Publish extends UAttributesValidator {

        /**
         * Validates that attributes for a message meant to publish state changes has the correct type.
         * @param attributes UAttributes object containing the message type to validate.
         * @return Returns a {@link UStatus} that is success or failed with a failure message.
         */
        @Override
        public UStatus validateType(UAttributes attributes) {
            return UMessageType.PUBLISH == attributes.type() ? UStatus.ok() :
                    UStatus.failed(String.format("Wrong Attribute Type [%s]", attributes.type()), Code.INVALID_ARGUMENT.value());
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
         * @param attributes UAttributes object containing the message type to validate.
         * @return Returns a {@link UStatus} that is success or failed with a failure message.
         */
        @Override
        public UStatus validateType(UAttributes attributes) {
            return UMessageType.REQUEST == attributes.type() ? UStatus.ok() :
                    UStatus.failed(String.format("Wrong Attribute Type [%s]", attributes.type()), Code.INVALID_ARGUMENT.value());
        }

        /**
         * Validates that attributes for a message meant for an RPC request has a destination sink.
         * In the case of an RPC request, the sink is required.
         * @param attributes UAttributes object containing the sink to validate.
        * @return Returns a {@link UStatus} that is success or failed with a failure message.
        */
        @Override
        public UStatus validateSink(UAttributes attributes) {
            return attributes.sink()
                    .map(UriValidator::validateRpcResponse)
                    .orElse(UStatus.failed("Missing Sink", Code.INVALID_ARGUMENT.value()));
        }

        /**
         * Validate the time to live configuration.
         * In the case of an RPC request, the time to live is required.
         * @param attributes UAttributes object containing the time to live to validate.
         * @return Returns a {@link UStatus} that is success or failed with a failure message.
         */
        @Override
        public UStatus validateTtl(UAttributes attributes) {
            return attributes.ttl()
                    .map(ttl -> ttl > 0 ? UStatus.ok() : UStatus.failed(String.format("Invalid TTL [%s]", ttl), Code.INVALID_ARGUMENT.value()))
                    .orElse(UStatus.failed("Missing TTL", Code.INVALID_ARGUMENT.value()));
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
         * @param attributes UAttributes object containing the message type to validate.
         * @return Returns a {@link UStatus} that is success or failed with a failure message.
         */
        @Override
        public UStatus validateType(UAttributes attributes) {
            return UMessageType.RESPONSE == attributes.type() ? UStatus.ok() :
                    UStatus.failed(String.format("Wrong Attribute Type [%s]", attributes.type()), Code.INVALID_ARGUMENT.value());
        }

        /**
         * Validates that attributes for a message meant for an RPC response has a destination sink.
         * In the case of an RPC response, the sink is required.
         * @param attributes UAttributes object containing the sink to validate.
         * @return Returns a {@link UStatus} that is success or failed with a failure message.
         */
        @Override
        public UStatus validateSink(UAttributes attributes) {
            return attributes.sink()
                    .map(UriValidator::validateRpcMethod)
                    .orElse(UStatus.failed("Missing Sink", Code.INVALID_ARGUMENT.value()));
        }


        /**
        * Validate the correlationId. n the case of an RPC response, the correlation id is required.
        * @param attributes UAttributes object containing the correlation id to validate.
        * @return Returns a {@link UStatus} that is success or failed with a failure message.
        */
        @Override
        public UStatus validateReqId(UAttributes attributes) {
            return attributes.reqid()
                    .map(correlationId -> UUIDUtils.isUuid(correlationId) ?
                            UStatus.ok() : UStatus.failed(String.format("Invalid correlationId [%s]", correlationId), Code.INVALID_ARGUMENT.value()))
                    .orElse(UStatus.failed("Missing correlationId", Code.INVALID_ARGUMENT.value()));
        }

        @Override
        public String toString() {
            return "UAttributesValidator.Response";
        }
    }

}
