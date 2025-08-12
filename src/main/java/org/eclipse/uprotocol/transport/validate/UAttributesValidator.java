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
package org.eclipse.uprotocol.transport.validate;

import org.eclipse.uprotocol.uri.validator.UriValidator;
import org.eclipse.uprotocol.uuid.factory.UuidUtils;
import org.eclipse.uprotocol.v1.UAttributes;
import org.eclipse.uprotocol.v1.UMessageType;
import org.eclipse.uprotocol.v1.UPriority;
import org.eclipse.uprotocol.v1.UUri;
import org.eclipse.uprotocol.v1.UUID;
import org.eclipse.uprotocol.validation.ValidationResult;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * {@link UAttributes} is the class that defines the Payload. It is the place
 * for configuring time to live, priority,
 * security tokens and more.
 * Each UAttributes class defines a different type of message payload. The
 * payload can represent a simple published
 * payload with some state change,
 * Payload representing an RPC request or Payload representing an RPC response.
 * UAttributesValidator is a base class for all UAttribute validators, that can
 * help validate that the
 * {@link UAttributes} object is correctly defined
 * to define the Payload correctly.
 */
public abstract class UAttributesValidator {

    /**
     * Static factory method for getting a validator according to the
     * {@link UMessageType} defined in the
     * {@link UAttributes}.
     *
     * @param attribute UAttributes containing the UMessageType.
     * @return returns a UAttributesValidator according to the {@link UMessageType}
     *         defined in the {@link UAttributes}.
     */
    public static UAttributesValidator getValidator(UAttributes attribute) {

        switch (attribute.getType()) {
            case UMESSAGE_TYPE_RESPONSE:
                return Validators.RESPONSE.validator();
            case UMESSAGE_TYPE_REQUEST:
                return Validators.REQUEST.validator();
            case UMESSAGE_TYPE_NOTIFICATION:
                return Validators.NOTIFICATION.validator();
            default:
                return Validators.PUBLISH.validator();
        }
    }

    /**
     * Take a {@link UAttributes} object and run validations.
     *
     * @param attributes The UAttriubes to validate.
     * @return Returns a {@link ValidationResult} that is success or failed with a
     *         message containing all validation
     *         errors for
     *         invalid configurations.
     */
    public ValidationResult validate(UAttributes attributes) {
        final String errorMessage = Stream.of(validateType(attributes),
                validateTtl(attributes), validateSink(attributes), validatePriority(attributes),
                validatePermissionLevel(attributes), validateReqId(attributes), validateId(attributes))
                .filter(ValidationResult::isFailure).map(ValidationResult::getMessage).collect(Collectors.joining(","));
        return errorMessage.isBlank() ? ValidationResult.success() : ValidationResult.failure(errorMessage);
    }

    /**
     * Check the time-to-live attribute to see if it has expired. <br>
     * The message has expired when the current time is greater than the original
     * UUID time
     * plus the ttl attribute.
     *
     * @param uAttributes UAttributes with time to live value.
     * @return Returns a true if the original time plus the ttl is less than the
     *         current time
     */
    public boolean isExpired(UAttributes uAttributes) {
        final int ttl = uAttributes.getTtl();
        final Optional<Long> maybeTime = UuidUtils.getTime(uAttributes.getId());

        // if the message does not have a ttl or the original time is not present or the
        // ttl is less than 0
        if (maybeTime.isEmpty() || ttl <= 0) {
            return false;
        }

        // the original time plus the ttl is less than the current time, the message has
        // expired
        return (maybeTime.get() + ttl) < System.currentTimeMillis();
    }

    /**
     * Validate the time to live configuration. If the UAttributes does not contain
     * a time to live then the
     * ValidationResult is ok.
     *
     * @param attributes UAttributes object containing the message time to live
     *                   configuration to validate.
     * @return Returns a {@link ValidationResult} that is success or failed with a
     *         failure message.
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
     * Validate the sink UriPart.
     *
     * @param attributes UAttributes object containing the sink to validate.
     * @return Returns a {@link ValidationResult} that is success or failed with a
     *         failure message.
     */
    public abstract ValidationResult validateSink(UAttributes attributes);

    /**
     * Validate the permissionLevel for the default case. If the UAttributes does
     * not contain a permission level then
     * the ValidationResult is ok.
     *
     * @param attributes UAttributes object containing the permission level to
     *                   validate.
     * @return Returns a ValidationResult indicating if the permissionLevel is valid
     *         or not.
     */
    public ValidationResult validatePermissionLevel(UAttributes attributes) {
        if (!attributes.hasPermissionLevel() || attributes.getPermissionLevel() > 0) {
            return ValidationResult.success();
        } else {
            return ValidationResult.failure("Invalid Permission Level");
        }
    }

    /**
     * Validate the correlationId for the default case. Only the response message
     * should have a reqid.
     *
     * @param attributes Attributes object containing the request id to validate.
     * @return Returns a {@link ValidationResult} that is success or failed with a
     *         failure message.
     */
    public ValidationResult validateReqId(UAttributes attributes) {
        return attributes.hasReqid() ? ValidationResult.failure("Message should not have a reqid")
                : ValidationResult.success();
    }

