package org.eclipse.uprotocol.ubus.socket;

import java.io.IOException;
import java.net.UnknownHostException;

import org.eclipse.uprotocol.cloudevent.datamodel.UCloudEventAttributes;
import org.eclipse.uprotocol.uri.datamodel.UAuthority;
import org.eclipse.uprotocol.uri.datamodel.UEntity;
import org.eclipse.uprotocol.uri.datamodel.UResource;
import org.eclipse.uprotocol.uri.datamodel.UUri;

import com.google.protobuf.Any;

public class uService {
    
    
    public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException, InterruptedException {
        UEntity use = new UEntity("HartleyApp", "1.0");
        
        //SocketUBus uBus = new SocketUBus();

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

       // uBus.registerEventListener(topic, listener);

       // uBus.send(topic, data.toByteArray(), uCloudEventAttributes);
    }

}