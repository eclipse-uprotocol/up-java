package org.eclipse.uprotocol.ubus;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.uprotocol.transport.Receiver;
import org.eclipse.uprotocol.transport.Transport;
import org.eclipse.uprotocol.uri.datamodel.UUri;

import com.google.protobuf.Any;
import com.google.rpc.Status;

import io.cloudevents.CloudEvent;


/* 
 * Helper class that is used by uApps and uServices to wrap
 * the transport layer for Invoking methods, publishing events, etc...
 */
public class UBus {

    private Transport mTransport;
    private final Map<UUri, Receiver> mReceivers = new HashMap<>();

    public UBus(Transport t) {
        mTransport = t;
    }

    /**
     * Support for RPC method invocation.
     * @param requestEvent req.v1 CloudEvent.
     * @return Returns the CompletableFuture with the result or exception. Tamara would rather have a CompletionStage since she likes it better
     *  when you program to an interface and not an implementation.
     */
    CompletableFuture<Any> invokeMethod(CloudEvent requestEvent) {
        /* See Ultifi Link for reference impl */
        return CompletableFuture.failedFuture(new Throwable("TODO"));
    }

    Status send(CloudEvent ce) {
        return mTransport.send(ce);
    }

}

