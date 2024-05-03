package org.eclipse.uprotocol.uri.validator;

import org.eclipse.uprotocol.v1.UUri;


/**
 * class for validating Uris.
 */
public interface UriValidator {

    /**
     * The minimum publish/notification topic id for a URI.
     */
    static final int MIN_TOPIC_ID = 0x8000;

    /**
     * The wildcard id for a field.
     */
    static final int WILDCARD_ID = 0xFFFF;

    /**
     * The id for a RPC response URI.
     */
    static final int RPC_RESPONSE_ID = 0;

    /**
     * major version wildcard
     */
    static final int MAJOR_VERSION_WILDCARD = 0xFF;


    /**
     * Indicates that this  URI is an empty as it does not contain authority, entity, and resource.
     *
     * @param uri {@link UUri} to check if it is empty
     * @return Returns true if this  URI is an empty container and has no valuable information in building uProtocol sinks or sources.
     */
    static boolean isEmpty(UUri uri) {
        return (uri == null || uri.equals(UUri.getDefaultInstance()));
    }


    /**
     * Returns true if URI is of type RPC. A UUri is of type RPC if it contains the word rpc in the resource name 
     * and has an instance name and/or the id is less than MIN_TOPIC_ID.
     *
     * @param uri {@link UUri} to check if it is of type RPC method
     * @return Returns true if URI is of type RPC.
     */
    static boolean isRpcMethod(UUri uri) {
        
        return !isEmpty(uri) &&  (uri.getResourceId() < MIN_TOPIC_ID);
    }


    /**
     * Returns true if URI is of type RPC response.
     *
     * @param uri {@link UUri} to check response
     * @return Returns true if URI is of type RPC response.
     */
    static boolean isRpcResponse(UUri uri) {
        return !isEmpty(uri) && uri.getResourceId() == RPC_RESPONSE_ID;
    }
}
