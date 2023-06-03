package org.eclipse.uprotocol.ulink;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;

import org.eclipse.uprotocol.cloudevent.datamodel.UCloudEventAttributes;
import org.eclipse.uprotocol.uri.datamodel.UAuthority;
import org.eclipse.uprotocol.uri.datamodel.UEntity;
import org.eclipse.uprotocol.uri.datamodel.UResource;
import org.eclipse.uprotocol.uri.datamodel.UUri;
import com.google.protobuf.Any;

public class uTestApp implements EventListener {
    
    public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException, InterruptedException {
        UEntity use = new UEntity("Hartley", "1.0");

        MockULink client = new MockULink(use, Executors.newSingleThreadExecutor());

        UUri topic = new UUri(UAuthority.local(), use,
                new UResource("door", "front_left", "Door"));

        // fake payload
        final Any data = Any.pack(io.cloudevents.v1.proto.CloudEvent.newBuilder()
        .setSpecVersion("1.0")
        .setId("hello")
        .setSource("https://example.com")
        .setType("example.demo")
        .setProtoData(Any.newBuilder().build())
        .build());

        final UCloudEventAttributes uCloudEventAttributes = new UCloudEventAttributes.UCloudEventAttributesBuilder()
                .withHash("somehash")
                .withPriority(UCloudEventAttributes.Priority.NETWORK_CONTROL)
                .withTtl(3)
                .withToken("someOAuthToken")
                .build();

        client.publish(topic, data, uCloudEventAttributes);
    }

    @Override
    public void onEvent(UUri topic, Any data, UCloudEventAttributes attributes) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onEvent'");
    }

   
}