    /**
     * Validate the priority value to ensure it is one of the known CS values.
     * 
     * @param attributes Attributes object containing the Priority to validate.
     * @return Returns a {@link ValidationResult} that is success or failed with a
     *         failure message.
     */
    public ValidationResult validatePriority(UAttributes attributes) {
        return attributes.getPriority().getNumber() >= UPriority.UPRIORITY_CS1_VALUE ? ValidationResult.success()
                : ValidationResult.failure(
                        String.format("Invalid UPriority [%s]", attributes.getPriority().name()));
    }

    /**
     * Validate the Id for the default case. If the UAttributes object does not
     * contain an Id,
     * the ValidationResult is failed.
     *
     * @param attributes Attributes object containing the id to validate.
     * @return Returns a {@link ValidationResult} that is success or failed with a
     *         failure message.
     */
    public ValidationResult validateId(UAttributes attributes) {
        if (!attributes.hasId()) {
            return ValidationResult.failure("Missing id");
        }
        if (!UuidUtils.isUuid(attributes.getId())) {
            return ValidationResult.failure("Attributes must contain valid uProtocol UUID in id property");
        } else {
            return ValidationResult.success();
        }
    }

    /**
     * Validate the {@link UMessageType} attribute, it is required.
     *
     * @param attributes UAttributes object containing the message type to validate.
     * @return Returns a {@link ValidationResult} that is success or failed with a
     *         failure message.
     */
    public abstract ValidationResult validateType(UAttributes attributes);

    /**
     * Validators Factory. Example:
     * UAttributesValidator validateForPublishMessageType =
     * UAttributesValidator.Validators.PUBLISH.validator()
     */
    public enum Validators {
        PUBLISH(new Publish()),
        REQUEST(new Request()),
        RESPONSE(new Response()),
        NOTIFICATION(new Notification());

        private final UAttributesValidator uattributesValidator;

        Validators(UAttributesValidator uattributesValidator) {
            this.uattributesValidator = uattributesValidator;
        }

        public UAttributesValidator validator() {
            return uattributesValidator;
        }
    }

    /**
     * Implements validations for UAttributes that define a message that is meant
     * for publishing state changes.
     */
    private static class Publish extends UAttributesValidator {

        /**
         * Validates that attributes for a message meant to publish state changes has
         * the correct type.
         *
         * @param attributes UAttributes object containing the message type to validate.
         * @return Returns a {@link ValidationResult} that is success or failed with a
         *         failure message.
         */
        @Override
        public ValidationResult validateType(UAttributes attributes) {
            return UMessageType.UMESSAGE_TYPE_PUBLISH == attributes.getType() ? ValidationResult.success()
                    : ValidationResult.failure(
                            String.format("Wrong Attribute Type [%s]", attributes.getType()));
        }

        /**
         * Validate the sink UriPart for Publish events. Publish should not have a sink.
         *
         * @param attributes UAttributes object containing the sink to validate.
         * @return Returns a {@link ValidationResult} that is success or failed with a
         *         failure message.
         */
        @Override
        public ValidationResult validateSink(UAttributes attributes) {
            return attributes.hasSink() ? ValidationResult.failure("Sink should not be present")
                    : ValidationResult.success();
        }

        @Override
        public String toString() {
            return "UAttributesValidator.Publish";
        }
    }

    /**
     * Implements validations for UAttributes that define a message that is meant
     * for an RPC request.
     */
    private static class Request extends UAttributesValidator {

        /**
         * Validates that attributes for a message meant for an RPC request has the
         * correct type.
         *
         * @param attributes UAttributes object containing the message type to validate.
         * @return Returns a {@link ValidationResult} that is success or failed with a
         *         failure message.
         */
        @Override
        public ValidationResult validateType(UAttributes attributes) {
            return UMessageType.UMESSAGE_TYPE_REQUEST == attributes.getType() ? ValidationResult.success()
                    : ValidationResult.failure(
                            String.format("Wrong Attribute Type [%s]", attributes.getType()));
        }

        /**
         * Validates that attributes for a message meant for an RPC request has a
         * destination sink.
         * In the case of an RPC request, the sink is required.
         *
         * @param attributes UAttributes object containing the sink to validate.
         * @return Returns a {@link ValidationResult} that is success or failed with a
         *         failure message.
         */
        @Override
        public ValidationResult validateSink(UAttributes attributes) {
            if (!attributes.hasSink()) {
                return ValidationResult.failure("Missing Sink");
            }
            return UriValidator.isRpcMethod(attributes.getSink()) ? ValidationResult.success()
                    : ValidationResult.failure("Invalid Sink Uri");
        }

