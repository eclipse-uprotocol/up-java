/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.eclipse.uprotocol.transport.socket;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

public final class Server implements Transport, Receiver {

    ExecutorService executor = Executors.newCachedThreadPool();
    private ServerSocket server = null; 
    
    private ObjectOutputStream oos = null;
    private ObjectInputStream ois = null;
    private final CloudEventSerializer serializer = CloudEventSerializers.PROTOBUF.serializer();

    public Server(String hostname, int port) {
        try {
            server = new ServerSocket(port);
    
            // For now only one client, this is only a mock/test
            executor.execute(() -> {
                try {
                    Socket socket = server.accept();
                    //creating socket and waiting for client connection
                    oos = new ObjectOutputStream(socket.getOutputStream());

                    //read from socket to ObjectInputStream object
                    ois = new ObjectInputStream(socket.getInputStream());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: " + hostname);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: " + hostname);
        }
    }


    @Override
    public Status send(CloudEvent cloudEvent) {
        try {
            if (oos != null) {
                oos.writeObject(serializer.serialize(cloudEvent));
            }
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
        try {
            ArrayList<CloudEvent> ces = new ArrayList<>();
            while ((ois != null) && (ois.available() > 0)) {
                ces.add(serializer.deserialize((byte[])ois.readObject()));
            }
            return (CloudEvent[])ces.toArray();

        } catch (Exception e) {
            System.err.println("Input error ");
        }
        return null;
    }



    @Override
    public void onReceive(CloudEvent ce) {
        throw new UnsupportedOperationException("Unimplemented method 'send'");
    }

    public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException, InterruptedException {
        Client client = new Client("localhost", 9876);
        
    }
    
}
