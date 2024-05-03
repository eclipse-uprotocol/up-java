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
 * SPDX-FileType: SOURCE
 * SPDX-FileCopyrightText: 2023 General Motors GTO LLC
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.uprotocol.cloudevent.validate;


import io.cloudevents.CloudEvent;
import org.eclipse.uprotocol.cloudevent.factory.UCloudEvent;
import org.eclipse.uprotocol.v1.*;
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
     * Obtain a CloudEventValidator according to the type attribute in the CloudEvent.
     * @param cloudEvent The CloudEvent with the type attribute.
     * @return Returns a CloudEventValidator according to the type attribute in the CloudEvent.
     */
    public static CloudEventValidator getValidator(CloudEvent cloudEvent){
        final String cloudEventType = cloudEvent.getType();
        if (cloudEventType.isEmpty()) {
            return Validators.PUBLISH.validator();
        }
        
        CloudEventValidator validator;
        switch (UCloudEvent.getMessageType(cloudEvent.getType())){
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
        PUBLISH (new Publish()),
        NOTIFICATION (new Notification()),
        REQUEST (new Request()),
        RESPONSE (new Response());

        private final CloudEventValidator cloudEventValidator;
        public CloudEventValidator validator() {
            return cloudEventValidator;
        }

        Validators(CloudEventValidator cloudEventValidator) {
            this.cloudEventValidator = cloudEventValidator;
        }
    }

    /**
     * Validate the CloudEvent. A CloudEventValidator instance is obtained according to the type attribute on the CloudEvent.
     * @param cloudEvent The CloudEvent to validate.
     * @return Returns a google.rpc.Status with success or a google.rpc.Status with failure containing all the errors that were found.
     */
    public ValidationResult validate(CloudEvent cloudEvent) {
        final String errorMessage = Stream.of(validateVersion(cloudEvent), validateId(cloudEvent),
                        validateSource(cloudEvent), validateType(cloudEvent), validateSink(cloudEvent))
                .filter(ValidationResult::isFailure)
                .map(ValidationResult::getMessage)
                .collect(Collectors.joining(","));
        return errorMessage.isBlank() ? ValidationResult.success() :
                ValidationResult.failure(errorMessage);
    }

    public static ValidationResult validateVersion(CloudEvent cloudEvent) {
        return validateVersion(cloudEvent.getSpecVersion().toString());

    }

    public static ValidationResult validateVersion(String version) {
        return version.equals("1.0") ?
                ValidationResult.success() : ValidationResult.failure(String.format("Invalid CloudEvent version [%s]. CloudEvent version must be 1.0.", version));
    }

    public static ValidationResult validateId(CloudEvent cloudEvent) {
        return UCloudEvent.isCloudEventId(cloudEvent) ?
                ValidationResult.success() : ValidationResult.failure(String.format("Invalid CloudEvent Id [%s]. CloudEvent Id must be of type UUIDv8.", cloudEvent.getId()));
    }

    /**
     * Validate the source value of a cloud event.
     * @param cloudEvent The cloud event containing the source to validate.
     * @return Returns the ValidationResult containing a success or a failure with the error message.
     */
    public abstract ValidationResult validateSource(CloudEvent cloudEvent);

    public abstract ValidationResult validateType(CloudEvent cloudEvent);

    /**
     * Validate the sink value of a cloud event in the default scenario where the sink attribute is optional.
     * @param cloudEvent The cloud event containing the sink to validate.
     * @return Returns the ValidationResult containing a success or a failure with the error message.
     */
    public ValidationResult validateSink(CloudEvent cloudEvent) {
        final Optional<String> maybeSink = UCloudEvent.getSink(cloudEvent);
        if (maybeSink.isPresent()){
            final String sink = maybeSink.get();
            ValidationResult checkSink = validateUEntityUri(sink);
            if (checkSink.isFailure()) {
                return ValidationResult.failure(String.format("Invalid CloudEvent sink [%s]. %s", sink, checkSink.getMessage()));
            }
        }
        return ValidationResult.success();
    }

    /**
     * Validate an  UriPart for an  Software Entity must have an authority in the case of a microRemote uri, and must contain
     * the name of the USE.
     * @param uri uri string to validate.
     * @return Returns the ValidationResult containing a success or a failure with the error message.
     */
    public static ValidationResult validateUEntityUri(String uri) {
        UUri Uri = UriSerializer.deserialize(uri);
        return validateUEntityUri(Uri);
    }

    public static ValidationResult validateUEntityUri(UUri Uri) {
        return UriValidator.isEmpty(Uri) ? 
            ValidationResult.failure("Uri is empty.") : ValidationResult.success();
    }

/**
     * Validate a UriPart that is to be used as a topic in publish scenarios for events such as publish, file and notification.
     * @param uri String UriPart to validate.
     * @return Returns the ValidationResult containing a success or a failure with the error message.
     */
    public static ValidationResult validateTopicUri(String uri) {
        UUri Uri = UriSerializer.deserialize(uri);
        return validateTopicUri(Uri);
    }

    /**
     * Validate a UriPart that is to be used as a topic in publish scenarios for events such as publish, file and notification.
     * @param Uri UriPart to validate.
     * @return Returns the ValidationResult containing a success or a failure with the error message.
     */
    public static ValidationResult validateTopicUri(UUri Uri) {
        final ValidationResult validationResult = validateUEntityUri(Uri);
        if (validationResult.isFailure()) {
            return validationResult;
        }
        if (UriValidator.isRpcMethod(Uri)) {
            return ValidationResult.failure("Invalid topic uri. UriPart should have a resource id greater than 0x8000.");
        }
        return ValidationResult.success();
    }

    /**
     * Validate a UriPart that is meant to be used as the application response topic for rpc calls. <br>
     * Used in Request source values and Response sink values.
     * @param uri String UriPart to validate.
     * @return Returns the ValidationResult containing a success or a failure with the error message.
     */
    public static ValidationResult validateRpcTopicUri(String uri) {
        UUri Uri = UriSerializer.deserialize(uri);
        return validateRpcTopicUri(Uri);
    }

    /**
     * Validate an  UriPart that is meant to be used as the application response topic for rpc calls. <br>
     * @param Uri  UriPart to validate.
     * @return Returns the ValidationResult containing a success or a failure with the error message.
     */
    public static ValidationResult validateRpcTopicUri(UUri Uri) {
        ValidationResult validationResult = validateUEntityUri(Uri);
        if (validationResult.isFailure()){
            return ValidationResult.failure(String.format("Invalid RPC uri application response topic. %s", validationResult.getMessage()));
        }
        
        if (UriValidator.isRpcResponse(Uri)) {
            return ValidationResult.failure("Invalid RPC uri application response topic. UriPart is missing rpc.response.");
        }
        return ValidationResult.success();
    }

    /**
     * Validate a UriPart that is meant to be used as an RPC method URI. Used in Request sink values and Response source values.
     * @param uri String UriPart to validate.
     * @return Returns the ValidationResult containing a success or a failure with the error message.
     */
    public static ValidationResult validateRpcMethod(String uri) {
        UUri Uri = UriSerializer.deserialize(uri);
        ValidationResult validationResult = validateUEntityUri(Uri);
        if (validationResult.isFailure()){
            return ValidationResult.failure(String.format("Invalid RPC method uri. %s", validationResult.getMessage()));
        }
        
        if (!UriValidator.isRpcMethod(Uri)) {
            return ValidationResult.failure("Invalid RPC method uri. UriPart should be the method to be called, or method from response.");
        }
        return ValidationResult.success();
    }

    /**
     * Implements Validations for a CloudEvent of type Publish.
     */
    private static class Publish extends CloudEventValidator {

        @Override
        public ValidationResult validateSource(CloudEvent cloudEvent) {
            final String source = cloudEvent.getSource().toString();
            ValidationResult checkSource = validateTopicUri(source);
            if (checkSource.isFailure()) {
                return ValidationResult.failure(String.format("Invalid Publish type CloudEvent source [%s]. %s", source, checkSource.getMessage()));
            }
            return ValidationResult.success();
        }

        @Override
        public ValidationResult validateType(CloudEvent cloudEvent) {
            return "pub.v1".equals(cloudEvent.getType()) ? ValidationResult.success() :
                    ValidationResult.failure(String.format("Invalid CloudEvent type [%s]. CloudEvent of type Publish must have a type of 'pub.v1'", cloudEvent.getType()));
        }

        @Override
        public String toString() {
            return "CloudEventValidator.Publish";
        }
    }

    /**
     * Implements Validations for a CloudEvent of type Publish that behaves as a Notification, meaning
     * it must have a sink.
     */
    private static class Notification extends CloudEventValidator {

        @Override
        public ValidationResult validateSink(CloudEvent cloudEvent) {
            final Optional<String> maybeSink = UCloudEvent.getSink(cloudEvent);
            if (maybeSink.isEmpty()){
                return ValidationResult.failure("Invalid CloudEvent sink. Notification CloudEvent sink must be an  uri.");
            } else {
                String sink = maybeSink.get();
                ValidationResult checkSink = validateUEntityUri(sink);
                if (checkSink.isFailure()) {
                    return ValidationResult.failure(String.format("Invalid Notification type CloudEvent sink [%s]. %s", sink, checkSink.getMessage()));
                }
            }
            return ValidationResult.success();
        }

        @Override
        public String toString() {
            return "CloudEventValidator.Notification";
        }

        @Override
        public ValidationResult validateSource(CloudEvent cloudEvent) {
            final String source = cloudEvent.getSource().toString();
            ValidationResult checkSource = validateTopicUri(source);
            if (checkSource.isFailure()) {
                return ValidationResult.failure(String.format("Invalid Notification type CloudEvent source [%s]. %s", source, checkSource.getMessage()));
            }
            return ValidationResult.success();
        }

        @Override
        public ValidationResult validateType(CloudEvent cloudEvent) {
            return "not.v1".equals(cloudEvent.getType()) ? ValidationResult.success() :
                    ValidationResult.failure(String.format("Invalid CloudEvent type [%s]. CloudEvent of type Notification must have a type of 'not.v1'", cloudEvent.getType()));
        }
    }


    /**
     * Implements Validations for a CloudEvent for RPC Request.
     */
    private static class Request extends CloudEventValidator {

        @Override
        public ValidationResult validateSource(CloudEvent cloudEvent) {
            final String source = cloudEvent.getSource().toString();
            ValidationResult checkSource = validateRpcTopicUri(source);
            if (checkSource.isFailure()) {
                return ValidationResult.failure(String.format("Invalid RPC Request CloudEvent source [%s]. %s", source, checkSource.getMessage()));
            }
            return ValidationResult.success();
        }

        @Override
        public ValidationResult validateSink(CloudEvent cloudEvent) {
            final Optional<String> maybeSink = UCloudEvent.getSink(cloudEvent);
            if (maybeSink.isEmpty()){
                return ValidationResult.failure("Invalid RPC Request CloudEvent sink. Request CloudEvent sink must be uri for the method to be called.");
            } else {
                String sink = maybeSink.get();
                ValidationResult checkSink = validateRpcMethod(sink);
                if (checkSink.isFailure()) {
                    return ValidationResult.failure(String.format("Invalid RPC Request CloudEvent sink [%s]. %s", sink, checkSink.getMessage()));
                }
            }
            return ValidationResult.success();
        }

        @Override
        public ValidationResult validateType(CloudEvent cloudEvent) {
            return "req.v1".equals(cloudEvent.getType()) ? ValidationResult.success() :
                    ValidationResult.failure(String.format("Invalid CloudEvent type [%s]. CloudEvent of type Request must have a type of 'req.v1'", cloudEvent.getType()));
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
            ValidationResult checkSource = validateRpcMethod(source);
            if (checkSource.isFailure()) {
                return ValidationResult.failure(String.format("Invalid RPC Response CloudEvent source [%s]. %s", source, checkSource.getMessage()));
            }
            return ValidationResult.success();
        }

        @Override
        public ValidationResult validateSink(CloudEvent cloudEvent) {
            final Optional<String> maybeSink = UCloudEvent.getSink(cloudEvent);
            if (maybeSink.isEmpty()){
                return ValidationResult.failure("Invalid CloudEvent sink. Response CloudEvent sink must be uri the destination of the response.");
            } else {
                String sink = maybeSink.get();
                ValidationResult checkSink = validateRpcTopicUri(sink);
                if (checkSink.isFailure()) {
                    return ValidationResult.failure(String.format("Invalid RPC Response CloudEvent sink [%s]. %s", sink, checkSink.getMessage()));
                }
            }
            return ValidationResult.success();
        }

        @Override
        public ValidationResult validateType(CloudEvent cloudEvent) {
            return "res.v1".equals(cloudEvent.getType()) ? ValidationResult.success() :
                    ValidationResult.failure(String.format("Invalid CloudEvent type [%s]. CloudEvent of type Response must have a type of 'res.v1'", cloudEvent.getType()));
        }

        @Override
        public String toString() {
            return "CloudEventValidator.Response";
        }
    }

}