        /**
         * Validate the time to live configuration.
         * In the case of an RPC request, the time to live is required.
         *
         * @param attributes UAttributes object containing the time to live to validate.
         * @return Returns a {@link ValidationResult} that is success or failed with a
         *         failure message.
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

        /**
         * Validate the priority value to ensure it is one of the known CS values
         * 
         * @param attributes Attributes object containing the Priority to validate.
         * @return Returns a {@link ValidationResult} that is success or failed with a
         *         failure message.
         */
        @Override
        public ValidationResult validatePriority(UAttributes attributes) {
            return attributes.getPriority().getNumber() >= UPriority.UPRIORITY_CS4_VALUE ? ValidationResult.success()
                    : ValidationResult.failure(
                            String.format("Invalid UPriority [%s]", attributes.getPriority().name()));
        }

        @Override
        public String toString() {
            return "UAttributesValidator.Request";
        }
    }

    /**
     * Implements validations for UAttributes that define a message that is meant
     * for an RPC response.
     */
    private static class Response extends UAttributesValidator {

        /**
         * Validates that attributes for a message meant for an RPC response has the
         * correct type.
         *
         * @param attributes UAttributes object containing the message type to validate.
         * @return Returns a {@link ValidationResult} that is success or failed with a
         *         failure message.
         */
        @Override
        public ValidationResult validateType(UAttributes attributes) {
            return UMessageType.UMESSAGE_TYPE_RESPONSE == attributes.getType() ? ValidationResult.success()
                    : ValidationResult.failure(
                            String.format("Wrong Attribute Type [%s]", attributes.getType()));
        }

        /**
         * Validates that attributes for a message meant for an RPC response has a
         * destination sink.
         * In the case of an RPC response, the sink is required.
         *
         * @param attributes UAttributes object containing the sink to validate.
         * @return Returns a {@link ValidationResult} that is success or failed with a
         *         failure message.
         */
        @Override
        public ValidationResult validateSink(UAttributes attributes) {
            Objects.requireNonNull(attributes, "UAttributes cannot be null.");
            if (!attributes.hasSink() || attributes.getSink() == UUri.getDefaultInstance()) {
                return ValidationResult.failure("Missing Sink");
            }
            return UriValidator.isRpcResponse(attributes.getSink()) ? ValidationResult.success()
                    : ValidationResult.failure("Invalid Sink Uri");
        }

        /**
         * Validate the correlationId. n the case of an RPC response, the correlation id
         * is required.
         *
         * @param attributes UAttributes object containing the correlation id to
         *                   validate.
         * @return Returns a {@link ValidationResult} that is success or failed with a
         *         failure message.
         */
        @Override
        public ValidationResult validateReqId(UAttributes attributes) {
            if (!attributes.hasReqid() || attributes.getReqid() == UUID.getDefaultInstance()) {
                return ValidationResult.failure("Missing correlationId");
            }
            if (!UuidUtils.isUuid(attributes.getReqid())) {
                return ValidationResult.failure("Invalid correlation UUID");
            } else {
                return ValidationResult.success();
            }

        }

        /**
         * Validate the priority value to ensure it is one of the known CS values
         * 
         * @param attributes Attributes object containing the Priority to validate.
         * @return Returns a {@link ValidationResult} that is success or failed with a
         *         failure message.
         */
        @Override
        public ValidationResult validatePriority(UAttributes attributes) {
            return attributes.getPriority().getNumber() >= UPriority.UPRIORITY_CS4_VALUE ? ValidationResult.success()
                    : ValidationResult.failure(
                            String.format("Invalid UPriority [%s]", attributes.getPriority().name()));
        }

        @Override
        public String toString() {
            return "UAttributesValidator.Response";
        }
    }

    /**
     * Implements validations for UAttributes that define a message that is meant
     * for notifications.
     */
    private static class Notification extends UAttributesValidator {

        /**
         * Validates that attributes for a message meant to Notification state changes
         * has the correct type.
         *
         * @param attributes UAttributes object containing the message type to validate.
         * @return Returns a {@link ValidationResult} that is success or failed with a
         *         failure message.
         */
        @Override
        public ValidationResult validateType(UAttributes attributes) {
            return UMessageType.UMESSAGE_TYPE_NOTIFICATION == attributes.getType() ? ValidationResult.success()
                    : ValidationResult.failure(
                            String.format("Wrong Attribute Type [%s]", attributes.getType()));
        }

        /**
         * Validates that attributes for a message meant for notifications has a
         * destination sink.
         * In the case of a notification, the sink is required.
         *
         * @param attributes UAttributes object containing the sink to validate.
         * @return Returns a {@link ValidationResult} that is success or failed with a
         *         failure message.
         */
        @Override
        public ValidationResult validateSink(UAttributes attributes) {
            Objects.requireNonNull(attributes, "UAttributes cannot be null.");
            if (!attributes.hasSink() || attributes.getSink() == UUri.getDefaultInstance()) {
                return ValidationResult.failure("Missing Sink");
            }
            return UriValidator.isDefaultResourceId(attributes.getSink()) ? ValidationResult.success()
                    : ValidationResult.failure("Invalid Sink Uri");
        }

        @Override
        public String toString() {
            return "UAttributesValidator.Notification";
        }
    }

}
