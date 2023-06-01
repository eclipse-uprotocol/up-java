package org.eclipse.uprotocol.transport.socket;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.uprotocol.cloudevent.factory.CloudEventFactory;
import org.eclipse.uprotocol.cloudevent.serialize.CloudEventSerializer;
import org.eclipse.uprotocol.cloudevent.serialize.CloudEventSerializers;
import org.eclipse.uprotocol.receiver.Receiver;
import org.eclipse.uprotocol.transport.Transport;
import com.google.rpc.Code;
import com.google.rpc.Status;

import io.cloudevents.CloudEvent;
import java.io.*;
import java.net.*;

public final class Client implements Transport, Receiver {

    private Socket clientSocket = null;  
    private ObjectOutputStream oos = null;
    private ObjectInputStream ois = null;
    private final CloudEventSerializer serializer = CloudEventSerializers.PROTOBUF.serializer();

    public Client(String hostname, int port) {
        try {
            clientSocket = new Socket(hostname, port);
            oos = new ObjectOutputStream(clientSocket.getOutputStream());
            ois = new ObjectInputStream(clientSocket.getInputStream());
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: " + hostname);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: " + hostname);
        }
    }


    @Override
    public Status send(CloudEvent cloudEvent) {
        try {
            oos.writeObject((serializer.serialize(cloudEvent)));
        } catch (IOException e) {
            System.err.println("IOException:  " + e);
            return Status.newBuilder().setCode(Code.INVALID_ARGUMENT_VALUE).build();
        }
        return Status.newBuilder().setCode(Code.OK_VALUE).build();
    }

    @Override
    public Status registerReceiver(Receiver receiver) {
        throw new UnsupportedOperationException("Unimplemented method 'send'");
    }

    @Override
    public Status unregisterReceiver(Receiver receiver) {
        throw new UnsupportedOperationException("Unimplemented method 'send'");
    }

    @Override
    public CloudEvent[] Receive() {
        ArrayList<CloudEvent> ces = new ArrayList<>(null); 
        try {
            while (ois.available() > 0) {
                ces.add(serializer.deserialize((byte[])ois.readObject()));
            }
        }
        catch (Exception e) {
            System.err.println("IOException:  " + e);
        }
        return (CloudEvent[])ces.toArray();
    }


    @Override
    public void onReceive(CloudEvent ce) {
        throw new UnsupportedOperationException("Unimplemented method 'send'");
    }

    public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException, InterruptedException {
        Client client = new Client("localhost", 9876);
        
    }
    
}
