package org.eclipse.uprotocol.ubus.socket;

import java.io.IOException;
import java.net.InetAddress;

import org.eclipse.uprotocol.cloudevent.datamodel.UCloudEventAttributes;
import org.eclipse.uprotocol.cloudevent.factory.CloudEventFactory;
import org.eclipse.uprotocol.cloudevent.serialize.CloudEventSerializer;
import org.eclipse.uprotocol.cloudevent.serialize.CloudEventSerializers;
import org.eclipse.uprotocol.transport.MessageReceiver;
import org.eclipse.uprotocol.transport.socket.SocketClient;
import org.eclipse.uprotocol.ubus.Publisher;
import org.eclipse.uprotocol.uri.datamodel.UEntity;
import org.eclipse.uprotocol.uri.datamodel.UUri;

import com.google.protobuf.Any;
import com.google.rpc.Code;
import com.google.rpc.Status;

import io.cloudevents.CloudEvent;

public class SocketPublisher implements Publisher, MessageReceiver {

    private SocketClient client = null;
    private final UEntity name;
    
    private final CloudEventSerializer serializer = CloudEventSerializers.PROTOBUF.serializer();

    public SocketPublisher(UEntity _name) {
        name = _name;
        try {
            client = new SocketClient(InetAddress.getLocalHost().getHostName(), this);
        } catch (IOException e) {
            System.out.println("IO Exception occured");
        }
    }

    @Override
    public Status publish(UUri topic, byte[] data, UCloudEventAttributes attributes) {
        final CloudEvent ce;
        try {
            ce = CloudEventFactory.publish(topic.uProtocolUri(), Any.parseFrom(data), attributes);
        } catch (Exception e) {
            System.out.println("Couldn't pass bytes to the publish API");
            return Status.newBuilder().setCode(Code.INVALID_ARGUMENT_VALUE).build();
        }
        return client.send(serializer.serialize(ce));
    }

    @Override
    public void onReceive(byte[] message) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onReceive'");
    }
}
