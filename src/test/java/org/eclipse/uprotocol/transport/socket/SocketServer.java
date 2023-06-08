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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Nonnull;

import org.eclipse.uprotocol.transport.MessageReceiver;
import org.eclipse.uprotocol.transport.Transport;

import com.google.rpc.Code;
import com.google.rpc.Status;

import io.cloudevents.CloudEvent;
import java.io.*;
import java.net.*;

/**
 * Server-side implementation of the Socket transport.
 * 
 * The following supports on;t a single client until we have a way of identiying
 * the client with each connection so that we can route events correctly.
 */
public final class SocketServer implements Transport {

    public static int PORT = 9876;

    private ExecutorService executor = Executors.newCachedThreadPool();
    private ServerSocket server = null; 

    private ArrayList<SocketClient> clients = new ArrayList<>();

    public SocketServer(@Nonnull MessageReceiver receiver) throws IOException {
        try {
            server = new ServerSocket(PORT);

            executor.execute(() -> {
                try {
                    System.err.println("Waiting or Clients to connect");
                    Socket socket = server.accept();

                    System.err.println("Received connection!");
                    SocketClient client = new SocketClient(socket, receiver);
                    clients.add(client);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection ");
            throw e;
        }
    }


    @Override
    public Status send(@Nonnull byte[] message) {

        if (clients.size() > 0) {
            return clients.get(0).send(message);
        }
        return Status.newBuilder().setCode(Code.INVALID_ARGUMENT_VALUE).build();
    }


    @Override
    public byte[] receive() {
        try {
            if (clients.size() > 0) {
                return clients.get(0).receive();
            }
        } catch (Exception e) {
            System.err.println("Input error ");
        }
        return null;
    }
}