package org.eclipse.uprotocol.communication;

import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.Test;

import org.eclipse.uprotocol.v1.UUri;

public class RpcClientExampleTest {
    @Test
    public void testInvokeMethod() {
        final UUri method = UUri.newBuilder()
            .setUeId(4).setUeVersionMajor(1).setResourceId(3)
            .build();
        TestUTransport transport = new TestUTransport();
        RpcClient rpcClient = UClient.create(transport);
        
        // Invoke command without payload, what is returned is a response 
        assertFalse(rpcClient.invokeMethod(method, UPayload.EMPTY, CallOptions.DEFAULT)
            .toCompletableFuture().isCompletedExceptionally());
    }
}
