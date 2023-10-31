package org.eclipse.uprotocol.uri.validator;

import java.util.Objects;

import org.eclipse.uprotocol.v1.UAuthority;
import org.eclipse.uprotocol.v1.UResource;
import org.eclipse.uprotocol.v1.UUri;
import org.eclipse.uprotocol.validation.ValidationResult;

/**
 * class for validating Uris.
 */
public interface UriValidator {

    /**
     * Validate a {@link UUri} to ensure that it has at least a name for the uEntity.
     * @param uri {@link UUri} to validate.
     * @return Returns UStatus containing a success or a failure with the error message.
     */
    static ValidationResult validate(UUri uri) {
        if (isEmpty(uri)) {
            return ValidationResult.failure("Uri is empty.");
        }
        if (uri.hasAuthority() && !isRemote(uri.getAuthority())) {
            return ValidationResult.failure("Uri is remote missing uAuthority.");
        }

        if (uri.getEntity().getName().isBlank()) {
            return ValidationResult.failure("Uri is missing uSoftware Entity name.");
        }

        return ValidationResult.success();
    }

    /**
     * Validate a {@link UUri} that is meant to be used as an RPC method URI. Used in Request sink values and Response source values.
     * @param uri {@link UUri} to validate.
     * @return Returns UStatus containing a success or a failure with the error message.
     */
    static ValidationResult validateRpcMethod(UUri uri) {
        ValidationResult status = validate(uri);
        if (status.isFailure()){
            return status;
        }
        
        if (!isRpcMethod(uri)) {
            return ValidationResult.failure("Invalid RPC method uri. Uri should be the method to be called, or method from response.");
        }
        return ValidationResult.success();
    }

    /**
     * Validate a {@link UUri} that is meant to be used as an RPC response URI. Used in Request source values and Response sink values.
     * @param uri {@link UUri} to validate.
     * @return Returns UStatus containing a success or a failure with the error message.
     */
    static ValidationResult validateRpcResponse(UUri uri) {
        ValidationResult status = validate(uri);
        if (status.isFailure()){
            return status;
        }
        
        if (!isRpcResponse(uri)) {
            return ValidationResult.failure("Invalid RPC response type.");
        }

        return ValidationResult.success();
    }


    /**
     * Indicates that this  URI is an empty as it does not contain authority, entity, and resource.
     * @param uri {@link UUri} to check if it is empty
     * @return Returns true if this  URI is an empty container and has no valuable information in building uProtocol sinks or sources.
     */
    static boolean isEmpty(UUri uri) {
        Objects.requireNonNull(uri, "Uri cannot be null.");
        return !uri.hasAuthority() && !uri.hasEntity() && !uri.hasResource();
    }


    /**
     * Returns true if URI is of type RPC.
     * @param uri {@link UUri} to check if it is of type RPC method
     * @return Returns true if URI is of type RPC.
     */
    static boolean isRpcMethod(UUri uri) {
        Objects.requireNonNull(uri, "Uri cannot be null.");
        return !isEmpty(uri) && uri.getResource().getName().contains("rpc");
    }

    /**
     * Returns true if URI contains both names and numeric representations of the names inside its belly.
     * Meaning that this UUri can be serialized to long or micro formats.
     * @param uri {@link UUri} to check if resolved.
     * @return Returns true if URI contains both names and numeric representations of the names inside its belly.
     *      Meaning that this UUri can buree serialized to long or micro formats.
     */
    static boolean isResolved(UUri uri) {
        Objects.requireNonNull(uri, "Uri cannot be null.");
        return !isEmpty(uri);
        // TODO: Finish this
    }


    /**
     * Returns true if URI is of type RPC response.
     * @param uri {@link UUri} to check response
     * @return Returns true if URI is of type RPC response.
     */
    static boolean isRpcResponse(UUri uri) {
        Objects.requireNonNull(uri, "Uri cannot be null.");
        final UResource resource = uri.getResource();
        return isRpcMethod(uri) && 
            ((resource.hasInstance() && resource.getInstance().contains("response")) || 
             (resource.hasId() && resource.getId() == 0));
    }



    /**
     * Returns true if URI contains numbers so that it can be serialized into micro format.
     * @param uri {@link UUri} to check
     * @return Returns true if URI contains numbers so that it can be serialized into micro format.
     */
    static boolean isMicroForm(UUri uri) {
        Objects.requireNonNull(uri, "Uri cannot be null.");

        return !isEmpty(uri) && 
            uri.getEntity().hasId() &&
            uri.getResource().hasId() &&
            (!uri.hasAuthority() || uri.getAuthority().hasIp() || uri.getAuthority().hasId());
    }

    /**
     * Returns true if URI contains names so that it can be serialized into long format.
     * @param uri {@link UUri} to check
     * @return Returns true if URI contains names so that it can be serialized into long format.
     */
    static boolean isLongForm(UUri uri) {
        Objects.requireNonNull(uri, "Uri cannot be null.");
        return !isEmpty(uri) && 
            !(uri.hasAuthority() && !uri.getAuthority().hasName()) &&
            !uri.getEntity().getName().isBlank() &&
            !uri.getResource().getName().isBlank();
    }


    static boolean isRemote(UAuthority authority) {
        Objects.requireNonNull(authority, "Uri cannot be null.");
        return authority.getRemoteCase() != UAuthority.RemoteCase.REMOTE_NOT_SET;
    }
}
