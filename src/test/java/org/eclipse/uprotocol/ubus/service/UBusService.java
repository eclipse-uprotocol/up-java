package org.eclipse.uprotocol.ubus.service;

import java.io.IOException;
import java.net.UnknownHostException;

import javax.annotation.Nonnull;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.uprotocol.transport.Receiver;
import org.eclipse.uprotocol.transport.socket.SocketServer;


import io.cloudevents.CloudEvent;

public final class UBusService implements Receiver {
    
    private final SocketServer server;
    
    public UBusService() throws IOException {
        server = new SocketServer(this);
        System.out.println("Server Initialized");
    }


    @Override
    public void onReceive(@Nonnull CloudEvent ce) {
        System.out.println("We received something from a client:" + ce.toString());
    }



    public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException, InterruptedException {
        UBusService ubus = new UBusService();

        for(;;) {
            Thread.sleep(100);
        }
    }
}
