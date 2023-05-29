package org.eclipse.uprotocol.transport.socket;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import org.eclipse.uprotocol.cloudevent.serialize.*;
import org.eclipse.uprotocol.transport.*;

import com.google.rpc.Code;
import com.google.rpc.Status;

import io.cloudevents.CloudEvent;

public class SocketTransport implements Transport{
    private Socket clientSocket = null;  
    private DataOutputStream os = null;
    private BufferedReader is = null;
    private final CloudEventSerializer serializer = CloudEventSerializers.PROTOBUF.serializer();
    
    public SocketTransport(String hostname, int port) {
        try {
            clientSocket = new Socket(hostname, port);
            os = new DataOutputStream(clientSocket.getOutputStream());
            is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: " + hostname);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: " + hostname);
        }
    }


    public final Status send(CloudEvent cloudEvent) {
        try {
            os.writeBytes(new String(serializer.serialize(cloudEvent)));
        } catch (IOException e) {
            System.err.println("IOException:  " + e);
            return Status.newBuilder().setCode(Code.INVALID_ARGUMENT_VALUE).build();
        }
        return Status.newBuilder().setCode(Code.OK_VALUE).build();
    }
  

    public final Status registerReceiver(Receiver listener) {
        return Status.newBuilder().setCode(Code.UNIMPLEMENTED_VALUE).build();
    }


    public final ArrayList<CloudEvent> Receive() {
        ArrayList<CloudEvent> events = new ArrayList<CloudEvent>();
        String inStr;
        try {
            while (clientSocket.isConnected() && ((inStr = is.readLine()) != null))
            {
                events.add(serializer.deserialize(inStr.getBytes()));      
            }
        } catch (IOException e) {
            System.err.println("IOException:  " + e);
        }
        return events;
    }

}
