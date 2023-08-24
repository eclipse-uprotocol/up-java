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

import com.google.rpc.Code;

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
                    validateCorrelationId(attributes))
                .filter(UStatus::isFailed)
                .map(UStatus::msg)
                .collect(Collectors.joining(","));
        return errorMessage.isBlank() ? UStatus.ok() :
                UStatus.failed("Invalid argument", Code.INVALID_ARGUMENT_VALUE);
    }


    public UStatus validatePriority(UAttributes attributes) {
        return attributes.priority() != null ? UStatus.ok() : UStatus.failed();
    }

    public UStatus validateTtl(UAttributes attributes) {
        Optional<Integer> ttl = attributes.ttl();
        if (ttl.isPresent()) {
            return ttl.get() > 0 ? UStatus.ok() : UStatus.failed();
        }
        return UStatus.ok();
    }

    public UStatus validateId(UAttributes attributes) {
        try {
            return UUIDUtils.isUuid(attributes.id()) ? UStatus.ok() : UStatus.failed();
        } catch (Exception e) {
            return UStatus.failed();
        }
    }

    /**
     * Validate the sink Uri for the default case.
     * @param attributes UAttributes to validate
     * @return Returns a UStatus indicating if the Uri is valid or not.
     */
    public UStatus validateSink(UAttributes attributes) {
        if (!attributes.sink().isEmpty()){
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
        if (!attributes.commstatus().isEmpty()){
            return Code.forNumber(attributes.commstatus().get()) != null ? UStatus.ok() : UStatus.failed();
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
            
            return plevel.get() > 0 ? UStatus.ok() : UStatus.failed();
        }
        return UStatus.ok();
    }

    /**
     * Validate the correlationId for the default case.
     * @param attributes UAttributes to validate
     * @return Returns a UStatus indicating if the correlationId is valid or not.
     */
    public UStatus validateCorrelationId(UAttributes attributes) {
        final Optional<UUID> correlationId = attributes.reqid();
        
        if (correlationId.isPresent()) {
            return UUIDUtils.isUuid(correlationId.get()) ? UStatus.ok() : UStatus.failed();
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
     * PUBLISH UAttributes Validator
     * 
     */
    private static class Publish extends UAttributesValidator {

        /**
         * Validate UAttributes with type UMessageType PUBLISH
         * @param attributes UAttributes to validate
         * @return Returns a UStatus indicating if the MessageType is valid or not.
         */
        @Override
        public UStatus validateType(UAttributes attributes) {
            return attributes.type() == UMessageType.PUBLISH ? UStatus.ok() : UStatus.failed();
        }
    }

    /**
     * Validate UAttributes with type UMessageType Request
     */
    private static class Request extends UAttributesValidator {

        @Override
        public UStatus validateType(UAttributes attributes) {
            return attributes.type() == UMessageType.REQUEST ? UStatus.ok() : UStatus.failed();
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
                return UStatus.failed("Missing Sink", Code.INVALID_ARGUMENT_VALUE);
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
                return UStatus.failed("Missing TTL", Code.INVALID_ARGUMENT_VALUE);
            } else {
                return ttl.get() > 0 ? UStatus.ok() : UStatus.failed();
            }
        }
    }


    /**
     * Validate UAttributes with type UMessageType RESPONSE
    */
    private static class Response extends UAttributesValidator {

        @Override
        public UStatus validateType(UAttributes attributes) {
            return attributes.type() == UMessageType.RESPONSE ? UStatus.ok() : UStatus.failed();
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
                return UStatus.failed("Missing Sink", Code.INVALID_ARGUMENT_VALUE);
            }
            return UriValidator.validateRpcMethod(sink.get());
        }

        /**
        * Validate the correlationId for the default case.
        * @param attributes
        * @return Returns a UStatus indicating if the correlationId is valid or not.
        */
        @Override
        public UStatus validateCorrelationId(UAttributes attributes) {
            final Optional<UUID> correlationId = attributes.reqid();
        
            if (!correlationId.isPresent()) {
                return UStatus.failed("Missing correlationId", Code.INVALID_ARGUMENT_VALUE);
            }
            return UUIDUtils.isUuid(correlationId.get()) ? UStatus.ok() : UStatus.failed();
        }
    }

}
