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
package org.eclipse.uprotocol.transport.validator;

import java.time.Instant;

import org.eclipse.uprotocol.uri.validator.UriValidator;
import org.eclipse.uprotocol.uuid.factory.UuidUtils;
import org.eclipse.uprotocol.v1.UAttributes;
import org.eclipse.uprotocol.v1.UCode;
import org.eclipse.uprotocol.v1.UMessageType;
import org.eclipse.uprotocol.v1.UPriority;
import org.eclipse.uprotocol.validation.ValidationException;
import org.eclipse.uprotocol.validation.ValidationUtils;

/**
 * A validator that checks if a given set of {@link UAttributes uProtocol
 * message attributes} are compliant with the uProtocol specification.
 * <p>
 * UAttributes contain a message's metadata like message ID, type,
 * priority, time-to-live, security tokens and more.
 * <p>
 * Each message contains some standard attributes like ID and type as well as
 * some attributes that are specific to the given type of message, such as
 * the client's permission level in an RPC request message or a status code in
 * an RPC response message.
 * <p>
 * {@code UAttributesValidator} is a base class that contains the functionality
 * shared by all type specific validators.
 */
public abstract class UAttributesValidator {

    /**
     * Gets a validator that can be used to check a given set of attributes..
     *
     * @param attributes The attributes to get a validator for.
     * @return A validator matching the type of message that the attributes
     *         belong to.
     */
    public static UAttributesValidator getValidator(UAttributes attributes) {
        return getValidator(attributes.getType());
    }

