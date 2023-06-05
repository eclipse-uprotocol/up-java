package org.eclipse.uprotocol.sdk;

import java.io.IOException;
import java.net.UnknownHostException;

import org.eclipse.uprotocol.cloudevent.datamodel.UCloudEventAttributes;
import org.eclipse.uprotocol.uri.datamodel.UAuthority;
import org.eclipse.uprotocol.uri.datamodel.UEntity;
import org.eclipse.uprotocol.uri.datamodel.UResource;
import org.eclipse.uprotocol.uri.datamodel.UUri;

import com.google.protobuf.Any;
import com.google.rpc.Status;

public class uApp {
    
    private class AppEventListener implements EventListener {

        @Override
        public Status onEvent(UUri topic, byte[] data, UCloudEventAttributes attributes) {
            throw new UnsupportedOperationException("Unimplemented method 'onEvent'");
        }

    };
    
    public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException, InterruptedException {
        UEntity use = new UEntity("HartleyApp", "1.0");
        AppEventListener listener;

        UUri topic = new UUri(UAuthority.local(), use,
                new UResource("hello", "world", "dummy"));

        // fake payload
        final Any data = Any.newBuilder().build();

        final UCloudEventAttributes uCloudEventAttributes = new UCloudEventAttributes.UCloudEventAttributesBuilder()
                .withHash("somehash")
                .withPriority(UCloudEventAttributes.Priority.NETWORK_CONTROL)
                .withTtl(3)
                .withToken("someToken")
                .build();
/*
        uBus.registerEventListener(topic, listener);

        uBus.send(topic, data.toByteArray(), uCloudEventAttributes);
        */
    }

}
