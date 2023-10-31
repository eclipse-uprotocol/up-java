package org.eclipse.uprotocol.uri.builder;

import org.eclipse.uprotocol.v1.UResource;

public interface UResourceBuilder {

    /**
     * Builds a UResource for an RPC response.
     * @return Returns a UResource for an RPC response.
     */
    static UResource forRpcResponse() {
        return UResource.newBuilder()
            .setName("rpc")
            .setInstance("response")
            .setId(0)
            .build();
    }


    /**
     * Builds a UResource for an RPC request.
     * @param method The method to be invoked.
     * @return Returns a UResource for an RPC request.
     */
    static UResource forRpcRequest(String method) {
        return UResource.newBuilder()
            .setName("rpc")
            .setInstance(method)
            .build();
    }
    
}
