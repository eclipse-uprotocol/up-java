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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Nonnull;

import org.eclipse.uprotocol.transport.MessageReceiver;
import org.eclipse.uprotocol.transport.Transport;

import com.google.rpc.Code;
import com.google.rpc.Status;

import java.io.*;
import java.net.*;

public class SocketClient implements Transport {

    private Socket socket = null;  
    private final ObjectOutputStream oos;
    @Nonnull private final ObjectInputStream ois;
    private ExecutorService executor = Executors.newCachedThreadPool();

    public SocketClient(Socket _socket, @Nonnull MessageReceiver receiver) throws IOException {
        socket = _socket;
        oos = new ObjectOutputStream(socket.getOutputStream());
        ois = new ObjectInputStream(socket.getInputStream());

        executor.execute(new SocketReader(ois, receiver));
    }

    public SocketClient(String hostname, @Nonnull MessageReceiver receiver) throws IOException {
        this(new Socket(hostname, SocketServer.PORT), receiver);
    }


    @Override
    public Status send(@Nonnull byte[] message) {
        try {
            oos.writeObject(message);
        } catch (IOException e) {
            System.err.println("IOException:  " + e);
            return Status.newBuilder().setCode(Code.INVALID_ARGUMENT_VALUE).build();
        }
        return Status.newBuilder().setCode(Code.OK_VALUE).build();
    }


    @Override
    public byte[] receive() {
        try {
            if (ois.available() > 0) {
                return (((byte[])ois.readObject()));
            }
        }
        catch (Exception e) {
            System.err.println("IOException:  " + e);
        }
        return null;
    }
}
