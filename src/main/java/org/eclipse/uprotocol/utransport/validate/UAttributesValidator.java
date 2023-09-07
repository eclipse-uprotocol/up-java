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
package org.eclipse.uprotocol.utransport.validate;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.uprotocol.uri.datamodel.UUri;
import org.eclipse.uprotocol.uri.validator.UriValidator;
import org.eclipse.uprotocol.utransport.datamodel.UAttributes;
import org.eclipse.uprotocol.utransport.datamodel.UMessageType;
import org.eclipse.uprotocol.utransport.datamodel.UStatus;
import org.eclipse.uprotocol.uuid.factory.UUIDUtils;
import org.eclipse.uprotocol.utransport.datamodel.UStatus.Code;

/**
 * Abstract class for validating UAttributes.
 * 
 * UPriority is not validated since it cannot be anything other than a valid UPriority.
 * 
 * 
 */
public abstract class UAttributesValidator {
 
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
     * Enum that hold the implementations of uattributesValidator according to type.
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


    public UStatus validate(UAttributes attributes) {
        final String errorMessage = Stream.of(validateType(attributes),
                    validateId(attributes), 
                    validateSink(attributes), 
                    validatePriority(attributes),
                    validateCommStatus(attributes),
                    validateTtl(attributes),
                    validatePermissionLevel(attributes),
                    validateReqId(attributes))
                .filter(UStatus::isFailed)
                .map(UStatus::msg)
                .collect(Collectors.joining(","));
        return errorMessage.isBlank() ? UStatus.ok() :
                UStatus.failed(errorMessage, Code.INVALID_ARGUMENT);
    }


    public UStatus validatePriority(UAttributes attributes) {
        return attributes.priority() != null ? UStatus.ok() : 
            UStatus.failed("Invalid Priority", Code.INVALID_ARGUMENT.value());
    }

    public UStatus validateTtl(UAttributes attributes) {
        Optional<Integer> ttl = attributes.ttl();
        if (ttl.isPresent()) {
            return ttl.get() > 0 ? UStatus.ok() : 
                UStatus.failed("Invalid TTL", Code.INVALID_ARGUMENT.value());
        }
        return UStatus.ok();
    }

    public UStatus validateId(UAttributes attributes) {
        try {
            return UUIDUtils.isUuid(attributes.id()) ? UStatus.ok() : 
                UStatus.failed("Invalid UUID", Code.INVALID_ARGUMENT.value());
        } catch (Exception e) {
            return UStatus.failed("Invalid UUID", Code.INVALID_ARGUMENT.value());
        }
    }

    /**
     * Validate the sink Uri for the default case.
     * @param attributes UAttributes to validate
     * @return Returns a UStatus indicating if the Uri is valid or not.
     */
    public UStatus validateSink(UAttributes attributes) {
        Optional<UUri> sink = attributes.sink();
        if (sink.isPresent()) {
            return UriValidator.validate(attributes.sink().get());
        }
        return UStatus.ok();
    }

    /**
     * Validate the commStatus for the default case.
     * @param attributes UAttributes to validate
     * @return Returns a UStatus indicating if the commStatus is valid or not.
     */
    public UStatus validateCommStatus(UAttributes attributes) {
        Optional<Integer> commStatus = attributes.commstatus();

        if (commStatus.isPresent()) {
            Optional<Code> code = Code.from(commStatus.get());
            return code.isPresent() ? UStatus.ok() : 
                UStatus.failed("Invalid Communication Status Code", Code.INVALID_ARGUMENT.value());
        }
        return UStatus.ok();
    }

    /**
     * Validate the permissionLevel for the default case.
     * @param attributes UAttributes to validate
     * @return Returns a UStatus indicating if the permissionLevel is valid or not.
     */
    public UStatus validatePermissionLevel(UAttributes attributes) {
        final Optional<Integer> plevel = attributes.plevel();
        if (plevel.isPresent()) {
            return plevel.get() >= 0 ? UStatus.ok() : 
                UStatus.failed("Invalid Permission Level", Code.INVALID_ARGUMENT.value());
        }
        return UStatus.ok();
    }