    /**
     * Gets a validator that can be used to check attributes of a given type of message.
     *
     * @param type The type of message to get a validator for.
     * @return A validator matching the type of message.
     */
    public static UAttributesValidator getValidator(UMessageType type) {
        switch (type) {
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
     * Verifies that a set of attributes contains a valid message ID.
     *
     * @param attributes The attributes to check.
     * @throws ValidationException if the message ID is invalid.
     */
    public final void validateId(UAttributes attributes) {
        // [impl->dsn~up-attributes-id~1]
        if (!attributes.hasId()) {
            throw new ValidationException("Missing message ID");
        }
        if (!UuidUtils.isUProtocol(attributes.getId())) {
            throw new ValidationException("Attributes must contain valid uProtocol UUID in id property");
        }
    }

    /**
     * Verifies that this validator is appropriate for a set of attributes.
     *
     * @param attributes The attributes to check.
     * @throws ValidationException if the attributes are not of the type returned by
     * {@link #messageType()}.
     */
    public final void validateType(UAttributes attributes) {
        if (attributes.getType() != messageType()) {
            throw new ValidationException(String.format(
                "Invalid message type [%s], expected [%s]",
                attributes.getType().name(), messageType().name()));
        }
    }

    /**
     * Checks if a given set of attributes contains a valid priority.
     *
     * @param attributes The attributes to check.
     * @throws ValidationException if the priority contained in the attributes is {@link UPriority#UNRECOGNIZED}.
     */
    public final void validatePriority(UAttributes attributes) {
        if (attributes.getPriority() == UPriority.UNRECOGNIZED) {
            throw new ValidationException(String.format(
                "Invalid UPriority [%s]", attributes.getPriority().name()));
        }
    }

    /**
     * Verifies that a set of attributes contains a priority that is appropriate for an RPC request message.
     * 
     * @param attributes The attributes to check.
     * @throws ValidationException if the priority contained in the attributes is not at
     * least {@link UPriority#UPRIORITY_CS4}.
     */
    public final void validateRpcPriority(UAttributes attributes) {
        if (attributes.getPriority().getNumber() < UPriority.UPRIORITY_CS4_VALUE) {
            throw new ValidationException(String.format(
                "RPC Request message must have at least priority [%s] but has [%s]",
                    UPriority.UPRIORITY_CS4.name(), attributes.getPriority().name()));
        }
    }

    /**
     * Validates the time-to-live configuration of RPC messages.
     *
     * @param attributes The attributes to check.
     * @throws IllegalArgumentException if the attributes do not represent an RPC message.
     * @throws ValidationException if the attributes do not contain a TTL or if its value is 0.
     */
    public final void validateRpcTtl(UAttributes attributes) {
        if (attributes.getType() != UMessageType.UMESSAGE_TYPE_REQUEST
            && attributes.getType() != UMessageType.UMESSAGE_TYPE_RESPONSE) {
            throw new IllegalArgumentException("Attributes do not represent an RPC message");
        }
        if (!attributes.hasTtl()) {
            throw new ValidationException("RPC messages must contain a TTL");
        }
        int ttl = attributes.getTtl();
        // TTL is interpreted as an unsigned integer, so negative values are not possible
        if (ttl == 0) {
            throw new ValidationException("RPC message's TTL must not be 0");
        }
    }

    /**
     * Checks if a given set of attributes belong to a message that has expired.
     * <p>
     * The message is considered expired if the message's creation time plus the
     * duration indicated by the <em>ttl</em> attribute is <em>before</em> the current
     * instant in (system) time.
     *
     * @param attributes The attributes to check.
     * @return {@code true} if the given attributes should be considered expired.
     */
    public final boolean isExpired(UAttributes attributes) {
        final int ttl = attributes.getTtl();
        // TTL is interpreted as an unsigned integer, so negative values are not possible
        return Integer.compareUnsigned(ttl, 0) > 0 && UuidUtils.isExpired(attributes.getId(), ttl, Instant.now());
    }

    /*
     * Gets the type of message that this validator can be used with.
     *
     * @return The message type.
     */
    abstract UMessageType messageType();

    /**
     * Verifies that a set of attributes contains a valid source URI.
     *
     * @param attributes The attributes to check.
     * @throws ValidationException if the attributes do not contain a valid source URI.
     */
    public abstract void validateSource(UAttributes attributes);

    /**
     * Verifies that a set of attributes contains a valid sink URI.
     *
     * @param attributes The attributes to check.
     * @throws ValidationException if the attributes do not contain a valid sink URI.
     */
    public abstract void validateSink(UAttributes attributes);


    /**
     * Checks if a given set of attributes complies with the rules specified for
     * the type of message they describe.
     *
     * @param attributes The attributes to check.
     * @throws ValidationException if the attributes are not consistent with the rules
     * specified for the message type.
     */
    public abstract void validate(UAttributes attributes);

    /**
     * Validators for the message types defined by uProtocol.
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

        /**
         * Gets the validator to use for checking attributes.
         *
         * @return The validator.
         */
        public UAttributesValidator validator() {
            return uattributesValidator;
        }
    }

    /**
     * Validates attributes describing a Publish message.
     */
    private static final class Publish extends UAttributesValidator {

        @Override
        protected UMessageType messageType() {
            return UMessageType.UMESSAGE_TYPE_PUBLISH;
        }

        /**
         * Verifies that attributes for a publish message contain a valid source URI.
         *
         * @param attributes The attributes to check.
         * @throws ValidationException if the attributes do not contain a source URI,
         * or if the source URI contains any wildcards, or if the source URI has an
         * invalid resource ID.
         */
        @Override
        public void validateSource(UAttributes attributes) {
            // [impl->dsn~up-attributes-publish-source~1]
            if (!attributes.hasSource()) {
                throw new ValidationException("Attributes for a publish message must contain a source URI");
            }
            final var source = attributes.getSource();
            if (UriValidator.hasWildcard(source)) {
                throw new ValidationException("Source URI must not contain wildcards");
            }
            if (!UriValidator.isTopic(source)) {
                throw new ValidationException("Source is not a valid topic URI");
            }
        }

        /**
         * Verifies that attributes for a publish message do not contain a sink URI.
         *
         * @param attributes The attributes to check.
         * @throws ValidationException if the sink attribute contains any URI.
         */
        @Override
        public void validateSink(UAttributes attributes) {
            // [impl->dsn~up-attributes-publish-sink~1]
            if (attributes.hasSink()) {
                throw new ValidationException("Attributes for a publish message must not contain a sink URI");
            }
        }

        @Override
        public void validate(UAttributes attributes) {
            final var errors = ValidationUtils.collectErrors(attributes, 
                    this::validateType,
                    this::validateId,
                    this::validateSource,
                    this::validateSink,
                    this::validatePriority
            );
            if (!errors.isEmpty()) {
                throw new ValidationException(errors);
            }
        }
    }

    /**
     * Validates attributes describing a Notification message.
     */
    private static final class Notification extends UAttributesValidator {

        @Override
        protected UMessageType messageType() {
            return UMessageType.UMESSAGE_TYPE_NOTIFICATION;
        }

        /**
         * Verifies that attributes for a notification message contain a valid source URI.
         *
         * @param attributes The attributes to check.
         * @throws ValidationException if the attributes do not contain a source URI,
         * or if the source URI contains any wildcards, or if the source URI has an invalid
         * resource ID.
         */
        @Override
        public void validateSource(UAttributes attributes) {
            // [impl->dsn~up-attributes-notification-source~1]
            if (!attributes.hasSource()) {
                throw new ValidationException("Attributes for a notification message must contain a source URI");
            }
            final var source = attributes.getSource();
            if (UriValidator.hasWildcard(source)) {
                throw new ValidationException("Source URI must not contain wildcards");
            }
            if (!UriValidator.isTopic(source)) {
                throw new ValidationException("Source is not a valid topic URI");
            }
        }

        /**
         * Verifies that attributes for a notification message contain a sink URI.
         *
         * @param attributes The attributes to check.
         * @throws ValidationException if the attributes do not contain a sink URI,
         * or if the sink URI contains any wildcards, or if the sink URI has a resource
         * ID != 0.
         */
        @Override
        public void validateSink(UAttributes attributes) {
            // [impl->dsn~up-attributes-notification-sink~1]
            if (!attributes.hasSink()) {
                throw new ValidationException("Attributes for a notification message must contain a sink URI");
            }
            final var sink = attributes.getSink();
            if (UriValidator.hasWildcard(sink)) {
                throw new ValidationException("Sink URI must not contain wildcards");
            }
            if (!UriValidator.isNotificationDestination(sink)) {
                throw new ValidationException("Sink's resource ID must be 0");
            }
        }

        @Override
        public void validate(UAttributes attributes) {
            final var errors = ValidationUtils.collectErrors(attributes, 
                    this::validateType,
                    this::validateId,
                    this::validateSource,
                    this::validateSink,
                    this::validatePriority
            );
            if (!errors.isEmpty()) {
                throw new ValidationException(errors);
            }
        }
    }

    /**
     Validates attributes describing an RPC Request message.
     */
    private static final class Request extends UAttributesValidator {

        @Override
        protected UMessageType messageType() {
            return UMessageType.UMESSAGE_TYPE_REQUEST;
        }

        /**
         * Verifies that attributes for a message representing an RPC request contain a reply-to-address.
         *
         * @param attributes The attributes to check.
         * @throws ValidationException if the attributes do not contain a reply-to-address,
         * or if the reply-to-address contains any wildcards, or if the reply-to-address has a
         * resource ID != 0.
         */
        @Override
        public void validateSource(UAttributes attributes) {
            // [impl->dsn~up-attributes-request-source~1]
            if (!attributes.hasSource()) {
                throw new ValidationException("""
                    Attributes for a request message must contain a reply-to address in the source property
                    """);
            }
            final var source = attributes.getSource();
            if (UriValidator.hasWildcard(source)) {
                throw new ValidationException("Source URI must not contain wildcards");
            }
            if (!UriValidator.isRpcResponse(source)) {
                throw new ValidationException("Source is not a valid reply-to address");
            }
        }

        /**
         * Verifies that attributes for a message representing an RPC request indicate the method to invoke.
         *
         * @param attributes The attributes to check.
         * @throws ValidationException if the attributes do not contain a method-to-invoke,
         * or if the method-to-invoke contains any wildcards, or if the method-to-invoke
         * has an invalid resource ID.
         */
        @Override
        public void validateSink(UAttributes attributes) {
            // [impl->dsn~up-attributes-request-sink~1]
            if (!attributes.hasSink()) {
                throw new ValidationException("""
                    Attributes for a request message must contain a method-to-invoke in the sink property
                    """);
            }
            final var sink = attributes.getSink();
            if (UriValidator.hasWildcard(sink)) {
                throw new ValidationException("Sink URI must not contain wildcards");
            }
            if (!UriValidator.isRpcMethod(sink)) {
                throw new ValidationException("Sink is not a valid method-to-invoke");
            }
        }

        @Override
        public void validate(UAttributes attributes) {
            final var errors = ValidationUtils.collectErrors(attributes, 
                    this::validateType,
                    this::validateId,
                    this::validateSource,
                    this::validateSink,
                    this::validateRpcTtl,
                    this::validateRpcPriority
            );
            if (!errors.isEmpty()) {
                throw new ValidationException(errors);
            }
        }
    }

    /**
     * Implements validations for UAttributes that define a message that is meant
     * for an RPC response.
     */
    private static final class Response extends UAttributesValidator {

        @Override
        protected UMessageType messageType() {
            return UMessageType.UMESSAGE_TYPE_RESPONSE;
        }

        /**
         * Verifies that attributes for a message representing an RPC response contain the invoked method.
         *
         * @param attributes The attributes to check.
         * @throws ValidationException if the attributes do not contain the invoked method,
         * or if the invoked method contains any wildcards, or if the invoked method has an invalid resource ID.
         */
        @Override
        public void validateSource(UAttributes attributes) {
            // [impl->dsn~up-attributes-response-source~1]
            if (!attributes.hasSource()) {
                throw new ValidationException("""
                    Attributes for a response message must contain the invoked method in the source property
                    """);
            }
            final var source = attributes.getSource();
            if (UriValidator.hasWildcard(source)) {
                throw new ValidationException("Source URI must not contain wildcards");
            }
            if (!UriValidator.isRpcMethod(source)) {
                throw new ValidationException("Source is not a valid method-to-invoke");
            }
        }

        /**
         * Verifies that attributes for a message representing an RPC response contain a valid
         * reply-to-address.
         *
         * @param attributes The attributes to check.
         * @throws ValidationException if the attributes do not contain a reply-to-address,
         * or if the reply-to-address contains any wildcards, or if the reply-to-address
         * has a resource ID != 0.
         */
        @Override
        public void validateSink(UAttributes attributes) {
            // [impl->dsn~up-attributes-response-sink~1]
            if (!attributes.hasSink()) {
                throw new ValidationException("""
                    Attributes for a response message must contain a reply-to address in the sink property
                    """);
            }
            final var sink = attributes.getSink();
            if (UriValidator.hasWildcard(sink)) {
                throw new ValidationException("Sink URI must not contain wildcards");
            }
            if (!UriValidator.isRpcResponse(sink)) {
                throw new ValidationException("Sink is not a valid reply-to address");
            }
        }

        /**
         * Verifies that the attributes contain a valid request ID.
         *
         * @param attributes The attributes to check.
         * @throws ValidationException if the attributes do not contain a request ID,
         * or if the request ID is not a valid uProtocol UUID.
         */
        public void validateReqId(UAttributes attributes) {
            if (!attributes.hasReqid()) {
                throw new ValidationException("RPC response message must contain a request ID");
            }
            final var reqid = attributes.getReqid();
            if (!UuidUtils.isUProtocol(reqid)) {
                throw new ValidationException("Request ID is not a valid uProtocol UUID");
            }
        }

        /**
         * Verifies that a set of attributes contains a valid communication status.
         *
         * @param attributes The attributes to check.
         * @throws ValidationException if the attributes contain an unsupported status code.
         */
        public void validateCommstatus(UAttributes attributes) {
            if (!attributes.hasCommstatus()) {
                return;
            }
            final var commstatus = attributes.getCommstatus();
            if (commstatus == UCode.UNRECOGNIZED) {
                throw new ValidationException("Unsupported communication status");
            }
        }

        @Override
        public void validate(UAttributes attributes) {
            final var errors = ValidationUtils.collectErrors(attributes, 
                    this::validateType,
                    this::validateId,
                    this::validateSource,
                    this::validateSink,
                    this::validateReqId,
                    this::validateRpcTtl,
                    this::validateRpcPriority,
                    this::validateCommstatus
            );
            if (!errors.isEmpty()) {
                throw new ValidationException(errors);
            }
        }
    }
}
