package org.eclipse.uprotocol.uri.validator;

import org.eclipse.uprotocol.uri.datamodel.UResource;
import org.eclipse.uprotocol.uri.datamodel.UUri;
import org.eclipse.uprotocol.uri.factory.UriFactory;
import org.eclipse.uprotocol.utransport.datamodel.UStatus;
import org.eclipse.uprotocol.utransport.datamodel.UStatus.Code;


/**
 * class for validating Uris.
 */
public interface UriValidator {

    /**
     * Validate a Uri to ensure that it has at least a name for the uEntity.
     * @param uri Uri to validate.
     * @return Returns UStatus containing a success or a failure with the error message.
     */
    public static UStatus validate(UUri uri) {
        if (uri.isEmpty()) {
            return UStatus.failed("Uri is empty.", Code.INVALID_ARGUMENT);
        }

        if (uri.uEntity().name().isBlank()) {
            return UStatus.failed("Uri is missing uSoftware Entity name.", Code.INVALID_ARGUMENT);
        }
        return UStatus.ok();
    }

    /**
     * Validate a Uri that is meant to be used as an RPC method URI. Used in Request sink values and Response source values.
     * @param uri Uri to validate.
     * @return Returns UStatus containing a success or a failure with the error message.
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
     * @return Returns UStatus containing a success or a failure with the error message.
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


    /**
     * Wrapper to validate explicitly a long form URI
     * @param uri Long form URI
     * @return Returns UStatus containing a success or a failure with the error message.
     */
    public static UStatus validateLongUUri(String uri) {
        final UUri uUri = UriFactory.parseFromUri(uri);
        return validate(uUri);
    }

    /**
     * Validate that a passed shortUri and microUri are the same. We cannot validate long to short
     * without the mapping of names to ids.
     * @param shortUri Short form URI
     * @param microUri Micro form URI
     * @return Returns the UStatus containing a success or a failure with the error message.
     */
    public static UStatus validateEqualsShortMicroUri(String shortUri, byte[] microUri) {
        final UUri uUri = UriFactory.parseFromUri(shortUri);
        final UUri uUriMicro = UriFactory.parseFromMicroUri(microUri);
        
        if (uUri.isEmpty()) {
            return UStatus.failed("Short Uri is invalid.", Code.INVALID_ARGUMENT);
        }
        if (uUriMicro.isEmpty()) {
            return UStatus.failed("Micro Uri is invalid.", Code.INVALID_ARGUMENT);
        }

        if (!uUri.equals(uUriMicro)) {
            String failure = String.format("Short URI %s and Micro Uri %s are not equal.", uUri.toString(), uUriMicro.toString());
            return UStatus.failed(failure, Code.INVALID_ARGUMENT);
        }
        return UStatus.ok();
    }

}