    /**
     * Validate the correlationId for the default case.
     * @param attributes UAttributes to validate
     * @return Returns a UStatus indicating if the correlationId is valid or not.
     */
    public UStatus validateReqId(UAttributes attributes) {
        final Optional<UUID> correlationId = attributes.reqid();
        
        if (correlationId.isPresent()) {
            return UUIDUtils.isUuid(correlationId.get()) ? UStatus.ok() : 
                UStatus.failed("Invalid UUID", Code.INVALID_ARGUMENT.value());
        }
        return UStatus.ok();
    }

    
    /**
     * Validate the MessageType of UAttributes
     * @param attributes UAttributes to validate
     * @return Returns a UStatus indicating if the MessageType is valid or not.
     */
    public abstract UStatus validateType(UAttributes attributes);


    /**
     * Implements validations for UAttributes that define a message that is meant for publishing state changes.
     */
    private static class Publish extends UAttributesValidator {

        /**
         * Validates that attributes for a message meant to publish state changes has the correct type.
         * @param attributes UAttributes to validate
         * @return Returns a UStatus indicating if the MessageType is of the correct type.
         */
        @Override
        public UStatus validateType(UAttributes attributes) {
            return attributes.type() == UMessageType.PUBLISH ? UStatus.ok() :
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
         * @param attributes UAttributes to validate.
         * @return Returns a UStatus indicating if the MessageType is of the correct type.
         */
        @Override
        public UStatus validateType(UAttributes attributes) {
            return attributes.type() == UMessageType.REQUEST ? UStatus.ok() : 
                UStatus.failed(String.format("Wrong Attribute Type [%s]", attributes.type()), Code.INVALID_ARGUMENT.value());
        }

        /**
         * Validates that attributes for a message meant for an RPC request has a destination sink.
         * @param attributes Attributes to validate.
        * @return Returns a UStatus indicating if the Uri is valid or not.
        */
        @Override
        public UStatus validateSink(UAttributes attributes) {
            final Optional<UUri> sink = attributes.sink();

            if (!sink.isPresent()) {
                return UStatus.failed("Missing Sink", Code.INVALID_ARGUMENT.value());
            }
            return UriValidator.validateRpcResponse(sink.get());
        }

        /**
         * Validate the ttl.
         * @param attributes
         * @return Returns a UStatus indicating if the ttl is valid or not.
         */
        @Override
        public UStatus validateTtl(UAttributes attributes) {
            final Optional<Integer> ttl = attributes.ttl();
            if (!ttl.isPresent()) {
                return UStatus.failed("Missing TTL", Code.INVALID_ARGUMENT.value());
            } else {
                return ttl.get() > 0 ? UStatus.ok() : UStatus.failed("Invalid TTL", Code.INVALID_ARGUMENT.value());
            }
        }

        @Override
        public String toString() {
            return "UAttributesValidator.Request";
        }
    }


    /**
     * Validate UAttributes with type UMessageType RESPONSE
    */
    private static class Response extends UAttributesValidator {

        @Override
        public UStatus validateType(UAttributes attributes) {
            return attributes.type() == UMessageType.RESPONSE ? UStatus.ok() : 
                UStatus.failed("Invalid Type", Code.INVALID_ARGUMENT.value());
        }

        /**
         * Validate the sink Uri.
         * @param attributes
        * @return Returns a UStatus indicating if the Uri is valid or not.
        */
        @Override
        public UStatus validateSink(UAttributes attributes) {
            final Optional<UUri> sink = attributes.sink();

            if (!sink.isPresent()) {
                return UStatus.failed("Missing Sink", Code.INVALID_ARGUMENT.value());
            }
            return UriValidator.validateRpcMethod(sink.get());
        }

        /**
        * Validate the correlationId for the default case.
        * @param attributes
        * @return Returns a UStatus indicating if the correlationId is valid or not.
        */
        @Override
        public UStatus validateReqId(UAttributes attributes) {
            final Optional<UUID> correlationId = attributes.reqid();
        
            if (!correlationId.isPresent()) {
                return UStatus.failed("Missing correlationId", Code.INVALID_ARGUMENT.value());
            }
            return UUIDUtils.isUuid(correlationId.get()) ? UStatus.ok() : 
                UStatus.failed("Invalid UUID", Code.INVALID_ARGUMENT.value());
        }

        @Override
        public String toString() {
            return "UAttributesValidator.Response";
        }
    }

}
