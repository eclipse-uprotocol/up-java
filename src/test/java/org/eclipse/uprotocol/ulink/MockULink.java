package org.eclipse.uprotocol.ulink;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashSet;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.eclipse.uprotocol.cloudevent.datamodel.UCloudEventAttributes;
import org.eclipse.uprotocol.cloudevent.factory.CloudEventFactory;
import org.eclipse.uprotocol.transport.Receiver;
import org.eclipse.uprotocol.transport.Transport;
import org.eclipse.uprotocol.transport.socket.SocketClient;
import org.eclipse.uprotocol.uri.datamodel.UEntity;
import org.eclipse.uprotocol.uri.datamodel.UUri;
import static org.eclipse.uprotocol.cloudevent.factory.UCloudEvent.getSink;
import static org.eclipse.uprotocol.cloudevent.factory.UCloudEvent.getSource;
import static org.eclipse.uprotocol.cloudevent.factory.UCloudEvent.isExpired;
import static org.eclipse.uprotocol.cloudevent.factory.UCloudEvent.getPayload;
import static org.eclipse.uprotocol.cloudevent.factory.UCloudEvent.getUCloudEventAttributes;
import static org.eclipse.uprotocol.uri.datamodel.UAuthority.local;
import static org.eclipse.uprotocol.uri.datamodel.UResource.empty;
import static org.eclipse.uprotocol.uri.datamodel.UResource.response;
import static org.eclipse.uprotocol.uri.factory.UriFactory.parseFromUri;


import com.google.protobuf.Any;
import com.google.rpc.Code;
import com.google.rpc.Status;

import io.cloudevents.CloudEvent;

/**
 * Mock implementation of the ulink interface
 */
public final class MockULink implements ULink, Receiver {

    static Transport transport;
    private final UUri source;
    private final UUri responseUri; 
    private Executor executor;

    private final class CallbackHandler implements Runnable {
        private final CloudEvent ce;
        public CallbackHandler(@Nonnull CloudEvent _ce) {
            ce = _ce;
        }

        @Override
        public void run() {
            final UUri sink = parseFromUri(getSink(ce).orElse(""));       
            if (!isExpired(ce) && (!sink.isEmpty() && !source.equals(sink))) {                        
                final UUri topic = parseFromUri(getSource(ce));
                final Set<EventListener> listeners = listenerMap.get(topic);
                if (!listeners.isEmpty()) {
                    listeners.forEach(listener -> listener.onEvent(
                        parseFromUri(ce.getSource().toString()), getPayload(ce), getUCloudEventAttributes(ce).get()));
                }
            }
            else {
                //Log.w(TAG, UCloudEvent.toString(event) + " skipped: Expired or wrong sink");
            }
        }
    };


    private final Map<UUri, Set<EventListener>> listenerMap = new HashMap<>();

    public MockULink(UEntity entity, @Nullable Executor _executor) throws IOException {
        try {
            transport = new SocketClient(InetAddress.getLocalHost().getHostName(), this);
        } catch (IOException e) {
            System.out.println("IO Exception creating the Client-side transport connection");
            throw e;
        }
        source = new UUri(local(), entity, empty());;
        responseUri = new UUri(local(), entity, response());
        executor = (_executor != null) ? executor : Executors.newSingleThreadExecutor();
    }


    @Override
    public CompletableFuture<Any> invokeRPC(UUri method, Any data, UCloudEventAttributes attributes) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'invokeRPC'");
    }


    @Override
    public Status publish(UUri topic, Any data, UCloudEventAttributes attributes) {
        final CloudEvent ce = CloudEventFactory.publish(source.uProtocolUri(), data, attributes); 
        if (ce != null) {
            return transport.send(ce);
        }
        return Status.newBuilder().setCode(Code.INVALID_ARGUMENT_VALUE).build();
    }

    @Override
    public Status notify(UUri topic, UUri destination, Any data, UCloudEventAttributes attributes) {
        final CloudEvent ce = CloudEventFactory.notification(source.uProtocolUri(), destination.uProtocolUri(), data, attributes); 
        if (ce != null) {
            return transport.send(ce);
        }
        return Status.newBuilder().setCode(Code.INVALID_ARGUMENT_VALUE).build();
    }


    @Override
    public Status registerEventListener(UUri topic, EventListener listener) {
        try {
            Set<EventListener> listeners = listenerMap.get(topic);
            if (listeners == null) {
                listeners = new HashSet<>();
            }
            if (listeners.isEmpty()) {
                listenerMap.put(topic, listeners);
            }
            listeners.add(listener);
            return Status.newBuilder().setCode(Code.OK_VALUE).build();
        } catch (Exception e) {
            return Status.newBuilder().setCode(Code.INVALID_ARGUMENT_VALUE).build();
        }
    }

    @Override
    public Status unregisterEventListener(UUri topic, EventListener listener) {
        try {
            Set<EventListener> listeners = listenerMap.get(topic);
            if ( (listeners != null) && listeners.contains(listener) ) {
                listeners.remove(listener);
                if (listeners.isEmpty()) {
                    listenerMap.remove(topic);
                }
            }
            
            return Status.newBuilder().setCode(Code.OK_VALUE).build();
        } catch (Exception e) {
            return Status.newBuilder().setCode(Code.INVALID_ARGUMENT_VALUE).build();
        }
    }


    @Override
    public void onReceive(@Nonnull CloudEvent ce) {
        if (executor != null) {
            executor.execute(new CallbackHandler(ce));
        }
    }
    
}
