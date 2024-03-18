package org.eclipse.uprotocol.uri.validator;

import org.eclipse.uprotocol.v1.UAuthority;
import org.eclipse.uprotocol.v1.UResource;
import org.eclipse.uprotocol.v1.UUri;
import org.eclipse.uprotocol.validation.ValidationResult;

import java.util.Objects;

/**
 * class for validating Uris.
 */
public interface UriValidator {

    /**
     * Validate a {@link UUri} to ensure that it has at least a name for the uEntity.
     *
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
     *
     * @param uri {@link UUri} to validate.
     * @return Returns UStatus containing a success or a failure with the error message.
     */
    static ValidationResult validateRpcMethod(UUri uri) {
        ValidationResult status = validate(uri);
        if (status.isFailure()) {
            return status;
        }

        if (!isRpcMethod(uri)) {
            return ValidationResult.failure("Invalid RPC method uri. Uri should be the method to be called, or method from response.");
        }
        return ValidationResult.success();
    }

    /**
     * Validate a {@link UUri} that is meant to be used as an RPC response URI. Used in Request source values and Response sink values.
     *
     * @param uri {@link UUri} to validate.
     * @return Returns UStatus containing a success or a failure with the error message.
     */
    static ValidationResult validateRpcResponse(UUri uri) {
        ValidationResult status = validate(uri);
        if (status.isFailure()) {
            return status;
        }

        if (!isRpcResponse(uri)) {
            return ValidationResult.failure("Invalid RPC response type.");
        }

        return ValidationResult.success();
    }


    /**
     * Indicates that this  URI is an empty as it does not contain authority, entity, and resource.
     *
     * @param uri {@link UUri} to check if it is empty
     * @return Returns true if this  URI is an empty container and has no valuable information in building uProtocol sinks or sources.
     */
    static boolean isEmpty(UUri uri) {
        Objects.requireNonNull(uri, "Uri cannot be null.");
        return !uri.hasAuthority() && !uri.hasEntity() && !uri.hasResource();
    }


    /**
     * Returns true if URI is of type RPC.
     *
     * @param uri {@link UUri} to check if it is of type RPC method
     * @return Returns true if URI is of type RPC.
     */
    static boolean isRpcMethod(UUri uri) {
        
        return (uri!= null) && uri.getResource().getName().contains("rpc") && 
            (uri.getResource().hasInstance() && !uri.getResource().getInstance().trim().isEmpty() 
                || (uri.getResource().hasId() && uri.getResource().getId() != 0));
    }

    /**
     * Returns true if URI contains both names and numeric representations of the names inside its belly.
     * Meaning that this UUri can be serialized to long or micro formats.
     *
     * @param uri {@link UUri} to check if resolved.
     * @return Returns true if URI contains both names and numeric representations of the names inside its belly.
     * Meaning that this UUri can buree serialized to long or micro formats.
     */
    static boolean isResolved(UUri uri) {
        return (uri != null) && !isEmpty(uri) && isLongForm(uri) && isMicroForm(uri);
    }


    /**
     * Returns true if URI is of type RPC response.
     *
     * @param uri {@link UUri} to check response
     * @return Returns true if URI is of type RPC response.
     */
    static boolean isRpcResponse(UUri uri) {
        if (uri == null) {
            return false;
        }

        final UResource resource = uri.getResource();
        return resource.getName().contains("rpc") && 
            resource.hasInstance() && resource.getInstance().contains("response") &&
            resource.hasId() && resource.getId() == 0;
    }


    /**
     * Returns true if URI contains numbers so that it can be serialized into micro format.
     *
     * @param uri {@link UUri} to check
     * @return Returns true if URI contains numbers so that it can be serialized into micro format.
     */
    static boolean isMicroForm(UUri uri) {
        return (uri !=null) && !isEmpty(uri) && uri.getEntity().hasId() && 
            uri.getResource().hasId() && isMicroForm(uri.getAuthority());
    }

    /**
     * check if UAuthority can be represented in micro format. Micro UAuthorities are local or ones 
     * that contain IP address or IDs.
     *
     * @param authority {@link UAuthority} to check
     * @return Returns true if UAuthority can be represented in micro format
     */
    static boolean isMicroForm(UAuthority authority) {
        return isLocal(authority) || (authority.hasIp() || authority.hasId());
    }

    /**
     * Returns true if URI contains names so that it can be serialized into long format.
     *
     * @param uri {@link UUri} to check
     * @return Returns true if URI contains names so that it can be serialized into long format.
     */
    static boolean isLongForm(UUri uri) {
        return (uri != null) && 
            !isEmpty(uri) && 
            isLongForm(uri.getAuthority()) && 
            !uri.getEntity().getName().isBlank() && !uri.getResource().getName().isBlank();
    }

    /**
     * Returns true if UAuthority is local or contains names so that it can be serialized into long format.
     *
     * @param authority {@link UAuthority} to check
     * @return Returns true if URI contains names so that it can be serialized into long format.
     */
    static boolean isLongForm(UAuthority authority) {
        return isLocal(authority) || 
            ((authority != null) && authority.hasName() && !authority.getName().isBlank());
    }


    /**
     * Returns true if UAuthority is local meaning there is no name/ip/id set.
     * 
     * @param authority {@link UAuthority} to check if it is local or not
     * @return Returns true if UAuthority is local meaning the Authority is not populated with name, ip and id
     */
    static boolean isLocal(UAuthority authority) {
        return (authority == null) || authority.equals(UAuthority.getDefaultInstance());
    }

    /**
     * Returns true if UAuthority is remote meaning the name and/or ip/id is populated.
     * @param authority {@link UAuthority} to check if it is remote or not
     * @return Returns true if UAuthority is remote meaning the name and/or ip/id is populated.
     */
    static boolean isRemote(UAuthority authority) {
        return (authority != null) && 
            !authority.equals(UAuthority.getDefaultInstance()) &&
            (isLongForm(authority) || isMicroForm(authority));
    }

    /**
     * Return True of the UUri is Short form. A UUri that is micro form (contains numbers) can
     * also be a Short form Uri.
     * @param uri {@link UUri} to check
     * @return Returns true if contains ids can can be serialized to short format.
     */
    static boolean isShortForm(UUri uri) {
        return isMicroForm(uri);
    }
}
