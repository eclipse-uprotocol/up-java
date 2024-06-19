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
package org.eclipse.uprotocol.cloudevent.validate;

import io.cloudevents.CloudEvent;
import org.eclipse.uprotocol.cloudevent.factory.UCloudEvent;
import org.eclipse.uprotocol.validation.ValidationResult;

import org.eclipse.uprotocol.uri.serializer.UriSerializer;
import org.eclipse.uprotocol.uri.validator.UriValidator;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Validates a CloudEvent using UStatus<br>
 */
public abstract class CloudEventValidator {

    /**
     * Obtain a CloudEventValidator according to the type attribute in the
     * CloudEvent.
     * 
     * @param cloudEvent The CloudEvent with the type attribute.
     * @return Returns a CloudEventValidator according to the type attribute in the
     *         CloudEvent.
     */
    public static CloudEventValidator getValidator(CloudEvent cloudEvent) {
        final String cloudEventType = cloudEvent.getType();
        if (cloudEventType.isEmpty()) {
            return Validators.PUBLISH.validator();
        }

        CloudEventValidator validator;
        switch (UCloudEvent.getMessageType(cloudEvent.getType())) {
            case UMESSAGE_TYPE_NOTIFICATION:
                validator = Validators.NOTIFICATION.validator();
                break;
            case UMESSAGE_TYPE_RESPONSE:
                validator = Validators.RESPONSE.validator();
                break;
            case UMESSAGE_TYPE_REQUEST:
                validator = Validators.REQUEST.validator();
                break;
            default:
                validator = Validators.PUBLISH.validator();
                break;
        }
        return validator;
    }

    /**
     * Enum that hold the implementations of CloudEventValidator according to type.
     */
    public enum Validators {
        PUBLISH(new Publish()),
        NOTIFICATION(new Notification()),
        REQUEST(new Request()),
        RESPONSE(new Response());

        private final CloudEventValidator cloudEventValidator;

        public CloudEventValidator validator() {
            return cloudEventValidator;
        }

        Validators(CloudEventValidator cloudEventValidator) {
            this.cloudEventValidator = cloudEventValidator;
        }
    }

    /**
     * Validate the CloudEvent. A CloudEventValidator instance is obtained according
     * to the
     * type attribute on the CloudEvent.
     * 
     * @param cloudEvent The CloudEvent to validate.
     * @return Returns a google.rpc.Status with success or a google.rpc.Status with
     *         failure containing
     *         all the errors that were found.
     */
    public ValidationResult validate(CloudEvent cloudEvent) {
        final String errorMessage = Stream.of(validateVersion(cloudEvent), validateId(cloudEvent),
                validateSource(cloudEvent), validateType(cloudEvent), validateSink(cloudEvent))
                .filter(ValidationResult::isFailure)
                .map(ValidationResult::getMessage)
                .collect(Collectors.joining(","));
        return errorMessage.isBlank() ? ValidationResult.success() : ValidationResult.failure(errorMessage);
    }

    public static ValidationResult validateVersion(CloudEvent cloudEvent) {
        return validateVersion(cloudEvent.getSpecVersion().toString());

    }

    public static ValidationResult validateVersion(String version) {
        return version.equals("1.0") ? ValidationResult.success()
                : ValidationResult.failure(
                        String.format("Invalid CloudEvent version [%s]. CloudEvent version must be 1.0.", version));
    }

    public static ValidationResult validateId(CloudEvent cloudEvent) {
        return UCloudEvent.isCloudEventId(cloudEvent) ? ValidationResult.success()
                : ValidationResult.failure(
                        String.format("Invalid CloudEvent Id [%s]. CloudEvent Id must be of type UUIDv8.",
                                cloudEvent.getId()));
    }

    /**
     * Validate the source value of a cloud event.
     * 
     * @param cloudEvent The cloud event containing the source to validate.
     * @return Returns the ValidationResult containing a success or a failure with
     *         the error message.
     */
    public abstract ValidationResult validateSource(CloudEvent cloudEvent);

    /**
     * 
     * @param cloudEvent
     * @return
     */
    public abstract ValidationResult validateType(CloudEvent cloudEvent);

    /**
     * Validate the sink value of a cloud event in the default scenario where the
     * sink attribute is optional.
     * 
     * @param cloudEvent The cloud event containing the sink to validate.
     * @return Returns the ValidationResult containing a success or a failure with
     *         the error message.
     */
    public abstract ValidationResult validateSink(CloudEvent cloudEvent);

    /**
     * Implements Validations for a CloudEvent of type Publish.
     */
    private static class Publish extends CloudEventValidator {

        @Override
        public ValidationResult validateSource(CloudEvent cloudEvent) {
            final String source = cloudEvent.getSource().toString();
            return UriValidator.isTopic(UriSerializer.deserialize(source)) ? ValidationResult.success()
                    : ValidationResult.failure(
                            String.format("Invalid Publish type CloudEvent source [%s].", source));
        }

        @Override
        public ValidationResult validateType(CloudEvent cloudEvent) {
            return "pub.v1".equals(cloudEvent.getType()) ? ValidationResult.success()
                    : ValidationResult.failure(
                            String.format("Invalid CloudEvent type [%s]. CloudEvent of type " +
                                    "Publish must have a type of 'pub.v1'", cloudEvent.getType()));
        }

