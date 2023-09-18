package org.eclipse.uprotocol.uri.validator;

import org.eclipse.uprotocol.uri.datamodel.UResource;
import org.eclipse.uprotocol.uri.datamodel.UUri;
import org.eclipse.uprotocol.uri.serializer.UriSerializer;
import org.eclipse.uprotocol.utransport.datamodel.UStatus;
import org.eclipse.uprotocol.utransport.datamodel.UStatus.Code;


/**
 * class for validating Uris.
 */
public interface UriValidator {

    /**
     * Validate a UriPart to ensure that it has at least a name for the uEntity.
     * @param uri UriPart to validate.
     * @return Returns UStatus containing a success or a failure with the error message.
     */
    public static UStatus validate(UUri uri) {
        if (uri.isEmpty()) {
            return UStatus.failed("UriPart is empty.", Code.INVALID_ARGUMENT);
        }

        if (uri.uEntity().name().isBlank()) {
            return UStatus.failed("UriPart is missing uSoftware Entity name.", Code.INVALID_ARGUMENT);
        }
        return UStatus.ok();
    }

    /**
     * Validate a UriPart that is meant to be used as an RPC method URI. Used in Request sink values and Response source values.
     * @param uri UriPart to validate.
     * @return Returns UStatus containing a success or a failure with the error message.
     */
    public static UStatus validateRpcMethod(UUri uri) {
        UStatus status = validate(uri);
        if (status.isFailed()){
            return status;
        }
        final UResource uResource = uri.uResource();
        if (!uResource.isRPCMethod()) {
            return UStatus.failed("Invalid RPC method uri. UriPart should be the method to be called, or method from response.", Code.INVALID_ARGUMENT);
        }
        return UStatus.ok();
    }

    /**
     * Validate a UriPart that is meant to be used as an RPC response URI. Used in Request source values and Response sink values.
     * 
     * @param uri UriPart to validate.
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
        final UUri uUri = UriSerializer.LONG.deserialize(uri);
        return validate(uUri);
    }

}
