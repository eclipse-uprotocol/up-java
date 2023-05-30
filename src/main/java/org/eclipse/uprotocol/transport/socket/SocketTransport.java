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

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import org.eclipse.uprotocol.cloudevent.serialize.*;
import org.eclipse.uprotocol.transport.*;
import org.eclipse.uprotocol.utils.Receiver;

import static org.eclipse.uprotocol.utils.StatusUtils.throwableToStatus;
import static org.eclipse.uprotocol.utils.StatusUtils.buildStatus;

import com.google.rpc.Code;
import com.google.rpc.Status;

import io.cloudevents.CloudEvent;

public class SocketTransport implements Transport {
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


    public final CloudEvent[] Receive() {
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
        return (CloudEvent[])events.toArray();
    }

         /*
	  * Register message Receier
      * When the transport supports push type delivery method, the caller invokes this
	  * method to register a listener to receive the events 
	  * @param receiver The message reciver
	  * @return Status The result from the send()
	 */
	public Status registerReceiver(Receiver receiver) {
        return buildStatus(Code.UNIMPLEMENTED);
    }


    /*
	 * Unregister Message Receier
     * When the transport supports push type delivery method, the caller invokes this
	 * method to unregister a message Receiver to receive the events 
	 * @param receiver to unregister
	 * @return Status The result from the send()
	 */
	public Status unregisterReceiver(Receiver receiver) {
        return buildStatus(Code.UNIMPLEMENTED);
    }

}
