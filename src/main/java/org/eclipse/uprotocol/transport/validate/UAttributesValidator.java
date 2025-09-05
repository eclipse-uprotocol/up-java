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
import org.eclipse.uprotocol.validation.ValidationException;
import org.eclipse.uprotocol.validation.ValidationUtils;

import java.util.Objects;
import java.util.Optional;

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
     * Checks if a given set of attributes complies with the rules specified for
     * the type of message they describe.
     *
     * @param attributes The attributes to validate.
     * @throws ValidationException if the attributes are not consistent with the rules specified for the message type.
     */
    public void validate(UAttributes attributes) {
        final var errors = ValidationUtils.collectErrors(attributes, 
                this::validateType,
                this::validateTtl,
                this::validateSink,
                this::validatePriority,
                this::validatePermissionLevel,
                this::validateReqId,
                this::validateId
        );
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
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
     * @throws ValidationException if TTL &lt;= 0.
     */
    public void validateTtl(UAttributes attributes) {
        int ttl = attributes.getTtl();
        if (attributes.hasTtl() && ttl <= 0) {
            throw new ValidationException(String.format("Invalid TTL [%s]", ttl));
        }

    }

    /**
     * Validate the sink UriPart.
     *
     * @param attributes UAttributes object containing the sink to validate.
     * @throws ValidationException if sink is invalid.
     */
    public abstract void validateSink(UAttributes attributes);

    /**
     * Validate the permissionLevel for the default case. If the UAttributes does
     * not contain a permission level then
     * the ValidationResult is ok.
     *
     * @param attributes UAttributes object containing the permission level to
     *                   validate.
     * @throws ValidationException if the permission level is invalid.
     */
    public void validatePermissionLevel(UAttributes attributes) {
        if (attributes.hasPermissionLevel() && attributes.getPermissionLevel() <= 0) {
            throw new ValidationException("Invalid Permission Level");
        }
    }

    /**
     * Validate the correlationId for the default case. Only the response message
     * should have a reqid.
     *
     * @param attributes Attributes object containing the request id to validate.
     * @throws ValidationException if the request id is invalid.
     */
    public void validateReqId(UAttributes attributes) {
        if (attributes.hasReqid()) {
            throw new ValidationException("Message should not have a reqid");
        }
    }

    /**
     * Validate the priority value to ensure it is one of the known CS values.
     * 
     * @param attributes Attributes object containing the Priority to validate.
     * @throws ValidationException if the priority is invalid.
     */
    public void validatePriority(UAttributes attributes) {
        if (attributes.getPriority().getNumber() < UPriority.UPRIORITY_CS1_VALUE) {
            throw new ValidationException(String.format("Invalid UPriority [%s]", attributes.getPriority().name()));
        }
    }

    /**
     * Validate the Id for the default case. If the UAttributes object does not
     * contain an Id,
     * the ValidationResult is failed.
     *
     * @param attributes Attributes object containing the id to validate.
     * @throws ValidationException if the message ID is invalid.
     */
    public void validateId(UAttributes attributes) {
        if (!attributes.hasId()) {
            throw new ValidationException("Missing id");
        }
        if (!UuidUtils.isUuid(attributes.getId())) {
            throw new ValidationException("Attributes must contain valid uProtocol UUID in id property");
        }
    }

    /**
     * Validate the {@link UMessageType} attribute, it is required.
     *
     * @param attributes UAttributes object containing the message type to validate.
     * @throws ValidationException if this validator is inappropriate for the message's type.
     */
    public abstract void validateType(UAttributes attributes);

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
         * @return Returns a {@link ValidationException} that is success or failed with a
         *         failure message.
         */
        @Override
        public void validateType(UAttributes attributes) {
            if (UMessageType.UMESSAGE_TYPE_PUBLISH != attributes.getType()) {
                throw new ValidationException(
                        String.format("Wrong Attribute Type [%s]", attributes.getType()));
            }
        }

        /**
         * Validate the sink UriPart for Publish events. Publish should not have a sink.
         *
         * @param attributes UAttributes object containing the sink to validate.
         * @return Returns a {@link ValidationException} that is success or failed with a
         *         failure message.
         */
        @Override
        public void validateSink(UAttributes attributes) {
            if (attributes.hasSink()) {
                throw new ValidationException("Sink should not be present");
            }
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
         * {@inheritDoc}
         *
         * Validates that attributes for a message meant for an RPC request has the
         * correct type.
         */
        @Override
        public void validateType(UAttributes attributes) {
            if (UMessageType.UMESSAGE_TYPE_REQUEST != attributes.getType()) {
                throw new ValidationException(
                        String.format("Wrong Attribute Type [%s]", attributes.getType()));
            }
        }

        /**
         * {@inheritDoc}
         *
         * Validates that attributes for a message meant for an RPC request has a
         * destination sink.
         * In the case of an RPC request, the sink is required.
         */
        @Override
        public void validateSink(UAttributes attributes) {
            if (!attributes.hasSink()) {
                throw new ValidationException("Missing Sink");
            }
            if (!UriValidator.isRpcMethod(attributes.getSink())) {
                throw new ValidationException("Invalid Sink Uri");
            }
        }

        /**
         * {@inheritDoc}
         *
         * Validate the time to live configuration.
         * In the case of an RPC request, the time to live is required.
         */
        @Override
        public void validateTtl(UAttributes attributes) {
            if (!attributes.hasTtl()) {
                throw new ValidationException("Missing TTL");
            }
            int ttl = attributes.getTtl();
            if (ttl <= 0) {
                throw new ValidationException(String.format("Invalid TTL [%s]", ttl));
            }
        }

        /**
         * {@inheritDoc}
         *
         * Validate the priority value to ensure it is one of the known CS values
         */
        @Override
        public void validatePriority(UAttributes attributes) {
            if (attributes.getPriority().getNumber() < UPriority.UPRIORITY_CS4_VALUE) {
                throw new ValidationException(
                        String.format("Invalid UPriority [%s]", attributes.getPriority().name()));
            }
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
         * {@inheritDoc}
         *
         * Validates that attributes for a message meant for an RPC response has the
         * correct type.
         */
        @Override
        public void validateType(UAttributes attributes) {
            if (UMessageType.UMESSAGE_TYPE_RESPONSE != attributes.getType()) {
                throw new ValidationException(
                        String.format("Wrong Attribute Type [%s]", attributes.getType()));
            }
        }

        /**
         * {@inheritDoc}
         *
         * Validates that attributes for a message meant for an RPC response has a
         * destination sink.
         * In the case of an RPC response, the sink is required.
         */
        @Override
        public void validateSink(UAttributes attributes) {
            Objects.requireNonNull(attributes, "UAttributes cannot be null.");
            if (!attributes.hasSink() || attributes.getSink() == UUri.getDefaultInstance()) {
                throw new ValidationException("Missing Sink");
            }
            if (!UriValidator.isRpcResponse(attributes.getSink())) {
                throw new ValidationException("Invalid Sink Uri");
            }
        }

        /**
         * {@inheritDoc}
         *
         * Validate the correlationId. n the case of an RPC response, the correlation id
         * is required.
         */
        @Override
        public void validateReqId(UAttributes attributes) {
            if (!attributes.hasReqid() || attributes.getReqid() == UUID.getDefaultInstance()) {
                throw new ValidationException("Missing correlationId");
            }
            if (!UuidUtils.isUuid(attributes.getReqid())) {
                throw new ValidationException("Invalid correlation UUID");
            }
        }

        /**
         * {@inheritDoc}
         *
         * Validate the priority value to ensure it is one of the known CS values
         */
        @Override
        public void validatePriority(UAttributes attributes) {
            if (attributes.getPriority().getNumber() < UPriority.UPRIORITY_CS4_VALUE) {
                throw new ValidationException(
                        String.format("Invalid UPriority [%s]", attributes.getPriority().name()));
            }
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
         * {@inheritDoc}
         *
         * Validates that attributes for a message meant to Notification state changes
         * has the correct type.
         */
        @Override
        public void validateType(UAttributes attributes) {
            if (UMessageType.UMESSAGE_TYPE_NOTIFICATION != attributes.getType()) {
                throw new ValidationException(
                        String.format("Wrong Attribute Type [%s]", attributes.getType()));
            }
        }

        /**
         * {@inheritDoc}
         *
         * Validates that attributes for a message meant for notifications has a
         * destination sink.
         * In the case of a notification, the sink is required.
         */
        @Override
        public void validateSink(UAttributes attributes) {
            Objects.requireNonNull(attributes, "UAttributes cannot be null.");
            if (!attributes.hasSink() || attributes.getSink() == UUri.getDefaultInstance()) {
                throw new ValidationException("Missing Sink");
            }
            if (!UriValidator.isNotificationDestination(attributes.getSink())) {
                throw new ValidationException("Invalid Sink Uri");
            }
        }

        @Override
        public String toString() {
            return "UAttributesValidator.Notification";
        }
    }
}