        @Override
        public String toString() {
            return "CloudEventValidator.Publish";
        }

        @Override
        public ValidationResult validateSink(CloudEvent cloudEvent) {
            return UCloudEvent.getSink(cloudEvent).isPresent()
                    ? ValidationResult.failure("Publish should not have a sink")
                    : ValidationResult.success();
        }
    }

    /**
     * Implements Validations for a CloudEvent of type Publish that behaves as a
     * Notification, meaning
     * it must have a sink.
     */
    private static class Notification extends CloudEventValidator {

        @Override
        public ValidationResult validateSink(CloudEvent cloudEvent) {
            final Optional<String> maybeSink = UCloudEvent.getSink(cloudEvent);
            if (maybeSink.isEmpty()) {
                return ValidationResult.failure("Invalid CloudEvent sink. " +
                        "Notification CloudEvent sink must be an uri.");
            }
            String sink = maybeSink.get();
            return UriValidator.isDefaultResourceId(UriSerializer.deserialize(sink)) ? ValidationResult.success()
                    : ValidationResult.failure(String.format("Invalid Notification type CloudEvent sink [%s].", sink));
        }

        @Override
        public ValidationResult validateSource(CloudEvent cloudEvent) {
            final String source = cloudEvent.getSource().toString();
            return UriValidator.isTopic(UriSerializer.deserialize(source)) ? ValidationResult.success()
                    : ValidationResult.failure(
                            String.format("Invalid Notification type CloudEvent source [%s].", source));
        }

        @Override
        public ValidationResult validateType(CloudEvent cloudEvent) {
            return "not.v1".equals(cloudEvent.getType()) ? ValidationResult.success()
                    : ValidationResult.failure(
                            String.format("Invalid CloudEvent type [%s]. CloudEvent of type " +
                                    "Notification must have a type of 'not.v1'", cloudEvent.getType()));
        }

        @Override
        public String toString() {
            return "CloudEventValidator.Notification";
        }
    }

    /**
     * Implements Validations for a CloudEvent for RPC Request.
     */
    private static class Request extends CloudEventValidator {

        @Override
        public ValidationResult validateSource(CloudEvent cloudEvent) {
            final String source = cloudEvent.getSource().toString();
            return UriValidator.isRpcResponse(UriSerializer.deserialize(source)) ? ValidationResult.success()
                    : ValidationResult.failure(
                            String.format("Invalid RPC Request type CloudEvent source [%s].", source));
        }

        @Override
        public ValidationResult validateSink(CloudEvent cloudEvent) {
            final Optional<String> sink = UCloudEvent.getSink(cloudEvent);
            if (sink.isEmpty()) {
                return ValidationResult.failure("Invalid CloudEvent sink. " +
                        "RPC Request CloudEvent sink must be an uri.");
            }
            return UriValidator.isRpcMethod(UriSerializer.deserialize(sink.get())) ? ValidationResult.success()
                    : ValidationResult.failure(
                            String.format("Invalid RPC Request type CloudEvent sink [%s].", sink.get()));
        }

        @Override
        public ValidationResult validateType(CloudEvent cloudEvent) {
            return "req.v1".equals(cloudEvent.getType()) ? ValidationResult.success()
                    : ValidationResult.failure(String.format("Invalid CloudEvent type [%s]. " +
                            "CloudEvent of type Request must have a type of 'req.v1'", cloudEvent.getType()));
        }

        @Override
        public String toString() {
            return "CloudEventValidator.Request";
        }
    }

    /**
     * Implements Validations for a CloudEvent for RPC Response.
     */
    private static class Response extends CloudEventValidator {

        @Override
        public ValidationResult validateSource(CloudEvent cloudEvent) {
            final String source = cloudEvent.getSource().toString();
            return UriValidator.isRpcMethod(UriSerializer.deserialize(source)) ? ValidationResult.success()
                    : ValidationResult.failure(
                            String.format("Invalid RPC Response type CloudEvent source [%s].", source));
        }

        @Override
        public ValidationResult validateSink(CloudEvent cloudEvent) {
            final Optional<String> sink = UCloudEvent.getSink(cloudEvent);
            if (sink.isEmpty()) {
                return ValidationResult.failure("Invalid CloudEvent sink. " +
                        "RPC Response CloudEvent sink must be an uri.");
            }
            return UriValidator.isRpcResponse(UriSerializer.deserialize(sink.get())) ? ValidationResult.success()
                    : ValidationResult.failure(
                            String.format("Invalid RPC Response type CloudEvent sink [%s].", sink.get()));
        }

        @Override
        public ValidationResult validateType(CloudEvent cloudEvent) {
            return "res.v1".equals(cloudEvent.getType()) ? ValidationResult.success()
                    : ValidationResult.failure(String.format("Invalid CloudEvent type [%s]. " +
                            "CloudEvent of type Response must have a type of 'res.v1'", cloudEvent.getType()));
        }

        @Override
        public String toString() {
            return "CloudEventValidator.Response";
        }
    }
}
