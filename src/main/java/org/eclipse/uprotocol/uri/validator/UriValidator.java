package org.eclipse.uprotocol.uri.validator;

import org.eclipse.uprotocol.uri.datamodel.UAuthority;
import org.eclipse.uprotocol.uri.datamodel.UResource;
import org.eclipse.uprotocol.uri.datamodel.UUri;
import org.eclipse.uprotocol.utransport.datamodel.UStatus;
import org.eclipse.uprotocol.utransport.datamodel.UStatus.Code;


/**
 * class for validating Uris.
 */
public abstract class UriValidator {

    /**
     * Validate a Uri to ensure that it has at least a name for the uEntity.
     * @param uri Uri to validate.
     * @return Returns UStatus containing a success or a failure with the error message.
     */
    public static UStatus validate(UUri uri) {
        final UAuthority uAuthority = uri.uAuthority();
        if (uAuthority.isMarkedRemote()) {
            if (uAuthority.device().isEmpty()) {
                return UStatus.failed("Uri is configured to be remote and is missing uAuthority device name.", Code.INVALID_ARGUMENT);
            }
        }
        if (uri.uEntity().name().isBlank()) {
            return UStatus.failed("Uri is missing uSoftware Entity name.", Code.INVALID_ARGUMENT);
        }
        return UStatus.ok();
    }

    /**
     * Validate a Uri that is meant to be used as an RPC method URI. Used in Request sink values and Response source values.
     * @param uri Uri to validate.
     * @return Returns the ValidationResult containing a success or a failure with the error message.
     */
    public static UStatus validateRpcMethod(UUri uri) {
        UStatus status = validate(uri);
        if (status.isFailed()){
            return status;
        }
        final UResource uResource = uri.uResource();
        if (!uResource.isRPCMethod()) {
            return UStatus.failed("Invalid RPC method uri. Uri should be the method to be called, or method from response.", Code.INVALID_ARGUMENT);
        }
        return UStatus.ok();
    }

    /**
     * Validate a Uri that is meant to be used as an RPC response URI. Used in Request source values and Response sink values.
     * 
     * @param uri Uri to validate.
     * @return Returns the ValidationResult containing a success or a failure with the error message.
     */
    public static UStatus validateRpcResponse(UUri uri) {
        UStatus status = validate(uri);
        if (status.isFailed()){
            return status;
        }
        
        final UResource uResource = uri.uResource();
        if (!uResource.isRPCMethod() || !uResource.equals(UResource.response())) {
            return UStatus.failed("Invalid RPC response type.", Code.INVALID_ARGUMENT);
        }

        return UStatus.ok();
    }
}
