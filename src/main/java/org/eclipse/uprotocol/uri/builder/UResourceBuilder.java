package org.eclipse.uprotocol.uri.builder;

import java.util.Objects;

import org.eclipse.uprotocol.v1.UResource;

public interface UResourceBuilder {

    static final int MAX_RPC_ID = 1000;

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
        return forRpcRequest(method, null);
    }

    /**
     * Builds a UResource for an RPC request with an ID and method name
     * @param method The method to be invoked.
     * @param id The ID of the request.
     * @return Returns a UResource for an RPC request.
     */
    static UResource forRpcRequest(String method, Integer id) {
        UResource.Builder builder = UResource.newBuilder().setName("rpc");
        
        if (method != null) {
            builder.setInstance(method);
        }
        if (id != null) {
            builder.setId(id);
        }

        return builder.build();
    }

    /**
     * Builds a UResource for an RPC request with an ID
     * @param id The ID of the request.
     * @return Returns a UResource for an RPC request.
     */
    static UResource forRpcRequest(Integer id) {
        return forRpcRequest(null, id);

    }

    /**
     * Build a UResource from an ID. This method will determine if
     * the id is a RPC or topic ID based on the range
     * @param id The ID of the request.
     * @return Returns a UResource for an RPC request.
     */
    static UResource fromId(Integer id) {
        Objects.requireNonNull(id, "id cannot be null");
        
        return (id < MAX_RPC_ID) ? forRpcRequest(id) : UResource.newBuilder().setId(id).build();
    }

}
